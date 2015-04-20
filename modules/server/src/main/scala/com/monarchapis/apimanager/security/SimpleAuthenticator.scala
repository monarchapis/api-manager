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

import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._

import grizzled.slf4j.Logging
import javax.annotation.PostConstruct
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Named

class SimpleAuthenticator @Inject() (
  tokenService: TokenService,
  @Named("apiKeyExtractorRegistry") apiKeyExtractorRegistry: StringExtractorRegistry,
  @Named("accessTokenExtractorRegistry") accessTokenExtractorRegistry: StringExtractorRegistry)
  extends Authenticator with Logging {

  debug(s"$this")

  val name = "simple"

  val displayName = "API Key / Bearer Token (Simple)"

  val propertyDescriptors = List(
    BooleanPropertyDescriptor(
      propertyName = "requiredAPIKeyWithToken",
      displayName = "Require API Key with Token for verification",
      defaultValue = true))

  def authenticate(keyStore: KeyStore, request: AuthenticationRequest): Either[AuthenticationContext, Seq[HttpHeader]] = {
    val token = accessTokenExtractorRegistry(request) match {
      case Some(accessToken) => tokenService.findByToken(accessToken)
      case _ => None
    }

    var keyFoundByExtractor = false

    val key = {
      apiKeyExtractorRegistry(request) match {
        case Some(apiKey) => {
          keyFoundByExtractor = true
          keyStore.findByApiKey(apiKey)
        }
        case _ => {
          if (token.isDefined && keyStore.isInstanceOf[ClientService]) {
            keyStore.asInstanceOf[ClientService].load(token.get.clientId)
          } else {
            None
          }
        }
      }
    }

    key match {
      case Some(key) => {
        val config = key.getAuthenticatorConfiguration(name) match {
          case Some(config) => config
          case _ => return Right(List())
        }

        val requiredKey = config.value[Boolean]("requiredAPIKeyWithToken").getOrElse(true)

        if (requiredKey && !keyFoundByExtractor) {
          return Right(List(HttpHeader("WWW-Authenticate", "Bearer realm=\"API Services\"")))
        }

        Left(AuthenticationContext(
          client = Some(key),
          token = token,
          principal = token match {
            case Some(token) => token.userId
            case _ => None
          }))
      }
      case _ => Right(List(HttpHeader("WWW-Authenticate", "Bearer realm=\"API Services\"")))
    }
  }

  override def toString = s"SimpleAuthenticator"
}