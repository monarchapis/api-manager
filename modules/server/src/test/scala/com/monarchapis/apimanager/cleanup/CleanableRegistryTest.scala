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

package com.monarchapis.apimanager.cleanup

import org.mockito.internal.util.reflection.Whitebox
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.helpers.SubstituteLogger
import grizzled.slf4j.Logger

class CleanableRegistryTest extends FlatSpec with Matchers {
  behavior of "CleanableRegistry"

  it should "register cleanup tasks and execute them" in {
    var called = false;

    val task = new Cleanable {
      def cleanup = called = true
    }
    val registry = new CleanableRegistry(task)

    registry.cleanAll

    called should be(true)
  }

  it should "catch exceptions and log them" in {
    var logged = false

    val task = new Cleanable {
      def cleanup = throw new RuntimeException("testing an exception")
    }

    val registry = new CleanableRegistry(task) {
      override def error(msg: => Any, t: => Throwable) {
        logged = true
      }
    }

    registry.cleanAll

    logged should be(true)
  }
}