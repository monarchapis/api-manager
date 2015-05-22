/*
 * Copyright (C) 2015 CapTech Ventures, Inc.
 * (http://www.captechconsulting.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monarchapis.apimanager.security

import java.util.UUID

import scala.collection.JavaConversions._
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable
import scala.util.matching.Regex

import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Value

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._

import grizzled.slf4j.Logging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import javax.inject.Inject

object AuthenticationProcessorImpl {
  lazy val (apiKeyExtractorRegistry, accessTokenExtractorRegistry) = {
    val contextProvider = ApplicationContextProvider()
    val apiKeyExtractorRegistry = contextProvider.getBean("apiKeyExtractorRegistry").asInstanceOf[StringExtractorRegistry]
    val accessTokenExtractorRegistry = contextProvider.getBean("accessTokenExtractorRegistry").asInstanceOf[StringExtractorRegistry]
    (apiKeyExtractorRegistry, accessTokenExtractorRegistry)
  }

  val providerApplication = new Application(
    id = "provider",
    name = "provider",
    applicationUrl = "provider",
    companyName = "provider",
    companyUrl = "provider")

  private val OBJECT_MAPPER = {
    val mapper = new ObjectMapper

    val jodaModule = new JodaModule
    mapper.registerModule(jodaModule)

    mapper.registerModule(DefaultScalaModule)
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    mapper
  }
}

class AuthenticationProcessorImpl(
  applicationService: ApplicationService,
  permissionService: PermissionService,
  tokenService: TokenService,
  planService: PlanService,
  rateLimitService: RateLimitService,
  serviceService: ServiceService,
  authenticatorRegistry: AuthenticatorRegistry,
  policyRegistry: PolicyRegistry,
  claimSourceRegistry: ClaimSourceRegistry,
  versionExtractor: VersionExtractor,
  accessTokenTypes: Set[String] = Set()) extends AuthenticationProcessor with Logging {

  @Inject def this(
    applicationService: ApplicationService,
    permissionService: PermissionService,
    tokenService: TokenService,
    planService: PlanService,
    rateLimitService: RateLimitService,
    serviceService: ServiceService,
    authenticatorRegistry: AuthenticatorRegistry,
    policyRegistry: PolicyRegistry,
    claimSourceRegistry: ClaimSourceRegistry,
    versionExtractor: VersionExtractor,
    accessTokenTypes: java.util.Set[String]) = this(
    applicationService,
    permissionService,
    tokenService,
    planService,
    rateLimitService,
    serviceService,
    authenticatorRegistry,
    policyRegistry,
    claimSourceRegistry,
    versionExtractor,
    if (accessTokenTypes != null) accessTokenTypes.toSet else null)

  import AuthenticationProcessorImpl._
  import scala.util.control.Breaks._
  import ClaimNames._

  @Value("${jwt.base64Key}")
  private val jwtBase64Key: String = null

  private lazy val jwtKey = Base64.decodeBase64(jwtBase64Key)

  private val loadBalancer: Option[LoadBalancer] = {
    try {
      Some(ApplicationContextProvider().getBean(classOf[LoadBalancer]))
    } catch {
      case e: NoSuchBeanDefinitionException => None
    }
  }

  private val uriConverter = new UriRegexConverter

  def authenticate(keyStore: KeyStore, request: AuthenticationRequest): AuthenticationResponse = {
    require(keyStore != null, "keyStore is required.")
    require(request != null, "request is required.")

    val (service, operationMatch) = if (request.performAuthorization) {
      val service = getService(request)
      val operation = service match {
        case Some(service) => getOperation(service, request)
        case None => None
      }
      (service, operation)
    } else {
      (None, None)
    }

    var variableContext = operationMatch match {
      case Some(operationMatch) => {
        val operation = operationMatch._1
        val regexMatch = operationMatch._2
        val pathParameters = uriConverter.getPathParameters(regexMatch)
        val version = versionExtractor.extractVersion(request, operation, pathParameters)

        Some(VariableContext(
          ProviderHolder.current.id,
          Some(operation.serviceId),
          version,
          Some(operation.operationName),
          Some(pathParameters)))
      }
      case _ => if (ProviderHolder.isSet)
        Some(VariableContext(
          ProviderHolder.current.id))
      else None
    }

    var context = AuthenticationContext(
      client = None,
      token = None,
      principal = None)

    val responseHeaders = List.newBuilder[HttpHeader]

    try {
      breakable {
        authenticatorRegistry.names foreach { name =>
          {
            try {
              val authenticator = authenticatorRegistry(name).get
              authenticator.authenticate(keyStore, request) match {
                case Left(authenticationContext) => {
                  context = authenticationContext
                  break
                }
                case Right(headers) => responseHeaders ++= headers
              }
            } catch {
              case ue: UnauthorizedException => responseHeaders ++= ue.responseHeaders
              case iae: InvalidAuthenticationException => {
                responseHeaders ++= iae.responseHeaders
                throw iae
              }
            }
          }
        }
      }

      context.client match {
        case Some(client) => {
          if (!client.enabled) {
            debug(s"This API key $client.apiKey is disabled.")
            throw new InvalidAuthenticationException("This API key is disabled.")
          }

          context.token match {
            case Some(token) => {
              if (token.clientId != client.id) {
                debug("The access token is not associated with this client.")
                throw new InvalidSignatureException("The access token is not associated with this client.")
              }

              if (!accessTokenTypes(token.tokenType)) {
                debug("The access token is a valid type for authentication.")
                throw new InvalidSignatureException("The access token is a valid type for authentication.")
              }

              if (token.isExpired) {
                debug("The access token has expired.")
                throw new InvalidSignatureException("The access token has expired.")
              }

              // If the life cycle is session, keep the token from expiring.
              if (token.lifecycle == "session") {
                tokenService.touch(token)
              }
            }
            case _ =>
          }

          var verified = true

          breakable {
            client.policies foreach { config =>
              policyRegistry(config.name) match {
                case Some(policy) => {
                  if (!policy.verify(config, request, context)) {
                    verified = false
                    break
                  }
                }
                case _ => {
                  warn(s"Could not find verifier named ${config.name}.")
                  verified = false
                  break
                }
              }
            }
          }

          if (!verified) {
            throw new VerificationException(s"The request failed verification.")
          }

          val applicationId = client.applicationId;

          val application = if (applicationId == "provider") providerApplication else {
            val _application = applicationService.load(applicationId)

            if (_application.isEmpty) {
              debug("Could not find associated application.")
              throw new InvalidCredentialsException()
            }

            _application.get
          }

          val (token, permissionIds) = context.token match {
            case Some(token) => {
              val permissionIds = if (token.scheme.isDefined) {
                client.getPermissionSet(token.scheme.get) match {
                  case Some(permissionSet) => if (permissionSet.global) {
                    permissionSet.permissionIds
                  } else {
                    token.permissionIds
                  }
                  case _ => token.permissionIds
                }
              } else token.permissionIds

              val tokenContext = Some(TokenContext(
                id = token.id,
                permissions = permissionService.getPermissionNames(permissionIds, "user"),
                extended = token.extended))

              (tokenContext, Some(permissionIds))
            }
            case _ => (None, None)
          }

          val principal = context.token match {
            case Some(token) => if (token.userId.isDefined) {
              Some(
                PrincipalContext(
                  id = token.userId.get,
                  context = token.userContext,
                  claims = {
                    val builder = Set.newBuilder[Claim]

                    if (context.claims.isDefined) {
                      builder ++= context.claims.get
                    }

                    if (client.isInstanceOf[Client]) {
                      val claimSources = client.asInstanceOf[Client].claimSources

                      for (config <- claimSources) {
                        claimSourceRegistry(config.name) match {
                          case Some(claimSource) => {
                            val claims = claimSource.getClaims(config, request, context)
                            builder ++= claims
                          }
                          case _ => {
                            warn(s"Could not find claim source named ${config.name}.")
                            verified = false
                            break
                          }
                        }
                      }
                    }

                    val claims = builder.result

                    val mapPrep = new collection.mutable.HashMap[String, collection.mutable.Set[String]]

                    for (claim <- claims) {
                      var set = mapPrep.get(claim.`type`) match {
                        case Some(set) => set
                        case _ => {
                          val set = new collection.mutable.HashSet[String]
                          mapPrep += claim.`type` -> set
                          set
                        }
                      }

                      set += claim.value
                    }

                    mapPrep map (c => c._1 -> c._2.toSet) toMap
                  }))
            } else { None }
            case _ => None
          }

          val provider = ProviderHolder.get

          val applicationContext = ApplicationContext(
            id = application.id,
            name = Some(application.name),
            extended = application.extended)

          val clientContext = ClientContext(
            id = client.id,
            label = client.label,
            permissions = if (client.isInstanceOf[Client]) {
              permissionService.getPermissionNames(client.asInstanceOf[Client].clientPermissionIds, "client")
            } else { Set.empty[String] },
            extended = if (client.isInstanceOf[Client]) client.asInstanceOf[Client].extended else Map())

          val providerContext = provider match {
            case Some(provider) => Some(ProviderContext(
              id = provider.id,
              label = provider.label))
            case _ => None
          }

          val builder = Map.newBuilder[String, Any]

          val now = System.currentTimeMillis / 1000
          builder += Claims.ID -> StringUtils.replace(UUID.randomUUID.toString, "-", "")
          builder += Claims.ISSUER -> "http://www.monarchapis.com/api-manager"
          builder += Claims.AUDIENCE -> "http://www.monarchapis.com/api-producer"
          builder += Claims.ISSUED_AT -> now
          builder += Claims.EXPIRATION -> (now + 60)

          principal match {
            case Some(principal) => {
              builder += Claims.SUBJECT -> principal.id
              builder += PRINCIPAL -> Map(
                "id" -> principal.id,
                "context" -> principal.context)

              for (claim <- principal.claims) {
                builder += claim._1 -> claim._2
              }
            }
            case None =>
          }

          token match {
            case Some(token) => builder += TOKEN -> token
            case None =>
          }

          builder += APPLICATION -> applicationContext
          builder += CLIENT -> clientContext

          providerContext match {
            case Some(context) => builder += PROVIDER -> context
            case None =>
          }

          val claims = builder.result

          if (request.performAuthorization) {
            if (service.isDefined) {
              if (service.get.accessControl) {
                operationMatch match {
                  case Some(om) => {
                    val om = operationMatch.get
                    val oper = om._1

                    if (!oper.delegatedPermissionIds.isEmpty && token.isEmpty) {
                      throw new InvalidAccessTokenException("Your request did not contain a valid access token.")
                    }

                    var valid =
                      (oper.clientPermissionIds.size == 0 && oper.delegatedPermissionIds.size == 0 && oper.claims.size == 0) ||
                        (containsAny(oper.clientPermissionIds, client.clientPermissionIds) &&
                          containsAny(oper.delegatedPermissionIds, permissionIds.getOrElse(Set.empty[String])) &&
                          containsAny(oper.claims, principal match {
                            case Some(principal) => principal.claims
                            case _ => Map.empty[String, Set[String]]
                          }))

                    if (!valid) {
                      throw new UnauthorizedOperationException()
                    }
                  }
                  case _ => throw new NotFoundException()
                }
              }
            } else {
              throw new NotFoundException()
            }
          }

          // Set this for security checks with the APIs
          if (client.isInstanceOf[Authorization]) {
            AuthorizationHolder.current(client.asInstanceOf[Authorization])
          }

          if (client.isInstanceOf[Provider]) {
            ProviderHolder.current(client.asInstanceOf[Provider])
            BehindReverseProxyHolder.current(client.asInstanceOf[Provider].behindReverseProxy)
          }

          if (client.isRateLimited && !request.bypassRateLimiting) {
            try {
              val quotas = application.planId match {
                case Some(planId) => planService.load(planId) match {
                  case Some(plan) => plan.quotas
                  case _ => List()
                }
                case _ => List()
              }

              rateLimitService.checkQuotas(
                application.id,
                request.requestWeight,
                quotas)
            } catch {
              case e: Exception => {
                return AuthenticationResponse(
                  403,
                  Some("rateLimitExceeded"),
                  Some("Access denied"),
                  Some(e.getMessage),
                  Some("LIM-0001"),
                  List(),
                  variableContext,
                  Some(claims),
                  Map.empty[String, String],
                  None)
              }
            }
          }

          val target = if (service.isDefined && request.useLoadBalancer) {
            val target = loadBalancer match {
              case Some(loadBalancer) => loadBalancer.getTarget(service.get)
              case None => None
            }

            if (target.isEmpty) {
              error(s"No available target for service ${service.get.name}")

              return AuthenticationResponse(
                503,
                Some("systemUnavailable"),
                Some("The system is currently unavailable"),
                Some("Please try again later."),
                Some("SYS-0001"),
                List(),
                variableContext,
                Some(claims),
                Map.empty[String, String],
                None)
            }
            target
          } else {
            None
          }

          val tokens = if (client.isInstanceOf[Client]) {
            val jwtBuilder = Jwts.builder.setPayload(OBJECT_MAPPER.writeValueAsString(claims))
            jwtBuilder.signWith(SignatureAlgorithm.HS256, jwtKey)

            var jwt = jwtBuilder.compact

            Map("jwt" -> jwt)
          } else {
            Map.empty[String, String]
          }

          AuthenticationResponse(200, None, None, None, None, List(),
            variableContext, Some(claims), tokens, target)
        }
        case _ => {
          if (operationMatch.isDefined) {
            val om = operationMatch.get
            val oper = om._1

            // Return OK if the resource operation is unprotected
            if (oper.clientPermissionIds.isEmpty && oper.delegatedPermissionIds.isEmpty && oper.claims.isEmpty) {
              return AuthenticationResponse(200, None, None, None, None, List(),
                variableContext, None, Map.empty[String, String], None)
            }
          }

          throw new CredentialsNotSuppliedException();
        }
      }
    } catch {
      case ae: AuthenticationException => {
        val provider = ProviderHolder.get

        val builder = Map.newBuilder[String, Any]

        val now = System.currentTimeMillis / 1000
        builder += Claims.ID -> StringUtils.replace(UUID.randomUUID.toString, "-", "")
        builder += Claims.ISSUED_AT -> now
        builder += Claims.EXPIRATION -> (now + 60)

        context.client match {
          case Some(client) => {
            builder += APPLICATION -> ApplicationContext(
              id = client.applicationId)
            builder += CLIENT -> ClientContext(
              id = client.id,
              label = client.label)
          }
          case _ =>
        }

        provider match {
          case Some(provider) => builder += PROVIDER -> provider
          case _ =>
        }

        val claims = builder.result

        AuthenticationResponse(
          ae.code,
          Some(ae.reason),
          Some(ae.getMessage),
          Some(ae.developerMessage),
          Some(ae.errorCode),
          responseHeaders.result,
          variableContext,
          Some(claims),
          Map.empty[String, String],
          None)
      }
    }
  }

  private def getService(request: AuthenticationRequest) = {
    var sortedServices = serviceService.getAccessControlled.sortBy(
      service => uriConverter.getPatternLength(service.uriPrefix.getOrElse("")) * -1)

    sortedServices find { service =>
      {
        (service.hostnames.size == 0 || service.hostnames.contains(request.host)) &&
          (service.uriPrefix.isDefined && {
            val regex = uriConverter.convertToRegex(service.uriPrefix.get, true)
            regex.findFirstMatchIn(request.path).isDefined
          })
      }
    }
  }

  private def getOperation(service: Service, request: AuthenticationRequest): Option[(OperationMatcher, Regex.Match)] = {
    val allOperations = List.newBuilder[OperationMatcher]

    allOperations ++= service.operations map { operation =>
      OperationMatcher(
        service.id,
        service.name,
        service.versionLocation match {
          case Some(location) => StringUtils.split(location, ", ").toSeq
          case _ => Seq.empty[String]
        },
        service.defaultVersion,
        operation.name,
        operation.method,
        uriConverter.convertToRegex(service.uriPrefix.getOrElse("") + operation.uriPattern),
        uriConverter.getPatternLength(operation.uriPattern),
        operation.clientPermissionIds,
        operation.delegatedPermissionIds,
        operation.claims)
    }

    val operations = allOperations.result.sortBy(operation => operation.patternLength * -1)

    operations foreach { operation =>
      {
        if (operation.method == request.method) {
          val regexMatch = operation.uriPattern.findFirstMatchIn(request.path)

          if (regexMatch.isDefined) {
            return Some((operation, regexMatch.get))
          }
        }
      }
    }

    None
  }

  private def containsAny(anyOf: Set[String], values: Set[String]): Boolean = {
    if (anyOf.size == 0 || anyOf.contains("*") && values.size > 0) {
      return true
    }

    anyOf foreach { value =>
      {
        if (values.contains(value)) {
          return true
        }
      }
    }

    false
  }

  private def containsAny(anyOf: Set[ClaimEntry], values: Map[String, Set[String]]): Boolean = {
    if (anyOf.size == 0 || anyOf.contains("*") && values.size > 0) {
      return true
    }

    anyOf foreach { entry =>
      {
        entry.value match {
          case Some(v) => {
            values.get(entry.`type`) match {
              case Some(valueSet) => if (valueSet.contains(v)) return true
              case _ =>
            }
          }
          case _ => if (values.containsKey(entry.`type`)) return true
        }
      }
    }

    false
  }
}