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

class DictionaryShortenerTest extends FlatSpec with Matchers {
  behavior of "DictionaryStringShortener"

  it should "not allow collisions of short names" in {
    val caught = the[IllegalArgumentException] thrownBy {
      new DictionaryStringShortener(Map(
        "long name 1" -> "ln",
        "long name 2" -> "ln"))
    }

    caught.getMessage should be("Collision detected for short name 'ln'")
  }

  val shortener = new DictionaryStringShortener(Map(
    "long name 1" -> "ln1",
    "long name 2" -> "ln2"))

  it should "shorten a long name" in {
    shortener("long name 1") should be("ln1")
    shortener("long name 2") should be("ln2")
  }

  it should "expand a short name to the original long name" in {
    shortener.unapply("ln1") should be("long name 1")
    shortener.unapply("ln2") should be("long name 2")
  }

  it should "return the name unchanged is there is no shortened version" in {
    shortener("not shortened") should be("not shortened")
    shortener.unapply("not shortened") should be("not shortened")
  }
}