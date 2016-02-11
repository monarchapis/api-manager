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

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._
import com.monarchapis.apimanager.exception._
import com.mongodb.casbah.Imports._

object FieldType extends Enumeration {
  case class EValue(name: String) extends Val(name) {}

  val IDENTIFIER = EValue("identifier")
  val CS_STRING = EValue("cs_string")
  val CI_STRING = EValue("ci_string")
  val ENC_STRING = EValue("enc_string")
  val ENC_SALTED_STRING = EValue("enc_salted_string")
  val INTEGER = EValue("integer")
  val BOOLEAN = EValue("boolean")
  val DATETIME = EValue("datetime")
  val OTHER = EValue("other")
}

object ServiceEvent extends Enumeration {
  case class EValue(name: String) extends Val(name) {}

  val CREATE = EValue("create")
  val UPDATE = EValue("update")
  val DELETE = EValue("delete")
}

case class MongoField[T](
  fieldName: String,
  fieldType: FieldType.Value,
  required: Boolean = false,
  creatable: Boolean = true,
  updateable: Boolean = true,
  multivalued: Boolean = false,
  accessor: (T) => Any = null,
  extras: (Builder[(String, Any), DBObject], T) => Unit = null) {
  //require(accessor != null, "Accessor is required")

  val lowerCaseField = fieldType match {
    case FieldType.CI_STRING => fieldName + "_lc"
    case _ => fieldName
  }
}

case class MongoSchema[T <: Entity]() {
  private val lookup = scala.collection.mutable.Map[String, MongoField[T]]()

  def field(
    propertyName: String,
    fieldName: String,
    fieldType: FieldType.Value,
    required: Boolean = false,
    creatable: Boolean = true,
    updateable: Boolean = true,
    multivalued: Boolean = false,
    accessor: (T) => Any = null,
    extras: (Builder[(String, Any), DBObject], T) => Unit = null) = {
    lookup += propertyName -> MongoField(
      fieldName,
      fieldType,
      required,
      creatable,
      updateable,
      multivalued,
      accessor,
      extras)
    this
  }

  def apply(fieldName: String) = lookup(fieldName)
  def contains(fieldName: String) = lookup.contains(fieldName)
  def propertyNames = lookup.keys
}

object NotSet {
  val Value = new Object

  def check(value: Any) = if (value == null) Value else value
}

object BuilderAppender {
  private lazy val encryptionManager = ApplicationContextProvider().getBean(classOf[EncryptionManager])
}

class BuilderAppender[T <: Entity](
  val setBuilder: Builder[(String, Any), DBObject],
  val unsetBuilder: Builder[(String, Any), DBObject],
  val entity: T,
  val schema: MongoSchema[T],
  val event: ServiceEvent.Value,
  val delta: Delta[T] = null) {

  import BuilderAppender._

  def apply(key: String) {
    if (delta == null || delta.pathChanged(key)) {
      val field = schema(key)

      if (event == ServiceEvent.CREATE && !field.creatable) {
        throw new BadRequestException(s"${field.fieldName} is not creatable")
      }

      if (event == ServiceEvent.UPDATE && !field.updateable) {
        //if (field.fieldName == "_id") return
        //throw new Exception(s"${field.fieldName} is not updateable")
        return
      }

      if (field.accessor != null) {
        val value = field.accessor(entity)

        if (field.required && value == null && field.fieldType != FieldType.IDENTIFIER) {
          throw new BadRequestException(s"${field.fieldName} is a required field")
        }

        if (value != null && value == NotSet.Value) {
          unsetBuilder += field.fieldName -> ""

          if (field.fieldType == FieldType.CI_STRING) {
            unsetBuilder += field.fieldName + "_lc" -> ""
          }
        } else {
          field.fieldType match {
            case FieldType.ENC_STRING => {
              val encVal = if (value != null) encryptionManager.encrypt(value.asInstanceOf[String]) else null
              setBuilder += field.fieldName -> encVal
            }
            case FieldType.ENC_SALTED_STRING => {
              val encVal = if (value != null) encryptionManager.encryptSalted(value.asInstanceOf[String]) else null
              setBuilder += field.fieldName -> encVal
            }
            case _ => {
              setBuilder += field.fieldName -> value

              if (field.fieldType == FieldType.CI_STRING) {
                setBuilder += field.fieldName + "_lc" -> (if (value != null) value.toString.toLowerCase else null)
              }
            }
          }
        }
      }
      if (field.extras != null) {
        field.extras(setBuilder, entity)
      }
    }
  }

  def all = schema.propertyNames foreach (property => this(property))
}

object ServiceSupport {
  private val special = Set(
    // order matters for these
    '-', '[', ']' // order doesn't matter for any of these
    , '/', '{', '}', '(', ')', '*', '+', '?', '.', '\\', '^', '$', '|')

  private def escapeStringForRegex(value: String) = {
    val sb = new StringBuilder

    for (c <- value) {
      if (special.contains(c)) sb.append('\\')
      sb.append(c)
    }

    sb.toString
  }

  private lazy val encryptionManager = ApplicationContextProvider().getBean(classOf[EncryptionManager])
  
  private val QUOTE = "\"";
}

trait ServiceSupport[T <: Entity] extends MongoDBUtils {
  import ServiceSupport._

  protected val logService: LogService
  protected val fieldMap: MongoSchema[T]
  protected val labelField: String

  protected val entityName: String
  protected val displayName: String
  protected val entityClass: Class[T]
  protected val aggregator: EntityEventAggregator[T]

  protected def collection: MongoCollection

  protected def build(
    setBuilder: Builder[(String, Any), DBObject],
    unsetBuilder: Builder[(String, Any), DBObject],
    entity: T,
    event: ServiceEvent.Value) {
    build(new BuilderAppender(setBuilder, unsetBuilder, entity, fieldMap, event))
  }

  protected def build(
    setBuilder: Builder[(String, Any), DBObject],
    unsetBuilder: Builder[(String, Any), DBObject],
    delta: Delta[T]) {
    build(new BuilderAppender(setBuilder, unsetBuilder, delta.entity, fieldMap, ServiceEvent.UPDATE, delta))
  }

  protected def build(appender: BuilderAppender[T]) {
    appender.all
  }

  protected def convert(o: DBObject): T
  protected def create(o: Option[DBObject]) = {
    o match {
      case Some(o) => Some(convert(o))
      case _ => None
    }
  }

  def count(
    filter: Map[String, List[String]] = Map()) = {
    val q = toQuery(filter)

    if (q.size > 0) {
      AuthorizationUtils.check(EntityAction.READ, entityName)
    }

    collection.count(q)
  }

  def find(
    offset: Integer = 0,
    limit: Integer = 10,
    filter: Map[String, List[String]] = Map(),
    orderBy: List[OrderByField] = List(),
    expand: Set[String] = Set()) = {
    AuthorizationUtils.check(EntityAction.READ, entityName)

    val q = toQuery(filter)
    val s = toSort(orderBy)
    val data = collection.find(q).sort(s).skip(offset).limit(limit)
    val total = data.count

    val items = handleExpand(
      data.toList.map(i => convert(i)),
      expand)

    new PagedList[T](
      offset = offset,
      limit = limit,
      count = items.size,
      total = total,
      items = items)
  }

  def exists(filter: Map[String, List[String]] = Map()) = {
    val q = toQuery(filter)
    val data = collection.findOne(q)
    data.isDefined
  }

  def exists(ids: Set[String]) = {
    val q = MongoDBObject("_id" -> MongoDBObject("$in" -> ids.map(id => new ObjectId(id))))
    val count = collection.count(q)
    count == ids.size
  }

  def exists(id: String) = {
    val q = MongoDBObject("_id" -> new ObjectId(id))
    collection.count(q) == 1
  }

  def load(id: String) = {
    AuthorizationUtils.check(EntityAction.READ, entityName)

    try {
      val q = MongoDBObject("_id" -> new ObjectId(id))
      val entity = create(collection.findOne(q))
      if (entity.isDefined) checkReadAccess(entity.get)
      entity
    } catch {
      case iar: IllegalArgumentException => None
    }
  }

  def loadSet(
    ids: Set[String],
    offset: Integer = 0,
    limit: Integer = 10,
    filter: Map[String, List[String]] = Map(),
    orderBy: List[OrderByField] = List()) = {
    AuthorizationUtils.check(EntityAction.READ, entityName)

    val q = toQuery(filter, ids)
    val s = toSort(orderBy)
    val data = collection.find(q).sort(s).skip(offset).limit(limit)
    /*val m = data.map(i => {
      val e = convert(i)
      (e.id, e)
    }).toMap
    val builder = List.newBuilder[T]
    ids foreach { id => builder += m(id) }
    builder.result*/

    val total = data.count
    var items = data.toList.map(i => convert(i))

    new PagedList[T](
      offset = offset,
      limit = limit,
      count = items.size,
      total = total,
      items = items)
  }

  def loadMap(
    ids: Set[String]) = {
    val q = MongoDBObject("_id" -> MongoDBObject("$in" -> ids.map(id => new ObjectId(id))))
    val data = collection.find(q)

    var items = data.toList.map(i => convert(i))
    items.map { i => (i.id, i) } toMap
  }

  protected def handleExpand(items: List[T], expand: Set[String]) = {
    items
  }

  protected def checkReadAccess(entity: T) {}

  def create(entity: T) = {
    AuthorizationUtils.check(EntityAction.CREATE, entityName)

    validate(ServiceEvent.CREATE, entity)
    checkCreateAccess(entity)
    checkRelationships(entity)

    val setBuilder = MongoDBObject.newBuilder
    val unsetBuilder = MongoDBObject.newBuilder

    build(setBuilder, unsetBuilder, entity, ServiceEvent.CREATE)

    entity match {
      case audit: AuditSupport => {
        val user = UserContext.current
        val now = DateTime.now.toDate
        setBuilder += "createdBy" -> user
        setBuilder += "createdDate" -> now
        setBuilder += "modifiedBy" -> user
        setBuilder += "modifiedDate" -> now
        null
      }
      case _ => null
    }

    createMixins(setBuilder, entity)

    val o = setBuilder.result
    collection.insert(o, WriteConcern.FsyncSafe)

    if (labelField != null && EnvironmentContext.isSet) {
      logService.log("info", s"${AuthorizationHolder.current.name} created $displayName $QUOTE${o.get(labelField)}$QUOTE")
    }

    convert(o)
  }

  protected def createMixins(setBuilder: Builder[(String, Any), DBObject], entity: T) {}

  protected def checkCreateAccess(entity: T) {}

  protected def checkRelationships(entity: T, delta: Delta[T] = null) {}

  def update(entity: T): Option[T] = {
    AuthorizationUtils.check(EntityAction.UPDATE, entityName)

    validate(ServiceEvent.UPDATE, entity)
    checkUpdateAccess(entity)
    checkRelationships(entity)

    val q = MongoDBObject("_id" -> new ObjectId(entity.id))
    val setBuilder = MongoDBObject.newBuilder
    val unsetBuilder = MongoDBObject.newBuilder
    build(setBuilder, unsetBuilder, entity, ServiceEvent.UPDATE)

    entity match {
      case audit: AuditSupport => {
        val user = UserContext.current
        val now = DateTime.now.toDate
        setBuilder += "modifiedBy" -> user
        setBuilder += "modifiedDate" -> now
      }
      case _ =>
    }

    val s = MongoDBObject("$set" -> setBuilder.result, "$unset" -> unsetBuilder.result)

    val res = collection.update(q, s, false, false, WriteConcern.FsyncSafe)
    handleCacheEvict(entity)

    if (res.getN != 1) {
      return None
    }

    val ret = create(collection.findOne(q))

    if (ret.isDefined) {
      aggregator.apply(ret.get, "update")
    }

    if (labelField != null) {
      val labelObj = collection.findOne(q, MongoDBObject(labelField -> 1))
      labelObj match {
        case Some(obj) => if (EnvironmentContext.isSet) { logService.log("info", s"${AuthorizationHolder.current.name} updated $displayName $QUOTE${obj.get(labelField)}$QUOTE") }
        case _ => None
      }
    }

    ret
  }

  def update(delta: Delta[T]): Option[T] = {
    AuthorizationUtils.check(EntityAction.UPDATE, entityName)

    val entity = delta.entity
    validate(ServiceEvent.UPDATE, entity, delta)
    checkUpdateAccess(entity, delta)
    checkRelationships(entity, delta)

    val q = MongoDBObject("_id" -> new ObjectId(entity.id))

    val check = create(collection.findOne(q))
    if (check.isEmpty) return None
    checkUpdateAccess(check.get)

    val setBuilder = MongoDBObject.newBuilder
    val unsetBuilder = MongoDBObject.newBuilder
    build(setBuilder, unsetBuilder, delta)

    if (classOf[AuditSupport].isAssignableFrom(entityClass)) {
      val user = UserContext.current
      val now = DateTime.now.toDate
      setBuilder += "modifiedBy" -> user
      setBuilder += "modifiedDate" -> now
    }

    val u = MongoDBObject.newBuilder

    var set = setBuilder.result
    var unset = unsetBuilder.result

    if (!set.isEmpty) {
      u += "$set" -> set
    }

    if (!unset.isEmpty) {
      u += "$unset" -> unset
    }

    val s = u.result

    if (!s.isEmpty) {
      val res = collection.update(q, s, false, false, WriteConcern.FsyncSafe)
      handleCacheEvict(check.get)

      if (res.getN != 1) {
        return None
      }
    }

    val ret = create(collection.findOne(q))

    if (ret.isDefined) {
      aggregator.apply(ret.get, "update")
    }

    if (labelField != null) {
      val labelObj = collection.findOne(q, MongoDBObject(labelField -> 1))
      labelObj match {
        case Some(obj) => if (EnvironmentContext.isSet) { logService.log("info", s"${AuthorizationHolder.current.name} updated $displayName $QUOTE${obj.get(labelField)}$QUOTE") }
        case _ => None
      }
    }

    ret
  }

  def update(id: String, delta: Map[String, List[Any]]): Option[T] = {
    AuthorizationUtils.check(EntityAction.UPDATE, entityName)

    val q = MongoDBObject("_id" -> new ObjectId(id))

    val check = create(collection.findOne(q))
    if (check.isEmpty) return None
    checkUpdateAccess(check.get)

    val builder = MongoDBObject.newBuilder

    delta foreach {
      case (key, values) => {
        val field = resolveField(key)

        if (!field.multivalued) {
          if (values.length > 0) {
            val value = values(0)
            val converted = value match {
              case s: String => convertType(s, field)
              case _ => checkType(value, field.fieldType)
            }
            builder += field.fieldName -> converted

            if (field.fieldType == FieldType.CI_STRING) {
              builder += field.lowerCaseField -> converted.asInstanceOf[String].toLowerCase()
            }
          }
        } else {
          val converted = values map (value => value match {
            case s: String => convertType(s, field)
            case _ => checkType(value, field.fieldType)
          })

          builder += field.fieldName -> converted
        }
      }
    }

    val o = builder.result
    val s = MongoDBObject("$set" -> o)

    val res = collection.update(q, s, false, false, WriteConcern.FsyncSafe)
    handleCacheEvict(check.get)

    if (res.getN != 1) {
      return None
    }

    val ret = create(collection.findOne(q))

    if (ret.isDefined) {
      aggregator.apply(ret.get, "update")
    }

    if (labelField != null) {
      val labelObj = collection.findOne(q, MongoDBObject(labelField -> 1))
      labelObj match {
        case Some(obj) => if (EnvironmentContext.isSet) { logService.log("info", s"${AuthorizationHolder.current.name} updated $displayName $QUOTE${obj.get(labelField)}$QUOTE") }
        case _ => None
      }
    }

    ret
  }
  
  protected def handleCacheEvict(entity: T) {}

  protected def checkUpdateAccess(entity: T, delta: Delta[T] = null) {}

  def delete(entity: T): Boolean = {
    delete(entity.id)
  }

  def delete(id: String): Boolean = {
    AuthorizationUtils.check(EntityAction.DELETE, entityName)

    val q = MongoDBObject("_id" -> new ObjectId(id))

    val obj = collection.findOne(q)
    val check = create(obj)
    if (check.isEmpty) return false
    validateOnDelete(check.get)
    checkUpdateAccess(check.get)
    aggregator.apply(check.get, "predelete")

    val res = collection.remove(q, WriteConcern.FsyncSafe)
    handleCacheEvict(check.get)

    if (res.getN == 1) {
      aggregator.apply(check.get, "delete")

      if (labelField != null) {
        obj match {
          case Some(obj) => if (EnvironmentContext.isSet) { logService.log("info", s"${AuthorizationHolder.current.name} deleted $displayName $QUOTE${obj.get(labelField)}$QUOTE") }
          case _ => None
        }
      }

      true
    } else false
  }

  protected def validate(event: ServiceEvent.Value, entity: T, delta: Delta[T] = null) {
    event match {
      case ServiceEvent.CREATE => validateOnCreate(entity, delta)
      case ServiceEvent.UPDATE => validateOnUpdate(entity, delta)
    }
  }

  protected def validateOnCreate(entity: T, delta: Delta[T] = null) {
  }

  protected def validateOnUpdate(entity: T, delta: Delta[T] = null) {
  }

  protected def validateOnDelete(entity: T, delta: Delta[T] = null) {
  }

  private def toQuery(filter: Map[String, List[String]], ids: Set[String] = null) = {
    val builder = MongoDBObject.newBuilder

    if (ids != null) {
      builder += "_id" -> MongoDBObject("$in" -> ids.map(id => new ObjectId(id)))
    }

    addSecurityFilter(builder, filter)

    filter foreach {
      case (key, values) => {
        val field = resolveField(key)

        if (values.size == 1) {
          builder += field.lowerCaseField -> toQueryValue(values(0), field.fieldType)
        } else if (values.size > 1) {
          val converted = values map (v => toQueryValue(v, field.fieldType))
          builder += field.lowerCaseField -> MongoDBObject("$in" -> converted)
        }
      }
    }

    builder.result
  }

  protected def addSecurityFilter(builder: Builder[(String, Any), DBObject], filter: Map[String, List[String]]) {
  }

  protected def append(builder: Builder[(String, Any), DBObject], delta: Delta[T], key: String, value: Any, lowerCase: Boolean = false) {
    if (delta.pathChanged(key)) {
      builder += key -> value

      if (lowerCase) {
        builder += key + "lc" -> (if (value != null) value.toString.toLowerCase else null)
      }
    }
  }

  protected def defaultSort: DBObject

  private def toSort(orderBy: List[OrderByField]): DBObject = {
    if (orderBy.size == 0) {
      return defaultSort
    }

    val builder = MongoDBObject.newBuilder[String, Int]

    orderBy foreach (field => {
      val mf: MongoField[_] = resolveField(field.field)
      val dir: Int = field.direction match {
        case OrderDirection.ASCENDING => 1
        case OrderDirection.DESCENDING => -1
        case _ => 1
      }

      builder += mf.fieldName -> dir
    })

    builder.result
  }

  private def resolveField(field: String) = {
    require(fieldMap.contains(field), s"$field is not a valid field")
    fieldMap(field)
  }

  private def convertType(value: String, field: MongoField[_]) = {
    field.fieldType match {
      case FieldType.IDENTIFIER => {
        val v = StringUtils.trimToNull(value)
        if (v == null && field.required) {
          throw new RuntimeException(s"Field ${field.fieldName} is required")
        }
        new ObjectId(v)
      }
      case FieldType.CS_STRING => {
        val v = StringUtils.trimToNull(value)
        if (v == null && field.required) {
          throw new RuntimeException(s"Field ${field.fieldName} is required")
        }
        v
      }
      case FieldType.CI_STRING => {
        val v = StringUtils.trimToNull(value)
        if (v == null && field.required) {
          throw new RuntimeException(s"Field ${field.fieldName} is required")
        }
        v
      }
      case FieldType.INTEGER => value.toInt
      case FieldType.BOOLEAN => value.toBoolean
      case FieldType.DATETIME => DateTime.parse(value)
      case FieldType.ENC_STRING => encryptionManager.encrypt(value)
      case FieldType.ENC_SALTED_STRING => encryptionManager.encryptSalted(value)
      case _ => value
    }
  }

  private def toQueryValue(value: String, fieldType: FieldType.Value) = {
    fieldType match {
      case FieldType.IDENTIFIER => new ObjectId(value.trim)
      case FieldType.CS_STRING => regexCheck(value)
      case FieldType.CI_STRING => regexCheck(StringUtils.lowerCase(value))
      case FieldType.INTEGER => value.toInt
      case FieldType.BOOLEAN => value.toBoolean
      case FieldType.DATETIME => DateTime.parse(value)
      case FieldType.ENC_STRING => encryptionManager.encrypt(value)
      case FieldType.ENC_SALTED_STRING => encryptionManager.encryptSalted(value)
      case _ => value
    }
  }

  private def checkType(value: Any, fieldType: FieldType.Value) = {
    value match {
      case a: Any => fieldType match {
        case FieldType.CS_STRING => value.asInstanceOf[String]
        case FieldType.CI_STRING => value.asInstanceOf[String]
        case FieldType.ENC_STRING => value.asInstanceOf[String]
        case FieldType.ENC_SALTED_STRING => value.asInstanceOf[String]
        case FieldType.INTEGER => value.asInstanceOf[Int]
        case FieldType.BOOLEAN => value.asInstanceOf[Boolean]
        case FieldType.DATETIME => value.asInstanceOf[DateTime].toGregorianCalendar()
        case _ => value
      }
      case _ => null
    }
  }

  private def regexCheck(value: String): Any = {
    if (value.length() > 2 && value.startsWith("*") && value.endsWith("*")) {
      val trimmed = escapeStringForRegex(value.substring(1, value.length() - 1))
      val regex = ("^.*" + trimmed + ".*$").r
      return regex
    } else if (value.endsWith("*")) {
      val trimmed = escapeStringForRegex(value.substring(0, value.length() - 1))
      val regex = ("^" + trimmed + ".*").r
      return regex
    } else if (value.startsWith("*")) {
      val trimmed = escapeStringForRegex(value.substring(1, value.length()))
      val regex = ("^.*" + trimmed + "$").r
      return regex
    }

    value
  }

  /*
   * Common / Shared conversions
   */

  protected def convert(l: List[Configuration]): MongoDBList = {
    val list = new BasicDBList
    if (l != null) {
      l foreach { value => list.add(convert(value)) }
    }
    list
  }

  protected def convert(o: Configuration): DBObject = {
    MongoDBObject(
      "name" -> o.name,
      "properties" -> convertProperties(o.properties))
  }

  protected def convert(o: Map[String, Map[String, List[Any]]]): DBObject = {
    val builder = MongoDBObject.newBuilder[String, DBObject]

    o foreach {
      case (key, value) => builder += key -> convertProperties(value)
    }

    builder.result
  }

  protected def convertProperties(properties: Map[String, List[Any]]): DBObject = {
    val builder = MongoDBObject.newBuilder[String, DBObject]

    if (properties != null) {
      properties foreach {
        case (key, value) => builder += key -> value
      }
    }

    builder.result
  }

  protected def convertConfiguration(list: MongoDBList): List[Configuration] = {
    val builder = List.newBuilder[Configuration]

    if (list != null) {
      list foreach { a =>
        a match {
          case a: DBObject =>
            {
              val name = expect[String](a.get("name"))
              val properties = convertProperties(a.getAs[DBObject]("properties"))

              builder += new Configuration(name, properties)
            }
          case _ =>
        }
      }
    }

    builder.result
  }

  protected def convertMappedProperties(properties: Option[DBObject]): Map[String, Map[String, List[Any]]] = {
    properties match {
      case Some(o) => convertMappedProperties(o)
      case _ => Map()
    }
  }

  protected def convertMappedProperties(properties: MongoDBObject): Map[String, Map[String, List[Any]]] = {
    val builder = Map.newBuilder[String, Map[String, List[Any]]]

    if (properties != null) {
      properties foreach {
        case (key: String, value: DBObject) => builder += key -> convertProperties(value)
      }
    }

    builder.result
  }

  protected def convertProperties(properties: Option[DBObject]): Map[String, List[Any]] = {
    properties match {
      case Some(o) => convertProperties(o)
      case _ => Map()
    }
  }

  protected def convertProperties(properties: DBObject): Map[String, List[Any]] = {
    val builder = Map.newBuilder[String, List[Any]]

    properties foreach {
      case (key: String, value: BasicDBList) => builder += key -> listFromDBList[Any](value)
      case _ =>
    }

    builder.result
  }

  def getDisplayLabels(ids: Set[String], property: String): Map[String, String] = {
    val objectIds = ids map { id => new ObjectId(id) }
    val result = collection.find(MongoDBObject("_id" -> MongoDBObject("$in" -> objectIds)), MongoDBObject(property -> 1))
    val builder = Map.newBuilder[String, String]

    result foreach {
      o =>
        {
          val id = o.get("_id").toString
          val label = o.getAs[String](property).get

          builder += id -> label
        }
    }

    builder.result
  }

  protected def permissionIds(id: String): MongoDBList = {
    val list = new BasicDBList
    list.add(id)
    list.add(id + ":create")
    list.add(id + ":read")
    list.add(id + ":update")
    list.add(id + ":delete")
    list
  }

  protected def convertAccessLevels(obj: Option[DBObject]): Map[String, String] = {
    val builder = Map.newBuilder[String, String]

    obj match {
      case Some(o) => {
        AuthorizationUtils.validAccessLevels foreach (level => {
          builder += level -> o.getAsOrElse[String](level, "noaccess")
        })
      }
      case _ => {
        AuthorizationUtils.validAccessLevels foreach (level => {
          builder += level -> "noaccess"
        })
      }
    }

    builder.result
  }
}