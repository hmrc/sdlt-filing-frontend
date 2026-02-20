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
import forms.land.AreaOfLandFormProvider
import models.NormalMode
import models.land.{LandSelectMeasurementUnit, LandTypeOfProperty}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.land.{AreaOfLandPage, LandSelectMeasurementUnitPage, LandTypeOfPropertyPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.land.AreaOfLandView

import scala.concurrent.Future

class AreaOfLandControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val squareMetresUnit = LandSelectMeasurementUnit.Sqms.toString
  private val hectaresUnit = LandSelectMeasurementUnit.Hectares.toString

  private val formProvider = new AreaOfLandFormProvider()
  private val formSquareMetres = formProvider(squareMetresUnit)
  private val formHectares = formProvider(hectaresUnit)

  lazy val areaOfLandRoute: String = controllers.land.routes.AreaOfLandController.onPageLoad(NormalMode).url

  "AreaOfLand Controller" - {

    "when unit type is square metres" - {

      "must return OK and the correct view for a GET" in {

        val userAnswers = emptyUserAnswers
          .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Sqms).success.value
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, areaOfLandRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AreaOfLandView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formSquareMetres, NormalMode, squareMetresUnit)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = emptyUserAnswers
          .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Sqms).success.value
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
          .set(AreaOfLandPage, "100").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, areaOfLandRoute)

          val view = application.injector.instanceOf[AreaOfLandView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formSquareMetres.fill("100"), NormalMode, squareMetresUnit)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val userAnswers = emptyUserAnswers
          .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Sqms).success.value
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, areaOfLandRoute)
              .withFormUrlEncodedBody(("value", "300"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val userAnswers = emptyUserAnswers
          .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Sqms).success.value
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, areaOfLandRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = formSquareMetres.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[AreaOfLandView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, squareMetresUnit)(request, messages(application)).toString
        }
      }
    }

    "when unit type is hectares" - {

      "must return OK and the correct view for a GET" in {

        val userAnswers = emptyUserAnswers
          .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Hectares).success.value
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, areaOfLandRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AreaOfLandView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formHectares, NormalMode, hectaresUnit)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        val userAnswers = emptyUserAnswers
          .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Hectares).success.value
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
          .set(AreaOfLandPage, "100.123").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, areaOfLandRoute)

          val view = application.injector.instanceOf[AreaOfLandView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formSquareMetres.fill("100.123"), NormalMode, hectaresUnit)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val userAnswers = emptyUserAnswers
          .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Hectares).success.value
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, areaOfLandRoute)
              .withFormUrlEncodedBody(("value", "300.999"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val userAnswers = emptyUserAnswers
          .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Hectares).success.value
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, areaOfLandRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = formHectares.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[AreaOfLandView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, hectaresUnit)(request, messages(application)).toString
        }
      }
    }

    "must redirect to select measurement unit type page when unit type not set for a GET" in {
      val userAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, areaOfLandRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.land.routes.LandSelectMeasurementUnitController.onPageLoad(NormalMode).url)
      }
    }

    "must redirect to select measurement unit type page when unit type not set for a POST" in {
      val userAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, areaOfLandRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.land.routes.LandSelectMeasurementUnitController.onPageLoad(NormalMode).url)
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, areaOfLandRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, areaOfLandRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
