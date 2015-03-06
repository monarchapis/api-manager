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

import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.servlet.ApiRequest

class IPv4AddressRangePolicyTest extends FlatSpec with Matchers with MockitoSugar {
  behavior of "IPv4AddressRangePolicy"

  it should "accept an IP address that matches a single IP" in {
    val config = mock[Configuration]
    val request = mock[AuthenticationRequest]
    val policy = new IPv4AddressRangePolicy

    when(config.value[String]("ipAddressRanges")).thenReturn(Some("10.10.10.5"))

    when(request.ipAddress).thenReturn("10.10.10.5")
    policy.verify(config, request, null) should equal(true)
  }

  it should "reject an IP address that does not match a single IP" in {
    val config = mock[Configuration]
    val request = mock[AuthenticationRequest]
    val policy = new IPv4AddressRangePolicy

    when(config.value[String]("ipAddressRanges")).thenReturn(Some("10.10.10.5"))

    when(request.ipAddress).thenReturn("10.10.10.4")
    policy.verify(config, request, null) should equal(false)
    when(request.ipAddress).thenReturn("10.10.10.6")
    policy.verify(config, request, null) should equal(false)
  }

  it should "accept an IP address is within a given set of ranges" in {
    val config = mock[Configuration]
    val request = mock[AuthenticationRequest]
    val policy = new IPv4AddressRangePolicy

    when(config.value[String]("ipAddressRanges")).thenReturn(Some("10.10.10.5 - 10.10.10.10"))

    when(request.ipAddress).thenReturn("10.10.10.5")
    policy.verify(config, request, null) should equal(true)
  }

  it should "reject an IP address is within a given set of ranges" in {
    val config = mock[Configuration]
    val request = mock[AuthenticationRequest]
    val policy = new IPv4AddressRangePolicy

    when(config.value[String]("ipAddressRanges")).thenReturn(Some("10.10.10.5 - 10.10.10.10"))

    when(request.ipAddress).thenReturn("10.10.10.4")
    policy.verify(config, request, null) should equal(false)
    when(request.ipAddress).thenReturn("10.10.10.11")
    policy.verify(config, request, null) should equal(false)
    when(request.ipAddress).thenReturn("11.10.10.5")
    policy.verify(config, request, null) should equal(false)
  }

  it should "test for valid IPs in multiple ranges or single IPs" in {
    val config = mock[Configuration]
    val request = mock[AuthenticationRequest]
    val policy = new IPv4AddressRangePolicy

    when(config.value[String]("ipAddressRanges")).thenReturn(Some("10.10.10.5 - 10.10.10.10, 10.10.10.250"))

    when(request.ipAddress).thenReturn("10.10.10.5")
    policy.verify(config, request, null) should equal(true)

    when(request.ipAddress).thenReturn("10.10.10.250")
    policy.verify(config, request, null) should equal(true)
  }
}