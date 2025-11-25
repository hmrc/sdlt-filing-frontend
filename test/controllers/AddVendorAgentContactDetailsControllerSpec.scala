/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import base.SpecBase
import forms.vendor.AddVendorAgentContactDetailsFormProvider
import models.vendor.AddVendorAgentContactDetails
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.vendor.AddVendorAgentContactDetailsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.vendor.AddVendorAgentContactDetailsView
import controllers.vendor.routes
import org.scalatest.matchers.must.Matchers
import pages.vendor.AgentNamePage

import scala.concurrent.Future

class AddVendorAgentContactDetailsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  def onwardRoute = Call("GET", "/foo")

  lazy val addVendorAgentContactDetailsRoute: String = controllers.vendor.routes.AddVendorAgentContactDetailsController.onPageLoad(NormalMode).url

  val formProvider = new AddVendorAgentContactDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  "AddVendorAgentContactDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(AgentNamePage, "Mary Brown").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addVendorAgentContactDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddVendorAgentContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "Mary Brown")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(AgentNamePage, "Mary Brown").success.value
        .set(AddVendorAgentContactDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addVendorAgentContactDetailsRoute)

        val view = application.injector.instanceOf[AddVendorAgentContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, "Mary Brown")(request, messages(application)).toString
      }
    }

    "must redirect to the Agent Contact Details View when 'Yes' is selected" in {

      val userAnswersWithAgentName = emptyUserAnswers.set(AgentNamePage, "Mary Brown").success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithAgentName))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addVendorAgentContactDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        //TODO - add proper route here when completed
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to the Agent Contact Details View when 'No' is selected" in {

      val userAnswersWithAgentName = emptyUserAnswers.set(AgentNamePage, "Mary Brown").success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithAgentName))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addVendorAgentContactDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        //TODO - add proper route here when completed
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswersWithAgentName = emptyUserAnswers.set(AgentNamePage, "Mary Brown").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentName)).build()

      running(application) {
        val request =
          FakeRequest(POST, addVendorAgentContactDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddVendorAgentContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "Mary Brown")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addVendorAgentContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addVendorAgentContactDetailsRoute)
            .withFormUrlEncodedBody(("value", AddVendorAgentContactDetails.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
