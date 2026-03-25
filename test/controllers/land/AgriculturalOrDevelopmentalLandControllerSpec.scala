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
import forms.land.AgriculturalOrDevelopmentalLandFormProvider
import models.{NormalMode, UserAnswers}
import models.land.{LandSelectMeasurementUnit, LandTypeOfProperty}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.land.{AgriculturalOrDevelopmentalLandPage, AreaOfLandPage, DoYouKnowTheAreaOfLandPage, LandSelectMeasurementUnitPage, LandTypeOfPropertyPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.land.AgriculturalOrDevelopmentalLandView

import scala.concurrent.Future

class AgriculturalOrDevelopmentalLandControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AgriculturalOrDevelopmentalLandFormProvider()
  val form = formProvider()

  lazy val agriculturalOrDevelopmentalLandRoute = controllers.land.routes.AgriculturalOrDevelopmentalLandController.onPageLoad(NormalMode).url

  "AgriculturalOrDevelopmentalLand Controller" - {

    "must return OK and the correct view for a GET when property type is Mixed" in {

      val userAnswers = emptyUserAnswers.set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agriculturalOrDevelopmentalLandRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[AgriculturalOrDevelopmentalLandView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when property type is NonResidential" in {

      val userAnswers = emptyUserAnswers.set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agriculturalOrDevelopmentalLandRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[AgriculturalOrDevelopmentalLandView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Land CYA for a GET if property type is Residential" in {

      val userAnswers = emptyUserAnswers.set(LandTypeOfPropertyPage, LandTypeOfProperty.Residential).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agriculturalOrDevelopmentalLandRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to Land CYA for a GET if property type is Additional" in {

      val userAnswers = emptyUserAnswers.set(LandTypeOfPropertyPage, LandTypeOfProperty.Additional).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agriculturalOrDevelopmentalLandRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value
        .set(AgriculturalOrDevelopmentalLandPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agriculturalOrDevelopmentalLandRoute)
        val view = application.injector.instanceOf[AgriculturalOrDevelopmentalLandView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when yes is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, agriculturalOrDevelopmentalLandRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to LandCheckYourAnswers when no is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, agriculturalOrDevelopmentalLandRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
      }
    }

    "must clear land area answers when answer changes from yes to no" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(AgriculturalOrDevelopmentalLandPage, true).success.value
        .set(DoYouKnowTheAreaOfLandPage, true).success.value
        .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Sqms).success.value
        .set(AreaOfLandPage, "500").success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, agriculturalOrDevelopmentalLandRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture())
        uaCaptor.getValue.get(DoYouKnowTheAreaOfLandPage).isDefined mustBe false
        uaCaptor.getValue.get(LandSelectMeasurementUnitPage).isDefined mustBe false
        uaCaptor.getValue.get(AreaOfLandPage).isDefined mustBe false
      }
    }

    "must not clear downstream pages when answer remains yes" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(AgriculturalOrDevelopmentalLandPage, true).success.value
        .set(DoYouKnowTheAreaOfLandPage, true).success.value
        .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Sqms).success.value
        .set(AreaOfLandPage, "500").success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, agriculturalOrDevelopmentalLandRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture())
        uaCaptor.getValue.get(DoYouKnowTheAreaOfLandPage) mustBe Some(true)
        uaCaptor.getValue.get(LandSelectMeasurementUnitPage) mustBe Some(LandSelectMeasurementUnit.Sqms)
        uaCaptor.getValue.get(AreaOfLandPage) mustBe Some("500")
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, agriculturalOrDevelopmentalLandRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[AgriculturalOrDevelopmentalLandView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, agriculturalOrDevelopmentalLandRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, agriculturalOrDevelopmentalLandRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}