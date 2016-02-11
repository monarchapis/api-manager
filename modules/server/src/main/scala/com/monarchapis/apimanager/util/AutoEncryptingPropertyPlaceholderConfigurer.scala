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

import java.security.SecureRandom

import scala.collection.JavaConversions._
import scala.collection.JavaConverters.seqAsJavaListConverter

import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.jasypt.encryption.StringEncryptor
import org.jasypt.properties.PropertyValueEncryptionUtils
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
import org.springframework.core.io.Resource

object AutoEncryptingPropertyPlaceholderConfigurer {
  private val AUTO_ENC_PREFIX = "AUTOENC("
  private val AUTO_ENC_SUFFIX = ")"
}

class AutoEncryptingPropertyPlaceholderConfigurer(stringEncryptor: StringEncryptor) extends PropertyPlaceholderConfigurer {
  import AutoEncryptingPropertyPlaceholderConfigurer._

  require(stringEncryptor != null, "Encryptor cannot be null")

  protected override def convertPropertyValue(originalValue: String): String = {
    if (!PropertyValueEncryptionUtils.isEncryptedValue(originalValue)) {
      return originalValue;
    }

    PropertyValueEncryptionUtils.decrypt(originalValue, stringEncryptor)
  }

  protected override def resolveSystemProperty(key: String): String = {
    convertPropertyValue(super.resolveSystemProperty(key))
  }

  override def setLocation(location: Resource) {
    encryptFile(location)
    super.setLocation(location)
  }

  override def setLocations(locations: Resource*) {
    locations.foreach(location => encryptFile(location))
    super.setLocations(locations: _*)
  }

  def setEncryptedLocations(locations: java.util.List[Resource]) {
    locations.foreach(location => encryptFile(location))
    super.setLocations(locations: _*)
  }

  private def encryptFile(location: Resource) {
    val builder = List.newBuilder[String]
    var change = false

    val file = location.getFile
    val lines = FileUtils.readLines(file)

    lines.foreach(line => {
      if (line.contains("=")) {
        val key = StringUtils.substringBefore(line, "=").trim
        val value = StringUtils.substringAfter(line, "=").trim

        if (key == "encryption.base64Key" && StringUtils.isEmpty(value)) {
          logger.info(s"Initializing master encryption key")
          val genkey = new Array[Byte](16)
          SecureRandom.getInstance("SHA1PRNG").nextBytes(genkey)
          val keyEncoded = Base64.encodeBase64String(genkey)
          builder += s"$key=ENC(${stringEncryptor.encrypt(keyEncoded)})"
          change = true
        } else if (key == "jwt.base64Key" && StringUtils.isEmpty(value)) {
          logger.info(s"Initializing JWT signing key")
          val genkey = new Array[Byte](64)
          SecureRandom.getInstance("SHA1PRNG").nextBytes(genkey)
          val keyEncoded = Base64.encodeBase64String(genkey)
          builder += s"$key=${keyEncoded}"
          change = true
        } else if (value.startsWith(AUTO_ENC_PREFIX) && value.endsWith(AUTO_ENC_SUFFIX)) {
          logger.info(s"Encrypting value for $key")
          val toEncrypt = value.substring(AUTO_ENC_PREFIX.length(), value.length() - AUTO_ENC_SUFFIX.length())
          builder += s"$key=ENC(${stringEncryptor.encrypt(toEncrypt)})"
          change = true
        } else {
          builder += line
        }
      } else {
        builder += line
      }
    })

    if (change) {
      logger.info(s"Rewriting ${file.getPath()} with updated values")
      FileUtils.writeLines(file, builder.result.asJava)
    }
  }
}