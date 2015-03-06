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

package com.monarchapis.apimanager.model

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.joda.time.DateTime

class ModelTests extends FlatSpec with Matchers {
  behavior of "Configuration"

  val config = Configuration(
    name = "test",
    properties = Map(
      "empty" -> List(),
      "single" -> List("one"),
      "many" -> List("one", "two", "three", "four", "five")))

  it should "return the first value or None from a provided list of values" in {
    config.value[String]("empty") should be(None)
    config.value[String]("single") should be(Some("one"))
    config.value[String]("many") should be(Some("one"))
  }

  it should "return all values from a provided list of values" in {
    config.values[String]("empty") should be(List())
    config.values[String]("single") should be(List("one"))
    config.values[String]("many") should be(List("one", "two", "three", "four", "five"))
  }

  behavior of "Key"

  val client = Client(
    id = "test",
    applicationId = "test",
    label = "test",
    enabled = true,
    apiKey = "1234",
    clientPermissionIds = Set(),
    authenticators = Map("test" -> Map(
      "empty" -> List(),
      "single" -> List("one"),
      "many" -> List("one", "two", "three", "four", "five"))))

  it should "return a valid Configuration for an authenticator or None" in {
    client.getAuthenticatorConfiguration("none") should be(None)

    val config = client.getAuthenticatorConfiguration("test") match {
      case Some(c) => c
      case _ => fail("configuration was not found")
    }

    config.name should be("test")
    config.values[String]("empty") should be(List())
    config.values[String]("single") should be(List("one"))
    config.values[String]("many") should be(List("one", "two", "three", "four", "five"))
  }

  behavior of "Token"

  private def createToken(created: DateTime) = Token(
    id = "1234",
    clientId = "1234",
    scheme = Some("test"),
    token = "1234",
    tokenType = "test",
    grantType = "test",
    createdDate = created,
    lastAccessedDate = created,
    expiresIn = Some(3600L),
    lifecycle = "test",
    userId = "test")

  it should "indicate that it is expired after the 'expiresIn' time has past" in {
    val created = DateTime.now.minusSeconds(3601)
    val token = createToken(created)

    token.isExpired should be(true)
  }

  it should "indicate that it is valid before the 'expiresIn' time has past" in {
    val created = DateTime.now.minusSeconds(3599)
    val token = createToken(created)

    token.isExpired should be(false)
  }
}