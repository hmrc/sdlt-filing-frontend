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
import models.FullReturn
import play.api.i18n.Messages
import play.api.test.Helpers.running

class SubmissionTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val fullReturnIncompleteSubmission = fullReturnComplete.copy(
    submission = Some(completeSubmission.copy(submissionID = None)))
  private val fullReturnMissingSubmission = fullReturnComplete.copy(submission = None)

  private def fullReturnWithStatus(status: Option[String]): FullReturn =
    fullReturnComplete.copy(submission = Some(completeSubmission.copy(submissionStatus = status)))

  private val fullReturnSubmitted = fullReturnWithStatus(Some("SUBMITTED"))

  private val completeRow: TaskListSectionRow =
    TaskListSectionRow("some.key", "some/url", "someTagId", TLCompleted)

  private val incompleteRow: TaskListSectionRow =
    TaskListSectionRow("some.key", "some/url", "someTagId", TLNotStarted)

  private val completeSections: Seq[TaskListSection] =
    Seq(TaskListSection("some heading", Seq(completeRow)))

  private val incompleteSections: Seq[TaskListSection] =
    Seq(TaskListSection("some heading", Seq(incompleteRow)))

  "SubmissionTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when preceding sections are complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.build(fullReturnComplete, completeSections)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.submissionQuestion.heading")
          result.rows.size mustBe 1
        }
      }

      "must return TaskListSection with correct heading when preceding sections are incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.build(emptyFullReturn, incompleteSections)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.submissionQuestion.heading")
        }
      }
    }

    ".buildSubmissionRow" - {

      "must return TaskListSectionRow with correct tag id and link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val messagesInstance: Messages = messages(application)

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete, readyToSubmit = true)

          result mustBe a[TaskListSectionRow]
          result.tagId mustBe "submissionQuestionDetailRow"
          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.submissionQuestion.details")
        }
      }

      "must have Submission Before You Start url when submission is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnMissingSubmission, readyToSubmit = true)

          result.url mustBe controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Submission Before You Start url when submission is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val fullReturnIncompleteSubmission = fullReturnComplete.copy(
            submission = Some(completeSubmission.copy(submissionID = None, submissionStatus = None))
          )

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnIncompleteSubmission, readyToSubmit = true)

          result.url mustBe controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Submission Before You Start url when submission ID is present but status is empty" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnWithStatus(None), readyToSubmit = true)

          result.url mustBe controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Submission Before You Start url when submission status is STARTED (ReSubmit)" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnWithStatus(Some("STARTED")), readyToSubmit = true)

          result.url mustBe controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Submission Success page url when submission status is SUBMITTED" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnSubmitted, readyToSubmit = true)

          result.url mustBe controllers.submission.routes.SubmissionCompleteController.onPageLoad().url
        }
      }

      "must have Submission Success page url when submission status is SUBMITTED_NO_RECEIPT" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnWithStatus(Some("SUBMITTED_NO_RECEIPT")), readyToSubmit = true)

          result.url mustBe controllers.submission.routes.SubmissionCompleteController.onPageLoad().url
        }
      }

      "must have Submission Awaiting Confirmation url when submission status is ACCEPTED" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnWithStatus(Some("ACCEPTED")), readyToSubmit = true)

          result.url mustBe controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad().url
        }
      }

      "must have Submission Failed url when submission status is DEPARTMENTAL_ERROR" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnWithStatus(Some("DEPARTMENTAL_ERROR")), readyToSubmit = true)

          result.url mustBe controllers.submission.routes.SubmissionFailedController.onPageLoad().url
        }
      }

      "must show 'Complete' status when submission ID is present and readyToSubmit is true" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete, readyToSubmit = true)

          result.status mustBe TLCompleted
        }
      }

      "must show 'Cannot start yet' status and display hint when readyToSubmit is false" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnMissingSubmission, readyToSubmit = false)

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.submissionQuestion.hint")
        }
      }

      "must show 'Cannot start yet' status and display hint when readyToSubmit is false even if submission is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete, readyToSubmit = false)

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.submissionQuestion.hint")
        }
      }

      "must show 'In progress' status and hide hint when submission has started but is not complete and readyToSubmit is true" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnIncompleteSubmission, readyToSubmit = true)

          result.status mustBe TLInProgress
          result.hint mustBe None
        }
      }

      "must show 'Not yet started' status and hide hint when submission is missing and readyToSubmit is true" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnMissingSubmission, readyToSubmit = true)

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with 'Complete' row when submission is submitted and preceding sections complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = SubmissionTaskList.build(fullReturnSubmitted, completeSections)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.submissionQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.submissionQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.submission.routes.SubmissionCompleteController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with 'Not yet started' row when submission absent and preceding sections complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = SubmissionTaskList.build(fullReturnComplete.copy(submission = None), completeSections)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.submissionQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.submissionQuestion.details")
          row.status mustBe TLNotStarted
          row.url mustBe controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with 'Cannot start yet' row when preceding sections are incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = SubmissionTaskList.build(emptyFullReturn, incompleteSections)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.submissionQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.submissionQuestion.details")
          row.status mustBe TLCannotStart
          row.url mustBe controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
        }
      }
    }
  }

}