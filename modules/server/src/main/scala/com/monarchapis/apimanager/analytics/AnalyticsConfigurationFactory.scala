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

package com.monarchapis.apimanager.analytics

import scala.collection.JavaConversions._

import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.FactoryBean
import org.springframework.core.io.Resource

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class AnalyticsConfigurationFactory(configurationFiles: java.util.List[Resource]) extends FactoryBean[AnalyticsConfiguration] {
  private val configuration = {
    val mapper = new ObjectMapper(new YAMLFactory)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    mapper.registerModule(DefaultScalaModule)

    val builder = List.newBuilder[EventType]

    configurationFiles.foreach(file => {
      val is = file.getInputStream

      try {
        val eventType = mapper.readValue(is, classOf[EventType])

        builder += eventType
      } finally {
        IOUtils.closeQuietly(is)
      }
    })

    AnalyticsConfiguration(builder.result)
  }

  override def getObject = configuration

  override def getObjectType = classOf[AnalyticsConfiguration]

  override def isSingleton = true
}