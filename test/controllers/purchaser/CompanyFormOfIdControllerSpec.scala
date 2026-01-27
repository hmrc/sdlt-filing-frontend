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
import forms.purchaser.CompanyFormOfIdFormProvider
import models.purchaser.{CompanyFormOfId, NameOfPurchaser, PurchaserConfirmIdentity, WhoIsMakingThePurchase}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{CompanyFormOfIdPage, NameOfPurchaserPage, PurchaserConfirmIdentityPage, WhoIsMakingThePurchasePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.CompanyFormOfIdView

import scala.concurrent.Future

class CompanyFormOfIdControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new CompanyFormOfIdFormProvider()
  val form: Form[CompanyFormOfId] = formProvider()

  lazy val companyFormOfIdRoute: String = controllers.purchaser.routes.CompanyFormOfIdController.onPageLoad(NormalMode).url

  val userAnswersWithPurchaserName: UserAnswers = emptyUserAnswers
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
    .set(NameOfPurchaserPage, NameOfPurchaser(None, None, "test company")).success.value
    .set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.AnotherFormOfID).success.value

  val userAnswerNoPurchaserName: UserAnswers = emptyUserAnswers
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
    .set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.AnotherFormOfID).success.value

  val userAnswersNoIdentity: UserAnswers = emptyUserAnswers
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
    .set(NameOfPurchaserPage, NameOfPurchaser(None, None, "test company")).success.value

  val userAnswersIndividual: UserAnswers = emptyUserAnswers
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
    .set(NameOfPurchaserPage, NameOfPurchaser(None, None, "test company")).success.value

  val userAnswersWithPurchaserNameAndInfo: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(None, None, "test company")).success.value
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
    .set(CompanyFormOfIdPage, CompanyFormOfId("123456", "Germany")).success.value
    .set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.AnotherFormOfID).success.value

  "CompanyFormOfId Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserName)).build()

      running(application) {
        val request = FakeRequest(GET, companyFormOfIdRoute)

        val view = application.injector.instanceOf[CompanyFormOfIdView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "test company")(request, messages(application)).toString
      }
    }

    "must redirect to purchaser name page when the name is missing for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswerNoPurchaserName)).build()

      running(application) {
        val request = FakeRequest(GET, companyFormOfIdRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url)
      }
    }

    "must redirect to purchaser confirm identity page when wrong or no selection was made for aGET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersNoIdentity)).build()

      running(application) {
        val request = FakeRequest(GET, companyFormOfIdRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(NormalMode).url)
      }
    }

    "must redirect to generic error controller if individual for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersIndividual)).build()

      running(application) {
        val request = FakeRequest(GET, companyFormOfIdRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserNameAndInfo)).build()

      running(application) {
        val request = FakeRequest(GET, companyFormOfIdRoute)

        val view = application.injector.instanceOf[CompanyFormOfIdView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(CompanyFormOfId("123456", "Germany")),
          NormalMode,
          "test company"
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithPurchaserNameAndInfo))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyFormOfIdRoute)
            .withFormUrlEncodedBody(("referenceId", "123456"), ("countryIssued", "Germany"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to purchaser name page when name is missing for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyFormOfIdRoute)
            .withFormUrlEncodedBody(("referenceId", "123456"), ("countryIssued", "Germany"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserName)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyFormOfIdRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CompanyFormOfIdView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "test company")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, companyFormOfIdRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, companyFormOfIdRoute)
            .withFormUrlEncodedBody(("referenceId", "value 1"), ("countryIssued", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
