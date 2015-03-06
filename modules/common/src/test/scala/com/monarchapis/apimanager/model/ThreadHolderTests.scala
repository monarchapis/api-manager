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
import com.monarchapis.apimanager.exception.BadRequestException

class ThreadHolderTest extends FlatSpec with Matchers {
  behavior of "UserContext"

  it should "return a default of system" in {
    UserContext.current should be("system")
    UserContext.get should be(None)
    UserContext.getOrElse("not set") should be("not set")
  }

  it should "allow the user to be set" in {
    val testUser = "testUser"
    UserContext.current(testUser)
    UserContext.current should be(testUser)
    UserContext.get should be(Some(testUser))
    UserContext.getOrElse("not set") should be(testUser)
  }

  it should "allow the user to be cleared" in {
    val testUser = "testUser"
    UserContext.current(testUser)
    UserContext.current should be(testUser)
    UserContext.remove
    UserContext.current should be("system")
    UserContext.get should be(None)
    UserContext.getOrElse("not set") should be("not set")
  }

  behavior of "EnvironmentContext"

  val environmentContext = EnvironmentContext("environmentId", "environmentDatabase", "environmentAnalytics")

  it should "throw an exception if the value is not set" in {
    a[BadRequestException] should be thrownBy {
      EnvironmentContext.current
    }

    EnvironmentContext.get should be(None)
    EnvironmentContext.getOrElse(environmentContext) should be(environmentContext)
  }

  it should "allow the environment context to be set" in {
    EnvironmentContext.current(environmentContext)
    val current = EnvironmentContext.current
    current.id should be("environmentId")
    current.systemDatabase should be("environmentDatabase")

    EnvironmentContext.get should be(Some(environmentContext))
    EnvironmentContext.getOrElse(null) should be(environmentContext)
  }

  it should "allow the user to be cleared" in {
    EnvironmentContext.current(environmentContext)
    val current = EnvironmentContext.current
    current.id should be("environmentId")
    current.systemDatabase should be("environmentDatabase")
    EnvironmentContext.remove

    a[BadRequestException] should be thrownBy {
      EnvironmentContext.current
    }

    EnvironmentContext.get should be(None)
    EnvironmentContext.getOrElse(environmentContext) should be(environmentContext)
  }

  behavior of "AuthorizationHolder"

  val authorization = UserAuthorization(id = "test", name = "test")

  it should "throw an exception if the value is not set" in {
    a[BadRequestException] should be thrownBy {
      AuthorizationHolder.current
    }

    AuthorizationHolder.get should be(None)
    AuthorizationHolder.getOrElse(authorization) should be(authorization)
  }

  it should "allow the environment context to be set" in {
    AuthorizationHolder.current(authorization)
    val current = AuthorizationHolder.current
    current.name should be("test")

    AuthorizationHolder.get should be(Some(authorization))
    AuthorizationHolder.getOrElse(null) should be(authorization)
  }

  it should "allow the user to be cleared" in {
    AuthorizationHolder.current(authorization)
    val current = AuthorizationHolder.current
    current.name should be("test")
    AuthorizationHolder.remove

    a[BadRequestException] should be thrownBy {
      AuthorizationHolder.current
    }

    AuthorizationHolder.get should be(None)
    AuthorizationHolder.getOrElse(authorization) should be(authorization)
  }
}