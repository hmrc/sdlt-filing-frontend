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

class SubmissionTaskListSpec extends SpecBase {

  private val fullReturnComplete = completeFullReturn
  private val fullReturnIncompleteSubmission = fullReturnComplete.copy(
    submission = Some(completeSubmission.copy(submissionID = None)))
  private val fullReturnMissingSubmission= fullReturnComplete.copy(submission = None)

  "SubmissionTaskList" - {

    ".build" - {
      "must return TaskListSection with correct heading when submission is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.build(fullReturnComplete)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.submissionQuestion.heading")
          result.rows.size mustBe 1
        }
      }

      "must return TaskListSection with correct heading when submission is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.build(emptyFullReturn)

          result mustBe a[TaskListSection]
          result.heading mustBe messagesInstance("tasklist.submissionQuestion.heading")
        }
      }
    }

    ".buildSubmissionRow" - {
      "must return TaskListSectionRow with correct tag id and link text" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete)

          result mustBe a[TaskListSectionRow]
          result.tagId mustBe "submissionQuestionDetailRow"
          messagesInstance(result.messageKey) mustBe messagesInstance("tasklist.submissionQuestion.details")
        }
      }

      "must have Submission Before You Start url when submission is missing" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnMissingSubmission)

          result.url mustBe controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
        }
      }

      "must have Submission Before You Start url when submission is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnIncompleteSubmission)

          result.url mustBe controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
        }
      }

      //TODO UPDATE to DTR-5731 Success page
      "must have Submission Success page url when submission is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete)

          result.url mustBe controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
        }
      }

      "must show completed status when submission ID is present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete)

          result.status mustBe TLCompleted
        }
      }

      "must show 'Cannot start' status and display hint when vendor is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(completeFullReturn.copy(vendor = None))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.submissionQuestion.hint")
        }
      }

      "must show 'Cannot start' status and display hint when purchaser is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(completeFullReturn.copy(purchaser = None))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.submissionQuestion.hint")
        }
      }

      "must show 'Cannot start' status and display hint when land is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(completeFullReturn.copy(land = None))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.submissionQuestion.hint")
        }
      }

      "must show 'Cannot start' status and display hint when transaction is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(completeFullReturn.copy(transaction = None))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.submissionQuestion.hint")
        }
      }

      "must show 'Cannot start' status and display hint when tax calculation is absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(completeFullReturn.copy(taxCalculation = None))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.submissionQuestion.hint")
        }
      }

      "must show 'Cannot start' status and display hint when vendor agent started but is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(completeFullReturn
            .copy(returnAgent = Some(Seq(completeReturnAgentVendor.copy(name = None)))))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.submissionQuestion.hint")
        }
      }

      "must show 'Cannot start' status and display hint when purchaser agent started but is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(completeFullReturn
            .copy(returnAgent = Some(Seq(completeReturnAgent.copy(name = None)))))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.submissionQuestion.hint")
        }
      }

      "must show 'Cannot start' status and display hint when lease is required but is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete
            .copy(transaction = Some(completeTransaction
              .copy(transactionDescription = Some("L"))), lease = Some(completeLease
              .copy(leaseType = None))))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.submissionQuestion.hint")
        }
      }

      "must show 'Cannot start' status and display hint when residency is required but is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete
            .copy(residency = Some(completeResidency
              .copy(isCloseCompany = None)), land = Some(Seq(completeLand
              .copy(propertyType = Some("01"))))))

          result.status mustBe TLCannotStart
          result.hint mustBe Some("tasklist.submissionQuestion.hint")
        }
      }

      "must show 'Not started' status and hide hint when no vendor agent exists but purchaser agent is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete.copy(submission = None))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not started' status and hide hint when vendor agent is complete and purchaser agent not started" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete
            .copy(submission = None, returnAgent = Some(Seq(completeReturnAgentVendor.copy(name = Some("Test"))))))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not started' status and hide hint when no purchaser agent exists but vendor agent is complete" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete.copy(submission = None, returnAgent = Some(Seq(completeReturnAgentVendor))))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not started' status and hide hint when purchaser agent is complete and vendor agent not started" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete.copy(submission = None))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not started' status and hide hint when lease is not required" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete.copy(submission = None, lease = None))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not started' status and hide hint when lease is required and complete" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete
            .copy(submission = None, transaction = Some(completeTransaction
                .copy(transactionDescription = Some("L")))))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

      "must show 'Not started' status and hide hint when residency is not required" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete
            .copy(submission = None, residency = None, land = Some(Seq(completeLand.copy(propertyType = Some("02"))))))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }


      "must show 'Not started' status and hide hint when residency is required and complete" in {

        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = SubmissionTaskList.buildSubmissionRow(fullReturnComplete
            .copy(submission = None))

          result.status mustBe TLNotStarted
          result.hint mustBe None
        }
      }

    }

    ".canStartSubmission" - {
      "must return false when vendor is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {

          val fullReturn = fullReturnComplete.copy(vendor = None)
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe false
        }
      }

      "must return false when land is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {

          val fullReturn = fullReturnComplete.copy(land = None)
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe false
        }
      }

      "must return false when transaction is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {

          val fullReturn = fullReturnComplete.copy(transaction = None)
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe false
        }
      }

      "must return false when purchaser is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {

          val fullReturn = fullReturnComplete.copy(purchaser = None)
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe false
        }
      }

      "must return false when tax calculation is incomplete" in {
        val application = applicationBuilder().build()

        running(application) {

          val fullReturn = fullReturnComplete.copy(taxCalculation = None)
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe false
        }
      }

      "must return true when no vendor agent exists but purchaser agent is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturnComplete) mustBe true
        }
      }

      "must return true when vendor agent is complete and purchaser agent not started" in {
        val application = applicationBuilder().build()

        val fullReturn = fullReturnComplete.copy(returnAgent = Some(Seq(completeReturnAgentVendor.copy(name = Some("Test")))))
        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe true
        }
      }

      "must return false when vendor agent is incomplete and purchaser agent not started" in {
        val application = applicationBuilder().build()

        val fullReturn = fullReturnComplete.copy(returnAgent = Some(Seq(completeReturnAgentVendor.copy(name = None))))
        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe false
        }
      }

      "must return true when no purchaser agent exists but vendor agent is complete" in {
        val application = applicationBuilder().build()

        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturnComplete.copy(returnAgent = Some(Seq(completeReturnAgentVendor)))) mustBe true
        }
      }

      "must return true when purchaser agent is complete and vendor agent not started" in {
        val application = applicationBuilder().build()

        val fullReturn = fullReturnComplete
        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe true
        }
      }

      "must return false when purchaser agent is incomplete and vendor agent not started" in {
        val application = applicationBuilder().build()

        val fullReturn = fullReturnComplete.copy(returnAgent = Some(Seq(completeReturnAgent.copy(name = None))))
        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe false
        }
      }

      "must return true when lease is not required" in {
        val application = applicationBuilder().build()

        val fullReturn = fullReturnComplete.copy(lease = None)
        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe true
        }
      }

      "must return true when lease is required and complete" in {
        val application = applicationBuilder().build()

        val fullReturn = fullReturnComplete.copy(transaction = Some(completeTransaction.copy(transactionDescription = Some("L"))))
        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe true
        }
      }

      "must return false when lease is required and incomplete" in {
        val application = applicationBuilder().build()

        val fullReturn = fullReturnComplete.copy(transaction = Some(completeTransaction.copy(transactionDescription = Some("L"))), lease = Some(completeLease.copy(leaseType = None)))
        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe false
        }
      }

      "must return true when residency is not required" in {
        val application = applicationBuilder().build()

        val fullReturn = fullReturnComplete.copy(residency = None, land = Some(Seq(completeLand.copy(propertyType = Some("02")))))
        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe true
        }
      }

      "must return true when residency is required and complete" in {
        val application = applicationBuilder().build()

        val fullReturn = fullReturnComplete
        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe true
        }
      }

      "must return false when residency is required and incomplete" in {
        val application = applicationBuilder().build()

        val fullReturn = fullReturnComplete.copy(residency = Some(completeResidency.copy(isCloseCompany = None)), land = Some(Seq(completeLand.copy(propertyType = Some("01")))))
        running(application) {
          SubmissionTaskList.canStartSubmission(fullReturn) mustBe false
        }
      }
    }

    "integration" - {
      "must build complete TaskListSection with completed row when submission present" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = SubmissionTaskList.build(fullReturnComplete)
          val row = section.rows.head

          section.heading mustBe messagesInstance("tasklist.submissionQuestion.heading")
          messagesInstance(row.messageKey) mustBe messagesInstance("tasklist.submissionQuestion.details")
          row.status mustBe TLCompleted
          row.url mustBe controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
        }
      }

      "must build complete TaskListSection with cannot start row when submission absent" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

          val section = SubmissionTaskList.build(emptyFullReturn)
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
