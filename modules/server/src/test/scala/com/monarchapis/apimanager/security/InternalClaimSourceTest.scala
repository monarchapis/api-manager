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

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.Matchers
import org.scalatest.FlatSpec
import com.monarchapis.apimanager.service.PrincipalClaimsService
import com.monarchapis.apimanager.service.PrincipalProfileService
import com.monarchapis.apimanager.model._

class InternalClaimSourceTest extends FlatSpec with Matchers with MockitoSugar {
  behavior of "InternalClaimSource"

  val principalProfileService = mock[PrincipalProfileService]
  val principalClaimsService = mock[PrincipalClaimsService]
  val claimSource = new InternalClaimSource(principalProfileService, principalClaimsService)

  when(principalProfileService.findByName("test")).thenReturn(Some(PrincipalProfile(id = "1234", name = "test")))

  when(principalClaimsService.findByName("1234", "employee")).thenReturn(Some(PrincipalClaims( //
    profileId = "1234", //
    name = "employee",
    claims = Map( //
      "ou" -> Set("company")))))

  when(principalClaimsService.findByName("1234", "department")).thenReturn(Some(PrincipalClaims( //
    profileId = "1234", //
    name = "department",
    claims = Map( //
      "ou" -> Set("department")))))

  when(principalClaimsService.findByName("1234", "marketing")).thenReturn(Some(PrincipalClaims( //
    profileId = "1234", //
    name = "marketing",
    inherits = Set("department"), //
    claims = Map( //
      "ou" -> Set("marketing")))))

  when(principalClaimsService.findByName("1234", "jdoe")).thenReturn(Some(PrincipalClaims( //
    profileId = "1234", //
    name = "jdoe",
    inherits = Set("employee", "marketing"), //
    claims = Map( //
      "firstName" -> Set("John"), //
      "lastName" -> Set("Doe"),
      "ou" -> Set("jdoe"), //
      "tags" -> Set("nice", "hard-working")))))

  it should "return an empty list if the principal is not set" in {
    val config = mock[Configuration]
    when(config.value[String]("profile")).thenReturn(Some("test"))
    val context = mock[AuthenticationContext]
    when(context.principal).thenReturn(None)

    val result = claimSource.getClaims(config, null, context)

    result.size should equal(0)
  }

  it should "return an empty list if the profile is not found" in {
    val config = mock[Configuration]
    when(config.value[String]("profile")).thenReturn(Some("doesnotexist"))
    val context = mock[AuthenticationContext]
    when(context.principal).thenReturn(Some("jdoe"))

    val result = claimSource.getClaims(config, null, context)

    result.size should equal(0)
  }

  it should "return an empty list if the claims is not found" in {
    val config = mock[Configuration]
    when(config.value[String]("profile")).thenReturn(Some("test"))
    val context = mock[AuthenticationContext]
    when(context.principal).thenReturn(Some("doesnotexist"))

    val result = claimSource.getClaims(config, null, context)

    result.size should equal(0)
  }

  it should "return the claims from the internal principal claims services" in {
    val config = mock[Configuration]
    when(config.value[String]("profile")).thenReturn(Some("test"))
    val context = mock[AuthenticationContext]
    when(context.principal).thenReturn(Some("jdoe"))

    val result = claimSource.getClaims(config, null, context)

    result should contain(Claim("firstName", "John"))
    result should contain(Claim("lastName", "Doe"))
    result should contain(Claim("ou", "jdoe"))
    result should contain(Claim("ou", "marketing"))
    result should contain(Claim("ou", "department"))
    result should contain(Claim("ou", "company"))
    result should contain(Claim("tags", "nice"))
    result should contain(Claim("tags", "hard-working"))
  }
}