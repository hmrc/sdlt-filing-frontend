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
import controllers.routes
import models.{CheckMode, Land, UserAnswers}
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
import scala.util.Try

class LandAuthorityCodeSingleEntityControllerSpec extends SpecBase with MockitoSugar {

  private val landA = Land(landID = Some("LND001"), address1 = Some("Castle Street"))
  private val landB = Land(landID = Some("LND002"), address1 = Some("Cathays Terrace"))

  private def userAnswersWithLands(lands: Seq[Land]): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(land = Some(lands))))

  lazy val singleEntityRoute: String =
    controllers.land.routes.LandAuthorityCodeSingleEntityController.onPageLoad("LND001").url

  private val authorityCodeFailure = CrossFlowFailure(
    ruleId     = "F17-6996-6997",
    affects    = ReturnSection.Land,
    messageKey = "crossflow.land.authority.welsh6996_6997.beforeEffectiveDate",
    inlineErrorKey = "crossflow.land.authority.welsh6996_6997.beforeEffectiveDate",
    targets    = Seq(CrossFlowTarget(Pages.LandAuthorityCode, "value"))
  )

  private val postcodeFailure = CrossFlowFailure(
    ruleId     = "F18-scottishPostcode",
    affects    = ReturnSection.Land,
    messageKey = "crossflow.land.postcode.scottish.afterCR223",
    inlineErrorKey = "crossflow.land.postcode.scottish.afterCR223",
    targets    = Seq(CrossFlowTarget(Pages.LandPostcode, "value"))
  )

  private def crossFlowWithFailures(failures: Seq[(Land, Seq[CrossFlowFailure])]) =
    new CrossFlowValidationService(Set.empty, Set.empty) {
      override def landFailuresGrouped(ua: UserAnswers): Seq[(Land, Seq[CrossFlowFailure])] =
        failures
    }

  private val crossFlowNoFailures = crossFlowWithFailures(Nil)

  private val populateLandPassthrough = new PopulateLandService() {
    override def populateLandInSession(land: Land, ua: UserAnswers): Try[UserAnswers] =
      Try(ua)
  }

  "LandAuthorityCodeSingleEntity Controller" - {

    "onPageLoad" - {

      "must return OK and render the view when the land has an authority-code failure" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = userAnswersWithLands(Seq(landA))
        val stub = crossFlowWithFailures(Seq((landA, Seq(authorityCodeFailure))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CrossFlowValidationService].toInstance(stub),
            bind[PopulateLandService].toInstance(populateLandPassthrough),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must render the per-rule heading from messages" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = userAnswersWithLands(Seq(landA))
        val stub = crossFlowWithFailures(Seq((landA, Seq(authorityCodeFailure))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CrossFlowValidationService].toInstance(stub),
            bind[PopulateLandService].toInstance(populateLandPassthrough),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)

          val result = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.land.F17-6996-6997.heading"))
        }
      }

      "must render the failure message from messages" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = userAnswersWithLands(Seq(landA))
        val stub = crossFlowWithFailures(Seq((landA, Seq(authorityCodeFailure))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CrossFlowValidationService].toInstance(stub),
            bind[PopulateLandService].toInstance(populateLandPassthrough),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)

          val result = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.land.authority.welsh6996_6997.beforeEffectiveDate"))
        }
      }

      "must point the Continue button at the local authority code page in CheckMode for code-targeted failures" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = userAnswersWithLands(Seq(landA))
        val stub = crossFlowWithFailures(Seq((landA, Seq(authorityCodeFailure))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CrossFlowValidationService].toInstance(stub),
            bind[PopulateLandService].toInstance(populateLandPassthrough),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)

          val result = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(controllers.land.routes.LocalAuthorityCodeController.onPageLoad(CheckMode).url)
        }
      }

      "must point the Continue button at the confirm-address page in CheckMode for postcode-targeted failures" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = userAnswersWithLands(Seq(landA))
        val stub = crossFlowWithFailures(Seq((landA, Seq(postcodeFailure))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CrossFlowValidationService].toInstance(stub),
            bind[PopulateLandService].toInstance(populateLandPassthrough),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)

          val result = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(controllers.land.routes.ConfirmLandOrPropertyAddressController.onPageLoad(CheckMode).url)
        }
      }

      "must redirect to ReturnTaskList when the landId is not in fullReturn" in {

        val userAnswers = userAnswersWithLands(Seq(landB)) // requested LND001, present is LND002
        val stub = crossFlowWithFailures(Seq((landB, Seq(authorityCodeFailure))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CrossFlowValidationService].toInstance(stub),
            bind[PopulateLandService].toInstance(populateLandPassthrough)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad(None).url
        }
      }

      "must redirect to ReturnTaskList when no failure exists for the land" in {

        val userAnswers = userAnswersWithLands(Seq(landA))
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CrossFlowValidationService].toInstance(crossFlowNoFailures),
            bind[PopulateLandService].toInstance(populateLandPassthrough)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad(None).url
        }
      }

      "must populate the land in session before rendering" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = userAnswersWithLands(Seq(landA))
        val stub = crossFlowWithFailures(Seq((landA, Seq(authorityCodeFailure))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CrossFlowValidationService].toInstance(stub),
            bind[PopulateLandService].toInstance(populateLandPassthrough),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          org.mockito.Mockito.verify(mockSessionRepository).set(any[UserAnswers])
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}