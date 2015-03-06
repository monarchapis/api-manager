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

import scala.collection.JavaConversions
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import javax.servlet.http.HttpServletRequest
import com.monarchapis.apimanager.model.BehindReverseProxyHolder
import org.scalatest.BeforeAndAfter
import java.io.ByteArrayInputStream
import java.io.InputStream
import com.google.common.collect.Lists
import java.util.Vector

class ApiRequestImplTest extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfter {
  behavior of "ApiRequestImpl"

  before {
    BehindReverseProxyHolder.current(true)
  }

  after {
    BehindReverseProxyHolder.remove
  }

  it should "provide access to the body content of the request (byte array & string)" in {
    val request = mock[HttpServletRequest]
    val is = new ServletInputStreamWrapper("test".getBytes())
    when(request.getInputStream()).thenReturn(is)

    val apiRequest = new ApiRequestImpl(request)
    new String(apiRequest.body) should equal("test")
    apiRequest.content should equal("test")
  }

  it should "calculate the path with the querystring if provided" in {
    val request = mock[HttpServletRequest]
    val is = new ServletInputStreamWrapper("test".getBytes())
    when(request.getInputStream()).thenReturn(is)
    val sb = new StringBuffer("/test")
    when(request.getRequestURL).thenReturn(sb)

    val apiRequest = new ApiRequestImpl(request)
    apiRequest.getURL should equal("/test")

    when(request.getQueryString).thenReturn("a=1&b=2")
    apiRequest.getURL should equal("/test?a=1&b=2")
  }

  it should "calculate the full URL" in {
    val request = mock[HttpServletRequest]
    val is = new ServletInputStreamWrapper("test".getBytes())
    when(request.getInputStream()).thenReturn(is)

    when(request.getScheme).thenReturn("http")
    when(request.getServerName).thenReturn("example.com")
    when(request.getServerPort).thenReturn(80)
    when(request.getPathInfo).thenReturn(";c=123")
    when(request.getQueryString).thenReturn("a=1&b=2")
    when(request.getContextPath).thenReturn("/myAPI")
    when(request.getServletPath).thenReturn("/entity/1")

    val apiRequest = new ApiRequestImpl(request)

    apiRequest.getRealURL should equal("http://example.com/myAPI/entity/1;c=123?a=1&b=2")
  }

  it should "provide access to all headers as a map" in {
    val request = mock[HttpServletRequest]
    val is = new ServletInputStreamWrapper("test".getBytes())
    when(request.getInputStream()).thenReturn(is)

    val headers = Lists.newArrayList("header1", "header2")
    val enumeration = new Vector(headers).elements()
    when(request.getHeaderNames).thenReturn(enumeration)

    val header1 = Lists.newArrayList("value1", "value2")
    val enumeration1 = new Vector(header1).elements()
    when(request.getHeaders("header1")).thenReturn(enumeration1)

    val header2 = Lists.newArrayList("value3", "value4")
    val enumeration2 = new Vector(header2).elements()
    when(request.getHeaders("header2")).thenReturn(enumeration2)

    val apiRequest = new ApiRequestImpl(request)

    apiRequest.headerMap should equal(Map("header1" -> List("value1", "value2"), "header2" -> List("value3", "value4")))
  }

  it should "provide access to the remote IP address, even if request went through a proxy" in {
    val request = mock[HttpServletRequest]
    val is = new ServletInputStreamWrapper("test".getBytes())
    when(request.getInputStream()).thenReturn(is)

    when(request.getRemoteAddr).thenReturn("127.0.0.1")
    when(request.getHeader("X-Forwarded-For")).thenReturn("10.10.10.250")

    val apiRequest = new ApiRequestImpl(request)

    apiRequest.getRealRemoteAddr should equal("10.10.10.250")
  }

  it should "provide access to the protocol, even if request went through a proxy and SSL was terminated" in {
    val request = mock[HttpServletRequest]
    val is = new ServletInputStreamWrapper("test".getBytes())
    when(request.getInputStream()).thenReturn(is)

    when(request.getScheme).thenReturn("http")
    when(request.getHeader("X-Forwarded-Proto")).thenReturn("https")

    val apiRequest = new ApiRequestImpl(request)

    apiRequest.getRealScheme should equal("https")
  }

  it should "provide access to the path pre-rewriting from a proxy" in {
    val request = mock[HttpServletRequest]
    val is = new ServletInputStreamWrapper("test".getBytes())
    when(request.getInputStream()).thenReturn(is)

    when(request.getContextPath).thenReturn("/myAPI")
    when(request.getServletPath).thenReturn("/entity/1")
    when(request.getHeader("X-Forwarded-Path")).thenReturn("/entity/1")

    val apiRequest = new ApiRequestImpl(request)

    apiRequest.getRequestPath should equal("/myAPI/entity/1")
    apiRequest.getRealRequestPath should equal("/entity/1")
  }
}