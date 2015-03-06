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

package com.monarchapis.apimanager.util

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class RequestUtilsTest extends FlatSpec with Matchers {
  behavior of "RequestUtils"

  it should "parse out the individual parameters from a querystring" in {
    val params = RequestUtils.parseUriParameters("one=test1&two=test2&three=test3")

    params.get("none") should be(None)
    params.get("one") should be(Some("test1"))
    params.get("two") should be(Some("test2"))
    params.get("three") should be(Some("test3"))
  }

  it should "ignore a leading scheme/host/port/path and ?" in {
    var params = RequestUtils.parseUriParameters("?one=test1&two=test2&three=test3")

    params.get("none") should be(None)
    params.get("one") should be(Some("test1"))

    params = RequestUtils.parseUriParameters("http://www.test.com/path?one=test1&two=test2&three=test3")

    params.get("none") should be(None)
    params.get("one") should be(Some("test1"))
  }
  
  it should "decode URL encoded parameters" in {
    val params = RequestUtils.parseUriParameters("test=test%20test%26test%3Dtest%3Ftest&two=test")
    
    params.get("none") should be(None)
    params.get("test") should be(Some("test test&test=test?test"))
  }
}