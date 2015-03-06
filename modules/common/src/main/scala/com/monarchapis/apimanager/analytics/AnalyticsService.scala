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

package com.monarchapis.apimanager.analytics

import org.joda.time.DateTime

import com.fasterxml.jackson.databind.node.ObjectNode

trait AnalyticsService {
  def event: List[EventDescriptor]

  def event(eventType: String): EventDescriptor

  def event(
    eventType: String,
    data: ObjectNode)

  def events(
    eventType: String,
    start: DateTime,
    end: DateTime = DateTime.now,
    query: Option[String] = None): EventsResponse

  def metrics(
    eventType: String,
    metric: String,
    tierKey: String,
    start: DateTime,
    end: DateTime = DateTime.now,
    query: Option[String] = None,
    fillGaps: Boolean = false,
    refreshing: Boolean = false): MetricsResponse

  def counts(
    eventType: String,
    metric: String,
    tierKey: String,
    startIn: DateTime,
    endIn: DateTime = DateTime.now,
    query: Option[String] = None,
    limit: Option[Int] = None): ValueCountsResponse

  def distinct(
    eventType: String,
    metric: String,
    start: DateTime,
    end: DateTime = DateTime.now,
    query: Option[String] = None): DistinctResponse
}