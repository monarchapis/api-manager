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

import java.io.ByteArrayOutputStream
import java.security.MessageDigest

import org.apache.commons.codec.binary.Base64

import com.monarchapis.apimanager.servlet.ApiRequest

import org.apache.commons.lang3.StringUtils

class HawkV1RequestHasher extends RequestHasher {
  val name = "Hawk V1"

  def getRequestHash(request: ApiRequest, algorithm: String) = {
    val translatedAlgorithm = HasherUtils.getMessageDigestAlgorithm(algorithm)
    val baos = new ByteArrayOutputStream()
    val contentType = StringUtils.trim(StringUtils.substringBefore(request.getContentType(), ";"))
    baos.write("hawk.1.payload\n".getBytes("UTF-8"))

    if (contentType != null) {
      baos.write(contentType.getBytes("UTF-8"))
    }

    baos.write('\n')
    baos.write(request.body)
    baos.write('\n')

    val digest = MessageDigest.getInstance(translatedAlgorithm)
    val hash = digest.digest(baos.toByteArray)
    Base64.encodeBase64String(hash)
  }
}