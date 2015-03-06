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

abstract class AuthenticationException(
  val code: Int,
  val reason: String,
  message: String,
  val developerMessage: String,
  val errorCode: String,
  val cause: Throwable = null,
  val responseHeaders: Seq[HttpHeader] = Seq())
  extends Exception(message, cause)

//--------------------------------------

class CredentialsNotSuppliedException(
  message: String = "Authentication is required to access this resource.",
  cause: Throwable = null, responseHeaders: Seq[HttpHeader] = Seq())
  extends AuthenticationException(401, "invalidCredentials", "Access Denied", message, "SEC-0001", cause, responseHeaders)

class InvalidCredentialsException(
  message: String = "The credientals you provided did not match any of our records.",
  cause: Throwable = null, responseHeaders: Seq[HttpHeader] = Seq())
  extends AuthenticationException(401, "invalidCredentials", "Access Denied", message, "SEC-0002", cause, responseHeaders)

class InvalidAccessTokenException(
  message: String = "This access token is invalid or has expired.",
  cause: Throwable = null, responseHeaders: Seq[HttpHeader] = Seq())
  extends AuthenticationException(401, "invalidAccessToken", "Access Denied", message, "SEC-0003", cause, responseHeaders)

class InvalidSignatureException(message: String, cause: Throwable = null, responseHeaders: Seq[HttpHeader] = Seq())
  extends AuthenticationException(401, "invalidSignature", "Access Denied", message, "SEC-0004", cause, responseHeaders)

class InvalidTimestampException(message: String, cause: Throwable = null, responseHeaders: Seq[HttpHeader] = Seq())
  extends AuthenticationException(401, "invalidSignature", "Access Denied", message, "SEC-0005", cause, responseHeaders)

class VerificationException(message: String, cause: Throwable = null, responseHeaders: Seq[HttpHeader] = Seq())
  extends AuthenticationException(401, "verificationFailed", "Access Denied", message, "SEC-0006", cause, responseHeaders)

//--------------------------------------

class UnauthorizedException(responseHeaders: HttpHeader*)
  extends AuthenticationException(403, "unauthorized", "Access Denied", "An Authorization header for this scheme was not supplied", "SEC-0007", responseHeaders = responseHeaders)

class UnauthorizedOperationException(
  message: String = "Your requesting a resource method that is not allowed.",
  cause: Throwable = null, responseHeaders: Seq[HttpHeader] = Seq())
  extends AuthenticationException(403, "unauthorized", "Access Denied", message, "SEC-0008", cause, responseHeaders)

class InvalidAuthenticationException(message: String, cause: Throwable = null, responseHeaders: Seq[HttpHeader] = Seq())
  extends AuthenticationException(403, "invalidAuthentication", "Access Denied", message, "SEC-0009", cause, responseHeaders)

//--------------------------------------

class NotFoundException(
  message: String = "The resource you requested was not found.",
  cause: Throwable = null, responseHeaders: Seq[HttpHeader] = Seq())
  extends AuthenticationException(404, "notFound", message, message, "CLNT-0001", cause, responseHeaders)