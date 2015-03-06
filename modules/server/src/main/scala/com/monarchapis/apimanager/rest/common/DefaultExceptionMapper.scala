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

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import javax.inject._
import javax.ws.rs.core._
import javax.ws.rs.ext._
import javax.ws.rs.ext.Provider
import grizzled.slf4j.Logging
import com.monarchapis.apimanager.exception.ApiException
import javax.ws.rs.NotFoundException
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException

@Provider
class DefaultExceptionMapper extends ExceptionMapper[Exception] with Logging {
  def toResponse(exception: Exception): Response = {
    exception match {
      // Jersey resource not found
      case e: NotFoundException => Response.status(Response.Status.NOT_FOUND).entity(
        Error(
          code = Response.Status.NOT_FOUND.getStatusCode(),
          reason = "notFound",
          message = "The resource you requested was not found.",
          developerMessage = "The resource you requested was not found.",
          errorCode = "CLIENT-0001")).build

      case e: IllegalArgumentException => Response.status(Response.Status.BAD_REQUEST).entity(
        Error(
          code = Response.Status.BAD_REQUEST.getStatusCode(),
          reason = "illegalArgument",
          message = e.getMessage,
          developerMessage = "Check the that you are accessing a valid field for this resource.",
          errorCode = "CLIENT-0002")).build

      case e: MessageBodyProviderNotFoundException => Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity(
        Error(
          code = Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(),
          reason = "unsupportedMediaType",
          message = "The requested media type is not allowed by this resource.",
          developerMessage = "The requested media type is not allowed by this resource.",
          errorCode = "CLIENT-0003")).build

      case e: ApiException => Response.status(e.code).entity(
        Error(
          code = e.code,
          reason = e.reason,
          message = e.getMessage,
          developerMessage = e.developerMessage,
          errorCode = e.errorCode,
          moreInfo = e.moreInfo)).build

      case _ => {
        error("An uncaught exception occurred", exception)

        Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
          Error(
            code = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            reason = "systemError",
            message = "A general error has occurred.",
            developerMessage = "A general error has occurred.",
            errorCode = "SYSTEM-0001")).build
      }
    }
  }
}