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

package controllers.purchaser

import base.SpecBase
import controllers.routes
import forms.purchaser.RegistrationNumberFormProvider
import models.{NormalMode, UserAnswers}
import models.purchaser.{NameOfPurchaser}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{NameOfPurchaserPage, RegistrationNumberPage}
import pages.purchaser.RegistrationNumberPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.RegistrationNumberView

import scala.concurrent.Future

class RegistrationNumberControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RegistrationNumberFormProvider()
  val form = formProvider()

   lazy val registrationNumberRoute = controllers.purchaser.routes.RegistrationNumberController.onPageLoad(NormalMode).url

  val userAnswersWithPurchaserName: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), None, "Smith")).success.value

  val userAnswersWithPurchaserNameAndVAT: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), None, "Smith")).success.value
    .set(RegistrationNumberPage, "123456789").success.value

  "RegistrationNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserName)).build()

      running(application) {
        val request = FakeRequest(GET, registrationNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "John Smith")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      // val userAnswers = UserAnswers(userAnswersId).set(RegistrationNumberPage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserNameAndVAT)).build()

      running(application) {
        val request = FakeRequest(GET, registrationNumberRoute)

        val view = application.injector.instanceOf[RegistrationNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("123456789"),NormalMode, "John Smith")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithPurchaserNameAndVAT))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, registrationNumberRoute)
            .withFormUrlEncodedBody(("registrationNumber", "123456789"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserName)).build()

      running(application) {
        val request =
          FakeRequest(POST, registrationNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RegistrationNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "John Smith")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, registrationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, registrationNumberRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
