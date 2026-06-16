/*
 * Copyright 2026 HM Revenue & Customs
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
import constants.FullReturnConstants.*
import play.api.i18n.Messages
import play.api.test.Helpers.running

class UkResidencyTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val fullReturnIncompleteResidency = fullReturnComplete.copy(
    residency = Some(completeResidency.copy(isNonUkResidents = None)))
  private val fullReturnMissingResidency = fullReturnComplete.copy(residency = None)

  "UkResidencyTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when uk residency is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.ukResidencyQuestion.heading")
          result.rows.size mustBe 1
        }
      }

      "must return TaskListSection with correct heading when uk residency is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.build(emptyFullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.ukResidencyQuestion.heading")
        }
      }
    }

    ".isResidencyComplete" - {

      "must return true if residency exists and mandatory fields defined" in {
        val application = applicationBuilder().build()

        running(application) {
          val result = UkResidencyTaskList.isResidencyComplete(fullReturnComplete)

          result mustBe true
        }
      }

      "must return false if residency but isNonUkResidents is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          val result = UkResidencyTaskList.isResidencyComplete(fullReturnComplete
            .copy(residency = Some(completeResidency
            .copy(isNonUkResidents = None))))

          result mustBe false
        }
      }

      "must return false if residency but isCloseCompany is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          val result = UkResidencyTaskList.isResidencyComplete(fullReturnComplete
            .copy(residency = Some(completeResidency
              .copy(isCloseCompany = None))))

          result mustBe false
        }
      }

      "must return false if residency but isCrownRelief is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          val result = UkResidencyTaskList.isResidencyComplete(fullReturnComplete
            .copy(residency = Some(completeResidency
              .copy(isCrownRelief = None))))

          result mustBe false
        }
      }
    }

    ".buildUkResidencyRow" - {
      "must return TaskListSectionRow with correct tag id and link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnComplete)

          result mustBe a[TaskListSectionRow]
          result.tagId mustBe "ukResidencyQuestionRow"
          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.ukResidencyQuestion.details")
        }
      }

      "must have Uk Residency Before You Start url when residency is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnMissingResidency)

          result.url mustBe controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Uk Residency Before You Start url when Uk Residency is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnIncompleteResidency)

          result.url mustBe controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad().url
        }
      }

      "must have  Uk Residency Check your answers url when  Uk Residency is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnComplete)

          result.url mustBe controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url
        }
      }

      "must show completed status when Uk Residency is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(fullReturnComplete)

          result.status mustBe TLCompleted
        }
      }

      "must show cannot start status when Uk Residency is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = UkResidencyTaskList.buildUkResidencyRow(emptyFullReturn)

          result.status mustBe TLCannotStart
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with completed row when Uk Residency present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = UkResidencyTaskList.build(fullReturnComplete)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.ukResidencyQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.ukResidencyQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with cannot start row when Uk Residency absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = UkResidencyTaskList.build(emptyFullReturn)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.ukResidencyQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.ukResidencyQuestion.details")
          row.status mustBe TLCannotStart
          row.url mustBe controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad().url
        }
      }
    }
  }

}
