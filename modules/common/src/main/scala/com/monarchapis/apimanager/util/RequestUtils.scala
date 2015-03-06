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

import java.net.URLDecoder
import org.apache.commons.lang3.StringUtils

object RequestUtils {
  def parseUriParameters(uri: String): Map[String, String] = {
    val params = Map.newBuilder[String, String]
    val parts = uri split "\\?"
    val query = if (parts.length > 1) parts(1) else parts(0)

    query split "&" map { param =>
      val pair = param split "="
      val key = URLDecoder.decode(pair(0), "UTF-8").toLowerCase
      val value = pair.length match {
        case l if l > 1 => URLDecoder.decode(pair(1), "UTF-8")
        case _ => ""
      }

      params += key -> value
    }

    params.result
  }

  def parseMimeTypeParameters(value: String) = {
    val params = Map.newBuilder[String, String]

    StringUtils.split(value, ", ") map { param =>
      val pair = param split "="
      val key = pair(0).toLowerCase
      val value = pair.length match {
        case l if l > 1 => pair(1)
        case _ => ""
      }

      params += key -> value
    }

    params.result
  }
}