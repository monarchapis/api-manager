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

package com.monarchapis.apimanager.util

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class RegistryTest extends FlatSpec with Matchers {
  behavior of "EntityEventAggregator"

  it should "retrieve a Registerable by name" in {
    val registerable1 = new Registerable {
      val name = "test1"
    }

    val registerable2 = new Registerable {
      val name = "test2"
    }

    val registry = new Registry[Registerable](registerable1, registerable2) {
    }

    registry("none") should be(None)
    registry("test1") should be(Some(registerable1))
    registry("test2") should be(Some(registerable2))
  }
}