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

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class ConversionUtilsTest extends FlatSpec with Matchers {
  behavior of "Int"

  it should "convert valid formatted strings to integers" in {
    Int.unapply("1") should be(Some(1))
  }

  it should "convert invalid formatted strings to None" in {
    Int.unapply("5000000000") should be(None)
    Int.unapply("1.234") should be(None)
    Int.unapply("abcdef") should be(None)
  }

  behavior of "Long"

  it should "convert valid formatted strings to integers" in {
    Long.unapply("1") should be(Some(1))
    Long.unapply("5000000000") should be(Some(5000000000L))
  }

  it should "convert invalid formatted strings to None" in {
    Long.unapply("1.234") should be(None)
    Long.unapply("abcdef") should be(None)
  }
}