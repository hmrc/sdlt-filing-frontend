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
import constants.FullReturnConstants.*
import controllers.routes
import forms.transaction.ConfirmTypeOfTransactionFormProvider
import models.{FullReturn, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.transaction.ConfirmTypeOfTransactionView

import scala.concurrent.Future

class ConfirmTypeOfTransactionControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ConfirmTypeOfTransactionFormProvider()
  val form = formProvider()

  lazy val confirmTypeOfTransactionRoute = controllers.transaction.routes.ConfirmTypeOfTransactionController.onPageLoad().url

  val fullReturnWithTransactionType: FullReturn =
    completeFullReturn.copy(
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("F"))))

  val userAnswersWithTransactionType: UserAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithTransactionType))

  val fullReturnWithoutTransactionType: FullReturn =
    completeFullReturn.copy(
      transaction = Some(completeTransaction.copy(
        transactionDescription = None)))

  val userAnswersWithoutTransactionType: UserAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithoutTransactionType))


  val transactionType: String = "conveyanceTransfer"

  "ConfirmTypeOfTransaction Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithTransactionType)).build()

      running(application) {
        val request = FakeRequest(GET, confirmTypeOfTransactionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmTypeOfTransactionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, transactionType)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when the value is 'Yes'" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithTransactionType))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmTypeOfTransactionRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
    "must redirect to Type Of Transaction Page when the value is 'No'" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithTransactionType))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmTypeOfTransactionRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.transaction.routes.TypeOfTransactionController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to Journey Recovery Page when transaction type is missing from full return for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutTransactionType)).build()

      running(application) {
        val request = FakeRequest(GET, confirmTypeOfTransactionRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery Page when transaction type is missing from full return for a POST" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutTransactionType)).build()

      running(application) {
        val request = FakeRequest(POST, confirmTypeOfTransactionRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithTransactionType)).build()

      running(application) {
        val request =
          FakeRequest(POST, confirmTypeOfTransactionRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ConfirmTypeOfTransactionView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, transactionType)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, confirmTypeOfTransactionRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, confirmTypeOfTransactionRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
