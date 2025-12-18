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
import forms.purchaser.PurchaserAndVendorConnectedFormProvider
import models.purchaser
import models.purchaser.{NameOfPurchaser, PurchaserAndVendorConnected}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{NameOfPurchaserPage, PurchaserAndVendorConnectedPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.PurchaserAndVendorConnectedView

import java.time.Instant
import scala.concurrent.Future

class PurchaserAndVendorConnectedControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val purchaserAndVendorConnectedRoute: String = controllers.purchaser.routes.PurchaserAndVendorConnectedController.onPageLoad(NormalMode).url

  val formProvider = new PurchaserAndVendorConnectedFormProvider()
  val form: Form[PurchaserAndVendorConnected] = formProvider()

  val testUserAnswers = UserAnswers(
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
        ),
        "whoIsMakingThePurchase" -> "Individual"
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
        "whoIsMakingThePurchase" -> "Company"
      )
    )
  )
  val userAnswersWithConnectedYes: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), None, "Smith")).success.value
    .set(PurchaserAndVendorConnectedPage, PurchaserAndVendorConnected.Yes).success.value

  val userAnswersWithConnectedNo: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), None, "Smith")).success.value
    .set(PurchaserAndVendorConnectedPage, PurchaserAndVendorConnected.No).success.value

  "PurchaserAndVendorConnected Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAndVendorConnectedRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PurchaserAndVendorConnectedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = testUserAnswers.set(PurchaserAndVendorConnectedPage, PurchaserAndVendorConnected.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAndVendorConnectedRoute)

        val view = application.injector.instanceOf[PurchaserAndVendorConnectedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(PurchaserAndVendorConnected.values.head), NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAndVendorConnectedRoute)
            .withFormUrlEncodedBody(("value", PurchaserAndVendorConnected.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to purchaser name page when the name is missing for a GET" in {
      val application = applicationBuilder(userAnswers = Some(testUserAnswersNoName)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAndVendorConnectedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to purchaser name page when name is missing for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAndVendorConnectedRoute)
            .withFormUrlEncodedBody(("value", PurchaserAndVendorConnected.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAndVendorConnectedRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PurchaserAndVendorConnectedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAndVendorConnectedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAndVendorConnectedRoute)
            .withFormUrlEncodedBody(("value", PurchaserAndVendorConnected.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
