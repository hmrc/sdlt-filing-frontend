/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import base.SpecBase
import forms.PurchaserSurnameOrCompanyNameFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{PurchaserIsIndividualPage, PurchaserSurnameOrCompanyNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.PurchaserSurnameOrCompanyNameView
import models.prelimQuestions.BusinessOrIndividualRequest

import scala.concurrent.Future

class PurchaserSurnameOrCompanyNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PurchaserSurnameOrCompanyNameFormProvider()
  val form = formProvider()

  lazy val purchaserSurnameOrCompanyNameRoute = routes.PurchaserSurnameOrCompanyNameController.onPageLoad(NormalMode).url

  val business = "Business"
  val individual = "Individual"

  val individualUserAnswers = UserAnswers(userAnswersId).set(PurchaserIsIndividualPage, BusinessOrIndividualRequest.Option1).success.value
  val businessUserAnswers = UserAnswers(userAnswersId).set(PurchaserIsIndividualPage, BusinessOrIndividualRequest.Option2).success.value

  "PurchaserSurnameOrCompanyName Controller" - {

    "must return OK and the correct view for a GET and individual has been answered" in {


      val application = applicationBuilder(userAnswers = Some(individualUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserSurnameOrCompanyNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PurchaserSurnameOrCompanyNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, individual)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered and individual has been answered" in {

      val userAnswers = individualUserAnswers.set(PurchaserSurnameOrCompanyNamePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserSurnameOrCompanyNameRoute)

        val view = application.injector.instanceOf[PurchaserSurnameOrCompanyNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, individual)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET and business has been answered" in {


      val application = applicationBuilder(userAnswers = Some(businessUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserSurnameOrCompanyNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PurchaserSurnameOrCompanyNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, business)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered and business has been answered" in {

      val userAnswers = businessUserAnswers.set(PurchaserSurnameOrCompanyNamePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserSurnameOrCompanyNameRoute)

        val view = application.injector.instanceOf[PurchaserSurnameOrCompanyNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, business)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

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
          FakeRequest(POST, purchaserSurnameOrCompanyNameRoute)
            .withFormUrlEncodedBody(("purchaserSurnameOrCompanyName", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted and individual has been answered" in {

      val application = applicationBuilder(userAnswers = Some(individualUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserSurnameOrCompanyNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PurchaserSurnameOrCompanyNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, individual)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, purchaserSurnameOrCompanyNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserSurnameOrCompanyNameRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
