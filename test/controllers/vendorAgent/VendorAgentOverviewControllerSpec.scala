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

package controllers.vendorAgent

import base.SpecBase
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
import services.vendorAgent.VendorAgentService

import scala.concurrent.Future
import scala.util.Success

class VendorAgentOverviewControllerSpec extends SpecBase with MockitoSugar {

  private val returnAgentId: String = completeReturnAgent.returnAgentID.getOrElse("NotFound")

  private val userAnswersWithAgent = emptyUserAnswers.copy(
    returnId = Some(completeFullReturn.returnResourceRef),
    fullReturn = Some(completeFullReturn.copy(returnAgent = Some(Seq(completeReturnAgentVendor))))
  )

  private val userAnswersWithoutAgent = emptyUserAnswers.copy(
    returnId = Some(completeFullReturn.returnResourceRef),
    fullReturn = Some(completeFullReturn.copy(returnAgent = None))
  )

  lazy val onPageLoadRoute = routes.VendorAgentOverviewController.onPageLoad().url
  lazy val onSubmitRoute = routes.VendorAgentOverviewController.onSubmit().url
  lazy val changeRoute = routes.VendorAgentOverviewController.changeVendorAgent(returnAgentId).url
  lazy val removeRoute = routes.VendorAgentOverviewController.removeVendorAgent(returnAgentId).url

  "VendorAgentOverviewController" - {

    "onPageLoad" - {

      "must return OK when no vendor agent exists" in {
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
          contentAsString(result) must include("Do you want to add an agent for the vendor?")
        }
      }

      "must return OK and render agent name when vendor agent exists" in {
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
          contentAsString(result) must not include "Do you want to add an agent for the vendor?"
        }
      }

      "must redirect to prelim before you start page if returnId is missing" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = None)))
            .build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.preliminary.routes.BeforeStartReturnController.onPageLoad().url
        }
      }

      "must redirect to journey recovery when failure retrieving fullReturn" in {
        val mockFullReturnService = mock[FullReturnService]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Some error")))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutAgent))
            .overrides(bind[FullReturnService].toInstance(mockFullReturnService))
            .build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to ReturnTaskList when vendor agent already exists" in {
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

      "must redirect to Vendor Agent Before You Start page when no vendor agent and answer yes" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutAgent)).build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
            .withFormUrlEncodedBody("value" -> "true")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.VendorAgentBeforeYouStartController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when no vendor agent and answer no" in {
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

    "changeVendorAgent" - {

      "must populate session and redirect when agent exists" in {
        val mockService = mock[VendorAgentService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockService.populateAssignedVendorAgentInSession(any(), any()))
          .thenReturn(Success(userAnswersWithAgent))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithAgent))
            .overrides(
              bind[VendorAgentService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, changeRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad().url

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

    "removeVendorAgent" - {
      "must redirect to removeVendorAgent view when agent exists" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithAgent))
            .build()

        running(application) {
          val request = FakeRequest(GET, removeRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when agent missing" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutAgent)).build()

        running(application) {
          val request = FakeRequest(GET, removeRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
