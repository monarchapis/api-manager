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

import scala.math.BigDecimal.int2bigDecimal

import org.joda.time.DateTime

import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBAppDeveloperService @Inject() (
  val applicationService: ApplicationService,
  val developerService: DeveloperService,
  val connectionManager: MultitenantMongoDBConnectionManager,
  val logService: LogService) extends AppDeveloperService with ServiceSupport[AppDeveloper] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "appDeveloper"
  override protected val displayName = "app developer"
  protected val entityClass = classOf[AppDeveloper]
  protected val aggregator = EntityEventAggregator.appDeveloper

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("appDevelopers")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing app developers")
    val collection = db("appDevelopers")

    ensureIndex(collection, MongoDBObject("applicationId" -> 1, "developerId" -> 1), "by-applicationId", true)
    ensureIndex(collection, MongoDBObject("developerId" -> 1, "applicationId" -> 1), "by-developerId", true)
  })

  protected val labelField = null // TODO find a label for logging
  protected val fieldMap = MongoSchema[AppDeveloper]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("applicationId", "applicationId", FieldType.CS_STRING, true, accessor = (e) => e.applicationId, extras = (set, e) => {
      if (!applicationService.exists(e.applicationId)) {
        throw new ConflictException("Could not find the referenced application")
      }
    })
    .field("developerId", "developerId", FieldType.CS_STRING, true, accessor = (e) => e.developerId, extras = (set, e) => {
      if (!developerService.exists(e.developerId)) {
        throw new ConflictException("Could not find the referenced developer")
      }
    })
    .field("role", "role", FieldType.CS_STRING, true, accessor = (e) => e.role)

  protected override def defaultSort = MongoDBObject("name_lc" -> 1)

  EntityEventAggregator.application += onApplicationChange
  EntityEventAggregator.developer += onDeveloperChange

  private def onApplicationChange(application: Application, eventType: String) {
    eventType match {
      case "delete" => collection.remove(MongoDBObject("applicationId" -> application.id))
      case _ =>
    }
  }

  private def onDeveloperChange(developer: Developer, eventType: String) {
    eventType match {
      case "delete" => collection.remove(MongoDBObject("developerId" -> developer.id))
      case _ =>
    }
  }

  def associate(applicationId: String, developerId: String, role: String) {
    AuthorizationUtils.check(EntityAction.UPDATE, entityName)

    val user = UserContext.current
    val now = DateTime.now.toDate

    val q = DBObject(
      "applicationId" -> applicationId,
      "developerId" -> developerId)

    val u = DBObject(
      "$set" -> DBObject("applicationId" -> applicationId,
        "developerId" -> developerId,
        "role" -> role,
        "modifiedBy" -> user,
        "modifiedDate" -> now),
      "$setOnInsert" -> DBObject(
        "createdBy" -> user,
        "createdDate" -> now))

    val result = collection.update(q, u, upsert = true)
  }

  def remove(applicationId: String, developerId: String): Boolean = {
    require(applicationId != null, "applicationId is required")
    require(developerId != null, "developerId is required")
    AuthorizationUtils.check(EntityAction.DELETE, entityName)

    val q = MongoDBObject(
      "developerId" -> developerId,
      "applicationId" -> applicationId)

    val check = create(collection.findOne(q))
    if (check.isEmpty) return false

    val res = collection.remove(q, WriteConcern.FsyncSafe)

    if (res.getN == 1) {
      aggregator.apply(check.get, "delete")
      true
    } else false
  }

  def findByApplicationId(applicationId: String) = {
    require(applicationId != null, "applicationId is required")
    AuthorizationUtils.check(EntityAction.READ, entityName)

    val q = MongoDBObject("applicationId" -> applicationId)
    val data = collection.find(q)

    data.toList.map(i => convert(i))
  }

  def findByDeveloperId(developerId: String) = {
    require(developerId != null, "developerId is required")
    AuthorizationUtils.check(EntityAction.READ, entityName)

    val q = MongoDBObject("developerId" -> developerId)
    val data = collection.find(q)

    data.toList.map(i => convert(i))
  }

  def find(developerId: String, applicationId: String) = {
    require(applicationId != null, "applicationId is required")
    require(developerId != null, "developerId is required")
    AuthorizationUtils.check(EntityAction.READ, entityName)

    val q = MongoDBObject(
      "applicationId" -> applicationId,
      "developerId" -> developerId)

    collection.findOne(q) match {
      case Some(dbo) => Some(convert(dbo))
      case _ => None
    }
  }

  protected def convert(o: DBObject) = {
    AppDeveloper(
      id = o.get("_id").toString,
      applicationId = expect[String](o.get("applicationId")),
      developerId = expect[String](o.get("developerId")),
      role = expect[String](o.get("role")),
      createdBy = expect[String](o.get("createdBy")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      modifiedBy = expect[String](o.get("modifiedBy")),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate"))))
  }

  override def toString = s"MongoDBAppDeveloperService($connectionManager)"
}