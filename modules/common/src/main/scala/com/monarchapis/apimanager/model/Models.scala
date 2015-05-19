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

package com.monarchapis.apimanager.model

import org.joda.time.DateTime
import scala.beans.BeanProperty
import scala.beans.BooleanBeanProperty
import scala.collection.mutable.Builder
import scala.collection.immutable.SortedSet
import com.monarchapis.apimanager.exception._
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import java.util.regex.Pattern
import scala.util.matching.Regex

trait AbstractContext[T] {
  private val currentValue: ThreadLocal[T] = new ThreadLocal[T]
  protected def defaultValue: T

  def current = {
    val value = currentValue.get.asInstanceOf[T]
    if (value != null) value else defaultValue
  }

  def get = {
    val value = currentValue.get.asInstanceOf[T]
    if (value != null) Some(value) else None
  }

  def getOrElse(defaultValue: T) = {
    val value = currentValue.get.asInstanceOf[T]
    if (value != null) value else defaultValue
  }

  def current(value: T) = currentValue.set(value)
  def isSet = currentValue.get != null
  def remove = currentValue.remove
}

object UserContext extends AbstractContext[String] {
  protected val defaultValue = "system"
}

case class EnvironmentContext(
  id: String,
  systemDatabase: String,
  analyticsDatabase: String);

object EnvironmentContext extends AbstractContext[EnvironmentContext] {
  protected def defaultValue = throw new BadRequestException("Environment context is not set")
}

trait Entity {
  val id: String
  def withId(id: String): Entity
}

trait AuditSupport {
  val createdBy: String
  val createdDate: DateTime
  val modifiedBy: String
  val modifiedDate: DateTime
}

///////////////////////////////////////////////////////////

case class PagedList[T](
  @BeanProperty offset: Integer,
  @BeanProperty limit: Integer,
  @BeanProperty count: Integer,
  @BeanProperty total: Integer,
  @BeanProperty items: List[T])

///////////////////////////////////////////////////////////

trait Authorization {
  val id: String
  val name: String
  val administrator: Boolean
  val permissions: Set[String]
  val accessLevels: Map[String, String]
}

case class UserAuthorization(
  @BeanProperty val id: String,
  @BeanProperty val name: String,
  @BooleanBeanProperty val administrator: Boolean = false,
  @BeanProperty val permissions: Set[String] = Set(),
  @BeanProperty val accessLevels: Map[String, String] = Map()) extends Authorization

object AuthorizationHolder extends AbstractContext[Authorization] {
  protected def defaultValue = throw new BadRequestException("Authorization is not set")
}

object BehindReverseProxyHolder extends AbstractContext[Boolean] {
  protected def defaultValue = true
}

object ProviderHolder extends AbstractContext[Provider] {
  protected def defaultValue = throw new BadRequestException("Provider is not set")
}

case class AuthenticatedUser(
  @BeanProperty id: String,
  @BeanProperty userName: String,
  @BeanProperty firstName: String,
  @BeanProperty lastName: String,
  @BooleanBeanProperty administrator: Boolean = false) {
  def withId(id: String) = copy(id = id)
  def withUserName(userName: String) = copy(userName = userName)
  def withFirstName(firstName: String) = copy(firstName = firstName)
  def withLastName(lastName: String) = copy(lastName = lastName)
  def withAdministrator(administrator: Boolean) = copy(administrator = administrator)
}

case class User(
  @BeanProperty id: String = null,
  @BeanProperty userName: String,
  @BeanProperty firstName: String,
  @BeanProperty lastName: String,
  @BooleanBeanProperty administrator: Boolean = false,
  @BeanProperty externalId: Option[String] = None,
  @JsonInclude(Include.NON_NULL)@BeanProperty role: Option[Role] = None,
  @JsonInclude(Include.NON_NULL)@BeanProperty roleId: Option[String] = None) extends Entity {
  def withId(id: String) = copy(id = id)
  def withUserName(userName: String) = copy(userName = userName)
  def withFirstName(firstName: String) = copy(firstName = firstName)
  def withLastName(lastName: String) = copy(lastName = lastName)
  def withRole(role: Option[Role]) = copy(role = role)
  def withRoleId(roleId: Option[String]) = copy(roleId = roleId)
}

case class Role(
  @BeanProperty id: String = null,
  @BeanProperty roleName: String,
  @BeanProperty displayName: String,
  @BeanProperty description: Option[String],
  @BeanProperty permissions: Set[String] = Set(),
  @BeanProperty accessLevels: Map[String, String] = Map(),
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now) extends Entity with AuditSupport {
  def withId(id: String) = copy(id = id)
  def withRoleName(roleName: String) = copy(roleName = roleName)
  def withDisplayName(displayName: String) = copy(displayName = displayName)
  def withDescription(description: Option[String]) = copy(description = description)
  def withPermissions(permissions: Set[String]) = copy(permissions = permissions)
  def withAccessLevels(accessLevels: Map[String, String]) = copy(accessLevels = accessLevels)

  override def equals(that: Any) = {
    that match {
      case r: Role => r.id == id
      case _ => false
    }
  }
}

///////////////////////////////////////////////////////////

case class Environment(
  @BeanProperty id: String,
  @BeanProperty name: String,
  @BeanProperty description: Option[String],
  @BeanProperty systemDatabase: String,
  @BeanProperty analyticsDatabase: String,
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now) extends Entity with AuditSupport {
  def withId(id: String) = copy(id = id)
  def withName(name: String) = modify(name = name)
  def withDescription(description: Option[String]) = modify(description = description)
  def modify(
    name: String = name,
    description: Option[String] = description) = copy(
    name = name,
    description = description,
    modifiedBy = UserContext.current,
    modifiedDate = DateTime.now)
}

case class ExtendedField(
  @BeanProperty fieldType: String,
  @BeanProperty required: Boolean,
  @BeanProperty pattern: Option[String]) {
  def withFieldType(fieldType: String) = copy(fieldType = fieldType)
  def withRequired(required: Boolean) = copy(required = required)
  def withPattern(pattern: Option[String]) = copy(pattern = pattern)
}

///////////////////////////////////////////////////////////

case class Application(
  @BeanProperty id: String,
  @BeanProperty name: String,
  @BeanProperty description: Option[String] = None,
  @BeanProperty applicationUrl: String,
  @BeanProperty applicationImageUrl: Option[String] = None,
  @BeanProperty companyName: String,
  @BeanProperty companyUrl: String,
  @BeanProperty companyImageUrl: Option[String] = None,
  @BeanProperty callbackUris: Set[String] = Set(),
  @BeanProperty planId: Option[String] = None,
  @BeanProperty extended: Map[String, Object] = Map(),
  @BeanProperty externalId: Option[String] = None,
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now,
  @JsonInclude(Include.NON_NULL)@BeanProperty role: Option[String] = None,
  @BeanProperty clientCount: Int = 0) extends Entity with AuditSupport {
  def withId(id: String) = copy(id = id)
  def withName(name: String) = modify(name = name)
  def withDescription(description: Option[String]) = modify(description = description)
  def withApplicationUrl(applicationUrl: String) = modify(applicationUrl = applicationUrl)
  def withApplicationImageUrl(applicationImageUrl: Option[String]) = modify(applicationImageUrl = applicationImageUrl)
  def withCompanyName(companyName: String) = modify(companyName = companyName)
  def withCompanyUrl(companyUrl: String) = modify(companyUrl = companyUrl)
  def withCompanyImageUrl(companyImageUrl: Option[String]) = modify(companyImageUrl = companyImageUrl)
  def withCallbackUris(callbackUris: Set[String]) = modify(callbackUris = callbackUris)
  def addCallbackUri(callbackUri: String) = modify(callbackUris = callbackUris + callbackUri)
  def removeCallbackUri(callbackUri: String) = modify(callbackUris = callbackUris - callbackUri)
  def withPlanId(planId: Option[String]) = modify(planId = planId)
  def withExternalId(externalId: Option[String]) = modify(externalId = externalId)
  def withRole(role: Option[String]) = copy(role = role)
  def withClientCount(clientCount: Int) = copy(clientCount = clientCount)

  def modify(
    name: String = name,
    description: Option[String] = description,
    applicationUrl: String = applicationUrl,
    applicationImageUrl: Option[String] = applicationImageUrl,
    companyName: String = companyName,
    companyUrl: String = companyUrl,
    companyImageUrl: Option[String] = companyImageUrl,
    callbackUris: Set[String] = callbackUris,
    planId: Option[String] = planId,
    extended: Map[String, Object] = extended,
    externalId: Option[String] = externalId) = copy(
    name = name,
    description = description,
    applicationUrl = applicationUrl,
    applicationImageUrl = applicationImageUrl,
    companyName = companyName,
    companyUrl = companyUrl,
    companyImageUrl = companyImageUrl,
    callbackUris = callbackUris,
    planId = planId,
    extended = extended,
    externalId = externalId,
    modifiedBy = UserContext.current,
    modifiedDate = DateTime.now)
}

///////////////////////////////////////////////////////////

case class Configuration(
  @BeanProperty val name: String,
  @BeanProperty val properties: Map[String, List[Any]]) {
  def value[T: Manifest](name: String): Option[T] = {
    properties.get(name) match {
      case Some(property) => if (property.size > 0) {
        val value = property(0)
        val m = manifest[T]
        val c = m.runtimeClass

        if (c == classOf[Boolean] && value.isInstanceOf[Boolean])
          Some(value.asInstanceOf[T])
        else if (c.isInstance(value))
          Some(value.asInstanceOf[T])
        else
          None
      } else None
      case _ => None
    }
  }

  def values[T: Manifest](name: String): List[T] = properties.get(name) match {
    case Some(property) => {
      val builder = List.newBuilder[T]
      val m = manifest[T]

      for (value <- property) {
        if (m.runtimeClass.isInstance(value)) {
          builder += value.asInstanceOf[T]
        }
      }

      builder.result
    }
    case _ => List()
  }
}

trait Key {
  val id: String
  val applicationId: String
  val label: String
  val enabled: Boolean
  val apiKey: String
  val sharedSecret: Option[String]
  val clientPermissionIds: Set[String]
  val authenticators: Map[String, Map[String, List[Any]]]
  val policies: List[Configuration]

  def getAuthenticatorConfiguration(name: String): Option[Configuration] = {
    authenticators.get(name) match {
      case Some(a) => Some(Configuration(name, a))
      case _ => None
    }
  }

  def getPermissionSet(authorizationScheme: String): Option[PermissionSet]

  def isRateLimited: Boolean
}

case class Client(
  @BeanProperty id: String,
  @BeanProperty applicationId: String,
  @BeanProperty label: String,
  @BooleanBeanProperty enabled: Boolean,
  @BeanProperty status: Option[String] = None,
  @BeanProperty apiKey: String,
  @BeanProperty sharedSecret: Option[String] = None,
  @BeanProperty authenticators: Map[String, Map[String, List[Any]]] = Map(),
  @BeanProperty policies: List[Configuration] = List(),
  @BeanProperty claimSources: List[Configuration] = List(),
  @BeanProperty clientPermissionIds: Set[String] = Set(),
  @BeanProperty permissionSets: Map[String, PermissionSet] = Map(),
  @BeanProperty extended: Map[String, Object] = Map(),
  @BeanProperty externalId: Option[String] = None,
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now,
  @JsonInclude(Include.NON_NULL)@BeanProperty application: Option[Application] = None) extends Entity with Key with AuditSupport {
  def withId(id: String) = copy(id = id)
  def withApplicationId(applicationId: String) = copy(applicationId = applicationId)
  def withLabel(label: String) = modify(label = label)
  def withEnabled(enabled: Boolean) = modify(enabled = enabled)
  def withStatus(status: Option[String]) = modify(status = status)
  def withApiKey(apiKey: String) = modify(apiKey = apiKey)
  def withSharedSecret(sharedSecret: Option[String]) = modify(sharedSecret = sharedSecret)
  def withAuthenticators(authenticators: Map[String, Map[String, List[Any]]]) = modify(authenticators = authenticators)
  def withPolicies(policies: List[Configuration]) = modify(policies = policies)
  def withClaimSources(claimSources: List[Configuration]) = modify(claimSources = claimSources)
  def withClientPermissionIds(clientPermissionIds: Set[String]) = modify(clientPermissionIds = clientPermissionIds)
  def addClientPermissionId(permissionId: String) = modify(clientPermissionIds = clientPermissionIds + permissionId)
  def removeClientPermissionId(permissionId: String) = modify(clientPermissionIds = clientPermissionIds - permissionId)
  def withPermissionSets(permissionSets: Map[String, PermissionSet]) = modify(permissionSets = permissionSets)
  def addPermissionSet(name: String, permissionSet: PermissionSet) = modify(permissionSets = permissionSets + (name -> permissionSet))
  def removePermissionSet(name: String) = modify(permissionSets = permissionSets - name)
  def withExternalId(externalId: Option[String]) = modify(externalId = externalId)
  def withApplication(application: Option[Application]) = modify(application = application)
  def redacted = modify(apiKey = "redacted", sharedSecret = Some("redacted"))

  def getPermissionSet(authorizationScheme: String) = permissionSets.get(authorizationScheme)

  def modify(
    label: String = label,
    enabled: Boolean = enabled,
    status: Option[String] = status,
    apiKey: String = apiKey,
    sharedSecret: Option[String] = sharedSecret,
    authenticators: Map[String, Map[String, List[Any]]] = authenticators,
    policies: List[Configuration] = policies,
    claimSources: List[Configuration] = claimSources,
    clientPermissionIds: Set[String] = clientPermissionIds,
    permissionSets: Map[String, PermissionSet] = permissionSets,
    extended: Map[String, Object] = extended,
    externalId: Option[String] = externalId,
    application: Option[Application] = application) = copy(
    label = label,
    enabled = enabled,
    status = status,
    apiKey = apiKey,
    sharedSecret = sharedSecret,
    authenticators = authenticators,
    policies = policies,
    claimSources = claimSources,
    clientPermissionIds = clientPermissionIds,
    permissionSets = permissionSets,
    extended = extended,
    externalId = externalId,
    modifiedBy = UserContext.current,
    modifiedDate = DateTime.now,
    application = application)

  val isRateLimited = true
}

case class PermissionSet(
  @BeanProperty enabled: Boolean,
  @BeanProperty global: Boolean,
  @BeanProperty expiration: Option[Long],
  @BeanProperty lifespan: String, //enum
  @BooleanBeanProperty refreshable: Boolean,
  @BeanProperty permissionIds: Set[String] = Set(),
  @BooleanBeanProperty autoAuthorize: Boolean = false,
  @BooleanBeanProperty allowWebView: Boolean = false,
  @BooleanBeanProperty allowPopup: Boolean = false) {
  def withEnabled(enabled: Boolean) = copy(enabled = enabled)
  def withGlobal(global: Boolean) = copy(global = global)
  def withExpiration(expiration: Option[Long]) = copy(expiration = expiration)
  def withLifespan(lifespan: String) = copy(lifespan = lifespan)
  def withRefreshable(refreshable: Boolean) = copy(refreshable = refreshable)
  def withPermissionIds(permissionIds: Set[String]) = copy(permissionIds = permissionIds)
  def addPermissionId(permissionId: String) = copy(permissionIds = permissionIds + permissionId)
  def removePermissionId(permissionId: String) = copy(permissionIds = permissionIds - permissionId)
  def withAutoAuthorize(autoAuthorize: Boolean) = copy(autoAuthorize = autoAuthorize)
  def withAllowWebView(allowWebView: Boolean) = copy(allowWebView = allowWebView)
  def withAllowPopup(allowPopup: Boolean) = copy(allowPopup = allowPopup)
}

///////////////////////////////////////////////////////////

case class Token(
  @BeanProperty id: String,
  @BeanProperty clientId: String,
  @BeanProperty scheme: Option[String],
  @BeanProperty token: String,
  @BeanProperty refreshToken: Option[String] = None,
  @BeanProperty tokenType: String,
  @BeanProperty grantType: String,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty lastAccessedDate: DateTime = DateTime.now,
  @BeanProperty expiresIn: Option[Long],
  @BeanProperty lifecycle: String, //enum
  @BeanProperty userCode: Option[String] = None,
  @BeanProperty fromToken: Option[String] = None,
  @BeanProperty permissionIds: Set[String] = Set(),
  @BeanProperty state: Option[String] = None,
  @BeanProperty uri: Option[String] = None,
  @BeanProperty userId: Option[String] = None,
  @BeanProperty userContext: Option[String] = None,
  @BeanProperty extended: Map[String, Object] = Map(),
  @BeanProperty externalId: Option[String] = None) extends Entity {
  def withId(id: String) = copy(id = id)

  def isExpired = {
    expiresIn match {
      case Some(timespan) => lastAccessedDate.plus(timespan * 1000).isBeforeNow()
      case _ => false
    }
  }
}

case class ClaimEntry(
  @BeanProperty `type`: String,
  @BeanProperty value: Option[String]) {
  def withType(`type`: String) = copy(`type` = `type`)
  def withValue(value: Option[String]) = copy(value = value)
}

case class Operation(
  @BeanProperty name: String,
  @BeanProperty method: String,
  @BeanProperty uriPattern: String,
  @BeanProperty clientPermissionIds: Set[String],
  @BeanProperty delegatedPermissionIds: Set[String],
  @BeanProperty claims: Set[ClaimEntry]) {
  def withName(name: String) = copy(name = name)
  def withMethod(method: String) = copy(method = method)
  def withUriPattern(uriPattern: String) = copy(uriPattern = uriPattern)
  def withClientPermissionIds(clientPermissionIds: Set[String]) = copy(clientPermissionIds = clientPermissionIds)
  def withDelegatedPermissionIds(delegatedPermissionIds: Set[String]) = copy(delegatedPermissionIds = delegatedPermissionIds)
  def withClaims(claims: Set[ClaimEntry]) = copy(claims = claims)
}

case class OperationMatcher(
  @BeanProperty serviceId: String,
  @BeanProperty serviceName: String,
  @BeanProperty versionLocation: Seq[String],
  @BeanProperty defaultVersion: Option[String],
  @BeanProperty operationName: String,
  @BeanProperty method: String,
  @BeanProperty uriPattern: Regex,
  @BeanProperty patternLength: Int,
  @BeanProperty clientPermissionIds: Set[String],
  @BeanProperty delegatedPermissionIds: Set[String],
  @BeanProperty claims: Set[ClaimEntry])

case class Service(
  @BeanProperty id: String,
  @BeanProperty name: String,
  @BeanProperty `type`: Option[String],
  @BeanProperty description: Option[String],
  @BeanProperty uriPrefix: Option[String] = None,
  @BeanProperty versionLocation: Option[String] = None,
  @BeanProperty defaultVersion: Option[String] = None,
  @BeanProperty hostnames: Set[String] = Set(),
  @BeanProperty requestWeights: Map[String, Int] = Map(),
  @BooleanBeanProperty accessControl: Boolean = false,
  @BeanProperty operations: List[Operation] = List(),
  @BeanProperty flags: Set[String] = Set(),
  @BeanProperty extended: Map[String, Object] = Map(),
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now) extends Entity with AuditSupport {
  def withId(id: String) = copy(id = id)
  def withName(name: String) = copy(name = name)
  def withType(`type`: Option[String]) = copy(`type` = `type`)
  def withDescription(description: Option[String]) = copy(description = description)
  def withUriPrefix(uriPrefix: Option[String]) = copy(uriPrefix = uriPrefix)
  def withVersionLocation(versionLocation: Option[String]) = copy(versionLocation = versionLocation)
  def withDefaultVersion(defaultVersion: Option[String]) = copy(defaultVersion = defaultVersion)
  def withHostnames(hostnames: Set[String]) = copy(hostnames = hostnames)
  def withRequestWeights(requestWeights: Map[String, Int]) = copy(requestWeights = requestWeights)
  def withAccessControl(accessControl: Boolean) = copy(accessControl = accessControl)
  def withOperations(operations: List[Operation]) = copy(operations = operations)
  def withFlags(flags: Set[String]) = copy(flags = flags)
}

object TimeUnit extends Enumeration {
  case class EValue(name: String) extends Val(name) {}

  val MINUTE = EValue("minute")
  val HOUR = EValue("hour")
  val DAY = EValue("day")
  val MONTH = EValue("month")
}

case class Quota(
  @BeanProperty requestCount: Long,
  @BeanProperty timeUnit: String)

case class Plan(
  @BeanProperty id: String,
  @BeanProperty name: String,
  @BeanProperty priceAmount: Option[BigDecimal] = None,
  @BeanProperty priceCurrency: Option[String] = None,
  @BeanProperty quotas: List[Quota],
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now) extends Entity with AuditSupport {
  def withId(id: String) = copy(id = id)
  def withName(name: String) = copy(name = name)
  def withPriceAmount(priceAmount: Option[BigDecimal]) = copy(priceAmount = priceAmount)
  def withPriceCurrency(priceCurrency: Option[String]) = copy(priceCurrency = priceCurrency)
  def withQuotas(quotas: List[Quota]) = copy(quotas = quotas)
}

object PermissionType extends Enumeration {
  case class EValue(name: String) extends Val(name) {}

  val ACTION = EValue("action")
  val ENTITY = EValue("entity")
}

object Permission {
  val validScopes = Set("client", "user", "both")
  val validPermissionTypes = Set("action", "entity")
}

case class Permission(
  @BeanProperty id: String,
  @BeanProperty name: String,
  @BeanProperty `type`: String,
  @BeanProperty description: Option[String],
  @BeanProperty scope: String,
  @BeanProperty messageId: String,
  @BeanProperty flags: Set[String] = Set(),
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now,
  @JsonInclude(Include.NON_NULL)@BeanProperty message: Option[Message] = None) extends Entity with AuditSupport {
  require(Permission.validScopes(scope), "Scope is invalid")
  require(Permission.validPermissionTypes(`type`), "Permission type is invalid")

  def withId(id: String) = copy(id = id)
  def withName(name: String) = copy(name = name)
  def withType(`type`: String) = copy(`type` = `type`)
  def withDescription(description: Option[String]) = copy(description = description)
  def withScope(scope: String) = copy(scope = scope)
  def withMessageId(messageId: String) = copy(messageId = messageId)
  def withFlags(flags: Set[String]) = copy(flags = flags)
  def withMessage(message: Option[Message]) = copy(message = message)
}

case class Message(
  @BeanProperty id: String,
  @BeanProperty parentId: Option[String],
  @BeanProperty key: String,
  @BeanProperty locales: Map[String, MessageContent],
  @BeanProperty displayOrder: Int,
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now) extends Entity with AuditSupport {
  def withId(id: String) = copy(id = id)
  def withParentId(parentId: Option[String]) = copy(parentId = parentId)
  def withParentId(key: String) = copy(key = key)
  def withLocales(locales: Map[String, MessageContent]) = copy(locales = locales)
  def addLocale(locale: String, message: MessageContent) = copy(locales = locales + (locale -> message))
  def removeLocale(locale: String) = copy(locales = locales - locale)
  def withDisplayOrder(displayOrder: Int) = copy(displayOrder = displayOrder)
}

case class MessageContent(
  @BeanProperty format: String,
  @BeanProperty content: String) {
  def withFormat(format: String) = copy(format = format)
  def withContent(content: String) = copy(content = content)
}

case class Provider(
  @BeanProperty id: String,
  @BeanProperty label: String,
  @BooleanBeanProperty enabled: Boolean,
  @BeanProperty apiKey: String,
  @BeanProperty sharedSecret: Option[String],
  @BeanProperty authenticators: Map[String, Map[String, List[Any]]] = Map(),
  @BeanProperty policies: List[Configuration] = List(),
  @BeanProperty permissions: Set[String] = Set(),
  @BeanProperty accessLevels: Map[String, String] = Map(),
  @BooleanBeanProperty behindReverseProxy: Boolean = false,
  @BeanProperty extended: Map[String, Object] = Map(),
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now) extends Entity with Key with Authorization with AuditSupport {
  @BeanProperty val applicationId: String = "provider"
  def withId(id: String) = copy(id = id)
  def withLabel(label: String) = copy(label = label)
  def withApiKey(apiKey: String) = copy(apiKey = apiKey)
  def withSharedSecret(sharedSecret: Option[String]) = copy(sharedSecret = sharedSecret)
  def withAuthenticators(authenticators: Map[String, Map[String, List[Any]]]) = copy(authenticators = authenticators)
  def withPolicies(policies: List[Configuration]) = copy(policies = policies)
  def withPermissions(permissions: Set[String]) = copy(permissions = permissions)
  def withAccessLevels(accessLevels: Map[String, String]) = copy(accessLevels = accessLevels)
  def redacted = copy(apiKey = "redacted", sharedSecret = Some("redacted"))

  def getPermissionSet(authorizationScheme: String) = None
  @BeanProperty val clientPermissionIds: Set[String] = Set()
  @BooleanBeanProperty val administrator = false

  @BeanProperty val name = "Provider " + label

  val isRateLimited = false
}

object LogEntry {
  private val validLevels = Set("trace", "debug", "info", "warn", "error")
}

case class LogEntry(
  @BeanProperty id: String = null,
  @BeanProperty level: String,
  @BeanProperty timestamp: DateTime,
  @BeanProperty message: String) extends Entity {
  require(LogEntry.validLevels(level), "Level is invalid")

  def withId(id: String) = copy(id = id)
  def withTimestamp(timestamp: DateTime) = copy(timestamp = timestamp)
  def withMessage(message: String) = copy(message = message)
}

case class Developer(
  @BeanProperty id: String = null,
  @BeanProperty username: String,
  @BeanProperty firstName: String,
  @BeanProperty lastName: String,
  @BeanProperty roles: Set[String] = Set(),
  @BeanProperty company: Option[String] = None,
  @BeanProperty title: Option[String] = None,
  @BeanProperty email: Option[String] = None,
  @BeanProperty phone: Option[String] = None,
  @BeanProperty mobile: Option[String] = None,
  @BeanProperty address1: Option[String] = None,
  @BeanProperty address2: Option[String] = None,
  @BeanProperty locality: Option[String] = None,
  @BeanProperty region: Option[String] = None,
  @BeanProperty postalCode: Option[String] = None,
  @BeanProperty countryCode: Option[String] = None,
  @BeanProperty registrationIp: Option[String] = None,
  @BeanProperty extended: Map[String, Object] = Map(),
  @BeanProperty externalId: Option[String] = None,
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now,
  @JsonInclude(Include.NON_NULL)@BeanProperty role: Option[String] = None) extends Entity with AuditSupport {
  def withId(id: String) = copy(id = id)
  def withUsername(username: String) = copy(username = username)
  def withFirstName(firstName: String) = copy(firstName = firstName)
  def withLastName(lastName: String) = copy(lastName = lastName)
  def withRoles(roles: Set[String]) = copy(roles = roles)
  def withCompany(company: Option[String]) = copy(company = company)
  def withTitle(title: Option[String]) = copy(title = title)
  def withEmail(email: Option[String]) = copy(email = email)
  def withPhone(phone: Option[String]) = copy(phone = phone)
  def withMobile(mobile: Option[String]) = copy(mobile = mobile)
  def withAddress1(address1: Option[String]) = copy(address1 = address1)
  def withAddress2(address2: Option[String]) = copy(address2 = address2)
  def withLocality(locality: Option[String]) = copy(locality = locality)
  def withRegion(region: Option[String]) = copy(region = region)
  def withPostalCode(postalCode: Option[String]) = copy(postalCode = postalCode)
  def withCountryCode(countryCode: Option[String]) = copy(countryCode = countryCode)
  def withRegistrationIp(registrationIp: Option[String] = None) = copy(registrationIp = registrationIp)
  def withExtended(extended: Map[String, Object]) = copy(extended = extended)
  def withExternalId(externalId: Option[String]) = copy(externalId = externalId)
  def withRole(role: Option[String]) = copy(role = role)
}

case class AppDeveloper(
  @BeanProperty id: String = null,
  @BeanProperty applicationId: String,
  @BeanProperty developerId: String,
  @BeanProperty role: String,
  @JsonInclude(Include.NON_NULL)@BeanProperty application: Option[Application] = None,
  @JsonInclude(Include.NON_NULL)@BeanProperty developer: Option[Developer] = None,
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now) extends Entity with AuditSupport {
  def withId(id: String) = copy(id = id)
  def withApplicationId(applicationId: String) = copy(applicationId = applicationId)
  def withDeveloperId(developerId: String) = copy(developerId = developerId)
  def withRole(role: String) = copy(role = role)
  def withApplication(application: Option[Application]) = copy(application = application)
  def withDeveloper(developer: Option[Developer]) = copy(developer = developer)
}

case class PrincipalProfile(
  @BeanProperty id: String = null,
  @BeanProperty name: String,
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now) extends Entity with AuditSupport {
  def withId(id: String) = copy(id = id)
  def withName(name: String) = copy(name = name)
}

case class PrincipalClaims(
  @BeanProperty id: String = null,
  @BeanProperty profileId: String,
  @BeanProperty name: String,
  @BeanProperty inherits: Set[String] = Set.empty[String],
  @BeanProperty claims: Map[String, Set[String]],
  @BeanProperty createdBy: String = UserContext.current,
  @BeanProperty createdDate: DateTime = DateTime.now,
  @BeanProperty modifiedBy: String = UserContext.current,
  @BeanProperty modifiedDate: DateTime = DateTime.now) extends Entity with AuditSupport {
  def withId(id: String) = copy(id = id)
  def withProfileId(profileId: String) = copy(profileId = profileId)
  def withName(name: String) = copy(name = name)
  def withInherits(inherits: Set[String]) = copy(inherits = inherits)
  def withClaims(claims: Map[String, Set[String]]) = copy(claims = claims)
}

///////////////////////////////////////////////////////////////////////////////

case class LocalMessage(
  @BeanProperty message: String,
  @BeanProperty children: List[LocalMessage] = List()) {
  def withMessage(message: String) = copy(message = message)
  def withChildren(children: List[LocalMessage]) = copy(children = children)
}

case class Error(
  @BeanProperty code: Int,
  @BeanProperty reason: String,
  @BeanProperty message: String,
  @BeanProperty developerMessage: String = null,
  @BeanProperty errorCode: String = null,
  @BeanProperty moreInfo: String = null)

case class Reference(
  @BeanProperty id: String,
  @BeanProperty name: String)

case class ServiceInfo(
  @BeanProperty environment: Reference,
  @JsonInclude(Include.NON_NULL)@BeanProperty service: Option[Reference],
  @JsonInclude(Include.NON_NULL)@BeanProperty provider: Option[Reference])