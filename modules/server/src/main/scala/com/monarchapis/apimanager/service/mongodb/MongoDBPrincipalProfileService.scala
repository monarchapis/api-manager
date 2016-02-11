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
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.CacheManager

class MongoDBPrincipalProfileService @Inject() (
    val connectionManager: MultitenantMongoDBConnectionManager,
    val logService: LogService,
    val cacheManager: CacheManager) extends PrincipalProfileService with ServiceSupport[PrincipalProfile] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(logService != null, "logService is required")
  require(cacheManager != null, "cacheManager is required")

  info(s"$this")

  val cache = cacheManager.getCache("principalProfiles")

  protected val entityName = "principalProfile"
  protected val displayName = "principal profile"
  protected val entityClass = classOf[PrincipalProfile]
  protected val aggregator = EntityEventAggregator.principalProfile

  protected def collection = connectionManager(EnvironmentContext.current.systemDatabase)("principalProfiles")

  connectionManager.addInitializer((db: MongoDB) => {
    debug("Initializing principal profiles")
    val collection = db("principalProfiles")
    ensureIndex(collection, MongoDBObject("name_lc" -> 1), "principalProfiles-by-name", true)
  })

  protected val labelField = "name"
  protected val fieldMap = MongoSchema[PrincipalProfile]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("name", "name", FieldType.CI_STRING, true, accessor = (e) => e.name)

  protected override def defaultSort = MongoDBObject("name_lc" -> 1)

  //DisplayLabelSources.lookup += "principals" -> this

  def getDisplayLabels(ids: Set[String]): Map[String, String] = getDisplayLabels(ids, "name")

  protected override def handleCacheEvict(principalProfile: PrincipalProfile) {
    cache.evict(principalProfile.id)
    cache.evict("name:" + principalProfile.name)
  }

  @Cacheable(value = Array("principalProfiles"), key = "'name:'.concat(#name)")
  def findByName(name: String): Option[PrincipalProfile] = {
    val q = MongoDBObject("name_lc" -> name.toLowerCase)
    val entity = create(collection.findOne(q))
    if (entity.isDefined) checkReadAccess(entity.get)
    entity
  }

  protected def convert(o: DBObject) = {
    PrincipalProfile(
      id = o.get("_id").toString,
      name = expect[String](o.get("name")),
      createdBy = expect[String](o.get("createdBy")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      modifiedBy = expect[String](o.get("modifiedBy")),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate"))))
  }

  override def toString = s"MongoDBPrincipalProfileService($connectionManager)"
}