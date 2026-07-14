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
import models.Transaction
import play.api.i18n.Messages
import play.api.test.Helpers.running
import services.crossflow.{CrossFlowTarget, PageId, Pages, ReturnSection, SectionStatus}

class TransactionTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn

  private val fullReturnMixedResNotGrandOfLease = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("A"), // not Grant of Lease
      effectiveDate = Some("01/02/2024"),
      isDependantOnFutureEvent = Some("YES"),
      agreedToDeferPayment = Some("YES"),
      isPartOfSaleOfBusiness = Some("YES"),
      postTransRulingApplied = Some("YES"),
      restrictionsAffectInterest = Some("NO"),
      isLandExchanged = Some("YES"),
      isPursuantToPreviousOption = Some("NO"),
      usedAsShop = Some("YES"),
      totalConsideration = Some("100000"),
      considerationCash = Some("YES")
    )),
    land = Some(Seq(completeLand.copy(
      propertyType = Some("02") // mixed
    )))
  )

  private val fullReturnMixedResNotGrandOfLeaseMissing = fullReturnMixedResNotGrandOfLease.copy(
    transaction = Some(fullReturnMixedResNotGrandOfLease.transaction.get.copy(
      totalConsideration = None,
      considerationCash = None
    ))
  )

  private val fullReturnMixedResGrandOfLease = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("L"), // Grant of Lease
      effectiveDate = Some("01/02/2024"),
      isDependantOnFutureEvent = Some("YES"),
      agreedToDeferPayment = Some("YES"),
      isPartOfSaleOfBusiness = Some("YES"),
      postTransRulingApplied = Some("YES"),
      restrictionsAffectInterest = Some("NO"),
      isLandExchanged = Some("YES"),
      isPursuantToPreviousOption = Some("NO"),
      usedAsShop = Some("YES")
    )),
    land = Some(Seq(completeLand.copy(
      propertyType = Some("02") // mixed
    ))))

  private val fullReturnMixedResGrandOfLeaseMissing = fullReturnMixedResGrandOfLease.copy(
    transaction = Some(fullReturnMixedResGrandOfLease.transaction.get.copy(
      usedAsShop = None
    ))
  )

  private val fullReturnNotMixedResNotGrandOfLease = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("A"), // not Grant of Lease
      effectiveDate = Some("01/02/2024"),
      isDependantOnFutureEvent = Some("YES"),
      agreedToDeferPayment = Some("YES"),
      isPartOfSaleOfBusiness = Some("YES"),
      postTransRulingApplied = Some("YES"),
      restrictionsAffectInterest = Some("NO"),
      isLandExchanged = Some("YES"),
      isPursuantToPreviousOption = Some("NO"),
      totalConsideration = Some("100000"),
      considerationCash = Some("YES")
    )),
    land = Some(Seq(completeLand.copy(
      propertyType = Some("01") // residential
    )))
  )

  private val fullReturnNotMixedResNotGrandOfLeaseMissing = fullReturnNotMixedResNotGrandOfLease.copy(
    transaction = Some(fullReturnNotMixedResNotGrandOfLease.transaction.get.copy(
      totalConsideration = None,
      considerationCash = None
    ))
  )

  private val fullReturnNotMixedResGrandOfLease = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("L"), // Grant of Lease
      effectiveDate = Some("01/02/2024"),
      isDependantOnFutureEvent = Some("YES"),
      agreedToDeferPayment = Some("YES"),
      isPartOfSaleOfBusiness = Some("YES"),
      postTransRulingApplied = Some("YES"),
      restrictionsAffectInterest = Some("NO"),
      isLandExchanged = Some("YES"),
      isPursuantToPreviousOption = Some("NO")
    )),
    land = Some(Seq(completeLand.copy(
      propertyType = Some("01") // residential
    ))))

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

    ".mandatoryFieldsDefined" - {

      "when property type is mixed or non residential and transaction is not Grant of Lease" - {

        "must return a sequence of true including use of land and consideration fields" in {

          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnMixedResNotGrandOfLease)

          result.length mustBe 12

          // first 9 are general transaction fields (all true here)
          result.take(9).forall(identity) mustBe true

          result(9) mustBe true // isAnyUseOfLandYes

          result(10) mustBe true // isTotalConsiderationDefined

          result(11) mustBe true // isAnyFormsOfConsiderationDefined
        }

        "must return a sequence with false for consideration fields when they are missing" in {

          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnMixedResNotGrandOfLeaseMissing)

          result.length mustBe 12
          result(10) mustBe false // isTotalConsiderationDefined
          result(11) mustBe false // isAnyFormsOfConsiderationDefined
        }
      }

      "when property type is mixed or non residential and transaction is Grant of Lease" - {

        "must return a sequence of true including use of land but not consideration fields" in {

          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnMixedResGrandOfLease)

          result.length mustBe 10

          // first 9 are general transaction fields (all true here)
          result.take(9).forall(identity) mustBe true

          result(9) mustBe true // isAnyUseOfLandYes
        }

        "must return a sequence with false for use of land when it is missing" in {
          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnMixedResGrandOfLeaseMissing)

          result.length mustBe 10
          result(9) mustBe false
        }
      }

      "when property type is not mixed or non residential and transaction is not Grant of Lease" - {

        "must return a sequence of true including consideration fields but not use of land" - {

          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnNotMixedResNotGrandOfLease)

          result.length mustBe 11

          // first 9 are general transaction fields (all true here)
          result.take(9).forall(identity) mustBe true

          result(9) mustBe true // isTotalConsiderationDefined

          result(10) mustBe true // isAnyFormsOfConsiderationDefined
        }

        "must return a sequence with false for consideration fields when they are missing" in {
          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnNotMixedResNotGrandOfLeaseMissing)

          result.length mustBe 11

          // first 9 are general transaction fields (all true here)
          result.take(9).forall(identity) mustBe true

          result(9) mustBe false // isTotalConsiderationDefined

          result(10) mustBe false // isAnyFormsOfConsiderationDefined
        }
      }

      "when property type is not mixed or non residential and transaction is Grant of Lease" - {

        "must return only the general transaction fields" - {

          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnNotMixedResGrandOfLease)

          result.length mustBe 9

          // first 9 are general transaction fields (all true here)
          result.take(9).forall(identity) mustBe true
        }

        "must return false for generic fields when they are missing" in {

          val result = TransactionTaskList.mandatoryFieldsDefined(emptyFullReturn)

          result.length mustBe 9
          result.forall(identity) mustBe false
        }
      }
    }

    ".isTransactionComplete" - {

      "must return true if transaction exists and mandatory fields are defined" in {
          val result = TransactionTaskList.isTransactionComplete(fullReturnMixedResNotGrandOfLease)

          result mustBe true
      }

      "must return false if transaction exists but some mandatory field are missing" in {
          val result = TransactionTaskList.isTransactionComplete(fullReturnMixedResNotGrandOfLeaseMissing)

          result mustBe false
      }

      "must return false if transaction exists but all mandatory field are missing" in {
          val result = TransactionTaskList.isTransactionComplete(emptyFullReturn)

          result mustBe false
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

      "must have Transaction Before You Start url and show 'In progress' status when some mandatory fields are present in transaction" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnMixedResNotGrandOfLeaseMissing, noFailures)

          result.url mustBe controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad().url

          result.status mustBe TLInProgress
        }
      }

      "must have Transaction Check Your Answers url when and show 'Complete' status when all mandatory fields are present in transaction" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnMixedResNotGrandOfLease, noFailures)

          result.url mustBe controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url

          result.status mustBe TLCompleted
        }
      }

      "must show 'Complete' status when transaction is present and there are no failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, noFailures)

          result.status mustBe TLCompleted
        }
      }

      "must show 'Not yet started' status when transaction is absent and there are no failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete.copy(transaction = None), noFailures)

          result.status mustBe TLNotStarted
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

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete.copy(transaction = None), noFailures)

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

      "must build a TaskListSection with not started row when transaction is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages           = messages(application)
          implicit val appConfig:        FrontendAppConfig  = application.injector.instanceOf[FrontendAppConfig]

          val section = TransactionTaskList.build(fullReturnComplete.copy(transaction = None), noFailures)
          val row     = section.rows.head

          section.heading                       mustBe messagesInstance("tasklist.transactionQuestion.heading")
          messagesInstance(row.messageKey)      mustBe messagesInstance("tasklist.transactionQuestion.details")
          row.status                            mustBe TLNotStarted
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