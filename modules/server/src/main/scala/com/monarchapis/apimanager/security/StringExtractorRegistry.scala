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

import scala.collection.JavaConversions._

import com.monarchapis.apimanager.util._

import javax.inject.Inject

/////////////////////////

class StringExtractorRegistry(val extractors: StringExtractor*) extends Registry[StringExtractor](extractors: _*) {
  @Inject def this(extractors: java.util.List[StringExtractor]) = this(extractors: _*)

  def apply(request: AuthenticationRequest): Option[String] = {
    items foreach { item =>
      {
        val value = item.extract(request)
        if (value.isDefined) return value
      }
    }

    None
  }
}

/////////////////////////

class HeaderApiKeyExtractor(private val values: String*) extends StringExtractor with HeaderExtraction {
  def this(args: java.util.List[String]) = this(args.toSeq: _*)

  val keys = values.map(c => c.toLowerCase)
  val name = "Header API Key Extractor"
  def extract(request: AuthenticationRequest) = extractHeader(request, keys: _*)
}

class QueryStringApiKeyExtractor(private val values: String*) extends StringExtractor with ParameterExtraction {
  def this(args: java.util.List[String]) = this(args.toSeq: _*)

  val keys = values.map(c => c.toLowerCase)
  val name = "QueryString API Key Extractor"
  def extract(request: AuthenticationRequest): Option[String] = extractParameter(request, keys: _*)
}

class BearerTokenExtractor extends StringExtractor {
  val name = "Bearer Token Extractor"
  def extract(request: AuthenticationRequest): Option[String] = {
    AuthenticationUtils.getAuthorizationWithPrefix(request, "Bearer") match {
      case Some(token) => Some(token)
      case None => {
        request.querystring match {
          case Some(qs) => {
            val params = RequestUtils.parseUriParameters(qs)
            params.get("access_token")
          }
          case _ => None
        }
      }
    }
  }
}