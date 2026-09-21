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

package controllers.submission

import base.SpecBase
import constants.FullReturnConstants.{completeFullReturn, completeSubmissionErrorDetails, incompleteFullReturn}
import models.{FullReturn, Submission}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.submission.SubmissionFailedView

class SubmissionFailedControllerSpec extends SpecBase {

  private val summaryError =
    "3001: Your submission failed due to business validation errors. Please see below for details."

  private val ruleError1 =
    "421: As Box 52 part 2 has been completed No or left blank, part 4 must be left blank. Please delete"

  private val ruleError2 =
    "The STORN supplied either is not of the correct length or type (10 numeric)."

  private def errorDetail(position: String, message: Option[String]) =
    completeSubmissionErrorDetails.copy(position = Some(position), errorMessage = message)

  private def fullReturnWith(errors: Seq[models.SubmissionErrorDetails]): FullReturn =
    completeFullReturn.copy(submissionErrorDetails = Some(errors))

  private def runPage(fullReturn: FullReturn) = {
    val mockSessionRepository = mock[SessionRepository]
    val userAnswers           = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
      .build()
  }

  "SubmissionFailed Controller" - {

    "must return OK and show every error message when multiple errors exist" in {
      val application = runPage(fullReturnWith(Seq(
        errorDetail("0", Some(summaryError)),
        errorDetail("1", Some(ruleError1)),
        errorDetail("2", Some(ruleError2))
      )))

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionFailedController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SubmissionFailedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(Seq(summaryError, ruleError1, ruleError2))(request, messages(application)).toString
      }
    }

    "must order error messages by position when they are returned out of order" in {
      val application = runPage(fullReturnWith(Seq(
        errorDetail("2", Some(ruleError2)),
        errorDetail("0", Some(summaryError)),
        errorDetail("1", Some(ruleError1))
      )))

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionFailedController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SubmissionFailedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(Seq(summaryError, ruleError1, ruleError2))(request, messages(application)).toString
      }
    }

    "must return OK and the correct view when a single error message exists" in {
      val application = runPage(fullReturnWith(Seq(errorDetail("0", Some(ruleError2)))))

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionFailedController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SubmissionFailedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq(ruleError2))(request, messages(application)).toString
      }
    }

    "must return OK with no error messages when error details have no message or a blank message" in {
      val application = runPage(fullReturnWith(Seq(
        errorDetail("0", None),
        errorDetail("1", Some("   "))
      )))

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionFailedController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SubmissionFailedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq.empty)(request, messages(application)).toString
        contentAsString(result) mustNot include(ruleError2)
      }
    }

    "must return OK with no error messages when there are no submission error details" in {
      val application = runPage(completeFullReturn.copy(submissionErrorDetails = None))

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionFailedController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SubmissionFailedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq.empty)(request, messages(application)).toString
      }
    }

    "must redirect to the task list when no submission has started and the prerequisite sections are not complete" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(fullReturn = Some(incompleteFullReturn)))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionFailedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must return OK and not redirect when a submission already exists, even if the prerequisite sections are not complete" in {

      val alreadyStartedIncompleteReturn = incompleteFullReturn.copy(submission = Some(Submission(None)))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(fullReturn = Some(alreadyStartedIncompleteReturn)))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionFailedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }
  }
}