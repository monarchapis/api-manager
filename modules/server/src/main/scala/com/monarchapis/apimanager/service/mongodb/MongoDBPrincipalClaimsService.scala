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

class MongoDBPrincipalClaimsService @Inject() (
  val connectionManager: MultitenantMongoDBConnectionManager,
  val principalProfileService: PrincipalProfileService,
  val logService: LogService) extends PrincipalClaimsService with ServiceSupport[PrincipalClaims] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(principalProfileService != null, "principalProfileService is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "principalClaims"
  protected val displayName = "principal claims"
  protected val entityClass = classOf[PrincipalClaims]
  protected val aggregator = EntityEventAggregator.principalClaims

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("principalClaims")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing principal claims")
    val collection = db("principalClaims")
    ensureIndex(collection, MongoDBObject("profileId" -> 1, "name_lc" -> 1), "principalClaims-by-name", true)
  })

  protected val labelField = "name"
  protected val fieldMap = MongoSchema[PrincipalClaims]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("profileId", "profileId", FieldType.CS_STRING, false, accessor = (e) => e.profileId, extras = (set, e) => {
      if (!principalProfileService.exists(e.profileId)) {
        throw new ConflictException("Could not find the referenced claim profile")
      }
    })
    .field("name", "name", FieldType.CI_STRING, true, accessor = (e) => e.name)
    .field("inherits", "inherits", FieldType.OTHER, false, accessor = (e) => toList(e.inherits))
    .field("claims", "claims", FieldType.OTHER, false, accessor = (e) => {
      val builder = MongoDBObject.newBuilder[String, MongoDBList]

      if (e.claims != null) {
        e.claims foreach {
          case (key, values) => builder += key -> toList(values)
        }
      }

      builder.result
    }, extras = (set, e) => {
      if (e.inherits != null) {
        if (e.inherits.contains(e.id)) {
          throw new ConflictException("Principal claims cannot inherit themselves")
        }
        if (!this.exists(e.inherits)) {
          throw new ConflictException("Could not find a referenced inherited principal claims")
        }
      }
    })

  protected override def defaultSort = MongoDBObject("name_lc" -> 1)

  EntityEventAggregator.principalProfile += onPrincipalProfileChange

  def findByName(profileId: String, name: String): Option[PrincipalClaims] = {
    val q = MongoDBObject("profileId" -> profileId, "name_lc" -> name.toLowerCase)
    val entity = create(collection.findOne(q))
    if (entity.isDefined) checkReadAccess(entity.get)
    entity
  }

  private def onPrincipalProfileChange(principalProfile: PrincipalProfile, eventType: String) {
    eventType match {
      case "predelete" => if (exists(Map("profileId" -> List(principalProfile.id)))) {
        throw new InvalidParamaterException("These principal claims cannot be deleted because it is being referenced by a profile.")
      }
      case _ =>
    }
  }

  protected def convert(o: DBObject) = {
    PrincipalClaims(
      id = o.get("_id").toString,
      profileId = expect[String](o.get("profileId")),
      name = expect[String](o.get("name")),
      inherits = set[String](o.getAs[MongoDBList]("inherits")),
      claims = {
        val builder = Map.newBuilder[String, Set[String]]

        o.getAs[DBObject]("claims") match {
          case Some(o) => {
            for (key <- o.keys) {
              val values = o.getAs[MongoDBList](key).get map (v => v.asInstanceOf[String]) toSet;
              builder += (key -> values)
            }
          }
          case _ =>
        }

        builder.result
      },
      createdBy = expect[String](o.get("createdBy")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      modifiedBy = expect[String](o.get("modifiedBy")),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate"))))
  }

  override def toString = s"MongoDBPrincipalClaimsService($connectionManager)"
}