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

import java.io.ByteArrayInputStream

import scala.beans.BeanProperty

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.monarchapis.apimanager.model.Entity

class JsonDeltaTest extends FlatSpec with Matchers {
  val delta = {
    val json = "{ \"property1\" : \"test1\", \"property2\" : { \"property1\" : \"test2\" } }"
    val input = new ByteArrayInputStream(json.getBytes)
    val delta = new JsonDelta("1234", input, classOf[ParentCaseClass])
    delta
  }

  behavior of "JsonDelta"

  it should "parse the input and produce the desired class" in {
    val entity = delta.entity
    entity.property1 should equal(Some("test1"))
    entity.property2 should equal(Some(ChildCaseClass(Some("test2"), None)))
  }

  it should "return true if a scalar property was present" in {
    val changed = delta.pathChanged("property1")
    changed should equal(true)
  }

  it should "return false if a scalar property was not present" in {
    val notChanged = delta.pathChanged("doesNotExist")
    notChanged should equal(false)
  }

  it should "return true if a nested property was present" in {
    val nestedChanged = delta.pathChanged("property2.property1")
    nestedChanged should equal(true)
  }

  it should "return false if a nested property was not present" in {
    val nestedChanged = delta.pathChanged("property2.property2")
    nestedChanged should equal(false)
  }
}

case class ParentCaseClass(
  @BeanProperty id: String,
  @BeanProperty property1: Option[String],
  @BeanProperty property2: Option[ChildCaseClass]) extends Entity {
  def withId(id: String) = copy(id = id)
  def withProperty1(property1: Option[String]) = copy(property1 = property1)
  def withProperty2(property2: Option[ChildCaseClass]) = copy(property2 = property2)
}

case class ChildCaseClass(
  @BeanProperty property1: Option[String],
  @BeanProperty property2: Option[String]) {
  def withProperty1(property1: Option[String]) = copy(property1 = property1)
  def withProperty2(property2: Option[String]) = copy(property2 = property2)
}
