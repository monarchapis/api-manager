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

import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBMessageService @Inject() (
  val connectionManager: MultitenantMongoDBConnectionManager,
  val logService: LogService) extends MessageService with ServiceSupport[Message] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "message"
  protected val displayName = "message"
  protected val entityClass = classOf[Message]
  protected val aggregator = EntityEventAggregator.message

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("messages")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing messages")
    val collection = db("messages")
    ensureIndex(collection, MongoDBObject("parentId" -> 1, "displayOrder" -> 1), "message-by-parent", false)
    ensureIndex(collection, MongoDBObject("key" -> 1), "message-by-key", true)
  })

  protected val labelField = "key"
  protected val fieldMap = MongoSchema[Message]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("parentId", "parentId", FieldType.CS_STRING, false, accessor = (e) => e.parentId.orNull, extras = (set, e) => {
      if (e.parentId.isDefined && !this.exists(e.parentId.get)) {
        throw new ConflictException("Could not find the referenced parent")
      }
    })
    .field("key", "key", FieldType.CS_STRING, true, accessor = (e) => e.key)
    .field("locales", "locales", FieldType.OTHER, false, accessor = (e) => {
      val builder = MongoDBObject.newBuilder[String, DBObject]
      if (e.locales != null) e.locales foreach {
        case (key, value) => builder += key -> convert(value)
      }
      builder.result
    })
    .field("displayOrder", "displayOrder", FieldType.INTEGER, true, accessor = (e) => e.displayOrder)

  protected override def defaultSort = MongoDBObject("key" -> 1)

  def findByParent(parentId: String): List[Message] = {
    val q = MongoDBObject("parentId" -> parentId)
    val s = MongoDBObject("displayOrder" -> 1)
    val data = collection.find(q)
    data.toList.map(i => convert(i))
  }

  def findByKey(key: String) = {
    require(key != null, "key is required")

    val q = MongoDBObject("key" -> key)
    create(collection.findOne(q))
  }

  protected def convert(o: DBObject) = {
    Message(
      id = o.get("_id").toString,
      parentId = optional[String](o.get("parentId")),
      key = expect[String](o.get("key")),
      locales = map[MessageContent](o.getAs[DBObject]("locales"), convertMessageContent),
      displayOrder = expect[Int](o.get("displayOrder")))
  }

  private def convertMessageContent(o: DBObject) = {
    MessageContent(
      format = expect[String](o.get("format")),
      content = expect[String](o.get("content")))
  }

  private def convert(content: MessageContent) = {
    MongoDBObject(
      "format" -> content.format,
      "content" -> content.content)
  }

  protected override def checkRelationships(entity: Message, delta: Delta[Message] = null) {
    if (delta == null || delta.pathChanged("parentId")) {
      var parentId = entity.parentId
      val found = new scala.collection.mutable.HashSet[String]

      while (parentId.isDefined) {
        val parentOption = load(parentId.get)
        require(parentOption.isDefined, s"The message with Id ${parentId.get} does not exist.")
        var parent = parentOption.get

        if (found.contains(parent.id)) {
          throw new RuntimeException("Recursion detected in message heirarchy at message id ${parent.id}");
        }

        found += parent.id
        parentId = parent.parentId
      }
    }
  }

  override def toString = s"MongoDBMessageService($connectionManager)"
}