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

import org.scalatest.mock.MockitoSugar
import org.scalatest.Matchers
import org.scalatest.FlatSpec

class HasherUtilsTest extends FlatSpec with Matchers with MockitoSugar {
  behavior of "HasherUtils"

  it should "convert simple names for hashing algorithms to the Java names" in {
    HasherUtils.getMessageDigestAlgorithm("md5") should equal("MD5")
    HasherUtils.getMessageDigestAlgorithm("sha1") should equal("SHA-1")
    HasherUtils.getMessageDigestAlgorithm("sha256") should equal("SHA-256")
    HasherUtils.getMessageDigestAlgorithm("sha384") should equal("SHA-384")
    HasherUtils.getMessageDigestAlgorithm("sha512") should equal("SHA-512")

    the[RuntimeException] thrownBy HasherUtils.getMessageDigestAlgorithm("unknown") should have message "Invalid algorithm unknown"
  }
}