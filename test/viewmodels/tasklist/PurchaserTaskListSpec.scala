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

class PurchaserTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val fullReturnCompleteWithOneMainPurchaser = fullReturnComplete.copy(
    purchaser = Some(Seq(completePurchaser1)))
  private val fullReturnCompleteWithMultiplePurchasers = completeFullReturn
  private val fullReturnIncompletePurchaser = fullReturnComplete.copy(
    purchaser = Some(Seq(completePurchaser1.copy(address1 = None))))
  private val fullReturnMissingPurchaser = fullReturnComplete.copy(purchaser = None)

  "PurchaserTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when purchaser is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.purchaserQuestion.heading")
        }
      }

      "must return TaskListSection with correct heading when purchaser is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.build(emptyFullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.purchaserQuestion.heading")
        }
      }

      "must return TaskListSection with one row" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.build(fullReturnComplete)

          result.rows.size mustBe 1
        }
      }
    }

    ".buildPurchaserRow" - {
      "must return TaskListSectionRow" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnComplete)

          result mustBe a[TaskListSectionRow]
        }
      }

      "must have correct tag id" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnComplete)

          result.tagId mustBe "purchaserQuestionDetailRow"
        }
      }

      "must have correct link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnComplete)

          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.purchaserQuestion.details")
        }
      }

      "must have Purchaser Before You Start url when main purchaser is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnMissingPurchaser)

          result.url mustBe controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Purchaser Before You Start url when main purchaser is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnIncompletePurchaser)

          result.url mustBe controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Purchaser Overview url when a main purchaser is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnCompleteWithOneMainPurchaser)

          result.url mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }

      "must have Purchaser Overview url when main purchaser complete among other purchasers" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnCompleteWithMultiplePurchasers)

          result.url mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }

      "must show completed status when a main purchaser is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(fullReturnComplete)

          result.status mustBe TLCompleted
        }
      }

      "must show cannot start status when purchaser is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = PurchaserTaskList.buildPurchaserRow(emptyFullReturn)

          result.status mustBe TLCannotStart
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with completed row when purchaser present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = PurchaserTaskList.build(fullReturnCompleteWithOneMainPurchaser)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.purchaserQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.purchaserQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with cannot start row when purchaser absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = PurchaserTaskList.build(emptyFullReturn)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.purchaserQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.purchaserQuestion.details")
          row.status mustBe TLCannotStart
          row.url mustBe controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url
        }
      }
    }
  }

}
