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

class EncryptorTest extends FlatSpec with Matchers {
  behavior of "Encryptor"

  it should "create random salt values" in {
    val salt1 = Encryptor.createSalt(16)
    val salt2 = Encryptor.createSalt(16)

    salt1 should not be (salt2)
  }

  it should "encrypt and decrypt values" in {
    val password = "test"
    val input = "my secret string"
    val salt: Array[Byte] = null

    val encrypted = Encryptor.encrypt(salt, 1, password, input.getBytes)
    val decrypted = Encryptor.decrypt(salt, 1, password, encrypted)

    new String(decrypted) should be(input)
  }

  it should "with a unique salted encrypted value each time" in {
    val password = "test"
    val input = "my secret string"

    val salt1 = Encryptor.createSalt(16)
    val salt2 = Encryptor.createSalt(16)

    val encrypted1 = Encryptor.encrypt(salt1, 1, password, input.getBytes)
    val decrypted1 = Encryptor.decrypt(salt1, 1, password, encrypted1)

    new String(decrypted1) should be(input)

    val encrypted2 = Encryptor.encrypt(salt2, 1, password, input.getBytes)
    val decrypted2 = Encryptor.decrypt(salt2, 1, password, encrypted2)

    new String(decrypted1) should be(input)

    encrypted1 should not be (encrypted2)

    decrypted1 should be(decrypted2)
  }
}