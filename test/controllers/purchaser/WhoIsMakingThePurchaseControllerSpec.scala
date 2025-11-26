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
import forms.purchaser.WhoIsMakingThePurchaseFormProvider
import models.purchaser.WhoIsMakingThePurchase
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.WhoIsMakingThePurchasePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.WhoIsMakingThePurchaseView

import scala.concurrent.Future

class WhoIsMakingThePurchaseControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new WhoIsMakingThePurchaseFormProvider()
  val form = formProvider()

  lazy val whoIsMakingThePurchaseRoute: String =
    controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url

  private val testStorn = "TESTSTORN"

  val userAnswersWithIndividual: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value

  val userAnswersWithCompany: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value

  "WhoIsMakingThePurchaseController.onPageLoad" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, whoIsMakingThePurchaseRoute)
        val result = route(application, request).value

        val view = application.injector.instanceOf[WhoIsMakingThePurchaseView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered with Individual" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividual))
        .build()

      running(application) {
        val request = FakeRequest(GET, whoIsMakingThePurchaseRoute)
        val result = route(application, request).value

        val view = application.injector.instanceOf[WhoIsMakingThePurchaseView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(WhoIsMakingThePurchase.Individual), NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered with Company" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithCompany))
        .build()

      running(application) {
        val request = FakeRequest(GET, whoIsMakingThePurchaseRoute)
        val result = route(application, request).value

        val view = application.injector.instanceOf[WhoIsMakingThePurchaseView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(WhoIsMakingThePurchase.Company), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, whoIsMakingThePurchaseRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "WhoIsMakingThePurchaseController.onSubmit" - {

    "must redirect to the next page when Individual is selected" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, whoIsMakingThePurchaseRoute)
          .withFormUrlEncodedBody(("value", WhoIsMakingThePurchase.Individual.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when Company is selected" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, whoIsMakingThePurchaseRoute)
          .withFormUrlEncodedBody(("value", WhoIsMakingThePurchase.Company.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(POST, whoIsMakingThePurchaseRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view = application.injector.instanceOf[WhoIsMakingThePurchaseView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when no data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(POST, whoIsMakingThePurchaseRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[WhoIsMakingThePurchaseView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, whoIsMakingThePurchaseRoute)
          .withFormUrlEncodedBody(("value", WhoIsMakingThePurchase.Individual.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}