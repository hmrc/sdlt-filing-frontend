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
import forms.purchaser.PurchaserFormOfIdIndividualFormProvider
import models.purchaser.{DoesPurchaserHaveNI, NameOfPurchaser, PurchaserFormOfIdIndividual}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{DoesPurchaserHaveNIPage, NameOfPurchaserPage, PurchaserFormOfIdIndividualPage, WhoIsMakingThePurchasePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.PurchaserFormOfIdIndividualView
import models.purchaser.WhoIsMakingThePurchase

import scala.concurrent.Future

class PurchaserFormOfIdIndividualControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PurchaserFormOfIdIndividualFormProvider()
  val form: Form[PurchaserFormOfIdIndividual] = formProvider()

  lazy val purchaserFormOfIdIndividualRoute: String = controllers.purchaser.routes.PurchaserFormOfIdIndividualController.onPageLoad(NormalMode).url

  val userAnswersWithPurchaserName: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), None, "Smith")).success.value

  val userAnswersWithPurchaserNameAndNoNino: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), None, "Smith")).success.value
    .set(DoesPurchaserHaveNIPage, DoesPurchaserHaveNI.No).success.value
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value

  val userAnswersWithPurchaserNameAndNino: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), None, "Smith")).success.value
    .set(DoesPurchaserHaveNIPage, DoesPurchaserHaveNI.Yes).success.value

  val userAnswersWithPurchaserNameAndIDAndNoNino: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(Some("John"), None, "Smith")).success.value
    .set(PurchaserFormOfIdIndividualPage, PurchaserFormOfIdIndividual("123456", "Germany")).success.value
    .set(DoesPurchaserHaveNIPage, DoesPurchaserHaveNI.No).success.value
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value


  "PurchaserFormOfIdIndividual Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserNameAndNoNino)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserFormOfIdIndividualRoute)

        val view = application.injector.instanceOf[PurchaserFormOfIdIndividualView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "John Smith")(request, messages(application)).toString
      }
    }

    "must redirect to purchaser name page when the name is missing for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserFormOfIdIndividualRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url)
      }
    }

    "must redirect to does purchaser have NI page when does purchaser have NINO is missing for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserName)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserFormOfIdIndividualRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(NormalMode).url)
      }
    }
    
    "must redirect to enter PurchaserNationalInsurance page when does purchaser have NI is YES for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserNameAndNino)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserFormOfIdIndividualRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.purchaser.routes.PurchaserNationalInsuranceController.onPageLoad(NormalMode).url)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserNameAndIDAndNoNino)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserFormOfIdIndividualRoute)

        val view = application.injector.instanceOf[PurchaserFormOfIdIndividualView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(PurchaserFormOfIdIndividual("123456", "Germany")),
          NormalMode,
          "John Smith"
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithPurchaserNameAndIDAndNoNino))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserFormOfIdIndividualRoute)
            .withFormUrlEncodedBody(("idNumberOrReference", "123456"), ("countryIssued", "Germany"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to purchaser name page when name is missing for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserFormOfIdIndividualRoute)
            .withFormUrlEncodedBody(("idNumberOrReference", "123456"), ("countryIssued", "Germany"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserName)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserFormOfIdIndividualRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PurchaserFormOfIdIndividualView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "John Smith")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, purchaserFormOfIdIndividualRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserFormOfIdIndividualRoute)
            .withFormUrlEncodedBody(("idNumberOrReference", "123456"), ("countryIssued", "Germany"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
