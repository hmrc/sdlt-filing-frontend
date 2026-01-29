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

package controllers.purchaserAgent

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.*
import models.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.FullReturnService
import services.purchaserAgent.PurchaserAgentService

import scala.concurrent.Future
import scala.util.Success

class PurchaserAgentOverviewControllerSpec extends SpecBase with MockitoSugar {

  private val returnAgentId: String = completeReturnAgent.returnAgentID.getOrElse("NotFound")

  private val userAnswersWithAgent = emptyUserAnswers.copy(
    returnId = Some(completeFullReturn.returnResourceRef),
    fullReturn = Some(completeFullReturn)
  )

  private val userAnswersWithoutAgent = emptyUserAnswers.copy(
    returnId = Some(completeFullReturn.returnResourceRef),
    fullReturn = Some(completeFullReturn.copy(returnAgent = None))
  )

  lazy val onPageLoadRoute = routes.PurchaserAgentOverviewController.onPageLoad().url
  lazy val onSubmitRoute = routes.PurchaserAgentOverviewController.onSubmit().url
  lazy val changeRoute = routes.PurchaserAgentOverviewController.changePurchaserAgent(returnAgentId).url
  lazy val removeRoute = routes.PurchaserAgentOverviewController.removePurchaserAgent(returnAgentId).url

  "PurchaserAgentOverviewController" - {

    "onPageLoad" - {

      "must return OK when no purchaser agent exists" in {
        val mockFullReturnService = mock[FullReturnService]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(userAnswersWithoutAgent.fullReturn.get))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutAgent))
            .overrides(bind[FullReturnService].toInstance(mockFullReturnService))
            .build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK and render agent name when purchaser agent exists" in {
        val mockFullReturnService = mock[FullReturnService]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(userAnswersWithAgent.fullReturn.get))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithAgent))
            .overrides(bind[FullReturnService].toInstance(mockFullReturnService))
            .build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Smith &amp; Partners LLP")
        }
      }
    }

    "onSubmit" - {

      "must redirect to ReturnTaskList when purchaser agent already exists" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithAgent)).build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to Purchaser Agent Before You Start page when no purchaser agent and answer yes" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutAgent)).build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
            .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.PurchaserAgentBeforeYouStartController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to ReturnTaskList when no purchaser agent and answer no" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutAgent)).build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
            .withFormUrlEncodedBody("value" -> "false")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must return BadRequest when invalid form submitted" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutAgent)).build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
            .withFormUrlEncodedBody("value" -> "")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }
    }

    "changePurchaserAgent" - {

      "must populate session and redirect when agent exists" in {
        val mockService = mock[PurchaserAgentService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockService.populateAssignedPurchaserAgentInSession(any(), any()))
          .thenReturn(Success(userAnswersWithAgent))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithAgent))
            .overrides(
              bind[PurchaserAgentService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, changeRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url

          verify(mockSessionRepository).set(any())
        }
      }

      "must redirect to JourneyRecovery when agent missing" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutAgent)).build()

        running(application) {
          val request = FakeRequest(GET, changeRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "removePurchaserAgent" - {
      "must delete agent and redirect with flash when agent exists" in {
        val mockConnector = mock[StampDutyLandTaxConnector]

        when(mockConnector.deleteReturnAgent(any())(any(), any()))
          .thenReturn(Future.successful(DeleteReturnAgentReturn(deleted = true)))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithAgent))
            .overrides(
              bind[StampDutyLandTaxConnector].toInstance(mockConnector)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, removeRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }
    }
  }
}
