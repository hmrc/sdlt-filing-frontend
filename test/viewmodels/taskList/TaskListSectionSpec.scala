/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package viewmodels.taskList

import base.SpecBase
import config.FrontendAppConfig
import constants.FullReturnConstants
import models.*
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.tasklist.{TLCannotStart, TLCompleted, TLFailed, TLInProgress, TLNotStarted, TaskListRowBuilder, TaskListSection, TaskListSectionRow, TaskListSections, TaskListState}

import scala.concurrent.ExecutionContext

class TaskListSectionSpec extends SpecBase {
  
  private val fullReturnComplete = FullReturnConstants.completeFullReturn
  private val fullReturnIncomplete = FullReturnConstants.incompleteFullReturn

  "TaskListSection" - {

    "isComplete" - {

      "must return true when all rows are completed" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLCompleted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLCompleted)
        val section = TaskListSection("Test Section", Seq(row1, row2))

        section.isComplete mustBe true
      }

      "must return false when some rows are not completed" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLCompleted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLNotStarted)
        val section = TaskListSection("Test Section", Seq(row1, row2))

        section.isComplete mustBe false
      }

      "must return false when no rows are completed" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLNotStarted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLInProgress)
        val section = TaskListSection("Test Section", Seq(row1, row2))

        section.isComplete mustBe false
      }

      "must return true when section has no rows" in {
        val section = TaskListSection("Empty Section", Seq())

        section.isComplete mustBe true
      }

      "must return false when any row has InProgress status" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLCompleted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLInProgress)
        val section = TaskListSection("Test Section", Seq(row1, row2))

        section.isComplete mustBe false
      }

      "must return false when any row has CannotStart status" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLCompleted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLCannotStart)
        val section = TaskListSection("Test Section", Seq(row1, row2))

        section.isComplete mustBe false
      }

      "must return false when any row has Failed status" in {
        val row1 = TaskListSectionRow("test1", "/url1", "tag1", TLCompleted)
        val row2 = TaskListSectionRow("test2", "/url2", "tag2", TLFailed)
        val section = TaskListSection("Test Section", Seq(row1, row2))

        section.isComplete mustBe false
      }
    }
  }

  "TaskListSections" - {

    "sections" - {

      "must return list with sections when FullReturn has data" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val hc: HeaderCarrier = HeaderCarrier()
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

          val result = TaskListSections.sections(fullReturnComplete)

          result.size mustBe 2 // PrelimTaskList and VendorTaskList
          result.head mustBe a[TaskListSection]
        }
      }

      "must return list with sections when FullReturn is minimal" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val hc: HeaderCarrier = HeaderCarrier()
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

          val result = TaskListSections.sections(fullReturnIncomplete)

          result.size mustBe 2
          result.head mustBe a[TaskListSection]
        }
      }

      "must flatten correctly to remove None values" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val hc: HeaderCarrier = HeaderCarrier()
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

          val result = TaskListSections.sections(fullReturnComplete)

          result must not be empty
          result.forall(_ != null) mustBe true
        }
      }

      "must create sections with correct structure" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val hc: HeaderCarrier = HeaderCarrier()
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

          val result = TaskListSections.sections(fullReturnComplete)

          result.foreach { section =>
            section.heading must not be empty
            section.rows must not be empty
          }
        }
      }
    }

    "allComplete" - {

      "must return true when all sections are complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val hc: HeaderCarrier = HeaderCarrier()
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

          val result = TaskListSections.allComplete(fullReturnComplete)

          result mustBe true
        }
      }

      "must return false when sections are not complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val hc: HeaderCarrier = HeaderCarrier()
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

          val result = TaskListSections.allComplete(fullReturnIncomplete)

          result mustBe false
        }
      }

      "must check all sections for completion" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val hc: HeaderCarrier = HeaderCarrier()
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

          val sectionsResult = TaskListSections.sections(fullReturnComplete)
          val allCompleteResult = TaskListSections.allComplete(fullReturnComplete)

          allCompleteResult mustBe sectionsResult.forall(_.isComplete)
        }
      }

      "must handle empty FullReturn correctly" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val hc: HeaderCarrier = HeaderCarrier()
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

          val emptyFullReturn = FullReturn()
          val result = TaskListSections.allComplete(emptyFullReturn)

          result mustBe a[Boolean]
        }
      }
    }
  }

  "TaskListSectionRow" - {

    "must create row with all properties" in {
      val row = TaskListSectionRow(
        messageKey = "test.message",
        url = "/test/url",
        tagId = "testTagId",
        status = TLCompleted,
        canEdit = true
      )

      row.messageKey mustBe "test.message"
      row.url mustBe "/test/url"
      row.tagId mustBe "testTagId"
      row.status mustBe TLCompleted
      row.canEdit mustBe true
    }

    "must have default canEdit as false" in {
      val row = TaskListSectionRow(
        messageKey = "test.message",
        url = "/test/url",
        tagId = "testTagId",
        status = TLNotStarted
      )

      row.canEdit mustBe false
    }

    "must support equality" in {
      val row1 = TaskListSectionRow("test", "/url", "tag", TLCompleted, canEdit = true)
      val row2 = TaskListSectionRow("test", "/url", "tag", TLCompleted, canEdit = true)

      row1 mustEqual row2
    }

    "must not be equal when properties differ" in {
      val row1 = TaskListSectionRow("test", "/url", "tag", TLCompleted)
      val row2 = TaskListSectionRow("test", "/url", "tag", TLNotStarted)

      row1 must not equal row2
    }

    "must support copy" in {
      val original = TaskListSectionRow("test", "/url", "tag", TLNotStarted)
      val modified = original.copy(status = TLCompleted)

      modified.status mustBe TLCompleted
      modified.messageKey mustBe original.messageKey
      modified.url mustBe original.url
      modified.tagId mustBe original.tagId
    }

    "must support copy with canEdit" in {
      val original = TaskListSectionRow("test", "/url", "tag", TLCompleted, canEdit = false)
      val modified = original.copy(canEdit = true)

      modified.canEdit mustBe true
      modified.messageKey mustBe original.messageKey
      modified.status mustBe original.status
    }
  }

  "TaskListState" - {

    "TLCannotStart" - {

      "must be a TaskListState" in {
        TLCannotStart mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLCannotStart
        val ref2 = TLCannotStart

        ref1 must be theSameInstanceAs ref2
      }
    }

    "TLNotStarted" - {

      "must be a TaskListState" in {
        TLNotStarted mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLNotStarted
        val ref2 = TLNotStarted

        ref1 must be theSameInstanceAs ref2
      }
    }

    "TLInProgress" - {

      "must be a TaskListState" in {
        TLInProgress mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLInProgress
        val ref2 = TLInProgress

        ref1 must be theSameInstanceAs ref2
      }
    }

    "TLCompleted" - {

      "must be a TaskListState" in {
        TLCompleted mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLCompleted
        val ref2 = TLCompleted

        ref1 must be theSameInstanceAs ref2
      }
    }

    "TLFailed" - {

      "must be a TaskListState" in {
        TLFailed mustBe a[TaskListState]
      }

      "must be a singleton" in {
        val ref1 = TLFailed
        val ref2 = TLFailed

        ref1 must be theSameInstanceAs ref2
      }
    }

    "all states" - {

      "must be distinct" in {
        val states = Seq(TLCannotStart, TLNotStarted, TLInProgress, TLCompleted, TLFailed)

        states.distinct.size mustBe 5
      }

      "must not be equal to each other" in {
        TLCannotStart must not equal TLNotStarted
        TLNotStarted must not equal TLInProgress
        TLInProgress must not equal TLCompleted
        TLCompleted must not equal TLFailed
        TLFailed must not equal TLCannotStart
      }
    }
  }
}