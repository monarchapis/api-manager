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

package com.monarchapis.apimanager.analytics.maxmind

import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.FactoryBean
import org.springframework.core.io.Resource
import com.maxmind.geoip2.DatabaseReader

class MaxMindGeoIP2DatabaseReaderFactory(configurationFile: Resource) extends FactoryBean[DatabaseReader] {
  private val databaseReader = {
    val is = configurationFile.getInputStream

    try {
      new DatabaseReader.Builder(is).build()
    } finally {
      IOUtils.closeQuietly(is)
    }
  }

  override def getObject = databaseReader

  override def getObjectType = classOf[DatabaseReader]

  override def isSingleton = true
}