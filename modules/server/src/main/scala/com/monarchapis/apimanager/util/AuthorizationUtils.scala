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

import scala.beans.BeanProperty
import scala.beans.BooleanBeanProperty

import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._

object EntityAction extends Enumeration {
  case class EValue(name: String) extends Val(name) {}

  val CREATE = EValue("create")
  val READ = EValue("read")
  val READ_SENSITIVE = EValue("readsensitive")
  val UPDATE = EValue("update")
  val DELETE = EValue("delete")
}

object AuthorizationUtils {
  val (authenticationService) = {
    val applicationContextProvider = ApplicationContextProvider()
    val authenticationService = applicationContextProvider.getBean(classOf[AuthenticationService])
    (authenticationService)
  }

  private def allowRead = Set("environment", "logEntry")

  private def authorizationMap = Map[EntityAction.Value, Set[String]](
    EntityAction.CREATE -> Set("fullaccess"),
    EntityAction.READ -> Set("redacted", "read", "readwrite", "fullaccess"),
    EntityAction.READ_SENSITIVE -> Set("read", "readwrite", "fullaccess"),
    EntityAction.UPDATE -> Set("readwrite", "fullaccess"),
    EntityAction.DELETE -> Set("fullaccess"))

  private def levelMap = Map[String, Set[String]](
    "redacted" -> Set("redacted", "read", "readwrite", "fullaccess"),
    "read" -> Set("read", "readwrite", "fullaccess"),
    "readwrite" -> Set("readwrite", "fullaccess"),
    "fullaccess" -> Set("fullaccess"))

  def validAccessLevels = Set(
    "token",
    "application",
    "client",
    "developer",
    "appDeveloper",
    "service",
    "plan",
    "permission",
    "message",
    "provider",
    "role",
    "logEntry",
    "principalProfile",
    "principalClaims")

  private def systemAccess = new Authorization {
    @BeanProperty val id = "system"
    @BeanProperty val name = "system"
    @BooleanBeanProperty val administrator = true
    @BeanProperty val permissions = Set[String]()
    @BeanProperty val accessLevels = Map(
      "token" -> "fullaccess",
      "application" -> "fullaccess",
      "client" -> "fullaccess",
      "developer" -> "fullaccess",
      "appDeveloper" -> "fullaccess",
      "service" -> "fullaccess",
      "plan" -> "fullaccess",
      "permission" -> "fullaccess",
      "message" -> "fullaccess",
      "provider" -> "fullaccess",
      "role" -> "fullaccess",
      "user" -> "fullaccess",
      "logEntry" -> "fullaccess",
      "principalProfile" -> "fullaccess",
      "principalClaims" -> "fullaccess")
  }

  private def authenticationAccess = new Authorization {
    @BeanProperty val id = "authentication"
    @BeanProperty val name = "authentication"
    @BooleanBeanProperty val administrator = false
    @BeanProperty val permissions = Set[String]()
    @BeanProperty val accessLevels = Map(
      "user" -> "fullaccess",
      "application" -> "read",
      "client" -> "read",
      "provider" -> "read",
      "plan" -> "read",
      "principalProfile" -> "read",
      "principalClaims" -> "read")
  }

  def validateAccessLevels(accessLevels: Map[String, String]) {
    accessLevels.keys foreach { accessLevel =>
      {
        if (!validAccessLevels(accessLevel)) {
          throw new InvalidParamaterException(s"$accessLevel is not a valid access level")
        }
      }
    }
  }

  def can(action: String) = {
    AuthorizationHolder.get match {
      case Some(authorization) => authorization.administrator || authorization.permissions(action)
      case _ => false
    }
  }

  def check(action: String) {
    if (!can(action)) {
      throw new NotAuthorizedException("You are not authorized to perform this action")
    }
  }

  def can(action: EntityAction.Value, entity: String): Boolean = {
    if (action == EntityAction.READ && allowRead(entity)) return true

    AuthorizationHolder.get match {
      case Some(authorization) =>
        (authorization.administrator && ("user" != entity || action == EntityAction.READ || authenticationService.isLocal)) ||
          (authorization.accessLevels.get(entity) match {
            case Some(accessLevel) => authorizationMap(action)(accessLevel)
            case _ => false
          })
      case _ => false
    }
  }

  def check(action: EntityAction.Value, entity: String) {
    if (!can(action, entity)) {
      throw new NotAuthorizedException("You are not authorized to perform this action")
    }
  }

  def hasAccessLevel(accessLevel: String, entity: String): Boolean = {
    levelMap.get(accessLevel) match {
      case Some(levelSet) => {
        AuthorizationHolder.get match {
          case Some(authorization) =>
            (authorization.administrator && ("user" != entity || authenticationService.isLocal)) ||
              (authorization.accessLevels.get(entity) match {
                case Some(level) => levelSet(level)
                case _ => false
              })
          case _ => false
        }
      }
      case _ => false
    }
  }

  def checkAccessLevel(accessLevel: String, entity: String) {
    if (!hasAccessLevel(accessLevel, entity)) {
      throw new NotAuthorizedException("You are not authorized to perform this action")
    }
  }

  def asSystem = {
    UserContext.current("system")
    AuthorizationHolder.current(systemAccess)
  }

  def continueWithSystemAccess = {
    AuthorizationHolder.current(systemAccess)
  }

  def continueWithAuthenticationAccess = {
    AuthorizationHolder.current(authenticationAccess)
  }

  def remove {
    UserContext.remove
    AuthorizationHolder.remove
  }
}