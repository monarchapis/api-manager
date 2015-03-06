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

package com.monarchapis.apimanager.servlet

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import org.apache.commons.lang3.StringUtils
import java.io.File

class SystemPropertiesListener extends ServletContextListener {
  def contextInitialized(event: ServletContextEvent) {
    var home = fixPath(System.getProperty("monarch.home"))

    if (home == null) {
      throw new IllegalStateException("The Java system property \"monarch.home\" must be set.")
    }

    var config = fixPath(System.getProperty("monarch.config"))

    if (config == null) {
      config = fixPath(home + File.separatorChar + "conf")
      System.setProperty("monarch.config", config)
    }

    println(s"Config directory = $config")

    var logs = fixPath(System.getProperty("monarch.logs"))

    if (logs == null) {
      logs = fixPath(home + File.separatorChar + "logs")
      System.setProperty("monarch.logs", logs)
    }

    println(s"Logs directory = $logs")
  }

  def contextDestroyed(event: ServletContextEvent) {
  }

  private def fixPath(path: String): String = {
    if (path != null) {
      var ret = path

      // Do it without regex due to a Java bug
      while (ret.contains("\\\\")) {
        ret = StringUtils.replace(ret, "\\\\", "/")
      }

      ret = ret.replaceAll("\\/{2,}", "/")

      if (File.separatorChar != '/') {
        ret = StringUtils.replace(ret, "/", File.separator)
      }

      ret
    } else {
      null
    }
  }
}