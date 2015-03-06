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

package com.monarchapis.apimanager.rest.open

import javax.inject.Inject
import javax.inject.Named
import javax.ws.rs._
import com.monarchapis.apimanager.exception.NotFoundException
import com.monarchapis.apimanager.service._
import com.monarchapis.apimanager.rest.service.IdWrapperResponse
import com.monarchapis.apimanager.rest.AbstractDocumentationResource

@Path("/v1")
@Named
class OpenApiResource @Inject() (serviceManager: ServiceManager) {
  require(serviceManager != null, "serviceManager is required")

  @Path("/serviceInfo")
  @GET
  def getServiceInfo(
    @QueryParam("environmentName") environmentName: String,
    @QueryParam("serviceName") serviceName: String,
    @QueryParam("providerKey") providerKey: String) = {
    require(environmentName != null, "environmentName is a required parameter")

    serviceManager.getServiceInfo(environmentName,
      serviceName = Option(serviceName),
      providerKey = Option(providerKey))
  }
}

@Path("/v1")
@Named
class OpenDocumentationResource extends AbstractDocumentationResource("V1")