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
import controllers.routes
import forms.lease.TypeOfLeaseFormProvider
import models.lease.TypeOfLease
import models.{FullReturn, Land, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.lease.TypeOfLeasePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.lease.TypeOfLeaseView

import scala.concurrent.Future

class TypeOfLeaseControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val typeOfLeaseRoute = controllers.lease.routes.TypeOfLeaseController.onPageLoad(NormalMode).url

  val formProvider = new TypeOfLeaseFormProvider()
  val form = formProvider()

  "TypeOfLease Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, typeOfLeaseRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TypeOfLeaseView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(TypeOfLeasePage, TypeOfLease.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, typeOfLeaseRoute)

        val view = application.injector.instanceOf[TypeOfLeaseView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(TypeOfLease.values.head), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val validLand =
        Land(propertyType = Some("01"))

      val validFullReturn =
        FullReturn(
          stornId = "1",
          returnResourceRef = "ref",
          land = Some(Seq(validLand))
        )

      val validUserAnswers =
        emptyUserAnswers.copy(fullReturn = Some(validFullReturn))

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(validUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, typeOfLeaseRoute)
            .withFormUrlEncodedBody(("value", TypeOfLease.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, typeOfLeaseRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TypeOfLeaseView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, typeOfLeaseRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, typeOfLeaseRoute)
            .withFormUrlEncodedBody(("value", TypeOfLease.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return BadRequest when residential lease does not match property type for a POST" in {
      val land = Land(propertyType = Some("02"))

      val fullReturn = FullReturn(
        stornId = "1",
        returnResourceRef = "ref",
        land = Some(Seq(land))
      )

      val userAnswers =
        emptyUserAnswers.copy(fullReturn = Some(fullReturn))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, typeOfLeaseRoute)
            .withFormUrlEncodedBody("value" -> "R")

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("When the type of lease is ‘R - Residential’, the type of property must be ‘01 - Residential’ or ‘04 - Additional residential property liable to higher rate’")
      }
    }

    "must return BadRequest when Non-Residential lease does not match property type for a POST" in {
      val land = Land(propertyType = Some("01"))

      val fullReturn = FullReturn(
        stornId = "1",
        returnResourceRef = "ref",
        land = Some(Seq(land))
      )

      val userAnswers =
        emptyUserAnswers.copy(fullReturn = Some(fullReturn))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, typeOfLeaseRoute)
            .withFormUrlEncodedBody("value" -> "N")

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("When the type of lease is ‘N - Non-residential’, the type of property must be ‘03 - Non-residential’")
      }
    }

    "must return BadRequest when mixed lease does not match property type for a POST" in {
      val land = Land(propertyType = Some("01"))

      val fullReturn = FullReturn(
        stornId = "1",
        returnResourceRef = "ref",
        land = Some(Seq(land))
      )

      val userAnswers =
        emptyUserAnswers.copy(fullReturn = Some(fullReturn))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, typeOfLeaseRoute)
            .withFormUrlEncodedBody("value" -> "M")

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("When the type of lease is ‘M - Mixed use’, the type of property must be ‘02 - Mixed’")
      }
    }
  }
}