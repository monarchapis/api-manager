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

package com.monarchapis.apimanager.analytics

import scala.collection.JavaConversions._
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import javax.inject.Inject
import com.fasterxml.jackson.databind.node.ObjectNode
import com.mongodb.BasicDBList
import com.mongodb.DBObject
import com.fasterxml.jackson.databind.node.ArrayNode

trait JsonKeyChanger {
  protected def convertObject(json: JsonNode): JsonNode = {
    if (!json.isObject()) return json

    val converted = JsonNodeFactory.instance.objectNode()
    putFields(json, converted)

    converted
  }

  private def putFields(json: JsonNode, converted: ObjectNode) {
    for (entry <- json.fields) {
      val field = handleKey(entry.getKey)
      val value = entry.getValue

      put(converted, field, value)
    }
  }

  private def put(converted: ObjectNode, field: String, value: JsonNode) {
    if (value.isBigDecimal) {
      converted.put(field, value.decimalValue)
      //} else if (value.isBigInteger) {
      //      converted.put(field, value.bigIntegerValue)
    } else if (value.isBoolean) {
      converted.put(field, value.booleanValue)
    } else if (value.isDouble) {
      converted.put(field, value.doubleValue)
    } else if (value.isFloat) {
      converted.put(field, value.floatValue)
    } else if (value.isInt) {
      converted.put(field, value.intValue)
    } else if (value.isLong) {
      converted.put(field, value.longValue)
    } else if (value.isShort) {
      converted.put(field, value.shortValue)
    } else if (value.isTextual) {
      converted.put(field, value.textValue)
    } else if (value.isArray) {
      val array = converted.putArray(field)

      for (i <- 0 until value.size) {
        add(array, value.get(i))
      }
    } else if (value.isObject) {
      val o = converted.putObject(field)
      putFields(value, o)
    } else {
      throw new IllegalStateException("Unhandled value type")
    }
  }

  private def add(converted: ArrayNode, value: JsonNode) {
    if (value.isObject) {
      val o = converted.addObject()
      putFields(value, o)
    } else if (value.isBigDecimal) {
      converted.add(value.decimalValue)
      //} else if (value.isBigInteger) {
      //      converted.put(field, value.bigIntegerValue)
    } else if (value.isBoolean) {
      converted.add(value.booleanValue)
    } else if (value.isDouble) {
      converted.add(value.doubleValue)
    } else if (value.isFloat) {
      converted.add(value.floatValue)
    } else if (value.isInt) {
      converted.add(value.intValue)
    } else if (value.isLong) {
      converted.add(value.longValue)
    } else if (value.isShort) {
      converted.add(value.shortValue)
    } else if (value.isTextual) {
      converted.add(value.textValue)
    } else if (value.isArray) {
      val array = converted.addArray

      for (i <- 0 until value.size) {
        add(array, value.get(i))
      }
    } else {
      throw new IllegalStateException("Unhandled value type")
    }
  }

  protected def handleKey(key: String): String
}

class JsonCompressor(@Inject shortener: StringShortener) extends JsonProcessor with JsonKeyChanger {
  def apply(json: JsonNode): JsonNode = convertObject(json)

  override def handleKey(key: String) = shortener(key)
}

class JsonExpander(@Inject shortener: StringShortener) extends JsonProcessor with JsonKeyChanger {
  def apply(json: JsonNode): JsonNode = convertObject(json)

  override def handleKey(key: String) = shortener.unapply(key)
}