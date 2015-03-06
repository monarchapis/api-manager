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

package com.monarchapis.apimanager.analytics.maxmind

import java.net.InetAddress

import com.fasterxml.jackson.databind.node.ObjectNode
import com.maxmind.geoip2.GeoIp2Provider
import com.maxmind.geoip2.exception.AddressNotFoundException
import com.monarchapis.apimanager.analytics.EventProcessor
import com.monarchapis.apimanager.analytics.EventType

object MaxMindGeoIP2EventProcessor {
  private val ipAddressRegex = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".r
}

class MaxMindGeoIP2EventProcessor(geoIp2Provider: GeoIp2Provider) extends EventProcessor {
  import MaxMindGeoIP2EventProcessor._

  val name = "MaxMind-GeoIP2"

  def process(eventType: EventType, data: ObjectNode, args: String*) {
    if (args.size != 1) {
      throw new IllegalArgumentException("MaxMind-GeoIP2 usage: <IP address field>")
    }

    val field = args(0)

    if (!data.has(field)) return

    val value = data.get(field)

    if (!value.isTextual) return

    val ipAddress = ipAddressRegex.findFirstIn(value.asText).getOrElse(return )

    try {
      val response = geoIp2Provider.city(InetAddress.getByName(ipAddress))

      data.put("country", response.getCountry.getIsoCode)
      data.put("state", response.getMostSpecificSubdivision.getIsoCode)
      data.put("city", response.getCity.getName)
      data.put("postal", response.getPostal.getCode)
      data.put("lat", response.getLocation.getLatitude)
      data.put("lon", response.getLocation.getLongitude)
    } catch {
      case anfe: AddressNotFoundException => // Ignore
    }
  }
}