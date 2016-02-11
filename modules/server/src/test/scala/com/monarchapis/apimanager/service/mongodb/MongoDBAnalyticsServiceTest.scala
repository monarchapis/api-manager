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
import org.joda.time.DateTimeZone
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.FileSystemResource
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.monarchapis.apimanager.analytics.AnalyticsConfigurationFactory
import com.monarchapis.apimanager.analytics.DictionaryStringShortener
import com.monarchapis.apimanager.analytics.JsonCompressor
import com.monarchapis.apimanager.analytics.JsonExpander
import com.monarchapis.apimanager.analytics.Tiers
import com.monarchapis.apimanager.analytics.EventProcessorRegistry
import com.monarchapis.apimanager.analytics.EventValidator
import com.monarchapis.apimanager.analytics.maxmind.MaxMindGeoIP2DatabaseReaderFactory
import com.monarchapis.apimanager.analytics.maxmind.MaxMindGeoIP2EventProcessor
import com.monarchapis.apimanager.model.EnvironmentContext
import com.monarchapis.apimanager.service.DisplayLabel
import com.monarchapis.apimanager.service.DisplayLabelSources
import javax.inject.Named
import java.util.Collections
import org.springframework.core.io.Resource

class MongoDBAnalyticsServiceTest extends FlatSpec with Matchers {
  behavior of "MongoDBAnalyticsService"

  val input = """
name        : traffic
timezone	: America/New_York
fields      :
  - 
    name        : application_id
    storeAs     : aid
    type        : string
    required    : true
    refersTo    : applications
  -
    name        : client_id
    storeAs     : cid
    type        : string
    required    : true
  -
    name        : service_id
    storeAs     : sid
    type        : string
    required    : true
  -
    name        : service_version
    storeAs     : ver
    type        : string
    required    : true
  -
    name        : operation_name
    storeAs     : oper
    type        : string
    required    : true
  -
    name        : provider_id
    storeAs     : pid
    type        : string
    required    : true
  -
    name        : request_size
    storeAs     : rqs
    type        : integer
    required    : true
  -
    name        : response_size
    storeAs     : rss
    type        : integer
    required    : true
  -
    name        : response_time
    storeAs     : rst
    type        : integer
    required    : true
  -
    name        : status_code
    storeAs     : sc
    type        : code
    required    : true
  -
    name        : error_reason
    storeAs     : er
    type        : string
    required    : true
  -
    name        : cache_hit
    storeAs     : ch
    type        : boolean
    required    : true
    default     : false
  -
    name        : token_id
    storeAs     : tid
    type        : string
    required    : false
  -
    name        : user_id
    storeAs     : uid
    type        : string
    required    : false
  -
    name        : host
    storeAs     : host
    type        : string
    required    : true
  -
    name        : path
    storeAs     : path
    type        : string
    required    : true
  -
    name        : port
    storeAs     : port
    type        : code
    required    : true
  -
    name        : verb
    storeAs     : verb
    type        : string
    required    : true
  -
    name        : parameters
    storeAs     : pars
    type        : object
    required    : false
  -
    name        : headers
    storeAs     : hdrs
    type        : object
    required    : false
  -
    name        : client_ip
    storeAs     : cip
    type        : string
    required    : true
  -
    name        : user_agent
    storeAs     : ua
    type        : string
    required    : false
  -
    name        : latitude
    storeAs     : lat
    type        : decimal
    required    : false
  -
    name        : longitude
    storeAs     : lon
    type        : decimal
    required    : false

processors  :
  -
    name		: Validator
  -
    name        : MaxMind-GeoIP2
    args        :
      - client_ip

indexes     :
  -
    name        : by_application
    on          :
      - application_id
      - service_id
      - operation_name
  -
    name        : by_client
    on          :
      - client_id
      - service_id
      - operation_name
"""

  val configFiles = Collections.singletonList(new ByteArrayResource(input.getBytes).asInstanceOf[Resource])

  val configuration =
    new AnalyticsConfigurationFactory(configFiles).getObject

  val mapper = new ObjectMapper

  val eventJson = """
    |{
    |	"application_id" : "1",
    |	"client_id" : "2",
    |	"service_id" : "3",
    |	"service_version" : "v1",
    |	"operation_name" : "greetings",
    |	"provider_id" : "4",
    |	"request_size" : 1000,
    |	"response_size" : 5000,
    |	"response_time" : 25,
    |	"status_code" : 200,
    |	"error_reason" : "ok",
    |	"cache_hit" : false,
    |	"token_id" : "1234",
    |	"user_id" : "jdoe",
    |	"host" : "localhost",
    |	"port" : 8080,
    |	"verb" : "POST",
    |	"path" : "/pets",
    |	"parameters" : {},
    |	"headers" : {
    |		"accept" : "application/json"
  	|	},
    |	"client_ip" : "4.34.224.131",
    |	"user_agent" : "iPhone 5s"
    |}
    """.trim.stripMargin

  val eventNode = mapper.readTree(eventJson).asInstanceOf[ObjectNode]

  val shortener = new DictionaryStringShortener(Map(
    "application_id" -> "aid",
    "client_id" -> "cid",
    "service_id" -> "sid",
    "service_version" -> "ver",
    "operation_name" -> "oper",
    "provider_id" -> "pid",
    "request_size" -> "rqs",
    "response_size" -> "rss",
    "response_time" -> "rst",
    "status_code" -> "sc",
    "error_reason" -> "er",
    "cache_hit" -> "ch",
    "token_id" -> "tid",
    "user_id" -> "uid",
    "host" -> "h",
    "port" -> "p",
    "verb" -> "v",
    "parameters" -> "pars",
    "headers" -> "hdrs",
    "client_ip" -> "cip",
    "user_agent" -> "ua"))

  val compressor = new JsonCompressor(shortener)
  val expander = new JsonExpander(shortener)

  val connectionManager = new BasicMongoDBConnectionManager("localhost:27017", "", "test-analytics")

  DisplayLabelSources.lookup += "applications" -> new DisplayLabel {
    def getDisplayLabels(ids: Set[String]) = Map("1" -> "test app")
  }

  val service = new MongoDBAnalyticsService(connectionManager, configuration)

  val timezone = DateTimeZone.forID("America/New_York")

  it should "handle timezones for days" in {
    val min = DateTime.parse("2014-09-21T00:00:00.000-04:00")
    val min_floor = Tiers.DAY.floor(min)
    val min_ceil = Tiers.DAY.ceil(min)

    val max = DateTime.parse("2014-09-21T23:59:59.999-04:00")
    val max_floor = Tiers.DAY.floor(max)
    val max_ceil = Tiers.DAY.ceil(max)

    min_floor.toString() should equal("2014-09-21T00:00:00.000-04:00")
    min_ceil.toString() should equal("2014-09-22T00:00:00.000-04:00")

    max_floor.toString() should equal("2014-09-21T00:00:00.000-04:00")
    max_ceil.toString() should equal("2014-09-22T00:00:00.000-04:00")
  }

  it should "handle timezones for weeks" in {
    val min = DateTime.parse("2014-09-21T00:00:00.000-04:00")
    val min_floor = Tiers.WEEK.floor(min)
    val min_ceil = Tiers.WEEK.ceil(min)

    val max = DateTime.parse("2014-09-27T23:59:59.999-04:00")
    val max_floor = Tiers.WEEK.floor(max)
    val max_ceil = Tiers.WEEK.ceil(max)

    min_floor.toString() should equal("2014-09-21T00:00:00.000-04:00")
    min_ceil.toString() should equal("2014-09-28T00:00:00.000-04:00")

    max_floor.toString() should equal("2014-09-21T00:00:00.000-04:00")
    max_ceil.toString() should equal("2014-09-28T00:00:00.000-04:00")
  }

  it should "handle timezones for months" in {
    val min = DateTime.parse("2014-09-01T00:00:00.000-04:00")
    val min_floor = Tiers.MONTH.floor(min)
    val min_ceil = Tiers.MONTH.ceil(min)

    val max = DateTime.parse("2014-09-30T23:59:59.999-04:00")
    val max_floor = Tiers.MONTH.floor(max)
    val max_ceil = Tiers.MONTH.ceil(max)

    min_floor.toString() should equal("2014-09-01T00:00:00.000-04:00")
    min_ceil.toString() should equal("2014-10-01T00:00:00.000-04:00")

    max_floor.toString() should equal("2014-09-01T00:00:00.000-04:00")
    max_ceil.toString() should equal("2014-10-01T00:00:00.000-04:00")
  }

  it should "handle timezones for years" in {
    val min = DateTime.parse("2014-01-01T00:00:00.000-04:00")
    val min_floor = Tiers.YEAR.floor(min)
    val min_ceil = Tiers.YEAR.ceil(min)

    val max = DateTime.parse("2014-12-31T23:59:59.999-04:00")
    val max_floor = Tiers.YEAR.floor(max)
    val max_ceil = Tiers.YEAR.ceil(max)

    min_floor.toString() should equal("2014-01-01T00:00:00.000-04:00")
    min_ceil.toString() should equal("2015-01-01T00:00:00.000-04:00")

    max_floor.toString() should equal("2014-01-01T00:00:00.000-04:00")
    max_ceil.toString() should equal("2015-01-01T00:00:00.000-04:00")
  }

  it should "log events to MongoDB" in {
    EnvironmentContext.current(EnvironmentContext("test", "test-analytics", "test-analytics"))

    //service.event("traffic", eventNode)

    //val events = service.events("traffic", start = DateTime.now().minusDays(7))
    //  
    //println(events)
  }

  it should "query metrics" in {
    EnvironmentContext.current(EnvironmentContext("test", "test-analytics", "test-analytics"))

    //val metrics = service.metrics("traffic", "response_size", "day", DateTime.now().minusYears(1), DateTime.now(), None)

    //val metrics = service.metrics("traffic", "operation_name", "day", DateTime.now().minusYears(1), DateTime.now(), None, fillGaps = false)
    //
    //println(metrics)
  }

  it should "query top counts" in {
    EnvironmentContext.current(EnvironmentContext("test", "test-analytics", "test-analytics"))

    //val metrics = service.metrics("traffic", "response_size", "day", DateTime.now().minusYears(1), DateTime.now(), None)

    //val metrics = service.counts("traffic", "application_id", DateTime.now().minusYears(1), DateTime.now(), None, Option(10))
    //
    //println(metrics)
  }
}