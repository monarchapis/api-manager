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

package com.monarchapis.apimanager.servlet

import javax.servlet.FilterConfig
import javax.servlet.Filter
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletResponse
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletResponse

class CorsFilter extends Filter {
  private var allowOrigin: String = null
  private var allowMethods: String = null
  private var allowHeaders: String = null

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val req = request.asInstanceOf[HttpServletRequest]
    val res = response.asInstanceOf[HttpServletResponse]

    val origin = req.getHeader("Origin")
    val isOptionsMethod = "OPTIONS".equalsIgnoreCase(req.getMethod())

    if (origin == null) {
      // Return standard response if OPTIONS request w/o Origin header
      if (isOptionsMethod) {
        res.setHeader("Allow", allowMethods)
        res.setStatus(200)
        return
      }
    } else {
      // Some browsers don't like * with allowCredentials=true so just
      // echo back the Origin.
      res.setHeader("Access-Control-Allow-Origin", if (allowOrigin == "*") { origin } else { allowOrigin })
      res.setHeader("Access-Control-Allow-Methods", allowMethods)
      res.setHeader("Access-Control-Allow-Headers", allowHeaders)
      res.setHeader("Access-Control-Max-Age", "1728000")
      res.setHeader("Access-Control-Allow-Credentials", "true")
    }

    // Pass request down the chain, except for OPTIONS
    if (!isOptionsMethod) {
      chain.doFilter(request, response)
    }
  }

  override def destroy {}

  override def init(config: FilterConfig) {
    allowOrigin = getInitParameter(config, "allowOrigin", "*")
    allowMethods = getInitParameter(config, "allowMethods",
      "GET, POST, HEAD, OPTIONS, DELETE, PUT, PATCH")
    allowHeaders = getInitParameter(
      config,
      "allowHeaders",
      "Content-Type, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers, Api-Key, X-Api-Key, Authorization, X-Environment-Id")
  }

  private def getInitParameter(config: FilterConfig, name: String, defaultValue: String) = {
    val value = config.getInitParameter(name)

    if (value == null) {
      defaultValue
    } else {
      value
    }
  }
}