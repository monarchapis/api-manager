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

package com.monarchapis.apimanager.util

import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig

class CloudFriendlyEnvironmentStringPBEConfig extends EnvironmentStringPBEConfig {
  def setPasswordPropertyName(passwordProperty: String) {
    val env = System.getenv(passwordProperty)
    val prop = System.getProperty(passwordProperty)

    if (env != null) {
      setPasswordEnvName(passwordProperty)
    } else if (prop != null) {
      setPasswordSysPropertyName(passwordProperty)
    }
  }
}