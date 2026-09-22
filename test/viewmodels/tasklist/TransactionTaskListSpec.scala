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

  private val fullReturnMixedResNotGrantOfLease = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("A"),
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

  private val fullReturnMixedResNotGrantOfLeaseMissing = fullReturnMixedResNotGrantOfLease.copy(
    transaction = Some(fullReturnMixedResNotGrantOfLease.transaction.get.copy(
      totalConsideration = None,
      considerationCash = None
    ))
  )

  private val fullReturnNonResNotGrantOfLease = fullReturnMixedResNotGrantOfLease.copy(
    land = Some(Seq(completeLand.copy(
      propertyType = Some("03") // non-residential
    )))
  )

  private val fullReturnMixedResGrantOfLease = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("L"),
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
      propertyType = Some("02")
    ))))

  private val fullReturnMixedResGrantOfLeaseMissing = fullReturnMixedResGrantOfLease.copy(
    transaction = Some(fullReturnMixedResGrantOfLease.transaction.get.copy(
      usedAsShop = None
    ))
  )

  private val fullReturnNotMixedResNotGrantOfLease = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("A"),
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
      propertyType = Some("01")
    )))
  )

  private val fullReturnNotMixedResNotGrantOfLeaseMissing = fullReturnNotMixedResNotGrantOfLease.copy(
    transaction = Some(fullReturnNotMixedResNotGrantOfLease.transaction.get.copy(
      totalConsideration = None,
      considerationCash = None
    ))
  )

  private val fullReturnNoTransactionDescription = fullReturnNotMixedResNotGrantOfLease.copy(
    transaction = Some(fullReturnNotMixedResNotGrantOfLease.transaction.get.copy(
      transactionDescription = None
    ))
  )

  private val fullReturnMainLandIdMismatch = fullReturnMixedResNotGrantOfLease.copy(
    returnInfo = fullReturnMixedResNotGrantOfLease.returnInfo.map(_.copy(mainLandID = Some("NOT-A-LAND-ID")))
  )

  private val fullReturnNotMixedResGrantOfLease = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("L"),
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
      propertyType = Some("01")
    ))))

  private val fullReturnPrelimOnly = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("A")
    ))
  )

  private val fullReturnPrelimFromBackend = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionID             = Some("221110172"),
      returnID                  = Some("221110168"),
      transactionDescription    = Some("F"),
      newTransactionDescription = Some("F")
    ))
  )

  private val fullReturnPrelimGrantOfLeaseMixedLand = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionID             = Some("383100162"),
      returnID                  = Some("383100158"),
      transactionDescription    = Some("L"),
      newTransactionDescription = Some("L")
    )),
    land = Some(Seq(completeLand.copy(
      propertyType = Some("02")
    )))
  )

  private val fullReturnPrelimPlusGeneralNo = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("A"),
      isLandExchanged        = Some("NO")
    ))
  )

  private val fullReturnPrelimPlusUseOfLandNo = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("A"),
      usedAsShop             = Some("NO")
    ))
  )

  private val fullReturnPrelimPlusConsiderationNo = fullReturnComplete.copy(
    transaction = Some(Transaction(
      transactionDescription = Some("A"),
      considerationCash      = Some("NO")
    ))
  )

  private val fullReturnEffectiveDateOnly = fullReturnComplete.copy(
    transaction = Some(Transaction(
      effectiveDate = Some("01/02/2024")
    ))
  )

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

  private def resumeUrl: String = controllers.routes.ResumeSectionController.resume("transaction", None).url

  private def beforeYouStartUrl: String =
    controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad().url

  private def singleEntityUrl: String =
    controllers.transaction.routes.TransactionSingleEntityController.onPageLoad().url

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

          result.rows.head.status mustBe TLCompleted
          result.rows.head.status mustNot be(TLInvalid)
        }
      }
    }

    ".mandatoryFieldsDefined" - {

      "when property type is mixed or non residential and transaction is not Grant of Lease" - {

        "must return a sequence of true including use of land and consideration fields" in {

          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnMixedResNotGrantOfLease)

          result.length mustBe 11

          // first 8 are general transaction fields (all true here)
          result.take(8).forall(identity) mustBe true

          result(8) mustBe true // isAnyUseOfLandYes

          result(9) mustBe true // isTotalConsiderationDefined

          result(10) mustBe true // isAnyFormsOfConsiderationDefined
        }

        "must treat '03 - Non-residential' the same as '02 - Mixed'" in {

          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnNonResNotGrantOfLease)

          result.length mustBe 11
          result.forall(identity) mustBe true
        }

        "must return a sequence with false for consideration fields when they are missing" in {

          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnMixedResNotGrantOfLeaseMissing)

          result.length mustBe 11
          result(9) mustBe false // isTotalConsiderationDefined
          result(10) mustBe false // isAnyFormsOfConsiderationDefined
        }
      }

      "when property type is mixed or non residential and transaction is Grant of Lease" - {

        "must return a sequence of true including use of land but not consideration fields" in {

          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnMixedResGrantOfLease)

          result.length mustBe 9

          // first 8 are general transaction fields (all true here)
          result.take(8).forall(identity) mustBe true

          result(8) mustBe true // isAnyUseOfLandYes
        }

        "must return a sequence with false for use of land when it is missing" in {
          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnMixedResGrantOfLeaseMissing)

          result.length mustBe 9
          result(8) mustBe false
        }
      }

      "when property type is not mixed or non residential and transaction is not Grant of Lease" - {

        "must return a sequence of true including consideration fields but not use of land" in {

          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnNotMixedResNotGrantOfLease)

          result.length mustBe 10

          // first 9 are general transaction fields (all true here)
          result.take(8).forall(identity) mustBe true

          result(8) mustBe true // isTotalConsiderationDefined

          result(9) mustBe true // isAnyFormsOfConsiderationDefined
        }

        "must return a sequence with false for consideration fields when they are missing" in {
          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnNotMixedResNotGrantOfLeaseMissing)

          result.length mustBe 10

          // first 9 are general transaction fields (all true here)
          result.take(8).forall(identity) mustBe true

          result(8) mustBe false // isTotalConsiderationDefined

          result(9) mustBe false // isAnyFormsOfConsiderationDefined
        }
      }

      "when property type is not mixed or non residential and transaction is Grant of Lease" - {

        "must return only the general transaction fields" in {

          val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnNotMixedResGrantOfLease)

          result.length mustBe 8

          // first 8 are general transaction fields (all true here)
          result.take(8).forall(identity) mustBe true
        }

        "must return false for generic fields when they are missing" in {

          val result = TransactionTaskList.mandatoryFieldsDefined(emptyFullReturn)

          result.length mustBe 8
          result.forall(identity) mustBe false
        }
      }

      "must require the consideration fields when the transaction description is unanswered" in {
        // `!transactionDescription.contains("L")` is true for None, so an unanswered
        // transaction type is treated as "not a grant of lease".
        val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnNoTransactionDescription)

        result.length mustBe 10
        result(0) mustBe false // transactionDescription
      }

      "must treat the property type as neither mixed nor non-residential when no land matches the main land id" in {
        val result = TransactionTaskList.mandatoryFieldsDefined(fullReturnMainLandIdMismatch)

        result.length mustBe 10 // general + consideration only, no use-of-land check
      }
    }

    ".isTransactionComplete" - {

      "must return true if transaction exists and mandatory fields are defined" in {
        val result = TransactionTaskList.isTransactionComplete(fullReturnMixedResNotGrantOfLease)

        result mustBe true
      }

      "must return false if transaction exists but some mandatory field are missing" in {
        val result = TransactionTaskList.isTransactionComplete(fullReturnMixedResNotGrantOfLeaseMissing)

        result mustBe false
      }

      "must return false if transaction exists but all mandatory field are missing" in {
        val result = TransactionTaskList.isTransactionComplete(emptyFullReturn)

        result mustBe false
      }

      "must return false when only the prelim is answered" in {
        TransactionTaskList.isTransactionComplete(fullReturnPrelimOnly) mustBe false
        TransactionTaskList.isTransactionComplete(fullReturnPrelimFromBackend) mustBe false
      }

      "must ignore cross-flow failures — it only reflects the mandatory fields" in {
        TransactionTaskList.isTransactionComplete(fullReturnComplete) mustBe true
      }
    }

    ".hasStartedBeyondPrelim" - {

      "must return false when only the prelim (type of transaction) is answered" in {
        TransactionTaskList.hasStartedBeyondPrelim(fullReturnPrelimOnly) mustBe false
      }

      "must return false for the prelim payload as persisted (ids and newTransactionDescription are ignored)" in {
        TransactionTaskList.hasStartedBeyondPrelim(fullReturnPrelimFromBackend) mustBe false
      }

      "must return false for a prelim-only grant of lease on mixed-use land" in {
        TransactionTaskList.hasStartedBeyondPrelim(fullReturnPrelimGrantOfLeaseMixedLand) mustBe false
      }

      "must return false when the transaction is absent" in {
        TransactionTaskList.hasStartedBeyondPrelim(emptyFullReturn) mustBe false
      }

      "must return true when only a single non-prelim field is answered" in {
        TransactionTaskList.hasStartedBeyondPrelim(fullReturnEffectiveDateOnly) mustBe true
      }

      "must return true when a general question beyond the prelim is answered NO" in {
        TransactionTaskList.hasStartedBeyondPrelim(fullReturnPrelimPlusGeneralNo) mustBe true
      }

      "must return true when a use of land question is answered NO" in {
        TransactionTaskList.hasStartedBeyondPrelim(fullReturnPrelimPlusUseOfLandNo) mustBe true
      }

      "must return true when a form of consideration question is answered NO" in {
        TransactionTaskList.hasStartedBeyondPrelim(fullReturnPrelimPlusConsiderationNo) mustBe true
      }

      "must return true when a mandatory field beyond the prelim is answered but the section is incomplete" in {
        TransactionTaskList.hasStartedBeyondPrelim(fullReturnMixedResNotGrantOfLeaseMissing) mustBe true
      }

      "must return true when every mandatory field is answered" in {
        TransactionTaskList.hasStartedBeyondPrelim(fullReturnMixedResNotGrantOfLease) mustBe true
      }
    }

    ".isPrelimTransaction" - {

      "must return true when only the transaction description is answered" in {
        TransactionTaskList.isPrelimTransaction(fullReturnPrelimOnly) mustBe true
      }

      "must return true for the prelim payload as persisted" in {
        TransactionTaskList.isPrelimTransaction(fullReturnPrelimFromBackend) mustBe true
      }

      "must return false when the transaction is absent" in {
        TransactionTaskList.isPrelimTransaction(emptyFullReturn) mustBe false
      }

      "must return false when the transaction description is not answered" in {
        TransactionTaskList.isPrelimTransaction(fullReturnEffectiveDateOnly) mustBe false
      }

      "must return false when any question beyond the prelim is answered, even with NO" in {
        TransactionTaskList.isPrelimTransaction(fullReturnPrelimPlusGeneralNo) mustBe false
        TransactionTaskList.isPrelimTransaction(fullReturnPrelimPlusUseOfLandNo) mustBe false
        TransactionTaskList.isPrelimTransaction(fullReturnPrelimPlusConsiderationNo) mustBe false
      }

      "must return false when the section is partially complete" in {
        TransactionTaskList.isPrelimTransaction(fullReturnMixedResNotGrantOfLeaseMissing) mustBe false
      }

      "must return false when the section is complete" in {
        TransactionTaskList.isPrelimTransaction(fullReturnMixedResNotGrantOfLease) mustBe false
      }
    }

    ".transactionChecks" - {

      "must return Seq(false) when only the prelim is answered" in {
        TransactionTaskList.transactionChecks(fullReturnPrelimOnly) mustBe Seq(false)
      }

      "must return Seq(false) for the prelim payload as persisted" in {
        TransactionTaskList.transactionChecks(fullReturnPrelimFromBackend) mustBe Seq(false)
      }

      "must return the mandatory field checks when started beyond the prelim" in {
        TransactionTaskList.transactionChecks(fullReturnPrelimPlusGeneralNo) mustBe
          TransactionTaskList.mandatoryFieldsDefined(fullReturnPrelimPlusGeneralNo)

        TransactionTaskList.transactionChecks(fullReturnMixedResNotGrantOfLeaseMissing) mustBe
          TransactionTaskList.mandatoryFieldsDefined(fullReturnMixedResNotGrantOfLeaseMissing)
      }

      "must return the mandatory field checks when the section is complete" in {
        val result = TransactionTaskList.transactionChecks(fullReturnMixedResNotGrantOfLease)

        result mustBe TransactionTaskList.mandatoryFieldsDefined(fullReturnMixedResNotGrantOfLease)
        result.forall(identity) mustBe true
      }

      "must return the mandatory field checks when the transaction is absent" in {
        val result = TransactionTaskList.transactionChecks(emptyFullReturn)

        result mustBe TransactionTaskList.mandatoryFieldsDefined(emptyFullReturn)
        result.exists(identity) mustBe false
      }
    }

    ".transactionRowBuilder" - {

      ".isComplete" - {

        "must return true when the transaction is complete and there are no failures" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            TransactionTaskList.transactionRowBuilder(fullReturnComplete, noFailures)
              .isComplete(fullReturnComplete) mustBe true
          }
        }

        "must return false when the transaction is complete but cross-flow reports failures" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            TransactionTaskList.transactionRowBuilder(fullReturnComplete, withFailure(Pages.ReliefReason))
              .isComplete(fullReturnComplete) mustBe false
          }
        }

        "must return false when the transaction is complete but there are multiple failures" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            TransactionTaskList.transactionRowBuilder(fullReturnComplete, multipleFailures)
              .isComplete(fullReturnComplete) mustBe false
          }
        }

        "must return false when the transaction is incomplete, with or without failures" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            TransactionTaskList.transactionRowBuilder(fullReturnMixedResNotGrantOfLeaseMissing, noFailures)
              .isComplete(fullReturnMixedResNotGrantOfLeaseMissing) mustBe false

            TransactionTaskList.transactionRowBuilder(fullReturnMixedResNotGrantOfLeaseMissing, withFailure(Pages.ReliefReason))
              .isComplete(fullReturnMixedResNotGrantOfLeaseMissing) mustBe false
          }
        }

        "must return false when only the prelim is answered" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            TransactionTaskList.transactionRowBuilder(fullReturnPrelimFromBackend, noFailures)
              .isComplete(fullReturnPrelimFromBackend) mustBe false
          }
        }

        "must return false when only the prelim is answered and cross-flow reports failures" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            TransactionTaskList.transactionRowBuilder(fullReturnPrelimFromBackend, multipleFailures)
              .isComplete(fullReturnPrelimFromBackend) mustBe false
          }
        }

        "must return false when the transaction is absent" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val fr = fullReturnComplete.copy(transaction = None)

            TransactionTaskList.transactionRowBuilder(fr, noFailures).isComplete(fr) mustBe false
          }
        }
      }

      ".prerequisitesMet" - {

        "must return true as the transaction row has no prerequisites" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            TransactionTaskList.transactionRowBuilder(fullReturnComplete, noFailures)
              .prerequisitesMet(fullReturnComplete) mustBe true
          }
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

      "must always be editable, whatever the status" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          TransactionTaskList.buildTransactionRow(fullReturnComplete, noFailures).canEdit mustBe true
          TransactionTaskList.buildTransactionRow(fullReturnPrelimOnly, noFailures).canEdit mustBe true
          TransactionTaskList.buildTransactionRow(fullReturnPrelimFromBackend, noFailures).canEdit mustBe true
          TransactionTaskList.buildTransactionRow(fullReturnComplete, multipleFailures).canEdit mustBe true
          TransactionTaskList.buildTransactionRow(fullReturnPrelimFromBackend, multipleFailures).canEdit mustBe true
        }
      }
    }

    ".buildTransactionRow status logic" - {

      "must show 'Not yet started' status and route to Before You Start when only the prelim (type of transaction) is answered" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnPrelimOnly, noFailures)

          result.status mustBe TLNotStarted
          result.url mustBe beforeYouStartUrl
        }
      }

      "must show 'Not yet started' status and route to Before You Start for the prelim payload as persisted" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnPrelimFromBackend, noFailures)

          result.status mustBe TLNotStarted
          result.url mustBe beforeYouStartUrl
        }
      }

      "must show 'In progress' status and route to resume when a question beyond the prelim is answered NO" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val general = TransactionTaskList.buildTransactionRow(fullReturnPrelimPlusGeneralNo, noFailures)
          general.status mustBe TLInProgress
          general.url    mustBe resumeUrl

          val useOfLand = TransactionTaskList.buildTransactionRow(fullReturnPrelimPlusUseOfLandNo, noFailures)
          useOfLand.status mustBe TLInProgress
          useOfLand.url    mustBe resumeUrl

          val consideration = TransactionTaskList.buildTransactionRow(fullReturnPrelimPlusConsiderationNo, noFailures)
          consideration.status mustBe TLInProgress
          consideration.url    mustBe resumeUrl
        }
      }

      "must route to the resume url and show 'In progress' status when mandatory fields beyond the prelim are present but incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnMixedResNotGrantOfLeaseMissing, noFailures)

          result.url mustBe resumeUrl

          result.status mustBe TLInProgress
        }
      }

      "must have Transaction Check Your Answers url when and show 'Complete' status when all mandatory fields are present in transaction" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnMixedResNotGrantOfLease, noFailures)

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

      "must mark the row as invalid when the section is incomplete and there are failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnMixedResNotGrantOfLeaseMissing, withFailure(Pages.ReliefReason))

          result.status mustBe TLInvalid
          result.url mustBe singleEntityUrl
        }
      }

      "must NOT mark the row as invalid when only the prelim is answered and there are failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnPrelimFromBackend, withFailure(Pages.EffectiveDate))

          result.status mustBe TLNotStarted
          result.url mustBe beforeYouStartUrl
        }
      }

      "must NOT mark the row as invalid when nothing has been answered and there are failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete.copy(transaction = None), withFailure(Pages.ReliefReason))

          result.status mustBe TLNotStarted
          result.url mustBe beforeYouStartUrl
        }
      }
    }

    ".buildTransactionRow cross-flow failures before the section is started" - {

      "must show 'Not yet started' and route to Before You Start when only the prelim is answered, with a single failure" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnPrelimOnly, withFailure(Pages.ReliefReason))

          result.status mustNot be(TLInvalid)
          result.status mustBe TLNotStarted
          result.url    mustBe beforeYouStartUrl
        }
      }

      "must show 'Not yet started' and route to Before You Start when only the prelim is answered, with multiple failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnPrelimFromBackend, multipleFailures)

          result.status mustNot be(TLInvalid)
          result.status mustBe TLNotStarted
          result.url    mustBe beforeYouStartUrl
        }
      }

      "must show 'Not yet started' for a prelim-only grant of lease on mixed-use land with failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnPrelimGrantOfLeaseMixedLand, multipleFailures)

          result.status mustNot be(TLInvalid)
          result.status mustBe TLNotStarted
          result.url    mustBe beforeYouStartUrl
        }
      }

      "must ignore failures whatever page they target while only the prelim is answered" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          Seq(Pages.EffectiveDate, Pages.ContractDate, Pages.LandPropertyType, Pages.ReliefReason).foreach { page =>
            val result = TransactionTaskList.buildTransactionRow(fullReturnPrelimFromBackend, withFailure(page))

            result.status mustBe TLNotStarted
            result.url    mustBe beforeYouStartUrl
          }
        }
      }

      "must show 'Not yet started' when the transaction is absent and there are multiple failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete.copy(transaction = None), multipleFailures)

          result.status mustBe TLNotStarted
          result.url    mustBe beforeYouStartUrl
        }
      }

      "must mark the row as invalid once a single question beyond the prelim is answered NO and there are failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          Seq(fullReturnPrelimPlusGeneralNo, fullReturnPrelimPlusUseOfLandNo, fullReturnPrelimPlusConsiderationNo).foreach { fr =>
            val result = TransactionTaskList.buildTransactionRow(fr, withFailure(Pages.ReliefReason))

            result.status mustBe TLInvalid
            result.url    mustBe singleEntityUrl
          }
        }
      }

      "must mark the row as invalid when only a non-prelim field is answered and there are failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnEffectiveDateOnly, withFailure(Pages.EffectiveDate))

          result.status mustBe TLInvalid
          result.url    mustBe singleEntityUrl
        }
      }

      "must start surfacing failures as soon as the user moves beyond the prelim" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val prelimOnly = fullReturnPrelimGrantOfLeaseMixedLand
          val oneAnswer  = prelimOnly.copy(
            transaction = prelimOnly.transaction.map(_.copy(effectiveDate = Some("01/02/2024")))
          )

          val before = TransactionTaskList.buildTransactionRow(prelimOnly, withFailure(Pages.EffectiveDate))
          val after  = TransactionTaskList.buildTransactionRow(oneAnswer, withFailure(Pages.EffectiveDate))

          before.status mustBe TLNotStarted
          before.url    mustBe beforeYouStartUrl

          after.status  mustBe TLInvalid
          after.url     mustBe singleEntityUrl
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

      "must route to the resume url when started beyond the prelim but incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnMixedResNotGrantOfLeaseMissing, noFailures)

          result.url mustBe resumeUrl
        }
      }

      "must route to TransactionBeforeYouStart when only the prelim is answered" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          TransactionTaskList.buildTransactionRow(fullReturnPrelimOnly, noFailures).url mustBe beforeYouStartUrl
          TransactionTaskList.buildTransactionRow(fullReturnPrelimFromBackend, noFailures).url mustBe beforeYouStartUrl
        }
      }

      "must route to TransactionBeforeYouStart when only the prelim is answered, even with failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          TransactionTaskList.buildTransactionRow(fullReturnPrelimOnly, withFailure(Pages.ReliefReason)).url mustBe beforeYouStartUrl
          TransactionTaskList.buildTransactionRow(fullReturnPrelimFromBackend, multipleFailures).url mustBe beforeYouStartUrl
        }
      }

      "must route to TransactionBeforeYouStart when transaction is absent and no failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete.copy(transaction = None), noFailures)

          result.url mustBe beforeYouStartUrl
        }
      }

      "must route to TransactionSingleEntity when there is a single cross-flow failure" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, withFailure(Pages.ReliefReason))

          result.url mustBe singleEntityUrl
        }
      }

      "must route to TransactionSingleEntity when there are multiple cross-flow failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, multipleFailures)

          result.url mustBe singleEntityUrl
        }
      }

      "must route to TransactionSingleEntity for any failure regardless of target page" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val effectiveDateResult = TransactionTaskList.buildTransactionRow(fullReturnComplete, withFailure(Pages.EffectiveDate))
          val contractDateResult  = TransactionTaskList.buildTransactionRow(fullReturnComplete, withFailure(Pages.ContractDate))
          val propertyTypeResult  = TransactionTaskList.buildTransactionRow(fullReturnComplete, withFailure(Pages.LandPropertyType))

          effectiveDateResult.url mustBe singleEntityUrl
          contractDateResult.url  mustBe singleEntityUrl
          propertyTypeResult.url  mustBe singleEntityUrl
        }
      }

      "must route to TransactionBeforeYouStart for a failure when nothing has been answered" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete.copy(transaction = None), withFailure(Pages.ReliefReason))

          result.url mustBe beforeYouStartUrl
        }
      }

      "must prioritise failure routing over completion routing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnComplete, withFailure(Pages.ReliefReason))

          result.url       mustBe singleEntityUrl
          result.url mustNot be(controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
        }
      }

      "must prioritise failure routing over resume routing once started" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = TransactionTaskList.buildTransactionRow(fullReturnMixedResNotGrantOfLeaseMissing, withFailure(Pages.ReliefReason))

          result.url mustBe singleEntityUrl
          result.url mustNot be(resumeUrl)
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

      "must build a TaskListSection with an in progress row routed to resume when started beyond the prelim but incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages           = messages(application)
          implicit val appConfig:        FrontendAppConfig  = application.injector.instanceOf[FrontendAppConfig]

          val section = TransactionTaskList.build(fullReturnMixedResNotGrantOfLeaseMissing, noFailures)
          val row     = section.rows.head

          row.status mustBe TLInProgress
          row.url    mustBe resumeUrl
        }
      }

      "must build a TaskListSection with a not started row routed to Before You Start when only the prelim is answered" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages           = messages(application)
          implicit val appConfig:        FrontendAppConfig  = application.injector.instanceOf[FrontendAppConfig]

          val section = TransactionTaskList.build(fullReturnPrelimFromBackend, noFailures)
          val row     = section.rows.head

          section.heading                  mustBe messagesInstance("tasklist.transactionQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.transactionQuestion.details")
          row.status                       mustBe TLNotStarted
          row.url                          mustBe beforeYouStartUrl
        }
      }

      "must build a TaskListSection with a not started row when only the prelim is answered and cross-flow reports failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages           = messages(application)
          implicit val appConfig:        FrontendAppConfig  = application.injector.instanceOf[FrontendAppConfig]

          val section = TransactionTaskList.build(fullReturnPrelimGrantOfLeaseMixedLand, multipleFailures)
          val row     = section.rows.head

          section.heading mustBe messagesInstance("tasklist.transactionQuestion.heading")
          row.status      mustBe TLNotStarted
          row.url         mustBe beforeYouStartUrl
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
          row.url                               mustBe beforeYouStartUrl
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
          row.url                               mustBe singleEntityUrl
        }
      }
    }
  }
}