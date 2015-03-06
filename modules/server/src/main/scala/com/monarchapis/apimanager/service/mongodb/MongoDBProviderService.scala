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
import com.monarchapis.apimanager.util._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBProviderService @Inject() (
  val connectionManager: MultitenantMongoDBConnectionManager,
  val encryptionManager: EncryptionManager,
  val logService: LogService) extends ProviderService with ServiceSupport[Provider] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(encryptionManager != null, "encryptionManager is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "provider"
  protected val displayName = "provider"
  protected val entityClass = classOf[Provider]
  protected val aggregator = EntityEventAggregator.provider

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("providers")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing providers")
    val collection = db("providers")
    ensureIndex(collection, MongoDBObject("label_lc" -> 1), "providerKey-by-label", true)
    ensureIndex(collection, MongoDBObject("apiKey" -> 1), "providerKey-by-apiKey", true)
  })

  protected val labelField = "label"
  protected val fieldMap = MongoSchema[Provider]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("label", "label", FieldType.CI_STRING, true, accessor = (e) => e.label)
    .field("enabled", "enabled", FieldType.BOOLEAN, true, accessor = (e) => e.enabled)
    .field("apiKey", "apiKey", FieldType.ENC_STRING, true, accessor = (e) => e.apiKey)
    .field("sharedSecret", "sharedSecret", FieldType.ENC_SALTED_STRING, false, accessor = (e) => e.sharedSecret.orNull)
    .field("authenticators", "authenticators", FieldType.CS_STRING, true, accessor = (e) => convert(e.authenticators))
    .field("policies", "policies", FieldType.CS_STRING, true, accessor = (e) => convert(e.policies))
    .field("permissions", "permissions", FieldType.OTHER, false, accessor = (e) => toList(e.permissions))
    .field("accessLevels", "accessLevels", FieldType.OTHER, false, accessor = (e) => {
      AuthorizationUtils.validateAccessLevels(e.accessLevels)
      toObject(e.accessLevels)
    })
    .field("behindReverseProxy", "behindReverseProxy", FieldType.BOOLEAN, true, accessor = (e) => e.behindReverseProxy)
    .field("extended", "extended", FieldType.OTHER, false, accessor = (e) => {
      val builder = MongoDBObject.newBuilder[String, AnyRef]
      if (e.extended != null) e.extended foreach {
        case (key, value) => builder += key -> value
      }
      builder.result
    })

  protected override def defaultSort = MongoDBObject("label_lc" -> 1)

  DisplayLabelSources.lookup += "providers" -> this

  def getDisplayLabels(ids: Set[String]): Map[String, String] = getDisplayLabels(ids, "label")

  def findByApiKey(apiKey: String) = {
    val q = MongoDBObject("apiKey" -> encryptionManager.encrypt(apiKey))
    val entity = create(collection.findOne(q))
    if (entity.isDefined) checkReadAccess(entity.get)
    entity
  }

  def lookupIdByLabel(name: String): Option[String] = {
    val q = MongoDBObject("label_lc" -> name.toLowerCase)
    val o = collection.findOne(q, MongoDBObject("_id" -> 1))

    o match {
      case Some(obj) => Some(obj.get("_id").toString)
      case _ => None
    }
  }

  protected def convert(o: DBObject) = {
    val provider = Provider(
      id = o.get("_id").toString,
      label = expect[String](o.get("label")),
      enabled = o.getAsOrElse[Boolean]("enabled", true),
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
      permissions = set[String](o.getAs[MongoDBList]("permissions")),
      accessLevels = convertAccessLevels(o.getAs[DBObject]("accessLevels")),
      behindReverseProxy = o.getAsOrElse[Boolean]("behindReverseProxy", false),
      extended = map(o.getAs[DBObject]("extended")),
      createdBy = expect[String](o.get("createdBy")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      modifiedBy = expect[String](o.get("modifiedBy")),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate"))))

    if (AuthorizationUtils.can(EntityAction.READ_SENSITIVE, entityName))
      provider
    else
      provider.redacted
  }

  override def toString = s"MongoDBProviderService($connectionManager)"
}