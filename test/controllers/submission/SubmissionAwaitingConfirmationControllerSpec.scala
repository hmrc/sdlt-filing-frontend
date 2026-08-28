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
import constants.FullReturnConstants.completeFullReturn
import models.Submission
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.submission.SubmissionAwaitingConfirmationView

class SubmissionAwaitingConfirmationControllerSpec extends SpecBase {

  "SubmissionAwaitingConfirmation Controller" - {

    "must return OK and the correct view for a GET when submission status is ACCEPTED" in {

      val testFullReturn = completeFullReturn.copy(submission = Some(Submission(None, submissionStatus = Some("ACCEPTED"))))
      val testUserAnswers = emptyUserAnswers.copy(fullReturn = Some(testFullReturn))

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionAwaitingConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must redirect to the task list when no submission exists" in {

      val testFullReturn = completeFullReturn.copy(submission = None)
      val testUserAnswers = emptyUserAnswers.copy(fullReturn = Some(testFullReturn))

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to the task list when fullReturn is absent" in {

      val testUserAnswers = emptyUserAnswers.copy(fullReturn = None)

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to submission before you start when a submission exists but status is not ACCEPTED" in {

      val testFullReturn = completeFullReturn.copy(submission = Some(Submission(None, submissionStatus = Some("STARTED"))))
      val testUserAnswers = emptyUserAnswers.copy(fullReturn = Some(testFullReturn))

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
      }
    }

    "must redirect to submission before you start when a submission exists and status is empty" in {

      val testFullReturn = completeFullReturn.copy(submission = Some(Submission(None, submissionStatus = None)))
      val testUserAnswers = emptyUserAnswers.copy(fullReturn = Some(testFullReturn))

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
      }
    }
  }
}