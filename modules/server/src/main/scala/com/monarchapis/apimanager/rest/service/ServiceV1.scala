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

package com.monarchapis.apimanager.rest.service

import scala.beans.BeanProperty
import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.security.ClientAuthenticationRequest
import com.monarchapis.apimanager.security.AuthenticationRequest
import com.monarchapis.apimanager.security.AuthorizationRequest
import com.monarchapis.apimanager.security.CreateTokenRequest
import com.monarchapis.apimanager.security.PermissionMessagesRequest
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._
import javax.inject.Inject
import javax.inject.Named
import javax.ws.rs._
import com.monarchapis.apimanager.exception.NotFoundException
import com.monarchapis.apimanager.exception.NotAuthorizedException
import com.monarchapis.apimanager.rest.AbstractDocumentationResource

case class IdWrapperResponse(@BeanProperty id: String)

@Path("/v1")
@Named
class ServiceApiResource @Inject() (serviceManager: ServiceManager) {
  require(serviceManager != null, "serviceManager is required")

  @Path("/requests/authenticate")
  @POST
  def authenticateRequest(request: AuthenticationRequest) = {
    AuthorizationUtils.check("authenticate")
    AuthorizationUtils.continueWithAuthenticationAccess

    serviceManager.authenticate(request)
  }

  @Path("/authorizations/details")
  @POST
  def getAuthorizationDetails(request: AuthorizationRequest) = {
    AuthorizationUtils.check("delegate")
    AuthorizationUtils.continueWithSystemAccess

    serviceManager.getAuthorizationDetails(request)
  }

  @Path("/clients/authenticate")
  @POST
  def authenticateClient(request: ClientAuthenticationRequest) = {
    AuthorizationUtils.check("delegate")
    AuthorizationUtils.continueWithSystemAccess

    if (serviceManager.authenticateClient(request)) "OK"
    else throw new NotAuthorizedException("Client authentication failed")
  }

  @Path("/tokens")
  @POST
  def createToken(request: CreateTokenRequest) = {
    AuthorizationUtils.check("delegate")
    AuthorizationUtils.continueWithSystemAccess

    serviceManager.createToken(request)
  }

  @Path("/tokens")
  @GET
  def getToken(
    @QueryParam("apiKey") apiKey: String,
    @QueryParam("token") token: String,
    @QueryParam("refresh") refresh: String,
    @QueryParam("callbackUri") callbackUri: String) = {
    require(apiKey != null, "apiKey is a required parameter")
    require(token != null || refresh != null, "either the token or refresh parameter is required")

    AuthorizationUtils.check("delegate")
    AuthorizationUtils.continueWithSystemAccess

    if (token != null) {
      serviceManager.getToken(apiKey, token, Option.apply(callbackUri))
    } else {
      serviceManager.getToken(apiKey, refresh, Option.apply(callbackUri))
    }
  }

  // Client revoking
  @Path("/tokens")
  @DELETE
  def revokeToken(
    @QueryParam("apiKey") apiKey: String,
    @QueryParam("token") token: String,
    @QueryParam("callbackUri") callbackUri: String) {
    require(apiKey != null, "apiKey is a required parameter")
    require(token != null, "token is a required parameter")

    AuthorizationUtils.check("revoke")
    AuthorizationUtils.continueWithSystemAccess

    serviceManager.revokeToken(apiKey, token, Option.apply(callbackUri))
  }

  @Path("/permissions/messages")
  @POST
  def getPermissionMessages(request: PermissionMessagesRequest) = {
    AuthorizationUtils.check("delegate")
    AuthorizationUtils.continueWithSystemAccess

    serviceManager.getPermissionMessages(request)
  }
}

@Path("/v1")
@Named
class ServiceDocumentationResource extends AbstractDocumentationResource("V1")