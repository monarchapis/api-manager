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

class AuthenticationRequestTest extends FlatSpec with Matchers {
  behavior of "Configuration"

  val request = AuthenticationRequest(
    protocol = "",
    method = "",
    host = "",
    port = 80,
    path = "/v1/pets",
    querystring = None,
    headers = Map(
      "Authorization" -> List("Bearer asdfqwer1234"),
      "many" -> List("one", "two")),
    ipAddress = "127.0.0.1")

  it should "test if a header name exists with case insensitivity" in {
    request.hasHeader("authoriZATION") should be(true)
    request.hasHeader("MAny") should be(true)
    request.hasHeader("nOnE") should be(false)
  }

  it should "retrieve header values with case insensitivity" in {
    request.getHeader("authoriZATION") should be(Some("Bearer asdfqwer1234"))
    request.getHeader("MAny") should be(Some("one"))
    request.getHeaderValues("MAny") should be(Some(List("one", "two")))
    request.getHeader("nOnE") should be(None)
    request.getHeaderValues("nOnE") should be(None)
  }
}