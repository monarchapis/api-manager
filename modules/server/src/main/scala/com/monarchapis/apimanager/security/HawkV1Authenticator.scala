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

import scala.Left
import scala.Right

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

object HawkV1Authenticator {
  val HMAC_MD5_ALGORITHM = "HmacMD5";
  val HMAC_SHA1_ALGORITHM = "HmacSHA1";
  val HMAC_SHA256_ALGORITHM = "HmacSHA256";

  val ALGORITHM_LOOKUP = Map(
    "md5" -> HMAC_MD5_ALGORITHM,
    "sha1" -> HMAC_SHA1_ALGORITHM,
    "sha256" -> HMAC_SHA256_ALGORITHM)
}

class HawkV1Authenticator @Inject() (
  tokenService: TokenService)
  extends Authenticator with Logging {

  import HawkV1Authenticator._

  @Value("${hawk.maxTimestampSkew:60}")
  var maxTimestampSkew: Int = 60

  @PostConstruct
  def constructed {
    debug(s"$this")
  }

  val name = "hawkV1"

  val displayName = "Hawk V1"

  val propertyDescriptors = List(
    StringPropertyDescriptor(
      propertyName = "algorithms",
      displayName = "Hash Algorithms",
      multi = true,
      options = List(
        PropertyOption("MD5", "md5"),
        PropertyOption("SHA-1", "sha1"),
        PropertyOption("SHA-256", "sha256"))),
    BooleanPropertyDescriptor(
      propertyName = "payloadValidation",
      displayName = "Require payload validation",
      defaultValue = true))

  def authenticate(keyStore: KeyStore, request: AuthenticationRequest): Either[AuthenticationContext, Seq[HttpHeader]] = {
    val signatureString = AuthenticationUtils.getAuthorizationWithPrefix(request, "Hawk") match {
      case Some(value) => value
      case _ => return Right(List(HttpHeader("WWW-Authenticate", "Hawk")))
    }

    val params = AuthenticationUtils.getSignatureParameters(signatureString)

    val id = require(params.get("id"), "Hawk id was not suppied")
    val nonce = require(params.get("nonce"), "Hawk nonce was not suppied")
    val hash = params.get("hash")
    val ts = require(params.get("ts"), "Hawk timestamp was not suppied")
    val timestamp = require(Long.unapply(ts), "Hawk timestamp was not a valid integer")
    val ext = params.get("ext")
    val signature = require(params.get("mac"), "Hawk mac was not suppied")
    val app = params.get("app")
    val dlg = params.get("dlg")

    if (nonce.length < 6) {
      debug("Hawk nonce must be at least 6 characters")
      throw new InvalidAuthenticationException("Hawk nonce must be at least 6 characters")
    }

    val compare = System.currentTimeMillis() / 1000;

    if (Math.abs(compare - timestamp) > maxTimestampSkew) {
      // TODO Add server time in response header
      debug("Hawk timestamp skew is more than the allowed amount by the server")
      throw new InvalidAuthenticationException("Hawk timestamp skew is more than the allowed amount by the server")
    }

    val (client, token) = {
      app match {
        case Some(apiKey) => {
          val client = require(keyStore.findByApiKey(apiKey), "Client for hawk id was not found")

          val token = tokenService.findByToken(id) match {
            case Some(value) => value
            case _ => {
              debug("Could not find the access token.")
              throw new InvalidAccessTokenException
            }
          }

          if (token.clientId != client.id) {
            debug("The access token is not associated with this client.")
            throw new InvalidAccessTokenException
          }

          (client, Some(token))
        }
        case _ => (require(keyStore.findByApiKey(id), "Client for hawk id was not found"), None)
      }
    }

    // Spare some cycles by returning if this is not a supported authenticator by the client.
    val configOption = client.getAuthenticatorConfiguration(name)

    if (configOption.isEmpty) {
      return Right(List())
    }

    val config = configOption.get

    val algorithms = config.values[String]("algorithms")
    val payloadHashRequired = config.value[Boolean]("payloadHash").getOrElse(true)

    // Check signature length against valid algorithms
    val decodedSignature = Base64.decodeBase64(signature)
    val hashAlgorithm = decodedSignature.length match {
      case 16 => "md5"
      case 20 => "sha1"
      case 32 => "sha256"
      case _ => {
        debug(s"Unknown MAC algorithm with length ${decodedSignature.length}")
        throw new InvalidAuthenticationException(s"Unknown MAC algorithm with length ${decodedSignature.length}")
      }
    }

    if (!algorithms.contains(hashAlgorithm)) {
      debug(s"The MAC algorithm $hashAlgorithm is not permitted")
      throw new InvalidAuthenticationException(s"The MAC algorithm $hashAlgorithm is not permitted")
    }

    val algorithm = ALGORITHM_LOOKUP(hashAlgorithm)

    // Check payload hash
    val payloadHash = AuthenticationUtils.getPayloadHash(request, "Hawk V1", hashAlgorithm)

    if (payloadHashRequired) {
      if (hash.isEmpty) {
        debug("The client did not provide a payload hash for Hawk")
        throw new InvalidAuthenticationException("The client did not provide a payload hash for Hawk")
      }

      if (payloadHash.isEmpty) {
        debug("A server-calculated payload hash for Hawk was not supplied")
        throw new InvalidAuthenticationException("A server-calculated payload hash for Hawk was not supplied")
      }
    }

    if (hash.isDefined && payloadHash.isDefined && hash.get != payloadHash.get) {
      debug("The hash failed the verification check")
      throw new InvalidAuthenticationException("The hash failed the verification check")
    }

    val key = require(client.sharedSecret, "A shared secret is required for Hawk")

    val sb = new StringBuilder

    val behindReverseProxy = BehindReverseProxyHolder.current
    val originalPath = if (behindReverseProxy) request.getHeader("X-Forwarded-Path") else None

    sb append "hawk.1.header\n"
    sb append ts append "\n"
    sb append nonce append "\n"
    sb append request.method append "\n"
    sb append originalPath.getOrElse(request.path)

    request.querystring match {
      case Some(qs) => sb append "?" append qs
      case _ =>
    }

    sb append "\n"
    sb append request.host append "\n"
    sb append request.port append "\n"

    if (payloadHash.isDefined) {
      sb append payloadHash.get
    }

    sb append "\n"

    if (ext.isDefined) {
      sb append ext.get
    }

    sb append "\n"

    if (app.isDefined) {
      sb append app.get append "\n"

      if (dlg.isDefined) {
        sb append dlg.get
      }

      sb append "\n"
    }

    val header = sb.toString

    val signingKey = new SecretKeySpec(key.getBytes("UTF-8"), algorithm)
    val mac = Mac.getInstance(algorithm)
    mac.init(signingKey)
    val hashCheck = mac.doFinal(header.getBytes("UTF-8"))

    val verify = Base64.encodeBase64String(hashCheck)

    if (verify != signature) {
      debug("The mac failed the verification check")
      throw new InvalidAuthenticationException("The mac failed the verification check")
    }

    Left(AuthenticationContext(
      client = Some(client),
      token = token,
      principal = token match {
        case Some(token) => token.userId
        case _ => None
      }))
  }

  private def require[T](value: Option[T], invalidMessage: String) = {
    value match {
      case Some(value) => value
      case _ => {
        debug(s"Required value not present: invalidMessage")
        throw new InvalidAuthenticationException(invalidMessage)
      }
    }
  }

  override def toString = s"HawkV1Authenticator(maxTimestampSkew = $maxTimestampSkew)"
}