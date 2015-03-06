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

package com.monarchapis.apimanager.security

import java.net.InetAddress
import java.net.UnknownHostException

import scala.collection.immutable.List

import org.apache.commons.lang3.StringUtils

import com.monarchapis.apimanager.model._

import javax.inject.Named

@Named
class IPv4AddressRangePolicy extends Policy {
  val name = "ipv4AddressRange"

  val displayName = "IPv4 Address Range"

  val propertyDescriptors = List(
    StringPropertyDescriptor(
      propertyName = "ipAddressRanges",
      displayName = "IP Address Ranges"))

  def verify(
    config: Configuration,
    request: AuthenticationRequest,
    context: AuthenticationContext) = {
    val ipAddress = request.ipAddress

    config.value[String]("ipAddressRanges") match {
      case Some(ranges) => {
        val parts = StringUtils.split(ranges, ",") map (v => v.trim)

        parts.exists(part => {
          val parts = StringUtils.split(part, "- ") map (v => v.trim)

          if (parts.length <= 2) {
            isValidRange(parts(0), parts(parts.length - 1), ipAddress)
          } else false
        })
      }
      case _ => false
    }
  }

  private def ipToLong(ip: InetAddress): Long = {
    val octets = ip.getAddress
    var result = 0L

    for (octet <- octets) {
      result <<= 8
      result |= octet & 0xff
    }

    result
  }

  private def isValidRange(ipStart: String, ipEnd: String,
    ipToCheck: String): Boolean = {
    try {
      val ipLo = ipToLong(InetAddress.getByName(ipStart))
      val ipHi = ipToLong(InetAddress.getByName(ipEnd))
      val ipToTest = ipToLong(InetAddress.getByName(ipToCheck))

      ipToTest >= ipLo && ipToTest <= ipHi
    } catch {
      case e: UnknownHostException => false
    }
  }
}