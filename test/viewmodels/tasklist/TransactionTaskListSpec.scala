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

class TransactionTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn

  private val noFailures: SectionStatus =
    SectionStatus(ReturnSection.Transaction, hasFailures = false, ruleIds = Nil, messageKeys = Nil, targets = Nil)

  private def withFailure(targetPage: PageId): SectionStatus =
    SectionStatus(
      section     = ReturnSection.Transaction,
      hasFailures = true,
      ruleIds     = Seq("F23-test"),
      messageKeys = Seq("test.message"),
      targets     = Seq(CrossFlowTarget(targetPage, "value"))
    )

  private val multipleFailures: SectionStatus =
    SectionStatus(
      section     = ReturnSection.Transaction,
      hasFailures = true,
      ruleIds     = Seq("F23-a", "F23-b"),
      messageKeys = Seq("a.message", "b.message"),
      targets     = Seq(
        CrossFlowTarget(Pages.ReliefReason,  "value"),
        CrossFlowTarget(Pages.EffectiveDate, "value")
      )
    )

  "TransactionTaskList" - {

    ".build" - {

      "must return a TaskListSection with the correct heading when transaction is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages   = messages(application)
          implicit val appConfig:        FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.build(fullReturnComplete, noFailures)

          result            mustBe a[TaskListSection]
          result.heading    mustBe messagesInstance("tasklist.transactionQuestion.heading")
        }
      }

      "must return a TaskListSection with the correct heading when transaction is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages   = messages(application)
          implicit val appConfig:        FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.build(emptyFullReturn, noFailures)

          result            mustBe a[TaskListSection]
          result.heading    mustBe messagesInstance("tasklist.transactionQuestion.heading")
        }
      }

      "must return a TaskListSection with exactly one row" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages   = messages(application)
          implicit val appConfig:        FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.build(fullReturnComplete, noFailures)

          result.rows.size mustBe 1
        }
      }

      "must default to no failures when status is omitted" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages   = messages(application)
          implicit val appConfig:        FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.build(fullReturnComplete)

          result.rows.head.status mustNot be(TLInvalid)
        }
      }
    }

    ".isTransactionComplete" - {

      "must return true if transaction exists and contains effective date" in {
        val application = applicationBuilder().build()

        running(application) {
          val result = TransactionTaskList.isTransactionComplete(fullReturnComplete)

          result mustBe true
        }
      }

      "must return false if transaction exists but effective date is empty" in {
        val application = applicationBuilder().build()

        running(application) {
          val result = TransactionTaskList.isTransactionComplete(fullReturnComplete.copy(transaction = Some(completeTransaction
          .copy(effectiveDate = None))))

          result mustBe false
        }
      }
    }

    ".buildTransactionRow" - {

      "must return a TaskListSectionRow" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, noFailures)

          result mustBe a[TaskListSectionRow]
        }
      }

      "must have the correct tag id" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, noFailures)

          result.tagId mustBe "transactionQuestionDetailRow"
        }
      }

      "must have the correct link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages           = messages(application)
          implicit val appConfig:        FrontendAppConfig  = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, noFailures)

          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.transactionQuestion.details")
        }
      }
    }

    ".buildTransactionRow status logic" - {

      "must show completed status when transaction is present and there are no failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, noFailures)

          result.status mustBe TLCompleted
        }
      }

      "must show cannot-start status when transaction is absent and there are no failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(emptyFullReturn, noFailures)

          result.status mustBe TLCannotStart
        }
      }

      "must mark the row as invalid when there is a single failure" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, withFailure(Pages.ReliefReason))

          result.status mustBe TLInvalid
        }
      }

      "must mark the row as invalid when there are multiple failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, multipleFailures)

          result.status mustBe TLInvalid
        }
      }

      "must defer to cannot-start when prerequisites are unmet, even with cross-flow failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(emptyFullReturn, withFailure(Pages.ReliefReason))

          result.status mustBe TLCannotStart
        }
      }
    }

    ".buildTransactionRow url routing" - {

      "must route to TransactionCheckYourAnswers when transaction is complete and no failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, noFailures)

          result.url mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
        }
      }

      "must route to TransactionBeforeYouStart when transaction is absent and no failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(emptyFullReturn, noFailures)

          result.url mustBe controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad().url
        }
      }

      "must route to TransactionSingleEntity when there is a single cross-flow failure" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, withFailure(Pages.ReliefReason))

          result.url mustBe controllers.transaction.routes.TransactionSingleEntityController.onPageLoad().url
        }
      }

      "must route to TransactionSingleEntity when there are multiple cross-flow failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, multipleFailures)

          result.url mustBe controllers.transaction.routes.TransactionSingleEntityController.onPageLoad().url
        }
      }

      "must route to TransactionSingleEntity for any failure regardless of target page" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val effectiveDateResult = TransactionTaskList.buildTransactionRow(fullReturnComplete, withFailure(Pages.EffectiveDate))
          val contractDateResult  = TransactionTaskList.buildTransactionRow(fullReturnComplete, withFailure(Pages.ContractDate))
          val propertyTypeResult  = TransactionTaskList.buildTransactionRow(fullReturnComplete, withFailure(Pages.LandPropertyType))

          val expected = controllers.transaction.routes.TransactionSingleEntityController.onPageLoad().url

          effectiveDateResult.url mustBe expected
          contractDateResult.url  mustBe expected
          propertyTypeResult.url  mustBe expected
        }
      }

      "must prioritise failure routing over completion routing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, withFailure(Pages.ReliefReason))

          result.url       mustBe controllers.transaction.routes.TransactionSingleEntityController.onPageLoad().url
          result.url mustNot be(controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
        }
      }
    }

    "integration" - {

      "must build a TaskListSection with completed row when transaction is present and no failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages           = messages(application)
          implicit val appConfig:        FrontendAppConfig  = application.injector.instanceOf[FrontendAppConfig]

          val section = TransactionTaskList.build(fullReturnComplete, noFailures)
          val row     = section.rows.head

          section.heading                       mustBe messagesInstance("tasklist.transactionQuestion.heading")
          messagesInstance(row.messageKey)      mustBe messagesInstance("tasklist.transactionQuestion.details")
          row.status                            mustBe TLCompleted
          row.url                               mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
        }
      }

      "must build a TaskListSection with cannot-start row when transaction is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages           = messages(application)
          implicit val appConfig:        FrontendAppConfig  = application.injector.instanceOf[FrontendAppConfig]

          val section = TransactionTaskList.build(emptyFullReturn, noFailures)
          val row     = section.rows.head

          section.heading                       mustBe messagesInstance("tasklist.transactionQuestion.heading")
          messagesInstance(row.messageKey)      mustBe messagesInstance("tasklist.transactionQuestion.details")
          row.status                            mustBe TLCannotStart
          row.url                               mustBe controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad().url
        }
      }

      "must build a TaskListSection with invalid row when there are cross-flow failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages           = messages(application)
          implicit val appConfig:        FrontendAppConfig  = application.injector.instanceOf[FrontendAppConfig]

          val section = TransactionTaskList.build(fullReturnComplete, withFailure(Pages.ReliefReason))
          val row     = section.rows.head

          section.heading                       mustBe messagesInstance("tasklist.transactionQuestion.heading")
          row.status                            mustBe TLInvalid
          row.url                               mustBe controllers.transaction.routes.TransactionSingleEntityController.onPageLoad().url
        }
      }
    }
  }
}