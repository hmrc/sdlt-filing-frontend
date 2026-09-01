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
import models.Lease
import play.api.i18n.Messages
import play.api.test.Helpers.running
import services.crossflow.{CrossFlowTarget, Pages, ReturnSection, SectionStatus}

class LeaseTaskListSpec extends SpecBase {

  // The "grant of lease" flag is derived from the TRANSACTION description ("L"), not the lease's
  // own leaseType. completeTransaction is a grant of lease, so the "not grant of lease" fixtures
  // must override the transaction to a non-"L" type ("F") to only require the 7 general fields.
  private val notGrantOfLeaseTransaction = Some(completeTransaction.copy(transactionDescription = Some("F")))

  private val fullReturnComplete        = completeFullReturn
  private val fullReturnIncompleteLease = fullReturnComplete.copy(
    lease = Some(completeLease.copy(leaseType = None)))
  private val fullReturnNotGrantOfLease = fullReturnComplete.copy(
    transaction = notGrantOfLeaseTransaction,
    lease = Some(Lease(
      leaseType = Some("A"),
      contractStartDate = Some("02-02-2026"),
      contractEndDate = Some("07-07-2000"),
      startingRent = Some("12345.00"),
      startingRentEndDate = Some("01-08-2024"),
      laterRentKnown = Some("NO")
    )))
  private val fullReturnSomeMandatoryFieldsMissing = fullReturnComplete.copy(
    transaction = notGrantOfLeaseTransaction,
    lease = Some(Lease(
      leaseType = None,
      contractStartDate = Some("02-02-2026"),
      contractEndDate = None,
      startingRent = None,
      startingRentEndDate = Some("01-08-2024"),
      laterRentKnown = None
    )))
  private val fullReturnAllMandatoryFieldsMissing = fullReturnComplete.copy(
    transaction = notGrantOfLeaseTransaction,
    lease = None
  )
  private val fullReturnGrantOfLease = fullReturnComplete.copy(
    lease = Some(Lease(
      leaseType = Some("L"),
      contractStartDate = Some("02-02-2026"),
      contractEndDate = Some("07-07-2000"),
      startingRent = Some("12345.00"),
      startingRentEndDate = Some("01-08-2024"),
      laterRentKnown = Some("NO"),
      totalPremiumPayable = Some("12345.00"),
      netPresentValue = Some("12345.00"),
      isAnnualRentOver1000 = Some("YES")
    )))
  private val fullReturnGOTSomeMandatoryFieldsMissing = fullReturnComplete.copy(
    lease = Some(Lease(
      leaseType = Some("L"),
      contractStartDate = None,
      contractEndDate = Some("07-07-2000"),
      startingRent = Some("12345.00"),
      startingRentEndDate = Some("01-08-2024"),
      laterRentKnown = Some("NO"),
      totalPremiumPayable = None,
      netPresentValue = Some("12345.00"),
      isAnnualRentOver1000 = None
    )))
  private val fullReturnGOLAllMandatoryFieldsMissing = fullReturnComplete.copy(
    lease = None
  )
  private val fullReturnMissingLease = fullReturnComplete.copy(lease = None)
  private val noFailures: SectionStatus =
    SectionStatus(ReturnSection.Lease, hasFailures = false, ruleIds = Nil, messageKeys = Nil, targets = Nil)
  private val cf5aFailureStatus: SectionStatus = SectionStatus(
    section     = ReturnSection.Lease,
    hasFailures = true,
    ruleIds     = Seq("Cf-5a"),
    messageKeys = Seq("crossflow.lease.Cf-5a.body"),
    targets     = Seq(CrossFlowTarget(Pages.LeaseType, "value"))
  )

  // NOTE: evaluated inside each running(application) block (not as a class val) so the reverse
  // route picks up the app's context path (/stamp-duty-land-tax-filing) — same as the other url
  // assertions in this spec.
  private def resumeUrl: String = controllers.routes.ResumeSectionController.resume("lease", None).url

  "LeaseTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when lease is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.leaseQuestion.heading")
          result.rows.size mustBe 1
        }
      }

      "must return TaskListSection with correct heading when lease is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.build(fullReturnMissingLease)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.leaseQuestion.heading")
        }
      }

      "must default to noFailures when no SectionStatus is provided" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.build(fullReturnComplete)
          val row    = result.rows.head

          row.url mustBe controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad().url
          row.status mustBe TLCompleted
        }
      }

      "must propagate a failing status to the row when provided" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.build(fullReturnComplete, cf5aFailureStatus)
          val row    = result.rows.head

          row.url mustBe controllers.lease.routes.LeaseSingleEntityController.onPageLoad().url
          row.status mustBe TLInvalid
        }
      }
    }

    ".mandatoryFieldsDefined" - {
      "must return a sequence of true if lease exists and mandatory fields are defined" in {
        val result = LeaseTaskList.mandatoryFieldsDefined(fullReturnNotGrantOfLease)
        result mustBe Seq(true, true, true, true, true, true)
      }

      "must return a sequence of true and false if lease exists but some mandatory field are missing" in {
        val result = LeaseTaskList.mandatoryFieldsDefined(fullReturnSomeMandatoryFieldsMissing)
        result mustBe Seq(false, true, false, false, true, false)
      }

      "must return a sequence of false if lease exists but all mandatory fields are missing" in {
        val result = LeaseTaskList.mandatoryFieldsDefined(fullReturnAllMandatoryFieldsMissing)
        result mustBe Seq(false, false, false, false, false, false)
      }

      "must return false if lease is absent" in {
        val result = LeaseTaskList.mandatoryFieldsDefined(emptyFullReturn)
        result mustBe Seq(false, false, false, false, false, false)
      }

      "must include the three grant-of-lease checks when the transaction is a grant of lease" in {
        val result = LeaseTaskList.mandatoryFieldsDefined(fullReturnGrantOfLease)
        result.size mustBe 9
        result.forall(identity) mustBe true
      }

      "must include the three grant-of-lease checks (some false) when the grant-of-lease lease is partial" in {
        val result = LeaseTaskList.mandatoryFieldsDefined(fullReturnGOTSomeMandatoryFieldsMissing)
        result.size mustBe 9
        result.contains(false) mustBe true
      }
    }

    ".isLeaseComplete" - {

      "when the lease is not grant of lease" - {

        "must return true if lease exists and mandatory fields are defined" in {
          val result = LeaseTaskList.isLeaseComplete(fullReturnNotGrantOfLease)
          result mustBe true
        }

        "must return false if lease exists but some mandatory field are missing" in {
          val result = LeaseTaskList.isLeaseComplete(fullReturnSomeMandatoryFieldsMissing)
          result mustBe false
        }

        "must return false if lease exists but all mandatory fields are missing" in {
          val result = LeaseTaskList.isLeaseComplete(fullReturnAllMandatoryFieldsMissing)

          result mustBe false
        }

        "must return false if lease is absent" in {
          val result = LeaseTaskList.isLeaseComplete(emptyFullReturn)
          result mustBe false
        }
      }

      "when the lease is grant of lease" - {

        "must return true if lease exists and mandatory fields are defined" in {
          val result = LeaseTaskList.isLeaseComplete(fullReturnGrantOfLease)
          result mustBe true
        }

        "must return false if lease exists but some mandatory field are missing" in {
          val result = LeaseTaskList.isLeaseComplete(fullReturnGOTSomeMandatoryFieldsMissing)
          result mustBe false
        }

        "must return false if lease exists but all mandatory fields are missing" in {
          val result = LeaseTaskList.isLeaseComplete(fullReturnGOLAllMandatoryFieldsMissing)
          result mustBe false
        }

        "must return false if lease is absent" in {
          val result = LeaseTaskList.isLeaseComplete(emptyFullReturn)
          result mustBe false
        }
      }

      "must ignore cross-flow failures — it only reflects the mandatory fields" in {
        LeaseTaskList.isLeaseComplete(fullReturnComplete) mustBe true
      }
    }

    ".hasStarted" - {

      "must return false when the lease is absent" in {
        LeaseTaskList.hasStarted(fullReturnMissingLease) mustBe false
      }

      "must return false when no mandatory fields are answered" in {
        LeaseTaskList.hasStarted(fullReturnAllMandatoryFieldsMissing) mustBe false
      }

      "must return true when some mandatory fields are answered" in {
        LeaseTaskList.hasStarted(fullReturnSomeMandatoryFieldsMissing) mustBe true
      }

      "must return true when the lease is complete" in {
        LeaseTaskList.hasStarted(fullReturnComplete) mustBe true
      }

      "must return false when lease is empty except for isAnnualRentOver1000 and is set to NO" in {
        val fullReturn = fullReturnComplete.copy(
          lease = Some(incompleteLease.copy(isAnnualRentOver1000 = Some("no"))))
        LeaseTaskList.hasStarted(fullReturn) mustBe false
      }

      "must return true when lease is empty except for isAnnualRentOver1000 and is set to YES" in {
        val fullReturn = fullReturnComplete.copy(
          lease = Some(incompleteLease.copy(isAnnualRentOver1000 = Some("yes"))))
        LeaseTaskList.hasStarted(fullReturn) mustBe true
      }

      "must return true when lease contains isAnnualRentOver1000 and is set to NO and any other mandatory field" in {
        val fullReturn = fullReturnComplete.copy(
          lease = Some(incompleteLease.copy(isAnnualRentOver1000 = Some("no"), leaseType = Some("N"))))
        LeaseTaskList.hasStarted(fullReturn) mustBe true
      }

      "must return true when lease contains isAnnualRentOver1000 and is set to YES and any other mandatory field" in {
        val fullReturn = fullReturnComplete.copy(
          lease = Some(incompleteLease.copy(isAnnualRentOver1000 = Some("yes"), leaseType = Some("N"))))
        LeaseTaskList.hasStarted(fullReturn) mustBe true
      }
    }

    ".isLeaseApplicable" - {

      "must return true when the transaction is a grant of lease" in {
        LeaseTaskList.isLeaseApplicable(fullReturnComplete) mustBe true
      }

      "must return false when the transaction is not a lease type" in {
        val fr = fullReturnComplete.copy(transaction = notGrantOfLeaseTransaction)
        LeaseTaskList.isLeaseApplicable(fr) mustBe false
      }

      "must return false when the transaction is absent" in {
        LeaseTaskList.isLeaseApplicable(fullReturnComplete.copy(transaction = None)) mustBe false
      }
    }

    ".leaseRowBuilder" - {

      ".isComplete" - {

        "must return true when the lease is complete and there are no failures" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            LeaseTaskList.leaseRowBuilder(fullReturnComplete, noFailures)
              .isComplete(fullReturnComplete) mustBe true
          }
        }

        "must return false when the lease is complete but cross-flow reports failures" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            LeaseTaskList.leaseRowBuilder(fullReturnComplete, cf5aFailureStatus)
              .isComplete(fullReturnComplete) mustBe false
          }
        }

        "must return false when the lease is incomplete, with or without failures" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            LeaseTaskList.leaseRowBuilder(fullReturnSomeMandatoryFieldsMissing, noFailures)
              .isComplete(fullReturnSomeMandatoryFieldsMissing) mustBe false

            LeaseTaskList.leaseRowBuilder(fullReturnSomeMandatoryFieldsMissing, cf5aFailureStatus)
              .isComplete(fullReturnSomeMandatoryFieldsMissing) mustBe false
          }
        }

        "must return false when the lease is absent" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            LeaseTaskList.leaseRowBuilder(fullReturnMissingLease, noFailures)
              .isComplete(fullReturnMissingLease) mustBe false
          }
        }
      }

      ".prerequisitesMet" - {

        "must return true as the lease row has no prerequisites" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            LeaseTaskList.leaseRowBuilder(fullReturnComplete, noFailures)
              .prerequisitesMet(fullReturnComplete) mustBe true
          }
        }
      }
    }

    ".buildLeaseRow" - {
      "must return TaskListSectionRow with correct tag id and link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnComplete, noFailures)

          result mustBe a[TaskListSectionRow]
          result.tagId mustBe "leaseQuestionDetailRow"
          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.leaseQuestion.details")
        }
      }

      "must have Lease Before You Start url when lease is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnMissingLease, noFailures)

          result.url mustBe controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url
        }
      }

      "when the lease is not grant of lease" - {

        "must have Lease Before You Start url and show 'Not yet started' status when all mandatory fields are missing" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = LeaseTaskList.buildLeaseRow(fullReturnAllMandatoryFieldsMissing, noFailures)

            result.url mustBe controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url

            result.status mustBe TLNotStarted
          }
        }

        "must have Lease resume url and show 'In progress' status when some mandatory fields are missing" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = LeaseTaskList.buildLeaseRow(fullReturnSomeMandatoryFieldsMissing, noFailures)

            result.url mustBe resumeUrl

            result.status mustBe TLInProgress
          }
        }

        "must have Lease Check your answers url and show 'Complete' status when all mandatory fields are present" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = LeaseTaskList.buildLeaseRow(fullReturnNotGrantOfLease, noFailures)

            result.url mustBe controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad().url

            result.status mustBe TLCompleted
          }
        }
      }

      "when the lease is grant of lease" - {

        "must have Lease Before You Start url and show 'Not yet started' status when all mandatory fields are missing" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = LeaseTaskList.buildLeaseRow(fullReturnGOLAllMandatoryFieldsMissing, noFailures)

            result.url mustBe controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url

            result.status mustBe TLNotStarted
          }
        }

        "must have Lease resume url and show 'In progress' status when some mandatory fields are missing" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = LeaseTaskList.buildLeaseRow(fullReturnGOTSomeMandatoryFieldsMissing, noFailures)

            result.url mustBe resumeUrl

            result.status mustBe TLInProgress
          }
        }

        "must have Lease Check your answers url and show 'Complete' status when all mandatory fields are present" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = LeaseTaskList.buildLeaseRow(fullReturnGrantOfLease, noFailures)

            result.url mustBe controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad().url

            result.status mustBe TLCompleted
          }
        }
      }

      "when cross-flow reports failures" - {

        "must have Lease Single Entity url and show 'Invalid' status when the lease is complete" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = LeaseTaskList.buildLeaseRow(fullReturnComplete, cf5aFailureStatus)

            result.url mustBe controllers.lease.routes.LeaseSingleEntityController.onPageLoad().url
            result.status mustBe TLInvalid
          }
        }

        "must have Lease Single Entity url and show 'Invalid' status for a complete grant of lease" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = LeaseTaskList.buildLeaseRow(fullReturnGrantOfLease, cf5aFailureStatus)

            result.url mustBe controllers.lease.routes.LeaseSingleEntityController.onPageLoad().url
            result.status mustBe TLInvalid
          }
        }

        "must show 'Invalid' status and route to the cross-flow target when the lease is incomplete" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = LeaseTaskList.buildLeaseRow(fullReturnIncompleteLease, cf5aFailureStatus)

            result.status mustBe TLInvalid
            result.url must endWith("update-lease-type")
          }
        }

        "must show 'Invalid' status even when nothing has been answered" in {
          val application = applicationBuilder().build()

          running(application) {
            implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = LeaseTaskList.buildLeaseRow(fullReturnAllMandatoryFieldsMissing, cf5aFailureStatus)

            result.status mustBe TLInvalid
          }
        }
      }

      "must show 'Complete' status when lease is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnComplete, noFailures)

          result.status mustBe TLCompleted
        }
      }

      "must show 'Not yet started' status when lease is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnMissingLease, noFailures)

          result.status mustBe TLNotStarted
        }
      }

      "must show 'Not yet started' status when lease only has isAnnualRentOver1000 and it's set to NO" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val fullReturn = fullReturnComplete.copy(
            lease = Some(incompleteLease.copy(isAnnualRentOver1000 = Some("no"))))
          val result = LeaseTaskList.buildLeaseRow(fullReturn, noFailures)

          result.status mustBe TLNotStarted
        }
      }

      "must show 'In Progress' status when lease only has isAnnualRentOver1000 and it's set to YES" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val fullReturn = fullReturnComplete.copy(
            lease = Some(incompleteLease.copy(isAnnualRentOver1000 = Some("yes"))))
          val result = LeaseTaskList.buildLeaseRow(fullReturn, noFailures)

          result.status mustBe TLInProgress
        }
      }

      "must show 'In Progress' status when lease has isAnnualRentOver1000 and it's set to NO and another mandatory field" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val fullReturn = fullReturnComplete.copy(
            lease = Some(incompleteLease.copy(isAnnualRentOver1000 = Some("no"), leaseType = Some("N"))))
          val result = LeaseTaskList.buildLeaseRow(fullReturn, noFailures)

          result.status mustBe TLInProgress
        }
      }

      "must show 'In Progress' status when lease has isAnnualRentOver1000 and it's set to YES and another mandatory field" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val fullReturn = fullReturnComplete.copy(
            lease = Some(incompleteLease.copy(isAnnualRentOver1000 = Some("yes"), leaseType = Some("N"))))
          val result = LeaseTaskList.buildLeaseRow(fullReturn, noFailures)

          result.status mustBe TLInProgress
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with 'Complete' row when lease present and no failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LeaseTaskList.build(fullReturnComplete)
          val row     = section.rows.head

          section.heading mustBe messagesInstance("tasklist.leaseQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.leaseQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad().url
        }
      }

      "must build a TaskListSection with an 'In progress' row routed to resume when the lease is partial" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LeaseTaskList.build(fullReturnSomeMandatoryFieldsMissing)
          val row     = section.rows.head

          row.status mustBe TLInProgress
          row.url mustBe resumeUrl
        }
      }

      "must build complete TaskListSection with 'Not yet started' row when lease absent but prerequisites complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LeaseTaskList.build(fullReturnMissingLease)
          val row     = section.rows.head

          section.heading mustBe messagesInstance("tasklist.leaseQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.leaseQuestion.details")
          row.status mustBe TLNotStarted
          row.url mustBe controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url
        }
      }

      "must build TaskListSection with single-entity url and 'Invalid' row when cross-flow failures are reported" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LeaseTaskList.build(fullReturnComplete, cf5aFailureStatus)
          val row     = section.rows.head

          section.heading mustBe messagesInstance("tasklist.leaseQuestion.heading")
          row.url mustBe controllers.lease.routes.LeaseSingleEntityController.onPageLoad().url
          row.status mustBe TLInvalid
        }
      }
    }
  }
}