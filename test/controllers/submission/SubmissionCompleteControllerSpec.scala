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
import constants.FullReturnConstants.{completeFullReturn, completeSubmission, completeTaxCalculation}
import models.{FullReturn, GetReturnByRefRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.FullReturnService
import views.html.submission.SubmissionCompleteView

import java.time.LocalDateTime
import scala.concurrent.Future


class SubmissionCompleteControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockFullReturnService = mock[FullReturnService]
  val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    reset(mockFullReturnService)
  }

  private val testReturnId = "123456"

  private val fullReturnWithRequiredData = completeFullReturn.copy(
    taxCalculation = Some(completeTaxCalculation.copy(
      taxDue = Some("15000.00"),
      taxDuePremium = None,
      taxDueNPV = None)),
    submission = Some(completeSubmission.copy(
      UTRN = Some("UTRN123456789012"),
      submissionReceipt = Some("RECEIPT-001"),
      email = Some("john.smith@email.com"),
      submissionRequestDate = Some("2024-10-15T10:15:00Z"),
      submissionStatus = Some("SUBMITTED")
    )))

  private val fullReturnWithoutReceipt = fullReturnWithRequiredData.copy(
    submission = Some(completeSubmission.copy(
      UTRN = Some("UTRN123456789012"),
      submissionReceipt = None,
      email = Some("john.smith@email.com"),
      submissionStatus = Some("SUBMITTED_NO_RECEIPT"),
      submissionRequestDate = Some("2024-10-15T10:15:00Z")
    ))
  )

  private val fullReturnWithoutEmail = fullReturnWithRequiredData.copy(
    submission = Some(completeSubmission.copy(
      UTRN = Some("UTRN123456789012"),
      submissionReceipt = Some("RECEIPT-001"),
      email = None,
      submissionStatus = Some("SUBMITTED"),
      submissionRequestDate = Some("2024-10-15T10:15:00Z")
    )))

  private val fullReturnWithoutUTRN = fullReturnWithRequiredData.copy(
    submission = Some(completeSubmission.copy(
      UTRN = None,
      email = Some("john.smith@email.com"),
      submissionRequestDate = Some("2024-10-15T10:15:00Z")
    )))

  private val fullReturnWithoutSubmissionRequestDate = fullReturnWithRequiredData.copy(
    submission = Some(completeSubmission.copy(
      UTRN = Some("UTRN123456789012"),
      submissionReceipt = Some("RECEIPT-001"),
      email = Some("john.smith@email.com"),
      submissionStatus = Some("SUBMITTED"),
      submissionRequestDate = None
    )))

  private val fullReturnWithoutAnyTaxDue = fullReturnWithRequiredData.copy(
    taxCalculation = Some(completeTaxCalculation.copy(
      taxDue = None,
      taxDuePremium = None,
      taxDueNPV = None
    )))

  private val fullReturnWithOnlyTaxDuePremiumAndTaxDueNPV = fullReturnWithRequiredData.copy(
    taxCalculation = Some(completeTaxCalculation.copy(
      taxDue = None,
      taxDuePremium = Some("15000"),
      taxDueNPV = Some("15000")
    )))

  private def fullReturnWithRequestDate(raw: Option[String]): FullReturn =
    fullReturnWithRequiredData.copy(
      submission = Some(completeSubmission.copy(
        UTRN = Some("UTRN123456789012"),
        submissionReceipt = Some("RECEIPT-001"),
        email = Some("john.smith@email.com"),
        submissionStatus = Some("SUBMITTED"),
        submissionRequestDate = raw
      )))

  private def applicationWith(fullReturn: FullReturn) = {
    val userAnswers = emptyUserAnswers.copy(returnId = Some(testReturnId), fullReturn = Some(fullReturn))

    when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
      .thenReturn(Future.successful(fullReturn))

    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[FullReturnService].toInstance(mockFullReturnService),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()
  }

  "SubmissionComplete Controller" - {

    "must return OK and the correct view for a GET when all required data is present" in {
      val application = applicationWith(fullReturnWithRequiredData)

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          utrn = "UTRN123456789012",
          submissionReceiptRef = Some("RECEIPT-001"),
          isSubmittedNoReceipt = false,
          deadline = "29 October 2024",
          amount = "£15,000",
          maybeEmail = Some("john.smith@email.com")
        )(request, messages(application)).toString

        contentAsString(result) must include("UTRN123456789012")
        contentAsString(result) must include("RECEIPT-001")
        contentAsString(result) must include("29 October 2024")
        contentAsString(result) must include("£15,000")
        contentAsString(result) must include("HMRC have sent a confirmation email to john.smith@email.com.")
      }
    }

    "must return OK and the correct view for a GET when submission has no receipt" in {
      val application = applicationWith(fullReturnWithoutReceipt)

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          utrn = "UTRN123456789012",
          submissionReceiptRef = None,
          isSubmittedNoReceipt = true,
          deadline = "29 October 2024",
          amount = "£15,000",
          maybeEmail = Some("john.smith@email.com")
        )(request, messages(application)).toString

        contentAsString(result) must include("UTRN123456789012")
        contentAsString(result) must not include "Your submission receipt reference number"
        contentAsString(result) must include("There has been a problem generating the submission receipt")
        contentAsString(result) must include("29 October 2024")
        contentAsString(result) must include("£15,000")
        contentAsString(result) must include("HMRC have sent a confirmation email to john.smith@email.com.")
      }
    }

    "must return OK and the correct view for a GET when email is absent" in {
      val application = applicationWith(fullReturnWithoutEmail)

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          utrn = "UTRN123456789012",
          submissionReceiptRef = Some("RECEIPT-001"),
          isSubmittedNoReceipt = false,
          deadline = "29 October 2024",
          amount = "£15,000",
          maybeEmail = None
        )(request, messages(application)).toString

        contentAsString(result) must include("UTRN123456789012")
        contentAsString(result) must include("RECEIPT-001")
        contentAsString(result) must include("29 October 2024")
        contentAsString(result) must include("£15,000")
        contentAsString(result) must not include ("HMRC have sent a confirmation email to")
      }
    }

    "must return OK and the correct view for a GET when only tax due is present" in {
      val application = applicationWith(fullReturnWithRequiredData)

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          utrn = "UTRN123456789012",
          submissionReceiptRef = Some("RECEIPT-001"),
          isSubmittedNoReceipt = false,
          deadline = "29 October 2024",
          amount = "£15,000",
          maybeEmail = Some("john.smith@email.com")
        )(request, messages(application)).toString

        contentAsString(result) must include("£15,000")
      }
    }

    "must return OK and the correct view for a GET when only tax due premium and tax due NPV are present" in {
      val application = applicationWith(fullReturnWithOnlyTaxDuePremiumAndTaxDueNPV)

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          utrn = "UTRN123456789012",
          submissionReceiptRef = Some("RECEIPT-001"),
          isSubmittedNoReceipt = false,
          deadline = "29 October 2024",
          amount = "£30,000",
          maybeEmail = Some("john.smith@email.com")
        )(request, messages(application)).toString

        contentAsString(result) must include("£30,000")
      }
    }

    "must return OK and calculate the deadline when submissionRequestDate is in the backend timestamp format" in {
      val application = applicationWith(fullReturnWithRequestDate(Some("2024-10-15 10:15:00")))

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("29 October 2024")
      }
    }

    "must return OK and calculate the deadline when submissionRequestDate has fractional seconds" in {
      val application = applicationWith(fullReturnWithRequestDate(Some("2024-10-15 10:15:00.010")))

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("29 October 2024")
      }
    }

    "must return OK with no deadline when submissionRequestDate cannot be parsed" in {
      val application = applicationWith(fullReturnWithRequestDate(Some("not-a-date")))

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          utrn = "UTRN123456789012",
          submissionReceiptRef = Some("RECEIPT-001"),
          isSubmittedNoReceipt = false,
          deadline = "",
          amount = "£15,000",
          maybeEmail = Some("john.smith@email.com")
        )(request, messages(application)).toString

        contentAsString(result) must include("UTRN123456789012")
      }
    }

    "must return OK with no deadline when Submission Request Date is absent" in {
      val application = applicationWith(fullReturnWithoutSubmissionRequestDate)

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          utrn = "UTRN123456789012",
          submissionReceiptRef = Some("RECEIPT-001"),
          isSubmittedNoReceipt = false,
          deadline = "",
          amount = "£15,000",
          maybeEmail = Some("john.smith@email.com")
        )(request, messages(application)).toString
      }
    }

    "must redirect to ReturnTaskList for a GET when UTRN is absent" in {
      val application = applicationWith(fullReturnWithoutUTRN)

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to ReturnTaskList for a GET when all tax due amounts are absent" in {
      val application = applicationWith(fullReturnWithoutAnyTaxDue)

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to BeforeStartReturn when no returnId exists" in {
      val userAnswers = emptyUserAnswers.copy(returnId = None, fullReturn = Some(fullReturnWithRequiredData))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.preliminary.routes.BeforeStartReturnController.onPageLoad().url
      }
    }
  }

  "SubmissionCompleteController.parseSubmissionRequestDate" - {

    "must parse the backend timestamp format without fractional seconds" in {
      SubmissionCompleteController.parseSubmissionRequestDate("2026-08-27 17:10:20") mustBe
        Some(LocalDateTime.of(2026, 8, 27, 17, 10, 20))
    }

    "must parse the backend timestamp format with fractional seconds" in {
      SubmissionCompleteController.parseSubmissionRequestDate("2026-09-11 12:32:36.010") mustBe
        Some(LocalDateTime.of(2026, 9, 11, 12, 32, 36, 10000000))
    }

    "must parse an ISO local date time" in {
      SubmissionCompleteController.parseSubmissionRequestDate("2026-08-27T17:10:20") mustBe
        Some(LocalDateTime.of(2026, 8, 27, 17, 10, 20))
    }

    "must parse an ISO offset date time" in {
      SubmissionCompleteController.parseSubmissionRequestDate("2024-10-15T10:15:00Z") mustBe
        Some(LocalDateTime.of(2024, 10, 15, 10, 15, 0))
    }

    "must trim surrounding whitespace before parsing" in {
      SubmissionCompleteController.parseSubmissionRequestDate("  2026-08-27 17:10:20  ") mustBe
        Some(LocalDateTime.of(2026, 8, 27, 17, 10, 20))
    }

    "must return None when the value cannot be parsed" in {
      SubmissionCompleteController.parseSubmissionRequestDate("27/08/2026") mustBe None
    }
  }
}