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

import scala.collection.mutable.Builder

import org.joda.time.DateTime

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBEnvironmentService @Inject() (
  val connectionManager: MongoDBConnectionManager,
  val multitenantConnectionManager: MultitenantMongoDBConnectionManager,
  val logService: LogService) extends EnvironmentService with ServiceSupport[Environment] with MongoDBNameProvider with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(multitenantConnectionManager != null, "multitenantConnectionManager is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "environment"
  protected val displayName = "environment"
  protected val entityClass = classOf[Environment]
  protected val aggregator = EntityEventAggregator.environment

  protected val collection = connectionManager()("environments")

  ensureIndex(collection, MongoDBObject("name_lc" -> 1), "environment-by-name", true)
  ensureIndex(collection, MongoDBObject("authorizedUsers" -> 1, "name_lc" -> 1), "environment-by-authorized-user", true)
  ensureIndex(collection, MongoDBObject("ownerId" -> 1, "name_lc" -> 1), "environment-by-owner", true)
  ensureIndex(collection, MongoDBObject("database" -> 1), "environment-by-database", true)

  def databaseNames = {
    val builder = Set.newBuilder[String]

    collection.find(MongoDBObject(), MongoDBObject("systemDatabase" -> 1)) foreach { o =>
      {
        val database = o.getAs[String]("systemDatabase").get
        builder += database
      }
    }

    builder.result
  }

  protected val labelField = "name"
  protected val fieldMap = MongoSchema[Environment]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("name", "name", FieldType.CI_STRING, true, accessor = (e) => e.name)
    .field("description", "description", FieldType.CS_STRING, false, accessor = (e) => e.description.orNull)
    .field("systemDatabase", "systemDatabase", FieldType.CS_STRING, true, accessor = (e) => e.systemDatabase)
    .field("analyticsDatabase", "analyticsDatabase", FieldType.CS_STRING, true, accessor = (e) => e.analyticsDatabase)

  protected override def defaultSort = MongoDBObject("name_lc" -> 1)

  EntityEventAggregator.environment += onEnvironmentChange

  private def onEnvironmentChange(environment: Environment, eventType: String) {
    eventType match {
      case "delete" => {
        multitenantConnectionManager.dropDatabase(environment.systemDatabase)
        multitenantConnectionManager.dropDatabase(environment.analyticsDatabase)
      }
      case _ =>
    }
  }

  protected def convert(o: DBObject) = {
    Environment(
      id = o.get("_id").toString,
      name = expect[String](o.get("name")),
      description = optional[String](o.get("description")),
      systemDatabase = expect[String](o.get("systemDatabase")),
      analyticsDatabase = expect[String](o.get("analyticsDatabase")),
      createdBy = expect[String](o.get("createdBy")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      modifiedBy = expect[String](o.get("modifiedBy")),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate"))))
  }

  protected override def addSecurityFilter(builder: Builder[(String, Any), DBObject], filter: Map[String, List[String]]) {
    if (!AuthorizationUtils.hasAccessLevel("fullaccess", "environment")) {
      builder += "authorizedUsers" -> AuthorizationHolder.current.id
    }
  }

  def lookupIdByName(name: String): Option[String] = {
    val q = MongoDBObject("name_lc" -> name.toLowerCase)
    val o = collection.findOne(q, MongoDBObject("_id" -> 1))

    o match {
      case Some(obj) => Some(obj.get("_id").toString)
      case _ => None
    }
  }

  def getDatabases(id: String) = {
    val q = MongoDBObject("_id" -> new ObjectId(id))
    val d = collection.findOne(q, MongoDBObject("systemDatabase" -> 1, "analyticsDatabase" -> 1, "database" -> 1))

    d match {
      case Some(u: DBObject) => {
        val systemDatabase = u.getAsOrElse[String]("systemDatabase", u.getAsOrElse[String]("database", "monarch"))
        val analyticsDatabase = u.getAsOrElse[String]("analyticsDatabase", u.getAsOrElse[String]("database", "monarch"))

        Some(EnvironmentDatabases(systemDatabase, analyticsDatabase))
      }
      case _ => None
    }
  }

  def setAuthorizedUser(id: String, user: User, authorized: Boolean): Boolean = {
    val q = MongoDBObject("_id" -> new ObjectId(id))

    val o = if (authorized)
      $addToSet("authorizedUsers" -> user.id)
    else
      $pull("authorizedUsers" -> user.id)

    val result = collection.update(q, o, false, false, WriteConcern.FsyncSafe)

    result.getN == 1
  }

  def setDeveloperExtendedFields(id: String, fields: Map[String, Option[ExtendedField]]) = {
    val ret = setExtendedFields(id, fields, "developerExtendedFields")

    logService.log("info", s"${AuthorizationHolder.current.name} updated the developer extended fields")

    ret
  }

  def getDeveloperExtendedFields(id: String) = getExtendedFields(id, "developerExtendedFields")

  def setApplicationExtendedFields(id: String, fields: Map[String, Option[ExtendedField]]) = {
    val ret = setExtendedFields(id, fields, "applicationExtendedFields")

    logService.log("info", s"${AuthorizationHolder.current.name} updated the application extended fields")

    ret
  }

  def getApplicationExtendedFields(id: String) = getExtendedFields(id, "applicationExtendedFields")

  private def setExtendedFields(id: String, fields: Map[String, Option[ExtendedField]], base: String) = {
    val q = MongoDBObject("_id" -> new ObjectId(id))
    val add = MongoDBObject.newBuilder
    val remove = MongoDBObject.newBuilder

    fields foreach {
      case (key, value) => {
        value match {
          case Some(field: ExtendedField) => add += s"$base.$key" ->
            MongoDBObject(
              "fieldType" -> field.fieldType,
              "required" -> field.required,
              "pattern" -> field.pattern.orNull)
          case None => remove += s"$base.$key" -> 1
        }
      }
    }

    add += (
      "modifiedBy" -> UserContext.current,
      "modifiedDate" -> DateTime.now.toDate)

    val update = MongoDBObject(
      "$unset" -> remove.result,
      "$set" -> add.result)
    val result = collection.update(q, update, false, false, WriteConcern.FsyncSafe)
    result.getN() > 0
  }

  private def getExtendedFields(id: String, base: String) = {
    val q = MongoDBObject("_id" -> new ObjectId(id))
    val fields = collection.findOne(q, MongoDBObject(base -> 1))

    fields match {
      case Some(u: DBObject) => {
        val builder = Map.newBuilder[String, ExtendedField]
        val baseFields = u.get(base).asInstanceOf[DBObject]
        baseFields foreach {
          case (key: String, o: DBObject) => builder += key -> ExtendedField(
            fieldType = expect[String](o.get("fieldType")),
            required = expect[Boolean](o.get("required")),
            pattern = optional[String](o.get("pattern")))
        }
        builder.result
      }
      case _ => Map[String, ExtendedField]()
    }
  }

  def getUserEnvironments(userId: String) = {
    val environmentIds = collection.find(
      MongoDBObject("authorizedUsers" -> userId),
      MongoDBObject("_id" -> 1))
    (environmentIds.toList map (oid => oid.get("_id").toString)).toSet
  }

  def hasAccess(id: String): Boolean = {
    val user = UserContext.current
    if (user == "system") true
    else {
      val q = MongoDBObject(
        "_id" -> new ObjectId(id),
        "authorizedUsers" -> user)
      collection.findOne(q).isDefined
    }
  }

  override def toString = s"MongoDBEnvironmentService($connectionManager)"
}