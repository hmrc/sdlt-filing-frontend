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
import forms.land.LandOverviewFormProvider
import models.{FullReturn, GetReturnByRefRequest, Land, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.FullReturnService
import services.land.PopulateLandService
import views.html.land.LandOverviewView

import scala.concurrent.Future
import scala.util.Success

class LandOverviewControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new LandOverviewFormProvider()
  val form = formProvider()

  private def createLand(landId: String, address: Option[String], landResourceRef: Option[String]): Land = {
    Land(
      landID = Some(landId),
      returnID = Some("RET123456789"),
      propertyType = Some("01"),
      interestCreatedTransferred = Some("FG"),
      houseNumber = Some("123"),
      address1 = address,
      address2 = Some("Marylebone"),
      address3 = Some("London"),
      address4 = None,
      postcode = Some("NW1 6XE"),
      landArea = Some("250.5"),
      areaUnit = Some("SQMETRE"),
      localAuthorityNumber = Some("5900"),
      mineralRights = Some("NO"),
      NLPGUPRN = Some("10012345678"),
      willSendPlanByPost = Some("NO"),
      titleNumber = Some("TGL12456"),
      landResourceRef = landResourceRef,
      nextLandID = None,
      DARPostcode = Some("NW1 6XE")
    )
  }

  val testStorn = "LND001"
  val testReturnRef = "LND-REF-001"

  private val testLand = createLand(
    testStorn,
    address = Some("Baker Street"),
    landResourceRef = Some(testReturnRef)
  )

  private val testFullReturn = FullReturn(
    stornId = testStorn,
    returnResourceRef = testReturnRef,
    land = Some(Seq(testLand))
  )

  private val testUserAnswers = UserAnswers(
    id = userAnswersId,
    returnId = Some("test-return-id"),
    storn = "TESTSTORN",
    fullReturn = Some(testFullReturn)
  )

  lazy val landOverviewRoute = controllers.land.routes.LandOverviewController.onPageLoad(1).url
  lazy val landOverviewSubmitRoute = controllers.land.routes.LandOverviewController.onSubmit().url
  lazy val changeLandRoute = controllers.land.routes.LandOverviewController.changeLand("LND001").url
  lazy val removeLandRoute = controllers.land.routes.LandOverviewController.removeLand("LND001").url

  "LandOverviewController" - {
    ".onPageLoad" - {
      "must return OK and the correct view when no lands exist" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val emptyFullReturn = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, land = None)
        val emptyUserAnswers = testUserAnswers.copy(fullReturn = Some(emptyFullReturn))

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(emptyFullReturn))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, landOverviewRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[LandOverviewView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(None, None, None, controllers.land.routes.LandBeforeYouStartController.onPageLoad(), form, NormalMode, false)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view with lands" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(testFullReturn))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, landOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Baker Street")
        }
      }

      "must calculate errorCalc correctly when lands > 98" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val lands = (1 to 99).map(i => createLand(
          s"LND0$i",
          address = Some(s"Address $i"),
          landResourceRef = Some(s"LND-REF-$i")
        ))

        val fullReturnWithMany = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, land = Some(lands))

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(fullReturnWithMany))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, landOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("To add a new land, you must remove one.")
        }
      }

      "must return OK with pagination when more than 15 lands exist" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val lands = (1 to 20).map(i => createLand(
          s"LND0$i",
          address = Some(s"Address $i"),
          landResourceRef = Some(s"LND-REF-$i")
        ))

        val fullReturnWithManyLands = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, land = Some(lands))

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(fullReturnWithManyLands))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, landOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("govuk-pagination")
        }
      }

      "must redirect to BeforeStartReturn when no returnId exists" in {
        val noReturnIdUserAnswers = testUserAnswers.copy(returnId = None)

        val application = applicationBuilder(userAnswers = Some(noReturnIdUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(GET, landOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.preliminary.routes.BeforeStartReturnController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when fullReturnService fails" in {
        val mockFullReturnService = mock[FullReturnService]

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Service error")))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, landOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when lands missing landResourceRef" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val landWithoutRef = createLand(
          testStorn,
          address = Some("Baker Street"),
          landResourceRef = None
        )

        val fullReturnWithBadLand = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, land = Some(Seq(landWithoutRef)))

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(fullReturnWithBadLand))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, landOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must handle pagination index correctly" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val lands = (1 to 30).map(i => createLand(
          s"LND0$i",
          address = Some(s"Address $i"),
          landResourceRef = Some(s"LND-REF-$i")
        ))

        val fullReturnWithManyLands = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, land = Some(lands))

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(fullReturnWithManyLands))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val page2Route = routes.LandOverviewController.onPageLoad(2).url
          val request = FakeRequest(GET, page2Route)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Address 16")
        }
      }
    }

    ".onSubmit" - {
      "must redirect to Before you start when user answers yes (true)" in {
        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, landOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.LandBeforeYouStartController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when user answers no (false)" in {
        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, landOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must return BadRequest when invalid data is submitted with no lands" in {
        val emptyFullReturn = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, land = None)
        val emptyUserAnswers = testUserAnswers.copy(fullReturn = Some(emptyFullReturn))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, landOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", ""))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }

      "must return BadRequest when invalid data is submitted with lands" in {
        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, landOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include("Baker Street")
        }
      }

      "must calculate errorCalc correctly on submit with form errors" in {
        val lands = (1 to 110).map(i => createLand(
          s"LND0$i",
          address = Some(s"Address $i"),
          landResourceRef = Some(s"LND-REF-$i")
        ))

        val fullReturnWithMany = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, land = Some(lands))
        val userAnswersWithMany = testUserAnswers.copy(fullReturn = Some(fullReturnWithMany))

        val application = applicationBuilder(userAnswers = Some(userAnswersWithMany))
          .build()

        running(application) {
          val request = FakeRequest(POST, landOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", ""))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }
    }

    ".changeLand" - {
      // TODO: redirect to CYA page
      "must populate session and redirect to Land CYA when land found" in {
        val mockPopulateLandService = mock[PopulateLandService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockPopulateLandService.populateLandInSession(any(), any()))
          .thenReturn(Success(testUserAnswers))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[PopulateLandService].toInstance(mockPopulateLandService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, changeLandRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandBeforeYouStartController.onPageLoad().url

          verify(mockPopulateLandService, times(1)).populateLandInSession(eqTo(testLand), any())
          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "must redirect to JourneyRecovery when land not found" in {
        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .build()

        running(application) {
          val nonExistentLandRoute = routes.LandOverviewController.changeLand("NONEXISTENT").url
          val request = FakeRequest(GET, nonExistentLandRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must handle lands with different IDs" in {
        val land2 = createLand(
          "LND002",
          address = Some("Regent's Street"),
          landResourceRef = Some("LND-REF-002")
        )
        val fullReturnWithMultiple = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, land = Some(Seq(testLand, land2)))
        val userAnswersMultiple = testUserAnswers.copy(fullReturn = Some(fullReturnWithMultiple))

        val mockPopulateLandService = mock[PopulateLandService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockPopulateLandService.populateLandInSession(any(), any()))
          .thenReturn(Success(userAnswersMultiple))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswersMultiple))
          .overrides(
            bind[PopulateLandService].toInstance(mockPopulateLandService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val changeLand2Route = routes.LandOverviewController.changeLand("LND002").url
          val request = FakeRequest(GET, changeLand2Route)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          verify(mockPopulateLandService, times(1)).populateLandInSession(eqTo(land2), any())
        }
      }
    }

    ".removeLand" - {
      "must set landId in session and redirect to Remove Land" in {
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, removeLandRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.RemoveLandController.onPageLoad().url

          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "must handle different lands IDs" in {
        val landIds = Seq("LND001", "LND002", "LND003")

        landIds.foreach { landId =>
          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(testUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val removeRoute = routes.LandOverviewController.removeLand(landId).url
            val request = FakeRequest(GET, removeRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.RemoveLandController.onPageLoad().url
          }
        }
      }
    }
  }
}
