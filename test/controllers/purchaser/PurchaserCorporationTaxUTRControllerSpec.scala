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
import forms.purchaser.PurchaserCorporationTaxUTRFormProvider
import models.purchaser.{NameOfPurchaser, PurchaserConfirmIdentity, WhoIsMakingThePurchase}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{NameOfPurchaserPage, PurchaserConfirmIdentityPage, PurchaserUTRPage, WhoIsMakingThePurchasePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaser.PurchaserCorporationTaxUTRView

import scala.concurrent.Future

class PurchaserCorporationTaxUTRControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PurchaserCorporationTaxUTRFormProvider()
  val form = formProvider()

  lazy val purchaserCorporationTaxUTRRoute: String = controllers.purchaser.routes.PurchaserCorporationTaxUTRController.onPageLoad(NormalMode).url

  val userAnswersWithoutPurchaserType: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(None, None, "Company Co")).success.value

  val userAnswersWithPurchaserTypeCompanyButNoName: UserAnswers = emptyUserAnswers
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value

  val userAnswersWithPurchaserCompanyWithUTR: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(None, None, "Company Co")).success.value
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
    .set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.CorporationTaxUTR).success.value

  val userAnswersWithPurchaserCompanyWithoutUTR: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(None, None, "Company Co")).success.value
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value

  val userAnswersWithPurchaserCompanyWithOtherId: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(None, None, "Company Co")).success.value
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
    .set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.VatRegistrationNumber).success.value


  val userAnswersWithPurchaserIndividual: UserAnswers = emptyUserAnswers
    .set(NameOfPurchaserPage, NameOfPurchaser(Some("Joe"), None, "Ioannides")).success.value
    .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value


  "PurchaserCorporationTaxUTR Controller" - {

    ".onPageLoad" - {
      "must return OK and the correct view" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserCompanyWithUTR)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserCorporationTaxUTRRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PurchaserCorporationTaxUTRView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, "Company Co", NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly when the question has previously been answered" in {

        val userAnswers = userAnswersWithPurchaserCompanyWithUTR.set(PurchaserUTRPage, "answer").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserCorporationTaxUTRRoute)

          val view = application.injector.instanceOf[PurchaserCorporationTaxUTRView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill("answer"), "Company Co", NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, purchaserCorporationTaxUTRRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to NameOfPurchaser page when purchaser name is not present" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserTypeCompanyButNoName)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserCorporationTaxUTRRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to GenericError page when the purchaser type selected is Individual" in { //TODO: DTR-1788: redirect to CYA
        val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserIndividual)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserCorporationTaxUTRRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.GenericErrorController.onPageLoad().url
        }
      }

      "must redirect to WhoIsMakingThePurchase page when the purchaser type is not present" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithoutPurchaserType)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserCorporationTaxUTRRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to PurchaserConfirmIdentity Page when the PurchaserConfirmIdentity is not CorporationTaxUTR" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserCompanyWithOtherId)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserCorporationTaxUTRRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to PurchaserConfirmIdentity Page when the PurchaserConfirmIdentity is not present" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserCompanyWithoutUTR)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserCorporationTaxUTRRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(NormalMode).url
        }
      }
    }

    ".onSubmit" - {
      "must redirect to NameOfPurchaser page when purchaser name is not present" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, purchaserCorporationTaxUTRRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithPurchaserCompanyWithUTR))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, purchaserCorporationTaxUTRRoute)
              .withFormUrlEncodedBody(("value", "1111111111"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserCompanyWithUTR)).build()

        running(application) {
          val request =
            FakeRequest(POST, purchaserCorporationTaxUTRRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PurchaserCorporationTaxUTRView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, "Company Co", NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, purchaserCorporationTaxUTRRoute)
              .withFormUrlEncodedBody(("value", "123456789123"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
    }
}
