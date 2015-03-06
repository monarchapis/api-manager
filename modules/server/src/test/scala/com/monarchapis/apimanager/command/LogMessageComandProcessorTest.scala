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

import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar

import com.monarchapis.apimanager.service._

import javax.inject.Inject
import javax.inject.Named

class LogMessageComandProcessorTest extends FlatSpec with Matchers with MockitoSugar {
  behavior of "LogMessageCommandProcessor"

  val levels = Seq("trace", "debug", "info", "warn", "error")
  val serviceLevels = collection.mutable.Set[String]()

  val logService = mock[LogService]

  levels foreach { level =>
    when(logService.log(level, "test")).thenAnswer(new Answer[Unit] {
      def answer(invocation: InvocationOnMock): Unit = serviceLevels += invocation.getArguments()(0).asInstanceOf[String]
    })
  }

  val logLevels = collection.mutable.Set[String]()

  val processor = new LogMessageCommandProcessor(logService) {
    override def trace(msg: => Any): Unit = logLevels += "trace"
    override def debug(msg: => Any): Unit = logLevels += "debug"
    override def info(msg: => Any): Unit = logLevels += "info"
    override def warn(msg: => Any): Unit = logLevels += "warn"
    override def error(msg: => Any): Unit = logLevels += "error"
  }

  it should "make sure the log level is valid" in {
    levels foreach { level =>
      processor.process(LogMessage(level, "test")) should be(true)
    }

    processor.process(LogMessage("none", "test")) should be(false)
  }

  it should "invoke the log service with the level and service" in {
    levels foreach { level =>
      logLevels.contains(level) should be(true)
      serviceLevels.contains(level) should be(true)
    }

    logLevels.contains("none") should be(false)
    serviceLevels.contains("none") should be(false)
  }
}