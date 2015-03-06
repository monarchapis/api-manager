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

import scala.collection.JavaConversions._
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import java.util.Enumeration
import com.monarchapis.apimanager.model.AuthorizationHolder
import com.monarchapis.apimanager.model.BehindReverseProxyHolder

class ApiRequestImpl(request: HttpServletRequest) extends HttpServletRequestWrapper(request) with ApiRequest {
  val body = {
    val is = request.getInputStream()
    val body = IOUtils.toByteArray(is)
    IOUtils.closeQuietly(is)
    body
  }

  override def getInputStream() = new ServletInputStreamWrapper(body)

  def content = new String(body, "UTF-8")

  def getURL = {
    val requestURL = request.getRequestURL
    val queryString = request.getQueryString

    if (queryString == null)
      requestURL.toString
    else
      requestURL.append('?').append(queryString).toString
  }

  def getRealURL = {
    val scheme = getRealScheme // http
    val serverName = getServerName // hostname.com
    val serverPort = getServerPort // 80
    val pathInfo = getPathInfo // /a/b;c=123
    val queryString = getQueryString // d=789

    // Reconstruct original requesting URL
    val url = new StringBuilder
    url.append(scheme).append("://").append(serverName)

    if ((serverPort != 80) && (serverPort != 443)) {
      url.append(":").append(serverPort)
    }

    url.append(getRealRequestPath)

    if (pathInfo != null) {
      url.append(pathInfo)
    }

    if (queryString != null) {
      url.append("?").append(queryString)
    }

    url.toString
  }

  lazy val headerMap: Map[String, List[String]] = {
    val headers = Map.newBuilder[String, List[String]]

    getHeaderNames.asInstanceOf[Enumeration[String]] foreach { h =>
      {
        headers += h -> getHeaders(h).asInstanceOf[Enumeration[String]].toList
      }
    }

    headers.result
  }

  def getRealRemoteAddr: String = {
    if (BehindReverseProxyHolder.current) {
      val ipAddress = StringUtils.trimToNull(getHeader("X-Forwarded-For"))

      // If there is an X-Forwarded-For header, pull the first IP in the comma
      // separated list.
      if (ipAddress != null) StringUtils.split(ipAddress, ',')(0).trim else getRemoteAddr
    } else super.getScheme
  }

  def getRealScheme: String = {
    if (BehindReverseProxyHolder.current) {
      val proto = StringUtils.trimToNull(getHeader("X-Forwarded-Proto"))

      // If there is an X-Forwarded-Proto header, pull the first value in the comma
      // separated list.
      if (proto != null) StringUtils.split(proto, ',')(0).trim else super.getScheme
    } else super.getScheme
  }

  lazy val getRequestPath: String = {
    new StringBuilder().append(getContextPath).append(getServletPath).toString
  }

  def getRealRequestPath: String = {
    if (BehindReverseProxyHolder.current) {
      val path = StringUtils.trimToNull(getHeader("X-Forwarded-Path"))

      // Since X-Forwarded-Path is not a de-facto reverse proxy header, we can assume there is only one value
      //
      if (path != null) path else getRequestPath
    } else getRequestPath
  }
}