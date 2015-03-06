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

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import scala.collection.JavaConverters.setAsJavaSetConverter
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.RandomStringUtils
import org.jasypt.encryption.pbe.config.SimplePBEConfig
import org.springframework.core.io.Resource
import java.nio.file.LinkOption

class AutoInitializingPBEConfig extends SimplePBEConfig {
  def setPasswordFile(passwordFile: Resource) {
    val file = passwordFile.getFile

    if (!file.exists) {
      val pbeKey = RandomStringUtils.randomAlphanumeric(24)
      val path = Paths.get(file.getPath)
      FileUtils.write(file, pbeKey)

      // Using PosixFilePermission to set file permissions 600
      val perms = Set(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE)

      try {
        Files.setPosixFilePermissions(path, perms.asJava)
      } catch {
        case usoe: UnsupportedOperationException => // Do nothing
      }

      try {
        Files.setAttribute(path, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS)
      } catch {
        case usoe: UnsupportedOperationException => // Do nothing
      }

      setPassword(pbeKey)
    } else {
      val pbeKey = FileUtils.readFileToString(file).trim
      setPassword(pbeKey)
    }
  }
}