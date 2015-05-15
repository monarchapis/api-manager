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

import java.util.Calendar
import java.util.Date

import scala.collection.JavaConversions._

import org.apache.commons.lang3.StringUtils
import org.bson.types.ObjectId
import org.joda.time.DateTime

import com.monarchapis.apimanager.service._
import com.mongodb.WriteConcernException
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging
import javax.annotation.PreDestroy
import javax.inject.Inject

trait MongoDBConnectionManager {
  def apply(): MongoDB
}

trait MultitenantMongoDBConnectionManager {
  def apply(database: String): MongoDB
  def apply(database: String, initialize: Boolean): MongoDB
  def dropDatabase(database: String)
  def addInitializer(initializer: (MongoDB) => Unit)
}

object BasicMongoDBConnectionManager {
  private def parseAddresses(servers: String): List[ServerAddress] = {
    val builder = List.newBuilder[ServerAddress]
    val parts = StringUtils.split(servers, ", ")

    parts foreach (part => {
      val parts = part.trim.split(':')
      if (parts.length == 2) {
        val address = new ServerAddress(parts(0), parts(1).toInt)
        builder += address
      } else {
        throw new IllegalArgumentException(s"Invalid server address $part")
      }
    })

    builder.result
  }

  private def parseCredentials(credentials: String): List[MongoCredential] = {
    val builder = List.newBuilder[MongoCredential]
    val parts = StringUtils.split(credentials, ", ")

    parts foreach (part => {
      val parts = part.trim.split(':')

      if (parts.length > 1) {
        parts(0) match {
          case "MONGODB-CR" => {
            if (parts.length == 4) {
              val credential = MongoCredential.createMongoCRCredential(parts(1), parts(2), parts(3).toCharArray())
              builder += credential
            } else {
              throw new IllegalArgumentException(s"Invalid server address $part")
            }
          }
          case "GSSAPI" => {
            if (parts.length == 2) {
              val credential = MongoCredential.createGSSAPICredential(parts(1))
              builder += credential
            } else {
              throw new IllegalArgumentException(s"Invalid server address $part")
            }
          }
          case "SASL-PLAIN" => {
            if (parts.length == 4) {
              val credential = MongoCredential.createPlainCredential(parts(1), parts(2), parts(3).toCharArray())
              builder += credential.withMechanismProperty("digestPassword", false)
            } else {
              throw new IllegalArgumentException(s"Invalid server address $part")
            }
          }
          case "PLAIN" => {
            if (parts.length == 4) {
              val credential = MongoCredential.createPlainCredential(parts(1), parts(2), parts(3).toCharArray())
              builder += credential
            } else {
              throw new IllegalArgumentException(s"Invalid server address $part")
            }
          }
          case "MONGODB-X509" => {
            if (parts.length == 2) {
              val credential = MongoCredential.createMongoX509Credential(parts(1))
              builder += credential
            } else {
              throw new IllegalArgumentException(s"Invalid server address $part")
            }
          }
          case "SCRAM-SHA-1" => {
            if (parts.length == 4) {
              val credential = MongoCredential.createScramSha1Credential(parts(1), parts(2), parts(3).toCharArray())
              builder += credential
            } else {
              throw new IllegalArgumentException(s"Invalid server address $part")
            }
          }
          case _ => throw new IllegalArgumentException(s"Unknown authentication mechanism ${parts(0)}")
        }
      } else {
        throw new IllegalArgumentException(s"Invalid server address $part")
      }
    })

    builder.result
  }
}

class BasicMongoDBConnectionManager(connection: MongoClient, systemDatabase: String)
  extends MongoDBConnectionManager with MultitenantMongoDBConnectionManager with Logging {

  def this(systemDatabase: String) = this(MongoClient(), systemDatabase)

  def this(
    servers: List[ServerAddress],
    credentials: List[MongoCredential],
    systemDatabase: String) = {
    this(MongoClient(servers, credentials), systemDatabase)
  }

  def this(
    servers: java.util.List[ServerAddress],
    credentials: java.util.List[MongoCredential],
    systemDatabase: String) = {
    this(MongoClient(servers.toList, credentials.toList), systemDatabase)
  }

  def this(
    servers: List[ServerAddress],
    credentials: List[MongoCredential],
    options: MongoClientOptions, systemDatabase: String) = {
    this(MongoClient(servers, credentials, options), systemDatabase)
  }

  def this(
    servers: java.util.List[ServerAddress],
    credentials: java.util.List[MongoCredential],
    options: MongoClientOptions,
    systemDatabase: String) = {
    this(MongoClient(servers.toList, credentials.toList, options), systemDatabase)
  }

  def this(
    servers: String,
    systemDatabase: String) = {
    this(MongoClient(
      BasicMongoDBConnectionManager.parseAddresses(servers)), systemDatabase)
  }

  def this(
    servers: String,
    options: MongoClientOptions, systemDatabase: String) = {
    this(MongoClient(
      BasicMongoDBConnectionManager.parseAddresses(servers),
      options), systemDatabase)
  }

  def this(
    servers: String,
    credentials: String,
    systemDatabase: String) = {
    this(MongoClient(
      BasicMongoDBConnectionManager.parseAddresses(servers),
      BasicMongoDBConnectionManager.parseCredentials(credentials)), systemDatabase)
  }

  def this(
    servers: String,
    credentials: String,
    options: MongoClientOptions, systemDatabase: String) = {
    this(MongoClient(
      BasicMongoDBConnectionManager.parseAddresses(servers),
      BasicMongoDBConnectionManager.parseCredentials(credentials),
      options), systemDatabase)
  }

  require(systemDatabase != null, "systemDatabase is required")

  private val databases = scala.collection.mutable.Map[String, MongoDB]()
  private val initializers = scala.collection.mutable.Buffer[(MongoDB) => Unit]()
  // TODO need to be sync across cluster.
  private val initialized = scala.collection.mutable.Set[String]()

  info(s"$this")

  def apply() = connection(systemDatabase)

  def apply(database: String) = apply(database, true)

  def apply(database: String, initialize: Boolean) = {
    databases.get(database) match {
      case Some(db) => db
      case _ => {
        val db = connection(database)
        databases += database -> db

        if (initialize && !initialized(database)) {
          initialized += database

          initializers foreach { init => init(db) }
        }

        db
      }
    }
  }

  def dropDatabase(database: String) {
    connection.dropDatabase(database)
    databases -= database
    initialized -= database
  }

  def addInitializer(initializer: (MongoDB) => Unit) { initializers += initializer }

  @PreDestroy
  def close() = connection.close

  override def toString = s"BasicMongoDBConnectionManager($systemDatabase)"
}

trait MongoDBNameProvider {
  def databaseNames: Set[String]
}

class MongoDBInitializerRegistry @Inject() (
  private val connectionManager: MultitenantMongoDBConnectionManager,
  private val provider: MongoDBNameProvider) {
  provider.databaseNames foreach {
    // Just requesting the connection will fire the initializers
    dbName => connectionManager(dbName)
  }
}

abstract class MongoInternalCursor[T](c: MongoCursor) extends Cursor[T] {
  def hasNext = c.hasNext
  def close = c.close
  def count = c.count
}

trait MongoDBUtils extends Logging {
  protected def identifier(value: Any): String = {
    value match {
      case v: ObjectId => v.toString()
      case v: String => v
      case _ => throw new ClassCastException
    }
  }

  protected def optionalId(value: Any): Option[String] = {
    value match {
      case v: ObjectId => Some(v.toString)
      case v: String => Some(v)
      case _ => None
    }
  }

  protected def integer(value: Any): Any = {
    value match {
      case v: java.lang.Double => v.toInt
      case v: java.lang.Long => v.toInt
      case v: java.lang.Integer => v.toInt
      case _ => value
    }
  }

  protected def datetime(value: Any): Any = {
    value match {
      case v: Date => {
        var cal = Calendar.getInstance();
        cal.setTime(v)
        new DateTime(cal)
      }
      case v: Calendar => new DateTime(v)
      case _ => value
    }
  }

  protected def expect[T](value: Option[T]) = {
    require(value.isDefined, "A value is required")
    value.get
  }

  protected def expect[T](value: Any) = {
    require(value != null, "A value is required")
    value.asInstanceOf[T]
  }

  protected def expect[T: Manifest](value: Any, defaultValue: T) = {
    val m = manifest[T]
    if (value != null && m.runtimeClass.isInstance(value)) value.asInstanceOf[T] else defaultValue
  }

  protected def optional[T: Manifest](value: Any) = {
    val m = manifest[T]
    if (m.runtimeClass.isInstance(value))
      Some(value.asInstanceOf[T])
    else
      None
  }

  protected def map[T](obj: Option[DBObject], convertFunc: (DBObject) => T = null): Map[String, T] = {
    val builder = Map.newBuilder[String, T]

    obj match {
      case Some(o) => o foreach {
        case (key, value) => builder += key -> {
          convertFunc match {
            case func: ((DBObject) => T) => func(value.asInstanceOf[DBObject])
            case _ => value.asInstanceOf[T]
          }
        }
      }
      case _ =>
    }

    builder.result
  }

  protected def toObject[T](map: Map[String, T]): DBObject = {
    val builder = MongoDBObject.newBuilder[String, T]
    if (map != null) map foreach { case (key, value) => builder += key -> value }
    builder.result
  }

  protected def toList[T <: AnyRef](set: Iterable[T]): BasicDBList = {
    val list = new BasicDBList

    if (set != null) set.foreach(v => list.add(v))

    list
  }

  protected def set[T](list: Option[MongoDBList]): Set[T] = {
    val builder = Set.newBuilder[T]

    list match {
      case Some(l) => l foreach (v => builder += v.asInstanceOf[T])
      case _ =>
    }

    builder.result
  }

  protected def list[T](list: Option[MongoDBList]): List[T] = {
    val builder = List.newBuilder[T]

    list match {
      case Some(l) => l foreach (v => builder += v.asInstanceOf[T])
      case _ =>
    }

    builder.result
  }

  protected def listOf[T](list: Option[MongoDBList], convertFunc: (DBObject) => T): List[T] = {
    val builder = List.newBuilder[T]

    list match {
      case Some(l) => l foreach (v => builder += convertFunc(v.asInstanceOf[DBObject]))
      case _ =>
    }

    builder.result
  }

  protected def listFromDBList[T](list: MongoDBList): List[T] = {
    val builder = List.newBuilder[T]

    list foreach (v => builder += v.asInstanceOf[T])

    builder.result
  }

  def long(a: AnyRef): Option[Long] = {
    a match {
      case v: java.lang.Integer => Some(v.longValue)
      case v: java.lang.Long => Some(v.longValue)
      case _ => None
    }
  }

  protected def applyPagination(pagination: Option[Pagination], cursor: com.mongodb.casbah.MongoCursor) = {
    pagination match {
      case Some(p) => {
        p.skip match {
          case Some(skip) => cursor.skip(skip)
          case _ =>
        }
        p.limit match {
          case Some(limit) => cursor.limit(limit)
          case _ =>
        }
      }
      case _ =>
    }
    cursor
  }

  protected def ensureIndex(collection: MongoCollection, fields: MongoDBObject, name: String, unique: Boolean = false) {
    try {
      collection.ensureIndex(fields, name, unique)
    } catch {
      case wce: MongoException => {
        // Drop and recreate the index if the spec has changed 
        if (wce.getCode == 86) {
          info(s"Recreating index '$name' on collection ${collection.name}")
          collection.dropIndex(name)
          collection.ensureIndex(fields, name, unique)
        } else {
          throw wce
        }
      }
    }
  }

  protected def ensureIndex(collection: MongoCollection, fields: MongoDBObject, options: MongoDBObject) {
    try {
      collection.createIndex(fields, options)
    } catch {
      case wce: WriteConcernException => {
        // Drop and recreate the index if the spec has changed 
        if (wce.getCode() == 86) {
          var name = options("name").asInstanceOf[String]
          info(s"Recreating index '$name' on collection ${collection.name}")
          collection.dropIndex(name)
          collection.createIndex(fields, options)
        } else {
          throw wce
        }
      }
    }
  }
}