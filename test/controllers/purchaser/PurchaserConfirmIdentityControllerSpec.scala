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
import forms.purchaser.PurchaserConfirmIdentityFormProvider
import models.purchaser.*
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.*
import play.api.inject.*
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.purchaser.PurchaserService
import views.html.purchaser.PurchaserConfirmIdentityView

import scala.concurrent.Future

class PurchaserConfirmIdentityControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val purchaserConfirmIdentityRoute = controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(NormalMode).url
  lazy val purchaserConfirmIdentityCheckRoute = controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(CheckMode).url

  val formProvider = new PurchaserConfirmIdentityFormProvider()
  val form = formProvider()

  lazy val nameOfPurchaserRoute: String =
    controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url

  private val testStorn = "TESTSTORN"

  private val individualNameOfPurchaser = NameOfPurchaser(
    forename1 = Some("John"),
    forename2 = Some("Middle"),
    name = "Doe"
  )

  private val companyNameOfPurchaser = NameOfPurchaser(
    forename1 = None,
    forename2 = None,
    name = "ACME Corporation"
  )

  val userAnswersWithIndividualAndName: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value
      .set(NameOfPurchaserPage, individualNameOfPurchaser).success.value

  val userAnswersWithCompanyAndName: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value
      .set(NameOfPurchaserPage, companyNameOfPurchaser).success.value

  "PurchaserConfirmIdentity Controller" - {

    "onPageLoad" - {

      "must redirect to Name page if no purchaser name in user answers" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserConfirmIdentityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to WhoIsMakingThePurchase when purchaser name exists but who is making purchase is missing" in {

        val userAnswers = emptyUserAnswers
          .set(NameOfPurchaserPage, companyNameOfPurchaser).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserConfirmIdentityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to Generic Error Page when user is an individual (not a company)" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualAndName)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserConfirmIdentityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
        }
      }

      "must populate the view correctly when user is a company" in {

        val userAnswers = userAnswersWithCompanyAndName.set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.values.head).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserConfirmIdentityRoute)

          val view = application.injector.instanceOf[PurchaserConfirmIdentityView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(PurchaserConfirmIdentity.values.head), NormalMode, companyNameOfPurchaser.fullName)(request, messages(application)).toString
        }
      }

      "must display empty form when user is a company with no previous answer" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyAndName)).build()

        running(application) {
          val request = FakeRequest(GET, purchaserConfirmIdentityRoute)

          val view = application.injector.instanceOf[PurchaserConfirmIdentityView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, companyNameOfPurchaser.fullName)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, purchaserConfirmIdentityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "in NormalMode" - {

        "must save the answer and redirect to the next page when PartnershipUTR is submitted" in {
          val mockSessionRepository = mock[SessionRepository]
          val mockPurchaserService = mock[PurchaserService]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          when(mockPurchaserService.confirmIdentityNextPage(eqTo(PurchaserConfirmIdentity.PartnershipUTR), eqTo(NormalMode)))
            .thenReturn(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

          val userAnswers = emptyUserAnswers.set(NameOfPurchaserPage, companyNameOfPurchaser).success.value

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[SessionRepository].toInstance(mockSessionRepository),
                bind[PurchaserService].toInstance(mockPurchaserService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, purchaserConfirmIdentityRoute)
                .withFormUrlEncodedBody(("value", PurchaserConfirmIdentity.PartnershipUTR.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
            verify(mockPurchaserService).confirmIdentityNextPage(eqTo(PurchaserConfirmIdentity.PartnershipUTR), eqTo(NormalMode))
          }
        }

        "must save the answer and redirect to the next page when CorporationTaxUTR is submitted" in {
          val mockSessionRepository = mock[SessionRepository]
          val mockPurchaserService = mock[PurchaserService]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          when(mockPurchaserService.confirmIdentityNextPage(eqTo(PurchaserConfirmIdentity.CorporationTaxUTR), eqTo(NormalMode)))
            .thenReturn(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

          val userAnswers = emptyUserAnswers.set(NameOfPurchaserPage, companyNameOfPurchaser).success.value

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[SessionRepository].toInstance(mockSessionRepository),
                bind[PurchaserService].toInstance(mockPurchaserService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, purchaserConfirmIdentityRoute)
                .withFormUrlEncodedBody(("value", PurchaserConfirmIdentity.CorporationTaxUTR.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
            verify(mockPurchaserService).confirmIdentityNextPage(eqTo(PurchaserConfirmIdentity.CorporationTaxUTR), eqTo(NormalMode))
          }
        }

        "must save the answer and redirect to the next page when VatRegistrationNumber is submitted" in {
          val mockSessionRepository = mock[SessionRepository]
          val mockPurchaserService = mock[PurchaserService]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          when(mockPurchaserService.confirmIdentityNextPage(eqTo(PurchaserConfirmIdentity.VatRegistrationNumber), eqTo(NormalMode)))
            .thenReturn(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

          val userAnswers = emptyUserAnswers.set(NameOfPurchaserPage, companyNameOfPurchaser).success.value

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[SessionRepository].toInstance(mockSessionRepository),
                bind[PurchaserService].toInstance(mockPurchaserService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, purchaserConfirmIdentityRoute)
                .withFormUrlEncodedBody(("value", PurchaserConfirmIdentity.VatRegistrationNumber.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
            verify(mockPurchaserService).confirmIdentityNextPage(eqTo(PurchaserConfirmIdentity.VatRegistrationNumber), eqTo(NormalMode))
          }
        }

        "must save the answer and redirect to the next page when AnotherFormOfID is submitted" in {
          val mockSessionRepository = mock[SessionRepository]
          val mockPurchaserService = mock[PurchaserService]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          when(mockPurchaserService.confirmIdentityNextPage(eqTo(PurchaserConfirmIdentity.AnotherFormOfID), eqTo(NormalMode)))
            .thenReturn(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

          val userAnswers = emptyUserAnswers.set(NameOfPurchaserPage, companyNameOfPurchaser).success.value

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[SessionRepository].toInstance(mockSessionRepository),
                bind[PurchaserService].toInstance(mockPurchaserService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, purchaserConfirmIdentityRoute)
                .withFormUrlEncodedBody(("value", PurchaserConfirmIdentity.AnotherFormOfID.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
            verify(mockPurchaserService).confirmIdentityNextPage(eqTo(PurchaserConfirmIdentity.AnotherFormOfID), eqTo(NormalMode))
          }
        }
      }

      "in CheckMode" - {

        "must save the answer and redirect to the next page" in {
          val mockSessionRepository = mock[SessionRepository]
          val mockPurchaserService = mock[PurchaserService]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          when(mockPurchaserService.confirmIdentityNextPage(eqTo(PurchaserConfirmIdentity.PartnershipUTR), eqTo(CheckMode)))
            .thenReturn(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())

          val userAnswers = emptyUserAnswers.set(NameOfPurchaserPage, companyNameOfPurchaser).success.value

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[SessionRepository].toInstance(mockSessionRepository),
                bind[PurchaserService].toInstance(mockPurchaserService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, purchaserConfirmIdentityCheckRoute)
                .withFormUrlEncodedBody(("value", PurchaserConfirmIdentity.PartnershipUTR.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
            verify(mockPurchaserService).confirmIdentityNextPage(eqTo(PurchaserConfirmIdentity.PartnershipUTR), eqTo(CheckMode))
          }
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val userAnswers = userAnswersWithCompanyAndName.set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.values.head).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, purchaserConfirmIdentityRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[PurchaserConfirmIdentityView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, companyNameOfPurchaser.fullName)(request, messages(application)).toString
        }
      }

      "must redirect to NameOfPurchaser if no purchaser name in user answers" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, purchaserConfirmIdentityRoute)
              .withFormUrlEncodedBody(("value", PurchaserConfirmIdentity.PartnershipUTR.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, purchaserConfirmIdentityRoute)
              .withFormUrlEncodedBody(("value", PurchaserConfirmIdentity.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}