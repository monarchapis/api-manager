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

class SecurityUtilsTests extends FlatSpec with Matchers {
  behavior of "Hashing"

  it should "produce a string sha256 hash (base 16) for a given string value" in {
    Hashing.sha256("this is my test value") should be("3fa996db10ea93b9cb69e8d5c67dc803fc0ef2041c026af4ef8d090d887aa7c1")
  }
}