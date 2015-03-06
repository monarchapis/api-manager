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

package com.monarchapis.apimanager.service

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class EntityEventAggregatorTest extends FlatSpec with Matchers {
  behavior of "EntityEventAggregator"

  val aggregator = new EntityEventAggregator[String]

  val listener = (entity: String, eventType: String) => {
  }

  it should "allow for the adding and removeal of listeners" in {
    aggregator += (listener)
    aggregator.listenerCount should be(1)

    aggregator -= (listener)
    aggregator.listenerCount should be(0)
  }

  it should "send and recieve messages about enities to subscribed listeners" in {
    var called = false

    aggregator += ((entity: String, eventType: String) => {
      entity should be("test entity")
      eventType should be("test type")
      called = true
    })

    aggregator("test entity", "test type")
    called should be(true)
  }
}