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

package com.monarchapis.apimanager.security

import org.scalatest.mock.MockitoSugar
import org.scalatest.Matchers
import org.scalatest.FlatSpec
import com.monarchapis.apimanager.model.OperationMatcher

import org.mockito.Mockito

class BasicVersionExtractorTest extends FlatSpec with Matchers with MockitoSugar {
  val extractor = new BasicVersionExtractor

  behavior of "BasicVersionExtractor"

  it should "extract the version from the URI" in {
    val request = mock[AuthenticationRequest]
    val operation = mock[OperationMatcher]
    Mockito.when(operation.versionLocation).thenReturn(Seq("path"))
    val actual = extractor.extractVersion(request, operation, Map("version" -> "v1"))
    actual should equal(Some("1"))
  }

  it should "extract the version from the Accept header subtype" in {
    val request = mock[AuthenticationRequest]
    Mockito.when(request.getHeader("Accept")).thenReturn(Some("application/vnd.github.com+json; version=1"))
    val operation = mock[OperationMatcher]
    Mockito.when(operation.versionLocation).thenReturn(Seq("header"))
    val actual = extractor.extractVersion(request, operation, Map.empty[String, String])
    actual should equal(Some("1"))
  }

  it should "extract the version from the Accept header mime type" in {
    val request = mock[AuthenticationRequest]
    Mockito.when(request.getHeader("Accept")).thenReturn(Some("application/vnd.example.v1+json"))
    val operation = mock[OperationMatcher]
    Mockito.when(operation.versionLocation).thenReturn(Seq("header"))
    val actual = extractor.extractVersion(request, operation, Map.empty[String, String])
    actual should equal(Some("1"))
  }

  it should "extract the version from the query string" in {
    val request = mock[AuthenticationRequest]
    Mockito.when(request.querystring).thenReturn(Some("?version=1"))
    val operation = mock[OperationMatcher]
    Mockito.when(operation.versionLocation).thenReturn(Seq("query"))
    val actual = extractor.extractVersion(request, operation, Map.empty[String, String])
    actual should equal(Some("1"))
  }
}