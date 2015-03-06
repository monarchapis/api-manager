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

class QueryStringApiKeyExtractorTest extends FlatSpec with Matchers with MockitoSugar {
  var parameters = Seq("api-key", "api_key", "apikey", "client-id", "client_id", "clientid")
  val extractor = new QueryStringApiKeyExtractor(parameters: _*)

  behavior of "QueryStringApiKeyExtractor"

  it should "extract the API key from multiple possible parameters" in {
    parameters foreach { parameter =>
      runTest(parameter)
      runTest(parameter.toLowerCase)
      runTest(parameter.toUpperCase)
    }
  }

  private def runTest(header: String) {
    val request = createRequest(header)
    val apiKey = extractor.extract(request)
    apiKey should equal(Some("APIKEY"))
  }

  private def createRequest(apiKeyParameter: String) = {
    new AuthenticationRequest(
      method = "GET",
      protocol = "http",
      host = "service.com",
      port = 8000,
      path = "/api/v1/resource",
      querystring = Some(s"test=1234&$apiKeyParameter=APIKEY&other=456"),
      headers = Map(),
      ipAddress = "127.0.0.1")
  }
}