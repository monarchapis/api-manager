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

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar

class HeaderApiKeyExtractorTest extends FlatSpec with Matchers with MockitoSugar {
  val headers = Seq("X-Api-Key", "Api-Key", "X-ApiKey", "ApiKey", "X-Client-Id", "Client-Id")
  val extractor = new HeaderApiKeyExtractor(headers: _*)

  behavior of "HeaderApiKeyExtractor"

  it should "extract the API key from multiple possible headers" in {
    headers foreach { header =>
      runTest(header)
      runTest(header.toLowerCase)
      runTest(header.toUpperCase)
    }
  }

  private def runTest(header: String) {
    val request = createRequest(header)
    val apiKey = extractor.extract(request)
    apiKey should equal(Some("APIKEY"))
  }

  private def createRequest(apiKeyHeader: String) = {
    new AuthenticationRequest(
      method = "GET",
      protocol = "http",
      host = "service.com",
      port = 8000,
      path = "/api/v1/resource",
      querystring = None,
      headers = Map(
        "not-the-key" -> List("should not be extracted"),
        apiKeyHeader -> List("APIKEY")),
      ipAddress = "127.0.0.1")
  }
}