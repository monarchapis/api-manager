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

object HasherUtils {
  private val algorithmLookup = Map(
    "md5" -> "MD5",
    "sha1" -> "SHA-1",
    "sha256" -> "SHA-256",
    "sha384" -> "SHA-384",
    "sha512" -> "SHA-512")

  def getMessageDigestAlgorithm(algorithm: String) = algorithmLookup.get(algorithm) match {
    case Some(javaAlgorithm) => javaAlgorithm
    case _ => throw new RuntimeException(s"Invalid algorithm ${algorithm}")
  }
}