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
import services.crossflow.{CrossFlowTarget, PageId, Pages, ReturnSection, SectionStatus}
import models.CheckMode

class TransactionTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val noFailures: SectionStatus =
    SectionStatus(ReturnSection.Transaction, hasFailures = false, ruleIds = Nil, messageKeys = Nil, targets = Nil)

  "TransactionTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when transaction is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.build(fullReturnComplete,noFailures)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.transactionQuestion.heading")
        }
      }

      "must return TaskListSection with correct heading when transaction is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.build(emptyFullReturn, noFailures)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.transactionQuestion.heading")
        }
      }

      "must return TaskListSection with one row" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.build(fullReturnComplete, noFailures)

          result.rows.size mustBe 1
        }
      }
    }

    ".buildTransactionRow" - {
      "must return TaskListSectionRow" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, noFailures)

          result mustBe a[TaskListSectionRow]
        }
      }

      "must have correct tag id" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, noFailures)

          result.tagId mustBe "transactionQuestionDetailRow"
        }
      }

      "must have correct link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, noFailures)

          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.transactionQuestion.details")
        }
      }


      "must show completed status when transaction is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, noFailures)

          result.status mustBe TLCompleted
        }
      }

      "must show cannot start status when transaction is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(emptyFullReturn, noFailures)

          result.status mustBe TLCannotStart
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with completed row when transaction present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = TransactionTaskList.build(fullReturnComplete, noFailures)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.transactionQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.transactionQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with cannot start row when transaction absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = TransactionTaskList.build(emptyFullReturn, noFailures)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.transactionQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.transactionQuestion.details")
          row.status mustBe TLCannotStart
          row.url mustBe controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad().url
        }
      }
    }
    ".buildTransactionRow with cross-flow failures" - {

      def singleFailure(targetPage: PageId): SectionStatus =
        SectionStatus(
          section = ReturnSection.Transaction,
          hasFailures = true,
          ruleIds = Seq("F23-test"),
          messageKeys = Seq("test.message"),
          targets = Seq(CrossFlowTarget(targetPage, "value"))
        )

      val multipleFailures: SectionStatus =
        SectionStatus(
          section = ReturnSection.Transaction,
          hasFailures = true,
          ruleIds = Seq("F23-a", "F23-b"),
          messageKeys = Seq("a.message", "b.message"),
          targets = Seq(
            CrossFlowTarget(Pages.ReliefReason, "value"),
            CrossFlowTarget(Pages.EffectiveDate, "value")
          )
        )

      "must mark the row as invalid when there is a single failure" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, singleFailure(Pages.ReliefReason))

          result.status mustBe TLInvalid
        }
      }

      "must route to the relief reason page when that is the single target" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, singleFailure(Pages.ReliefReason))

          result.url mustBe controllers.transaction.routes.ReasonForReliefController.onPageLoad(CheckMode).url
        }
      }

      "must route to the effective date page when that is the single target" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, singleFailure(Pages.EffectiveDate))

          result.url mustBe controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(CheckMode).url
        }
      }

      "must route to CYA when the single target has no specific route" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, singleFailure(Pages.LandPropertyType))

          result.url mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
        }
      }

      "must route to CYA when there are multiple failures" - {

        "to surface the error summary listing every conflict" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, multipleFailures)

            result.url mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
          }
        }
      }

      "must defer to cannot-start when prerequisites are unmet, even with cross-flow failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(emptyFullReturn, singleFailure(Pages.ReliefReason))

          result.status mustBe TLCannotStart
        }
      }
      
      "must default to no failures when status is omitted" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.build(fullReturnComplete)

          result.rows.head.status mustNot be(TLInvalid)
        }
      }
    }
  }

}