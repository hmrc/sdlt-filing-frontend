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
import models.prelimQuestions.TransactionType
import models.taxCalculation.{MissingAboutTheTransactionError, MissingFullReturnError, TaxCalculationResult}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.preliminary.TransactionTypePage
import play.api.inject.bind
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.taxCalculation.SdltCalculationService
import views.html.taxCalculation.TaxCalculationBeforeYouStartView

import scala.concurrent.Future

class TaxCalculationBeforeYouStartControllerSpec extends SpecBase with MockitoSugar {

  private val calculatedResult     = TaxCalculationResult(totalTax = 5000, resultHeading = None,                     resultHint = None, npv = None, taxCalcs = Seq.empty)
  private val selfAssessedResult   = TaxCalculationResult(totalTax = 0,    resultHeading = Some("self-assessed"),    resultHint = None, npv = None, taxCalcs = Seq.empty)

  private lazy val onPageLoadUrl = controllers.taxCalculation.routes.TaxCalculationBeforeYouStartController.onPageLoad().url

  "TaxCalculationBeforeYouStart Controller" - {

    "must render the view with false when the calculation succeeds and the transaction is not leasehold" in {

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any()))
        .thenReturn(Future.successful(Right(calculatedResult)))

      val answers = emptyUserAnswers.set(TransactionTypePage, TransactionType.ConveyanceTransfer).success.value
      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[SdltCalculationService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadUrl).withCSRFToken
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[TaxCalculationBeforeYouStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(isLeaseholdAndSelfAssessed = false)(request, messages(application)).toString
      }
    }

    "must render the view with true when the calculation is self-assessed and the transaction is leasehold" in {

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any()))
        .thenReturn(Future.successful(Right(selfAssessedResult)))

      val answers = emptyUserAnswers.set(TransactionTypePage, TransactionType.GrantOfLease).success.value
      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[SdltCalculationService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadUrl).withCSRFToken
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[TaxCalculationBeforeYouStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(isLeaseholdAndSelfAssessed = true)(request, messages(application)).toString
      }
    }

    "must render the view with false when the calculation is self-assessed but the transaction is not leasehold" in {

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any()))
        .thenReturn(Future.successful(Right(selfAssessedResult)))

      val answers = emptyUserAnswers.set(TransactionTypePage, TransactionType.ConveyanceTransfer).success.value
      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[SdltCalculationService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadUrl).withCSRFToken
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[TaxCalculationBeforeYouStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(isLeaseholdAndSelfAssessed = false)(request, messages(application)).toString
      }
    }

    "must render the view with false when the transaction is leasehold but the calculation is not self-assessed" in {

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any()))
        .thenReturn(Future.successful(Right(calculatedResult)))

      val answers = emptyUserAnswers.set(TransactionTypePage, TransactionType.GrantOfLease).success.value
      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[SdltCalculationService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadUrl).withCSRFToken
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[TaxCalculationBeforeYouStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(isLeaseholdAndSelfAssessed = false)(request, messages(application)).toString
      }
    }

    "must redirect to NoReturnReferenceController when the service reports no full return" in {

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any()))
        .thenReturn(Future.successful(Left(MissingFullReturnError)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SdltCalculationService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadUrl).withCSRFToken
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.NoReturnReferenceController.onPageLoad().url
      }
    }

    "must redirect to ReturnTaskListController when the service reports any other missing data" in {

      val mockService = mock[SdltCalculationService]
      when(mockService.calculateStampDutyLandTax(any())(any(), any()))
        .thenReturn(Future.successful(Left(MissingAboutTheTransactionError)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SdltCalculationService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadUrl).withCSRFToken
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "onSubmit must redirect to the next page determined by the navigator" in {
      val onSubmitUrl = controllers.taxCalculation.routes.TaxCalculationBeforeYouStartController.onSubmit().url
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, onSubmitUrl).withCSRFToken
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        // Navigator currently routes both branches to IndexController (placeholders until
        // downstream tax-calc controllers exist) — assert we redirect somewhere, not nowhere.
        redirectLocation(result) mustBe defined
      }
    }
  }
}
