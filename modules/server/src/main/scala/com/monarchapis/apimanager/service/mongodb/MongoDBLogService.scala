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

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBLogService @Inject() (
  val connectionManager: MultitenantMongoDBConnectionManager) extends LogService with ServiceSupport[LogEntry] with Logging {
  require(connectionManager != null, "connectionManager is required")

  info(s"$this")

  protected val logService = this
  protected val entityName = "logEntry"
  protected val displayName = "log entry"
  protected val entityClass = classOf[LogEntry]
  protected val aggregator = EntityEventAggregator.logEntry

  protected def collection = {
    val db = connectionManager(EnvironmentContext.current.systemDatabase)
    getCollection(db)
  }

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing log entries")
    val collection = getCollection(db)
    ensureIndex(collection, MongoDBObject("timestamp" -> -1), "logEntry-by-timestamp", false)
    ensureIndex(collection, MongoDBObject("level" -> 1, "timestamp" -> -1), "logEntry-by-level", false)
  })

  protected val labelField = null
  protected val fieldMap = MongoSchema[LogEntry]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("timestamp", "timestamp", FieldType.DATETIME, updateable = false, accessor = (e) => e.timestamp)
    .field("level", "level", FieldType.CS_STRING, true, updateable = false, accessor = (e) => e.level)
    .field("message", "message", FieldType.CS_STRING, true, updateable = false, accessor = (e) => e.message)

  protected override def defaultSort = MongoDBObject("timestamp" -> -1)

  def log(level: String, message: String) {
    val o = MongoDBObject(
      "timestamp" -> DateTime.now.toDate,
      "level" -> level,
      "message" -> message)
    collection.insert(o)
  }

  def getLogsEnties(offset: Int = 0, limit: Int = 25) = {
    var data = collection.find().sort(MongoDBObject("timestamp" -> -1)).skip(offset).limit(limit)
    data.toList.map(i => convert(i))
  }

  protected def convert(o: DBObject) = {
    LogEntry(
      id = o.get("_id").toString,
      timestamp = expect[DateTime](datetime(o.get("timestamp"))),
      level = expect[String](o.get("level")),
      message = expect[String](o.get("message")))
  }

  private def getCollection(connection: com.mongodb.casbah.Imports.MongoDB): com.mongodb.casbah.MongoCollection = {
    if (!connection.collectionExists("logEntries")) {
      val coll = connection.createCollection(
        "logEntries", MongoDBObject(
          "capped" -> true,
          "size" -> 100000)).asScala
      val o = MongoDBObject(
        "timestamp" -> DateTime.now.toDate,
        "level" -> "info",
        "message" -> "Environment created")
      coll.insert(o)
      coll
    } else connection("logEntries")
  }

  override def toString = s"MongoDBLogService($connectionManager)"
}