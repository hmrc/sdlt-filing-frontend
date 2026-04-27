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

package controllers.preliminary

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.completeFullReturn
import controllers.routes
import models.address.{Address, Country}
import models.prelimQuestions.{CompanyOrIndividualRequest, TransactionType}
import models.{CheckMode, CreateReturnResult, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.preliminary.{PurchaserAddressPage, PurchaserIsIndividualPage, PurchaserSurnameOrCompanyNamePage, TransactionTypePage}
import play.api.inject.bind
import play.api.libs.json.{JsNull, JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.checkAnswers.CheckAnswersService
import viewmodels.govuk.SummaryListFluency

import java.time.Instant
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockBackendConnector = mock[StampDutyLandTaxConnector]
  private val mockCheckAnswersService = mock[CheckAnswersService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  "Check Your Answers Controller" - {

    "onPageLoad" - {

      val address = Address(
        line1 = "Test Street",
        line2 = Some("Test line 2"),
        line3 = Some("Test line 3"),
        line4 = Some("Test line 4"),
        line5 = Some("Test line 5"),
        postcode = Some("LE9 7RF"),
        country = Some(Country(
          code = Some("GB"),
          name = Some("United Kingdom")
        )),
        addressValidated = true
      )

      "must redirect to BeforeStartReturnController when the UserAnswers data is empty and there is no returnId" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.preliminary.routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.preliminary.routes.BeforeStartReturnController.onPageLoad().url
        }
      }

      "must return OK and the correct view when UserAnswers contains valid data" in {

        val userAnswers = emptyUserAnswers
          .set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option1).success.value
          .set(PurchaserSurnameOrCompanyNamePage, "Test Company").success.value
          .set(PurchaserAddressPage, address).success.value
          .set(TransactionTypePage, TransactionType.ConveyanceTransfer).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.preliminary.routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustBe OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, controllers.preliminary.routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must return redirect call when page is missing" in {

        val userAnswers = emptyUserAnswers
          .set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option1).success.value
          .set(PurchaserSurnameOrCompanyNamePage, "Test name").success.value
          .set(PurchaserAddressPage, address).success.value

        val redirectCall = controllers.preliminary.routes.TransactionTypeController.onPageLoad(CheckMode)

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockCheckAnswersService.redirectOrRender(any()))
          .thenReturn(Left(redirectCall))

        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CheckAnswersService].toInstance(mockCheckAnswersService)
          )
          .build()

        running(application) {

          val request = FakeRequest(
            GET,
            controllers.preliminary.routes.CheckYourAnswersController.onPageLoad().url
          )

          val result = route(application, request).value

          redirectLocation(result).value must include("transaction-type/change")
        }
      }

      "must redirect to journey recovery for a GET if session data is not found" in {

        val userAnswers = emptyUserAnswers

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.preliminary.routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to ReturnTaskListController when all required data is present and valid" in {

        val userAnswers = UserAnswers(
          id = "12345",
          storn = "TESTSTORN",
          returnId = None,
          data = Json.obj(
            "purchaserIsIndividual" -> "YES",
            "purchaserSurnameOrCompanyName" -> "Test Company",
            "purchaserAddress" -> Json.obj(
              "houseNumber" -> JsNull,
              "line1" -> "Test Street",
              "line2" -> JsNull,
              "line3" -> JsNull,
              "line4" -> JsNull,
              "line5" -> JsNull ,
              "postcode" ->  JsNull,
              "country" -> JsNull,
              "addressValidated" -> false
            ),
            "transactionType" -> "O"
          ),
          lastUpdated = Instant.now
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockBackendConnector.createReturn(any())(any(),any())).thenReturn(Future.successful(CreateReturnResult("12345")))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.preliminary.routes.CheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect back to CheckYourAnswersController when required data is missing or invalid" in {

        val incompleteData = Json.obj(
          "purchaserIsIndividual" -> "Individual"
        )

        val userAnswers = UserAnswers(
          id = userAnswersId,
          storn = "TESTSTORN",
          data = incompleteData
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockBackendConnector.createReturn(any())(any(), any())).thenReturn(Future.successful(CreateReturnResult("12345")))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.preliminary.routes.CheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.preliminary.routes.CheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to CheckYourAnswersController when returnId is an empty string" in {

        val userAnswers = UserAnswers(
          id = "12345",
          returnId = None,
          storn = "TESTSTORN",
          data = Json.obj(
            "purchaserIsIndividual" -> "YES",
            "purchaserSurnameOrCompanyName" -> "Test Company",
            "purchaserAddress" -> Json.obj(
              "houseNumber" -> JsNull,
              "line1" -> "Test Street",
              "line2" -> JsNull,
              "line3" -> JsNull,
              "line4" -> JsNull,
              "line5" -> JsNull,
              "postcode" -> JsNull,
              "country" -> JsNull,
              "addressValidated" -> false
            ),
            "transactionType" -> "O"
          ),
          lastUpdated = Instant.now
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockBackendConnector.createReturn(any())(any(), any())).thenReturn(Future.successful(CreateReturnResult("")))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.preliminary.routes.CheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.preliminary.routes.CheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecoveryController when session repository returns no data" in {

        val userAnswers = None

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(userAnswers))

        val application = applicationBuilder(userAnswers = userAnswers)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.preliminary.routes.CheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecoveryController when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, controllers.preliminary.routes.CheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to journey recovery for a POST if session data is not found" in {

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(completeFullReturn))

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.preliminary.routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}