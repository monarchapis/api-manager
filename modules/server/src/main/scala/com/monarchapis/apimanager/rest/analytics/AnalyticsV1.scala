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

package com.monarchapis.apimanager.rest.analytics

import org.joda.time.DateTime
import org.springframework.core.task.TaskExecutor

import com.fasterxml.jackson.databind.node.ObjectNode
import com.monarchapis.apimanager.analytics.AnalyticsService
import com.monarchapis.apimanager.exception._
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.rest.AbstractDocumentationResource

import javax.inject.Inject
import javax.inject.Named
import javax.ws.rs._
import javax.ws.rs.core._

object DateTimeLongParse {
  def unapply(s: String): Option[DateTime] = try {
    Some(new DateTime(s.toLong * 1000))
  } catch {
    case _: java.lang.NumberFormatException => None
  }
}

object DateTimeStringParse {
  def unapply(s: String): Option[DateTime] = try {
    Some(DateTime.parse(s))
  } catch {
    case _: java.lang.Exception => None
  }
}

object AnalyticsResource {
  private def parseDate(value: String) = value match {
    case null => DateTime.now
    case DateTimeStringParse(value) => value
    case DateTimeLongParse(value) => value
    case _ => throw new InvalidParamaterException("Date must be in ISO8601 or second format")
  }
}

@Path("/v1")
@Named
class AnalyticsResource @Inject() (analyticsService: AnalyticsService, taskExecutor: TaskExecutor) {
  require(analyticsService != null, "analyticsService is required")

  import AnalyticsService._
  import AnalyticsResource._

  @GET
  def event = {
    analyticsService.event
  }

  @Path("/{eventType}")
  @GET
  def event(@PathParam("eventType") eventType: String) = {
    analyticsService.event(eventType)
  }

  @Path("/{eventType}/events")
  @POST
  def event(@PathParam("eventType") eventType: String, data: ObjectNode) = {
    val environmentContext = EnvironmentContext.current

    taskExecutor.execute(new Runnable {
      def run {
        try {
          EnvironmentContext.current(environmentContext)
          analyticsService.event(eventType, data)
        } finally {
          EnvironmentContext.remove
        }
      }
    })

    Response.noContent.build
  }

  @Path("/{eventType}/events")
  @GET
  def events(
    @PathParam("eventType") eventType: String,
    @QueryParam("start") start: String,
    @QueryParam("end") end: String,
    @QueryParam("query") query: String) = {
    if (start == null) throw new InvalidParamaterException("start is a required field")

    val events = analyticsService.events(
      eventType,
      parseDate(start),
      parseDate(end),
      if (query != null) Some(query) else None)

    events
  }

  @Path("/{eventType}/metrics/{metric}/{tier}/series")
  @GET
  def metrics(
    @PathParam("eventType") eventType: String,
    @PathParam("metric") metric: String,
    @PathParam("tier") tier: String,
    @QueryParam("start") start: String,
    @QueryParam("end") end: String,
    @QueryParam("query") query: String,
    @QueryParam("fillGaps") fillGaps: Boolean,
    @QueryParam("refreshing") refreshing: Boolean,
    @QueryParam("limit") limit: Integer) = {
    if (start == null) throw new InvalidParamaterException("start is a required field")

    analyticsService.metrics(
      eventType, metric, tier,
      parseDate(start),
      parseDate(end),
      if (query != null) Some(query) else None,
      fillGaps,
      refreshing,
      if (limit != null) limit else DEFAULT_SAMPLE_LIMIT)
  }

  @Path("/{eventType}/metrics/{metric}/{tier}/counts")
  @GET
  def counts(
    @PathParam("eventType") eventType: String,
    @PathParam("metric") metric: String,
    @PathParam("tier") tier: String,
    @QueryParam("start") start: String,
    @QueryParam("end") end: String,
    @QueryParam("query") query: String,
    @QueryParam("limit") limit: Integer) = {
    if (start == null) throw new InvalidParamaterException("start is a required field")

    analyticsService.counts(
      eventType, metric, tier,
      parseDate(start),
      parseDate(end),
      if (query != null) Some(query) else None,
      if (limit != null) Some(limit) else None)
  }

  @Path("/{eventType}/metrics/{metric}/distinct")
  @GET
  def distinct(
    @PathParam("eventType") eventType: String,
    @PathParam("metric") metric: String,
    @QueryParam("start") start: String,
    @QueryParam("end") end: String,
    @QueryParam("query") query: String) = {
    if (start == null) throw new InvalidParamaterException("start is a required field")

    analyticsService.distinct(
      eventType, metric,
      parseDate(start),
      parseDate(end),
      if (query != null) Some(query) else None)
  }
}

@Path("/v1")
@Named
class AnalyticsDocumentationResource extends AbstractDocumentationResource("V1")