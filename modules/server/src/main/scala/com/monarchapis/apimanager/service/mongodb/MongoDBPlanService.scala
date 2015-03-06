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

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBPlanService @Inject() (
  val connectionManager: MultitenantMongoDBConnectionManager,
  val logService: LogService) extends PlanService with ServiceSupport[Plan] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "plan"
  protected val displayName = "plan"
  protected val entityClass = classOf[Plan]
  protected val aggregator = EntityEventAggregator.plan

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("plans")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing plans")
    val collection = db("plans")
    ensureIndex(collection, MongoDBObject("name_lc" -> 1), "plan-by-name", true)
  })

  protected val labelField = "name"
  protected val fieldMap = MongoSchema[Plan]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("name", "name", FieldType.CI_STRING, true, accessor = (e) => e.name)
    .field("priceAmount", "priceAmount", FieldType.INTEGER, false, accessor = (e) => e.priceAmount match {
      case Some(price) => (price * 1000).toInt
      case _ => null
    })
    .field("priceCurrency", "priceCurrency", FieldType.CS_STRING, false, accessor = (e) => e.priceCurrency.orNull)
    .field("quotas", "quotas", FieldType.OTHER, false, accessor = (e) => {
      val list = new BasicDBList

      if (e.quotas != null) {
        e.quotas foreach {
          quota => list.add(convert(quota))
        }
      }

      list
    })

  protected override def defaultSort = MongoDBObject("name_lc" -> 1)

  def findByName(name: String) = {
    require(name != null, "name is required")

    val q = MongoDBObject("name_lc" -> name.toLowerCase)
    create(collection.findOne(q))
  }

  protected def convert(o: DBObject) = {
    Plan(
      id = o.get("_id").toString,
      name = expect[String](o.get("name")),
      priceAmount = o.getAs[Int]("priceAmount") match {
        case Some(price) => Some(BigDecimal(price) / 1000)
        case _ => None
      },
      priceCurrency = optional[String](o.get("priceCurrency")),
      quotas = listOf[Quota](o.getAs[MongoDBList]("quotas"), convertQuota),
      createdBy = expect[String](o.get("createdBy")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      modifiedBy = expect[String](o.get("modifiedBy")),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate"))))
  }

  private def convertQuota(o: DBObject) = {
    Quota(
      requestCount = expect[Long](o.get("requestCount")),
      timeUnit = expect[String](o.get("timeUnit")))
  }

  private def convert(quota: Quota) = {
    MongoDBObject(
      "requestCount" -> quota.requestCount,
      "timeUnit" -> quota.timeUnit.toString)
  }

  override def toString = s"MongoDBPlanService($connectionManager)"
}