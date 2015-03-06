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

package com.monarchapis.apimanager.rest.management

import scala.beans.BeanProperty
import scala.beans.BooleanBeanProperty
import org.apache.commons.lang3.RandomStringUtils
import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.model.{ Application => App }
import com.monarchapis.apimanager.rest.AbstractDocumentationResource
import com.monarchapis.apimanager.rest.common.Resource
import com.monarchapis.apimanager.security.AuthenticatorRegistry
import com.monarchapis.apimanager.security.PolicyRegistry
import com.monarchapis.apimanager.security.PropertyDescriptor
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.util._
import javax.inject.Inject
import javax.inject.Named
import javax.ws.rs._
import javax.ws.rs.core._
import com.monarchapis.apimanager.exception.NotAuthorizedException
import com.monarchapis.apimanager.exception.NotFoundException
import com.monarchapis.apimanager.security.ClaimSourceRegistry

case class SetPassword(password: String)
case class Result(@BeanProperty result: String)

@Path("/v1/users")
@Named
class UserResource @Inject() (
  userService: UserService,
  authenticationService: AuthenticationService) extends Resource[User] {
  require(userService != null, "userService is required")

  val resourceName = "user"
  val entityClass = classOf[User]
  val service = userService

  @Path("/{id}/password")
  @PUT
  def setPassword(
    @PathParam("id") id: String,
    password: SetPassword) = {
    if (authenticationService.isLocal) {
      userService.load(id) match {
        case Some(user) => {
          authenticationService.setPassword(user.userName, password.password)
          Result("Password set successfully")
        }
        case _ => Result("Could not set password")
      }
    } else {
      Result("Passwords are stored externally and cannot be set")
    }
  }
}

@Path("/v1/roles")
@Named
class RoleResource @Inject() (roleService: RoleService) extends Resource[Role] {
  require(roleService != null, "roleService is required")

  val resourceName = "role"
  val entityClass = classOf[Role]
  val service = roleService
}

@Path("/v1/environments")
@Named
class EnvironmentResource @Inject() (
  environmentService: EnvironmentService,
  applicationService: ApplicationService,
  clientService: ClientService,
  developerService: DeveloperService) extends Resource[Environment] {
  require(environmentService != null, "environmentService is required")

  val resourceName = "environment"
  val entityClass = classOf[Environment]
  val service = environmentService
}

@Path("/v1/applications")
@Named
class ApplicationResource @Inject() (
  applicationService: ApplicationService,
  clientService: ClientService,
  developerService: DeveloperService,
  appDeveloperService: AppDeveloperService) extends Resource[App] {
  require(applicationService != null, "applicationService is required")
  require(clientService != null, "clientService is required")
  require(developerService != null, "developerService is required")
  require(appDeveloperService != null, "appDeveloperService is required")

  val resourceName = "application"
  val entityClass = classOf[App]
  val service = applicationService

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  override def collection(
    @Context uriInfo: UriInfo,
    @QueryParam("offset")@DefaultValue("0") offset: Integer,
    @QueryParam("limit")@DefaultValue("10") limit: Integer,
    @QueryParam("expand") expand: String): PagedList[App] = {
    val params = uriInfo.getQueryParameters
    val filter = toFilter(uriInfo)
    val orderBy = toOrderBy(uriInfo)
    val expandSet = toExpand(expand)

    if (params != null && params.containsKey("withRolesFor")) {
      val developerId = params.getFirst("withRolesFor")

      val ret = service.find(offset, limit, filter - "withRolesFor", orderBy, expandSet)
      val applications = ret.items

      val idSet = applications.map(application => application.id).toList
      val appDevelopers = appDeveloperService.find(0, limit, Map("developerId" -> List(developerId), "applicationId" -> idSet))
      val appDevLookup = appDevelopers.items map { appDev => (appDev.applicationId, appDev) } toMap
      var builder = List.newBuilder[App]

      applications.foreach(application => {
        appDevLookup.get(application.id) match {
          case Some(appDev) => builder += application.withRole(Some(appDev.role))
          case _ => builder += application
        }
      })

      PagedList(ret.offset, ret.limit, ret.count, ret.total, builder.result)
    } else {
      service.find(offset, limit, filter, orderBy, expandSet)
    }
  }

  @Path("/{id}/clients")
  @GET
  def getClients(
    @Context uriInfo: UriInfo,
    @PathParam("id") id: String,
    @QueryParam("offset")@DefaultValue("0") offset: Integer,
    @QueryParam("limit")@DefaultValue("10") limit: Integer,
    @QueryParam("expand") expand: String): PagedList[Client] = {
    var filter = toFilter(uriInfo)
    filter = filter + ("applicationId" -> List(id))
    clientService.find(offset, limit, filter, toOrderBy(uriInfo), toExpand(expand))
  }

  @Path("/{id}/developers")
  @GET
  def getDevelopers(
    @Context uriInfo: UriInfo,
    @PathParam("id") id: String,
    @QueryParam("offset")@DefaultValue("0") offset: Integer,
    @QueryParam("limit")@DefaultValue("10") limit: Integer): PagedList[Developer] = {
    val filter = toFilter(uriInfo)

    val appDevelopers = appDeveloperService.findByApplicationId(id)
    val idSet = appDevelopers.map(developerApp => developerApp.developerId).toSet
    val developers = developerService.loadSet(idSet, offset, limit, filter, toOrderBy(uriInfo))
    val developerLookup = developers.items map { developer => (developer.id, developer) } toMap

    var builder = List.newBuilder[Developer]

    appDevelopers.foreach(developerApp => {
      developerLookup.get(developerApp.developerId) match {
        case Some(developer) => builder += developer.withRole(Some(developerApp.role))
        case _ =>
      }
    })

    PagedList(offset, limit, developers.count, developers.total, builder.result)
  }

  @Path("/{id}/developers/{developerId}")
  @GET
  def getDeveloper(
    @PathParam("id") applicationId: String,
    @PathParam("developerId") developerId: String): Developer = {
    appDeveloperService.find(applicationId = applicationId, developerId = developerId) match {
      case Some(developerApp) => {
        developerService.load(developerApp.developerId) match {
          case Some(developer) => developer.withRole(Some(developerApp.role))
          case _ => throw new NotFoundException(s"Could not find application developer ${developerApp.developerId}");
        }
      }
      case _ => throw new NotFoundException(s"Could not find application developer");
    }
  }

  @Path("/{id}/developers/{developerId}")
  @PUT
  def addDeveloper(
    @PathParam("id") applicationId: String,
    @PathParam("developerId") developerId: String,
    appDeveloper: AppDeveloper) = {
    var developer = developerService.load(developerId) match {
      case Some(developer) => developer.withRole(Some(appDeveloper.role))
      case _ => throw new NotFoundException(s"Could not find developer $developerId")
    }

    appDeveloperService.associate(
      applicationId = applicationId,
      developerId = developerId,
      role = appDeveloper.role)

    developer
  }

  @Path("/{id}/developers/{developerId}")
  @DELETE
  def deleteApplication(
    @PathParam("id") applicationId: String,
    @PathParam("developerId") developerId: String) = {
    var success = appDeveloperService.remove(
      applicationId = applicationId,
      developerId = developerId)

    if (success) Result("success") else throw new NotFoundException(s"Could not find application developer")
  }
}

@Path("/v1/clients")
@Named
class ClientResource @Inject() (clientService: ClientService) extends Resource[Client] {
  require(clientService != null, "clientService is required")

  val resourceName = "client"
  val entityClass = classOf[Client]
  val service = clientService

  @Path("/{id}/permissionSets")
  @PUT
  def setPermissionSets(
    @PathParam("id") id: String,
    permissionSets: Map[String, Option[PermissionSet]]): Option[Client] = {
    val updated = clientService.setPermissionSets(id, permissionSets)
    if (updated) clientService.load(id) else None
  }
}

@Path("/v1/tokens")
@Named
class TokenResource @Inject() (tokenService: TokenService) extends Resource[Token] {
  require(tokenService != null, "tokenService is required")

  val resourceName = "token"
  val entityClass = classOf[Token]
  val service = tokenService
}

@Path("/v1/services")
@Named
class ServiceResource @Inject() (serviceService: ServiceService) extends Resource[Service] {
  require(serviceService != null, "serviceService is required")

  val resourceName = "service"
  val entityClass = classOf[Service]
  val service = serviceService
}

@Path("/v1/plans")
@Named
class PlanResource @Inject() (planService: PlanService) extends Resource[Plan] {
  require(planService != null, "planService is required")

  val resourceName = "plan"
  val entityClass = classOf[Plan]
  val service = planService
}

@Path("/v1/permissions")
@Named
class PermissionResource @Inject() (permissionService: PermissionService) extends Resource[Permission] {
  require(permissionService != null, "permissionService is required")

  val resourceName = "permission"
  val entityClass = classOf[Permission]
  val service = permissionService
}

@Path("/v1/messages")
@Named
class MessageResource @Inject() (messageService: MessageService) extends Resource[Message] {
  require(messageService != null, "messageService is required")

  val resourceName = "message"
  val entityClass = classOf[Message]
  val service = messageService
}

@Path("/v1/providers")
@Named
class ProvidersResource @Inject() (providerService: ProviderService) extends Resource[Provider] {
  require(providerService != null, "providerService is required")

  val resourceName = "provider"
  val entityClass = classOf[Provider]
  val service = providerService
}

@Path("/v1/developers")
@Named
class DeveloperResource @Inject() (
  developerService: DeveloperService,
  appDeveloperService: AppDeveloperService,
  applicationService: ApplicationService) extends Resource[Developer] {
  require(developerService != null, "developerService is required")
  require(appDeveloperService != null, "appDeveloperService is required")

  val resourceName = "developer"
  val entityClass = classOf[Developer]
  val service = developerService

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  override def collection(
    @Context uriInfo: UriInfo,
    @QueryParam("offset")@DefaultValue("0") offset: Integer,
    @QueryParam("limit")@DefaultValue("10") limit: Integer,
    @QueryParam("expand") expand: String): PagedList[Developer] = {
    val params = uriInfo.getQueryParameters
    val filter = toFilter(uriInfo)
    val orderBy = toOrderBy(uriInfo)
    val expandSet = toExpand(expand)

    if (params != null && params.containsKey("withRolesFor")) {
      val applicationId = params.getFirst("withRolesFor")

      val ret = service.find(offset, limit, filter - "withRolesFor", orderBy, expandSet)
      val developers = ret.items

      val idSet = developers.map(developer => developer.id).toList
      val appDevelopers = appDeveloperService.find(0, limit, Map("applicationId" -> List(applicationId), "developerId" -> idSet))
      val appDevLookup = appDevelopers.items map { appDev => (appDev.developerId, appDev) } toMap
      var builder = List.newBuilder[Developer]

      developers.foreach(developer => {
        appDevLookup.get(developer.id) match {
          case Some(appDev) => builder += developer.withRole(Some(appDev.role))
          case _ => builder += developer
        }
      })

      PagedList(ret.offset, ret.limit, ret.count, ret.total, builder.result)
    } else {
      service.find(offset, limit, filter, orderBy, expandSet)
    }
  }

  @Path("/authenticate")
  @POST
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def authenticate(@FormParam("username") username: String, @FormParam("password") password: String) = {
    val developer = developerService.authenticate(username, password)
    if (developer.isEmpty) throw new NotAuthorizedException(s"Could not authenticate developer")
    developer.get
  }

  @Path("/{id}/password")
  @PUT
  def setPassword(
    @PathParam("id") id: String,
    password: SetPassword) = {
    developerService.load(id) match {
      case Some(developer) => {
        developerService.setPassword(developer, password.password)
        Result("Password set successfully")
      }
      case _ => Result("Could not set password")
    }
  }

  @Path("/{id}/applications")
  @GET
  def getApplications(
    @Context uriInfo: UriInfo,
    @PathParam("id") id: String,
    @QueryParam("offset")@DefaultValue("0") offset: Integer,
    @QueryParam("limit")@DefaultValue("10") limit: Integer): PagedList[App] = {
    val filter = toFilter(uriInfo)

    val appDevelopers = appDeveloperService.findByDeveloperId(id)
    val idSet = appDevelopers.map(developerApp => developerApp.applicationId).toSet
    val applications = applicationService.loadSet(idSet, offset, limit, filter, toOrderBy(uriInfo))
    val applicationLookup = applications.items map { application => (application.id, application) } toMap
    var builder = List.newBuilder[App]

    appDevelopers.foreach(developerApp => {
      applicationLookup.get(developerApp.applicationId) match {
        case Some(app) => builder += app.withRole(Some(developerApp.role))
        case _ =>
      }
    })

    PagedList(offset, limit, applications.count, applications.total, builder.result)
  }

  @Path("/{id}/applications/{applicationId}")
  @GET
  def getApplication(
    @PathParam("id") developerId: String,
    @PathParam("applicationId") applicationId: String): App = {
    appDeveloperService.find(applicationId = applicationId, developerId = developerId) match {
      case Some(developerApp) => {
        applicationService.load(developerApp.applicationId) match {
          case Some(application) => application.withRole(Some(developerApp.role))
          case _ => throw new NotFoundException(s"Could not find application developer ${developerApp.applicationId}");
        }
      }
      case _ => throw new NotFoundException(s"Could not find application developer");
    }
  }

  @Path("/{id}/applications/{applicationId}")
  @PUT
  def addApplication(
    @PathParam("id") developerId: String,
    @PathParam("applicationId") applicationId: String,
    appDeveloper: AppDeveloper) = {
    var application = applicationService.load(applicationId) match {
      case Some(application) => application.withRole(Some(appDeveloper.role))
      case _ => throw new NotFoundException(s"Could not find application $applicationId")
    }

    appDeveloperService.associate(
      applicationId = applicationId,
      developerId = developerId,
      role = appDeveloper.role)

    application
  }

  @Path("/{id}/applications/{applicationId}")
  @DELETE
  def deleteApplication(
    @PathParam("id") developerId: String,
    @PathParam("applicationId") applicationId: String) = {
    var success = appDeveloperService.remove(
      applicationId = applicationId,
      developerId = developerId)

    if (success) Result("success") else throw new NotFoundException(s"Could not find application developer")
  }
}

@Path("/v1/logEntries")
@Named
class LogEntryResource @Inject() (logService: LogService) extends Resource[LogEntry] {
  require(logService != null, "logService is required")

  val resourceName = "logEntry"
  val entityClass = classOf[LogEntry]
  val service = logService
}

case class NamesWrapperResponse[T](@BeanProperty names: Seq[T])
case class ItemsWrapperResponse[T](@BeanProperty items: Seq[T])
case class ValuesWrapperResponse[T](@BeanProperty values: Seq[T])

case class ConfigurationDescriptor(
  @BeanProperty name: String,
  @BeanProperty displayName: String,
  @BeanProperty properties: List[PropertyDescriptor])

@Path("/v1/authenticators")
@Named
class AuthenticatorsResource @Inject() (authenticatorRegistry: AuthenticatorRegistry) {
  require(authenticatorRegistry != null, "authenticatorRegistry is required")

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def getAuthenticators() = ItemsWrapperResponse(authenticatorRegistry.names map { a =>
    {
      val authenticator = authenticatorRegistry(a).get
      val propertyDescriptors = authenticator.propertyDescriptors
      ConfigurationDescriptor(a, authenticator.displayName, propertyDescriptors)
    }
  })
}

@Path("/v1/policies")
@Named
class PoliciesResource @Inject() (policyRegistry: PolicyRegistry) {
  require(policyRegistry != null, "policyRegistry is required")

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def getPolicies() = ItemsWrapperResponse(policyRegistry.names map { a =>
    {
      val policy = policyRegistry(a).get
      val propertyDescriptors = policy.propertyDescriptors
      ConfigurationDescriptor(a, policy.displayName, propertyDescriptors)
    }
  })
}

@Path("/v1/claimSources")
@Named
class ClaimSourcesResource @Inject() (claimSourceRegistry: ClaimSourceRegistry) {
  require(claimSourceRegistry != null, "claimSourceRegistry is required")

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def getClaimSources() = ItemsWrapperResponse(claimSourceRegistry.names map { a =>
    {
      val claimSource = claimSourceRegistry(a).get
      val propertyDescriptors = claimSource.propertyDescriptors
      ConfigurationDescriptor(a, claimSource.displayName, propertyDescriptors)
    }
  })
}

case class EnvironmentSummary(
  name: String,
  applications: Long,
  clients: Long,
  developers: Long,
  services: Long,
  plans: Long,
  providers: Long,
  users: Long,
  roles: Long)

@Path("/v1/environment/summary")
@Named
class EnvironmentInfoResource @Inject() (
  environmentService: EnvironmentService,
  applicationService: ApplicationService,
  clientSerivce: ClientService,
  developerService: DeveloperService,
  serviceService: ServiceService,
  planService: PlanService,
  providerService: ProviderService,
  userService: UserService,
  roleService: RoleService) {
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def getEnvironmentInfo() = {
    AuthorizationUtils.continueWithSystemAccess
    val envId = EnvironmentContext.current.id;

    environmentService.load(envId) match {
      case Some(e) => EnvironmentSummary(
        name = e.name,
        applications = applicationService.count(),
        clients = clientSerivce.count(),
        developers = developerService.count(Map("environmentId" -> List(envId))),
        services = serviceService.count(),
        plans = planService.count(),
        providers = providerService.count(),
        users = userService.count(),
        roles = roleService.count())
      case _ => throw new NotFoundException(s"Could not find environment $envId");
    }
  }
}

case class ValueWrapperResponse[T](@BeanProperty value: T)

@Path("/v1/generateRandomString")
@Named
class GenerateTokenResource {
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def generateRandomString(@QueryParam("length")@DefaultValue("24") length: Int) = {
    ValueWrapperResponse(RandomStringUtils.randomAlphanumeric(length))
  }
}

case class AuthorizationResponse(
  @BeanProperty name: String,
  @BooleanBeanProperty administrator: Boolean,
  @BeanProperty permissions: Set[String],
  @BeanProperty accessLevels: Map[String, String],
  @BeanProperty usersLocked: Boolean)

@Path("/v1/me")
@Named
class MeResource @Inject() (
  permissionService: PermissionService,
  authenticationService: AuthenticationService) {
  @Path("/permissions")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getPermissions() = {
    AuthorizationHolder.get match {
      case Some(authorization) => AuthorizationResponse(
        name = authorization.name,
        administrator = authorization.administrator,
        permissions = authorization.permissions,
        accessLevels = authorization.accessLevels,
        usersLocked = !authenticationService.isLocal)
      case _ => throw new NotAuthorizedException("You do not have permissions in this environment")
    }
  }
}

@Path("/v1/principalProfiles")
@Named
class PrincipalProfileResource @Inject() (principalProfileService: PrincipalProfileService) extends Resource[PrincipalProfile] {
  require(principalProfileService != null, "principalProfileService is required")

  val resourceName = "principalProfile"
  val entityClass = classOf[PrincipalProfile]
  val service = principalProfileService
}

@Path("/v1/principalClaims")
@Named
class PrincipalClaimsResource @Inject() (principalClaimsService: PrincipalClaimsService) extends Resource[PrincipalClaims] {
  require(principalClaimsService != null, "principalClaimsService is required")

  val resourceName = "principalClaims"
  val entityClass = classOf[PrincipalClaims]
  val service = principalClaimsService
}

@Path("/v1")
@Named
class ManagementDocumentationResource extends AbstractDocumentationResource("V1")