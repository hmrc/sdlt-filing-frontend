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
import models.{Land, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.land.LandOverviewRemovePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.crossflow.*
import services.crossflow.fields.CrossFlowValidationService

import scala.concurrent.Future

class LandAuthorityCodeMultiEntityControllerSpec extends SpecBase with MockitoSugar {

  lazy val multiEntityRoute: String =
    controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url

  private val landA = Land(landID = Some("LND001"), address1 = Some("Castle Street"))
  private val landB = Land(landID = Some("LND002"), address1 = Some("Cathays Terrace"))
  private val landC = Land(landID = Some("LND003"), address1 = Some("St Mary Street"))

  private def userAnswersWithLands(lands: Seq[Land]): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(land = Some(lands))))

  private val cf9aFailure = CrossFlowFailure(
    ruleId         = "Cf-9a",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-9.welsh6996_6997.body",
    inlineErrorKey = "crossflow.land.Cf-9.welsh6996_6997.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-9.welsh6996_6997.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandAuthorityCode, "value")),
    headingKey     = "crossflow.land.heading"
  )

  private val cf3Failure = CrossFlowFailure(
    ruleId         = "Cf-3",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-3.body",
    inlineErrorKey = "crossflow.land.Cf-3.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-3.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandPropertyType, "value")),
    headingKey     = "crossflow.land.Cf-3.heading"
  )

  private val cf6Failure = CrossFlowFailure(
    ruleId         = "Cf-6",
    affects        = ReturnSection.Land,
    messageKey     = "crossflow.land.Cf-6.body",
    inlineErrorKey = "crossflow.land.Cf-6.inline",
    body           = CrossFlowBody.Single("crossflow.land.Cf-6.body"),
    targets        = Seq(CrossFlowTarget(Pages.LandPropertyType, "value")),
    headingKey     = "crossflow.land.Cf-6.heading"
  )

  /** Mimics the real dispatcher: takes all failures and applies the rule-id exclusion
   *  filter the same way `CrossFlowValidationService.landFailuresExcluding` does. */
  private def crossFlowWithAllFailures(failures: Seq[(Land, Seq[CrossFlowFailure])]) =
    new CrossFlowValidationService(Set.empty, Set.empty) {
      override def landFailuresExcluding(ruleIds: Set[String], ua: UserAnswers): Seq[(Land, Seq[CrossFlowFailure])] =
        failures
          .map { case (land, fs) => land -> fs.filterNot(f => ruleIds.contains(f.ruleId)) }
          .filter(_._2.nonEmpty)
    }

  private val crossFlowNoFailures = crossFlowWithAllFailures(Nil)

  "LandAuthorityCodeMultiEntity Controller" - {

    "onPageLoad" - {

      "must redirect to ReturnTaskList when there are no land failures" in {

        val userAnswers = userAnswersWithLands(Seq(landA))
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(crossFlowNoFailures))
          .build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad(None).url
        }
      }

      "must redirect to the single-entity controller when exactly one land has failures" in {

        val userAnswers = userAnswersWithLands(Seq(landA))
        val stub = crossFlowWithAllFailures(Seq((landA, Seq(cf9aFailure))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(stub))
          .build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.land.routes.LandAuthorityCodeSingleEntityController.onPageLoad("LND001").url
        }
      }

      "must redirect to the single-entity controller when one land has a Cf-3 (property type) failure" in {

        val userAnswers = userAnswersWithLands(Seq(landA))
        val stub = crossFlowWithAllFailures(Seq((landA, Seq(cf3Failure))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(stub))
          .build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.land.routes.LandAuthorityCodeSingleEntityController.onPageLoad("LND001").url
        }
      }

      "must redirect to Journey Recovery when the single failing land has no landID" in {

        val landNoId    = Land(landID = None, address1 = Some("Anonymous Lane"))
        val userAnswers = userAnswersWithLands(Seq(landNoId))
        val stub        = crossFlowWithAllFailures(Seq((landNoId, Seq(cf9aFailure))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(stub))
          .build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must return OK and render the multi-entity view when multiple lands have failures" in {

        val userAnswers = userAnswersWithLands(Seq(landA, landB, landC))
        val stub = crossFlowWithAllFailures(Seq(
          (landA, Seq(cf9aFailure)),
          (landB, Seq(cf9aFailure))
        ))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(stub))
          .build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must render the multi-entity view when multiple lands have Cf-3 property-type failures" in {

        val userAnswers = userAnswersWithLands(Seq(landA, landB, landC))
        val stub = crossFlowWithAllFailures(Seq(
          (landA, Seq(cf3Failure)),
          (landB, Seq(cf3Failure))
        ))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(stub))
          .build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include("Castle Street")
          content must include("Cathays Terrace")
          content must not include "St Mary Street"
        }
      }

      "must render the multi-entity view when lands fail with mixed rule types (Cf-3 and Cf-9a)" in {

        val userAnswers = userAnswersWithLands(Seq(landA, landB, landC))
        val stub = crossFlowWithAllFailures(Seq(
          (landA, Seq(cf3Failure)),
          (landB, Seq(cf9aFailure))
        ))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(stub))
          .build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include("Castle Street")
          content must include("Cathays Terrace")
        }
      }

      "must render the multi-entity heading and paragraph when multiple lands have failures" in {

        val userAnswers = userAnswersWithLands(Seq(landA, landB, landC))
        val stub = crossFlowWithAllFailures(Seq(
          (landA, Seq(cf9aFailure)),
          (landB, Seq(cf9aFailure))
        ))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(stub))
          .build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.land.Cf-7.heading", 2))
          content must include(messages(application)("crossflow.land.multiEntity.p2"))
        }
      }

      "must render each offending land in the summary list" in {

        val userAnswers = userAnswersWithLands(Seq(landA, landB, landC))
        val stub = crossFlowWithAllFailures(Seq(
          (landA, Seq(cf9aFailure)),
          (landB, Seq(cf9aFailure))
        ))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(stub))
          .build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)

          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include("Castle Street")
          content must include("Cathays Terrace")
          content must not include "St Mary Street"
        }
      }

      "must not surface Cf-6 failures (those go to the property type multi-entity flow)" in {

        val userAnswers = userAnswersWithLands(Seq(landA, landB))
        val stub = crossFlowWithAllFailures(Seq(
          (landA, Seq(cf6Failure)),
          (landB, Seq(cf6Failure))
        ))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(stub))
          .build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad(None).url
        }
      }

      "must surface a non-Cf-6 failure even when Cf-6 is also present" in {

        val userAnswers = userAnswersWithLands(Seq(landA, landB))
        val stub = crossFlowWithAllFailures(Seq(
          (landA, Seq(cf6Failure, cf9aFailure)),
          (landB, Seq(cf6Failure))
        ))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CrossFlowValidationService].toInstance(stub))
          .build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.land.routes.LandAuthorityCodeSingleEntityController.onPageLoad("LND001").url
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, multiEntityRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "removeLand" - {

      "must set LandOverviewRemovePage and redirect to RemoveLandController" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = userAnswersWithLands(Seq(landA))
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlowNoFailures)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET,
            controllers.land.routes.LandAuthorityCodeMultiEntityController.removeLand("LND001").url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.RemoveLandController.onPageLoad().url
        }
      }

      "must save LandOverviewRemovePage with the given landId" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = userAnswersWithLands(Seq(landA, landB))
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CrossFlowValidationService].toInstance(crossFlowNoFailures)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET,
            controllers.land.routes.LandAuthorityCodeMultiEntityController.removeLand("LND002").url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])
          org.mockito.Mockito.verify(mockSessionRepository).set(captor.capture())
          captor.getValue.get(LandOverviewRemovePage) mustBe Some("LND002")
        }
      }

      "must redirect to Journey Recovery for a removeLand if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET,
            controllers.land.routes.LandAuthorityCodeMultiEntityController.removeLand("LND001").url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}