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

package com.monarchapis.apimanager.servlet

import java.io.IOException
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils
import com.monarchapis.apimanager.security._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._
import grizzled.slf4j.Logging
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.cache.CacheManager

object ApiFilter {
  val (environmentService, providerService, userService, authenticationService, roleService, authenticationProcessor, requestHasherRegistry, cacheManager) = {
    val applicationContextProvider = ApplicationContextProvider()
    val environmentService = applicationContextProvider.getBean(classOf[EnvironmentService])
    val providerService = applicationContextProvider.getBean(classOf[ProviderService])
    val userService = applicationContextProvider.getBean(classOf[UserService])
    val authenticationService = applicationContextProvider.getBean(classOf[AuthenticationService])
    val roleService = applicationContextProvider.getBean(classOf[RoleService])
    val authenticationProcessor = applicationContextProvider.getBean(classOf[AuthenticationProcessor])
    val requestHasherRegistry = applicationContextProvider.getBean(classOf[RequestHasherRegistry])
    val cacheManager = applicationContextProvider.getBean(classOf[CacheManager])
    (environmentService, providerService, userService, authenticationService, roleService, authenticationProcessor, requestHasherRegistry, cacheManager)
  }

  private val CACHE_NAME = "credentialCache"
}

class ApiFilter extends Filter with Logging {
  import ApiFilter._

  @throws(classOf[IOException])
  @throws(classOf[ServletException])
  def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
    try {
      val request = req.asInstanceOf[HttpServletRequest]
      val response = res.asInstanceOf[HttpServletResponse]

      val uri = request.getRequestURL.toString

      if (uri.endsWith("/swagger.json") || uri.endsWith("/swagger.yaml")) {
        chain.doFilter(request, response)
        return
      }

      AuthorizationUtils.continueWithAuthenticationAccess

      val apiRequest: ApiRequest = new ApiRequestImpl(request)
      ApiRequest.current(apiRequest)

      setEnvironmentContext(request)
      var authenticated = authenticateUser(request)

      if (!authenticated) {
        val (success, authResponse) = authenticateProvider(apiRequest)
        authenticated = success

        if (!authenticated) {
          handleNotAuthenticatedResponse(response, authResponse)
        }
      }

      if (authenticated) {
        chain.doFilter(apiRequest, response)
      }
    } finally {
      UserContext.remove
      AuthorizationHolder.remove
      ProviderHolder.remove
      BehindReverseProxyHolder.remove
      EnvironmentContext.remove
      ApiRequest.remove
    }
  }

  @throws(classOf[ServletException])
  def init(config: FilterConfig) {
  }

  def destroy() {
  }

  private def setEnvironmentContext(request: HttpServletRequest) {
    val environmentId = StringUtils.trimToNull(request.getHeader("X-Environment-Id"));

    if (environmentId != null) {
      val databases = environmentService.getDatabases(environmentId)

      databases match {
        case Some(value) => EnvironmentContext.current(
          EnvironmentContext(environmentId, value.system, value.analytics))
        case _ =>
      }
    }
  }

  private def authenticateUser(request: HttpServletRequest) = {
    val authorizationHeader = request.getHeader("Authorization")

    if (authorizationHeader != null && authorizationHeader.startsWith("Basic ")) {
      val base64 = authorizationHeader.substring(6).trim
      val bytes = Base64.decodeBase64(base64)
      val creds = new String(bytes, "UTF-8")
      val parts = StringUtils.split(creds, ':')

      if (parts.length == 2) {
        authenticateUsingCache(parts(0), parts(1)) match {
          case Some(authenticatedUser) => {
            val user = if (authenticationService.isLocal) {
              userService.findByName(authenticatedUser.userName).get
            } else {
              userService.findByExternalId(authenticatedUser.id) match {
                case Some(user) => {
                  var adjustedUser = user

                  // Check if name needs to be updated, otherwise just return.
                  if (user.userName != authenticatedUser.userName ||
                    user.firstName != authenticatedUser.firstName ||
                    user.lastName != authenticatedUser.lastName) {
                    adjustedUser = user
                      .withUserName(authenticatedUser.userName)
                      .withFirstName(authenticatedUser.firstName)
                      .withLastName(authenticatedUser.lastName)
                    userService.update(adjustedUser)
                  }

                  if (user.administrator != authenticatedUser.administrator) {
                    userService.setAdmininstrator(user.id, authenticatedUser.administrator)
                    adjustedUser = userService.load(user.id).getOrElse(user)
                  }

                  adjustedUser
                }
                case _ => {
                  val user = User(
                    userName = authenticatedUser.userName,
                    firstName = authenticatedUser.firstName,
                    lastName = authenticatedUser.lastName,
                    externalId = Some(authenticatedUser.id))
                  userService.create(user)
                }
              }
            }

            UserContext.current(user.userName)
            val fullName = s"${user.firstName} ${user.lastName}"

            if (EnvironmentContext.isSet) {
              roleService.getUserRole(user) match {
                case Some(role) =>
                  AuthorizationHolder.current(
                    UserAuthorization(
                      user.id,
                      fullName,
                      user.administrator,
                      role.permissions,
                      role.accessLevels))
                case _ =>
                  AuthorizationHolder.current(
                    UserAuthorization(
                      user.id,
                      fullName,
                      user.administrator,
                      Set(),
                      Map()))
              }
            } else {
              AuthorizationHolder.current(
                UserAuthorization(
                  user.id,
                  fullName,
                  user.administrator,
                  Set(),
                  Map()))
            }

            true
          }
          case _ => false
        }
      } else false
    } else false
  }

  private def authenticateUsingCache(username: String, password: String) = {
    // Access cacheManager directly because we don't want to cache invalid credentials
    val cacheKey = Hashing.sha256(username + ":" + password)
    val cache = cacheManager.getCache(CACHE_NAME)

    val cacheValue = cache.get(cacheKey)

    if (cacheValue != null) {
      Some(cacheValue.get.asInstanceOf[AuthenticatedUser])
    } else {
      authenticationService.authenticate(username, password) match {
        case Some(authenticatedUser) => {
          cache.put(cacheKey, authenticatedUser)
          Some(authenticatedUser)
        }
        case None => None
      }
    }
  }

  private def authenticateProvider(request: ApiRequest): (Boolean, AuthenticationResponse) = {
    val payloadHashes = generatePayloadHashes(request)
    val qs = request.getQueryString

    val authenticationRequest = new AuthenticationRequest(
      protocol = request.getProtocol,
      method = request.getMethod,
      host = request.getServerName,
      port = request.getServerPort,
      path = request.getRequestURI,
      querystring = if (qs != null) Some(qs) else None,
      ipAddress = request.getRemoteAddr,
      headers = request.headerMap,
      payloadHashes = payloadHashes)

    val response = authenticationProcessor.authenticate(providerService, authenticationRequest)

    if (response.code == 200 || request.getMethod == "OPTIONS") {
      response.context match {
        case Some(context) => {
          UserContext.current(context.provider match {
            case Some(provider) => provider.label
            case _ => "provider"
          })
          (true, response)
        }
        case _ => (false, response)
      }
    } else (false, response)
  }

  private def generatePayloadHashes(apiRequest: ApiRequest): Map[String, Map[String, String]] = {
    val builder = Map.newBuilder[String, Map[String, String]]

    requestHasherRegistry.names foreach { name =>
      {
        val hasher = requestHasherRegistry(name).get
        val algoMap = Map.newBuilder[String, String]

        val algorithm = "sha256"
        val hash = hasher.getRequestHash(apiRequest, algorithm)
        algoMap += algorithm -> hash
        builder += name -> algoMap.result
      }
    }

    builder.result
  }

  private def handleNotAuthenticatedResponse(response: HttpServletResponse, authResponse: AuthenticationResponse) {
    response.setStatus(authResponse.code)

    if (authResponse.code == 401) {
      response.addHeader("WWW-Authenticate", "Basic realm=\"Monarch API Manager\"")
    }

    authResponse.responseHeaders foreach { h =>
      {
        response.addHeader(h.name, h.value)
      }
    }
  }
}