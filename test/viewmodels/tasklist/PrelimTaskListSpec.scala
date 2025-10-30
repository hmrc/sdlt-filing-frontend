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

package viewmodels.tasklist

import base.SpecBase
import config.FrontendAppConfig
import constants.FullReturnConstants
import models.{FullReturn, NormalMode, PrelimReturn, VendorReturn}
import play.api.i18n.Messages
import play.api.test.Helpers.*
import viewmodels.tasklist.{PrelimTaskList, TLCompleted, TLNotStarted, TaskListSection, TaskListSectionRow}

class PrelimTaskListSpec extends SpecBase {

  private val fullReturnComplete = FullReturnConstants.completeFullReturn
  private val fullReturnIncomplete = FullReturnConstants.incompleteFullReturn

  "PrelimTaskList" - {

    "build" - {

      "must return TaskListSection with correct heading when prelimReturn is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PrelimTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.prelimQuestion.heading")
        }
      }

      "must return TaskListSection with correct heading when prelimReturn is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val fullReturn = FullReturn(None, None)
          val result = PrelimTaskList.build(fullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.prelimQuestion.heading")
        }
      }

      "must return TaskListSection with one row" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PrelimTaskList.build(fullReturnComplete)

          result.rows.size mustBe 1
        }
      }
    }

    "buildPrelimRow" - {

      "must return TaskListSectionRow" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PrelimTaskList.buildPrelimRow(fullReturnComplete).build(fullReturnComplete)

          result mustBe a[TaskListSectionRow]
        }
      }

      "must have correct tag id" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PrelimTaskList.buildPrelimRow(fullReturnComplete)

          result.tagId mustBe "prelimQuestionDetailRow"
        }
      }

      "must have correct link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PrelimTaskList.buildPrelimRow(fullReturnComplete).build(fullReturnComplete)

          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.prelimQuestion.details")
        }
      }

      "must have correct URL" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PrelimTaskList.buildPrelimRow(fullReturnComplete).build(fullReturnComplete)

          result.url mustBe controllers.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(NormalMode).url
        }
      }

      "must show completed status when prelimReturn is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PrelimTaskList.buildPrelimRow(fullReturnComplete).build(fullReturnComplete)

          result.status mustBe TLCompleted
        }
      }

      "must show not started status when prelimReturn is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val fullReturn = FullReturn(None, None)
          val result = PrelimTaskList.buildPrelimRow(fullReturn).build(fullReturnComplete)

          result.status mustBe TLNotStarted
        }
      }
    }

    "integration" - {

      "must build complete TaskListSection with completed row when prelimReturn present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = PrelimTaskList.build(fullReturnComplete)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.prelimQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.prelimQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(NormalMode).url
        }
      }

      "must build complete TaskListSection with not started row when prelimReturn absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val fullReturn = FullReturn(None, None)
          val section = PrelimTaskList.build(fullReturn)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.prelimQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.prelimQuestion.details")
          row.status mustBe TLNotStarted
          row.url mustBe controllers.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(NormalMode).url
        }
      }
    }
  }
}