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

package com.monarchapis.apimanager.rest.command

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.monarchapis.apimanager.command.CommandProcessorRegistry
import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.exception.NotFoundException
import com.monarchapis.apimanager.rest.AbstractDocumentationResource

import javax.inject.Inject
import javax.inject.Named
import javax.ws.rs._

@Path("/v1")
@Named
class CommandResource @Inject() (eventProcessorRegistry: CommandProcessorRegistry) {
  require(eventProcessorRegistry != null, "eventProcessorRegistry is required")

  @Path("/{commandType}")
  @POST
  def processCommand(@PathParam("commandType") commandType: String, request: JsonNode) {
    eventProcessorRegistry(commandType) match {
      case Some(commandProcessor) => {
        val clazz = commandProcessor.objectType
        val data = mapper.readValue(request.toString, clazz)
        commandProcessor.processAny(data)
      }
      case _ => throw new NotFoundException(s"$commandType is not a valid command processor")
    }
  }

  private lazy val mapper: ObjectMapper = {
    val mapper = new ObjectMapper

    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.registerModule(DefaultScalaModule)

    val jodaModule = new JodaModule
    mapper.registerModule(jodaModule)
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    mapper
  }
}

@Path("/v1")
@Named
class CommandDocumentationResource extends AbstractDocumentationResource("V1")