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

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext

class BigDecimalDeserializerTest extends FlatSpec with Matchers with MockitoSugar {
  val ctxt = mock[DeserializationContext]
  when(ctxt.weirdStringException(anyString(), any(classOf[Class[_]]), anyString())).thenThrow(new RuntimeException("test"))

  behavior of "BigDecimalDeserializer"

  it should "convert numbers, floats, and valid strings to BigDecimal" in {
    val deserializer = new BigDecimalDeserializer

    val intJson = mock[JsonParser]
    when(intJson.getCurrentToken).thenReturn(JsonToken.VALUE_NUMBER_INT)
    when(intJson.getDecimalValue).thenReturn(new java.math.BigDecimal("100"))

    val actual1 = deserializer.deserialize(intJson, ctxt)
    actual1 should equal(BigDecimal("100"))

    val floatJson = mock[JsonParser]
    when(floatJson.getCurrentToken).thenReturn(JsonToken.VALUE_NUMBER_FLOAT)
    when(floatJson.getDecimalValue).thenReturn(new java.math.BigDecimal("100.5"))

    val actual2 = deserializer.deserialize(floatJson, ctxt)
    actual2 should equal(BigDecimal("100.5"))

    val stringJson = mock[JsonParser]
    when(stringJson.getCurrentToken).thenReturn(JsonToken.VALUE_STRING)
    when(stringJson.getText).thenReturn("100.8")

    val actual3 = deserializer.deserialize(stringJson, ctxt)
    actual3 should equal(BigDecimal("100.8"))
  }

  it should "throw an exception for invalid strings and other bad input" in {
    val deserializer = new BigDecimalDeserializer

    val stringJson = mock[JsonParser]
    when(stringJson.getCurrentToken).thenReturn(JsonToken.VALUE_STRING)
    when(stringJson.getText).thenReturn("1000asdfafasdf")

    an[Exception] should be thrownBy deserializer.deserialize(stringJson, ctxt)
  }
}