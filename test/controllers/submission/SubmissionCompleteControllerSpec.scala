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
import constants.FullReturnConstants
import constants.FullReturnConstants.{completeFullReturn, completeSubmission, completeTaxCalculation}
import models.{GetReturnByRefRequest, TaxCalculation, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.FullReturnService
import views.html.submission.SubmissionCompleteView

import scala.concurrent.Future


class SubmissionCompleteControllerSpec extends SpecBase with MockitoSugar {

  private val testReturnId = "123456"

  private val fullReturnWithRequiredData = completeFullReturn.copy(
    taxCalculation = Some(completeTaxCalculation.copy(
      taxDue = Some("15000.00"),
      taxDuePremium = None,
      taxDueNPV = None)),
    submission = Some(completeSubmission.copy(
      UTRN = Some("UTRN123456789012"),
      email = Some("john.smith@email.com"),
      submissionRequestDate = Some("2024-10-15T10:15:00Z"),
    )))

  private val fullReturnWithoutEmail = fullReturnWithRequiredData.copy(
    submission = Some(completeSubmission.copy(
      UTRN = Some("UTRN123456789012"),
      email = None,
      submissionRequestDate = Some("2024-10-15T10:15:00Z")
    )))

  private val fullReturnWithoutUTRN = completeFullReturn.copy(
    submission = Some(completeSubmission.copy(
      UTRN = None,
      email = Some("john.smith@email.com"),
      submissionRequestDate = Some("2024-10-15T10:15:00Z")
  )))
  
  private val fullReturnWithoutSubmissionRequestDate = completeFullReturn.copy(
    submission = Some(completeSubmission.copy(
      UTRN = Some("UTRN123456789012"),
      email = Some("john.smith@email.com"),
      submissionRequestDate = None
    )))
  
  private val fullReturnWithoutAnyTaxDue = completeFullReturn.copy(
    taxCalculation = Some(completeTaxCalculation.copy(
      taxDue = None,
      taxDuePremium = None,
      taxDueNPV = None
    )))
  
  private val fullReturnWithOnlyTaxDuePremiumAndTaxDueNPV = completeFullReturn.copy(
    taxCalculation = Some(completeTaxCalculation.copy(
      taxDue = None,
      taxDuePremium = Some("15000"),
      taxDueNPV = Some("15000")
    )))

  "SubmissionComplete Controller" - {

    "must return OK and the correct view for a GET when all required data is present" in {
      val mockFullReturnService = mock[FullReturnService]
      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = emptyUserAnswers.copy(returnId = Some(testReturnId), fullReturn = Some(fullReturnWithRequiredData))

      when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
        .thenReturn(Future.successful(fullReturnWithRequiredData))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()


      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          utrn = "UTRN123456789012",
          deadline = "29 October 2024",
          amount = "£15,000",
          maybeEmail = Some("john.smith@email.com")
        )(request, messages(application)).toString

        contentAsString(result) must include("UTRN123456789012")
        contentAsString(result) must include("29 October 2024")
        contentAsString(result) must include("£15,000")
        contentAsString(result) must include("HMRC have sent a confirmation email to john.smith@email.com.")
      }
    }

    "must return OK and the correct view for a GET when email is absent" in {
      val mockFullReturnService = mock[FullReturnService]
      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = emptyUserAnswers.copy(returnId = Some(testReturnId), fullReturn = Some(fullReturnWithoutEmail))

      when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
        .thenReturn(Future.successful(fullReturnWithoutEmail))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          utrn = "UTRN123456789012",
          deadline = "29 October 2024",
          amount = "£15,000",
          maybeEmail = None
        )(request, messages(application)).toString

        contentAsString(result) must include("UTRN123456789012")
        contentAsString(result) must include("29 October 2024")
        contentAsString(result) must include("£15,000")
        contentAsString(result) must not include("HMRC have sent a confirmation email to")
      }
    }

    "must return OK and the correct view for a GET when only tax due is present" in {
      val mockFullReturnService = mock[FullReturnService]
      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = emptyUserAnswers.copy(returnId = Some(testReturnId), fullReturn = Some(fullReturnWithRequiredData))

      when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
        .thenReturn(Future.successful(fullReturnWithRequiredData))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          utrn = "UTRN123456789012",
          deadline = "29 October 2024",
          amount = "£15,000",
          maybeEmail = Some("john.smith@email.com")
        )(request, messages(application)).toString

        contentAsString(result) must include("UTRN123456789012")
        contentAsString(result) must include("29 October 2024")
        contentAsString(result) must include("£15,000")
        contentAsString(result) must include("HMRC have sent a confirmation email to john.smith@email.com.")
      }
    }

    "must return OK and the correct view for a GET when only tax due premium and tax due NPV are present" in {
      val mockFullReturnService = mock[FullReturnService]
      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = emptyUserAnswers.copy(returnId = Some(testReturnId), fullReturn = Some(fullReturnWithOnlyTaxDuePremiumAndTaxDueNPV))

      when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
        .thenReturn(Future.successful(fullReturnWithOnlyTaxDuePremiumAndTaxDueNPV))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          utrn = "UTRN123456789012",
          deadline = "29 October 2024",
          amount = "£30,000",
          maybeEmail = Some("john.smith@email.com")
        )(request, messages(application)).toString

        contentAsString(result) must include("UTRN123456789012")
        contentAsString(result) must include("29 October 2024")
        contentAsString(result) must include("£30,000")
        contentAsString(result) must include("HMRC have sent a confirmation email to john.smith@email.com.")
      }
    }
    
    "must redirect to ReturnTaskList for a GET when UTRN is absent" in {
      val mockFullReturnService = mock[FullReturnService]
      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = emptyUserAnswers.copy(returnId = Some(testReturnId), fullReturn = Some(fullReturnWithoutUTRN))

      when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
        .thenReturn(Future.successful(fullReturnWithoutUTRN))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value
        
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }
    
    "must redirect to ReturnTaskList for a GET when Submission Request Date is absent" in {
      val mockFullReturnService = mock[FullReturnService]
      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = emptyUserAnswers.copy(returnId = Some(testReturnId), fullReturn = Some(fullReturnWithoutSubmissionRequestDate))

      when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
        .thenReturn(Future.successful(fullReturnWithoutSubmissionRequestDate))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionCompleteController.onPageLoad().url)

        val result = route(application, request).value
        
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }
    
    "must redirect to ReturnTaskList for a GET when all tax due amounts are absent" in {
      val mockFullReturnService = mock[FullReturnService]
      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = emptyUserAnswers.copy(returnId = Some(testReturnId), fullReturn = Some(fullReturnWithoutAnyTaxDue))

      when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
        .thenReturn(Future.successful(fullReturnWithoutAnyTaxDue))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

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
}
