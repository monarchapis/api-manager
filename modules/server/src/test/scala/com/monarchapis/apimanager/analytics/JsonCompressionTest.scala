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

class JsonCompressionTest extends FlatSpec with Matchers {
  val mapper = new ObjectMapper

  val input = """
    |{
    |	"key1" : 1,
    |	"key2" : 2.2,
    |	"key3" : true,
    |	"key4" : "value",
    |	"key5" : {
    |		"one" : 1,
    |		"two" : "two",
    |		"three" : {
    |			"sub1" : "one",
    |			"sub2" : "two"
    |		}
  	|	}
    |}
    """.trim.stripMargin

  val inputNode = mapper.readTree(input)

  val inputJson = mapper.writeValueAsString(inputNode)

  val output = """
    |{
    |	"v1" : 1,
    |	"v2" : 2.2,
    |	"v3" : true,
    |	"v4" : "value",
    |	"v5" : {
    |		"v6" : 1,
    |		"v7" : "two",
    |		"v8" : {
    |			"v9" : "one",
    |			"v10" : "two"
    |		}
  	|	}
    |}
    """.trim.stripMargin

  val outputNode = mapper.readTree(output)

  val outputJson = mapper.writeValueAsString(outputNode)

  val shortener = new StringShortener {
    private val lookup = collection.mutable.Map[String, String]()
    private var i = 0;

    def apply(longName: String): String = {
      i += 1;
      val shortName = s"v$i"

      lookup += (shortName -> longName)
      shortName
    }

    def unapply(shortName: String): String = lookup.get(shortName) match {
      case Some(name) => name
      case _ => throw new IllegalStateException(s"could not find short name $shortName")
    }
  }

  behavior of "JsonCompressor"

  val compressor = new JsonCompressor(shortener)

  it should "compress object keys to shorter strings" in {
    val compressed = compressor(inputNode)
    val compressedJson = mapper.writeValueAsString(compressed)

    compressedJson should be(outputJson)
  }

  behavior of "JsonExpander"

  val expander = new JsonExpander(shortener)

  it should "expand object keys to original values" in {
    val expanded = expander(outputNode)
    val expandedJson = mapper.writeValueAsString(expanded)

    expandedJson should be(inputJson)
  }
}