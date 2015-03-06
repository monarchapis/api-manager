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

package com.monarchapis.apimanager.servlet

import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.springframework.context.ApplicationContext

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._

import javax.annotation.PreDestroy

class ServerContextListenerTest extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfter {
  behavior of "ServerContextListener"

  val logService = mock[LogService]
  val userService = mock[UserService]
  val authenticationService = mock[AuthenticationService]
  val roleService = mock[RoleService]
  val environmentService = mock[EnvironmentService]

  when(authenticationService.isLocal).thenReturn(true)

  val environment = Environment(
    id = "1234",
    name = "test",
    description = None,
    systemDatabase = "test",
    analyticsDatabase = "test")

  when(environmentService.find(0, 1000)).thenReturn(PagedList(0, 1000, 1, 1, List(environment)))

  val user = User(
    userName = "admin",
    firstName = "Administrator",
    lastName = "",
    administrator = true)
  val createdUser = user.withId("1")

  when(userService.create(user)).thenReturn(createdUser)
  when(userService.findByName("admin")).thenReturn(Some(createdUser))

  val role = Role(
    roleName = "admin",
    displayName = "Administrator",
    description = Some("Environment administrator"),
    permissions = Set(),
    accessLevels = Map(
      "token" -> "fullaccess",
      "application" -> "fullaccess",
      "client" -> "fullaccess",
      "developer" -> "fullaccess",
      "service" -> "fullaccess",
      "plan" -> "fullaccess",
      "permission" -> "fullaccess",
      "message" -> "fullaccess",
      "provider" -> "fullaccess",
      "role" -> "fullaccess",
      "logEntry" -> "fullaccess",
      "principalProfile" -> "fullaccess",
      "principalClaims" -> "fullaccess"))
  val createdRole = role.withId("1")

  when(roleService.create(role)).thenReturn(createdRole)

  before {
    val context = mock[ApplicationContext]
    val provider = new ApplicationContextProvider
    provider.setApplicationContext(context)

    when(context.getBean(classOf[GlobalSettings])).thenReturn(GlobalSettings(serverName = "test"))
    when(context.getBean(classOf[LogService])).thenReturn(logService)
    when(context.getBean(classOf[UserService])).thenReturn(userService)
    when(context.getBean(classOf[AuthenticationService])).thenReturn(authenticationService)
    when(context.getBean(classOf[RoleService])).thenReturn(roleService)
    when(context.getBean(classOf[EnvironmentService])).thenReturn(environmentService)
  }

  after {
    val provider = new ApplicationContextProvider
    provider.destroy
    AuthorizationHolder.remove
    UserContext.remove
  }

  it should "log when the server is started and stopped" in {
    when(userService.count()).thenReturn(1)
    when(roleService.count()).thenReturn(1)

    val listener = new ServerContextListener
    listener.contextInitialized(null)

    verify(logService).log("info", "Server test started")

    listener.contextDestroyed(null)

    verify(logService).log("info", "Server test stopped")
  }

  it should "create the admin account if the user count is 0" in {
    when(userService.count()).thenReturn(0)
    when(roleService.count()).thenReturn(1)

    val listener = new ServerContextListener
    listener.contextInitialized(null)

    verify(userService).create(user)
    verify(userService).setAdmininstrator(createdUser.id, true)
    verify(authenticationService).setPassword("admin", "admin")
  }

  it should "create the admin role for each environment where the role count is 0 and add the admin user to it" in {
    when(userService.count()).thenReturn(1)
    when(roleService.count()).thenReturn(0)

    val listener = new ServerContextListener
    listener.contextInitialized(null)

    verify(roleService).create(role)
    verify(roleService).setUserRole(createdUser, Some(createdRole.id))
  }
}