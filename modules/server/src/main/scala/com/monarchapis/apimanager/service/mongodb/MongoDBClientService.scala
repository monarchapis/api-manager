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

import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime

import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBClientService @Inject() (
  val connectionManager: MultitenantMongoDBConnectionManager,
  val applicationService: ApplicationService,
  val permissionService: PermissionService,
  val environmentService: EnvironmentService,
  val encryptionManager: EncryptionManager,
  val logService: LogService) extends ClientService with ServiceSupport[Client] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(applicationService != null, "applicationService is required")
  require(permissionService != null, "permissionService is required")
  require(encryptionManager != null, "encryptionManager is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "client"
  protected val displayName = "client"
  protected val entityClass = classOf[Client]
  protected val aggregator = EntityEventAggregator.client

  EntityEventAggregator.permission += onPermissionChange

  private def onPermissionChange(permission: Permission, eventType: String) {
    eventType match {
      case "delete" => {
        val ids = permissionIds(permission.id)

        val q1 = DBObject("_refsClientPermissions" -> permission.id)
        val u1 = DBObject(
          "$pullAll" -> DBObject("clientPermissionIds" -> ids),
          "$pull" -> DBObject("_refsClientPermissions" -> permission.id))
        collection.update(q1, u1, multi = true)

        val q2 = DBObject("_refsUserPermissions" -> permission.id)
        val c = collection.find(q2, DBObject("permissionSets" -> 1))
        val u2b = DBObject.newBuilder[String, MongoDBList]

        c foreach (o => {
          o.getAs[DBObject]("permissionSets") match {
            case Some(ps) => {
              ps.keys foreach (key => {
                u2b += s"permissionSets.$key.permissionIds" -> ids
              })
            }
            case _ =>
          }

          val id = DBObject("_id" -> o.getAs[ObjectId]("_id"))
          val u = DBObject.newBuilder[String, DBObject]
          val u2 = u2b.result

          if (u2.size > 0) {
            u += "$pullAll" -> u2
          }

          u += "$pull" -> DBObject("_refsUserPermissions" -> permission.id)

          collection.update(id, u.result)
        })
      }
      case "update" => {
        val ids = permissionIds(permission.id)

        if (permission.scope == "user") {
          val q1 = DBObject("_refsClientPermissions" -> permission.id)
          val u1 = DBObject(
            "$pullAll" -> DBObject("clientPermissionIds" -> ids),
            "$pull" -> DBObject("_refsClientPermissions" -> permission.id))
          collection.update(q1, u1, multi = true)
        } else if (permission.scope == "client") {
          val q2 = DBObject("_refsUserPermissions" -> permission.id)
          val c = collection.find(q2, DBObject("permissionSets" -> 1))
          val u2b = DBObject.newBuilder[String, MongoDBList]

          c foreach (o => {
            o.getAs[DBObject]("permissionSets") match {
              case Some(ps) => {
                ps.keys foreach (key => {
                  u2b += s"permissionSets.$key.permissionIds" -> ids
                })
              }
              case _ =>
            }

            val id = DBObject("_id" -> o.getAs[ObjectId]("_id"))
            val u = DBObject.newBuilder[String, DBObject]
            val u2 = u2b.result

            if (u2.size > 0) {
              u += "$pullAll" -> u2
            }

            u += "$pull" -> DBObject("_refsUserPermissions" -> permission.id)

            collection.update(id, u.result)
          })
        }
      }
      case _ =>
    }
  }

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("clients")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing clients")
    val collection = db("clients")
    ensureIndex(collection, MongoDBObject("applicationId" -> 1, "label_lc" -> 1), "client-by-label", true)
    ensureIndex(collection, MongoDBObject("apiKey" -> 1), "client-by-apiKey", true)
    ensureIndex(collection, MongoDBObject("label_lc" -> 1, "applicationName_lc" -> 1), "client-by-app-label")
    ensureIndex(collection, MongoDBObject("applicationName_lc" -> 1), "client-by-app")
    // Sparse index by externalId
    ensureIndex(collection,
      MongoDBObject("externalId" -> 1),
      MongoDBObject(
        "name" -> "client-by-externalId",
        "unique" -> true,
        "sparse" -> true))

    ensureIndex(collection, MongoDBObject("_refsClientPermissions" -> 1), "ref-clientPermissions")
    ensureIndex(collection, MongoDBObject("_refsUserPermissions" -> 1), "ref-userPermissions")
  })

  protected val labelField = "label"
  protected val fieldMap = MongoSchema[Client]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("application.name", "applicationName", FieldType.CI_STRING, true, updateable = false)
    .field("label", "label", FieldType.CI_STRING, true, accessor = (e) => e.label)
    .field("enabled", "enabled", FieldType.BOOLEAN, true, accessor = (e) => e.enabled)
    .field("status", "status", FieldType.CS_STRING, false, accessor = (e) => e.status)
    .field("applicationId", "applicationId", FieldType.CS_STRING, true, updateable = false, accessor = (e) => e.applicationId, extras = (set, e) => {
      if (!applicationService.exists(e.applicationId)) {
        throw new ConflictException("Could not find the referenced application")
      }
    })
    .field("apiKey", "apiKey", FieldType.ENC_STRING, true, accessor = (e) => e.apiKey)
    .field("sharedSecret", "sharedSecret", FieldType.ENC_SALTED_STRING, false, accessor = (e) => e.sharedSecret.orNull)
    .field("authenticators", "authenticators", FieldType.OTHER, true, accessor = (e) => convert(e.authenticators))
    .field("policies", "policies", FieldType.OTHER, true, accessor = (e) => convert(e.policies))
    .field("claimSources", "claimSources", FieldType.OTHER, true, accessor = (e) => convert(e.claimSources))
    .field("clientPermissionIds", "clientPermissionIds", FieldType.OTHER, false, accessor = (e) => toList(e.clientPermissionIds), extras = (set, e) => {
      val refPermissions = if (e.clientPermissionIds != null) {
        e.clientPermissionIds map (permission => StringUtils.substringBefore(permission, ":")) toSet
      } else {
        Set.empty[String]
      }

      if (!permissionService.exists(refPermissions)) {
        throw new ConflictException("Could not find a referenced permission")
      }

      set += "_refsClientPermissions" -> refPermissions
    })
    .field("permissionSets", "permissionSets", FieldType.OTHER, false, accessor = (e) => {
      val builder = MongoDBObject.newBuilder[String, DBObject]
      if (e.permissionSets != null) {
        e.permissionSets foreach {
          case (key, value) => builder += key -> convert(value)
        }
      }
      builder.result
    }, extras = (set, e) => {
      val refPermissions = Set.newBuilder[String]

      if (e.permissionSets != null) {
        e.permissionSets foreach {
          case (key, value) => {
            if (value.permissionIds != null) {
              value.permissionIds foreach (permission => refPermissions += StringUtils.substringBefore(permission, ":"))
            }
          }
        }
      }

      val permissions = refPermissions.result

      if (!permissionService.exists(permissions)) {
        throw new ConflictException("Could not find a referenced permission")
      }

      set += "_refsUserPermissions" -> permissions
    })
    .field("extended", "extended", FieldType.OTHER, false, accessor = (e) => {
      val builder = MongoDBObject.newBuilder[String, AnyRef]
      if (e.extended != null) e.extended foreach {
        case (key, value) => builder += key -> value
      }
      builder.result
    })
    .field("externalId", "externalId", FieldType.CS_STRING, false, accessor = (e) => NotSet.check(e.externalId.orNull))

  protected override def defaultSort = MongoDBObject("label_lc" -> 1)

  DisplayLabelSources.lookup += "clients" -> this

  def getDisplayLabels(ids: Set[String]): Map[String, String] = {
    val objectIds = ids map { id => new ObjectId(id) }
    val result = collection.find(
      MongoDBObject("_id" -> MongoDBObject("$in" -> objectIds)),
      MongoDBObject("applicationName" -> 1, "label" -> 1))
    val builder = Map.newBuilder[String, String]

    result foreach {
      o =>
        {
          val id = o.get("_id").toString
          val name = o.getAs[String]("applicationName").getOrElse("Unknown")
          val label = o.getAs[String]("label").get

          builder += id -> s"$name: $label"
        }
    }

    builder.result
  }

  EntityEventAggregator.application += onApplicationChange

  private def onApplicationChange(application: Application, eventType: String) {
    eventType match {
      case "update" => collection.update(
        MongoDBObject("applicationId" -> application.id),
        MongoDBObject("$set" -> MongoDBObject(
          "applicationName" -> application.name,
          "applicationName_lc" -> application.name.toLowerCase)),
        multi = true)
      case "delete" => collection.remove(MongoDBObject("applicationId" -> application.id))
      case _ =>
    }
  }

  def findByApiKey(apiKey: String) = {
    val q = MongoDBObject("apiKey" -> encryptionManager.encrypt(apiKey))
    val entity = create(collection.findOne(q))
    if (entity.isDefined) checkReadAccess(entity.get)
    entity
  }

  def authenticate(apiKey: String, sharedSecret: String) = {
    findByApiKey(apiKey) match {
      case Some(client) =>
        if ("redacted" != sharedSecret && client.sharedSecret == sharedSecret)
          Some(client)
        else
          None
      case _ => None
    }
  }

  def lookupId(apiKey: String): Option[String] = {
    val q = MongoDBObject("apiKey" -> encryptionManager.encrypt(apiKey))
    val o = collection.findOne(q, MongoDBObject("_id" -> 1))
    o match {
      case Some(obj) => Some(obj.get("_id").toString)
      case _ => None
    }
  }

  def setPermissionSets(id: String, fields: Map[String, Option[PermissionSet]]) = {
    val q = MongoDBObject("_id" -> new ObjectId(id))
    val set = DBObject.newBuilder[String, DBObject]
    val unset = DBObject.newBuilder[String, DBObject]

    fields foreach {
      case (profileName, option) => {
        option match {
          case Some(permissionSet: PermissionSet) => set += "permissionSets." + profileName -> convert(permissionSet)
          case _ => unset += "permissionSets." + profileName -> ""
        }
      }
    }

    set += (
      "modifiedBy" -> UserContext.current,
      "modifiedDate" -> DateTime.now.toDate)

    val setObj = set.result
    val unsetObj = unset.result
    var count = 0

    if (setObj.size > 2 || unsetObj.size > 0) {
      val update = MongoDBObject(
        "$set" -> setObj,
        "$unset" -> unsetObj)
      val result = collection.update(q, update, false, false, WriteConcern.FsyncSafe)
      count += result.getN
    }

    count > 0
  }

  private def convert(o: PermissionSet): DBObject = {
    MongoDBObject(
      "enabled" -> o.enabled,
      "global" -> o.global,
      "expiration" -> o.expiration.getOrElse(null),
      "lifespan" -> o.lifespan,
      "refreshable" -> o.refreshable,
      "permissionIds" -> toList(o.permissionIds),
      "autoAuthorize" -> o.autoAuthorize,
      "allowWebView" -> o.allowWebView,
      "allowPopup" -> o.allowPopup)
  }

  protected def convert(o: DBObject) = {
    val client = Client(
      id = o.get("_id").toString,
      label = expect[String](o.get("label")),
      enabled = o.getAsOrElse[Boolean]("enabled", true),
      status = optional[String](o.get("status")),
      applicationId = expect[String](o.get("applicationId")),
      apiKey = encryptionManager.decryptAsString(expect[String](o.get("apiKey"))),
      sharedSecret = optional[String](o.get("sharedSecret")) match {
        case Some(secret) => Some(encryptionManager.decryptSaltedAsString(secret))
        case _ => null
      },
      authenticators = {
        val authenticators = o.getAs[BasicDBObject]("authenticators").orNull
        convertMappedProperties(authenticators)
      },
      policies = {
        val configs = o.getAs[MongoDBList]("policies").orNull
        convertConfiguration(configs)
      },
      claimSources = {
        val configs = o.getAs[MongoDBList]("claimSources").orNull
        convertConfiguration(configs)
      },
      clientPermissionIds = set[String](o.getAs[MongoDBList]("clientPermissionIds")),
      permissionSets = {
        val builder = Map.newBuilder[String, PermissionSet]
        val permissionSets = o.getAs[DBObject]("permissionSets")

        permissionSets match {
          case Some(dbo) =>
            dbo foreach {
              case (key, o: DBObject) => {
                builder += key -> PermissionSet(
                  enabled = o.getAs[Boolean]("enabled").getOrElse(true),
                  global = o.getAs[Boolean]("global").getOrElse(true),
                  expiration = long(o.get("expiration")),
                  lifespan = expect[String](o.get("lifespan"), "finite"),
                  refreshable = o.getAs[Boolean]("refreshable").getOrElse(false),
                  permissionIds = set[String](o.getAs[MongoDBList]("permissionIds")),
                  autoAuthorize = o.getAs[Boolean]("autoAuthorize").getOrElse(false),
                  allowWebView = o.getAs[Boolean]("allowWebView").getOrElse(false),
                  allowPopup = o.getAs[Boolean]("allowPopup").getOrElse(false))
              }
            }
          case _ =>
        }

        builder.result
      },
      extended = map(o.getAs[DBObject]("extended")),
      externalId = optional[String](o.get("externalId")),
      createdBy = expect[String](o.get("createdBy")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      modifiedBy = expect[String](o.get("modifiedBy")),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate"))))

    if (AuthorizationUtils.can(EntityAction.READ_SENSITIVE, entityName))
      client
    else
      client.redacted
  }

  protected override def handleExpand(items: List[Client], expand: Set[String]) = {
    var ret = items

    if (expand.contains("application")) {
      val ids = items.map(i => i.applicationId) toSet
      val map = applicationService.loadMap(ids)

      ret = ret.map(i => i.withApplication(map.get(i.applicationId)))
    }

    ret
  }

  protected override def createMixins(setBuilder: Builder[(String, Any), DBObject], entity: Client) {
    val application = applicationService.load(entity.applicationId)
    require(!application.isEmpty, s"The application with Id $entity.applicationId does not exist.")

    // _lc to match case-insensitive property above
    val name = application.get.name
    setBuilder += "applicationName" -> name
    setBuilder += "applicationName_lc" -> name.toLowerCase
  }

  override def toString = s"MongoDBClientService($connectionManager)"
}