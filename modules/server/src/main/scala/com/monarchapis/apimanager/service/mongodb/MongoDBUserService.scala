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

class MongoDBUserService @Inject() (
  val connectionManager: MongoDBConnectionManager,
  val roleService: RoleService,
  val environmentService: EnvironmentService,
  val logService: LogService) extends UserService with ServiceSupport[User] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(roleService != null, "roleService is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "user"
  protected val displayName = "user"
  protected val entityClass = classOf[User]
  protected val aggregator = EntityEventAggregator.user

  protected val collection = connectionManager()("users")

  ensureIndex(collection, MongoDBObject("userName_lc" -> 1), "user-by-userName", true)
  // Sparse index by externalId
  ensureIndex(collection,
    MongoDBObject("externalId" -> 1),
    MongoDBObject(
      "name" -> "user-by-externalId",
      "unique" -> true,
      "sparse" -> true))

  protected val labelField = "userName"
  protected val fieldMap = MongoSchema[User]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("userName", "userName", FieldType.CI_STRING, true, accessor = (e) => e.userName)
    .field("firstName", "firstName", FieldType.CI_STRING, true, accessor = (e) => e.firstName)
    .field("lastName", "lastName", FieldType.CI_STRING, true, accessor = (e) => e.lastName)
    .field("externalId", "externalId", FieldType.CS_STRING, true, accessor = (e) => NotSet.check(e.externalId.orNull))

  protected override def defaultSort = MongoDBObject("userName" -> 1)

  override def load(id: String) = {
    // Role membership is stored in the role entity and has to be loaded from there.
    super.load(id) match {
      case Some(user) => {
        if (EnvironmentContext.isSet) {
          roleService.getUserRole(user) match {
            case Some(role) => Some(user.withRoleId(Some(role.id)))
            case _ => Some(user)
          }
        } else {
          Some(user)
        }
      }
      case _ => None
    }
  }

  def findByExternalId(externalId: String) = {
    AuthorizationUtils.check(EntityAction.READ, entityName)

    try {
      val q = DBObject("externalId" -> externalId)
      val entity = create(collection.findOne(q))
      if (entity.isDefined) checkReadAccess(entity.get)

      entity match {
        case Some(user) => {
          if (EnvironmentContext.isSet) {
            roleService.getUserRole(user) match {
              case Some(role) => Some(user.withRoleId(Some(role.id)))
              case _ => Some(user)
            }
          } else {
            Some(user)
          }
        }
        case _ => None
      }
    } catch {
      case iar: IllegalArgumentException => None
    }
  }

  override def update(user: User): Option[User] = {
    val ret = super.update(user)

    roleService.setUserRole(ret.get, user.roleId)

    environmentService.setAuthorizedUser(EnvironmentContext.current.id, user, user.roleId.isDefined)

    ret
  }

  override def update(delta: Delta[User]): Option[User] = {
    val ret = super.update(delta)

    if (ret.isDefined && delta.pathChanged("roleId")) {
      roleService.setUserRole(ret.get, delta.entity.roleId)
      environmentService.setAuthorizedUser(EnvironmentContext.current.id, ret.get, delta.entity.roleId.isDefined)
    }

    ret
  }

  def setAdmininstrator(userId: String, administrator: Boolean) {
    val q = MongoDBObject("_id" -> new ObjectId(userId))

    val o = if (administrator)
      $set("administrator" -> true)
    else
      $unset("administrator")

    collection.update(q, o, false, false, WriteConcern.FsyncSafe)
  }

  protected def convert(o: DBObject) = {
    User(
      id = o.get("_id").toString,
      userName = expect[String](o.get("userName")),
      firstName = expect[String](o.get("firstName")),
      lastName = expect[String](o.get("lastName")),
      administrator = o.getAsOrElse[Boolean]("administrator", false),
      externalId = optional[String](o.get("externalId")))
  }

  def findByName(userName: String) = {
    require(userName != null, "userName is required")

    val q = MongoDBObject("userName_lc" -> userName.toLowerCase())
    create(collection.findOne(q))
  }

  protected override def handleExpand(items: List[User], expand: Set[String]) = {
    var ret = items

    if (expand.contains("role")) {
      ret = ret.map(i => i.withRole(roleService.getUserRole(i)))
    }

    ret
  }

  override def toString = s"MongoDBUserService($connectionManager)"
}