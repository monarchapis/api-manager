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

import org.joda.time.DateTimeZone

import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging

object MongoDBRateLimitService {
  private val rateUnits = Set("month", "day", "hour", "minute")

  private val unitCollections = Map(
    "month" -> "requestCountMonth",
    "day" -> "requestCountDay",
    "hour" -> "requestCountHour",
    "minute" -> "requestCountMinute")
}

class MongoDBRateLimitService(
  connectionManager: MultitenantMongoDBConnectionManager,
  timezone: DateTimeZone) extends RateLimitService with MongoDBUtils with Logging {

  import JulianUtils._

  def this(connectionManager: MultitenantMongoDBConnectionManager, timezoneId: String) =
    this(
      connectionManager,
      if ("default" == timezoneId) DateTimeZone.getDefault else DateTimeZone.forID(timezoneId))

  require(connectionManager != null, "connectionManager is required")

  info(s"$this")

  import MongoDBRateLimitService._

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing rate limits")

    rateUnits foreach { unit =>
      {
        ensureIndex(db(unitCollections(unit)), MongoDBObject(
          unit -> 1,
          "aid" -> 1), "count-key", true)
      }
    }
  })

  def checkQuotas(applicationId: String, requestWeight: Option[BigDecimal], quotas: List[Quota]) {
    val conn = connectionManager(EnvironmentContext.current.systemDatabase)
    val now = System.currentTimeMillis / 1000

    conn.requestStart

    try {
      conn.requestEnsureConnection

      quotas foreach { quote =>
        {
          val unit = quote.getTimeUnit.toString
          val julian = convertUnitToJulian(unit)(now, timezone)
          val count = conn(unitCollections(unit)).findOne(
            MongoDBObject(
              unit -> julian,
              "aid" -> applicationId))

          count match {
            case Some(count) => {
              count.getAs[Int]("n") match {
                case Some(n) => {
                  if (n >= quote.requestCount * 1000) {
                    throw new BadRequestException(s"You have exceeded your maximum allowed requests per $unit")
                  }
                }
                case _ =>
              }
            }
            case _ =>
          }
        }
      }

      val weight = (requestWeight match {
        case Some(weight) => (weight * 1000).toInt
        case _ => 1000
      })
      val inc = $inc("n" -> weight)

      rateUnits foreach { unit =>
        {
          val julian = convertUnitToJulian(unit)(now, timezone)
          conn(unitCollections(unit)).update(
            MongoDBObject(
              unit -> julian,
              "aid" -> applicationId),
            inc, true, false)
        }
      }
    } finally {
      conn.requestDone
    }
  }

  override def toString = s"MongoDBRateLimitService($connectionManager)"
}