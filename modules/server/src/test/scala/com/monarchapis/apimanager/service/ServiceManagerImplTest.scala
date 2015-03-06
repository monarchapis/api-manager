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

import scala.beans.BeanProperty
import scala.collection.immutable.Set
import scala.collection.JavaConversions._

import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.mockito.Mockito.when

import com.monarchapis.apimanager.security._
import com.monarchapis.apimanager.model._

import java.io.ByteArrayInputStream

class ServiceManagerImplTest extends FlatSpec with Matchers with MockitoSugar {
  val (applicationService, clientService, tokenService, apiService) = {
    val authenticationProcessor = mock[AuthenticationProcessor]
    val environmentService = mock[EnvironmentService]
    val applicationService = mock[ApplicationService]
    val clientService = mock[ClientService]
    val tokenService = mock[TokenService]
    val permissionService = mock[PermissionService]
    val messageService = mock[MessageService]
    val serviceService = mock[ServiceService]
    val providerService = mock[ProviderService]

    when(applicationService.load("1")).thenReturn(Some(Application(
      id = "1",
      name = "Test Application",
      description = Some("Mock test application"),
      applicationUrl = "http://mockapp.com",
      applicationImageUrl = Some("http://mockapp.com/logo.png"),
      companyName = "Mock App, Inc.",
      companyUrl = "http://mockapp.com",
      companyImageUrl = Some("http://mockapp.com/logo.png"))))

    when(clientService.findByApiKey("abcdef")).thenReturn(Some(Client(
      id = "1",
      applicationId = "1",
      label = "Test Client 1",
      enabled = true,
      apiKey = "abcdef",
      clientPermissionIds = Set("permission1"))))

    when(clientService.findByApiKey("ghijkl")).thenReturn(Some(Client(
      id = "2",
      applicationId = "1",
      label = "Test Client 2",
      enabled = true,
      apiKey = "ghijkl",
      sharedSecret = Some("secret"),
      policies = List(Configuration("dummy", Map())),
      clientPermissionIds = Set("permission1"))))

    when(clientService.findByApiKey("none")).thenReturn(None)

    when(tokenService.findByToken("123456")).thenReturn(Some(Token(
      id = "1",
      clientId = "1",
      scheme = None,
      token = "123456",
      tokenType = "bearer",
      grantType = "oauth",
      expiresIn = Some(3600),
      lifecycle = "finite",
      uri = Some("http://mockapp.com/oauth"),
      userId = "testuser",
      permissionIds = Set("permission1"))))

    when(tokenService.findByToken("654321")).thenReturn(Some(Token(
      id = "2",
      clientId = "2",
      scheme = None,
      token = "654321",
      tokenType = "bearer",
      grantType = "oauth",
      expiresIn = Some(3600),
      lifecycle = "finite",
      uri = Some("http://mockapp.com/oauth"),
      userId = "testuser",
      permissionIds = Set("permission1"))))

    when(tokenService.findByToken("none")).thenReturn(None)

    val authenticator = new Authenticator {
      def name = "dummy"
      def displayName = "Dummy"
      val propertyDescriptors = List()
      def authenticate(keyStore: KeyStore, request: AuthenticationRequest) = Right(List())
    }

    val authenticatorRegistry = new AuthenticatorRegistry(authenticator)

    val policy = new Policy {
      def name = "dummy"
      def displayName = "Dummy"
      val propertyDescriptors = List()
      def verify(config: Configuration, request: AuthenticationRequest, context: AuthenticationContext) = false
    }

    val policyRegistry = new PolicyRegistry(policy)

    val apiService = new ServiceManagerImpl(
      authenticationProcessor = authenticationProcessor,
      environmentService = environmentService,
      applicationService = applicationService,
      clientService = clientService,
      tokenService = tokenService,
      permissionService = permissionService,
      messageService = messageService,
      serviceService = serviceService,
      providerService = providerService,
      authorizationCodeTokenTypes = Set("test"))

    (applicationService, clientService, tokenService, apiService)
  }

  /*
  behavior of "ApiServiceImpl"

  it should "verify that the provided API key exists" in {
    val request = ApiRequest(
      serviceId = "12345",
      url = "http://api.test.com/testResource/1",
      querystring = None,
      headers = Map(),
      timestamp = DateTime.now,
      apiKey = "none",
      accessToken = Some("123456"),
      ipAddress = "127.0.0.1")

    intercept[InvalidApiKeyException] {
      //apiService.authorize(request)
    }

    //context.client.apiKey should equal("abcdef")
  }

  it should "verify the signature (e.g. HMAC) of the request" in {
    val request = ApiRequest(
      serviceId = "12345",
      url = "http://api.test.com/testResource/1",
      querystring = None,
      headers = Map(),
      timestamp = DateTime.now,
      apiKey = "ghijkl",
      accessToken = Some("654321"),
      ipAddress = "127.0.0.1",
      signature = Some("invalid"))

    intercept[InvalidSignatureException] {
      //apiService.authorize(request)
    }

    //apiService.authorize(request.withSignature(Some("valid")))
  }

  it should "throw an exception if a signature was required but not provided" in {
    val request = ApiRequest(
      serviceId = "12345",
      url = "http://api.test.com/testResource/1",
      querystring = None,
      headers = Map(),
      timestamp = DateTime.now,
      apiKey = "ghijkl",
      accessToken = Some("654321"),
      ipAddress = "127.0.0.1")

    intercept[InvalidSignatureException] {
      //apiService.authorize(request)
    }
  }

  it should "verify that the access token exists if provided" in {
    val request = ApiRequest(
      serviceId = "12345",
      url = "http://api.test.com/testResource/1",
      querystring = None,
      headers = Map(),
      timestamp = DateTime.now,
      apiKey = "abcdef",
      accessToken = Some("none"),
      ipAddress = "127.0.0.1")

    intercept[InvalidAccessTokenException] {
      //apiService.authorize(request)
    }
  }

  it should "verify that the access token is associated provided API key" in {
    val request = ApiRequest(
      serviceId = "12345",
      url = "http://api.test.com/testResource/1",
      querystring = None,
      headers = Map(),
      timestamp = DateTime.now,
      apiKey = "abcdef",
      accessToken = Some("654321"),
      ipAddress = "127.0.0.1")

    intercept[InvalidAccessTokenException] {
      //apiService.authorize(request)
    }
  }

  it should "verify that the timestamp is within 5 minutes" in {
    val request1 = ApiRequest(
      serviceId = "12345",
      url = "http://api.test.com/testResource/1",
      querystring = None,
      headers = Map(),
      timestamp = DateTime.now.minusMinutes(5).minusMillis(1),
      apiKey = "abcdef",
      ipAddress = "127.0.0.1")

    intercept[InvalidTimestampException] {
      //apiService.authorize(request1)
    }

    val request2 = request1.withTimestamp(DateTime.now.plusMinutes(5).plusMillis(200))

    intercept[InvalidTimestampException] {
      //apiService.authorize(request2)
    }
  }
  */
}