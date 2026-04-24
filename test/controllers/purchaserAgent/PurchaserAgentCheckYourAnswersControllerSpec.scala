/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.purchaserAgent

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.completeFullReturn
import models.{CheckMode, CreateReturnAgentRequest, CreateReturnAgentReturn, NormalMode, ReturnVersionUpdateReturn, UpdateReturnAgentRequest, UpdateReturnAgentReturn}
import models.address.{Address, Country}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{atLeastOnce, reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import repositories.SessionRepository
import pages.purchaserAgent.*
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.checkAnswers.CheckAnswersService
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class PurchaserAgentCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockBackendConnector = mock[StampDutyLandTaxConnector]
  private val mockCheckAnswersService = mock[CheckAnswersService]

  private val userAnswersWithCompleteAnswersAndReturnAgentId = emptyUserAnswers
    .copy(
      fullReturn = Some(completeFullReturn),
      data = Json.obj(
        "purchaserAgentCurrent" -> Json.obj(
          "returnAgentId" -> "RA-001"
        )
      )
    )
    .set(PurchaserAgentNamePage, "Agent name").success.value
    .set(PurchaserAgentAddressPage, Address(
      line1 = "123 Test Street",
      line2 = Some("Test Area"),
      line3 = Some("Test Town"),
      line4 = Some("Test County"),
      line5 = None,
      postcode = Some("AA1 1AA"),
      country = Some(Country(Some("GB"), Some("United Kingdom")))
    )).success.value
    .set(AddContactDetailsForPurchaserAgentPage, false).success.value
    .set(AddPurchaserAgentReferenceNumberPage, false).success.value
    .set(PurchaserAgentAuthorisedPage, true).success.value

  private val userAnswersWithCompleteAnswers = emptyUserAnswers
    .copy(
      fullReturn = Some(completeFullReturn),
    )
    .set(PurchaserAgentNamePage, "Agent name").success.value
    .set(PurchaserAgentAddressPage, Address(
      line1 = "123 Test Street",
      line2 = Some("Test Area"),
      line3 = Some("Test Town"),
      line4 = Some("Test County"),
      line5 = None,
      postcode = Some("AA1 1AA"),
      country = Some(Country(Some("GB"), Some("United Kingdom")))
    )).success.value
    .set(AddContactDetailsForPurchaserAgentPage, false).success.value
    .set(AddPurchaserAgentReferenceNumberPage, false).success.value
    .set(PurchaserAgentAuthorisedPage, true).success.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  "PurchaserAgentCheckYourAnswers Controller" - {

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

      "must return OK and the correct view for a GET when required data is present" in {

        val userAnswers = emptyUserAnswers
          .set(PurchaserAgentNamePage, "Agent Name").success.value
          .set(PurchaserAgentAddressPage, address).success.value
          .set(AddContactDetailsForPurchaserAgentPage, false).success.value
          .set(AddPurchaserAgentReferenceNumberPage, false).success.value
          .set(PurchaserAgentAuthorisedPage, true).success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must redirect to purchaser agent before you start when data is missing for a GET" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentBeforeYouStartController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to journey recovery for a GET if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must return redirect call when page is missing" in {

        val userAnswers = emptyUserAnswers
          .set(PurchaserAgentNamePage, "Agent Name").success.value
          .set(PurchaserAgentAddressPage, address).success.value
          .set(AddContactDetailsForPurchaserAgentPage, false).success.value
          .set(AddPurchaserAgentReferenceNumberPage, false).success.value


        val redirectCall = controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(CheckMode)

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
            controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url
          )

          val result = route(application, request).value

          redirectLocation(result).value must include("agent-authorised-for-correspondence/change")
        }
      }
    }

    "onSubmit" - {

      "must update return agent if data is valid and returnAgentId is set" in {
        val userAnswers = userAnswersWithCompleteAnswersAndReturnAgentId.set(PurchaserAgentOverviewPage, "AGENT123").success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(1))))
        when(mockBackendConnector.updateReturnAgent(any[UpdateReturnAgentRequest])(any(), any()))
          .thenReturn(Future.successful(UpdateReturnAgentReturn(true)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad().url

          verify(mockBackendConnector, times(1)).updateReturnAgent(any[UpdateReturnAgentRequest])(any(), any())
          flash(result).get("purchaserAgentUpdated") mustBe Some("Agent name")
        }
      }

      "must create return agent if data is valid and returnAgentId is not set" in {
        val userAnswers = userAnswersWithCompleteAnswers

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.createReturnAgent(any[CreateReturnAgentRequest])(any(), any()))
          .thenReturn(Future.successful(CreateReturnAgentReturn("RA-001")))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad().url
          verify(mockBackendConnector, atLeastOnce()).createReturnAgent(any[CreateReturnAgentRequest])(any(), any())
          flash(result).get("purchaserAgentCreated") mustBe Some("Agent name")
        }
      }

      "must redirect to same page if mandatory data is missing" in {
        val userAnswers = userAnswersWithCompleteAnswers
          .set(AddContactDetailsForPurchaserAgentPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to same page when update call returns false" in {
        val userAnswers = userAnswersWithCompleteAnswersAndReturnAgentId
          .set(PurchaserAgentOverviewPage, "TestAgent").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.updateReturnVersion(any())(any(), any())).thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(1))))
        when(mockBackendConnector.updateReturnAgent(any[UpdateReturnAgentRequest])(any(), any())).thenReturn(Future.successful(UpdateReturnAgentReturn(false)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to same page when create call returns an empty string" in {
        val userAnswers = userAnswersWithCompleteAnswers

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.createReturnAgent(any())(any(), any())).thenReturn(Future.successful(CreateReturnAgentReturn("")))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to journey recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to journey recovery for a POST if session data is not found" in {

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(completeFullReturn))
          .set(PurchaserAgentNamePage, "Agent name").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect back to check your answers for a POST if questions are not validated" in {

        val userAnswers = emptyUserAnswers
          .copy(fullReturn = Some(completeFullReturn))
          .set(PurchaserAgentNamePage, "Agent name").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url
        }
      }
    }
  }
}
