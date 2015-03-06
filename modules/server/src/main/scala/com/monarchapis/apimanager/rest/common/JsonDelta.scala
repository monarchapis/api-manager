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

package com.monarchapis.apimanager.rest.common

import scala.collection.JavaConversions._
import java.io.InputStream
import org.apache.commons.io.IOUtils
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonNode

object JsonDelta {
  private val mapper = {
    val mapper = new ObjectMapper()

    val jodaModule = new JodaModule
    mapper.registerModule(jodaModule)

    mapper.registerModule(DefaultScalaModule)
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val module =
      new SimpleModule("BigDecimalDeserializerModule",
        new Version(1, 0, 0, null, null, null));
    module.addDeserializer(classOf[BigDecimal], new BigDecimalDeserializer);
    mapper.registerModule(module)

    mapper
  }
}

class JsonDelta[T <: Entity](
  private val id: String,
  private val input: InputStream,
  private val clazz: Class[T]) extends Delta[T] {
  val (json, entity) = {
    val s = IOUtils.toString(input)
    input.close

    val json = JsonDelta.mapper.readValue(s, classOf[JsonNode])
    val entity = JsonDelta.mapper.readValue(s, clazz).withId(id).asInstanceOf[T]

    (json, entity)
  }

  def pathChanged(path: String) = {
    var node = json
    path.split('.') foreach (s => node = node.path(s))
    !node.isMissingNode()
  }
  
  def changedKeys = json.fieldNames toSet
}