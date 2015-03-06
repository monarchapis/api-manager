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

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.mockito.Mockito.when

class ApplicationContextProviderTest extends FlatSpec with Matchers with MockitoSugar {
  behavior of "ApplicationContextProvider"

  it should "allow for the application context to be injected by Spring" in {
    val provider = new ApplicationContextProvider

    (classOf[ApplicationContextAware] isAssignableFrom provider.getClass()) should be(true)

    val ctx = mock[ApplicationContext]

    when(ctx.getApplicationName()).thenReturn("test")

    provider.setApplicationContext(ctx)

    ApplicationContextProvider().getApplicationName() should be("test")
  }
}
