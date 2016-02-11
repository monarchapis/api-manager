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

package com.monarchapis.apimanager.service

import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.security.AuthenticationRequest
import com.monarchapis.apimanager.security.AuthenticationResponse
import com.monarchapis.apimanager.security.AuthorizationDetails
import com.monarchapis.apimanager.security.AuthorizationRequest
import com.monarchapis.apimanager.security.ClientAuthenticationRequest
import com.monarchapis.apimanager.security.CreateTokenRequest
import com.monarchapis.apimanager.security.MessageDetails
import com.monarchapis.apimanager.security.MessageDetailsList
import com.monarchapis.apimanager.security.PermissionMessagesRequest
import com.monarchapis.apimanager.security.TokenDetails

object OrderDirection extends Enumeration {
  case class EValue(name: String) extends Val(name) {}

  val ASCENDING = EValue("ascending")
  val DESCENDING = EValue("descending")
}

trait Delta[T <: Entity] {
  val entity: T
  def pathChanged(path: String): Boolean
  def changedKeys: Set[String]
}

case class OrderByField(
  val field: String,
  val direction: OrderDirection.Value)

trait BaseService[T <: Entity] {
  def load(id: String): Option[T]
  def loadSet(ids: Set[String],
    offset: Integer = 0,
    limit: Integer = 10,
    filter: Map[String, List[String]] = Map(),
    orderBy: List[OrderByField] = List()): PagedList[T]
  def loadMap(ids: Set[String]): Map[String, T]
  def count(
    filter: Map[String, List[String]] = Map()): Int
  def find(
    offset: Integer = 0,
    limit: Integer = 10,
    filter: Map[String, List[String]] = Map(),
    orderBy: List[OrderByField] = List(),
    expand: Set[String] = Set()): PagedList[T]
  def exists(filter: Map[String, List[String]] = Map()): Boolean
  def exists(ids: Set[String]): Boolean
  def exists(id: String): Boolean
  def create(entity: T): T
  def update(entity: T): Option[T]
  def update(delta: Delta[T]): Option[T]
  def delete(entity: T): Boolean
  def delete(id: String): Boolean
}

trait DisplayLabel {
  def getDisplayLabels(ids: Set[String]): Map[String, String]
}

object DisplayLabelSources {
  val lookup = new scala.collection.mutable.HashMap[String, DisplayLabel]
}

trait KeyStore extends {
  def findByApiKey(apiKey: String): Option[Key]
}

trait UserService extends BaseService[User] {
  def setAdmininstrator(userId: String, administrator: Boolean)
  def findByName(userName: String): Option[User]
  def findByExternalId(externalId: String): Option[User]
}

trait AuthenticationService {
  def authenticate(userName: String, password: String): Option[AuthenticatedUser]
  def isLocal: Boolean = false
  def setPassword(userName: String, password: String)
}

trait RoleService extends BaseService[Role] {
  def findByName(roleName: String): Option[Role]
  def getUserRole(user: User): Option[Role]
  def setUserRole(user: User, roleId: Option[String]): Boolean
  def getMembers(role: Role): Set[String]
}

case class EnvironmentDatabases(system: String, analytics: String)

trait EnvironmentService extends BaseService[Environment] {
  def lookupIdByName(name: String): Option[String]
  def getDatabases(id: String): Option[EnvironmentDatabases]
  def setAuthorizedUser(id: String, user: User, authorized: Boolean): Boolean
  def setDeveloperExtendedFields(id: String, fields: Map[String, Option[ExtendedField]]): Boolean
  def getDeveloperExtendedFields(id: String): Map[String, ExtendedField]
  def setApplicationExtendedFields(id: String, fields: Map[String, Option[ExtendedField]]): Boolean
  def getApplicationExtendedFields(id: String): Map[String, ExtendedField]
  def getUserEnvironments(userId: String): Set[String]
  def hasAccess(id: String): Boolean
}

trait ApplicationService extends BaseService[Application] with DisplayLabel {
}

trait ClientService extends BaseService[Client] with KeyStore with DisplayLabel {
  def findByApiKey(apiKey: String): Option[Client]
  def authenticate(apiKey: String, sharedSecret: String): Option[Client]
  def lookupId(apiKey: String): Option[String]
  def setPermissionSets(id: String, fields: Map[String, Option[PermissionSet]]): Boolean
}

trait TokenService extends BaseService[Token] {
  def findByToken(token: String): Option[Token]
  def findByRefresh(token: String): Option[Token]
  def touch(token: Token)
}

trait ServiceService extends BaseService[Service] with DisplayLabel {
  def lookupIdByName(name: String): Option[String]
  def getAccessControlled: List[Service]
}

trait PermissionService extends BaseService[Permission] {
  def findByName(name: String): Option[Permission]
  def getPermissionNames(permissionIds: Set[String], scope: String): Set[String]
  def getPermissionIds(permissionNames: Set[String], scope: String): Set[String]
}

trait MessageService extends BaseService[Message] {
  def findByParent(parentId: String): List[Message]
  def findByKey(key: String): Option[Message]
}

trait ProviderService extends BaseService[Provider] with KeyStore with DisplayLabel {
  def findByApiKey(apiKey: String): Option[Provider]
  def lookupIdByLabel(name: String): Option[String]
}

trait DeveloperService extends BaseService[Developer] {
  def authenticate(userName: String, password: String): Option[Developer]
  def setPassword(developer: Developer, password: String)
  def findByUsername(username: String): Option[Developer]
}

trait AppDeveloperService extends BaseService[AppDeveloper] {
  def associate(applicationId: String, developerId: String, role: String)
  def remove(applicationId: String, developerId: String): Boolean
  def findByDeveloperId(developerId: String): List[AppDeveloper]
  def findByApplicationId(applicationId: String): List[AppDeveloper]
  def find(developerId: String, applicationId: String): Option[AppDeveloper]
}

trait LogService extends BaseService[LogEntry] {
  def log(level: String, message: String)
  def getLogsEnties(offset: Int = 0, limit: Int = 25): List[LogEntry]
}

trait PlanService extends BaseService[Plan] {
  def findByName(name: String): Option[Plan]
}

trait RateLimitService {
  def checkQuotas(applicationId: String, requestWeight: Option[BigDecimal], quotas: List[Quota])
}

trait PrincipalProfileService extends BaseService[PrincipalProfile] {
  def findByName(name: String): Option[PrincipalProfile]
}

trait PrincipalClaimsService extends BaseService[PrincipalClaims] {
  def findByName(profileId: String, name: String): Option[PrincipalClaims]
}

trait ServiceManager {
  @throws(classOf[BadRequestException])
  def getServiceInfo(environmentName: String, serviceName: Option[String], providerKey: Option[String]): ServiceInfo
  def lookupEnvironmentIdByName(name: String): Option[String]
  def lookupServiceIdByName(name: String): Option[String]
  def authenticate(request: AuthenticationRequest): AuthenticationResponse
  def getAuthorizationDetails(request: AuthorizationRequest): AuthorizationDetails
  def authenticateClient(request: ClientAuthenticationRequest): Boolean
  def createToken(request: CreateTokenRequest): TokenDetails

  def getToken(
    apiKey: String,
    token: String,
    callbackUri: Option[String]): TokenDetails

  def getTokenByRefresh(
    apiKey: String,
    token: String,
    callbackUri: Option[String]): TokenDetails

  def revokeToken(
    apiKey: String,
    token: String,
    callbackUri: Option[String])

  def getPermissionMessages(request: PermissionMessagesRequest): MessageDetailsList
}

trait LoadBalancer {
  def getTarget(service: Service, request: AuthenticationRequest, claims: Map[String, Any]): Option[String]
}

class EntityEventAggregator[T]() {
  private var invocationList: List[(T, String) => Unit] = Nil

  def apply(entity: T, eventType: String) {
    invocationList foreach { invoker => invoker(entity, eventType) }
  }

  def +=(invoker: (T, String) => Unit) {
    invocationList = invoker :: invocationList
  }

  def -=(invoker: (T, String) => Unit) {
    invocationList = invocationList filter ((x: (T, String) => Unit) => (x != invoker))
  }

  def listenerCount = invocationList.size
}

object EntityEventAggregator {
  val application = new EntityEventAggregator[Application]
  val client = new EntityEventAggregator[Client]
  val token = new EntityEventAggregator[Token]
  val developer = new EntityEventAggregator[Developer]
  val appDeveloper = new EntityEventAggregator[AppDeveloper]
  val service = new EntityEventAggregator[Service]
  val plan = new EntityEventAggregator[Plan]
  val provider = new EntityEventAggregator[Provider]
  val permission = new EntityEventAggregator[Permission]
  val message = new EntityEventAggregator[Message]
  val environment = new EntityEventAggregator[Environment]
  val user = new EntityEventAggregator[User]
  val role = new EntityEventAggregator[Role]
  val logEntry = new EntityEventAggregator[LogEntry]
  val principalProfile = new EntityEventAggregator[PrincipalProfile]
  val principalClaims = new EntityEventAggregator[PrincipalClaims]
}