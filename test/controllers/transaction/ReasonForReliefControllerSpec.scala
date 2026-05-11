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

package controllers.transaction

import base.SpecBase
import controllers.routes
import forms.transaction.ReasonForReliefFormProvider
import models.{CheckMode, NormalMode}
import models.transaction.ReasonForRelief
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.transaction.{AddRegisteredCharityNumberPage, CharityRegisteredNumberPage, ReasonForReliefPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.transaction.ReasonForReliefView

import scala.concurrent.Future

class ReasonForReliefControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val reasonForReliefRoute          = controllers.transaction.routes.ReasonForReliefController.onPageLoad(NormalMode).url
  lazy val reasonForReliefRouteCheckMode = controllers.transaction.routes.ReasonForReliefController.onPageLoad(CheckMode).url

  val formProvider = new ReasonForReliefFormProvider()
  val form = formProvider()

  "ReasonForRelief Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, reasonForReliefRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[ReasonForReliefView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(ReasonForReliefPage, ReasonForRelief.values.head).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, reasonForReliefRoute)
        val view = application.injector.instanceOf[ReasonForReliefView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(ReasonForRelief.values.head), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to IsPurchaserRegisteredWithCIS when partExchange is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reasonForReliefRoute)
            .withFormUrlEncodedBody(("value", "partExchange"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.transaction.routes.IsPurchaserRegisteredWithCISController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to AddRegisteredCharityNumber when charitiesRelief is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reasonForReliefRoute)
            .withFormUrlEncodedBody(("value", "charitiesRelief"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.transaction.routes.AddRegisteredCharityNumberController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to the next page when any other valid value is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reasonForReliefRoute)
            .withFormUrlEncodedBody(("value", "relocationOfEmployment"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the Transaction CYA page when partExchange is submitted in check mode" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reasonForReliefRouteCheckMode)
            .withFormUrlEncodedBody(("value", "partExchange"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to the Transaction CYA page when charitiesRelief is submitted in check mode" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reasonForReliefRouteCheckMode)
            .withFormUrlEncodedBody(("value", "charitiesRelief"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to the Transaction CYA page when any other valid value is submitted in check mode" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reasonForReliefRouteCheckMode)
            .withFormUrlEncodedBody(("value", "relocationOfEmployment"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
      }
    }

    "must clear charity sub-pages and redirect to CYA when a non-charitiesRelief value is submitted in check mode" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(AddRegisteredCharityNumberPage, true).success.value
        .set(CharityRegisteredNumberPage, "CS123456").success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reasonForReliefRouteCheckMode)
            .withFormUrlEncodedBody(("value", "partExchange"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, reasonForReliefRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view = application.injector.instanceOf[ReasonForReliefView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, reasonForReliefRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, reasonForReliefRoute)
            .withFormUrlEncodedBody(("value", ReasonForRelief.values.head.toString))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}