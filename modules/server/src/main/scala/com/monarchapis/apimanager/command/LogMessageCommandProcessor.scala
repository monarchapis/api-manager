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

package com.monarchapis.apimanager.command

import com.monarchapis.apimanager.service.LogService

import grizzled.slf4j.Logging
import javax.inject.Inject
import javax.inject.Named

case class LogMessage(val level: String, val message: String)

@Named
class LogMessageCommandProcessor @Inject() (logService: LogService) extends CommandProcessor[LogMessage] with Logging {
  val name = "logEntry"

  val objectType = classOf[LogMessage]

  def process(data: LogMessage): Boolean = {
    data.level match {
      case "trace" => trace(data.message)
      case "debug" => debug(data.message)
      case "info" => info(data.message)
      case "warn" => warn(data.message)
      case "error" => error(data.message)
      case _ => return false
    }

    logService.log(data.level, data.message)
    true
  }
}