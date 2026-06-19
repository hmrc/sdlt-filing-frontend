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
import constants.FullReturnConstants.emptyFullReturn
import models.{CheckMode, FullReturn, Land, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.land.LandOverviewRemovePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.crossflow.*
import services.crossflow.fields.CrossFlowValidationService
import services.land.{LandService, PopulateLandService}
import viewmodels.land.LandPropertyTypeRow

import scala.concurrent.Future
import scala.util.Success

class LandPropertyTypeMultiEntityControllerSpec extends SpecBase with MockitoSugar {

  private val landA: Land = Land(
    landID               = Some("LND001"),
    returnID             = Some("RET123456789"),
    propertyType         = Some("01"),
    address1             = Some("Baker Street"),
    address2             = Some("Marylebone"),
    postcode             = Some("NW1 6XE"),
    localAuthorityNumber = Some("5900")
  )

  private val landB: Land = Land(
    landID               = Some("LND002"),
    returnID             = Some("RET123456789"),
    propertyType         = Some("03"),
    address1             = Some("Cardiff Castle"),
    address2             = Some("Cardiff"),
    postcode             = Some("CF10 3RB"),
    localAuthorityNumber = Some("6996")
  )

  private val landC: Land = Land(
    landID               = Some("LND003"),
    returnID             = Some("RET123456789"),
    propertyType         = Some("02"),
    address1             = Some("Park Lane"),
    address2             = Some("Mayfair"),
    postcode             = Some("W1K 1LB"),
    localAuthorityNumber = Some("5901")
  )

  private def fullReturnWith(lands: Land*): FullReturn =
    emptyFullReturn.copy(land = Some(lands))

  private def userAnswersWith(lands: Land*): UserAnswers =
    UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturnWith(lands*)))

  lazy val multiEntityRoute: String =
    controllers.land.routes.LandPropertyTypeMultiEntityController.onPageLoad().url

  private def updateLandRoute(landId: String): String =
    controllers.land.routes.LandPropertyTypeMultiEntityController.updateLand(landId).url

  private def removeLandRoute(landId: String): String =
    controllers.land.routes.LandPropertyTypeMultiEntityController.removeLand(landId).url

  /** Cf-6 — aggregate-only multi-land property type mismatch.
   *  Targets land property type page. Uses `crossflow.land.Cf-6.heading`. */
  private val cf6Failure = CrossFlowFailure(
    ruleId         = "Cf-6",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-6.body",
    inlineErrorKey = "crossflow.land.Cf-6.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-6.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandPropertyType, "value")),
    headingKey     = "crossflow.land.Cf-6.heading"
  )

  /** Cf-9a — F17 Welsh authority code rule. Used here to demonstrate non-Cf-6 failures
   *  redirect away from the Cf-6 multi-entity flow. */
  private val cf9aFailure = CrossFlowFailure(
    ruleId         = "Cf-9a",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-9.welsh6996_6997.body",
    inlineErrorKey = "crossflow.land.Cf-9.welsh6996_6997.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-9.welsh6996_6997.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandAuthorityCode, "value")),
    headingKey     = "crossflow.land.heading"
  )

  private def crossFlowWith(
                             cf6:    Seq[(Land, Seq[CrossFlowFailure])] = Nil,
                             nonCf6: Seq[(Land, Seq[CrossFlowFailure])] = Nil
                           ) =
    new CrossFlowValidationService(Set.empty, Set.empty) {
      override def landFailuresOnly(ruleIds: Set[String], ua: UserAnswers): Seq[(Land, Seq[CrossFlowFailure])] =
        if (ruleIds == Set("Cf-6")) cf6 else Nil

      override def landFailuresExcluding(ruleIds: Set[String], ua: UserAnswers): Seq[(Land, Seq[CrossFlowFailure])] =
        if (ruleIds == Set("Cf-6")) nonCf6 else Nil
    }

  private def stubLandService(rows: Seq[LandPropertyTypeRow] = Nil): LandService = {
    val mockService = mock[LandService]
    when(mockService.generateLandPropertyTypeRows(any(), any())).thenReturn(rows)
    mockService
  }

  private def passThroughPopulate: PopulateLandService = new PopulateLandService() {
    override def populateLandInSession(land: Land, ua: UserAnswers) = Success(ua)
  }

  private def appWith(
                       userAnswers:  UserAnswers,
                       crossFlow:    CrossFlowValidationService,
                       landService:  LandService                = stubLandService(),
                       populate:     PopulateLandService        = passThroughPopulate,
                       session:      SessionRepository          = mock[SessionRepository]
                     ) = {
    when(session.set(any())) thenReturn Future.successful(true)

    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[CrossFlowValidationService].toInstance(crossFlow),
        bind[LandService].toInstance(landService),
        bind[PopulateLandService].toInstance(populate),
        bind[SessionRepository].toInstance(session)
      )
      .build()
  }

  "LandPropertyTypeMultiEntity Controller" - {

    "onPageLoad" - {

      "must return OK and render the view when only Cf-6 failures exist" in {
        val crossFlow   = crossFlowWith(cf6 = Seq((landA, Seq(cf6Failure)), (landB, Seq(cf6Failure))))
        val application = appWith(userAnswersWith(landA, landB), crossFlow)

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must render the Cf-6 heading from messages" in {
        val crossFlow   = crossFlowWith(cf6 = Seq((landA, Seq(cf6Failure)), (landB, Seq(cf6Failure))))
        val application = appWith(userAnswersWith(landA, landB), crossFlow)

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.land.Cf-6.heading"))
        }
      }

      "must call LandService.generateLandPropertyTypeRows with all lands and the failing lands" in {
        val mockLandService = mock[LandService]
        when(mockLandService.generateLandPropertyTypeRows(any(), any())).thenReturn(Nil)

        val crossFlow   = crossFlowWith(cf6 = Seq((landA, Seq(cf6Failure)), (landB, Seq(cf6Failure))))
        val application = appWith(userAnswersWith(landA, landB, landC), crossFlow, landService = mockLandService)

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual OK

          val allLandsCaptor      = org.mockito.ArgumentCaptor.forClass(classOf[Seq[Land]])
          val failingLandsCaptor  = org.mockito.ArgumentCaptor.forClass(classOf[Seq[Land]])
          verify(mockLandService, times(1)).generateLandPropertyTypeRows(allLandsCaptor.capture(), failingLandsCaptor.capture())

          allLandsCaptor.getValue.flatMap(_.landID)     must contain allOf ("LND001", "LND002", "LND003")
          failingLandsCaptor.getValue.flatMap(_.landID) must contain allOf ("LND001", "LND002")
          failingLandsCaptor.getValue.flatMap(_.landID) must not contain "LND003"
        }
      }

      "must redirect to LandAuthorityCodeMultiEntity when non-Cf-6 failures also exist" in {
        val crossFlow = crossFlowWith(
          nonCf6 = Seq((landA, Seq(cf9aFailure))),
          cf6    = Seq((landB, Seq(cf6Failure)))
        )
        val application = appWith(userAnswersWith(landA, landB), crossFlow)

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when no failures exist at all" in {
        val crossFlow   = crossFlowWith()
        val application = appWith(userAnswersWith(landA), crossFlow)

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad(None).url
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "updateLand" - {

      "must populate the land in session and redirect to LandTypeOfProperty in CheckMode" in {
        val crossFlow   = crossFlowWith(cf6 = Seq((landA, Seq(cf6Failure)), (landB, Seq(cf6Failure))))
        val application = appWith(userAnswersWith(landA, landB), crossFlow)

        running(application) {
          val request = FakeRequest(GET, updateLandRoute("LND001"))
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.land.routes.LandTypeOfPropertyController.onPageLoad(CheckMode).url
        }
      }

      "must populate the correct land matching the landId" in {
        val mockPopulate = mock[PopulateLandService]
        when(mockPopulate.populateLandInSession(any(), any())).thenReturn(Success(userAnswersWith(landA, landB)))

        val crossFlow   = crossFlowWith(cf6 = Seq((landA, Seq(cf6Failure))))
        val application = appWith(userAnswersWith(landA, landB), crossFlow, populate = mockPopulate)

        running(application) {
          val request = FakeRequest(GET, updateLandRoute("LND002"))
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER

          val landCaptor = org.mockito.ArgumentCaptor.forClass(classOf[Land])
          verify(mockPopulate, times(1)).populateLandInSession(landCaptor.capture(), any())
          landCaptor.getValue.landID mustBe Some("LND002")
        }
      }

      "must redirect to Journey Recovery when the landId is not in fullReturn" in {
        val crossFlow   = crossFlowWith()
        val application = appWith(userAnswersWith(landA), crossFlow)

        running(application) {
          val request = FakeRequest(GET, updateLandRoute("UNKNOWN"))
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, updateLandRoute("LND001"))
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "removeLand" - {

      "must set LandOverviewRemovePage and redirect to RemoveLandController" in {
        val crossFlow   = crossFlowWith()
        val application = appWith(userAnswersWith(landA), crossFlow)

        running(application) {
          val request = FakeRequest(GET, removeLandRoute("LND001"))
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.RemoveLandController.onPageLoad().url
        }
      }

      "must save LandOverviewRemovePage with the given landId" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val crossFlow   = crossFlowWith()
        val application = appWith(
          userAnswersWith(landA, landB),
          crossFlow,
          session = mockSessionRepository
        )

        running(application) {
          val request = FakeRequest(GET, removeLandRoute("LND002"))
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER

          val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(captor.capture())
          captor.getValue.get(LandOverviewRemovePage) mustBe Some("LND002")
        }
      }

      "must redirect to Journey Recovery for a removeLand if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removeLandRoute("LND001"))
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}