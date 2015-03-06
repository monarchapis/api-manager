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

import org.apache.commons.lang3.StringUtils
import org.bson.types.ObjectId
import org.joda.time.DateTime

import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBPermissionService @Inject() (
  val connectionManager: MultitenantMongoDBConnectionManager,
  val messageService: MessageService,
  val logService: LogService) extends PermissionService with ServiceSupport[Permission] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "permission"
  protected val displayName = "permission"
  protected val entityClass = classOf[Permission]
  protected val aggregator = EntityEventAggregator.permission

  EntityEventAggregator.message += onMessageChange

  private def onMessageChange(message: Message, eventType: String) {
    eventType match {
      case "predelete" => if (exists(Map("messageId" -> List(message.id)))) {
        throw new InvalidParamaterException("This message cannot be deleted because it is being referenced by a permission.")
      }
      case _ =>
    }
  }

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("permissions")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing permissions")
    val collection = db("permissions")
    ensureIndex(collection, MongoDBObject("name_lc" -> 1), "permission-by-name", true)
  })

  protected val labelField = "name"
  protected val fieldMap = MongoSchema[Permission]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("name", "name", FieldType.CI_STRING, true, accessor = (e) => e.name)
    .field("type", "type", FieldType.CS_STRING, true, accessor = (e) => e.`type`)
    .field("description", "description", FieldType.CS_STRING, true, accessor = (e) => e.description)
    .field("scope", "scope", FieldType.CS_STRING, true, accessor = (e) => e.scope)
    .field("messageId", "messageId", FieldType.CS_STRING, true, accessor = (e) => e.messageId, extras = (set, e) => {
      if (!messageService.exists(e.messageId)) {
        throw new ConflictException("Could not find the referenced message")
      }
    })
    .field("flags", "flags", FieldType.OTHER, false, accessor = (e) => toList(e.flags))

  protected override def defaultSort = MongoDBObject("name_lc" -> 1)

  def findByName(name: String) = {
    require(name != null, "name is required")

    val q = MongoDBObject("name_lc" -> name.toLowerCase)
    create(collection.findOne(q))
  }

  def getPermissionNames(permissionIds: Set[String], scope: String): Set[String] = {
    val nameBuilder = Set.newBuilder[String]
    var idBuilder = Set.newBuilder[String]

    val subnames = scala.collection.mutable.Map.empty[String, scala.collection.mutable.Set[String]]

    val objectIds = permissionIds foreach { p =>
      val idx = p.indexOf(':')

      val id = if (idx != -1) {
        val id = p.substring(0, idx)
        val subname = p.substring(idx + 1)

        if (subnames.contains(id)) {
          subnames(id) += subname
        } else {
          subnames(id) = scala.collection.mutable.Set(subname)
        }

        id
      } else {
        p
      }

      if (ObjectId.isValid(id)) {
        idBuilder += id
      } else {
        nameBuilder += id
      }
    }

    val q = MongoDBObject(
      "_id" -> MongoDBObject("$in" -> idBuilder.result.map(id => new ObjectId(id))),
      "scope" -> MongoDBObject("$in" -> List(scope, "both")))
    val permissionNames = collection.find(
      q,
      MongoDBObject("name" -> 1))

    permissionNames foreach { o =>
      {
        val id = o.get("_id").toString
        val name = o.getAs[String]("name").get

        if (subnames.contains(id)) {
          subnames(id) foreach {
            subname => nameBuilder += name + ':' + subname
          }
        } else {
          nameBuilder += name
        }
      }
    }

    nameBuilder.result
  }

  def getPermissionIds(permissionNames: Set[String], scope: String): Set[String] = {
    val nameBuilder = Set.newBuilder[String]
    var idBuilder = Set.newBuilder[String]

    val subnames = scala.collection.mutable.Map.empty[String, scala.collection.mutable.Set[String]]

    val objectIds = permissionNames foreach { p =>
      val idx = p.indexOf(':')

      val name = if (idx != -1) {
        val name = p.substring(0, idx)
        val subname = p.substring(idx + 1)

        if (subnames.contains(name)) {
          subnames(name) += subname
        } else {
          subnames(name) = scala.collection.mutable.Set(subname)
        }

        name
      } else {
        p
      }

      nameBuilder += name
    }

    val q = MongoDBObject(
      "name_lc" -> MongoDBObject("$in" -> nameBuilder.result.map(name => name.toLowerCase)),
      "scope" -> MongoDBObject("$in" -> List(scope, "both")))
    val permissionIds = collection.find(
      q,
      MongoDBObject("name" -> 1))

    permissionIds foreach { o =>
      {
        val id = o.get("_id").toString
        val name = o.getAs[String]("name").get

        if (subnames.contains(name)) {
          subnames(name) foreach {
            subname => idBuilder += id + ':' + subname
          }
        } else {
          idBuilder += id
        }
      }
    }

    idBuilder.result
  }

  protected def convert(o: DBObject) = {
    Permission(
      id = o.get("_id").toString,
      name = expect[String](o.get("name")),
      `type` = expect[String](o.get("type"), "action"),
      description = optional[String](o.get("description")),
      scope = {
        val scope = expect[String](o.get("scope"))
        if (Permission.validScopes(scope)) { scope } else { "both" }
      },
      messageId = expect[String](o.get("messageId")),
      flags = set[String](o.getAs[MongoDBList]("flags")),
      createdBy = expect[String](o.get("createdBy")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      modifiedBy = expect[String](o.get("modifiedBy")),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate"))))
  }

  protected override def handleExpand(items: List[Permission], expand: Set[String]) = {
    var ret = items

    if (expand.contains("message")) {
      val ids = items.map(i => i.messageId) toSet
      val map = messageService.loadMap(ids)

      ret = ret.map(i => i.withMessage(map.get(i.messageId)))
    }

    ret
  }

  protected override def checkRelationships(entity: Permission, delta: Delta[Permission] = null) {
    if (delta == null || delta.pathChanged("messageId")) {
      val message = messageService.load(entity.messageId)
      require(message.isDefined, s"The message with Id $entity.messageId does not exist.")
    }
  }

  override def toString = s"MongoDBPermissionService($connectionManager)"
}