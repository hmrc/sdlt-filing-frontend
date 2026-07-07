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
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.submission.SubmissionReceiptViewModel
import views.html.submission.SubmissionReceiptView

class SubmissionReceiptControllerSpec extends SpecBase {

  private val fullReturnWithRequiredData = completeFullReturn.copy(
    submission = Some(completeSubmission.copy(
      UTRN = Some("UTRN123456789012"),
      submissionRequestDate = Some("2024-10-15T10:15:00Z")
    ))
  )

  private val fullReturnWithoutUTRN = fullReturnWithRequiredData.copy(
    submission = Some(completeSubmission.copy(
      UTRN = None,
      submissionRequestDate = Some("2024-10-15T10:15:00Z")
    ))
  )

  private val fullReturnWithoutSubmissionReceipt = fullReturnWithRequiredData.copy(
    submission = Some(completeSubmission.copy(
      UTRN = Some("UTRN123456789012"),
      submissionReceipt = None,
      submissionRequestDate = Some("2024-10-15T10:15:00Z")
    ))
  )

  private val fullReturnWithoutSubmissionRequestDate = fullReturnWithRequiredData.copy(
    submission = Some(completeSubmission.copy(
      UTRN = Some("UTRN123456789012"),
      submissionRequestDate = None
    ))
  )

  private val fullReturnWithoutPurchaserAgent = fullReturnWithRequiredData.copy(
    returnAgent = None
  )

  "SubmissionReceipt Controller" - {

    "must return OK and the correct view for a GET when the full return is present in session" in {
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithRequiredData))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionReceiptController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionReceiptView]
        implicit val msgs: Messages = messages(application)
        val viewModel = SubmissionReceiptViewModel(fullReturnWithRequiredData).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, msgs).toString

        contentAsString(result) must include("UTRN123456789012")
        contentAsString(result) must include("RECEIPT-001")
        contentAsString(result) must include("John David Smith")
        contentAsString(result) must include("Johnson")
        contentAsString(result) must include("TGL123456")
        contentAsString(result) must include("10012345678")
      }
    }

    "must not show an agent reference row when the purchaser has no agent" in {
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithoutPurchaserAgent))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionReceiptController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must not include "Agent reference"
      }
    }

    "must redirect to ReturnTaskList for a GET when there is no full return in session" in {
      val userAnswers = emptyUserAnswers.copy(fullReturn = None)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionReceiptController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to ReturnTaskList for a GET when UTRN is absent" in {
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithoutUTRN))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionReceiptController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to ReturnTaskList for a GET when submission receipt is absent" in {
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithoutSubmissionReceipt))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionReceiptController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to ReturnTaskList for a GET when submission request date is absent" in {
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithoutSubmissionRequestDate))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionReceiptController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }
  }
}
