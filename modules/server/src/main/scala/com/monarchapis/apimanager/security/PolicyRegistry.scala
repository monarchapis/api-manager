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

import scala.collection.JavaConversions._

import javax.inject.Inject

class PolicyRegistry(
  private val policies: Policy*) {
  @Inject def this(policies: java.util.List[Policy]) = this(policies: _*)

  private val lookup = policies map { policy => (policy.name, policy) } toMap

  val names = policies map { policy => policy.name } sortWith (_.toLowerCase < _.toLowerCase)

  def apply(name: String) = lookup.get(name)
}