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

package com.monarchapis.apimanager.service

import scala.collection.JavaConversions._
import scala.collection.immutable.Set
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import org.apache.commons.lang3.RandomStringUtils

import com.monarchapis.apimanager.security._
import com.monarchapis.apimanager.exception.InvalidParamaterException
import com.monarchapis.apimanager.exception.NotAuthorizedException
import com.monarchapis.apimanager.exception.NotFoundException
import com.monarchapis.apimanager.model.EnvironmentContext
import com.monarchapis.apimanager.model.Message
import com.monarchapis.apimanager.model.Reference
import com.monarchapis.apimanager.model.ServiceInfo
import com.monarchapis.apimanager.model.Token

import grizzled.slf4j.Logging
import javax.inject.Inject

class ServiceManagerImpl(
  authenticationProcessor: AuthenticationProcessor,
  environmentService: EnvironmentService,
  applicationService: ApplicationService,
  clientService: ClientService,
  tokenService: TokenService,
  permissionService: PermissionService,
  messageService: MessageService,
  serviceService: ServiceService,
  providerService: ProviderService,
  authorizationCodeTokenTypes: Set[String]) extends ServiceManager with Logging {

  @Inject def this(
    authenticationProcessor: AuthenticationProcessor,
    environmentService: EnvironmentService,
    applicationService: ApplicationService,
    clientService: ClientService,
    tokenService: TokenService,
    permissionService: PermissionService,
    messageService: MessageService,
    serviceService: ServiceService,
    providerService: ProviderService,
    authorizationCodeTokenTypes: java.util.Set[String]) = this(
    authenticationProcessor,
    environmentService,
    applicationService,
    clientService,
    tokenService,
    permissionService,
    messageService,
    serviceService,
    providerService,
    if (authorizationCodeTokenTypes != null) authorizationCodeTokenTypes.toSet else null)

  require(applicationService != null, "applicationService is required")
  require(clientService != null, "clientService is required")
  require(tokenService != null, "tokenService is required")
  require(permissionService != null, "permissionService is required")
  require(messageService != null, "messageService is required")
  require(authorizationCodeTokenTypes != null, "authorizationCodeTokenTypes is required")

  import scala.util.control.Breaks._

  def getServiceInfo(
    environmentName: String,
    serviceName: Option[String],
    providerKey: Option[String]): ServiceInfo = {
    val environment = environmentService.lookupIdByName(environmentName) match {
      case Some(environmentId) => Reference(environmentId, environmentName)
      case _ => throw new NotFoundException(s"Could not find environment $environmentName")
    }

    environmentService.getDatabases(environment.id) match {
      case Some(databases) => EnvironmentContext.current(
        EnvironmentContext(
          environment.id,
          databases.system,
          databases.analytics))
      case _ => throw new NotFoundException(s"Could not find environment $environmentName")
    }

    val service = serviceName match {
      case Some(serviceName) => {
        serviceService.lookupIdByName(serviceName) match {
          case Some(serviceId) => Some(Reference(serviceId, serviceName))
          case _ => throw new NotFoundException(s"Could not find service $serviceName")
        }
      }
      case _ => None
    }

    val provider = providerKey match {
      case Some(providerKey) => {
        providerService.findByApiKey(providerKey) match {
          case Some(provider) => Some(Reference(provider.id, provider.label))
          case _ => throw new NotFoundException(s"Could not find provider $providerKey")
        }
      }
      case _ => None
    }

    ServiceInfo(environment, service, provider)
  }

  def lookupEnvironmentIdByName(name: String): Option[String] = {
    environmentService.lookupIdByName(name)
  }

  def lookupServiceIdByName(name: String): Option[String] = {
    serviceService.lookupIdByName(name)
  }

  def authenticate(request: AuthenticationRequest): AuthenticationResponse = {
    authenticationProcessor.authenticate(clientService, request)
  }

  def getAuthorizationDetails(request: AuthorizationRequest): AuthorizationDetails = {
    val client = clientService.findByApiKey(request.apiKey).getOrElse(
      throw new NotFoundException("The client was not found"))

    val application = applicationService.load(client.applicationId).getOrElse(
      throw new NotFoundException("The application was not found"))

    // Callback URI Check
    request.callbackUri match {
      case Some(callbackUri) => {
        var ok = false;

        breakable {
          application.callbackUris foreach { uri =>
            {
              if (callbackUri.startsWith(uri)) {
                ok = true
                break
              }
            }
          }
        }

        if (!ok) {
          throw new InvalidParamaterException("The callback URI was invalid")
        }
      }
      case _ =>
    }

    val permissionSet = client.permissionSets.get(request.authorizationScheme).getOrElse(
      throw new NotFoundException("The authorization scheme was not found"))

    val permissionNames = permissionService.getPermissionNames(permissionSet.permissionIds, "user")
    val simpleNames = getUniquePermissions(permissionNames)

    // Permission check
    request.permissions foreach { permission =>
      {
        if (!permissionNames(permission) && !simpleNames(permission)) {
          throw new InvalidParamaterException("An unauthorized permission was requested")
        }
      }
    }

    val permissions = {
      val builder = List.newBuilder[PermissionDetails]

      if (permissionSet.global) {
        val ids = getUniquePermissions(permissionSet.permissionIds)

        ids foreach { p =>
          {
            permissionService.load(p) match {
              case Some(permission) => {
                builder += new PermissionDetails(
                  name = permission.name,
                  flags = permission.flags)
              }
              case _ =>
            }
          }
        }
      } else {
        val names = getUniquePermissions(request.permissions)

        names foreach { p =>
          {
            permissionService.findByName(p) match {
              case Some(permission) => {
                builder += new PermissionDetails(
                  name = permission.name,
                  flags = permission.flags)
              }
              case _ =>
            }
          }
        }
      }

      builder.result
    }

    val applicationDetails = ApplicationDetails(
      id = application.id,
      name = application.name,
      description = application.description,
      applicationUrl = application.applicationUrl,
      applicationImageUrl = application.applicationImageUrl,
      companyName = application.companyName,
      companyUrl = application.companyUrl,
      companyImageUrl = application.companyImageUrl)

    val clientDetails = ClientDetails(
      id = client.id,
      apiKey = client.apiKey,
      expiration = permissionSet.expiration,
      autoAuthorize = permissionSet.autoAuthorize,
      allowWebView = permissionSet.allowWebView,
      allowPopup = permissionSet.allowPopup)

    AuthorizationDetails(
      application = applicationDetails,
      client = clientDetails,
      permissions = permissions)
  }

  def authenticateClient(request: ClientAuthenticationRequest): Boolean = {
    val client = clientService.findByApiKey(request.apiKey).getOrElse(
      throw new NotAuthorizedException("The credentials provided to not match a client."))

    val permissionSet = client.permissionSets.get(request.authorizationScheme).getOrElse(
      throw new NotFoundException("The authorization scheme was not found"))

    // TODO: Check if authentication is mandatory
    // permissionSet.??

    if (request.sharedSecret.isDefined && client.sharedSecret.isDefined &&
      ("redacted" == request.sharedSecret || client.sharedSecret.get != request.sharedSecret.get)) {
      throw new NotAuthorizedException("The credentials provided to not match a client.")
    }

    true
  }

  def createToken(request: CreateTokenRequest): TokenDetails = {
    val client = clientService.findByApiKey(request.apiKey).getOrElse(
      throw new NotFoundException("Client not found"))

    val permissionSet = client.permissionSets.get(request.authorizationScheme).getOrElse(
      throw new NotFoundException("The authorization scheme was not found"))

    if (!permissionSet.enabled) {
      throw new InvalidParamaterException("Permission set is disabled")
    }

    val refreshToken =
      if (permissionSet.refreshable)
        Some(RandomStringUtils.randomAlphanumeric(24))
      else
        None
        
    val permissionIds = permissionService.getPermissionIds(request.permissions, "user")

    val newToken = Token(
      id = null,
      clientId = client.id,
      scheme = Some(request.authorizationScheme),
      token = RandomStringUtils.randomAlphanumeric(24),
      refreshToken = refreshToken,
      tokenType = request.tokenType,
      grantType = request.grantType,
      expiresIn = if (authorizationCodeTokenTypes(request.tokenType)) Some(60) else permissionSet.expiration,
      lifecycle = permissionSet.lifespan,
      permissionIds = permissionIds,
      state = request.state,
      uri = request.uri,
      userId = request.userId,
      userContext = request.userContext,
      extended = request.extended)

    val t = tokenService.create(newToken)

    TokenDetails(
      id = t.id,
      token = t.token,
      refreshToken = t.refreshToken,
      expiresIn = t.expiresIn,
      grantType = t.grantType,
      tokenType = t.tokenType,
      permissions = permissionService.getPermissionNames(t.permissionIds, "user"),
      state = t.state,
      uri = t.uri,
      userId = t.userId,
      userContext = t.userContext)
  }

  def getToken(
    apiKey: String,
    tokenKey: String,
    callbackUri: Option[String]): TokenDetails = {
    val token = tokenService.findByToken(tokenKey).getOrElse(
      throw new NotFoundException("The token was not found"))

    val client = clientService.load(token.clientId).getOrElse(
      throw new NotFoundException("The client was not found"))

    if (client.apiKey != apiKey) {
      throw new InvalidParamaterException("The API key supplied does not match the token")
    }

    checkCallbackUri(callbackUri, token)

    TokenDetails(
      id = token.id,
      token = token.token,
      refreshToken = token.refreshToken,
      expiresIn = token.expiresIn,
      grantType = token.grantType,
      tokenType = token.tokenType,
      permissions = permissionService.getPermissionNames(token.permissionIds, "user"),
      state = token.state,
      uri = token.uri,
      userId = token.userId,
      userContext = token.userContext,
      extended = token.extended)
  }

  def getTokenByRefresh(
    apiKey: String,
    tokenKey: String,
    callbackUri: Option[String]): TokenDetails = {
    val token = tokenService.findByRefresh(tokenKey).getOrElse(
      throw new NotFoundException("The token was not found"))

    val client = clientService.load(token.clientId).getOrElse(
      throw new NotFoundException("The client was not found"))

    if (client.apiKey != apiKey) {
      throw new InvalidParamaterException("The API key supplied does not match the token")
    }

    checkCallbackUri(callbackUri, token)

    TokenDetails(
      id = token.id,
      token = token.token,
      refreshToken = token.refreshToken,
      expiresIn = token.expiresIn,
      grantType = token.grantType,
      tokenType = token.tokenType,
      permissions = permissionService.getPermissionNames(token.permissionIds, "user"),
      state = token.state,
      uri = token.uri,
      userId = token.userId,
      userContext = token.userContext,
      extended = token.extended)
  }

  def revokeToken(
    apiKey: String,
    tokenKey: String,
    callbackUri: Option[String]) {
    val token = tokenService.findByToken(tokenKey).getOrElse(
      throw new NotFoundException("The token was not found"))

    val client = clientService.load(token.clientId).getOrElse(
      throw new NotFoundException("The client was not found"))

    if (client.apiKey != apiKey) {
      throw new InvalidParamaterException("The API key supplied does not match the token")
    }

    checkCallbackUri(callbackUri, token)

    tokenService.delete(token.id)
  }

  def getPermissionMessages(request: PermissionMessagesRequest): MessageList = {
    val loadedMessages = scala.collection.mutable.Set[String]()
    val allMessages = scala.collection.mutable.Buffer[Message]();

    var nextToLoad = {
      val builder = Set.newBuilder[String]

      request.permissions foreach { p =>
        {
          val idx = p.indexOf(':')
          val trimmedPermission = if (idx != -1) {
            p.substring(0, idx);
          } else {
            p
          }

          permissionService.findByName(trimmedPermission) match {
            case Some(permission) => builder += permission.messageId
            case _ =>
          }
        }
      }

      builder.result
    }

    while (nextToLoad.size > 0) {
      val messages = messageService.loadSet(nextToLoad, 0, 1000).items
      allMessages ++= messages
      loadedMessages ++= nextToLoad

      val nextSet = Set.newBuilder[String]

      messages foreach { message =>
        message.parentId match {
          case Some(parentId) => if (!loadedMessages(parentId)) { nextSet += parentId }
          case _ =>
        }
      }

      nextToLoad = nextSet.result
    }

    val root = allMessages find { m => m.parentId.isEmpty }

    MessageList(buildMessageDetails(allMessages, None, request.locales))
  }

  private def buildMessageDetails(messages: scala.collection.mutable.Buffer[Message], parent: Option[String], locales: List[LocaleInfo]): List[MessageDetails] = {
    val withParent = messages filter (m => m.parentId == parent) toList

    withParent map { m =>
      {
        var key = "default"

        locales foreach (l => {
          val localKey = l()
          if (m.locales.contains(localKey)) {
            key = localKey
          }
        })

        val content = m.locales(key)

        MessageDetails(
          format = content.format,
          content = content.content,
          children = buildMessageDetails(messages, Some(m.id), locales))
      }
    }
  }

  private def getUniquePermissions(permissions: Set[String]) = {
    val idBuilder = Set.newBuilder[String]

    permissions foreach { p =>
      val idx = p.indexOf(':')
      val trimmedPermission = if (idx != -1) {
        p.substring(0, idx);
      } else {
        p
      }

      idBuilder += trimmedPermission
    }

    idBuilder.result
  }

  private def checkCallbackUri(callbackUri: Option[String], token: Token) {
    if (callbackUri.isDefined && (token.uri.isEmpty || token.uri.get != callbackUri.get)) {
      throw new InvalidParamaterException("The redirect URI supplied does not match the token")
    }
  }
}