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

package com.monarchapis.apimanager.exception

import javax.ws.rs.core._

abstract class ApiException(
  val code: Int,
  val reason: String,
  val message: String,
  val developerMessage: String = null,
  val errorCode: String = null,
  val moreInfo: String = null) extends RuntimeException(message)

abstract class ClientException(
  code: Int,
  reason: String,
  message: String,
  developerMessage: String = null,
  errorCode: String = null,
  moreInfo: String = null)
  extends ApiException(
    code,
    reason,
    message,
    developerMessage,
    errorCode,
    moreInfo)

class BadRequestException(
  message: String,
  developerMessage: String = null,
  errorCode: String = null,
  moreInfo: String = null)
  extends ClientException(
    Response.Status.BAD_REQUEST.getStatusCode(),
    "badRequest",
    message,
    developerMessage,
    errorCode,
    moreInfo)

class InvalidParamaterException(
  message: String,
  developerMessage: String = null,
  errorCode: String = null,
  moreInfo: String = null)
  extends ClientException(
    Response.Status.BAD_REQUEST.getStatusCode(),
    "invalidParameter",
    message,
    developerMessage,
    errorCode,
    moreInfo)

class ConflictException(
  message: String,
  developerMessage: String = null,
  errorCode: String = null,
  moreInfo: String = null)
  extends ClientException(
    Response.Status.CONFLICT.getStatusCode(),
    "conflict",
    message,
    developerMessage,
    errorCode,
    moreInfo)

class NotFoundException(
  message: String,
  developerMessage: String = null,
  errorCode: String = null,
  moreInfo: String = null)
  extends ClientException(
    Response.Status.NOT_FOUND.getStatusCode(),
    "notFound",
    message,
    developerMessage,
    errorCode,
    moreInfo)

class NotAuthorizedException(
  message: String,
  developerMessage: String = null,
  errorCode: String = null,
  moreInfo: String = null)
  extends ClientException(
    Response.Status.UNAUTHORIZED.getStatusCode(),
    "invalidCredentials",
    message,
    developerMessage,
    errorCode,
    moreInfo)

class AccessDeniedException(
  message: String,
  developerMessage: String = null,
  errorCode: String = null,
  moreInfo: String = null)
  extends ClientException(
    Response.Status.FORBIDDEN.getStatusCode(),
    "insufficientPermissions",
    message,
    developerMessage,
    errorCode,
    moreInfo)