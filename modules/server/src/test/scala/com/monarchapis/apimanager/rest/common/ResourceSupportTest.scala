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

package com.monarchapis.apimanager.rest.common

import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar

import com.monarchapis.apimanager.service._

import javax.ws.rs.core._

class ResourceSupportTest extends FlatSpec with Matchers with MockitoSugar {
  behavior of "ResourceSupport"

  val resource = new ResourceSupport {
    val resourceName = "test"
  }

  it should "convert URI parameters to a filter" in {
    val map = new MultivaluedHashMap[String, String]
    val uriInfo = mock[UriInfo]

    when(uriInfo.getQueryParameters).thenReturn(map)

    map.add("single", "value")
    map.addAll("multi", "value 1", "value 2", "value 3")

    val actual = resource.toFilter(uriInfo)

    actual.get("single") should equal(Some(List("value")))
    actual.get("multi") should equal(Some(List("value 1", "value 2", "value 3")))
  }

  it should "convert URI parameters to an order by" in {
    val map = new MultivaluedHashMap[String, String]
    val uriInfo = mock[UriInfo]

    when(uriInfo.getQueryParameters).thenReturn(map)

    // Use default and different cases for ascending and descending
    map.add("orderBy", "field1, field2 ASC, field3 DESC, field4 asc, field5 desc, field6 aSc, field7 dESc")

    val actual = resource.toOrderBy(uriInfo)

    actual should equal(List( //
      OrderByField("field1", OrderDirection.ASCENDING), //
      OrderByField("field2", OrderDirection.ASCENDING), //
      OrderByField("field3", OrderDirection.DESCENDING), //
      OrderByField("field4", OrderDirection.ASCENDING), //
      OrderByField("field5", OrderDirection.DESCENDING), //
      OrderByField("field6", OrderDirection.ASCENDING), //
      OrderByField("field7", OrderDirection.DESCENDING)))
  }

  it should "convert the expand URI parameters to a set" in {
    val actual1 = resource.toExpand(null)
    actual1 should equal(Set.empty[String])

    val actual2 = resource.toExpand("  ")
    actual2 should equal(Set.empty[String])

    val actual3 = resource.toExpand("one, two, three")
    actual3 should equal(Set("one", "two", "three"))

    val actual4 = resource.toExpand("one,two,three")
    actual4 should equal(Set("one", "two", "three"))
  }
}