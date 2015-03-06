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

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar

class JulianUtilsTest extends FlatSpec with Matchers with MockitoSugar {

  behavior of "JulianUtils"

  it should "convert months to julian" in {
    val timezone = DateTimeZone.forID("America/New_York")
    val date = DateTime.parse("2014-01-27T23:00:00-05:00")
    val unit = date.getMillis / 1000
    val month = JulianUtils.convertUnitToJulianMonth(unit, timezone)
    month should equal(528)
    val out = JulianUtils.convertJulianToUnitMonth(month, timezone)
    val dout = DateTime.now.withMillis(out * 1000)
    dout.toString() should equal("2014-01-01T00:00:00.000-05:00")
  }

  it should "convert days to julian" in {
    val timezone = DateTimeZone.forID("America/New_York")
    val date = DateTime.parse("2014-01-27T23:00:00-05:00")
    val unit = date.getMillis / 1000
    val month = JulianUtils.convertUnitToJulianDay(unit, timezone)
    month should equal(16097)
    val out = JulianUtils.convertJulianToUnitDay(month, timezone)
    val dout = DateTime.now.withMillis(out * 1000)
    dout.toString() should equal("2014-01-27T00:00:00.000-05:00")
  }

  it should "convert hours to julian" in {
    val timezone = DateTimeZone.forID("America/New_York")
    val date = DateTime.parse("2014-01-27T23:45:22-05:00")
    val unit = date.getMillis / 1000
    val month = JulianUtils.convertUnitToJulianHour(unit, timezone)
    month should equal(386351)
    val out = JulianUtils.convertJulianToUnitHour(month, timezone)
    val dout = DateTime.now.withMillis(out * 1000)
    dout.toString() should equal("2014-01-27T23:00:00.000-05:00")
  }

  it should "convert minutes to julian" in {
    val timezone = DateTimeZone.forID("America/New_York")
    val date = DateTime.parse("2014-01-27T23:45:22-05:00")
    val unit = date.getMillis / 1000
    val month = JulianUtils.convertUnitToJulianMinute(unit, timezone)
    month should equal(23181105)
    val out = JulianUtils.convertJulianToUnitMinute(month, timezone)
    val dout = DateTime.now.withMillis(out * 1000)
    dout.toString() should equal("2014-01-27T23:45:00.000-05:00")
  }

  it should "convert seconds to julian" in {
    val timezone = DateTimeZone.forID("America/New_York")
    val date = DateTime.parse("2014-01-27T23:45:22.555-05:00")
    val unit = date.getMillis / 1000
    val month = JulianUtils.convertUnitToJulianSecond(unit, timezone)
    month should equal(1390866322L)
    val out = JulianUtils.convertJulianToUnitSecond(month, timezone)
    val dout = DateTime.now.withMillis(out * 1000)
    dout.toString() should equal("2014-01-27T23:45:22.000-05:00")
  }
}