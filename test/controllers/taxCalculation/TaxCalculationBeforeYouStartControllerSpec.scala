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

package controllers.taxCalculation

import base.SpecBase
import models.taxCalculation.CalculationOutcome.{Calculated, SelfAssessed}
import models.taxCalculation.{MissingAboutTheTransactionError, MissingFullReturnError, TaxCalculationFlow, TaxCalculationResult}
import models.{FullReturn, NormalMode, Transaction, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.http.{BadGatewayException, UpstreamErrorResponse}
import viewmodels.taxCalculation.selfAssessedViewModels.CannotCalculateViewModel
import views.html.taxCalculation.shared.CannotCalculateSdltDueView

import scala.concurrent.Future

class TaxCalculationBeforeYouStartControllerSpec extends SpecBase with MockitoSugar {

  private val calculatedResult   = TaxCalculationResult(totalTax = 5000, resultHeading = None,                  resultHint = None, npv = None, taxCalcs = Seq.empty)

  private lazy val onPageLoadUrl = controllers.taxCalculation.routes.TaxCalculationBeforeYouStartController.onPageLoad().url

  private val freeholdSectionKey: String = "site.taxCalculation.freeholdSelfAssessed.section"
  private val leaseholdSectionKey: String = "site.taxCalculation.leaseholdSelfAssessed.caption"
  private def freeholdContinueUrl: String =
    controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedSdltSelfAssessmentController.onPageLoad(NormalMode).url
  private def leaseholdContinueUrl: String =
    controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedPremiumPayableTaxController.onPageLoad(NormalMode).url

  private def answersWith(transactionDescription: String): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(FullReturn(
      stornId           = "TESTSTORN",
      returnResourceRef = "REF001",
      transaction       = Some(Transaction(transactionDescription = Some(transactionDescription)))
    )))

  "TaxCalculationBeforeYouStart Controller" - {

    "must redirect to FreeholdTaxCalculatedBYSController and add the FreeholdTaxCalculated flow to the session" in {
      val answers = answersWith("F")

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(Future.successful(Right(Calculated(calculatedResult))))

      val repo = mock[SessionRepository]
      when(repo.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[SdltCalculationService].toInstance(mockService),
          bind[SessionRepository].toInstance(repo)
        )
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadUrl)).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdTaxCalculatedBYSController.onPageLoad().url

        savedAnswers(repo).get(TaxCalculationFlowPage) mustBe Some(TaxCalculationFlow.FreeholdTaxCalculated)
      }
    }

    "must redirect to FreeholdSelfAssessedBYSController and add the FreeholdSelfAssessed flow to the session" in {
      val answers = answersWith("F")

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(Future.successful(Right(SelfAssessed)))

      val repo = mock[SessionRepository]
      when(repo.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[SdltCalculationService].toInstance(mockService),
          bind[SessionRepository].toInstance(repo)
        )
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadUrl)).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedBYSController.onPageLoad().url

        savedAnswers(repo).get(TaxCalculationFlowPage) mustBe Some(TaxCalculationFlow.FreeholdSelfAssessed)
      }
    }

    "must redirect to LeaseholdTaxCalculatedBYSController and add the LeaseholdTaxCalculated flow to the session" in {
      val answers = answersWith("L")

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(Future.successful(Right(Calculated(calculatedResult))))

      val repo = mock[SessionRepository]
      when(repo.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[SdltCalculationService].toInstance(mockService),
          bind[SessionRepository].toInstance(repo)
        )
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadUrl)).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.taxCalculation.leaseholdTaxCalculated.routes.LeaseholdTaxCalculatedBYSController.onPageLoad().url

        savedAnswers(repo).get(TaxCalculationFlowPage) mustBe Some(TaxCalculationFlow.LeaseholdTaxCalculated)
      }
    }

    "must redirect to LeaseholdSelfAssessedBYSController and add the LeaseholdSelfAssessed flow to the session" in {
      val answers = answersWith("L")

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(Future.successful(Right(SelfAssessed)))

      val repo = mock[SessionRepository]
      when(repo.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[SdltCalculationService].toInstance(mockService),
          bind[SessionRepository].toInstance(repo)
        )
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadUrl)).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedBYSController.onPageLoad().url

        savedAnswers(repo).get(TaxCalculationFlowPage) mustBe Some(TaxCalculationFlow.LeaseholdSelfAssessed)
      }
    }

    "must clear any flow previously added to the session when no transaction type is recorded" in {
      val staleAnswers = emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(Future.successful(Right(Calculated(calculatedResult))))

      val repo = mock[SessionRepository]
      when(repo.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(staleAnswers))
        .overrides(
          bind[SdltCalculationService].toInstance(mockService),
          bind[SessionRepository].toInstance(repo)
        )
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadUrl)).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url

        savedAnswers(repo).get(TaxCalculationFlowPage) mustBe None
      }
    }

    "must redirect to NoReturnReferenceController and clear any flow from the session when the service reports no full return" in {
      val staleAnswers = emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(Future.successful(Left(MissingFullReturnError)))

      val repo = mock[SessionRepository]
      when(repo.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(staleAnswers))
        .overrides(
          bind[SdltCalculationService].toInstance(mockService),
          bind[SessionRepository].toInstance(repo)
        )
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadUrl)).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.NoReturnReferenceController.onPageLoad().url

        savedAnswers(repo).get(TaxCalculationFlowPage) mustBe None
      }
    }

    "must redirect to ReturnTaskListController and clear any flow from the session when the service reports any other missing data" in {
      val staleAnswers = emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(Future.successful(Left(MissingAboutTheTransactionError)))

      val repo = mock[SessionRepository]
      when(repo.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(staleAnswers))
        .overrides(
          bind[SdltCalculationService].toInstance(mockService),
          bind[SessionRepository].toInstance(repo)
        )
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadUrl)).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url

        savedAnswers(repo).get(TaxCalculationFlowPage) mustBe None
      }
    }

    "must render the freehold cannot-calculate page and persist the FreeholdSelfAssessed flow when the calculation call fails for a freehold transaction" in {
      val answers = answersWith("F")

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(Future.failed(UpstreamErrorResponse("sdltc unavailable", 500)))

      val repo = mock[SessionRepository]
      when(repo.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[SdltCalculationService].toInstance(mockService),
          bind[SessionRepository].toInstance(repo)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadUrl)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[CannotCalculateSdltDueView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          CannotCalculateViewModel.toViewModel(Nil)(messages(application)), freeholdSectionKey, freeholdContinueUrl
        )(request, messages(application)).toString
        savedAnswers(repo).get(TaxCalculationFlowPage) mustBe Some(TaxCalculationFlow.FreeholdSelfAssessed)
      }
    }

    "must render the leasehold cannot-calculate page and persist the LeaseholdSelfAssessed flow when the calculation call fails for a leasehold transaction" in {
      val answers = answersWith("L")

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(Future.failed(UpstreamErrorResponse("sdltc unavailable", 500)))

      val repo = mock[SessionRepository]
      when(repo.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[SdltCalculationService].toInstance(mockService),
          bind[SessionRepository].toInstance(repo)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadUrl)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[CannotCalculateSdltDueView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          CannotCalculateViewModel.toViewModel(Nil)(messages(application)), leaseholdSectionKey, leaseholdContinueUrl
        )(request, messages(application)).toString
        savedAnswers(repo).get(TaxCalculationFlowPage) mustBe Some(TaxCalculationFlow.LeaseholdSelfAssessed)
      }
    }

    "must route to self-assessment when sdltc is unreachable, not only when it returns an error response" in {
      val answers = answersWith("F")

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(Future.failed(new BadGatewayException("sdltc unreachable")))

      val repo = mock[SessionRepository]
      when(repo.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[SdltCalculationService].toInstance(mockService),
          bind[SessionRepository].toInstance(repo)
        )
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, onPageLoadUrl)).value

        status(result) mustEqual OK
        savedAnswers(repo).get(TaxCalculationFlowPage) mustBe Some(TaxCalculationFlow.FreeholdSelfAssessed)
      }
    }
  }

  private def savedAnswers(repo: SessionRepository): UserAnswers = {
    val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
    verify(repo).set(captor.capture())
    captor.getValue
  }
}
