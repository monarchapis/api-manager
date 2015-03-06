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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import javax.ws.rs._
import javax.ws.rs.core._
import javax.ws.rs.ext._
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationFeature

@Provider
@Produces(Array(MediaType.APPLICATION_JSON))
class JacksonContextResolver extends ContextResolver[ObjectMapper] {
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

  def getContext(clazz: Class[_]) = mapper
}

class BigDecimalDeserializer extends StdScalarDeserializer[BigDecimal](classOf[BigDecimal]) {
  override def deserialize(jp: JsonParser, ctxt: DeserializationContext): BigDecimal = {
    val t = jp.getCurrentToken

    if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
      return BigDecimal(jp.getDecimalValue)
    }

    // String is ok too, can easily convert
    if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
      val text = jp.getText.trim

      if (text.length() == 0) {
        return null;
      }

      try {
        return BigDecimal(text)
      } catch {
        case iae: IllegalArgumentException =>
          throw ctxt.weirdStringException(text, _valueClass, "not a valid representation")
      }
    }

    // Otherwise, no can do:
    throw ctxt.mappingException(_valueClass, t);
  }
}