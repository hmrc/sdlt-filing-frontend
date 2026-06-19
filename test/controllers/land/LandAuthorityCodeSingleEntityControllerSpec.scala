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
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.crossflow.*
import services.crossflow.fields.CrossFlowValidationService
import services.land.PopulateLandService

import scala.concurrent.Future
import scala.util.Success

class LandAuthorityCodeSingleEntityControllerSpec extends SpecBase with MockitoSugar {

  private val testLandId = "LND001"

  private val testLand: Land = Land(
    landID               = Some(testLandId),
    returnID             = Some("RET123456789"),
    propertyType         = Some("01"),
    postcode             = Some("AB1 2CD"),
    localAuthorityNumber = Some("6996"),
    address1             = Some("1 Test Street"),
    address2             = Some("Test Town")
  )

  private val testFullReturn: FullReturn =
    emptyFullReturn.copy(land = Some(Seq(testLand)))

  private val testUserAnswers: UserAnswers =
    UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(testFullReturn))

  lazy val singleEntityRoute: String =
    controllers.land.routes.LandAuthorityCodeSingleEntityController.onPageLoad(testLandId).url

  /** Cf-9a — F17, Welsh 6996/6997 codes used with effective date before Wales Act.
   *  Targets land authority code page. Uses default `crossflow.land.heading`. */
  private val cf9aFailure = CrossFlowFailure(
    ruleId         = "Cf-9a",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-9.welsh6996_6997.body",
    inlineErrorKey = "crossflow.land.Cf-9.welsh6996_6997.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-9.welsh6996_6997.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandAuthorityCode, "value")),
    headingKey     = "crossflow.land.heading"
  )

  /** Cf-16 — F18, Scottish postcodes used with effective date on/after CR223.
   *  Targets land postcode page. Uses default `crossflow.land.heading`. */
  private val cf16Failure = CrossFlowFailure(
    ruleId         = "Cf-16",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-16.body",
    inlineErrorKey = "crossflow.land.Cf-16.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-16.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandPostcode, "value")),
    headingKey     = "crossflow.land.heading"
  )

  /** Cf-3 — F24, residential additional + pre-2016 effective date.
   *  Targets land property type page. **Only rule that overrides headingKey.** */
  private val cf3Failure = CrossFlowFailure(
    ruleId         = "Cf-3",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-3.body",
    inlineErrorKey = "crossflow.land.Cf-3.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-3.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandPropertyType, "value")),
    headingKey     = "crossflow.land.Cf-3.heading"
  )

  /** Cf-6 — F30, multi-land property type mismatch with lease involvement.
   *  Targets land property type page. Aggregate-only; should be filtered out
   *  by `landFailuresExcluding(Set("Cf-6"), ...)` before reaching this controller. */
  private val cf6Failure = CrossFlowFailure(
    ruleId         = "Cf-6",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-6.body",
    inlineErrorKey = "crossflow.land.Cf-6.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-6.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandPropertyType, "value")),
    headingKey     = "crossflow.land.Cf-6.heading"
  )

  /** Mimics the real dispatcher: takes all failures and applies the rule-id filter
   *  the same way `CrossFlowValidationService.landFailuresExcluding` does. */
  private def crossFlowWith(failures: Seq[(Land, Seq[CrossFlowFailure])]) =
    new CrossFlowValidationService(Set.empty, Set.empty) {
      override def landFailuresExcluding(ruleIds: Set[String], ua: UserAnswers): Seq[(Land, Seq[CrossFlowFailure])] =
        failures
          .map { case (land, fs) => land -> fs.filterNot(f => ruleIds.contains(f.ruleId)) }
          .filter(_._2.nonEmpty)
    }

  private def appWith(
                       userAnswers: UserAnswers,
                       crossFlow:   CrossFlowValidationService,
                       populate:    PopulateLandService = passThroughPopulate,
                       session:     SessionRepository   = mock[SessionRepository]
                     ) = {
    when(session.set(any())) thenReturn Future.successful(true)

    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[CrossFlowValidationService].toInstance(crossFlow),
        bind[PopulateLandService].toInstance(populate),
        bind[SessionRepository].toInstance(session)
      )
      .build()
  }

  private def passThroughPopulate: PopulateLandService = new PopulateLandService() {
    override def populateLandInSession(land: Land, ua: UserAnswers) = Success(ua)
  }

  "LandAuthorityCodeSingleEntity Controller" - {

    "onPageLoad" - {

      "must return OK and render the view when the land has an authority-code failure" in {
        val crossFlow   = crossFlowWith(Seq((testLand, Seq(cf9aFailure))))
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must render the default land heading from messages" in {
        val crossFlow   = crossFlowWith(Seq((testLand, Seq(cf9aFailure))))
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.land.heading"))
        }
      }

      "must render the failure body message from messages" in {
        val crossFlow   = crossFlowWith(Seq((testLand, Seq(cf9aFailure))))
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.land.Cf-9.welsh6996_6997.body"))
        }
      }

      "must point the Continue button at the local authority code page in CheckMode for code-targeted failures (Cf-9a)" in {
        val crossFlow   = crossFlowWith(Seq((testLand, Seq(cf9aFailure))))
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(controllers.land.routes.LocalAuthorityCodeController.onPageLoad(CheckMode).url)
        }
      }

      "must point the Continue button at the address lookup for postcode-targeted failures (Cf-16)" in {
        val crossFlow   = crossFlowWith(Seq((testLand, Seq(cf16Failure))))
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(controllers.land.routes.LandAddressController.redirectToAddressLookupLand(Some("change")).url)
        }
      }

      "must point the Continue button at the type-of-property page in CheckMode for property-type-targeted failures (Cf-3)" in {
        val crossFlow   = crossFlowWith(Seq((testLand, Seq(cf3Failure))))
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(CheckMode).url)
        }
      }

      "must render the Cf-3 specific heading when the rule overrides it" in {
        val crossFlow   = crossFlowWith(Seq((testLand, Seq(cf3Failure))))
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.land.Cf-3.heading"))
        }
      }

      "must render the Cf-3 body message" in {
        val crossFlow   = crossFlowWith(Seq((testLand, Seq(cf3Failure))))
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.land.Cf-3.body"))
        }
      }

      "must redirect to ReturnTaskList when the landId is not in fullReturn" in {
        val crossFlow   = crossFlowWith(Nil)
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val unknownLandRoute =
            controllers.land.routes.LandAuthorityCodeSingleEntityController.onPageLoad("UNKNOWN").url
          val request = FakeRequest(GET, unknownLandRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when no failure exists for the land" in {
        val crossFlow   = crossFlowWith(Nil)
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must populate the land in session before rendering" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val crossFlow   = crossFlowWith(Seq((testLand, Seq(cf9aFailure))))
        val application = appWith(testUserAnswers, crossFlow, session = mockSessionRepository)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
          org.mockito.Mockito.verify(mockSessionRepository).set(any[UserAnswers])
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when the only failure for the land is Cf-6" in {
        val crossFlow   = crossFlowWith(Seq((testLand, Seq(cf6Failure))))
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must surface a non-Cf-6 failure even when Cf-6 is also present for the land" in {
        val crossFlow   = crossFlowWith(Seq((testLand, Seq(cf6Failure, cf9aFailure))))
        val application = appWith(testUserAnswers, crossFlow)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.land.Cf-9.welsh6996_6997.body"))
        }
      }
    }
  }
}