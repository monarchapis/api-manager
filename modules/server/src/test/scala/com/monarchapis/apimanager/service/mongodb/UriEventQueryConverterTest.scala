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

package com.monarchapis.apimanager.service.mongodb

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.monarchapis.apimanager.analytics.DictionaryStringShortener

class UriEventQueryConverterTest extends FlatSpec with Matchers {
  val shortener = new DictionaryStringShortener(Map(
    "application_id" -> "aid",
    "client_id" -> "cid",
    "service_id" -> "sid",
    "service_version" -> "ver",
    "operation_name" -> "oper",
    "provider_id" -> "pid",
    "request_size" -> "rqs",
    "response_size" -> "rss",
    "response_time" -> "rst",
    "status_code" -> "sc",
    "error_reason" -> "er",
    "cache_hit" -> "ch",
    "token_id" -> "tid",
    "user_id" -> "uid",
    "host" -> "h",
    "port" -> "p",
    "parameters" -> "pars",
    "headers" -> "hdrs",
    "client_ip" -> "cip",
    "user_agent" -> "ua"))

  behavior of "UriEventQueryConverter"

  val query = """
    user_agent.eq("iPhone 5s") + or([response_size.gt(4999).lt(5001), verb.in(["GET", "POST"])]) + path.regex(/\/pets.*/i)
    """.trim();

  it should "parse a valid expression" in {
    val parser = new UriEventQueryConverter(shortener)
    val dbo = parser(query)

    dbo should not be null

    dbo.get("ua") should equal("iPhone 5s")
  }
}