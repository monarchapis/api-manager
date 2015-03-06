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
import javax.inject.Inject
import javax.inject.Named
import com.monarchapis.apimanager.util.Registry
import EventProcessorRegistry.{instance_= => instance_=}

object EventProcessorRegistry {
  private var instance: EventProcessorRegistry = null

  def apply() = instance
}

@Named
class EventProcessorRegistry(
  private val processors: EventProcessor*) extends Registry[EventProcessor](processors: _*) {
  @Inject def this(processors: java.util.List[EventProcessor]) = this(processors: _*)

  import EventProcessorRegistry._

  instance = this
}