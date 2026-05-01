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

class LeaseTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val fullReturnIncompleteLease = fullReturnComplete.copy(
    lease = Some(completeLease.copy(leaseType = None)))
  private val fullReturnMissingLease = fullReturnComplete.copy(lease = None)

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

          val result = LeaseTaskList.build(emptyFullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.leaseQuestion.heading")
        }
      }
    }

    ".buildLeaseRow" - {
      "must return TaskListSectionRow with correct tag id and link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnComplete)

          result mustBe a[TaskListSectionRow]
          result.tagId mustBe "leaseQuestionDetailRow"
          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.leaseQuestion.details")
        }
      }

      "must have Lease Before You Start url when lease is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnMissingLease)

          result.url mustBe controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Lease Before You Start url when lease is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnIncompleteLease)

          result.url mustBe controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url
        }
      }

      // TODO: DTR-3545 - change to LeaseCheckYourAnswersController once implemented
      "must have Lease Check your answers url when lease is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnComplete)

          result.url mustBe controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must show completed status when lease is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(fullReturnComplete)

          result.status mustBe TLCompleted
        }
      }

      "must show cannot start status when lease is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = LeaseTaskList.buildLeaseRow(emptyFullReturn)

          result.status mustBe TLCannotStart
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with completed row when lease present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LeaseTaskList.build(fullReturnComplete)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.leaseQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.leaseQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with cannot start row when lease absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = LeaseTaskList.build(emptyFullReturn)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.leaseQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.leaseQuestion.details")
          row.status mustBe TLCannotStart
          row.url mustBe controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url
        }
      }
    }
  }

}
