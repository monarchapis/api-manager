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

import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.DateTime

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.servlet.ApiRequest
import com.monarchapis.apimanager.util._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.inject.Inject

class MongoDBDeveloperService @Inject() (
  val connectionManager: MongoDBConnectionManager,
  val logService: LogService) extends DeveloperService with ServiceSupport[Developer] with Logging {
  require(connectionManager != null, "connectionManager is required")
  require(logService != null, "logService is required")

  info(s"$this")

  protected val entityName = "developer"
  protected val displayName = "developer"
  protected val entityClass = classOf[Developer]
  protected val aggregator = EntityEventAggregator.developer

  protected val collection = connectionManager()("developers")

  ensureIndex(collection, MongoDBObject("username_lc" -> 1), "developer-by-username", true)
  ensureIndex(collection, MongoDBObject("firstName_lc" -> 1, "lastName_lc" -> 1), "developer-by-firstName", false)
  ensureIndex(collection, MongoDBObject("lastName_lc" -> 1, "firstName_lc" -> 1), "developer-by-lastName", false)
  ensureIndex(collection, MongoDBObject("environmentIds" -> 1, "username_lc" -> 1), "developer-by-environmentId-username", true)
  // Sparse index by externalId
  ensureIndex(collection,
    MongoDBObject("externalId" -> 1),
    MongoDBObject(
      "name" -> "developer-by-externalId",
      "unique" -> true,
      "sparse" -> true))

  protected val labelField = "username"
  protected val fieldMap = MongoSchema[Developer]
    .field("id", "_id", FieldType.IDENTIFIER, true, updateable = false, accessor = (e) => e.id)
    .field("environmentId", "environmentIds", FieldType.CS_STRING, multivalued = true, updateable = false)
    .field("username", "username", FieldType.CI_STRING, true, accessor = (e) => e.username)
    .field("firstName", "firstName", FieldType.CI_STRING, true, accessor = (e) => e.firstName)
    .field("lastName", "lastName", FieldType.CI_STRING, true, accessor = (e) => e.lastName)
    .field("roles", "roles", FieldType.OTHER, false, accessor = (e) => toList(e.roles))
    .field("company", "company", FieldType.CI_STRING, false, accessor = (e) => e.company.orNull)
    .field("title", "title", FieldType.CI_STRING, false, accessor = (e) => e.title.orNull)
    .field("email", "email", FieldType.CI_STRING, false, accessor = (e) => e.email.orNull)
    .field("phone", "phone", FieldType.CI_STRING, false, accessor = (e) => e.phone.orNull)
    .field("mobile", "mobile", FieldType.CI_STRING, false, accessor = (e) => e.mobile.orNull)
    .field("address1", "address1", FieldType.CI_STRING, false, accessor = (e) => e.address1.orNull)
    .field("address2", "address2", FieldType.CI_STRING, false, accessor = (e) => e.address2.orNull)
    .field("locality", "locality", FieldType.CI_STRING, false, accessor = (e) => e.locality.orNull)
    .field("region", "region", FieldType.CI_STRING, false, accessor = (e) => e.region.orNull)
    .field("postalCode", "postalCode", FieldType.CI_STRING, false, accessor = (e) => e.postalCode.orNull)
    .field("countryCode", "countryCode", FieldType.CI_STRING, false, accessor = (e) => e.countryCode.orNull)
    .field("registrationIp", "registrationIp", FieldType.CS_STRING, false, accessor = (e) => e.registrationIp)
    .field("extended", "extended", FieldType.OTHER, false, accessor = (e) => {
      val builder = MongoDBObject.newBuilder[String, AnyRef]
      if (e.extended != null) e.extended foreach {
        case (key, value) => builder += key -> value
      }
      builder.result
    })
    .field("externalId", "externalId", FieldType.CS_STRING, false, accessor = (e) => NotSet.check(e.externalId.orNull))

  protected override def defaultSort = MongoDBObject("username_lc" -> 1)

  protected override def addSecurityFilter(builder: Builder[(String, Any), DBObject], filter: Map[String, List[String]]) {
    builder += "environmentIds" -> EnvironmentContext.current.id
  }

  protected def convert(o: DBObject) = {
    Developer(
      id = o.get("_id").toString,
      username = expect[String](o.get("username")),
      firstName = expect[String](o.get("firstName")),
      lastName = expect[String](o.get("lastName")),
      roles = set[String](o.getAs[MongoDBList]("roles")),
      company = optional[String](o.get("company")),
      title = optional[String](o.get("title")),
      email = optional[String](o.get("email")),
      phone = optional[String](o.get("phone")),
      mobile = optional[String](o.get("mobile")),
      address1 = optional[String](o.get("address1")),
      address2 = optional[String](o.get("address2")),
      locality = optional[String](o.get("locality")),
      region = optional[String](o.get("region")),
      postalCode = optional[String](o.get("postalCode")),
      countryCode = optional[String](o.get("countryCode")),
      registrationIp = optional[String](o.get("registrationIp")),
      extended = map(o.getAs[DBObject]("extended")),
      externalId = optional[String](o.get("externalId")),
      createdBy = expect[String](o.get("createdBy")),
      createdDate = expect[DateTime](datetime(o.get("createdDate"))),
      modifiedBy = expect[String](o.get("modifiedBy")),
      modifiedDate = expect[DateTime](datetime(o.get("modifiedDate"))))
  }

  def isWritable = true

  def findByUsername(username: String) = {
    require(username != null, "username is required")

    val q = MongoDBObject("username_lc" -> username.toLowerCase())
    create(collection.findOne(q))
  }

  def authenticate(username: String, password: String) = {
    require(username != null, "username is required")
    require(password != null, "password is required")

    val q = MongoDBObject("username_lc" -> username.toLowerCase())
    val user = collection.findOne(q)

    user match {
      case Some(u) => {
        val salt = expect[String](u.get("salt"))
        val expected = expect[String](u.get("password"))
        val sha256 = Hashing.sha256(password + salt)

        if (sha256 == expected) Some(convert(u)) else None
      }
      case _ => None
    }
  }

  def setPassword(developer: Developer, password: String) {
    val q = MongoDBObject("_id" -> new ObjectId(developer.id))

    val salt = RandomStringUtils.randomAlphanumeric(10)
    val hashed = Hashing.sha256(password + salt)

    val o = MongoDBObject(
      "salt" -> salt,
      "password" -> hashed)
    val s = MongoDBObject("$set" -> o)

    collection.update(q, s, false, false, WriteConcern.FsyncSafe)

    val labelObj = collection.findOne(q, MongoDBObject(labelField -> 1))
    labelObj match {
      case Some(obj) => logService.log("info", s"${AuthorizationHolder.current.name} reset password for $entityName ${o.get(labelField)}")
      case _ => None
    }
  }

  // Todo materialize environments
  def addApplication(developer: Developer, application: Application) = {
    val q = MongoDBObject("_id" -> new ObjectId(developer.id))
    val u = MongoDBObject("$addToSet" -> MongoDBObject("applicationIds" -> application.id))
    val result = collection.update(q, u, false, false, WriteConcern.FsyncSafe)

    val u2 = MongoDBObject("$addToSet" -> MongoDBObject("environmentIds" -> EnvironmentContext.current.id))
    collection.update(q, u2, false, false, WriteConcern.FsyncSafe)

    result.getN() > 0
  }

  def removeApplication(developer: Developer, application: Application) = {
    val q = MongoDBObject("_id" -> new ObjectId(developer.id))
    val u = MongoDBObject("$pull" -> MongoDBObject("members" -> application.id))
    val result = collection.update(q, u, false, false, WriteConcern.FsyncSafe)
    result.getN() > 0
  }

  protected override def createMixins(setBuilder: Builder[(String, Any), DBObject], entity: Developer) {
    setBuilder += "environmentIds" -> {
      val list = new BasicDBList()
      list.add(EnvironmentContext.current.id)
      list
    }
    setBuilder += "registrationIp" -> {
      val request = ApiRequest.current
      request.getHeader("X-Forwarded-For") match {
        case forwardedFor: String => forwardedFor.split(",")(0).trim
        case _ => request.getRemoteAddr
      }
    }
  }

  override def toString = s"MongoDBDeveloperService($connectionManager)"
}