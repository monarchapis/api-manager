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

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar

import com.monarchapis.apimanager.service.KeyStore

class AuthenticatorRegistryTest extends FlatSpec with Matchers with MockitoSugar {
  val (verifier1, registry) = {
    val verifier1 = new Authenticator {
      def name = "verifier1"
      def displayName = "Verifier 1"
      val propertyDescriptors = List()
      def authenticate(keyStore: KeyStore, request: AuthenticationRequest) = Right(List())
    }

    val verifier2 = new Authenticator {
      def name = "abcdef"
      def displayName = "ABCDEF"
      val propertyDescriptors = List()
      def authenticate(keyStore: KeyStore, request: AuthenticationRequest) = Right(List())
    }

    val verifier3 = new Authenticator {
      def name = "BCDEFG"
      def displayName = "BCDEFG"
      val propertyDescriptors = List()
      def authenticate(keyStore: KeyStore, request: AuthenticationRequest) = Right(List())
    }

    val registry = new AuthenticatorRegistry(verifier1, verifier2, verifier3)

    (verifier1, registry)
  }

  behavior of "AuthenticatorRegistry"

  it should "return the verifier using the name of the verifer as the key" in {
    val verifier = registry(verifier1.name)
    verifier should equal(Some(verifier1))
  }

  it should "return None if the verifier could not be found" in {
    val verifier = registry("unknown")
    verifier should equal(None)
  }

  it should "return the name of the authenticators in alphabetical order (case insensitve)" in {
    val names = registry.names
    names(0) should equal("abcdef")
    names(1) should equal("BCDEFG")
    names(2) should equal("verifier1")
  }
}