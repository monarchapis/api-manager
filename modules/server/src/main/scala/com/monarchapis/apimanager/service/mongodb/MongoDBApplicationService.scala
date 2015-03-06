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

import org.joda.time.DateTime

import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBApplicationService @Inject() (
  val connectionManager: MultitenantMongoDBConnectionManager,
  val planService: PlanService,
  val environmentService: EnvironmentService,
  val logService: LogService) extends ApplicationService with ServiceSupport[Application] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(planService != null, "planService is required")
  require(environmentService != null, "environmentService is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "application"
  protected val displayName = "application"
  protected val entityClass = classOf[Application]
  protected val aggregator = EntityEventAggregator.application

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("applications")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing applications")
    val collection = db("applications")
    ensureIndex(collection, MongoDBObject("environmentId" -> 1, "name_lc" -> 1), "application-by-name", true)
    ensureIndex(collection, MongoDBObject("planId" -> 1), "application-by-plan", false)
    // Sparse index by externalId
    ensureIndex(collection,
      MongoDBObject("externalId" -> 1),
      MongoDBObject(
        "name" -> "application-by-externalId",
        "unique" -> true,
        "sparse" -> true))
  })

  protected val labelField = "name"
  protected val fieldMap = MongoSchema[Application]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("name", "name", FieldType.CI_STRING, true, accessor = (e) => e.name)
    .field("description", "description", FieldType.CS_STRING, false, accessor = (e) => e.description.orNull)
    .field("applicationUrl", "applicationUrl", FieldType.CS_STRING, true, accessor = (e) => e.applicationUrl)
    .field("applicationImageUrl", "applicationImageUrl", FieldType.CS_STRING, false, accessor = (e) => e.applicationImageUrl.orNull)
    .field("companyName", "companyName", FieldType.CI_STRING, true, accessor = (e) => e.companyName)
    .field("companyUrl", "companyUrl", FieldType.CS_STRING, true, accessor = (e) => e.companyUrl)
    .field("companyImageUrl", "companyImageUrl", FieldType.CS_STRING, false, accessor = (e) => e.companyImageUrl.orNull)
    .field("callbackUris", "callbackUris", FieldType.OTHER, false, accessor = (e) => toList(e.callbackUris))
    .field("planId", "planId", FieldType.CS_STRING, false, accessor = (e) => e.planId.orNull, extras = (set, e) => {
      if (e.planId.isDefined && !planService.exists(e.planId.get)) {
        throw new BadRequestException("Could not find the referenced plan")
      }
    })
    .field("extended", "extended", FieldType.OTHER, false, accessor = (e) => {
      val builder = MongoDBObject.newBuilder[String, AnyRef]
      if (e.extended != null) e.extended foreach {
        case (key, value) => builder += key -> value
      }
      builder.result
    })
    .field("externalId", "externalId", FieldType.CS_STRING, false, accessor = (e) => NotSet.check(e.externalId.orNull))

  protected override def defaultSort = MongoDBObject("name_lc" -> 1)

  DisplayLabelSources.lookup += "applications" -> this

  def getDisplayLabels(ids: Set[String]): Map[String, String] = getDisplayLabels(ids, "name")

  EntityEventAggregator.plan += onPlanChange

  private def onPlanChange(plan: Plan, eventType: String) {
    eventType match {
      case "predelete" => if (exists(Map("planId" -> List(plan.id)))) {
        throw new InvalidParamaterException("This plan cannot be deleted because it is being referenced by an application.")
      }
      case _ =>
    }
  }

  protected def convert(o: DBObject) = {
    Application(
      id = o.get("_id").toString,
      name = expect[String](o.get("name")),
      description = optional[String](o.get("description")),
      applicationUrl = expect[String](o.get("applicationUrl")),
      applicationImageUrl = optional[String](o.get("applicationImageUrl")),
      companyName = expect[String](o.get("companyName")),
      companyUrl = expect[String](o.get("companyUrl")),
      companyImageUrl = optional[String](o.get("companyImageUrl")),
      callbackUris = set[String](o.getAs[MongoDBList]("callbackUris")),
      planId = optional[String](o.get("planId")),
      extended = map(o.getAs[DBObject]("extended")),
      externalId = optional[String](o.get("externalId")),
      createdBy = expect[String](o.get("createdBy")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      modifiedBy = expect[String](o.get("modifiedBy")),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate"))))
  }

  protected override def handleExpand(items: List[Application], expand: Set[String]) = {
    var clients = connectionManager(EnvironmentContext.current.systemDatabase)("clients")

    val ids = items.map(i => i.id) toSet

    val output = clients.aggregate(List(
      MongoDBObject("$match" -> MongoDBObject("applicationId" -> MongoDBObject("$in" -> ids))),
      MongoDBObject("$group" -> MongoDBObject("_id" -> "$applicationId", "count" -> MongoDBObject("$sum" -> 1)))))

    val builder = Map.newBuilder[String, Int]
    output.results.foreach(o => builder += o.getAs[String]("_id").get -> o.getAs[Int]("count").get)
    val lookup = builder.result

    items.map(i => i.withClientCount(lookup.getOrElse(i.id, 0)))
  }

  override def toString = s"MongoDBApplicationService($connectionManager)"
}