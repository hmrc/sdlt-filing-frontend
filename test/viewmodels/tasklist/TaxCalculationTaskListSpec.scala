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

class TaxCalculationTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn

  "TaxCalculationTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when tax calculation is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.taxCalculationQuestion.heading")
        }
      }

      "must return TaskListSection with correct heading when tax calculation is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.build(emptyFullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.taxCalculationQuestion.heading")
        }
      }

      "must return TaskListSection with one row" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.build(fullReturnComplete)

          result.rows.size mustBe 1
        }
      }
    }

    ".buildTransactionRow" - {
      "must return TaskListSectionRow" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete)

          result mustBe a[TaskListSectionRow]
        }
      }

      "must have correct tag id" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete)

          result.tagId mustBe "taxCalculationQuestionDetailRow"
        }
      }

      "must have correct link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete)

          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.taxCalculationQuestion.details")
        }
      }


      "must show completed status when tax calculation is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.buildTaxCalculationRow(fullReturnComplete)

          result.status mustBe TLCompleted
        }
      }

      "must show cannot start status when tax calculation is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TaxCalculationTaskList.buildTaxCalculationRow(emptyFullReturn)

          result.status mustBe TLCannotStart
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with completed row when tax calculation present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = TaxCalculationTaskList.build(fullReturnComplete)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.taxCalculationQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.taxCalculationQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.taxCalculation.routes.TaxCalculationBeforeYouStartController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with cannot start row when tax calculation absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = TaxCalculationTaskList.build(emptyFullReturn)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.taxCalculationQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.taxCalculationQuestion.details")
          row.status mustBe TLCannotStart
          row.url mustBe controllers.taxCalculation.routes.TaxCalculationBeforeYouStartController.onPageLoad().url
        }
      }
    }
  }

}
