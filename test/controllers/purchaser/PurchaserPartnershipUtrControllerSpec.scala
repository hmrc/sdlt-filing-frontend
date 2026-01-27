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
import forms.purchaser.PurchaserPartnershipUtrFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.PurchaserUTRPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.PurchaserPartnershipUtrView

import scala.concurrent.Future

class PurchaserPartnershipUtrControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PurchaserPartnershipUtrFormProvider()
  val form: Form[String] = formProvider()

  lazy val purchaserPartnershipUtrRoute: String = controllers.purchaser.routes.PurchaserPartnershipUtrController.onPageLoad(NormalMode).url

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
        "whoIsMakingThePurchase" -> "Company",
        "purchaserConfirmIdentity" -> "partnershipUniqueTaxpayerReference"
      )
    )
  )

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
        "whoIsMakingThePurchase" -> "Individual"
      )
    )
  )

  val testUserAnswersWrongPurchaserInfo = UserAnswers(
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
        "whoIsMakingThePurchase" -> "Company",
        "purchaserConfirmIdentity" -> "corporationTaxUniqueTaxpayerReference"
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
        "whoIsMakingThePurchase" -> "Company"
      )
    )
  )

  "PurchaserPartnershipUtr Controller" - {

    "must return OK and the correct view for a GET when purchaser name present, purchaser type is 'company' and purchaser information is 'partnership'" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserPartnershipUtrRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PurchaserPartnershipUtrView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = testUserAnswers.set(PurchaserUTRPage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserPartnershipUtrRoute)

        val view = application.injector.instanceOf[PurchaserPartnershipUtrView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, "John Middle Doe")(request, messages(application)).toString
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
          FakeRequest(POST, purchaserPartnershipUtrRoute)
            .withFormUrlEncodedBody(
              "partnershipUniqueTaxpayerReference" -> "1111111111"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to purchaser name page when the name is missing for a GET" in {
      val application = applicationBuilder(userAnswers = Some(testUserAnswersNoName)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserPartnershipUtrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to purchaser name page when the name is missing for a POST" in {
      val application = applicationBuilder(userAnswers = Some(testUserAnswersNoName)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserPartnershipUtrRoute)
            .withFormUrlEncodedBody("partnershipUniqueTaxpayerReference" -> "answer")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to Generic Error if purchaser type (company vs individual) does not match for a GET" in {
      val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserPartnershipUtrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to PurchaserConfirmIdentity page if purchaser information does not match partnership" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswersWrongPurchaserInfo)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserPartnershipUtrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserPartnershipUtrRoute)
            .withFormUrlEncodedBody("partnershipUniqueTaxpayerReference" -> "123")

        val boundForm = form.bind(Map("partnershipUniqueTaxpayerReference" -> "123"))

        val view = application.injector.instanceOf[PurchaserPartnershipUtrView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "John Middle Doe")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, purchaserPartnershipUtrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserPartnershipUtrRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
