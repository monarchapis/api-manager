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
import java.security.MessageDigest
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import com.monarchapis.apimanager.service._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.monarchapis.apimanager.model.Key
import com.monarchapis.apimanager.model.Configuration
import com.monarchapis.apimanager.model.Client
import org.apache.commons.codec.binary.Base64

class HawkV1AuthenticatorTest extends FlatSpec with Matchers with MockitoSugar {
  val verifier = HawkV1Authenticator
  val tokenService = mock[TokenService]
  val authenticator = new HawkV1Authenticator(tokenService)
  val keyStore = mock[KeyStore]

  val client = new Client(
    id = "1234",
    applicationId = "1234",
    label = "test",
    enabled = true,
    apiKey = "dh37fgj492ja",
    sharedSecret = None,
    clientPermissionIds = Set.empty[String],
    authenticators = Map("hawkV1" -> Map("algorithms" -> List("sha256"))),
    policies = List.empty[Configuration],
    claimSources = List.empty[Configuration])

  val clientNoAlgorithms = client.withApiKey("dh37fgj492jb").withAuthenticators(Map("hawkV1" -> Map()))
  val clientWithSecret = client.withApiKey("dh37fgj492je").withSharedSecret(Some("abdefabcdefabdef"))

  when(keyStore.findByApiKey("dh37fgj492ja")).thenReturn(Some(client))
  when(keyStore.findByApiKey("dh37fgj492jb")).thenReturn(Some(clientNoAlgorithms))
  when(keyStore.findByApiKey("dh37fgj492je")).thenReturn(Some(clientWithSecret))

  behavior of "HawkV1Authenticator"

  it should "return an WWW-Authenticate response header if the request Authorization header was not supplied" in {
    val request = mock[AuthenticationRequest]
    when(request.getHeaderValues("Authorization")).thenReturn(None)

    authenticator.authenticate(keyStore, request) match {
      case Left(authenticationContext) => fail("Expected failure")
      case Right(headers) => headers should equal(List(HttpHeader("WWW-Authenticate", "Hawk")))
    }
  }

  it should "return an WWW-Authenticate response header if the authorization header does not start with Hawk" in {
    val request = mock[AuthenticationRequest]
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Basic test")))

    authenticator.authenticate(keyStore, request) match {
      case Left(authenticationContext) => fail("Expected failure")
      case Right(headers) => headers should equal(List(HttpHeader("WWW-Authenticate", "Hawk")))
    }
  }

  it should "throw an exception if the id was not supplied" in {
    val request = mock[AuthenticationRequest]
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk ts=\"1353832234\", nonce=\"j4h3g2\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAE=\"")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("Hawk id was not suppied")
  }

  it should "throw an exception if the mac was not supplied" in {
    val request = mock[AuthenticationRequest]
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492je\", ts=\"1353832234\", nonce=\"j4h3g2\", ext=\"some-app-ext-data\"")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("Hawk mac was not suppied")
  }

  it should "throw an exception if the nonce was not supplied" in {
    val request = mock[AuthenticationRequest]
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492je\", ts=\"1353832234\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAE=\"")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("Hawk nonce was not suppied")
  }

  it should "throw an exception if the nonce is less then 6 characters" in {
    val request = mock[AuthenticationRequest]
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492je\", ts=\"1353832234\", nonce=\"12345\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAE=\"")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("Hawk nonce must be at least 6 characters")
  }

  it should "throw an exception if the timestamp was not supplied" in {
    val request = mock[AuthenticationRequest]
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492je\", ts2=\"1353832234\", nonce=\"j4h3g2\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAE=\"")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("Hawk timestamp was not suppied")
  }

  it should "throw an exception if the timestamp not a valid integer" in {
    val request = mock[AuthenticationRequest]
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492je\", ts=\"not a timestamp\", nonce=\"j4h3g2\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAE=\"")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("Hawk timestamp was not a valid integer")
  }

  it should "throw an exception if the timestamp not within 60 seconds of clock skew" in {
    val request = mock[AuthenticationRequest]
    val now = System.currentTimeMillis() / 1000 - 61
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492je\", ts=\"" + now + "\", nonce=\"j4h3g2\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAE=\"")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("Hawk timestamp skew is more than the allowed amount by the server")
  }

  it should "throw an exception if the shared secret was not supplied" in {
    val request = mock[AuthenticationRequest]
    val now = System.currentTimeMillis() / 1000
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492ja\", ts=\"" + now + "\", nonce=\"j4h3g2\", hash=\"Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAE=\"")))
    when(request.payloadHashes).thenReturn(Map("Hawk V1" -> Map("sha256" -> "Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("A shared secret is required for Hawk")
  }

  it should "throw an exception if the request content hash was not supplied" in {
    val request = mock[AuthenticationRequest]
    val now = System.currentTimeMillis() / 1000
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492ja\", ts=\"" + now + "\", nonce=\"j4h3g2\", hash=\"Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAE=\"")))
    when(request.payloadHashes).thenReturn(Map.empty[String, Map[String, String]])

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("A server-calculated payload hash for Hawk was not supplied")
  }

  it should "throw an exception if the mac is an invalid length" in {
    val request = mock[AuthenticationRequest]
    val now = System.currentTimeMillis() / 1000
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492ja\", ts=\"" + now + "\", nonce=\"j4h3g2\", hash=\"Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAEEE=\"")))
    when(request.payloadHashes).thenReturn(Map("Hawk V1" -> Map("sha256" -> "Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("Unknown MAC algorithm with length 33")
  }

  it should "throw an exception if the client does not allow the MAC algorithm" in {
    val request = mock[AuthenticationRequest]
    val now = System.currentTimeMillis() / 1000
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492jb\", ts=\"" + now + "\", nonce=\"j4h3g2\", hash=\"Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAE=\"")))
    when(request.payloadHashes).thenReturn(Map("Hawk V1" -> Map("sha256" -> "Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("The MAC algorithm sha256 is not permitted")
  }

  it should "throw an exception if the hashes do not match" in {
    val request = mock[AuthenticationRequest]
    val now = System.currentTimeMillis() / 1000
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492ja\", ts=\"" + now + "\", nonce=\"j4h3g2\", hash=\"Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAE=\"")))
    when(request.payloadHashes).thenReturn(Map("Hawk V1" -> Map("sha256" -> "thiswillnotmatch")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("The hash failed the verification check")
  }

  it should "throw an exception if the calculated signatures do not match" in {
    val request = mock[AuthenticationRequest]
    when(request.getHeader("X-Forwarded-Path")).thenReturn(None)
    when(request.path).thenReturn("/resource/1")
    when(request.method).thenReturn("POST")
    when(request.querystring).thenReturn(Some("a=1&b=2"))
    when(request.host).thenReturn("example.com")
    when(request.port).thenReturn(8000)

    val now = System.currentTimeMillis() / 1000
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492je\", ts=\"" + now + "\", nonce=\"j4h3g2\", hash=\"Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=\", ext=\"some-app-ext-data\", mac=\"6R4rV5iE+NPoym+WwjeHzjAGXUtLNIxmo1vpMofpLAE=\"")))
    when(request.payloadHashes).thenReturn(Map("Hawk V1" -> Map("sha256" -> "Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=")))

    val exception = the[InvalidAuthenticationException] thrownBy {
      authenticator.authenticate(keyStore, request)
    }

    exception.developerMessage should equal("The mac failed the verification check")
  }

  it should "return the API context if none of the failure conditions are present" in {
    val request = mock[AuthenticationRequest]
    when(request.getHeader("X-Forwarded-Path")).thenReturn(None)
    when(request.path).thenReturn("/resource/1")
    when(request.method).thenReturn("POST")
    when(request.querystring).thenReturn(Some("a=1&b=2"))
    when(request.host).thenReturn("example.com")
    when(request.port).thenReturn(8000)

    val now = System.currentTimeMillis() / 1000

    val sb = new StringBuilder

    sb append "hawk.1.header\n"
    sb append now append "\n"
    sb append "j4h3g2" append "\n"
    sb append request.method append "\n"
    sb append request.path append "?" append request.querystring.get append "\n"
    sb append request.host append "\n"
    sb append request.port append "\n"
    sb append "Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=" append "\n"
    sb append "some-app-ext-data" append "\n"

    val signingKey = new SecretKeySpec(clientWithSecret.sharedSecret.get.getBytes("UTF-8"), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(signingKey)
    val hashCheck = mac.doFinal(sb.toString.getBytes("UTF-8"))
    val verify = Base64.encodeBase64String(hashCheck)

    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Hawk id=\"dh37fgj492je\", ts=\"" + now + "\", nonce=\"j4h3g2\", hash=\"Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=\", ext=\"some-app-ext-data\", mac=\"" + verify + "\"")))
    when(request.payloadHashes).thenReturn(Map("Hawk V1" -> Map("sha256" -> "Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=")))

    authenticator.authenticate(keyStore, request) match {
      case Left(context) => {
        context.client should equal(Some(clientWithSecret))
        context.token should equal(None)
        context.principal should equal(None)
        context.claims should equal(None)
      }
      case Right(headers) => fail("Expected failure")
    }
  }
}