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

package com.monarchapis.apimanager.rest.common

import org.glassfish.jersey.CommonProperties
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import javax.ws.rs._
import javax.ws.rs.core._
import javax.ws.rs.ext.MessageBodyReader
import javax.ws.rs.ext.MessageBodyWriter

class JacksonFeatureTest extends FlatSpec with Matchers with MockitoSugar {
  import org.mockito.Mockito._
  import org.mockito.Matchers._

  behavior of "JacksonFeature"

  val runtimeType = RuntimeType.SERVER
  val configuration = mock[Configuration]
  val context = mock[FeatureContext]

  when(configuration.getRuntimeType).thenReturn(runtimeType)
  when(context.getConfiguration).thenReturn(configuration)

  val jacksonFeature = new JacksonFeature
  jacksonFeature.configure(context)

  it should "disable moxy support" in {
    verify(context).property(CommonProperties.MOXY_JSON_FEATURE_DISABLE + ".server", true)
  }

  it should "register JacksonJaxbJsonProvider" in {
    verify(context).register(classOf[JacksonJaxbJsonProvider], classOf[MessageBodyReader[_]], classOf[MessageBodyWriter[_]])
  }
}