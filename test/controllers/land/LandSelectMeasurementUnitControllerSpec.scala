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
import forms.land.LandSelectMeasurementUnitFormProvider
import models.land.{LandSelectMeasurementUnit, LandTypeOfProperty}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.land.{AreaOfLandPage, LandSelectMeasurementUnitPage, LandTypeOfPropertyPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.land.LandSelectMeasurementUnitView

import scala.concurrent.Future

class LandSelectMeasurementUnitControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val landSelectMeasurementUnitRoute: String = controllers.land.routes.LandSelectMeasurementUnitController.onPageLoad(NormalMode).url

  val formProvider = new LandSelectMeasurementUnitFormProvider()
  val form: Form[LandSelectMeasurementUnit] = formProvider()
  val testStorn = "TESTSTORN"

  "LandSelectMeasurementUnit Controller" - {

    "must return OK and the correct view for a GET when property type is mixed " in {

      val userAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, landSelectMeasurementUnitRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LandSelectMeasurementUnitView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when property type is non-residential" in {

      val userAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, landSelectMeasurementUnitRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LandSelectMeasurementUnitView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    //TODO - DTR-2495 - SPRINT-10 - update redirect to CYA
    "redirect to CYA for a GET if property type is Residential" in {

      val userAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.Residential).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, landSelectMeasurementUnitRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode).url
      }
    }

    //TODO - DTR-2495 - SPRINT-10 - update redirect to CYA
    "redirect to CYA for a GET if property type is Additional" in {

      val userAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.Additional).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, landSelectMeasurementUnitRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode).url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value
        .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Sqms).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, landSelectMeasurementUnitRoute)

        val view = application.injector.instanceOf[LandSelectMeasurementUnitView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(LandSelectMeasurementUnit.Sqms), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted and property type is non-residential" in {

      val userAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value

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
          FakeRequest(POST, landSelectMeasurementUnitRoute)
            .withFormUrlEncodedBody(("value", LandSelectMeasurementUnit.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted and property type is mixed" in {

      val userAnswers = emptyUserAnswers
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
          FakeRequest(POST, landSelectMeasurementUnitRoute)
            .withFormUrlEncodedBody(("value", LandSelectMeasurementUnit.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must clear land area and redirect to the next page when previous unit type was different" in {

      val userAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
        .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Sqms).success.value
        .set(AreaOfLandPage, "100.000").success.value

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
          FakeRequest(POST, landSelectMeasurementUnitRoute)
            .withFormUrlEncodedBody(("value", LandSelectMeasurementUnit.Hectares.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture())
        uaCaptor.getValue.get(AreaOfLandPage).isDefined mustBe false
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, landSelectMeasurementUnitRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[LandSelectMeasurementUnitView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, landSelectMeasurementUnitRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, landSelectMeasurementUnitRoute)
            .withFormUrlEncodedBody(("value", LandSelectMeasurementUnit.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
