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

package controllers.land

import base.SpecBase
import controllers.routes
import models.{FullReturn, Land, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.land.PopulateLandService
import views.html.land.LandIncompleteOverviewView

import scala.concurrent.Future
import scala.util.Success

class LandIncompleteOverviewControllerSpec extends SpecBase with MockitoSugar {

  private val testStorn     = "TESTSTORN"
  private val testReturnRef = "REF001"

  private val continueUrl = controllers.routes.ReturnTaskListController.onPageLoad(None).url

  private lazy val onPageLoadRoute = controllers.land.routes.LandIncompleteOverviewController.onPageLoad().url
  private def updateLandRoute(landId: String) = controllers.land.routes.LandIncompleteOverviewController.updateLand(landId).url
  private def removeLandRoute(landId: String) = controllers.land.routes.LandIncompleteOverviewController.removeLand(landId).url

  // All six mandatory fields defined => complete
  private val completeLand = Land(
    landID = Some("LAND-1"),
    propertyType = Some("residential"),
    interestCreatedTransferred = Some("created"),
    address1 = Some("1 Test Street"),
    localAuthorityNumber = Some("LA123"),
    willSendPlanByPost = Some("yes"),
    mineralRights = Some("no")
  )

  private val incompleteLand = Land(
    landID = Some("LAND-2"),
    propertyType = Some("residential"),
    interestCreatedTransferred = Some("created"),
    address1 = Some("2 Test Road"),
    localAuthorityNumber = None,
    willSendPlanByPost = None,
    mineralRights = None
  )

  private def fullReturnWith(lands: Seq[Land]): FullReturn =
    FullReturn(
      stornId = testStorn,
      returnResourceRef = testReturnRef,
      vendor = None,
      land = Some(lands)
    )

  private def userAnswersWith(fr: FullReturn): UserAnswers =
    UserAnswers(
      id = userAnswersId,
      returnId = Some("test-return-id"),
      storn = testStorn,
      fullReturn = Some(fr)
    )

  private val testUserAnswers = userAnswersWith(fullReturnWith(Seq(completeLand, incompleteLand)))

  "LandIncompleteOverview Controller" - {

    "onPageLoad" - {

      "must return OK and render the view listing only the incomplete lands" in {

        val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[LandIncompleteOverviewView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(Seq(incompleteLand), continueUrl)(request, messages(application)).toString
        }
      }

      "must redirect to the return task list when every land is complete" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWith(fullReturnWith(Seq(completeLand))))).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad(None).url
        }
      }

      "must redirect to the return task list when there are no lands" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWith(fullReturnWith(Seq.empty)))).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad(None).url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "updateLand" - {

      "must populate the land in session and redirect to Check Your Answers when the land is found" in {

        val mockSessionRepository   = mock[SessionRepository]
        val mockPopulateLandService = mock[PopulateLandService]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockPopulateLandService.populateLandInSession(any(), any())) thenReturn Success(testUserAnswers)

        val application =
          applicationBuilder(userAnswers = Some(testUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[PopulateLandService].toInstance(mockPopulateLandService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, updateLandRoute("LAND-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when the land is not found" in {

        val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, updateLandRoute("UNKNOWN"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, updateLandRoute("LAND-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "removeLand" - {

      "must set the remove page and redirect to the Remove Land controller" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(testUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, removeLandRoute("LAND-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.RemoveLandController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removeLandRoute("LAND-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}