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
import forms.purchaser.PurchaserNationalInsuranceFormProvider
import models.purchaser.{DoesPurchaserHaveNI, NameOfPurchaser}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{DoesPurchaserHaveNIPage, NameOfPurchaserPage, PurchaserNationalInsurancePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.PurchaserNationalInsuranceView

import scala.concurrent.Future

class PurchaserNationalInsuranceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PurchaserNationalInsuranceFormProvider()
  val form: Form[String] = formProvider()

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
        ),
        "doesPurchaserHaveNI" -> "yes"
      )
    )
  )

  val testUserAnswersNoNino = UserAnswers(
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
      )
    )
  )

  val testUserAnswersWithNino = UserAnswers(
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
        "doesPurchaserHaveNI" -> "yes",
        "nationalInsuranceNumber" -> "AA123465A"
      )
    )
  )

  val testUserAnswersNoName = UserAnswers(
    id = "test-session-id",
    storn = "test-storn-123",
    returnId = Some("test-return-id"),
    fullReturn = None,
    data = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "doesPurchaserHaveNI" -> "yes"
      )
    )
  )

  lazy val purchaserNationalInsuranceRoute: String = controllers.purchaser.routes.PurchaserNationalInsuranceController.onPageLoad(NormalMode).url

  "PurchaserNationalInsurance Controller" - {

    "must return OK and the correct view for a GET when purchaser name exists and do you know NiNo is yes" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserNationalInsuranceRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PurchaserNationalInsuranceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = testUserAnswersIndividual.set(PurchaserNationalInsurancePage, "AA123456A").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserNationalInsuranceRoute)

        val view = application.injector.instanceOf[PurchaserNationalInsuranceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("AA123456A"), NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(testUserAnswersWithNino))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserNationalInsuranceRoute)
            .withFormUrlEncodedBody(("nationalInsuranceNumber", "AA123456A"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to DoesPurchaserHaveNi when DoesPurchaserHaveNi is missing" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswersNoNino)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserNationalInsuranceRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(NormalMode).url)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserNationalInsuranceRoute)
            .withFormUrlEncodedBody(("nationalInsuranceNumber", "GB123456X"))

        val boundForm = form.bind(Map("nationalInsuranceNumber" -> "GB123456X"))

        val view = application.injector.instanceOf[PurchaserNationalInsuranceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, purchaserNationalInsuranceRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserNationalInsuranceRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to DoesPurchaserHaveNIPage if DoesPurchaserHaveNI data does not exist" in {

      val userAnswers = testUserAnswersIndividual.set(DoesPurchaserHaveNIPage, DoesPurchaserHaveNI.No).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, purchaserNationalInsuranceRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(NormalMode).url
    }

    "must redirect to NameOfPurchaserPage if purchaser name data is missing for a GET" in {
      val application = applicationBuilder(userAnswers = Some(testUserAnswersNoName)).build()

      val request = FakeRequest(GET, purchaserNationalInsuranceRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url

    }

    "must redirect to NameOfPurchaser page when purchaser name is missing for POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, purchaserNationalInsuranceRoute)
          .withFormUrlEncodedBody("nationalInsuranceNumber" -> "AA123456A")
        
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
      }
    }

    "must pass purchaser name to view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserNationalInsuranceRoute)

        val view = application.injector.instanceOf[PurchaserNationalInsuranceView]

        val purchaserName = testUserAnswersIndividual.get(NameOfPurchaserPage).map(_.fullName).getOrElse("")

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, purchaserName)(request, messages(application)).toString
      }
    }

    "must pass the agentName to view for a POST when BAD REQUEST returned" in {
      val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual)).build()

      running(application) {
        val request = FakeRequest(POST, purchaserNationalInsuranceRoute)
          .withFormUrlEncodedBody(
            ("nationalInsuranceNumber", "QQ123456Q")
          )

        val boundForm = form.bind(Map(
          "nationalInsuranceNumber" -> "QQ123456Q"
        ))

        val view = application.injector.instanceOf[PurchaserNationalInsuranceView]

        val purchaserName = testUserAnswersIndividual.get(NameOfPurchaserPage).map(_.fullName).getOrElse("")

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, purchaserName)(request, messages(application)).toString
      }
    }
  }
}
