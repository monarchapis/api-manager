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

import org.joda.time.DateTime

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBTokenService @Inject() (
  val connectionManager: MultitenantMongoDBConnectionManager,
  val clientService: ClientService,
  val encryptionManager: EncryptionManager,
  val logService: LogService) extends TokenService with ServiceSupport[Token] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(clientService != null, "clientService is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "token"
  protected val displayName = "token"
  protected val entityClass = classOf[Token]
  protected val aggregator = EntityEventAggregator.token

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("tokens")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing tokens")
    val collection = db("tokens")
    ensureIndex(collection, MongoDBObject("token" -> 1), "token-by-token", true)
    ensureIndex(collection,
      MongoDBObject("refreshToken" -> 1),
      MongoDBObject(
        "name" -> "token-by-refreshToken",
        "unique" -> true,
        "sparse" -> true))
    // Sparse index by externalId
    ensureIndex(collection,
      MongoDBObject("externalId" -> 1),
      MongoDBObject(
        "name" -> "token-by-externalId",
        "unique" -> true,
        "sparse" -> true))
    // Create a TTL index to cleanup expired tokens
    ensureIndex(collection,
      MongoDBObject("expiry" -> 1),
      MongoDBObject(
        "name" -> "token-expiry",
        "expireAfterSeconds" -> 0))
  })

  protected val labelField = null
  protected val fieldMap = MongoSchema[Token]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("clientId", "clientId", FieldType.CS_STRING, true, accessor = (e) => e.clientId)
    .field("scheme", "scheme", FieldType.CS_STRING, true, accessor = (e) => e.scheme.orNull)
    .field("token", "token", FieldType.ENC_STRING, true, updateable = false, accessor = (e) => e.token)
    .field("refreshToken", "refreshToken", FieldType.ENC_STRING, false, accessor = (e) => NotSet.check(e.refreshToken.orNull))
    .field("tokenType", "tokenType", FieldType.CS_STRING, true, accessor = (e) => e.tokenType)
    .field("grantType", "grantType", FieldType.CS_STRING, true, accessor = (e) => e.grantType)
    .field("expiresIn", "expiresIn", FieldType.INTEGER, true, accessor = (e) => e.expiresIn.orNull)
    .field("expiry", "expiry", FieldType.DATETIME, false, accessor = (e) => if (e.refreshToken.isEmpty) {
      e.expiresIn match {
        case Some(timespan) => e.lastAccessedDate.plus(timespan * 1000L).toDate
        case _ => null
      }
    } else null)
    .field("lifecycle", "lifecycle", FieldType.CS_STRING, true, accessor = (e) => e.lifecycle)
    .field("fromToken", "fromToken", FieldType.ENC_STRING, false, accessor = (e) => e.fromToken.orNull)
    .field("permissionIds", "permissionIds", FieldType.OTHER, false, accessor = (e) => toList(e.permissionIds))
    .field("state", "state", FieldType.CS_STRING, false, accessor = (e) => e.state.orNull)
    .field("uri", "uri", FieldType.CS_STRING, false, accessor = (e) => e.uri)
    .field("userId", "userId", FieldType.ENC_STRING, false, accessor = (e) => e.userId)
    .field("userContext", "userContext", FieldType.ENC_STRING, false, accessor = (e) => e.userContext.orNull)
    .field("extended", "extended", FieldType.OTHER, false, accessor = (e) => toObject(e.extended))
    .field("externalId", "externalId", FieldType.CS_STRING, false, accessor = (e) => NotSet.check(e.externalId.orNull))

  protected override def defaultSort = MongoDBObject("_id" -> 1)

  def findByToken(token: String) = {
    val q = MongoDBObject("token" -> encryptionManager.encrypt(token))
    val entity = create(collection.findOne(q))
    if (entity.isDefined) checkReadAccess(entity.get)
    entity
  }

  def findByRefresh(token: String) = {
    val q = MongoDBObject("refreshToken" -> encryptionManager.encrypt(token))
    val entity = create(collection.findOne(q))
    if (entity.isDefined) checkReadAccess(entity.get)
    entity
  }

  def touch(token: Token) {
    token.expiresIn match {
      case Some(timespan) => {
        val q = MongoDBObject("_id" -> new ObjectId(token.id))
        val now = DateTime.now
        collection.update(q, MongoDBObject("$set" -> MongoDBObject(
          "lastAccessedDate" -> now.toDate,
          "expiry" -> now.plus(timespan * 1000).toDate)))
      }
      case _ =>
    }
  }

  protected override def build(
    setBuilder: Builder[(String, Any), DBObject],
    unsetBuilder: Builder[(String, Any), DBObject],
    entity: Token,
    event: ServiceEvent.Value) {
    super.build(setBuilder, unsetBuilder, entity, event)

    if (event == ServiceEvent.CREATE) {
      val now = DateTime.now.toDate
      setBuilder += "createdDate" -> now
      setBuilder += "lastAccessedDate" -> now
    }
  }

  protected def convert(o: DBObject) = {
    Token(
      id = o.get("_id").toString,
      clientId = expect[String](o.get("clientId")),
      scheme = optional[String](o.get("scheme")),
      token = encryptionManager.decryptAsString(expect[String](o.get("token"))),
      refreshToken = optional[String](o.get("refreshToken")) match {
        case Some(s) => Some(encryptionManager.decryptAsString(s))
        case _ => None
      },
      tokenType = expect[String](o.get("tokenType")),
      grantType = expect[String](o.get("grantType")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      lastAccessedDate = expect[DateTime](datetime(o.get("lastAccessedDate"))),
      expiresIn = long(o.get("expiresIn")),
      lifecycle = expect[String](o.get("lifecycle")),
      fromToken = optional[String](o.get("fromToken")) match {
        case Some(s) => Some(encryptionManager.decryptAsString(s))
        case _ => None
      },
      permissionIds = set[String](o.getAs[MongoDBList]("permissionIds")),
      state = optional[String](o.get("state")),
      uri = optional[String](o.get("uri")),
      userId = encryptionManager.decryptAsString(expect[String](o.get("userId"))),
      userContext = optional[String](o.get("userContext")) match {
        case Some(s) => Some(encryptionManager.decryptAsString(s))
        case _ => None
      },
      extended = map[String](o.getAs[DBObject]("extended")),
      externalId = optional[String](o.get("externalId")))
  }

  protected override def checkRelationships(entity: Token, delta: Delta[Token] = null) {
    if (delta == null || delta.pathChanged("clientId")) {
      val client = clientService.load(entity.clientId)
      require(client.isDefined, s"The client with Id $entity.clientId does not exist.")
    }
  }

  override def toString = s"MongoDBTokenService($connectionManager)"
}