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

import org.scalatest.mock.MockitoSugar
import org.scalatest.Matchers
import org.scalatest.FlatSpec
import scala.util.matching.Regex

class UriRegexConverterTest extends FlatSpec with Matchers with MockitoSugar {
  val converter = new UriRegexConverter

  behavior of "UriRegexConverter"

  it should "not change simple patterns" in {
    val actual = converter.convertToRegex("/test")
    actual.regex should equal("^/test$")
  }

  it should "escape regex metacharacters" in {
    val actual = converter.convertToRegex("/1\\/2^/3$/4./5|/6*/7+/8(/9)/10[/11{")
    actual.regex should equal("^/1\\\\/2\\^/3\\$/4\\./5\\|/6\\*/7\\+/8\\(/9\\)/10\\[/11\\{$")
  }

  it should "replace simple path variables" in {
    val actual = converter.convertToRegex("/collection/{id}")
    actual.regex should equal("^/collection/([^/]+?)$")
  }

  it should "replace path variables with customer regex expressions" in {
    val actual = converter.convertToRegex("/collection/{id: [a-zA-Z][a-zA-Z_0-9]}")
    actual.regex should equal("^/collection/([a-zA-Z][a-zA-Z_0-9])$")
  }

  it should "provide group names for path variables in matches" in {
    val line = "/collection/123/sub"
    val actual = converter.convertToRegex("/collection/{id}/{relationship}")
    actual.regex should equal("^/collection/([^/]+?)/([^/]+?)$")

    val m = actual.findFirstMatchIn(line)

    val pathParams = m match {
      case Some(m) => converter.getPathParameters(m)
      case _ => Map.empty[String, String]
    }

    pathParams("id") should equal("123")
    pathParams("relationship") should equal("sub")
  }

  it should "determine the pattern length with simple path variables" in {
    val actual = converter.getPatternLength("/collection/{id}")
    actual should equal(13)
  }

  it should "determine the pattern length with customer regex expressions" in {
    val actual = converter.getPatternLength("/collection/{id: [a-zA-Z][a-zA-Z_0-9]}")
    actual should equal(13)
  }
}