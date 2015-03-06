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

package com.monarchapis.apimanager.analytics

import scala.collection.JavaConversions._
import org.apache.commons.lang3.NotImplementedException

class DictionaryStringShortener(shortNameLookup: Map[String, String]) extends StringShortener {
  def this(shortNameLookup: java.util.Map[String, String]) = this(shortNameLookup.toMap)

  val longNameLookup = {
    val builder = Map.newBuilder[String, String]
    val addedShortNames = collection.mutable.Set[String]()

    for ((longName, shortName) <- shortNameLookup) {
      if (addedShortNames.contains(shortName)) {
        throw new IllegalArgumentException(s"Collision detected for short name '$shortName'")
      }

      builder += shortName -> longName
      addedShortNames += shortName
    }

    builder.result
  }

  def apply(longName: String): String = {
    shortNameLookup.get(longName) match {
      case Some(shortName) => shortName
      case _ => defaultShortName(longName)
    }
  }

  protected def defaultShortName(longName: String) = longName

  def unapply(shortName: String): String = {
    longNameLookup.get(shortName) match {
      case Some(longName) => longName
      case _ => defaultLongName(shortName)
    }
  }

  protected def defaultLongName(shortName: String) = shortName
}