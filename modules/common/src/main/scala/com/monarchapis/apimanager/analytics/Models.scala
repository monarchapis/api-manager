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

package com.monarchapis.apimanager.analytics

import scala.beans.BeanProperty
import scala.beans.BooleanBeanProperty
import scala.util.matching.Regex
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.text.WordUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import org.joda.time.Months
import org.joda.time.Seconds
import org.joda.time.Weeks
import org.joda.time.Years
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

trait PropertyValidation {
  @transient protected val nameRegex = "([a-zA-Z\\_\\-]+)".r
  @transient protected val typeRegex = "(string|boolean|int|decimal|object|array|code)".r

  protected def validate(name: String, regex: Regex): Boolean = regex.findFirstIn(name).isDefined
}

case class AnalyticsConfiguration(
  @BeanProperty val events: List[EventType]) {
  private val lookup = events map { item => (item.name, item) } toMap

  def event(name: String) = lookup.get(name)
}

case class EventType(
  @BeanProperty val name: String,
  @JsonProperty("display") private val _display: Option[String],
  @BeanProperty val timezone: String,
  @BeanProperty val fields: List[Field],
  @BeanProperty val processors: Option[List[Processor]],
  @BeanProperty val indexes: Option[List[Index]]) extends PropertyValidation {
  require(validate(name, nameRegex), s"Invalid name format for $name")
  require(timezone != null, "Timezone is required")

  lazy val display = _display.getOrElse(WordUtils.capitalize(StringUtils.replaceChars(name, '_', ' ')))

  val dateTimeZone = if ("default" == timezone) DateTimeZone.getDefault else DateTimeZone.forID(timezone)

  private val fieldLookup = fields map { item => (item.name, item) } toMap
  val requiredFields = fields filter { item => item.required }

  private val processorLookup = processors.getOrElse(List.empty[Processor]) map {
    item => (item.name, item)
  } toMap

  def field(name: String) = fieldLookup.get(name)
  def processor(name: String) = processorLookup.get(name)
}

case class EventDescriptor(
  @BeanProperty val name: String,
  @BeanProperty val display: String,
  @BeanProperty val timezone: String,
  @JsonInclude(Include.NON_NULL)@BeanProperty val fields: Option[List[FieldDescriptor]] = None,
  @JsonInclude(Include.NON_NULL)@BeanProperty val indexes: Option[List[Index]] = None)

object FieldType extends Enumeration {
  case class EValue(name: String) extends Val(name) {}

  val STRING: Value = EValue("string")
  val BOOLEAN: Value = EValue("boolean")
  val INTEGER: Value = EValue("integer")
  val DECIMAL: Value = EValue("decimal")
  val OBJECT: Value = EValue("object")
  val ARRAY: Value = EValue("array")
  val CODE: Value = EValue("code")

  def unapply(s: String): Value = values.find(s == _.toString).getOrElse(throw new IllegalArgumentException(s"Invalid value $s"))
}

object UsageType extends Enumeration {
  case class EValue(name: String) extends Val(name) {}

  val DIMENSION: Value = EValue("dimension")
  val MEASURE: Value = EValue("measure")

  def unapply(s: String): Value = values.find(s == _.toString).getOrElse(throw new IllegalArgumentException(s"Invalid value $s"))
}

case class Field(
  @BeanProperty val name: String,
  @JsonProperty("display") private val _display: Option[String],
  @BeanProperty val storeAs: String,
  @JsonProperty("type") private val _type: String,
  @JsonProperty("usage") private val _usage: Option[String],
  @BooleanBeanProperty val required: Boolean,
  @BeanProperty val refersTo: Option[String],
  @JsonInclude(Include.NON_NULL)@BeanProperty val default: Option[String] = None) extends PropertyValidation {
  require(validate(name, nameRegex), s"Invalid name format for $name")
  require(validate(storeAs, nameRegex), s"Invalid storeAs format for $storeAs")
  require(validate(_type, typeRegex), s"Invalid type ${_type}.  Should be one of string, boolean, int, decimal, object, array, code")

  @BeanProperty val `type`: FieldType.Value = FieldType.unapply(_type)
  @BeanProperty val usage: UsageType.Value = UsageType.unapply(_usage.getOrElse("dimension"))

  lazy val display = _display.getOrElse(WordUtils.capitalize(StringUtils.replaceChars(name, '_', ' ')))

  if (default.isDefined)
    `type` match {
      case FieldType.OBJECT | FieldType.ARRAY => throw new IllegalArgumentException("Default values are not allowed for object or array field types")
      case _ =>
    }
}

case class FieldDescriptor(
  @BeanProperty val name: String,
  @BeanProperty val display: String,
  @BeanProperty val `type`: String,
  @BeanProperty val usage: String,
  @BooleanBeanProperty val required: Boolean,
  @JsonInclude(Include.NON_NULL)@BeanProperty val default: Option[String] = None)

case class Processor(
  @BeanProperty val name: String,
  @BeanProperty val args: Seq[String] = Seq()) extends PropertyValidation {
  require(validate(name, nameRegex), s"Invalid name format for $name")
}

case class Index(
  @BeanProperty val name: String,
  @BeanProperty val on: List[String],
  @BooleanBeanProperty val unique: Boolean = false) extends PropertyValidation {
  on foreach { name => require(validate(name, nameRegex), s"Invalid name format for $name") }
}

trait Tier {
  val key: String
  val stored: Boolean
  def jsFloor: String
  def floor(dateTime: DateTime): DateTime
  def ceil(dateTime: DateTime): DateTime = {
    floor(step(dateTime))
  }
  def step(dateTime: DateTime): DateTime
  def next: Option[Tier] = None
  def size(dateTime: DateTime): Option[Int] = None
  def samples(start: DateTime, end: DateTime): Int
}

trait SecondsBasedTier extends Tier {
  def jsFloor = s"new Date(Math.floor(t.getTime() / $millis) * $millis)"
  protected val millis: Long
  def floor(dateTime: DateTime) = //
    new DateTime((dateTime.getMillis / millis) * millis)
  def step(dateTime: DateTime) = dateTime.plus(millis)
}

object Millis {
  val SECOND = 1000L
  val SECOND10 = SECOND * 10L // = 10000
  val MINUTE = SECOND * 60L // = 60000
  val MINUTE5 = MINUTE * 5L // = 300000
  val MINUTE15 = MINUTE * 15L // = 900000
  val MINUTE30 = MINUTE * 30L // = 1800000
  val HOUR = MINUTE * 60L // = 3600000
  val DAY = HOUR * 24L // = 86400000
}

object Tiers {
  val SECOND = new SecondsBasedTier {
    val key = "second"
    val stored = true
    protected val millis = Millis.SECOND
    def samples(start: DateTime, end: DateTime) =
      Seconds.secondsBetween(start, end).getSeconds
  }

  val SECOND10 = new SecondsBasedTier {
    val key = "second10"
    val stored = true
    protected val millis = Millis.SECOND10
    def samples(start: DateTime, end: DateTime) =
      Seconds.secondsBetween(start, end).getSeconds / 10
  }

  val MINUTE = new SecondsBasedTier {
    val key = "minute"
    val stored = true
    protected val millis = Millis.MINUTE
    def samples(start: DateTime, end: DateTime) =
      Minutes.minutesBetween(start, end).getMinutes
  }

  val MINUTE5 = new SecondsBasedTier {
    val key = "minute5"
    val stored = true
    protected val millis = Millis.MINUTE5
    def samples(start: DateTime, end: DateTime) =
      Minutes.minutesBetween(start, end).getMinutes / 5
  }

  val MINUTE15 = new SecondsBasedTier {
    val key = "minute15"
    val stored = false
    protected val millis = Millis.MINUTE15
    override val next = Some(MINUTE5)
    override def size(dateTime: DateTime) = Some(3)
    def samples(start: DateTime, end: DateTime) =
      Minutes.minutesBetween(start, end).getMinutes / 15
  }

  val MINUTE30 = new SecondsBasedTier {
    val key = "minute30"
    val stored = false
    protected val millis = Millis.MINUTE30
    override val next = Some(MINUTE5)
    override def size(dateTime: DateTime) = Some(6)
    def samples(start: DateTime, end: DateTime) =
      Minutes.minutesBetween(start, end).getMinutes / 30
  }

  val HOUR = new SecondsBasedTier {
    val key = "hour"
    val stored = true
    protected val millis = Millis.HOUR
    override val next = Some(MINUTE5)
    override def size(dateTime: DateTime) = Some(12)
    def samples(start: DateTime, end: DateTime) =
      Hours.hoursBetween(start, end).getHours
  }

  val DAY = new Tier {
    val key = "day"
    val stored = true
    def jsFloor = throw new IllegalStateException("JavaScript floor function not available for day")
    override val next = Some(HOUR)
    def floor(dateTime: DateTime) = dateTime.withTimeAtStartOfDay()
    def step(dateTime: DateTime) = dateTime.plusDays(1)
    override def size(dateTime: DateTime) = Some(24)
    def samples(start: DateTime, end: DateTime) =
      Days.daysBetween(start, end).getDays
  }

  val WEEK = new Tier {
    val key = "week"
    val stored = false
    def jsFloor = throw new IllegalStateException("JavaScript floor function not available for week")
    def floor(dateTime: DateTime) = //
      dateTime.minusDays(6).withDayOfWeek(DateTimeConstants.SUNDAY).withTimeAtStartOfDay()
    def step(dateTime: DateTime) = dateTime.plusDays(7)
    override val next = Some(DAY)
    override def size(dateTime: DateTime) = Some(7)
    def samples(start: DateTime, end: DateTime) =
      Weeks.weeksBetween(start, end).getWeeks
  }

  val MONTH = new Tier {
    val key = "month"
    val stored = false
    def jsFloor = throw new IllegalStateException("JavaScript floor function not available for month")
    def floor(dateTime: DateTime) = dateTime.withDayOfMonth(1).withTimeAtStartOfDay()
    def step(dateTime: DateTime) = dateTime.plusMonths(1)
    override val next = Some(DAY)
    override def size(dateTime: DateTime) = Some(dateTime.dayOfMonth.getMaximumValue)
    def samples(start: DateTime, end: DateTime) =
      Months.monthsBetween(start, end).getMonths
  }

  val YEAR = new Tier {
    val key = "year"
    val stored = false
    def jsFloor = throw new IllegalStateException("JavaScript floor function not available for year")
    def floor(dateTime: DateTime) = //
      dateTime.withMonthOfYear(DateTimeConstants.JANUARY).withDayOfMonth(1).withTimeAtStartOfDay()
    def step(dateTime: DateTime) = dateTime.plusYears(1)
    override val next = Some(DAY)
    override def size(dateTime: DateTime) = Some(dateTime.dayOfYear.getMaximumValue)
    def samples(start: DateTime, end: DateTime) =
      Years.yearsBetween(start, end).getYears
  }

  val tiers = Map(
    "second" -> SECOND,
    "second10" -> SECOND10,
    "minute" -> MINUTE,
    "minute5" -> MINUTE5,
    "minute15" -> MINUTE15,
    "minute30" -> MINUTE30,
    "hour" -> HOUR,
    "day" -> DAY,
    "week" -> WEEK,
    "month" -> MONTH,
    "year" -> YEAR)

  val prevs = Map(
    "second" -> SECOND10,
    "second10" -> MINUTE,
    "minute" -> MINUTE5,
    "minute5" -> MINUTE15,
    "minute15" -> MINUTE30,
    "minute30" -> HOUR,
    "hour" -> DAY,
    "day" -> WEEK,
    "week" -> MONTH,
    "month" -> YEAR)

  val largest = YEAR
}

case class BinValue(
  val count: Long,
  val sum: Double)

case class Bin(
  val time: DateTime,
  @JsonInclude(Include.NON_NULL) val value: Option[BinValue] = None,
  @JsonInclude(Include.NON_NULL) val counts: Option[Map[String, Long]] = None)

case class EventsResponse(
  @BeanProperty start: DateTime,
  @BeanProperty end: DateTime,
  @BeanProperty timezoneOffset: Int,
  @BeanProperty query: Option[String],
  @BeanProperty samples: Long,
  @BeanProperty events: List[JsonNode])

case class MetricsResponse(
  @BeanProperty start: DateTime,
  @BeanProperty end: DateTime,
  @BeanProperty timezoneOffset: Int,
  @BeanProperty query: Option[String],
  @BeanProperty fieldType: String,
  @BeanProperty unit: String,
  @BeanProperty samples: Long,
  @BeanProperty data: List[Bin],
  @JsonInclude(Include.NON_NULL)@BeanProperty labels: Option[Map[String, String]])

case class ValueCount(
  val value: String,
  val count: Long)

case class ValueCountsResponse(
  @BeanProperty start: DateTime,
  @BeanProperty end: DateTime,
  @BeanProperty timezoneOffset: Int,
  @BeanProperty query: Option[String],
  @BeanProperty data: List[ValueCount],
  @JsonInclude(Include.NON_NULL)@BeanProperty labels: Option[Map[String, String]])

case class DistinctResponse(
  @BeanProperty start: DateTime,
  @BeanProperty end: DateTime,
  @BeanProperty timezoneOffset: Int,
  @BeanProperty query: Option[String],
  @BeanProperty values: List[Any],
  @JsonInclude(Include.NON_NULL)@BeanProperty labels: Option[Map[String, String]])
  
  