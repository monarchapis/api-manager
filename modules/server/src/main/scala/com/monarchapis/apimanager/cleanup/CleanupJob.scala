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

package com.monarchapis.apimanager.cleanup

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._

import grizzled.slf4j.Logging
import javax.inject.Inject

class CleanupJob @Inject() (
  environmentService: EnvironmentService,
  cleanableRegistry: CleanableRegistry) extends Logging {
  require(environmentService != null, "environmentService is required")
  require(cleanableRegistry != null, "cleanableRegistry is required")

  def cleanup {
    debug("Starting cleanup process...")

    val environments = environmentService.find(0, 1000).items

    environments foreach { environment =>
      {
        val name = environment.name

        try {
          debug(s"Cleaning environment -> ${name}")
          UserContext.current("system")
          EnvironmentContext.current(
            EnvironmentContext(
              environment.id,
              environment.systemDatabase,
              environment.analyticsDatabase))
          cleanableRegistry.cleanAll
        } catch {
          case e: Exception => error(s"Error cleaning up ${name}", e)
        } finally {
          UserContext.remove
          EnvironmentContext.remove
        }
      }
    }
  }
}