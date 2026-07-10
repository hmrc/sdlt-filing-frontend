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
import services.crossflow.{CrossFlowTarget, Pages, ReturnSection, SectionStatus}

class LeaseTaskListSpec extends SpecBase {

  private val fullReturnComplete        = completeFullReturn
  private val fullReturnIncompleteLease = fullReturnComplete.copy(
    lease = Some(completeLease.copy(leaseType = None)))
  private val fullReturnMissingLease    = fullReturnComplete.copy(lease = None)

  private val noFailures: SectionStatus =
    SectionStatus(ReturnSection.Lease, hasFailures = false, ruleIds = Nil, messageKeys = Nil, targets = Nil)

  private val cf5aFailureStatus: SectionStatus = SectionStatus(
    section     = ReturnSection.Lease,
    hasFailures = true,
    ruleIds     = Seq("Cf-5a"),
    messageKeys = Seq("crossflow.lease.Cf-5a.body"),
    targets     = Seq(CrossFlowTarget(Pages.LeaseType, "value"))
  )

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
        }
      }
    }

    ".isLeaseComplete" - {

      "must return true if lease exists and leaseType is defined" in {
        val result = LeaseTaskList.isLeaseComplete(fullReturnComplete)
        result mustBe true
      }

      "must return false if lease exists but leaseType is missing" in {
        val result = LeaseTaskList.isLeaseComplete(
          fullReturnComplete.copy(lease = Some(completeLease.copy(leaseType = None)))
        )

        result mustBe false
      }

      "must return false if lease is absent" in {
        val result = LeaseTaskList.isLeaseComplete(fullReturnMissingLease)
        result mustBe false
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

      "must have Lease Before You Start url when lease is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnIncompleteLease, noFailures)

          result.url mustBe controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Lease Check your answers url when lease is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnComplete, noFailures)

          result.url mustBe controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad().url
        }
      }

      "must have Lease Single Entity url when cross-flow reports failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnComplete, cf5aFailureStatus)

          result.url mustBe controllers.lease.routes.LeaseSingleEntityController.onPageLoad().url
        }
      }

      "must route to before you start controller when the lease is incomplete and there are failures" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          // Failures take precedence over completion/start routing
          val result = LeaseTaskList.buildLeaseRow(fullReturnIncompleteLease, cf5aFailureStatus)

          result.url mustBe controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url
        }
      }

      "must show completed status when lease is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnComplete, noFailures)

          result.status mustBe TLCompleted
        }
      }

      "must show not started status when lease is absent but prerequisites are complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnMissingLease, noFailures)

          result.status mustBe TLNotStarted
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with completed row when lease present and no failures" in {
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

      "must build complete TaskListSection with not started row when lease absent but prerequisites complete" in {
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

      "must build TaskListSection with single-entity url when cross-flow failures are reported" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LeaseTaskList.build(fullReturnComplete, cf5aFailureStatus)
          val row     = section.rows.head

          section.heading mustBe messagesInstance("tasklist.leaseQuestion.heading")
          row.url mustBe controllers.lease.routes.LeaseSingleEntityController.onPageLoad().url
        }
      }
    }
  }
}