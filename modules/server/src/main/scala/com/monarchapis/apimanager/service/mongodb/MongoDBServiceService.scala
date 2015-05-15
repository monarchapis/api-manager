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
import org.joda.time.DateTime

import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.security.UriRegexConverter
import com.monarchapis.apimanager.service._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBServiceService @Inject() (
  val connectionManager: MultitenantMongoDBConnectionManager,
  val permissionService: PermissionService,
  val logService: LogService) extends ServiceService with ServiceSupport[Service] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(permissionService != null, "permissionService is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "service"
  protected val displayName = "service"
  protected val entityClass = classOf[Service]
  protected val aggregator = EntityEventAggregator.service

  EntityEventAggregator.permission += onPermissionChange

  private def onPermissionChange(permission: Permission, eventType: String) {
    eventType match {
      case "delete" => {
        val ids = permissionIds(permission.id)

        val q = $or(
          "_refsClientPermissions" -> permission.id,
          "_refsDelegatedPermissions" -> permission.id)
        val c = collection.find(q, DBObject("operations" -> 1))
        val ub = DBObject.newBuilder[String, MongoDBList]

        c foreach (o => {
          val test = o.get("operations")
          o.getAs[MongoDBList]("operations") match {
            case Some(operations) => {
              for (i <- 0 until operations.length) {
                ub += s"operations.$i.clientPermissionIds" -> ids
                ub += s"operations.$i.delegatedPermissionIds" -> ids
              }
            }
            case _ =>
          }

          val id = DBObject("_id" -> o.getAs[ObjectId]("_id"))
          val u = DBObject.newBuilder[String, DBObject]
          val pa = ub.result

          if (pa.size > 0) {
            u += "$pullAll" -> pa
          }

          u += "$pull" -> DBObject(
            "_refsClientPermissions" -> permission.id,
            "_refsDelegatedPermissions" -> permission.id)

          collection.update(id, u.result)
        })
      }
      case "update" => {
        if (permission.scope != "both") {
          val opp = if (permission.scope == "client") "user" else "client"
          val refOpp = if (permission.scope == "client") "User" else "Client"
          val ids = permissionIds(permission.id)

          val q = DBObject(s"_refs${refOpp}Permissions" -> permission.id)
          val c = collection.find(q, DBObject("operations" -> 1))
          val ub = DBObject.newBuilder[String, MongoDBList]

          c foreach (o => {
            o.getAs[MongoDBList]("operations") match {
              case Some(operations) => {
                for (i <- 0 until operations.length) {
                  ub += s"operations.$i.${opp}PermissionIds" -> ids
                }
              }
              case _ =>
            }

            val id = DBObject("_id" -> o.getAs[ObjectId]("_id"))
            val u = DBObject.newBuilder[String, DBObject]
            val pa = ub.result

            if (pa.size > 0) {
              u += "$pullAll" -> pa
            }

            u += "$pull" -> DBObject(s"_refs${refOpp}Permissions" -> permission.id)
            collection.update(id, u.result)
          })
        }
      }
      case _ =>
    }
  }

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("services")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing services")
    val collection = db("services")
    ensureIndex(collection, MongoDBObject("name_lc" -> 1), "service-by-name", true)
    ensureIndex(collection, MongoDBObject("accessControl" -> 1), "service-by-accessControl")

    ensureIndex(collection, MongoDBObject("_refsClientPermissions" -> 1), "ref-clientPermissions")
    ensureIndex(collection, MongoDBObject("_refsDelegatedPermissions" -> 1), "ref-delegatedPermissions")
  })

  protected val labelField = "name"
  protected val fieldMap = MongoSchema[Service]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("name", "name", FieldType.CI_STRING, true, accessor = (e) => e.name)
    .field("type", "type", FieldType.CS_STRING, true, accessor = (e) => e.`type`)
    .field("description", "description", FieldType.CS_STRING, true, accessor = (e) => e.description)
    .field("uriPrefix", "uriPrefix", FieldType.CS_STRING, true, accessor = (e) => e.uriPrefix)
    .field("versionLocation", "versionLocation", FieldType.CS_STRING, false, accessor = (e) => e.versionLocation)
    .field("defaultVersion", "defaultVersion", FieldType.CS_STRING, false, accessor = (e) => e.defaultVersion)
    .field("hostnames", "hostnames", FieldType.OTHER, false, accessor = (e) => toList(e.hostnames))
    .field("requestWeights", "requestWeights", FieldType.OTHER, false, accessor = (e) => {
      val builder = MongoDBObject.newBuilder[String, Int]
      if (e.requestWeights != null) e.requestWeights foreach {
        case (key, value) => builder += key -> value
      }
      builder.result
    })
    .field("accessControl", "accessControl", FieldType.BOOLEAN, false, accessor = (e) => e.accessControl)
    .field("operations", "operations", FieldType.OTHER, false, accessor = (e) => {
      val builder = MongoDBList.newBuilder
      if (e.operations != null) {
        val converter = new UriRegexConverter

        val sorted = e.operations.sortWith((left, right) => {
          val leftLength = converter.getPatternLength(left.uriPattern)
          val rightLength = converter.getPatternLength(right.uriPattern)

          if (leftLength != rightLength) {
            leftLength < rightLength
          } else {
            val leftMethod = methodIndex(left.method)
            val rightMethod = methodIndex(right.method)

            leftMethod < rightMethod
          }
        })

        sorted foreach { value => builder += convert(value) }
      }
      builder.result
    }, extras = (set, e) => {
      val refClientPermissions = Set.newBuilder[String]
      val refDelegatedPermissions = Set.newBuilder[String]

      if (e.operations != null) {
        e.operations foreach (operation => {
          operation.clientPermissionIds foreach (permission => refClientPermissions += StringUtils.substringBefore(permission, ":"))
          operation.delegatedPermissionIds foreach (permission => refDelegatedPermissions += StringUtils.substringBefore(permission, ":"))
        })
      }

      val clientPermissions = refClientPermissions.result
      val delegatedPermissions = refDelegatedPermissions.result

      if (!permissionService.exists(clientPermissions) ||
        !permissionService.exists(delegatedPermissions)) {
        throw new ConflictException("Could not find a referenced permission")
      }

      set += "_refsClientPermissions" -> clientPermissions
      set += "_refsDelegatedPermissions" -> delegatedPermissions
    })
    .field("extended", "extended", FieldType.OTHER, false, accessor = (e) => {
      val builder = MongoDBObject.newBuilder[String, AnyRef]
      if (e.extended != null) e.extended foreach {
        case (key, value) => builder += key -> value
      }
      builder.result
    })

  protected override def defaultSort = MongoDBObject("name_lc" -> 1)

  private def methodIndex(method: String) = {
    method.toUpperCase match {
      case "GET" => 1
      case "POST" => 2
      case "PUT" => 3
      case "PATCH" => 4
      case "DELETE" => 5
      case "OPTIONS" => 6
      case _ => 999
    }
  }

  DisplayLabelSources.lookup += "services" -> this

  def getDisplayLabels(ids: Set[String]): Map[String, String] = getDisplayLabels(ids, "name")

  def lookupIdByName(name: String): Option[String] = {
    val q = MongoDBObject("name_lc" -> name.toLowerCase)
    val o = collection.findOne(q, MongoDBObject("_id" -> 1))

    o match {
      case Some(obj) => Some(obj.get("_id").toString)
      case _ => None
    }
  }

  def getAccessControlled = {
    //val q = MongoDBObject("accessControl" -> true)
    val data = collection.find()

    data.toList.map(i => convert(i))
  }

  private def convert(o: Operation): DBObject = {
    MongoDBObject(
      "name" -> o.name,
      "method" -> o.method,
      "uriPattern" -> o.uriPattern,
      "clientPermissionIds" -> toList(o.clientPermissionIds),
      "delegatedPermissionIds" -> toList(o.delegatedPermissionIds),
      "claims" -> convert(o.claims))
  }

  private def convert(claims: Set[ClaimEntry]) = {
    val list = new BasicDBList

    if (claims != null) {
      for (entry <- claims) {
        list += DBObject("type" -> entry.`type`, "value" -> entry.value.orNull)
      }
    }

    list
  }

  protected def convert(o: DBObject) = {
    Service(
      id = o.get("_id").toString,
      name = expect[String](o.get("name")),
      `type` = optional[String](o.get("type")),
      description = optional[String](o.get("description")),
      uriPrefix = optional[String](o.get("uriPrefix")),
      versionLocation = optional[String](o.get("versionLocation")),
      defaultVersion = optional[String](o.get("defaultVersion")),
      hostnames = set[String](o.getAs[MongoDBList]("hostnames")),
      requestWeights = map[Int](o.getAs[DBObject]("requestWeights")),
      accessControl = o.getAsOrElse[Boolean]("accessControl", false),
      operations = {
        val operations = o.getAs[MongoDBList]("operations").orNull
        convertOperations(operations)
      },
      extended = map(o.getAs[DBObject]("extended")),
      createdBy = expect[String](o.get("createdBy")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      modifiedBy = expect[String](o.get("modifiedBy")),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate"))))
  }

  protected def convertOperations(list: MongoDBList): List[Operation] = {
    val builder = List.newBuilder[Operation]

    if (list != null) {
      list foreach { a =>
        a match {
          case a: DBObject =>
            {
              val name = expect[String](a.get("name"))
              val method = expect[String](a.get("method"))
              val uriPattern = expect[String](a.get("uriPattern"))
              val clientPermissionIds = set[String](a.getAs[MongoDBList]("clientPermissionIds"))
              val delegatedPermissionIds = set[String](a.getAs[MongoDBList]("delegatedPermissionIds"))
              val claims = convertClaims(a.getAs[MongoDBList]("claims").orNull)

              builder += new Operation(name, method, uriPattern, clientPermissionIds, delegatedPermissionIds, claims)
            }
          case _ =>
        }
      }
    }

    builder.result
  }

  protected def convertClaims(list: MongoDBList) = {
    val builder = Set.newBuilder[ClaimEntry]

    if (list != null) {
      list foreach { a =>
        a match {
          case a: DBObject =>
            {
              val `type` = expect[String](a.get("type"))
              val value = optional[String](a.get("value"))

              builder += ClaimEntry(`type`, value)
            }
          case _ =>
        }
      }
    }

    builder.result
  }

  override def toString = s"MongoDBServiceService($connectionManager)"
}