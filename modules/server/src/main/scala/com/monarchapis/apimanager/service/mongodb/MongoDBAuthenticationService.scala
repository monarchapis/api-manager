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

package com.monarchapis.apimanager.service.mongodb

import org.apache.commons.lang3.RandomStringUtils
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._
import com.mongodb.casbah.Imports._
import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBAuthenticationService @Inject() (
  val connectionManager: MongoDBConnectionManager,
  val roleService: RoleService,
  val environmentService: EnvironmentService,
  val logService: LogService) extends AuthenticationService with MongoDBUtils with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(roleService != null, "roleService is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val collection = connectionManager()("users")

  override val isLocal = true

  def authenticate(userName: String, password: String) = {
    require(userName != null, "userName is required")
    require(password != null, "password is required")

    val q = MongoDBObject("userName_lc" -> userName.toLowerCase())
    val user = collection.findOne(q)

    user match {
      case Some(u) => {
        val salt = optional[String](u.get("salt"))
        val expected = optional[String](u.get("password"))

        if (salt.isDefined && expected.isDefined) {
          val sha256 = Hashing.sha256(password + salt.get)

          if (sha256 == expected.get) Some(convert(u)) else None
        } else None
      }
      case _ => None
    }
  }

  def setPassword(userName: String, password: String) {
    AuthorizationUtils.checkAccessLevel("fullaccess", "user")

    val q = MongoDBObject("userName_lc" -> userName)

    val salt = RandomStringUtils.randomAlphanumeric(10)
    val hashed = Hashing.sha256(password + salt)

    val o = MongoDBObject(
      "salt" -> salt,
      "password" -> hashed)
    val s = MongoDBObject("$set" -> o)

    collection.update(q, s, false, false, WriteConcern.FsyncSafe)

    if (EnvironmentContext.isSet) {
      logService.log("info", s"${AuthorizationHolder.current.name} reset password for user ${userName}")
    }
  }

  protected def convert(o: DBObject) = {
    AuthenticatedUser(
      id = o.get("_id").toString,
      userName = expect[String](o.get("userName")),
      firstName = expect[String](o.get("firstName")),
      lastName = expect[String](o.get("lastName")),
      administrator = o.getAsOrElse[Boolean]("administrator", false))
  }

  override def toString = s"MongoDBAuthenticationService($connectionManager)"
}