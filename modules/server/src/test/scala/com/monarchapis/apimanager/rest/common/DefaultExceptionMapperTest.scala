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

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import javax.ws.rs.NotFoundException
import com.monarchapis.apimanager.model.Error
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException
import com.monarchapis.apimanager.exception.ApiException
import com.monarchapis.apimanager.exception.ClientException
import com.monarchapis.apimanager.exception.AccessDeniedException
import org.mockito.internal.util.reflection.Whitebox
import grizzled.slf4j.Logger
import org.scalatest.mock.MockitoSugar

class DefaultExceptionMapperTest extends FlatSpec with Matchers with MockitoSugar {
  val mapper = new DefaultExceptionMapper {
    protected override def error(msg: => Any, t: => Throwable): Unit = {}
  }

  behavior of "DefaultExceptionMapper"

  it should "convert Jersey not found exceptions to error responses w/ code 404" in {
    val response = mapper.toResponse(new NotFoundException("testing 123"))
    val error = response.getEntity.asInstanceOf[Error]

    error.code should equal(404)
    error.reason should equal("notFound")
    error.message should equal("The resource you requested was not found.")
    error.developerMessage should equal("The resource you requested was not found.")
    error.errorCode should equal("CLIENT-0001")
  }

  it should "convert IllegalArgumentExceptions to error responses w/ code 400" in {
    val response = mapper.toResponse(new IllegalArgumentException("test is a required field"))
    val error = response.getEntity.asInstanceOf[Error]

    error.code should equal(400)
    error.reason should equal("illegalArgument")
    error.message should equal("test is a required field")
    error.developerMessage should equal("Check the that you are accessing a valid field for this resource.")
    error.errorCode should equal("CLIENT-0002")
  }

  it should "convert MessageBodyProviderNotFoundException to error responses w/ code 415" in {
    val response = mapper.toResponse(new MessageBodyProviderNotFoundException("unsupported media type"))
    val error = response.getEntity.asInstanceOf[Error]

    error.code should equal(415)
    error.reason should equal("unsupportedMediaType")
    error.message should equal("The requested media type is not allowed by this resource.")
    error.developerMessage should equal("The requested media type is not allowed by this resource.")
    error.errorCode should equal("CLIENT-0003")
  }

  it should "convert API exceptions to error responses w/ the indicated code" in {
    val response = mapper.toResponse(new AccessDeniedException(
      message = "testing 123",
      developerMessage = "be more careful",
      errorCode = "CLIENT-9999",
      moreInfo = "http://example.com/CLIENT-9999"))
    val error = response.getEntity.asInstanceOf[Error]

    error.code should equal(403)
    error.reason should equal("insufficientPermissions")
    error.message should equal("testing 123")
    error.developerMessage should equal("be more careful")
    error.errorCode should equal("CLIENT-9999")
    error.moreInfo should equal("http://example.com/CLIENT-9999")
  }

  it should "convert general exceptions to error responses w/ code 500" in {
    val response = mapper.toResponse(new RuntimeException("something bad happened"))
    val error = response.getEntity.asInstanceOf[Error]

    error.code should equal(500)
    error.reason should equal("systemError")
    error.message should equal("A general error has occurred.")
    error.developerMessage should equal("A general error has occurred.")
    error.errorCode should equal("SYSTEM-0001")
  }
}