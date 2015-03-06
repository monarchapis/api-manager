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

import com.monarchapis.apimanager.model.OperationMatcher
import com.monarchapis.apimanager.util.RequestUtils
import org.apache.commons.lang3.StringUtils

class BasicVersionExtractor extends VersionExtractor {
  def extractVersion(request: AuthenticationRequest, operation: OperationMatcher, pathParameters: Map[String, String]): Option[String] = {
    operation.versionLocation foreach { location =>
      {
        val version = location match {
          case "path" => fromPath(pathParameters)
          case "header" => fromHeader(request)
          case "query" => fromQuery(request)
        }

        if (version.isDefined) {
          return Some(trimVersion(version.get))
        }
      }
    }

    operation.defaultVersion
  }

  def fromPath(pathParameters: Map[String, String]) = pathParameters.get("version")

  def fromHeader(request: AuthenticationRequest): Option[String] =
    request.getHeader("Accept") match {
      case Some(accept) => fromHeader(accept)
      case _ => None
    }

  def fromHeader(header: String): Option[String] = {
    if (header.indexOf(";") != -1) {
      val params = RequestUtils.parseMimeTypeParameters(StringUtils.substringAfter(header, ";").trim)
      val version = params.get("version")

      if (version.isDefined) {
        return version
      }
    }

    var mimeType = StringUtils.split(header, "; ")(0)
    mimeType = StringUtils.substringBeforeLast(mimeType, "+")
    mimeType = StringUtils.substringAfter(mimeType, "/")

    val parts = StringUtils.split(mimeType, '.')

    if ("vnd" == parts(0)) {
      val last = parts(parts.length - 1)

      if (last.startsWith("v")) {
        return Some(last)
      }
    }

    None
  }

  def fromQuery(request: AuthenticationRequest): Option[String] =
    request.querystring match {
      case Some(qs) => {
        val params = RequestUtils.parseUriParameters(qs)
        params.get("version")
      }
      case _ => None
    }

  def trimVersion(value: String) = {
    if (value.startsWith("version")) {
      value.substring(7)
    } else if (value.startsWith("v")) {
      value.substring(1)
    } else {
      value
    }
  }
}