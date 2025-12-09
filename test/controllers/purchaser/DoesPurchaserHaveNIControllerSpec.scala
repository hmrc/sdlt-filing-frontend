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
import forms.purchaser.DoesPurchaserHaveNIFormProvider
import models.purchaser.DoesPurchaserHaveNI
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.DoesPurchaserHaveNIPage
import play.api.inject.bind
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.DoesPurchaserHaveNIView

import java.time.Instant
import scala.concurrent.Future

class DoesPurchaserHaveNIControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val mockSessionRepository = mock[SessionRepository]

  lazy val doesPurchaserHaveNIRoute = controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(NormalMode).url

  val formProvider = new DoesPurchaserHaveNIFormProvider()
  val form = formProvider()

  val testUserAnswersIndividual = UserAnswers(
    id = "test-session-id",
    storn = "test-storn-123",
    returnId = Some("test-return-id"),
    fullReturn = None,
    data = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "nameOfPurchaser" -> Json.obj(
          "forename1" -> "John",
          "forename2" -> "Middle",
          "name" -> "Doe"
        )
      )
    ),
    lastUpdated = Instant.now
  )

  val testUserAnswersNoName = UserAnswers(
    id = "test-session-id",
    storn = "test-storn-123",
    returnId = Some("test-return-id"),
    fullReturn = None,
    data = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "nameOfPurchaser" -> Json.obj(
          "forename1" -> JsNull,
          "forename2" -> JsNull,
          "name" -> JsNull
        )
      )
    ),
    lastUpdated = Instant.now
  )

  "DoesPurchaserHaveNI Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual)).build()

      running(application) {
        val request = FakeRequest(GET, doesPurchaserHaveNIRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DoesPurchaserHaveNIView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = testUserAnswersIndividual.set(DoesPurchaserHaveNIPage, DoesPurchaserHaveNI.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, doesPurchaserHaveNIRoute)

        val view = application.injector.instanceOf[DoesPurchaserHaveNIView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(DoesPurchaserHaveNI.values.head), NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(testUserAnswersIndividual))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, doesPurchaserHaveNIRoute)
            .withFormUrlEncodedBody(("value", DoesPurchaserHaveNI.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual)).build()

      running(application) {
        val request =
          FakeRequest(POST, doesPurchaserHaveNIRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DoesPurchaserHaveNIView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, doesPurchaserHaveNIRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, doesPurchaserHaveNIRoute)
            .withFormUrlEncodedBody(("value", DoesPurchaserHaveNI.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to PurchaserNamePage when the purchaser name data is missing for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, doesPurchaserHaveNIRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to purchaser name page when name is missing for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, doesPurchaserHaveNIRoute)
            .withFormUrlEncodedBody(("value", DoesPurchaserHaveNI.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
      }
    }

    "redirect to PurchaserFormOfIdIndividualPage when user selects no" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual)).build()

      running(application) {
        val request =
          FakeRequest(POST, doesPurchaserHaveNIRoute)
            .withFormUrlEncodedBody(("value", DoesPurchaserHaveNI.No.toString))

        val result = route(application, request).value

        redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserFormOfIdIndividualController.onPageLoad(NormalMode).url
      }
    }

  }
}
