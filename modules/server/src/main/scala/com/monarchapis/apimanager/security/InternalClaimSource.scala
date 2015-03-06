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

import scala.collection.immutable.List
import scala.collection.mutable.Builder

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._

import javax.inject.Inject
import javax.inject.Named

@Named
class InternalClaimSource @Inject() (
  principalProfileService: PrincipalProfileService,
  principalClaimsService: PrincipalClaimsService) extends ClaimSource {
  val name = "internal"
  val displayName = "Internal"

  val propertyDescriptors = List(
    StringPropertyDescriptor(
      propertyName = "profile",
      displayName = "Profile"))

  def getClaims(
    config: Configuration,
    request: AuthenticationRequest,
    context: AuthenticationContext) = {
    val builder = Set.newBuilder[Claim]

    context.principal match {
      case Some(userId) => {
        val profile = config.value[String]("profile").get

        val processed = new collection.mutable.HashSet[String]()

        principalProfileService.findByName(profile) match {
          case Some(profile) => {
            recurseClaims(profile.id, userId, builder, processed)
          }
          case _ =>
        }
      }
      case _ =>
    }

    builder.result
  }

  private def recurseClaims(profileId: String, principal: String, builder: Builder[Claim, Set[Claim]], processed: collection.mutable.Set[String]) {
    // Prevent infinite recursion
    if (processed.contains(principal)) return

    processed += principal

    principalClaimsService.findByName(profileId, principal) match {
      case Some(claims) => {
        for (v <- claims.inherits) {
          recurseClaims(profileId, v, builder, processed)
        }

        for (e <- claims.claims) {
          for (v <- e._2) {
            builder += Claim(e._1, v)
          }
        }
      }
      case _ =>
    }
  }
}