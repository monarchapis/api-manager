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

package com.monarchapis.apimanager.servlet

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class ServletInputStreamWrapperTest extends FlatSpec with Matchers {
  behavior of "ServletInputStreamWrapper"

  val wrapper = new ServletInputStreamWrapper("test".getBytes)

  it should "return data character by character of the provided data" in {
    wrapper.read should be('t'.toInt)
    wrapper.read should be('e'.toInt)
    wrapper.read should be('s'.toInt)
    wrapper.read should be('t'.toInt)
  }

  it should "return -1 when no more data is available" in {
    wrapper.read should be(-1)
  }
}