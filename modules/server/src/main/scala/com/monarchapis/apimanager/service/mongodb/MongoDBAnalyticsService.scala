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

import java.security.MessageDigest
import java.util.Date

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

import org.joda.time.DateTime

import com.fasterxml.jackson.databind.node.ObjectNode
import com.monarchapis.apimanager.analytics.AnalyticsConfiguration
import com.monarchapis.apimanager.analytics.AnalyticsService
import com.monarchapis.apimanager.analytics.Bin
import com.monarchapis.apimanager.analytics.BinValue
import com.monarchapis.apimanager.analytics.DBObjectConvertor
import com.monarchapis.apimanager.analytics.DictionaryStringShortener
import com.monarchapis.apimanager.analytics.DistinctResponse
import com.monarchapis.apimanager.analytics.EventDescriptor
import com.monarchapis.apimanager.analytics.EventProcessorRegistry
import com.monarchapis.apimanager.analytics.EventsResponse
import com.monarchapis.apimanager.analytics.FieldDescriptor
import com.monarchapis.apimanager.analytics.{ FieldType => AnalyticsFieldType }
import com.monarchapis.apimanager.analytics.MetricsResponse
import com.monarchapis.apimanager.analytics.StringShortener
import com.monarchapis.apimanager.analytics.Tier
import com.monarchapis.apimanager.analytics.Tiers
import com.monarchapis.apimanager.analytics.ValueCount
import com.monarchapis.apimanager.analytics.ValueCountsResponse
import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import com.mongodb.casbah.Imports._

import grizzled.slf4j.Logging

object MongoDBAnalyticsService {
  private val EVENT_VALUE_MAP: (String, String) => String = (dateFn: String, metric: String) => {
    s"""
function m() {
    var t = this._t;
	var date = $dateFn;

	emit( date, {
		c : NumberLong(1),
		s : this.$metric
	});
}
"""
  }

  private val EVENT_BOOLEAN_MAP: (String, String) => String = (dateFn: String, metric: String) => {
    s"""
function m() {
    var t = this._t;
	var date = $dateFn;
	var o = {};
	o[this.$metric ? 'Hits' : 'Misses'] = 1;

	emit( date, { counts : o });
}
"""
  }

  private val EVENT_COUNT_MAP: (String, String) => String = (dateFn: String, metric: String) => {
    s"""
function m() {
    var t = this._t;
	var date = $dateFn;
	
	var o = {};
	o['' + this.$metric] = NumberLong(1);

	emit( date, { counts : o });
}
"""
  }

  private val eventMaps = Map( //
    AnalyticsFieldType.STRING -> EVENT_COUNT_MAP,
    AnalyticsFieldType.CODE -> EVENT_COUNT_MAP,
    AnalyticsFieldType.INTEGER -> EVENT_VALUE_MAP,
    AnalyticsFieldType.DECIMAL -> EVENT_VALUE_MAP,
    AnalyticsFieldType.BOOLEAN -> EVENT_BOOLEAN_MAP)

  private val eventValueReduce = s"""
function r(key, values) {
  	var c = 0;
  	var s = 0;

	for (var i = 0; i < values.length; i++) {
  		c += values[i].c;
		s += values[i].s;
	}

	return { c : NumberLong(c), s : s };
}
""".trim

  private val eventCountReduce = s"""
function r(key, values) {
	var reduced = { };

	for (var i = 0; i < values.length; i++) {
  		var counts = values[i].counts;
  
  		for (var key in counts) {
  			reduced[key] = NumberLong(counts[key] + (reduced[key] || 0));
  		}
	}

	return { counts : reduced };
}
""".trim

  private val eventReduces = Map( //
    AnalyticsFieldType.STRING -> eventCountReduce,
    AnalyticsFieldType.CODE -> eventCountReduce,
    AnalyticsFieldType.INTEGER -> eventValueReduce,
    AnalyticsFieldType.DECIMAL -> eventValueReduce,
    AnalyticsFieldType.BOOLEAN -> eventCountReduce)
}

class MongoDBAnalyticsService(
  connectionManager: MultitenantMongoDBConnectionManager,
  configuration: AnalyticsConfiguration) extends AnalyticsService with MongoDBUtils with Logging {

  import AnalyticsService._
  import MongoDBAnalyticsService._

  private val indexedEvents = new scala.collection.mutable.HashSet[String]
  private val indexedMetrics = new scala.collection.mutable.HashSet[String]

  val shorteners = configuration.events map {
    event =>
      (
        event.name,
        new DictionaryStringShortener(
          event.fields map { field => (field.name, field.storeAs) } toMap).asInstanceOf[StringShortener])
  } toMap

  require(connectionManager != null, "connectionManager is required")

  var healthOk = true

  info(s"$this")

  def event = {
    configuration.events map (event => EventDescriptor(
      name = event.name,
      display = event.display,
      timezone = event.timezone))
  }

  def event(eventType: String) = {
    val event = configuration.event(eventType).getOrElse(throw new NotFoundException(s"Could not find event type $eventType"))

    EventDescriptor(
      name = event.name,
      display = event.display,
      timezone = event.timezone,
      fields = Some(event.fields map (field => {
        FieldDescriptor(
          name = field.name,
          display = field.display,
          `type` = field.`type`.toString,
          usage = field.usage.toString,
          required = field.required,
          defaultValue = field.default)
      })),
      indexes = event.indexes)
  }

  def event(eventType: String, data: ObjectNode) {
    val event = configuration.event(eventType).getOrElse(throw new NotFoundException(s"Could not find event type $eventType"))

    event.processors match {
      case Some(processors) => {
        processors foreach (processorRef => {
          val processor = EventProcessorRegistry()(processorRef.name)
            .getOrElse(throw new NotFoundException(s"Could not find processor ${processorRef.name}"))
          processor.process(event, data, processorRef.args: _*)
        })
      }
      case None =>
    }

    val shortener = shorteners(eventType)
    val objectConvertor = new DBObjectConvertor(shortener)

    val conn = connectionManager(EnvironmentContext.current.analyticsDatabase, false)
    val events = conn(eventType + "_events")
    val metrics = conn(eventType + "_metrics")

    val builder = DBObject.newBuilder[String, Int]

    val dateTime = DateTime.now

    val o = DBObject("_t" -> dateTime.toDate)
    objectConvertor(data, o)

    val invalidation = MongoDBObject(
      "$set" -> MongoDBObject(
        "i" -> dateTime.getMillis))

    if (!indexedEvents.contains(eventType)) {
      ensureIndex(events, MongoDBObject("_t" -> 1), "t")

      event.indexes match {
        case Some(indexes) => {
          indexes foreach (index => {
            val builder = MongoDBObject.newBuilder

            index.on.foreach(field => {
              val f = event.field(field).getOrElse(throw new NotFoundException(s"Could not find event field $field"))
              builder += (f.storeAs -> 1)
            })

            builder += "_t" -> 1

            ensureIndex(events, builder.result, index.name, unique = index.unique)
          })
        }
        case None =>
      }

      indexedEvents += eventType
    }

    events.insert(o, WriteConcern.Normal)

    var tier: Tier = Tiers.largest

    val arrayBuffer = new ArrayBuffer[MongoDBObject]

    while (tier != null) {
      arrayBuffer += MongoDBObject(
        "_id.k" -> tier.key,
        "_id.t" -> MongoDBObject(
          "$gte" -> tier.floor(dateTime).toDate,
          "$lt" -> tier.ceil(dateTime).toDate))

      tier = tier.next.orNull
    }

    metrics.update(MongoDBObject("$or" -> arrayBuffer.toArray), invalidation, multi = true)
  }

  def events(eventType: String, startIn: DateTime, endIn: DateTime = DateTime.now, query: Option[String] = None) = {
    val event = configuration.event(eventType).getOrElse(throw new NotFoundException(s"Could not find event type $eventType"))
    val shortener = shorteners(eventType)
    val objectConvertor = new DBObjectConvertor(shortener)

    val start = startIn.toDateTime(event.dateTimeZone)
    val end = endIn.toDateTime(event.dateTimeZone)

    val conn = connectionManager(EnvironmentContext.current.analyticsDatabase, false)
    val coll = conn(eventType + "_events")
    val q = DBObject("_t" -> DBObject("$gte" -> start.toDate, "$lt" -> end.toDate))

    query match {
      case Some(query) => {
        val queryConverter = new UriEventQueryConverter(shortener)
        queryConverter(query, q)
      }
      case _ =>
    }

    val cursor = coll.find(q)

    val ret = cursor map {
      o => objectConvertor.unapply(o)
    } toList

    EventsResponse(
      start,
      end,
      event.dateTimeZone.getOffset(0),
      query,
      ret.size,
      ret)
  }

  def metrics(
    eventType: String,
    metric: String,
    tierKey: String,
    startIn: DateTime,
    endIn: DateTime = DateTime.now,
    query: Option[String] = None,
    fillGaps: Boolean = false,
    refreshing: Boolean = false,
    limit: Integer = DEFAULT_SAMPLE_LIMIT) = {
    val (start, end, offset, fieldType, unit, bins, labels) = metricSeries( //
      eventType, //
      metric, //
      tierKey, //
      startIn, //
      endIn, //
      query, //
      fillGaps, //
      refreshing, //
      limit)

    MetricsResponse(
      start,
      end,
      offset,
      query,
      fieldType,
      unit,
      bins.size,
      bins,
      labels)
  }

  def metricSeries(
    eventType: String,
    metric: String,
    tierKey: String,
    startIn: DateTime,
    endIn: DateTime = DateTime.now,
    query: Option[String] = None,
    fillGaps: Boolean = false,
    refreshing: Boolean = false,
    limit: Integer = DEFAULT_SAMPLE_LIMIT): (DateTime, DateTime, Int, String, String, List[Bin], Option[Map[String, String]]) = {
    val maxInvalidationTs = System.currentTimeMillis
    var tier = Tiers.tiers.getOrElse(tierKey, throw new NotFoundException(s"Unknown tier $tierKey"))
    var event = configuration.event(eventType).getOrElse(throw new NotFoundException(s"Could not find event type $eventType"))

    var start: DateTime = null
    var end: DateTime = null

    var samples: Int = 0

    do {
      start = tier.floor(startIn.toDateTime(event.dateTimeZone))
      end = tier.ceil(endIn.toDateTime(event.dateTimeZone))
      samples = tier.samples(start, end)

      if (samples > limit) {
        tier = Tiers.prevs.getOrElse(tier.key, throw new BadRequestException(s"Too many samples were requested.  The limit is $limit.  You requested $samples"))
        event = configuration.event(eventType).getOrElse(throw new NotFoundException(s"Could not find event type $eventType"))
      }
    } while (samples > limit)

    if (refreshing) {
      val max = tier.floor(DateTime.now)

      if (end.isAfter(max)) {
        end = max
      }
    }

    val metricField = event.field(metric).getOrElse(throw new NotFoundException(s"Could not find metric $metric"))
    val eventMap = eventMaps.getOrElse(metricField.`type`, throw new NotFoundException(s"Could not find event map function for $metric"))
    val eventReduce = eventReduces.getOrElse(metricField.`type`, throw new NotFoundException(s"Could not find event reduce function for $metric"))
    val shortener = shorteners(eventType)
    val objectConvertor = new DBObjectConvertor(shortener)

    val conn = connectionManager(EnvironmentContext.current.analyticsDatabase, false)
    val events = conn(eventType + "_events")
    val metrics = conn(eventType + "_metrics")

    if (!indexedMetrics.contains(eventType)) {
      ensureIndex(metrics, MongoDBObject("i" -> 1, "_id.e" -> 1, "_id.k" -> 1, "_id.t" -> 1), "ekt")
      ensureIndex(metrics, MongoDBObject("i" -> 1, "_id.k" -> 1, "_id.t" -> 1), "kt")

      indexedMetrics += eventType
    }

    val expressionFilter = query match {
      case Some(query) => {
        val queryConverter = new UriEventQueryConverter(shortener)
        queryConverter(query)
      }
      case _ => MongoDBObject()
    }

    val queryHash = query match {
      case Some(query) => {
        val digest = MessageDigest.getInstance("MD5")
        appendDigest(digest, expressionFilter)
        digest.digest.foldLeft("")((s: String, b: Byte) => s +
          Character.forDigit((b & 0xf0) >> 4, 16) +
          Character.forDigit(b & 0x0f, 16))
      }
      case _ => "*"
    }

    val expression = metricField.storeAs + ":" + queryHash

    val insert = (tier: Tier, gaps: List[Bin]) => {
      if (tier.stored) {
        gaps.foreach(gap => {
          val date = new Date(gap.time.getMillis)

          val q = MongoDBObject(
            "$or" -> Array(
              MongoDBObject("i" -> MongoDBObject("$lte" -> maxInvalidationTs)),
              MongoDBObject("i" -> MongoDBObject("$exists" -> false))),
            "_id.e" -> expression,
            "_id.k" -> tier.key,
            "_id.t" -> date)

          val u = MongoDBObject(
            "_id" -> MongoDBObject(
              "e" -> expression,
              "k" -> tier.key,
              "t" -> date))

          if (gap.value.isDefined) {
            val value = gap.value.get
            u.put("value", MongoDBObject(
              "c" -> value.count,
              "s" -> value.sum.toDouble))
          }

          if (gap.counts.isDefined) {
            val counts = gap.counts.get
            val o = MongoDBObject()

            counts foreach {
              case (key, value) => {
                o.put(key, value)
              }
            }

            u.put("counts", counts)
          }

          metrics.update(
            q, //
            u, //
            upsert = true, //
            multi = false, //
            concern = WriteConcern.Normal)
        })
      }
    }

    val computeFlat = (tier: Tier, start: DateTime, end: DateTime) => {
      val builder = List.newBuilder[Bin]

      val query = MongoDBObject.newBuilder
      query += "_t" -> DBObject("$gte" -> start.toDate, "$lt" -> end.toDate)
      query ++= expressionFilter

      val q = query.result

      val result = events.mapReduce( //
        eventMap(tier.jsFloor, metricField.storeAs), //
        eventReduce, //
        MapReduceInlineOutput, //
        Some(q), //
        Some(DBObject("_t" -> 1))).foreach(o => {
          val timestamp = new DateTime(o.getAs[Date]("_id").get)

          val container = o.getAs[DBObject]("value").get

          val value =
            if (container.containsField("c") && container.containsField("s")) {
              val count = container.getAs[Long]("c").get
              val sum = container.getAs[Double]("s").get
              Some(BinValue(count, sum))
            } else {
              None
            }

          val counts =
            if (container.containsField("counts")) {
              val counts = container.getAs[DBObject]("counts").get
              val builder = Map.newBuilder[String, Long]

              counts.keys foreach { key =>
                {
                  builder += key -> counts.getAs[Long](key).get
                }
              }

              Some(builder.result)
            } else {
              None
            }

          builder += Bin(timestamp, value, counts)
        })

      builder.result
    }

    var self: (Tier, DateTime, DateTime) => List[Bin] = null

    val computePyramidal: (Tier, DateTime, DateTime, Boolean) => List[Bin] = (tier: Tier, start: DateTime, end: DateTime, fillGaps: Boolean) => {
      val query = MongoDBObject(
        "i" -> DBObject("$exists" -> false),
        "_id.e" -> expression,
        "_id.k" -> tier.key,
        "_id.t" -> DBObject("$gte" -> start.toDate, "$lt" -> end.toDate))

      val builder = List.newBuilder[Bin]
      var nextExpected = start

      metrics.find(query).foreach(o => {
        val id = o.getAs[DBObject]("_id").get
        val timestamp = new DateTime(id.getAs[Date]("t").get)

        val value =
          if (o.containsField("value")) {
            val value = o.getAs[DBObject]("value").get
            val count = value.getAs[Long]("c").get
            val sum = value.getAs[Double]("s").get
            Some(BinValue(count, sum))
          } else {
            None
          }

        val counts =
          if (o.containsField("counts")) {
            val counts = o.getAs[DBObject]("counts").get
            val builder = Map.newBuilder[String, Long]

            counts.keys foreach { key =>
              {
                builder += key -> counts.getAs[Long](key).get
              }
            }

            Some(builder.result)
          } else {
            None
          }

        if (nextExpected.isBefore(timestamp)) {
          val gaps = self(tier, nextExpected, timestamp)
          val reduced = reduce(tier, gaps)
          insert(tier, reduced)
          builder ++= (if (fillGaps) insertGaps(tier, reduced, nextExpected, timestamp) else reduced)
        }

        builder += Bin(timestamp, value, counts)

        nextExpected = tier.ceil(timestamp)
      })

      if (nextExpected.isBefore(end)) {
        val gaps = self(tier, nextExpected, end)
        val reduced = reduce(tier, gaps)
        insert(tier, reduced)
        builder ++= (if (fillGaps) insertGaps(tier, reduced, nextExpected, end) else reduced)
      }

      builder.result
    }

    val compute: (Tier, DateTime, DateTime) => List[Bin] = (tier: Tier, start: DateTime, end: DateTime) => {
      tier.next match {
        case Some(next) => {
          computePyramidal(next, start, end, false)
        }
        case None => {
          computeFlat(tier, start, end)
        }
      }
    }

    self = compute

    val bins = computePyramidal(tier, start, end, fillGaps)

    val labels = if (metricField.`type` == AnalyticsFieldType.STRING) {
      metricField.refersTo match {
        case Some(refersTo) => {
          DisplayLabelSources.lookup.get(refersTo) match {
            case Some(lookup) => {
              val builder = Set.newBuilder[String]

              bins.foreach(bin => {
                bin.counts.foreach(count => {
                  builder ++= count.keySet filter (v => v.length > 0)
                })
              })

              Some(lookup.getDisplayLabels(builder.result))
            }
            case None => {
              warn(s"Invalid reference $refersTo")
              None
            }
          }
        }
        case None => None
      }
    } else {
      None
    }

    ( //
      start, //
      end, //
      event.dateTimeZone.getOffset(0), //
      metricField.`type`.toString,
      tier.key,
      bins,
      labels)
  }

  def counts(
    eventType: String,
    metric: String,
    tierKey: String,
    startIn: DateTime,
    endIn: DateTime = DateTime.now,
    query: Option[String] = None,
    limit: Option[Int] = None) = {
    val event = configuration.event(eventType).getOrElse(throw new NotFoundException(s"Could not find event type $eventType"))
    val metricField = event.field(metric).getOrElse(throw new NotFoundException(s"Could not find field $metric"))

    if (metricField.`type` != AnalyticsFieldType.STRING) {
      throw new BadRequestException("The metric requested must be a string type")
    }

    val (start, end, offset, fieldType, unit, bins, labels) = metricSeries( //
      eventType, //
      metric, //
      tierKey, //
      startIn, //
      endIn, //
      query, //
      false)

    val countMap = new scala.collection.mutable.HashMap[String, Long]

    bins.foreach(bin => {
      bin.counts.get foreach {
        case (key, value) => {
          countMap(key) = countMap.getOrElse(key, 0L) + value
        }
      }
    })

    val valuesList = List.newBuilder[ValueCount]

    countMap foreach {
      case (key, value) => valuesList += ValueCount(key, value)
    }

    var sortedItems = valuesList.result.sortWith(_.count > _.count)

    if (limit.isDefined && sortedItems.size > limit.get) {
      val reduce = sortedItems.slice(limit.get - 1, limit.size)
      var count = 0L
      reduce.foreach(vc => count += vc.count)

      val builder = List.newBuilder[ValueCount]
      builder ++= sortedItems.slice(0, limit.get - 1)
      builder += ValueCount("other", count)
      sortedItems = builder.result
    }

    ValueCountsResponse(
      start,
      end,
      offset,
      query,
      sortedItems,
      labels)
  }

  def distinct(
    eventType: String,
    metric: String,
    startIn: DateTime,
    endIn: DateTime = DateTime.now,
    query: Option[String] = None) = {
    val event = configuration.event(eventType).getOrElse(throw new NotFoundException(s"Could not find event type $eventType"))
    val metricField = event.field(metric).getOrElse(throw new NotFoundException(s"Could not find field $metric"))

    val start = startIn.toDateTime(event.dateTimeZone)
    val end = endIn.toDateTime(event.dateTimeZone)

    val conn = connectionManager(EnvironmentContext.current.analyticsDatabase, false)
    val events = conn(eventType + "_events")
    val shortener = shorteners(eventType)

    val expressionFilter = query match {
      case Some(query) => {
        val queryConverter = new UriEventQueryConverter(shortener)
        queryConverter(query)
      }
      case _ => MongoDBObject()
    }

    val values = events.distinct(metricField.storeAs, expressionFilter).toList.sortWith((a, b) => {
      if (a.isInstanceOf[String] && b.isInstanceOf[String]) {
        a.asInstanceOf[String].toLowerCase < b.asInstanceOf[String].toLowerCase
      } else if (a.isInstanceOf[Number] && b.isInstanceOf[Number]) {
        a.asInstanceOf[Number].doubleValue < b.asInstanceOf[Number].doubleValue
      } else {
        true
      }
    })

    val labels = if (metricField.`type` == AnalyticsFieldType.STRING) {
      metricField.refersTo match {
        case Some(refersTo) => {
          DisplayLabelSources.lookup.get(refersTo) match {
            case Some(lookup) => {
              val builder = Set.newBuilder[String]

              values.foreach(value => {
                if (value.isInstanceOf[String]) {
                  builder += value.asInstanceOf[String]
                }
              })

              Some(lookup.getDisplayLabels(builder.result))
            }
            case None => {
              warn(s"Invalid reference $refersTo")
              None
            }
          }
        }
        case None => None
      }
    } else {
      None
    }

    DistinctResponse(
      start,
      end,
      event.dateTimeZone.getOffset(0),
      query,
      values,
      labels)
  }

  private def reduce(tier: Tier, gaps: List[Bin]) = {
    val builder = List.newBuilder[Bin]

    var last: Bin = null

    gaps.foreach(gap => {
      val floored = Bin(
        tier.floor(gap.time),
        gap.value,
        gap.counts)

      if (last != null) {
        if (last.time.equals(floored.time)) {
          last = Bin(
            last.time,
            combineValues(last.value, floored.value),
            combineCounts(last.counts, floored.counts)) //last.count + floored.count, last.sum + floored.sum)
        } else {
          builder += last
          last = floored
        }
      } else {
        last = floored
      }
    })

    if (last != null) {
      builder += last
    }

    builder.result
  }

  private def insertGaps(tier: Tier, reduced: List[Bin], start: DateTime, end: DateTime) = {
    val builder = List.newBuilder[Bin]
    var nextExpected = start

    reduced.foreach(bin => {
      while (nextExpected.isBefore(bin.time)) {
        builder += Bin(nextExpected)
        nextExpected = tier.ceil(nextExpected)
      }

      builder += bin

      nextExpected = tier.ceil(bin.time)
    })

    while (nextExpected.isBefore(end)) {
      builder += Bin(nextExpected)
      nextExpected = tier.ceil(nextExpected)
    }

    builder.result
  }

  private def combineValues(first: Option[BinValue], second: Option[BinValue]) = {
    if (first.isDefined && second.isDefined) {
      val one = first.get
      val two = second.get
      Some(BinValue(one.count + two.count, one.sum + two.sum))
    } else {
      None
    }
  }

  private def combineCounts(first: Option[Map[String, Long]], second: Option[Map[String, Long]]) = {
    if (first.isDefined && second.isDefined) {
      val one = first.get
      val two = second.get

      val keys = one.keySet ++ two.keySet
      val builder = Map.newBuilder[String, Long]

      keys foreach { key =>
        {
          builder += key -> (one.getOrElse(key, 0L) + two.getOrElse(key, 0L))
        }
      }

      Some(builder.result)
    } else {
      None
    }
  }

  private def appendDigest(digest: MessageDigest, dbo: DBObject) {
    val sortedKeys = dbo.keySet.toSeq.sorted

    sortedKeys.foreach(key => {
      val value = dbo.get(key)
      digest.update(key.getBytes)
      digest.update('='.toByte)

      value match {
        case o: DBObject => {
          digest.update('{'.toByte)
          appendDigest(digest, o)
          digest.update('}'.toByte)
        }
        case _ => digest.update(String.valueOf(value).getBytes)
      }
    })
  }

  override def toString = s"MongoDBAnalyticsService($connectionManager)"
}
