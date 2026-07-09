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
import constants.FullReturnConstants.{completeFullReturn, completeSubmission}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class CompletedSdltReturnControllerSpec extends SpecBase {

  "CompletedSdltReturn Controller" - {

    "must return OK and the correct view for a GET for a submitted return" in {

      val submission = completeSubmission.copy(submissionStatus = Some("submitted"))
      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers.copy(fullReturn = Some(completeFullReturn.copy(submission = Some(submission)))))
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.CompletedSdltReturnController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Your submitted SDLT return")
        contentAsString(result) must include("The Stamp Duty Land Tax return: UTRN123456789012 for John David Smith was submitted to HMRC at 10:30 on 15 October 2024.")
        contentAsString(result) must include("RECEIPT-001")
      }
    }

    "must return OK and the correct view for a GET for a completed return" in {
      val submission = completeSubmission.copy(submissionStatus = None, submittedDate = None, submissionReceipt = Some("RECEIPT-001"), UTRN = None)
      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers.copy(fullReturn = Some(completeFullReturn.copy(submission = Some(submission)))))
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.CompletedSdltReturnController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Your completed SDLT return")
        contentAsString(result) must include("RECEIPT-001")
        contentAsString(result) must not include "The Stamp Duty Land Tax return:"
        contentAsString(result) must not include "UTRN:"
      }
    }

    "must return OK and the correct view for a GET for a completed return missing submission receipt reference" in {
      val submission = completeSubmission.copy(submissionStatus = None, submittedDate = None, submissionReceipt = None, UTRN = None)
      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers.copy(fullReturn = Some(completeFullReturn.copy(submission = Some(submission)))))
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.CompletedSdltReturnController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Your completed SDLT return")
        contentAsString(result) must not include "Submission receipt reference number:"
        contentAsString(result) must not include "The Stamp Duty Land Tax return:"
        contentAsString(result) must not include "UTRN:"
      }
    }

    "must redirect to return task list when fullReturn is missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.CompletedSdltReturnController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to return task list when submission is missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(fullReturn = Some(completeFullReturn.copy(submission = None))))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.CompletedSdltReturnController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to return task list when submission status can't be parsed" in {
      val submission = completeSubmission.copy(submissionStatus = Some("unexpected"))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(fullReturn = Some(completeFullReturn.copy(submission = Some(submission)))))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.CompletedSdltReturnController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }
  }
}
