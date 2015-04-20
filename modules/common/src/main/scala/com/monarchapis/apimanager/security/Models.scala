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

import java.util.UUID
import scala.beans.BeanProperty
import scala.beans.BooleanBeanProperty
import scala.collection.mutable.Builder
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import javax.validation.constraints.NotNull
import com.monarchapis.apimanager.model._

case class AuthenticationContext(
  var client: Option[Key] = None,
  var token: Option[Token] = None,
  var principal: Option[String] = None,
  var claims: Option[Set[Claim]] = None)

case class AuthenticationRequest(
  @BeanProperty protocol: String,
  @BeanProperty method: String,
  @BeanProperty host: String,
  @BeanProperty port: Int,
  @BeanProperty path: String,
  @BeanProperty querystring: Option[String],
  @BeanProperty headers: Map[String, List[String]],
  @BeanProperty ipAddress: String,
  @BeanProperty payloadHashes: Map[String, Map[String, String]] = Map(),
  @BeanProperty requestWeight: Option[BigDecimal] = None,
  @BeanProperty performAuthorization: Boolean = false,
  @BeanProperty bypassRateLimiting: Boolean = false) {
  def withProtocol(protocol: String) = copy(protocol = protocol)
  def withMethod(method: String) = copy(method = method)
  def withPath(path: String) = copy(path = path)
  def withQuerystring(querystring: Option[String]) = copy(querystring = querystring)
  def withHeaders(headers: Map[String, List[String]]) = copy(headers = headers)
  def withIpAddress(ipAddress: String) = copy(ipAddress = ipAddress)
  def withPayloadHashes(payloadHashes: Map[String, Map[String, String]]) = copy(payloadHashes = payloadHashes)
  def withRequestWeight(requestWeight: Option[BigDecimal]) = copy(requestWeight = requestWeight)
  def withPerformAuthorization(performAuthorization: Boolean) = copy(performAuthorization = performAuthorization)
  def withBypassRateLimiting(bypassRateLimiting: Boolean) = copy(bypassRateLimiting = bypassRateLimiting)

  // Create parallel map with lower case keys
  private val ciHeaders = {
    val mb = scala.collection.mutable.Map[String, Builder[String, List[String]]]()

    headers foreach {
      case (k, v) => {
        val lowerKey = k.toLowerCase
        val _list = mb.get(lowerKey)
        val list = _list match {
          case Some(l) => l
          case _ => {
            val l = List.newBuilder[String]
            mb += lowerKey -> l
            l
          }
        }
        v foreach (s => list += s)
      }
    }

    val builder = Map.newBuilder[String, List[String]]

    mb foreach {
      case (k, v) => {
        builder += k -> v.result
      }
    }

    builder.result
  }

  def hasHeader(name: String): Boolean = ciHeaders contains name.toLowerCase
  def getHeaderValues(name: String): Option[List[String]] = ciHeaders get name.toLowerCase
  def getHeader(name: String): Option[String] = {
    val list = ciHeaders get name.toLowerCase()
    list match {
      case Some(l) => if (l.length > 0) Some(l(0)) else None
      case _ => None
    }
  }
}

case class ContextScope(
  permissions: Set[String] = Set(),
  extended: Map[String, Object] = Map()) {
  def withPermissions(permissions: Set[String]) = copy(permissions = permissions)
  def addPermission(permission: String) = copy(permissions = permissions + permission)
  def removePermission(permission: String) = copy(permissions = permissions - permission)
  def withExtended(extended: Map[String, List[String]]) = copy(extended = extended)
}

case class ApplicationContext(
  @BeanProperty id: String,
  @BeanProperty name: Option[String] = None,
  @BeanProperty extended: Map[String, Object] = Map())

case class ClientContext(
  @BeanProperty id: String,
  @BeanProperty label: String,
  @BeanProperty permissions: Set[String] = Set(),
  @BeanProperty extended: Map[String, Object] = Map())

case class TokenContext(
  @BeanProperty id: String,
  @BeanProperty permissions: Set[String] = Set(),
  @BeanProperty extended: Map[String, Object] = Map())

case class PrincipalContext(
  @BeanProperty id: String,
  @BeanProperty context: Option[String],
  @BeanProperty claims: Map[String, Set[String]] = Map())

case class ProviderContext(
  @BeanProperty id: String,
  @BeanProperty label: String)

case class ApiContext(
  @BeanProperty correlationId: String = StringUtils.replace(UUID.randomUUID.toString, "-", ""),
  @BeanProperty application: ApplicationContext,
  @BeanProperty client: ClientContext,
  @BeanProperty token: Option[TokenContext],
  @BeanProperty principal: Option[PrincipalContext],
  @BeanProperty provider: Option[ProviderContext]) {
  def withApplication(application: ApplicationContext) = copy(application = application)
  def withClient(client: ClientContext) = copy(client = client)
  def withToken(token: Option[TokenContext]) = copy(token = token)
  def withPrincipal(principal: Option[PrincipalContext]) = copy(principal = principal)
  def withProvider(provider: Option[ProviderContext]) = copy(provider = provider)
}

case class HttpHeader(
  @BeanProperty name: String,
  @BeanProperty value: String)

case class VariableContext(
  @BeanProperty providerId: String,
  @BeanProperty serviceId: Option[String] = None,
  @BeanProperty serviceVersion: Option[String] = None,
  @BeanProperty operation: Option[String] = None,
  @BeanProperty pathParameters: Option[Map[String, String]] = None)

case class AuthenticationResponse(
  @BeanProperty code: Int,
  @BeanProperty reason: Option[String],
  @BeanProperty message: Option[String],
  @BeanProperty developerMessage: Option[String],
  @BeanProperty errorCode: Option[String],
  @BeanProperty responseHeaders: List[HttpHeader],
  @BeanProperty vars: Option[VariableContext],
  @BeanProperty context: Option[ApiContext])

case class AuthorizationRequest(
  @BeanProperty authorizationScheme: String,
  @BeanProperty apiKey: String,
  @BeanProperty callbackUri: Option[String],
  @BeanProperty permissions: Set[String])

case class ClientAuthenticationRequest(
  @BeanProperty authorizationScheme: String,
  @BeanProperty apiKey: String,
  @BeanProperty sharedSecret: Option[String])

case class ClientDetails(
  @BeanProperty id: String,
  @BeanProperty apiKey: String,
  @BeanProperty expiration: Option[Long],
  @BooleanBeanProperty autoAuthorize: Boolean,
  @BooleanBeanProperty allowWebView: Boolean,
  @BooleanBeanProperty allowPopup: Boolean)

case class ApplicationDetails(
  @BeanProperty id: String,
  @BeanProperty name: String,
  @BeanProperty description: Option[String],
  @BeanProperty applicationUrl: String,
  @BeanProperty applicationImageUrl: Option[String],
  @BeanProperty companyName: String,
  @BeanProperty companyUrl: String,
  @BeanProperty companyImageUrl: Option[String])

case class PermissionDetails(
  @BeanProperty name: String,
  @BeanProperty flags: Set[String])

case class AuthorizationDetails(
  @BeanProperty client: ClientDetails,
  @BeanProperty application: ApplicationDetails,
  @BeanProperty permissions: List[PermissionDetails])

case class CreateTokenRequest(
  @NotNull @BeanProperty authorizationScheme: String,
  @NotNull @BeanProperty apiKey: String,
  @NotNull @BeanProperty grantType: String,
  @NotNull @BeanProperty tokenType: String,
  @NotNull @BeanProperty permissions: Set[String] = Set(),
  @BeanProperty state: Option[String] = None,
  @NotNull @BeanProperty uri: Option[String] = None,
  @NotNull @BeanProperty userId: Option[String] = None,
  @BeanProperty userContext: Option[String] = None,
  @BeanProperty extended: Map[String, Object] = Map())

case class GetTokenRequest(
  @BeanProperty apiKey: String,
  @BeanProperty token: String,
  @BeanProperty callbackUri: Option[String])

case class TokenDetails(
  @BeanProperty id: String,
  @BeanProperty token: String,
  @BeanProperty refreshToken: Option[String] = None,
  @BeanProperty expiresIn: Option[Long] = None,
  @BeanProperty grantType: String,
  @BeanProperty tokenType: String,
  @BeanProperty permissions: Set[String] = Set(),
  @BeanProperty state: Option[String] = None,
  @BeanProperty uri: Option[String] = None,
  @BeanProperty userId: Option[String] = None,
  @BeanProperty userContext: Option[String] = None,
  @BeanProperty extended: Map[String, Object] = Map())

case class LocaleInfo(
  @BeanProperty language: String,
  @BeanProperty country: Option[String] = None,
  @BeanProperty variant: Option[String] = None) {
  def apply() = {
    val sb = new StringBuilder
    sb.append(language)

    country match {
      case Some(s) => sb.append('-').append(s)
      case _ =>
    }

    variant match {
      case Some(s) => sb.append('-').append(s)
      case _ =>
    }

    sb.toString
  }
}

case class MessageList(messages: List[MessageDetails])

case class MessageDetails(
  @BeanProperty format: String,
  @BeanProperty content: String,
  @BeanProperty children: List[MessageDetails])

case class PermissionMessagesRequest(
  @BeanProperty locales: List[LocaleInfo],
  @BeanProperty permissions: Set[String])

object PropertyType extends Enumeration {
  case class EValue(name: String) extends Val(name) {}

  val STRING = EValue("string")
  val INTEGER = EValue("integer")
  val DECIMAL = EValue("decimal")
  val BOOLEAN = EValue("boolean")
  val DATETIME = EValue("datetime")
}

case class PropertyOption[T](
  @BeanProperty val label: String,
  @BeanProperty val value: T)

trait PropertyDescriptor {
  val propertyName: String
  val displayName: String
  val propertyType: String
  val required: Boolean
  val multi: Boolean
}

case class StringPropertyDescriptor(
  @BeanProperty propertyName: String,
  @BeanProperty displayName: String,
  @BooleanBeanProperty required: Boolean = true,
  @BooleanBeanProperty multi: Boolean = false,
  @BeanProperty defaultValue: Option[String] = None,
  @BeanProperty options: List[PropertyOption[String]] = List()) extends PropertyDescriptor {
  @BeanProperty val propertyType = PropertyType.STRING.name
}

case class IntegerPropertyDescriptor(
  @BeanProperty propertyName: String,
  @BeanProperty displayName: String,
  @BooleanBeanProperty required: Boolean = true,
  @BooleanBeanProperty multi: Boolean = false,
  @BeanProperty defaultValue: Option[Int] = None,
  @BeanProperty options: List[PropertyOption[Int]] = List()) extends PropertyDescriptor {
  @BeanProperty val propertyType = PropertyType.INTEGER.name
}

case class DecimalPropertyDescriptor(
  @BeanProperty propertyName: String,
  @BeanProperty displayName: String,
  @BooleanBeanProperty required: Boolean = true,
  @BooleanBeanProperty multi: Boolean = false,
  @BeanProperty defaultValue: Option[Double] = None,
  @BeanProperty options: List[PropertyOption[Double]] = List()) extends PropertyDescriptor {
  @BeanProperty val propertyType = PropertyType.DECIMAL.name
}

case class BooleanPropertyDescriptor(
  @BeanProperty propertyName: String,
  @BeanProperty displayName: String,
  @BooleanBeanProperty required: Boolean = true,
  @BooleanBeanProperty multi: Boolean = false,
  @BeanProperty defaultValue: Boolean = false) extends PropertyDescriptor {
  @BeanProperty val propertyType = PropertyType.BOOLEAN.name
}

case class DateTimePropertyDescriptor(
  @BeanProperty propertyName: String,
  @BeanProperty displayName: String,
  @BooleanBeanProperty required: Boolean = true,
  @BooleanBeanProperty multi: Boolean = false,
  @BeanProperty defaultValue: Option[DateTime] = None) extends PropertyDescriptor {
  @BeanProperty val propertyType = PropertyType.DATETIME.name
}