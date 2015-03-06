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

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.monarchapis.apimanager.exception.BadRequestException

class EventValidatorTest extends FlatSpec with Matchers {
  val input = """
events:
  -
    name        : test
    timezone	: America/New_York
    fields      :
      - 
        name        : string_value
        storeAs     : sv
        type        : string
        required    : true
      -
        name        : boolean_value
        storeAs     : bv
        type        : boolean
        required    : true
      -
        name        : int_value
        storeAs     : iv
        type        : integer
        required    : true
      -
        name        : decimal_value
        storeAs     : dv
        type        : decimal
        required    : true
      -
        name        : code_value
        storeAs     : cv
        type        : code
        required    : true
      -
        name        : object_value
        storeAs     : ov
        type        : object
        required    : true
      -
        name        : array_value
        storeAs     : av
        type        : array
        required    : true
      -
        name        : optional_value
        storeAs     : ov
        type        : integer
        required    : false
      - 
        name        : string_default
        storeAs     : sd
        type        : string
        required    : true
        default     : test
      -
        name        : boolean_default
        storeAs     : bd
        type        : boolean
        required    : true
        default     : true
      -
        name        : int_default
        storeAs     : id
        type        : integer
        required    : true
        default     : 54321
      -
        name        : decimal_default
        storeAs     : dd
        type        : decimal
        required    : true
        default     : 543.21
      -
        name        : code_default
        storeAs     : dd
        type        : code
        required    : true
        default     : 404
"""

  val config = {
    val mapper = new ObjectMapper(new YAMLFactory)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    mapper.registerModule(DefaultScalaModule)
    mapper.readValue(input, classOf[AnalyticsConfiguration])
  }

  val eventType = config.event("test").getOrElse(throw new IllegalStateException("Could not find test event type"))

  val validator = new EventValidator

  behavior of "EventValidator"

  it should "verify all required fields are present that do not have a default value" in {
    val event = createObject.put("dummy", "dummy")

    val thrown = the[BadRequestException] thrownBy {
      validator.process(eventType, event)
    }
    thrown.message should be("string_value is a required field")
  }

  it should "verify that all fields are known" in {
    val event = createObject
      .put("dummy", "dummy")
      .put("string_value", "1234")
      .put("boolean_value", true)
      .put("int_value", 123)
      .put("decimal_value", 123.456)
      .put("code_value", 404)
    event.putObject("object_value").put("test", "test")
    event.putArray("array_value").add("one").add("two")

    val thrown = the[BadRequestException] thrownBy {
      validator.process(eventType, event)
    }
    thrown.message should be("Invalid field dummy")
  }

  it should "verify the type of each field" in {
    val event1 = createObject
      .put("string_value", 1234)
      .put("boolean_value", true)
      .put("int_value", 123)
      .put("decimal_value", 123.456)
      .put("code_value", 404)
    event1.putObject("object_value").put("test", "test")
    event1.putArray("array_value").add("one").add("two")

    val thrown1 = the[BadRequestException] thrownBy {
      validator.process(eventType, event1)
    }
    thrown1.message should be("string_value must be a string")

    val event2 = createObject
      .put("string_value", 1234)
      .put("boolean_value", true)
      .put("int_value", 123)
      .put("decimal_value", 123.456)
      .put("code_value", 404)
    event2.putObject("object_value").put("test", "test")
    event2.putArray("array_value").add("one").add("two")

    val thrown2 = the[BadRequestException] thrownBy {
      validator.process(eventType, event2)
    }
    thrown2.message should be("string_value must be a string")
  }

  it should "assign default values for required fields that aren't set" in {
    val event = createObject
      .put("string_value", "1234")
      .put("boolean_value", true)
      .put("int_value", 123)
      .put("decimal_value", 123.456)
      .put("code_value", 404)
    event.putObject("object_value").put("test", "test")
    event.putArray("array_value").add("one").add("two")

    validator.process(eventType, event)

    val stringDefault = event.get("string_default")
    stringDefault should not be null
    stringDefault.isTextual should be(true)
    stringDefault.asText should be("test")

    val booleanDefault = event.get("boolean_default")
    booleanDefault should not be null
    booleanDefault.isBoolean should be(true)
    booleanDefault.asBoolean should be(true)

    val intDefault = event.get("int_default")
    intDefault should not be null
    intDefault.isInt should be(true)
    intDefault.asInt should be(54321)

    val decimalDefault = event.get("decimal_default")
    decimalDefault should not be null
    decimalDefault.isFloatingPointNumber should be(true)
    decimalDefault.asText should be("543.21")

    val codeDefault = event.get("code_default")
    codeDefault should not be null
    codeDefault.isInt should be(true)
    codeDefault.asInt should be(404)
  }

  private def createObject = JsonNodeFactory.instance.objectNode
}