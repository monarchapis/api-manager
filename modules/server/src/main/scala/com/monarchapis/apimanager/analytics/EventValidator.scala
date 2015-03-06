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
import com.fasterxml.jackson.databind.node.ObjectNode
import com.monarchapis.apimanager.exception.BadRequestException

class EventValidator extends EventProcessor {
  val name = "Validator"

  def process(eventType: EventType, data: ObjectNode, args: String*) {
    eventType.requiredFields foreach (required =>
      {
        val fieldName = required.name

        if (!data.has(fieldName)) {
          val defaultValue = required.default.getOrElse(throw new BadRequestException(s"$fieldName is a required field"))

          required.`type` match {
            case FieldType.ARRAY => throw new BadRequestException(s"$fieldName is a required field")
            case FieldType.BOOLEAN => data.put(fieldName, defaultValue.toBoolean)
            case FieldType.CODE => data.put(fieldName, defaultValue.toInt)
            case FieldType.DECIMAL => data.put(fieldName, new java.math.BigDecimal(defaultValue))
            case FieldType.INTEGER => data.put(fieldName, defaultValue.toInt)
            case FieldType.OBJECT => throw new BadRequestException(s"$fieldName is a required field")
            case FieldType.STRING => data.put(fieldName, defaultValue)
          }
        }
      })

    for (entry <- data.fields) {
      val fieldName = entry.getKey
      val value = entry.getValue

      if (!value.isNull()) {
        val field = eventType.field(fieldName).getOrElse(throw new BadRequestException(s"Invalid field $fieldName"))

        field.`type` match {
          case FieldType.ARRAY => if (!value.isArray) throw new BadRequestException(s"$fieldName must be an array")
          case FieldType.BOOLEAN => if (!value.isBoolean) throw new BadRequestException(s"$fieldName must be a boolean")
          case FieldType.CODE => if (!value.isIntegralNumber) throw new BadRequestException(s"$fieldName must be a code (integer)")
          case FieldType.DECIMAL => if (!value.isFloatingPointNumber) throw new BadRequestException(s"$fieldName must be a decimal")
          case FieldType.INTEGER => if (!value.isIntegralNumber) throw new BadRequestException(s"$fieldName must be a integer")
          case FieldType.OBJECT => if (!value.isObject) throw new BadRequestException(s"$fieldName must be an object")
          case FieldType.STRING => if (!value.isTextual) throw new BadRequestException(s"$fieldName must be a string")
        }
      }
    }
  }
}