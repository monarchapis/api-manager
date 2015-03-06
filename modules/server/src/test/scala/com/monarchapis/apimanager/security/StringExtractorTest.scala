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
import org.scalatest.mock.MockitoSugar
import org.scalatest.Matchers
import org.scalatest.FlatSpec

class StringExtractorTest extends FlatSpec with Matchers with MockitoSugar {
  behavior of "HeaderApiKeyExtractor"

  it should "extract an API key from API request headers" in {
    val extractor = new HeaderApiKeyExtractor("X-Api-Key")
    val request = mock[AuthenticationRequest]
    when(request.hasHeader("x-api-key")).thenReturn(true)
    when(request.getHeader("x-api-key")).thenReturn(Some("1234"))
    val actual = extractor.extract(request)
    actual should equal(Some("1234"))
  }

  behavior of "QueryStringApiKeyExtractor"

  it should "extract an API key from API request query string" in {
    val extractor = new QueryStringApiKeyExtractor("api-key")
    val request = mock[AuthenticationRequest]
    when(request.querystring).thenReturn(Some("api-key=1234&test=other"))
    val actual = extractor.extract(request)
    actual should equal(Some("1234"))
  }

  behavior of "BearerTokenExtractor"

  it should "extract an Access Token from an API request query string" in {
    val extractor = new BearerTokenExtractor
    val request = mock[AuthenticationRequest]
    when(request.querystring).thenReturn(Some("access_token=1234"))
    when(request.getHeaderValues("Authorization")).thenReturn(None)
    val actual = extractor.extract(request)
    actual should equal(Some("1234"))
  }

  it should "extract an Access Token from API request headers" in {
    val extractor = new BearerTokenExtractor
    val request = mock[AuthenticationRequest]
    when(request.querystring).thenReturn(None)
    when(request.getHeaderValues("Authorization")).thenReturn(Some(List("Bearer 1234")))
    val actual = extractor.extract(request)
    actual should equal(Some("1234"))
  }
}