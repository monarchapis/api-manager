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

import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import com.monarchapis.apimanager.model.AuthorizationHolder
import com.monarchapis.apimanager.model.Authorization
import org.springframework.context.ApplicationContext
import org.scalatest.BeforeAndAfter
import com.monarchapis.apimanager.exception.NotAuthorizedException
import com.monarchapis.apimanager.model.UserContext

class AuthorizationUtilsTest extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfter {
  behavior of "AuthorizationUtils"

  before {
    val context = mock[ApplicationContext]
    val provider = new ApplicationContextProvider
    provider.setApplicationContext(context)
  }

  after {
    val provider = new ApplicationContextProvider
    provider.destroy
    AuthorizationHolder.remove
    UserContext.remove
  }

  it should "test if the user can perform an action" in {
    val authorization = mock[Authorization]
    AuthorizationHolder.current(authorization)

    when(authorization.permissions).thenReturn(Set("test"))

    AuthorizationUtils.can("test") should equal(true)
    AuthorizationUtils.check("test") // Should not throw an exception

    AuthorizationUtils.can("other") should equal(false)
    a[NotAuthorizedException] should be thrownBy {
      AuthorizationUtils.check("other")
    }
  }

  it should "test if the user has a specific access level to an entity" in {
    val authorization = mock[Authorization]
    AuthorizationHolder.current(authorization)

    //----

    when(authorization.accessLevels).thenReturn(Map.empty[String, String])

    AuthorizationUtils.hasAccessLevel("redacted", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("redacted", "application")

    AuthorizationUtils.hasAccessLevel("read", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("read", "application")

    AuthorizationUtils.hasAccessLevel("readwrite", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("readwrite", "application")

    AuthorizationUtils.hasAccessLevel("fullaccess", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("fullaccess", "application")

    //----

    when(authorization.accessLevels).thenReturn(Map("application" -> "redacted"))

    AuthorizationUtils.hasAccessLevel("redacted", "application") should equal(true)
    AuthorizationUtils.checkAccessLevel("redacted", "application") // Should not throw an exception

    AuthorizationUtils.hasAccessLevel("read", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("read", "application")

    AuthorizationUtils.hasAccessLevel("readwrite", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("readwrite", "application")

    AuthorizationUtils.hasAccessLevel("fullaccess", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("fullaccess", "application")

    //----

    when(authorization.accessLevels).thenReturn(Map("application" -> "read"))

    AuthorizationUtils.hasAccessLevel("redacted", "application") should equal(true)
    AuthorizationUtils.checkAccessLevel("redacted", "application") // Should not throw an exception

    AuthorizationUtils.hasAccessLevel("read", "application") should equal(true)
    AuthorizationUtils.checkAccessLevel("read", "application") // Should not throw an exception

    AuthorizationUtils.hasAccessLevel("readwrite", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("readwrite", "application")

    AuthorizationUtils.hasAccessLevel("fullaccess", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("fullaccess", "application")

    //----

    when(authorization.accessLevels).thenReturn(Map("application" -> "readwrite"))

    AuthorizationUtils.hasAccessLevel("redacted", "application") should equal(true)
    AuthorizationUtils.checkAccessLevel("redacted", "application") // Should not throw an exception

    AuthorizationUtils.hasAccessLevel("read", "application") should equal(true)
    AuthorizationUtils.checkAccessLevel("read", "application") // Should not throw an exception

    AuthorizationUtils.hasAccessLevel("readwrite", "application") should equal(true)
    AuthorizationUtils.checkAccessLevel("readwrite", "application") // Should not throw an exception

    AuthorizationUtils.hasAccessLevel("fullaccess", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("fullaccess", "application")

    //----

    when(authorization.accessLevels).thenReturn(Map("application" -> "fullaccess"))

    AuthorizationUtils.hasAccessLevel("redacted", "application") should equal(true)
    AuthorizationUtils.checkAccessLevel("redacted", "application") // Should not throw an exception

    AuthorizationUtils.hasAccessLevel("read", "application") should equal(true)
    AuthorizationUtils.checkAccessLevel("read", "application") // Should not throw an exception

    AuthorizationUtils.hasAccessLevel("readwrite", "application") should equal(true)
    AuthorizationUtils.checkAccessLevel("readwrite", "application") // Should not throw an exception

    AuthorizationUtils.hasAccessLevel("fullaccess", "application") should equal(true)
    AuthorizationUtils.checkAccessLevel("fullaccess", "application") // Should not throw an exception
  }

  it should "pass the access level check if the entity and access level are valid" in {
    val authorization = mock[Authorization]
    AuthorizationHolder.current(authorization)

    when(authorization.accessLevels).thenReturn(Map("application" -> "unknown", "foo" -> "fullacces"))

    AuthorizationUtils.hasAccessLevel("redacted", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("redacted", "application")

    AuthorizationUtils.hasAccessLevel("read", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("read", "application")

    AuthorizationUtils.hasAccessLevel("readwrite", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("readwrite", "application")

    AuthorizationUtils.hasAccessLevel("fullaccess", "application") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("fullaccess", "application")

    AuthorizationUtils.hasAccessLevel("fullaccess", "foo") should equal(false)
    a[NotAuthorizedException] should be thrownBy AuthorizationUtils.checkAccessLevel("fullaccess", "foo")
  }

  it should "allow the thread to run as the system" in {
    AuthorizationUtils.asSystem
    UserContext.current should equal("system")
    val auth = AuthorizationHolder.current
    auth.name should equal("system")
    auth.administrator should equal(true)
  }

  it should "allow the thread to continue with system privileges" in {
    UserContext.current("test")
    AuthorizationUtils.continueWithSystemAccess
    UserContext.current should equal("test")
    val auth = AuthorizationHolder.current
    auth.name should equal("system")
    auth.administrator should equal(true)

    AuthorizationUtils.validAccessLevels foreach (entity => {
      AuthorizationUtils.hasAccessLevel("fullaccess", entity) should equal(true)
    })
  }

  it should "allow the thread to continue with authentication privileges" in {
    UserContext.current("test")
    AuthorizationUtils.continueWithAuthenticationAccess
    UserContext.current should equal("test")
    val auth = AuthorizationHolder.current
    auth.name should equal("authentication")
    auth.administrator should equal(false)

    AuthorizationUtils.validAccessLevels foreach (entity => {
      AuthorizationUtils.hasAccessLevel("fullaccess", entity) should equal(entity == "user")
    })
  }
}