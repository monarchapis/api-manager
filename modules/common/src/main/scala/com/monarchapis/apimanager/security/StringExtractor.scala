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

import com.monarchapis.apimanager.util.Registerable
import com.monarchapis.apimanager.util.RequestUtils

trait StringExtractor extends Registerable {
  def extract(request: AuthenticationRequest): Option[String]
}

trait HeaderExtraction {
  protected def extractHeader(request: AuthenticationRequest, validHeaders: String*): Option[String] = {
    val header = validHeaders find (header => {
      request.hasHeader(header)
    })

    header match {
      case Some(h) => request.getHeader(h)
      case _ => None
    }
  }
}

trait ParameterExtraction {
  protected def extractParameter(request: AuthenticationRequest, validParameters: String*): Option[String] = {
    request.querystring match {
      case Some(qs) => {
        val params = RequestUtils.parseUriParameters(qs)
        validParameters foreach { param =>
          {
            if (params.contains(param)) return Some(params(param))
          }
        }

        None
      }
      case _ => None
    }
  }
}