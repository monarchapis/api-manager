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

import com.monarchapis.apimanager.model.Key
import com.monarchapis.apimanager.model.Token
import com.monarchapis.apimanager.model.Configuration

trait Policy {
  def name: String
  def displayName: String
  def propertyDescriptors: List[PropertyDescriptor]
  def verify(
    config: Configuration,
    request: AuthenticationRequest,
    context: AuthenticationContext): Boolean
}