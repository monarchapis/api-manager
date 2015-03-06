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

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.SerializationFeature

class YamlReaderTest extends FlatSpec with Matchers {
  val input = """
events:
  -
    name        : traffic
    timezone	: America/New_York
    fields      :
      - 
        name        : application_id
        storeAs     : aid
        type        : string
        required    : true
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
        storeAs     : h
        type        : string
        required    : true
      -
        name        : port
        storeAs     : p
        type        : code
        required    : true
      -
        name        : verb
        storeAs     : v
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
        name        : MaxMind-GeoIP
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

  behavior of "test"

  it should "read an analytics configuration into POJOs" in {
    val mapper1 = new ObjectMapper(new YAMLFactory)
    mapper1.enable(SerializationFeature.INDENT_OUTPUT)
    mapper1.registerModule(DefaultScalaModule)
    val config = mapper1.readValue(input, classOf[AnalyticsConfiguration])

    val mapper2 = new ObjectMapper
    mapper2.enable(SerializationFeature.INDENT_OUTPUT)
    mapper2.registerModule(DefaultScalaModule)
    
    //println(config)

    //val indented = mapper2.writeValueAsString(config)

    //println(indented)
  }
}