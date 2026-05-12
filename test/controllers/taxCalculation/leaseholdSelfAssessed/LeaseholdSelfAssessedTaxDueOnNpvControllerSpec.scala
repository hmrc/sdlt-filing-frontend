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
import controllers.routes
import forms.taxCalculation.TaxDueOnNpvFormProvider
import models.NormalMode
import models.taxCalculation.TaxCalculationFlow
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedNpvTaxPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedTaxDueOnNpvView

import scala.concurrent.Future

class LeaseholdSelfAssessedTaxDueOnNpvControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new TaxDueOnNpvFormProvider()
  val form: Form[String] = formProvider()

  val npv = "95000.00"

  private val fullReturnWithLeaseData =
    emptyFullReturn.copy(lease = Some(completeLease))
  
  private lazy val taxDueOnNpvRoute =
    controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedTaxDueOnNpvController.onPageLoad(NormalMode).url

  "LeaseholdSelfAssessedTaxDueOnNpv Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers
        .copy(fullReturn = Some(fullReturnWithLeaseData))
        .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, taxDueOnNpvRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LeaseholdSelfAssessedTaxDueOnNpvView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, npv, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithLeaseData))
        .set(LeaseholdSelfAssessedNpvTaxPage, "1898").success.value
        .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, taxDueOnNpvRoute)

        val view = application.injector.instanceOf[LeaseholdSelfAssessedTaxDueOnNpvView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("1898"), npv, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to next page when valid data is submitted in normal mode" in {

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
          FakeRequest(POST, taxDueOnNpvRoute)
            .withFormUrlEncodedBody(("value", "1897"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the return task list for a GET when the user is not in the leasehold self-assessed flow" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, taxDueOnNpvRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to the return task list for a POST when the user is not in the leasehold self-assessed flow" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, taxDueOnNpvRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithLeaseData))
        .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, taxDueOnNpvRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[LeaseholdSelfAssessedTaxDueOnNpvView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, npv, NormalMode)(request, messages(application)).toString
      }
    }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, taxDueOnNpvRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, taxDueOnNpvRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no npv is found" in {

      val userAnswers = emptyUserAnswers
        .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, taxDueOnNpvRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no npv is found" in {

      val userAnswers = emptyUserAnswers
        .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, taxDueOnNpvRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
