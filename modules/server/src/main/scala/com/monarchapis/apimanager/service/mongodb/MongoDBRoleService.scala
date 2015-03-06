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

import scala.collection.JavaConversions._

import org.bson.types.ObjectId
import org.joda.time.DateTime

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBRoleService @Inject() (
  val connectionManager: MultitenantMongoDBConnectionManager,
  val logService: LogService) extends RoleService with ServiceSupport[Role] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "role"
  protected val displayName = "role"
  protected val entityClass = classOf[Role]
  protected val aggregator = EntityEventAggregator.role
  private val epoch = new DateTime().withMillis(0)

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("roles")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing roles")
    val collection = db("roles")
    ensureIndex(collection, MongoDBObject("roleName_lc" -> 1), "role-by-roleName", true)
  })

  protected val labelField = "roleName"
  protected val fieldMap = MongoSchema[Role]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("roleName", "roleName", FieldType.CI_STRING, true, accessor = (e) => e.roleName)
    .field("displayName", "displayName", FieldType.CI_STRING, true, accessor = (e) => e.displayName)
    .field("description", "description", FieldType.CS_STRING, true, accessor = (e) => e.description.orNull)
    .field("permissions", "permissions", FieldType.OTHER, false, accessor = (e) => toList(e.permissions))
    .field("accessLevels", "accessLevels", FieldType.OTHER, false, accessor = (e) => {
      AuthorizationUtils.validateAccessLevels(e.accessLevels)
      toObject(e.accessLevels)
    })

  protected override def defaultSort = MongoDBObject("roleName_lc" -> 1)

  protected def convert(o: DBObject) = {
    new Role(
      id = o.get("_id").toString,
      roleName = expect[String](o.get("roleName")),
      displayName = expect[String](o.get("displayName")),
      description = optional[String](o.get("description")),
      permissions = set[String](o.getAs[MongoDBList]("permissions")),
      accessLevels = convertAccessLevels(o.getAs[DBObject]("accessLevels")),
      createdBy = expect[String](o.get("createdBy"), "system"),
      createdDate = expect[DateTime](datetime(o.get("createdDate")), epoch),
      modifiedBy = expect[String](o.get("modifiedBy"), "system"),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate")), epoch))
  }

  def isWritable = true

  def findByName(roleName: String) = {
    val q = MongoDBObject("roleName_lc" -> roleName.toLowerCase())
    create(collection.findOne(q))
  }

  def getUserRole(user: User) = {
    val q = MongoDBObject("members" -> user.id)
    create(collection.findOne(q))
  }

  def setUserRole(user: User, roleId: Option[String]): Boolean = {
    val db = collection.getDB
    db.requestStart

    try {
      val q = MongoDBObject("members" -> user.id)
      val u = MongoDBObject("$pull" -> MongoDBObject("members" -> user.id))
      collection.update(q, u, false, false, WriteConcern.FsyncSafe)

      roleId match {
        case Some(roleId) => {
          val q = MongoDBObject("_id" -> new ObjectId(roleId))
          val u = MongoDBObject("$addToSet" -> MongoDBObject("members" -> user.id))
          val result = collection.update(q, u, false, false, WriteConcern.FsyncSafe)
          result.getN() > 0
        }
        case _ => false
      }
    } finally {
      db.requestDone
    }
  }

  def getMembers(role: Role) = {
    val q = MongoDBObject("_id" -> new ObjectId(role.id))

    val doc = collection.findOne(q)

    doc match {
      case Some(r) => expect[java.util.List[String]](r.get("members")).toSet
      case _ => Set()
    }
  }

  def getUserRoles(user: User) = {
    val q = MongoDBObject("members" -> user.id)
    val cursor = collection.find(q)

    cursor map { r => convert(r) } toSet
  }

  def addMember(role: Role, user: User) = {
    val q = MongoDBObject("_id" -> new ObjectId(role.id))
    val u = MongoDBObject("$addToSet" -> MongoDBObject("members" -> user.id))
    val result = collection.update(q, u, false, false, WriteConcern.FsyncSafe)
    result.getN() > 0
  }

  def removeMember(role: Role, user: User) = {
    val q = MongoDBObject("_id" -> new ObjectId(role.id))
    val u = MongoDBObject("$pull" -> MongoDBObject("members" -> user.id))
    val result = collection.update(q, u, false, false, WriteConcern.FsyncSafe)
    result.getN() > 0
  }

  override def toString = s"MongoDBRoleService($connectionManager)"
}