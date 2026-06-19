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

package controllers.lease

import base.SpecBase
import constants.FullReturnConstants.{completeLease, emptyFullReturn}
import models.{CheckMode, Lease, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.crossflow.*
import services.crossflow.fields.CrossFlowValidationService
import services.lease.PopulateLeaseService

import scala.concurrent.Future
import scala.util.{Failure, Success}

class LeaseSingleEntityControllerSpec extends SpecBase with MockitoSugar {

  private val testLease: Lease = completeLease

  private val testUserAnswers: UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(lease = Some(testLease))))

  lazy val singleEntityRoute: String =
    controllers.lease.routes.LeaseSingleEntityController.onPageLoad().url

  /** Cf-5a — F30, lease type must be Residential when main land type is 01/04.
   *  Targets lease type page. */
  private val cf5aFailure = CrossFlowFailure(
    ruleId         = "Cf-5a",
    affects        = ReturnSection.Lease,
    messageKey     = "crossflow.lease.Cf-5a.body",
    inlineErrorKey = "crossflow.lease.Cf-5a.inline",
    body           = CrossFlowBody.Single("crossflow.lease.Cf-5a.body"),
    targets        = Seq(CrossFlowTarget(Pages.LeaseType, "value")),
    headingKey     = "crossflow.lease.heading"
  )

  /** Cf-5b — F30, lease type must be Mixed when main land type is 02. */
  private val cf5bFailure = cf5aFailure.copy(
    ruleId         = "Cf-5b",
    messageKey     = "crossflow.lease.Cf-5b.body",
    inlineErrorKey = "crossflow.lease.Cf-5b.inline",
    body           = CrossFlowBody.Single("crossflow.lease.Cf-5b.body")
  )

  /** Cf-5c — F30, lease type must be Non-residential when main land type is 03. */
  private val cf5cFailure = cf5aFailure.copy(
    ruleId         = "Cf-5c",
    messageKey     = "crossflow.lease.Cf-5c.body",
    inlineErrorKey = "crossflow.lease.Cf-5c.inline",
    body           = CrossFlowBody.Single("crossflow.lease.Cf-5c.body")
  )

  /** Some other future lease rule used to exercise the default CTA fallback. */
  private val unknownLeaseFailure = cf5aFailure.copy(
    ruleId         = "Cf-99",
    messageKey     = "crossflow.lease.Cf-99.body",
    inlineErrorKey = "crossflow.lease.Cf-99.inline",
    body           = CrossFlowBody.Single("crossflow.lease.Cf-99.body")
  )

  private def crossFlowWith(failures: Seq[CrossFlowFailure]) =
    new CrossFlowValidationService(Set.empty, Set.empty) {
      override def failuresAffecting(section: ReturnSection, ua: UserAnswers): Seq[CrossFlowFailure] =
        if (section == ReturnSection.Lease) failures else Nil
    }

  private val crossFlowSilent = crossFlowWith(Nil)

  private def passThroughPopulate: PopulateLeaseService = new PopulateLeaseService() {
    override def populateLeaseInSession(lease: Lease, ua: UserAnswers) = Success(ua)
  }

  private def failingPopulate: PopulateLeaseService = new PopulateLeaseService() {
    override def populateLeaseInSession(lease: Lease, ua: UserAnswers) =
      Failure(new RuntimeException("Exception"))
  }

  private def appWith(
                       userAnswers: UserAnswers,
                       crossFlow:   CrossFlowValidationService,
                       populate:    PopulateLeaseService = passThroughPopulate,
                       session:     SessionRepository    = mock[SessionRepository]
                     ) = {
    when(session.set(any())) thenReturn Future.successful(true)

    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[CrossFlowValidationService].toInstance(crossFlow),
        bind[PopulateLeaseService].toInstance(populate),
        bind[SessionRepository].toInstance(session)
      )
      .build()
  }

  "LeaseSingleEntity Controller" - {

    "onPageLoad" - {

      "must return OK and render the view when the lease has a Cf-5a failure" in {
        val application = appWith(testUserAnswers, crossFlowWith(Seq(cf5aFailure)))

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must render the lease heading from messages" in {
        val application = appWith(testUserAnswers, crossFlowWith(Seq(cf5aFailure)))

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.lease.heading"))
        }
      }

      "must render the failure body message" in {
        val application = appWith(testUserAnswers, crossFlowWith(Seq(cf5aFailure)))

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.lease.Cf-5a.body"))
        }
      }

      "must use the Cf-5a CTA key when the failure is Cf-5a" in {
        val application = appWith(testUserAnswers, crossFlowWith(Seq(cf5aFailure)))

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.lease.Cf-5a.cta"))
        }
      }

      "must use the Cf-5b CTA key when the failure is Cf-5b" in {
        val application = appWith(testUserAnswers, crossFlowWith(Seq(cf5bFailure)))

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.lease.Cf-5b.cta"))
        }
      }

      "must use the Cf-5c CTA key when the failure is Cf-5c" in {
        val application = appWith(testUserAnswers, crossFlowWith(Seq(cf5cFailure)))

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.lease.Cf-5c.cta"))
        }
      }

      "must use the default change-lease-type CTA key for any other failure rule id" in {
        val application = appWith(testUserAnswers, crossFlowWith(Seq(unknownLeaseFailure)))

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.lease.cta.changeLeaseType"))
        }
      }

      "must point the Continue button at the type-of-lease page in CheckMode" in {
        val application = appWith(testUserAnswers, crossFlowWith(Seq(cf5aFailure)))

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(controllers.lease.routes.TypeOfLeaseController.onPageLoad(CheckMode).url)
        }
      }

      "must use the first failure when the cross-flow service returns multiple" in {
        // headOption: only the first failure should drive the rendered CTA.
        val application = appWith(testUserAnswers, crossFlowWith(Seq(cf5bFailure, cf5aFailure)))

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK
          content must include(messages(application)("crossflow.lease.Cf-5b.cta"))
        }
      }

      "must redirect to LeaseCheckYourAnswers when there are no lease failures" in {
        val application = appWith(testUserAnswers, crossFlowSilent)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to LeaseBeforeYouStart when the fullReturn has no lease" in {
        val userAnswersNoLease = emptyUserAnswers.copy(
          fullReturn = Some(emptyFullReturn.copy(lease = None))
        )

        val application = appWith(userAnswersNoLease, crossFlowSilent)

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url
        }
      }

      "must redirect to LeaseBeforeYouStart when populateLeaseInSession fails" in {
        val application = appWith(
          testUserAnswers,
          crossFlowWith(Seq(cf5aFailure)),
          populate = failingPopulate
        )

        running(application) {
          val request = FakeRequest(GET, singleEntityRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url
        }
      }

      "must populate the lease in session before rendering" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = appWith(
          testUserAnswers,
          crossFlowWith(Seq(cf5aFailure)),
          session = mockSessionRepository
        )

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
    }
  }
}