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

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.Matchers
import org.scalatest.FlatSpec
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.FilterConfig

class CorsFilterTest extends FlatSpec with Matchers with MockitoSugar {
  behavior of "CorsFilter"

  it should "omit the CORS response headers when the Origin request header is not set" in {
    val filter = new CorsFilter

    val config = mock[FilterConfig]
    val request = mock[HttpServletRequest]
    val response = mock[HttpServletResponse]
    val chain = mock[FilterChain]

    when(config.getInitParameter("allowOrigin")).thenReturn(null)
    when(config.getInitParameter("allowMethods")).thenReturn(null)
    when(config.getInitParameter("allowHeaders")).thenReturn(null)

    when(request.getHeader("Origin")).thenReturn(null)
    when(request.getMethod()).thenReturn("OPTIONS")

    filter.init(config)

    filter.doFilter(request, response, chain)

    verify(response).setHeader("Allow", "GET, POST, HEAD, OPTIONS, DELETE, PUT, PATCH")
    verify(response).setStatus(200)

    verify(response, times(0)).setHeader("Access-Control-Allow-Origin", "localhost:80")
    verify(response, times(0)).setHeader("Access-Control-Allow-Methods", "GET, POST, HEAD, OPTIONS, DELETE, PUT, PATCH")
    verify(response, times(0)).setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers, Api-Key, X-Api-Key, Authorization, X-Environment-Id")
    verify(response, times(0)).setHeader("Access-Control-Max-Age", "1728000")
    verify(response, times(0)).setHeader("Access-Control-Allow-Credentials", "true")
  }

  it should "adding the proper CORS response headers when the Origin request header is set" in {
    val filter = new CorsFilter

    val config = mock[FilterConfig]
    val request = mock[HttpServletRequest]
    val response = mock[HttpServletResponse]
    val chain = mock[FilterChain]

    when(config.getInitParameter("allowOrigin")).thenReturn(null)
    when(config.getInitParameter("allowMethods")).thenReturn(null)
    when(config.getInitParameter("allowHeaders")).thenReturn(null)

    when(request.getHeader("Origin")).thenReturn("localhost:80")
    when(request.getMethod()).thenReturn("OPTIONS")

    filter.init(config)

    filter.doFilter(request, response, chain)

    verify(response, times(0)).setHeader("Allow", "GET, POST, HEAD, OPTIONS, DELETE, PUT, PATCH")
    verify(response, times(0)).setStatus(200)

    verify(response).setHeader("Access-Control-Allow-Origin", "localhost:80")
    verify(response).setHeader("Access-Control-Allow-Methods", "GET, POST, HEAD, OPTIONS, DELETE, PUT, PATCH")
    verify(response).setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers, Api-Key, X-Api-Key, Authorization, X-Environment-Id")
    verify(response).setHeader("Access-Control-Max-Age", "1728000")
    verify(response).setHeader("Access-Control-Allow-Credentials", "true")
  }

  it should "allow the configuration of origin, methods, and headers" in {
    val filter = new CorsFilter

    val config = mock[FilterConfig]
    val request = mock[HttpServletRequest]
    val response = mock[HttpServletResponse]
    val chain = mock[FilterChain]

    when(config.getInitParameter("allowOrigin")).thenReturn("example.com:80")
    when(config.getInitParameter("allowMethods")).thenReturn("GET, POST")
    when(config.getInitParameter("allowHeaders")).thenReturn("Content-Type, Accept, Origin")

    when(request.getHeader("Origin")).thenReturn("localhost:80")
    when(request.getMethod()).thenReturn("OPTIONS")

    filter.init(config)

    filter.doFilter(request, response, chain)

    verify(response, times(0)).setHeader("Allow", "GET, POST")
    verify(response, times(0)).setStatus(200)

    verify(response).setHeader("Access-Control-Allow-Origin", "example.com:80")
    verify(response).setHeader("Access-Control-Allow-Methods", "GET, POST")
    verify(response).setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, Origin")
    verify(response).setHeader("Access-Control-Max-Age", "1728000")
    verify(response).setHeader("Access-Control-Allow-Credentials", "true")

    when(request.getHeader("Origin")).thenReturn(null)

    filter.doFilter(request, response, chain)

    verify(response).setHeader("Allow", "GET, POST")
    verify(response).setStatus(200)
  }
}