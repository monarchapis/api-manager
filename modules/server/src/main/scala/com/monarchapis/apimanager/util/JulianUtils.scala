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

package com.monarchapis.apimanager.util

import org.joda.time.DateTimeZone
import org.joda.time.Months
import org.joda.time.DateTimeUtils
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes

object JulianUtils {
  val epoch = new DateTime(0)

  val units = Set("month", "day", "hour", "minute", "second")

  def convertUnitToDate = (unit: Long, timezone: DateTimeZone) =>
    new DateTime(timezone.convertUTCToLocal(unit * 1000))

  val convertUnitToJulianMonth = (unit: Long, timezone: DateTimeZone) => {
    val dateTime = convertUnitToDate(unit, timezone)
    Months.monthsBetween(epoch, dateTime).getMonths.toLong
  }

  val convertDateToJulianMonth = (dateTime: DateTime) =>
    Months.monthsBetween(epoch, dateTime).getMonths.toLong

  val convertUnitToJulianDay = (unit: Long, timezone: DateTimeZone) => {
    val dateTime = convertUnitToDate(unit, timezone)
    Days.daysBetween(epoch, dateTime).getDays.toLong
  }

  val convertDateToJulianDay = (dateTime: DateTime) =>
    Days.daysBetween(epoch, dateTime).getDays.toLong

  val convertUnitToJulianHour = (unit: Long, timezone: DateTimeZone) => {
    val dateTime = convertUnitToDate(unit, timezone)
    Hours.hoursBetween(epoch, dateTime).getHours.toLong
  }

  val convertDateToJulianHour = (dateTime: DateTime) =>
    Hours.hoursBetween(epoch, dateTime).getHours.toLong

  val convertUnitToJulianMinute = (unit: Long, timezone: DateTimeZone) => {
    val dateTime = convertUnitToDate(unit, timezone)
    Minutes.minutesBetween(epoch, dateTime).getMinutes.toLong
  }

  val convertDateToJulianMinute = (dateTime: DateTime) =>
    Minutes.minutesBetween(epoch, dateTime).getMinutes.toLong

  val convertUnitToJulianSecond = (unit: Long, timezone: DateTimeZone) => {
    timezone.convertUTCToLocal(unit * 1000) / 1000
  }

  val convertUnitToJulian = Map(
    "month" -> convertUnitToJulianMonth,
    "day" -> convertUnitToJulianDay,
    "hour" -> convertUnitToJulianHour,
    "minute" -> convertUnitToJulianMinute,
    "second" -> convertUnitToJulianSecond)

  val convertJulianToUnitMonth = (month: Long, timezone: DateTimeZone) => {
    val dateTime = epoch.plusMonths(month.toInt)
    timezone.convertLocalToUTC(dateTime.getMillis, false) / 1000
  }

  val convertJulianToUnitDay = (day: Long, timezone: DateTimeZone) => {
    val ms = day * 86400000L
    timezone.convertLocalToUTC(ms, false) / 1000
  }

  val convertJulianToUnitHour = (hour: Long, timezone: DateTimeZone) => {
    val ms = hour * 3600000L
    timezone.convertLocalToUTC(ms, false) / 1000
  }

  val convertJulianToUnitMinute = (minute: Long, timezone: DateTimeZone) => {
    val ms = minute * 60000L
    timezone.convertLocalToUTC(ms, false) / 1000
  }

  val convertJulianToUnitSecond = (second: Long, timezone: DateTimeZone) => {
    timezone.convertLocalToUTC(second * 1000, false) / 1000
  }

  val convertJulianToUnit = Map(
    "month" -> convertJulianToUnitMonth,
    "day" -> convertJulianToUnitDay,
    "hour" -> convertJulianToUnitHour,
    "minute" -> convertJulianToUnitMinute,
    "second" -> convertJulianToUnitSecond)
}