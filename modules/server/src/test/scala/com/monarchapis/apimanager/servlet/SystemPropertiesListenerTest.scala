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

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import java.io.File
import org.scalatest.BeforeAndAfter

class SystemPropertiesListenerTest extends FlatSpec with Matchers with BeforeAndAfter {
  behavior of "SystemPropertiesListener"

  after {
    System.clearProperty("monarch.home")
    System.clearProperty("monarch.config")
    System.clearProperty("monarch.logs")
  }

  it should "set the value of monarch.config and monarch.logs based on monarch.home if not set" in {
    System.setProperty("monarch.home", "/usr/local/monarch")
    System.clearProperty("monarch.config")
    System.clearProperty("monarch.logs")

    val listener = new SystemPropertiesListener

    listener.contextInitialized(null)

    val config = System.getProperty("monarch.config")
    val logs = System.getProperty("monarch.logs")

    val root = File.separatorChar + "usr" + File.separatorChar + "local" + File.separatorChar + "monarch" + File.separatorChar
    config should equal(root + "conf")
    logs should equal(root + "logs")
  }

  it should "remove duplicate forward and back slashes" in {
    System.setProperty("monarch.home", "//usr//local\\\\monarch")
    System.clearProperty("monarch.config")
    System.clearProperty("monarch.logs")

    val listener = new SystemPropertiesListener

    listener.contextInitialized(null)

    val config = System.getProperty("monarch.config")
    val logs = System.getProperty("monarch.logs")

    val root = File.separatorChar + "usr" + File.separatorChar + "local" + File.separatorChar + "monarch" + File.separatorChar
    config should equal(root + "conf")
    logs should equal(root + "logs")
  }
}