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

class AuthenticationUtilsTest extends FlatSpec with Matchers with MockitoSugar {
  behavior of "AuthorizationUtils"

  it should "return the value of an Authorization header with a specified prefix" in {
    val request1 = mock[AuthenticationRequest]
    when(request1.getHeaderValues("Authorization")).thenReturn(None)
    var actual1 = AuthenticationUtils.getAuthorizationWithPrefix(request1, "Basic", false)
    actual1 should equal(None)

    val request2 = mock[AuthenticationRequest]
    when(request2.getHeaderValues("Authorization")).thenReturn(Some(List("Basic dGVzdDp0ZXN0")))
    var actual2a = AuthenticationUtils.getAuthorizationWithPrefix(request2, "Basic", false)
    actual2a should equal(Some("dGVzdDp0ZXN0"))
    var actual2b = AuthenticationUtils.getAuthorizationWithPrefix(request2, "Basic", true)
    actual2b should equal(Some("test:test"))
  }

  it should "return the payload hash for a given scheme and algorithm" in {
    val request = mock[AuthenticationRequest]
    when(request.payloadHashes).thenReturn(Map("hawk" -> Map("sha256" -> "test")))
    var actual1 = AuthenticationUtils.getPayloadHash(request, "miss", "sha256")
    actual1 should equal(None)
    var actual2 = AuthenticationUtils.getPayloadHash(request, "hawk", "sha1")
    actual2 should equal(None)
    var actual3 = AuthenticationUtils.getPayloadHash(request, "hawk", "sha256")
    actual3 should equal(Some("test"))
  }

  it should "return a map of parameters of a signature string" in {
    val params = AuthenticationUtils.getSignatureParameters("nonce=\"1234\", timestamp=\"5678\", method=\"HMAC-SHA1\", signature=\"abcdef\"")
    params("nonce") should equal("1234")
    params("timestamp") should equal("5678")
    params("method") should equal("HMAC-SHA1")
    params("signature") should equal("abcdef")
  }
}