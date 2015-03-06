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

package com.monarchapis.apimanager.rest

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

import javax.servlet.http.HttpServletRequest
import javax.ws.rs._
import javax.ws.rs.core._

abstract class AbstractDocumentationResource(private val version: String) {
  @Path("/swagger.yaml")
  @GET
  @Produces(Array("*/*"))
  def getSwaggerYaml(@Context request: HttpServletRequest): Response = {
    val template = getTemplate(request)

    val mapper = new ObjectMapper(new YAMLFactory)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    val tree = mapper.readTree(template)
    val yaml = mapper.writeValueAsString(tree)

    val response = Response.ok(yaml)
    response.`type`("application/x-yaml")
    response.build
  }

  @Path("/swagger.json")
  @GET
  @Produces(Array("*/*"))
  def getSwaggerJson(@Context request: HttpServletRequest): Response = {
    val template = getTemplate(request)

    val mapper1 = new ObjectMapper(new YAMLFactory)
    val mapper2 = new ObjectMapper()
    mapper2.enable(SerializationFeature.INDENT_OUTPUT)
    val tree = mapper1.readTree(template)
    val json = mapper2.writeValueAsString(tree)

    val response = Response.ok(json)
    response.`type`("application/json")
    response.build
  }

  private def getTemplate(request: HttpServletRequest): String = {
    val is = this.getClass().getResourceAsStream(s"swagger.yaml.$version.template")

    try {
      var template = IOUtils.toString(is)

      template = StringUtils.replace(template, "${context}", request.getContextPath)
      template = StringUtils.replace(template, "${host}", request.getServerName + ":" + request.getServerPort)

      template
    } finally {
      IOUtils.closeQuietly(is)
    }
  }
}