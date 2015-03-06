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

import javax.inject.Inject

import scala.collection.JavaConversions._

import com.monarchapis.apimanager.servlet.ApiRequest

class RequestHasherRegistry(
  private val hashers: RequestHasher*) {
  @Inject def this(hashers: java.util.List[RequestHasher]) = this(hashers: _*)

  private val lookup = hashers map { hasher => (hasher.name, hasher) } toMap

  val names = hashers map { authenticator => authenticator.name } sortWith (_.toLowerCase < _.toLowerCase)

  def apply(name: String) = lookup.get(name)
}