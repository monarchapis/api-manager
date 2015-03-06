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

import java.io.InputStream

import scala.collection.JavaConversions._

import org.apache.commons.lang3.StringUtils

import com.monarchapis.apimanager.exception.NotFoundException
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._

import grizzled.slf4j.Logging
import javax.ws.rs._
import javax.ws.rs.core._

trait ResourceSupport extends Logging {
  val resourceName: String
  val ignore = Set("offset", "limit", "filter", "orderBy", "expand", "_")

  def toFilter(uriInfo: UriInfo) = {
    val map = uriInfo.getQueryParameters
    toMap(map)
  }

  def toMap(map: MultivaluedMap[String, String]): Map[String, List[String]] = {
    val filter = Map.newBuilder[String, List[String]]

    map foreach {
      case (key, value) => {
        if (!ignore.contains(key)) {
          val builder = List.newBuilder[String]
          for (v1 <- value.iterator) {
        	  builder ++= StringUtils.split(v1, '|') map (v => v.trim)
          }
          
          filter += key -> builder.result
        }
      }
    }

    filter.result
  }

  def toOrderBy(uriInfo: UriInfo) = {
    val orderBy = List.newBuilder[OrderByField]
    val values = uriInfo.getQueryParameters.get("orderBy")

    values match {
      case a: java.util.List[String] => {
        a foreach (f => {
          val parts = f.split(',')

          parts foreach (part => {
            val nParts = StringUtils.split(part.trim, " \t\r\n")

            nParts.length match {
              case 1 => orderBy += OrderByField(nParts(0), OrderDirection.ASCENDING)
              case 2 => {
                val field = nParts(0)
                val dir = nParts(1)

                dir.toUpperCase() match {
                  case "ASC" => orderBy += OrderByField(field, OrderDirection.ASCENDING)
                  case "DESC" => orderBy += OrderByField(field, OrderDirection.DESCENDING)
                  case _ =>
                }
              }
              case _ =>
            }
          })
        })
      }
      case _ =>
    }

    orderBy.result
  }

  def toExpand(value: String): Set[String] = {
    if (value != null) {
      val split = StringUtils.split(value, ", ")
      split.toSet
    } else {
      Set.empty[String]
    }
  }
}

abstract class Resource[T <: Entity] extends ResourceSupport {
  val service: BaseService[T]

  val entityClass: Class[T]

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def collection(
    @Context uriInfo: UriInfo,
    @QueryParam("offset")@DefaultValue("0") offset: Integer,
    @QueryParam("limit")@DefaultValue("10") limit: Integer,
    @QueryParam("expand") expand: String): PagedList[T] = {
    service.find(offset, limit, toFilter(uriInfo), toOrderBy(uriInfo), toExpand(expand))
  }

  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def create(
    entity: T): T = {
    service.create(entity)
  }

  @Path("/{id}")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def load(@PathParam("id") id: String) = {
    val resource = service.load(id)
    if (resource.isEmpty) throw new NotFoundException(s"Could not find $resourceName $id")
    resource.get
  }

  @Path("/multi/{ids}")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def load(
    @Context uriInfo: UriInfo,
    @PathParam("ids") ids: String,
    @QueryParam("offset")@DefaultValue("0") offset: Integer,
    @QueryParam("limit")@DefaultValue("10") limit: Integer,
    @QueryParam("expand") expand: String): PagedList[T] = {
    val idSet = toExpand(ids)
    service.loadSet(idSet, offset, limit, toFilter(uriInfo), toOrderBy(uriInfo))
  }

  /*
  @Path("/{id}")
  @PUT
  @Consumes(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def update(@PathParam("id") id: String, entity: T) = {
    val resource = service.update(entity.withId(id).asInstanceOf[T])
    if (resource.isEmpty) throw new WebApplicationException(Response.Status.NOT_FOUND)
    resource.get
  }
  */

  @Path("/{id}") //@HttpMethod("PATCH")
  @PUT
  @Consumes(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def patch(
    @PathParam("id") id: String,
    body: InputStream) = {
    val delta = new JsonDelta[T](id, body, entityClass)
    val resource = service.update(delta)
    if (resource.isEmpty) throw new NotFoundException(s"Could not find $resourceName $id")
    resource.get
  }

  @Path("/{id}")
  @DELETE
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_XML))
  def delete(@PathParam("id") id: String) = {
    val resource = service.load(id)
    if (resource.isEmpty) throw new NotFoundException(s"Could not find $resourceName $id")
    val success = service.delete(id)
    if (!success) throw new NotFoundException(s"Could not find $resourceName $id")
    resource.get
  }
}