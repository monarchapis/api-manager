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

import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils

object AuthenticationUtils {
  def getAuthorizationWithPrefix(request: AuthenticationRequest, prefix: String, decodeBase64: Boolean = false): Option[String] = {
    val authorization = request.getHeaderValues("Authorization")

    authorization match {
      case Some(list) => {
        val value = list.find(s => s.startsWith(prefix + " "))
        value match {
          case Some(v) => {
            val value = v.substring(prefix.length + 1).trim
            val ret = if (decodeBase64) new String(Base64.decodeBase64(value), "UTF-8") else value
            Some(ret)
          }
          case _ => None
        }
      }
      case None => None
    }
  }

  def getPayloadHash(request: AuthenticationRequest, scheme: String, algorithm: String): Option[String] = {
    request.payloadHashes.get(scheme) match {
      case Some(algos) => algos.get(algorithm)
      case _ => None
    }
  }

  def getSignatureParameters(signatureString: String): Map[String, String] = {
    val variables = Map.newBuilder[String, String]
    val parts = StringUtils.splitByWholeSeparator(signatureString, ", ");

    parts foreach (part => {
      val idx = part.indexOf("=");

      if (idx != -1) {
        val key = part.substring(0, idx)
        val quotedValue = part.substring(idx + 1) //pair(1).trim();

        if (quotedValue.startsWith("\"") && quotedValue.endsWith("\"")) {
          val value = quotedValue.substring(1, quotedValue.length() - 1);

          variables += key -> value;
        }
      }
    })

    variables.result
  }
}