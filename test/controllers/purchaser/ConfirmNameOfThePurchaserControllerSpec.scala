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
import forms.purchaser.ConfirmNameOfThePurchaserFormProvider
import models.purchaser.ConfirmNameOfThePurchaser
import models.{FullReturn, NormalMode, Purchaser, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.ConfirmNameOfThePurchaserPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.purchaser.PurchaserService
import views.html.purchaser.ConfirmNameOfThePurchaserView

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ConfirmNameOfThePurchaserControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockPopulateService = mock[PurchaserService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    reset(mockPopulateService)
  }

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new ConfirmNameOfThePurchaserFormProvider()
  val form = formProvider()

  lazy val confirmNameOfThePurchaserRoute: String =
    controllers.purchaser.routes.ConfirmNameOfThePurchaserController.onPageLoad(NormalMode).url

  private val testStorn = "TESTSTORN"

  private val individualPurchaser = Purchaser(
    purchaserID = Some("PURCH001"),
    forename1 = Some("John"),
    forename2 = Some("Middle"),
    surname = Some("Doe"),
    companyName = None,
    address1 = None,
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = None
  )

  private val companyPurchaser = Purchaser(
    purchaserID = Some("PURCH002"),
    forename1 = None,
    forename2 = None,
    surname = None,
    companyName = Some("ACME Corporation"),
    address1 = None,
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = None
  )

  private val purchaserWithAddress = Purchaser(
    purchaserID = Some("PURCH003"),
    forename1 = Some("Jane"),
    forename2 = None,
    surname = Some("Smith"),
    companyName = None,
    address1 = Some("123 Main St"),
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = Some("AB1 2CD")
  )

  private def emptyFullReturn: FullReturn = FullReturn(
    returnResourceRef = "2123",
    stornId = testStorn,
    vendor = None,
    purchaser = None,
    transaction = None
  )

  private def fullReturnWithIndividualPurchaser: FullReturn =
    emptyFullReturn.copy(purchaser = Some(Seq(individualPurchaser)))

  private def fullReturnWithCompanyPurchaser: FullReturn =
    emptyFullReturn.copy(purchaser = Some(Seq(companyPurchaser)))

  private def fullReturnWithPurchaserWithAddress: FullReturn =
    emptyFullReturn.copy(purchaser = Some(Seq(purchaserWithAddress)))

  val userAnswersWithIndividualPurchaser: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))

  val userAnswersWithCompanyPurchaser: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithCompanyPurchaser))

  val userAnswersWithPurchaserWithAddress: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithPurchaserWithAddress))

  val userAnswersWithConfirmation: UserAnswers =
    UserAnswers(userAnswersId, storn = testStorn)
      .copy(fullReturn = Some(fullReturnWithIndividualPurchaser))
      .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.Yes).success.value

  "ConfirmNameOfThePurchaserController.onPageLoad" - {

    "when purchaser exists without address" - {

      "must return OK and the correct view for a GET with individual purchaser" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualPurchaser))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmNameOfThePurchaserRoute)
          val result = route(application, request).value

          val view = application.injector.instanceOf[ConfirmNameOfThePurchaserView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, "Doe", false)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET with company purchaser" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmNameOfThePurchaserRoute)
          val result = route(application, request).value

          val view = application.injector.instanceOf[ConfirmNameOfThePurchaserView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, "ACME Corporation", true)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        val application = applicationBuilder(userAnswers = Some(userAnswersWithConfirmation))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmNameOfThePurchaserRoute)
          val result = route(application, request).value

          val view = application.injector.instanceOf[ConfirmNameOfThePurchaserView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(ConfirmNameOfThePurchaser.Yes), NormalMode, "Doe", false)(request, messages(application)).toString
        }
      }
    }

    "when purchaser has an address" - {
      "must redirect to WhoIsMakingThePurchase" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithPurchaserWithAddress))
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmNameOfThePurchaserRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url
        }
      }
    }

    "when no purchaser exists" - {
      "must redirect to WhoIsMakingThePurchase" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmNameOfThePurchaserRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, confirmNameOfThePurchaserRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "ConfirmNameOfThePurchaserController.onSubmit" - {

    "when purchaser exists without address" - {

      "must redirect to PurchaserAddress when user selects Yes and service succeeds" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockPopulateService.populatePurchaserNameInSession(any(), any())) thenReturn
          Success(userAnswersWithIndividualPurchaser)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualPurchaser))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PurchaserService].toInstance(mockPopulateService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmNameOfThePurchaserRoute)
            .withFormUrlEncodedBody("value" -> "Yes")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser().url
        }
      }

      "must redirect to WhoIsMakingThePurchase when user selects No and service succeeds" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockPopulateService.populatePurchaserNameInSession(any(), any())) thenReturn
          Success(userAnswersWithIndividualPurchaser)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualPurchaser))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PurchaserService].toInstance(mockPopulateService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmNameOfThePurchaserRoute)
            .withFormUrlEncodedBody("value" -> "No")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode).url
        }
      }

      "must return InternalServerError when service fails" in {
        when(mockPopulateService.populatePurchaserNameInSession(any(), any())) thenReturn
          Failure(new Exception("Service failed"))

        val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualPurchaser))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PurchaserService].toInstance(mockPopulateService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmNameOfThePurchaserRoute)
            .withFormUrlEncodedBody("value" -> "Yes")

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

      "must return a Bad Request and errors when invalid data is submitted for individual" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualPurchaser))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmNameOfThePurchaserRoute)
            .withFormUrlEncodedBody("value" -> "invalid")

          val boundForm = form.bind(Map("value" -> "invalid"))
          val view = application.injector.instanceOf[ConfirmNameOfThePurchaserView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, "Doe", false)(request, messages(application)).toString
        }
      }

      "must return a Bad Request and errors when invalid data is submitted for company" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyPurchaser))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmNameOfThePurchaserRoute)
            .withFormUrlEncodedBody("value" -> "")

          val boundForm = form.bind(Map("value" -> ""))
          val view = application.injector.instanceOf[ConfirmNameOfThePurchaserView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, "ACME Corporation", true)(request, messages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, confirmNameOfThePurchaserRoute)
          .withFormUrlEncodedBody("value" -> "Yes")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}