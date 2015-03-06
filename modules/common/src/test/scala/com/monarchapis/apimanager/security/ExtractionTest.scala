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

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class ExtractionTest extends FlatSpec with Matchers {
  behavior of "HeaderExtraction"

  val request = AuthenticationRequest(
    protocol = "",
    method = "",
    host = "",
    port = 80,
    path = "/v1/pets",
    querystring = Some("one=test1&two=test2&three=test3"),
    headers = Map(
      "Authorization" -> List("Bearer asdfqwer1234"),
      "many" -> List("one", "two")),
    ipAddress = "127.0.0.1")

  val extractor = new HeaderExtraction with ParameterExtraction {
    def header(request: AuthenticationRequest, validHeaders: String*) = extractHeader(request, validHeaders: _*)
    def parameter(request: AuthenticationRequest, validHeaders: String*) = extractParameter(request, validHeaders: _*)
  }

  it should "extract the first existing request header given a list of possible headers" in {
    extractor.header(request, "none1", "none2", "none3") should be(None)
    extractor.header(request, "none1", "none2", "MANY") should be(Some("one"))
  }

  behavior of "ParameterExtraction"

  it should "extract the first existing request parameter given a list of possible headers" in {
    // Query string parameters are case sensitive
    extractor.parameter(request, "none1", "none2", "none3") should be(None)
    extractor.parameter(request, "none1", "none2", "three") should be(Some("test3"))
  }
}