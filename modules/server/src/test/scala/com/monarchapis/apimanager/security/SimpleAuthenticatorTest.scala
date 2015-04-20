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

import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._

import javax.inject.Inject

class SimpleAuthenticatorTest extends FlatSpec with Matchers with MockitoSugar {
  behavior of "SimpleAuthenticator"

  val tokenService = mock[TokenService]
  val apiKeyExtractorRegistry = mock[StringExtractorRegistry]
  val accessTokenExtractorRegistry = mock[StringExtractorRegistry]
  val authenticator = new SimpleAuthenticator(tokenService, apiKeyExtractorRegistry, accessTokenExtractorRegistry)

  val request = mock[AuthenticationRequest]
  val testKey = mock[Client]
  val testToken = mock[Token]

  val config = mock[Configuration]
  when(testKey.getAuthenticatorConfiguration("simple")).thenReturn(Some(config))

  when(testToken.clientId).thenReturn("1234")
  when(testToken.userId).thenReturn(Some("jdoe"))

  val keyStore = mock[ClientService]
  when(keyStore.findByApiKey("test")).thenReturn(Some(testKey))
  when(keyStore.load("1234")).thenReturn(Some(testKey))

  when(tokenService.findByToken("test")).thenReturn(Some(testToken))

  val failureHeaders = List(HttpHeader("WWW-Authenticate", "Bearer realm=\"API Services\""))

  it should "authenticate a request with only an API key" in {
    when(config.value[Boolean]("requiredAPIKeyWithToken")).thenReturn(Some(true))
    when(accessTokenExtractorRegistry(request)).thenReturn(None)
    when(apiKeyExtractorRegistry(request)).thenReturn(Some("test"))

    authenticator.authenticate(keyStore, request) match {
      case Left(context) => {
        context.client should equal(Some(testKey))
        context.token should equal(None)
        context.principal should equal(None)
        context.claims should equal(None)
      }
      case Right(headers) => fail("Expected success")
    }
  }

  it should "authenticate a request with only an Access Token key" in {
    when(config.value[Boolean]("requiredAPIKeyWithToken")).thenReturn(Some(false))
    when(accessTokenExtractorRegistry(request)).thenReturn(Some("test"))
    when(apiKeyExtractorRegistry(request)).thenReturn(None)

    authenticator.authenticate(keyStore, request) match {
      case Left(context) => {
        context.client should equal(Some(testKey))
        context.token should equal(Some(testToken))
        context.principal should equal(Some("jdoe"))
        context.claims should equal(None)
      }
      case Right(headers) => fail("Expected success")
    }
  }

  it should "authenticate a request with only an Access Token key when the API is also required" in {
    when(config.value[Boolean]("requiredAPIKeyWithToken")).thenReturn(Some(true))
    when(accessTokenExtractorRegistry(request)).thenReturn(Some("test"))
    when(apiKeyExtractorRegistry(request)).thenReturn(None)

    authenticator.authenticate(keyStore, request) match {
      case Left(context) => fail("Expected failure")
      case Right(headers) => headers should equal(failureHeaders)
    }
  }

  it should "authenticate a request with an API key and Access Token" in {
    when(config.value[Boolean]("requiredAPIKeyWithToken")).thenReturn(Some(true))
    when(accessTokenExtractorRegistry(request)).thenReturn(Some("test"))
    when(apiKeyExtractorRegistry(request)).thenReturn(Some("test"))

    authenticator.authenticate(keyStore, request) match {
      case Left(context) => {
        context.client should equal(Some(testKey))
        context.token should equal(Some(testToken))
        context.principal should equal(Some("jdoe"))
        context.claims should equal(None)
      }
      case Right(headers) => fail("Expected success")
    }
  }
}