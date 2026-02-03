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

class LandTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val fullReturnCompleteWithOneMainLand = fullReturnComplete.copy(
    land = Some(Seq(completeLand)))
  private val fullReturnCompleteWithMultipleLands = completeFullReturn
  private val fullReturnIncompleteLand = fullReturnComplete.copy(
    land = Some(Seq(completeLand.copy(landArea = None))))
  private val fullReturnMissingLand = fullReturnComplete.copy(land = None)

  "LandTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when land is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.landQuestion.heading")
        }
      }

      "must return TaskListSection with correct heading when land is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.build(emptyFullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.landQuestion.heading")
        }
      }

      "must return TaskListSection with one row" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.build(fullReturnComplete)

          result.rows.size mustBe 1
        }
      }
    }

    ".buildLandRow" - {
      "must return TaskListSectionRow" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnComplete)

          result mustBe a[TaskListSectionRow]
        }
      }

      "must have correct tag id" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnComplete)

          result.tagId mustBe "landQuestionDetailRow"
        }
      }

      "must have correct link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnComplete)

          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.landQuestion.details")
        }
      }

      "must have Land Before You Start url when main land is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnMissingLand)

          result.url mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Land Before You Start url when main land is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnIncompleteLand)

          result.url mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Land Overview url when main land is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithOneMainLand)

          result.url mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }

      "must have Land Overview url when main land complete among other lands" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnCompleteWithMultipleLands)

          result.url mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }

      "must show completed status when a main land is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(fullReturnComplete)

          result.status mustBe TLCompleted
        }
      }

      "must show cannot start status when land is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LandTaskList.buildLandRow(emptyFullReturn)

          result.status mustBe TLCannotStart
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with completed row when land present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LandTaskList.build(fullReturnCompleteWithOneMainLand)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.landQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.landQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with cannot start row when land absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LandTaskList.build(emptyFullReturn)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.landQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.landQuestion.details")
          row.status mustBe TLCannotStart
          row.url mustBe controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
        }
      }
    }
  }

}
