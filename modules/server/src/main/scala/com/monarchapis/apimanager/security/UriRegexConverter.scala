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

import org.apache.commons.lang3.StringUtils
import scala.util.matching.Regex

object UriRegexConverter {
  private val REGEX_METACHARACTERS = Array(
    "\\",
    "^",
    "$",
    ".",
    "|",
    "?",
    "*",
    "+",
    "(",
    ")",
    "[",
    "{")
  private val REGEX_ESCAPED = Array(
    "\\\\",
    "\\^",
    "\\$",
    "\\.",
    "\\|",
    "\\?",
    "\\*",
    "\\+",
    "\\(",
    "\\)",
    "\\[",
    "\\{")

  def escape(value: String) = {
    StringUtils.replaceEach(value,
      REGEX_METACHARACTERS,
      REGEX_ESCAPED)
  }
}

class UriRegexConverter {
  import UriRegexConverter._

  def convertToRegex(uriPattern: String) = {
    val parts = StringUtils.split(uriPattern, '/')
    val sb = new StringBuilder
    val groupNames = Seq.newBuilder[String]

    sb.append('^')

    parts foreach { part =>
      {
        if (part.length() > 0) {
          sb.append('/')

          val next = if (part.startsWith("{") && part.endsWith("}")) {
            val variable = part.substring(1, part.length() - 1)

            if (variable.contains(":")) {
              groupNames += StringUtils.substringBefore(variable, ":")
              "(" + StringUtils.substringAfter(variable, ":").trim + ")"
            } else {
              groupNames += variable
              "([^/]+?)"
            }
          } else {
            escape(part)
          }

          sb.append(next)
        }
      }
    }

    sb.append('$')

    new Regex(sb.toString, groupNames.result: _*)
  }

  def getPathParameters(regexMatch: Regex.Match) = {
    val builder = Map.newBuilder[String, String]

    regexMatch.groupNames foreach { group =>
      {
        builder += group -> regexMatch.group(group)
      }
    }

    builder.result
  }

  def getPatternLength(uriPattern: String) = {
    val parts = StringUtils.split(uriPattern, '/')
    val sb = new StringBuilder

    parts foreach { part =>
      {
        if (part.length() > 0) {
          sb.append('/')

          val next = if (part.startsWith("{") && part.endsWith("}")) {
            "_"
          } else {
            part
          }

          sb.append(next)
        }
      }
    }

    sb.toString.length
  }
}