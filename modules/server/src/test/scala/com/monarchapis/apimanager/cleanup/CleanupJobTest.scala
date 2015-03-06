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

package com.monarchapis.apimanager.cleanup

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._

class CleanupJobTest extends FlatSpec with MockitoSugar {
  import org.mockito.Mockito._
  import org.mockito.Matchers._

  behavior of "CleanupJob"

  it should "call all cleanup tasks for each environment" in {
    val environmentService = mock[EnvironmentService]
    val cleanableRegistry = mock[CleanableRegistry]

    when(environmentService.find( //
      org.mockito.Matchers.eq(0), //
      anyInt(), //
      any(classOf[Map[String, List[String]]]), //
      any(classOf[List[OrderByField]]), //
      any(classOf[Set[String]]))).thenReturn(PagedList[Environment]( //
      0, 2, 2, 2,
      List( //
        Environment( //
          id = "1", //
          name = "Test 1", //
          description = None, //
          systemDatabase = "test1", //
          analyticsDatabase = "test1", //
          createdBy = "test", //
          modifiedBy = "test"), //
        Environment( //
          id = "2", //
          name = "Test 2", //
          description = None, //
          systemDatabase = "test2", //
          analyticsDatabase = "test2", //
          createdBy = "test", //
          modifiedBy = "test"))))

    val cleanupJob = new CleanupJob(environmentService, cleanableRegistry)

    cleanupJob.cleanup

    verify(cleanableRegistry, times(2)).cleanAll
  }
}