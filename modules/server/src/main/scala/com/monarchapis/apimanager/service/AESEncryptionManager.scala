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

package com.monarchapis.apimanager.service

import com.monarchapis.apimanager.util.Encryptor
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils

class AESEncryptionManager(base64Key: String) extends EncryptionManager {
  private val key = Base64.decodeBase64(base64Key)

  def createSalt: Array[Byte] = Encryptor.createSalt(16)

  def encryptSalted(value: String): String = {
    if (value != null) {
      val salt = createSalt
      val saltString = Base64.encodeBase64String(salt)
      val encryptedValue = encrypt(salt, value)
      saltString + '|' + encryptedValue
    } else null
  }

  def encrypt(value: String): String = encrypt(null, value)

  def encrypt(bytes: Array[Byte]): String = encrypt(null, bytes)

  def encrypt(salt: Array[Byte], value: String): String = if (value != null) encrypt(salt, value.getBytes("UTF-8")) else null

  def encrypt(salt: Array[Byte], bytes: Array[Byte]) = {
    val ret = Encryptor.encrypt(salt, 1, key, bytes)
    Base64.encodeBase64String(ret)
  }

  def decryptSaltedAsString(value: String): String = {
    if (value != null) {
      val parts = StringUtils.split(value, '|')
      require(parts.length == 2, "Invalid salted value")
      val salt = Base64.decodeBase64(parts(0))
      decryptAsString(salt, parts(1))
    } else null
  }

  def decryptAsString(value: String): String = decryptAsString(null, value)

  def decrypt(value: String): Array[Byte] = decrypt(null, value)

  def decryptAsString(salt: Array[Byte], value: String) = if (value != null) new String(decrypt(salt, value), "UTF-8") else null

  def decrypt(salt: Array[Byte], string: String): Array[Byte] = {
    require(string != null, "The string to decrypt must not be null")

    val bytes = Base64.decodeBase64(string)

    Encryptor.decrypt(salt, 1, key, bytes)
  }
}