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

package controllers.taxCalculation.leaseholdSelfAssessed

import base.SpecBase
import constants.FullReturnConstants.{completeLease, emptyFullReturn}
import forms.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedPremiumPayableTaxFormProvider
import org.scalatest.freespec.AnyFreeSpec
import models.NormalMode
import models.taxCalculation.TaxCalculationFlow
import navigation.{FakeNavigator, Navigator}
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedPremiumPayableTaxPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedPremiumPayableTaxView

import scala.concurrent.Future


class LeaseholdSelfAssessedPremiumPayableTaxControllerSpec extends AnyFreeSpec with SpecBase {

  def onwardRoute = Call("GET", "/foo")
  private val fullReturnWithLeaseData = emptyFullReturn.copy(lease = Some(completeLease))
  val form = new LeaseholdSelfAssessedPremiumPayableTaxFormProvider()()
  val premiumPayable = "50000.00"
  lazy val premiumPayableRoute = controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedPremiumPayableTaxController.onPageLoad(NormalMode).url

  "LeaseholdSelfAssessedPremiumPayableTax Controller" - {
    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .copy(fullReturn = Some(fullReturnWithLeaseData))
        .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, premiumPayableRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[LeaseholdSelfAssessedPremiumPayableTaxView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, premiumPayable, NormalMode)(request, messages(application)).toString
      }
    }
    "must populate correct view on a GET when previously answered" in {

      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithLeaseData))
        .set(LeaseholdSelfAssessedPremiumPayableTaxPage, "5000").success.value
        .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, premiumPayableRoute)

        val view = application.injector.instanceOf[LeaseholdSelfAssessedPremiumPayableTaxView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("5000"), premiumPayable, NormalMode)(request, messages(application)).toString
      }
    }
    "must redirect to next page when valid data is submitted in NormalMode" in {

      val userAnswers = emptyUserAnswers
        .copy(fullReturn = Some(fullReturnWithLeaseData))
        .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, premiumPayableRoute)
            .withFormUrlEncodedBody(("leaseholdSelfAssessedPremiumPayableTax", "5000"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
    "must redirect to the ReturnTaskListController for a GET when not leasehold self-assessed" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, premiumPayableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }
    "must redirect to the ReturnTaskListController for a POST when not leasehold self-assessed" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, premiumPayableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }
    "must redirect to JourneyRecoveryController for a GET when no premium payable is found" in {

      val userAnswers = emptyUserAnswers
        .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, premiumPayableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "must redirect to JourneyRecoveryController for a POST when no premium payable is found" in {

      val userAnswers = emptyUserAnswers
        .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, premiumPayableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "must return a BadRequest when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithLeaseData))
        .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, premiumPayableRoute)
            .withFormUrlEncodedBody(("leaseholdSelfAssessedPremiumPayableTax", ""))

        val filledForm = form.bind(Map("leaseholdSelfAssessedPremiumPayableTax" -> ""))

        val view = application.injector.instanceOf[LeaseholdSelfAssessedPremiumPayableTaxView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(filledForm, premiumPayable, NormalMode)(request, messages(application)).toString
      }
    }
  }
}

