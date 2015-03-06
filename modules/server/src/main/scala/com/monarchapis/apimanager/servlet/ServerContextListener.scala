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

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util.ApplicationContextProvider
import grizzled.slf4j.Logging
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import com.monarchapis.apimanager.util.AuthorizationUtils

object ServerContextListener {
  lazy val globalSettings = ApplicationContextProvider.apply.getBean(classOf[GlobalSettings])
  lazy val logService = ApplicationContextProvider.apply.getBean(classOf[LogService])
  lazy val userService = ApplicationContextProvider.apply.getBean(classOf[UserService])
  lazy val authenticationService = ApplicationContextProvider.apply.getBean(classOf[AuthenticationService])
  lazy val roleService = ApplicationContextProvider.apply.getBean(classOf[RoleService])
  lazy val environmentService = ApplicationContextProvider.apply.getBean(classOf[EnvironmentService])
}
class ServerContextListener extends ServletContextListener with Logging {
  import ServerContextListener._

  def contextInitialized(event: ServletContextEvent) {
    val message = s"Server ${globalSettings.serverName} started"
    initializeEnvironmnts()
    info(message)
    logToEnvironments("info", message)
  }

  def contextDestroyed(event: ServletContextEvent) {
    val message = s"Server ${globalSettings.serverName} stopped"
    info(message)
    logToEnvironments("info", message)
  }

  private def initializeEnvironmnts() {
    try {
      AuthorizationUtils.asSystem

      var user: Option[User] = None;

      if (authenticationService.isLocal && userService.count() == 0) {
        val admin = userService.create(User(
          userName = "admin",
          firstName = "Administrator",
          lastName = "",
          administrator = true))
        userService.setAdmininstrator(admin.id, true)
        authenticationService.setPassword("admin", "admin")
        info("Created admin user account")
        user = Some(admin)
      }

      val environments = environmentService.find(0, 1000).items

      environments foreach { environment =>
        {
          val name = environment.name

          try {
            EnvironmentContext.current(
              EnvironmentContext(
                environment.id,
                environment.systemDatabase,
                environment.analyticsDatabase))

            if (roleService.count() == 0) {
              val role = roleService.create(Role(
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
                  "principalClaims" -> "fullaccess")))

              if (user.isEmpty) {
                user = userService.findByName("admin")
              }

              if (user.isDefined) {
                roleService.setUserRole(user.get, Some(role.id))
              }
            }
          } catch {
            case e: Exception => error(s"Error initializing environment ${name}", e)
          } finally {
            EnvironmentContext.remove
          }
        }
      }
    } finally {
      AuthorizationUtils.remove
    }
  }

  private def logToEnvironments(level: String, message: String) = {
    try {
      AuthorizationUtils.asSystem

      val environments = environmentService.find(0, 1000).items

      environments foreach { environment =>
        {
          val name = environment.name

          try {
            EnvironmentContext.current(
              EnvironmentContext(
                environment.id,
                environment.systemDatabase,
                environment.analyticsDatabase))

            logService.log(level, message)
          } catch {
            case e: Exception => error(s"Error logging to environment ${name}", e)
          } finally {
            EnvironmentContext.remove
          }
        }
      }
    } finally {
      AuthorizationUtils.remove
    }
  }
}