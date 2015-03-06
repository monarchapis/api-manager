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
import org.scalatest.mock.MockitoSugar
import org.scalatest.Matchers
import org.scalatest.FlatSpec
import com.monarchapis.apimanager.servlet.ApiRequest

class HawkV1RequestHasherTest extends FlatSpec with Matchers with MockitoSugar {
  behavior of "HawkV1RequestHasher"

  it should "generate a request hash per the Hawk authentication scheme" in {
    val request = mock[ApiRequest]
    val hasher = new HawkV1RequestHasher

    when(request.getContentType()).thenReturn("text/plain; encodong=utf8");
    when(request.body).thenReturn("Thank you for flying Hawk".getBytes())

    val actual = hasher.getRequestHash(request, "sha256")
    actual should equal("Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=")
  }
}