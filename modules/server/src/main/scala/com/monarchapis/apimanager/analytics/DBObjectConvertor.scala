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
import org.bson.types.BasicBSONList
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.bson.types.ObjectId
import org.apache.commons.lang3.time.DateFormatUtils

class DBObjectConvertor(shortener: StringShortener) {
  def apply(json: JsonNode): DBObject = {
    val converted = new BasicDBObject

    apply(json, converted)
    converted
  }

  def apply(json: JsonNode, converted: DBObject) {
    if (!json.isObject()) throw new IllegalStateException("can only convert json objects")

    for (entry <- json.fields) {
      val field = shortener(entry.getKey)
      val value = entry.getValue

      converted.put(field, convert(value))
    }
  }

  private def convert(value: JsonNode): AnyRef = {
    if (value.isObject) {
      apply(value)
    } else if (value.isBigDecimal) {
      value.decimalValue
    } else if (value.isBigInteger) {
      value.bigIntegerValue
    } else if (value.isBoolean) {
      value.booleanValue.asInstanceOf[AnyRef]
    } else if (value.isDouble) {
      value.doubleValue.asInstanceOf[AnyRef]
    } else if (value.isFloat) {
      value.floatValue.asInstanceOf[AnyRef]
    } else if (value.isInt) {
      value.intValue.asInstanceOf[AnyRef]
    } else if (value.isLong) {
      value.longValue.asInstanceOf[AnyRef]
    } else if (value.isShort) {
      value.shortValue.asInstanceOf[AnyRef]
    } else if (value.isTextual) {
      value.textValue
    } else if (value.isArray) {
      val array = new BasicDBList

      for (i <- 0 until value.size) {
        array.add(convert(value.get(i)))
      }

      array
    } else if (value.isNull) {
      null
    } else {
      throw new IllegalStateException("Unhandled value type")
    }
  }

  def unapply(dbObject: DBObject): JsonNode = {
    val converted = JsonNodeFactory.instance.objectNode
    putFields(dbObject, converted)
    converted
  }

  private def putFields(dbObject: DBObject, converted: ObjectNode): Unit = {
    for (field <- dbObject.keySet()) {
      val value = dbObject.get(field)

      put(converted, shortener.unapply(field), value)
    }
  }

  private def put(converted: ObjectNode, field: String, value: Any) {
    value match {
      case v: java.lang.Boolean => converted.put(field, v)
      case v: java.lang.Double => converted.put(field, v)
      case v: java.lang.Float => converted.put(field, v)
      case v: java.lang.Integer => converted.put(field, v)
      case v: java.math.BigDecimal => converted.put(field, v)
      case v: java.lang.Long => converted.put(field, v)
      case v: java.lang.Short => converted.put(field, v)
      case v: java.lang.String => converted.put(field, v)
      case v: java.util.Date => converted.put(field, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(v))
      case v: java.util.List[_] => {
        val array = converted.putArray(field)

        for (i <- 0 until v.size) {
          add(array, v.get(i))
        }
      }
      case v: DBObject => {
        val o = converted.putObject(field)
        putFields(v, o)
      }
      case v: ObjectId =>
      case _ => throw new IllegalStateException("Unhandled value type for " + value)
    }
  }

  private def add(converted: ArrayNode, value: Any) {
    value match {
      case v: java.lang.Boolean => converted.add(v)
      case v: java.lang.Double => converted.add(v)
      case v: java.lang.Float => converted.add(v)
      case v: java.lang.Integer => converted.add(v)
      case v: java.math.BigDecimal => converted.add(v)
      case v: java.lang.Long => converted.add(v)
      case v: java.lang.Short => converted.add(v.toInt)
      case v: java.lang.String => converted.add(v)
      case v: java.util.List[_] => {
        val array = converted.addArray

        for (i <- 0 until v.size) {
          add(array, v.get(i))
        }
      }
      case v: DBObject => converted.add(unapply(v))
      case _ => throw new IllegalStateException("Unhandled value type")
    }
  }
}