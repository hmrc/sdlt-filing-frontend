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

package controllers.vendor

import base.SpecBase
import constants.FullReturnConstants.completeFullReturn
import controllers.routes
import forms.vendor.AgentNameFormProvider
import models.{FullReturn, NormalMode, ReturnAgent, ReturnInfo, UserAnswers, Vendor}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.vendor.AgentNamePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.vendor.AgentNameView

import scala.concurrent.Future

class AgentNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AgentNameFormProvider()
  val form = formProvider()
  val formKey = "agentName"

  lazy val agentNameRoute = controllers.vendor.routes.AgentNameController.onPageLoad(NormalMode).url

  "AgentName Controller" - {

    "must return OK and the correct view for a GET when no return agent or vendors" in {
      val fullReturn = completeFullReturn.copy(returnAgent = None, vendor = None)

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when return agent exists and no vendors" in {
      val fullReturn = completeFullReturn.copy(vendor = None)

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to VendorCYA when agent is type VENDOR and main vendor is represented by agent" in {
      val fullReturn = completeFullReturn.copy(
        returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
        returnAgent = Some(Seq(ReturnAgent(agentType = Some("VENDOR")))),
        vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("true"))))
      )

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to VendorCYA when agent is not type VENDOR and main vendor is not represented by agent" in {
      val fullReturn = completeFullReturn.copy(
        returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
        returnAgent = Some(Seq(ReturnAgent(agentType = Some("SOLICITOR")))),
        vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("false"))))
      )

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to VendorCYA there is no return agent and main vendor is not represented by agent" in {
      val fullReturn = completeFullReturn.copy(
        returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
        returnAgent = None,
        vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("false"))))
      )
      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to error page when agent is type VENDOR and main vendor is not represented by agent" in { // Change to actual error page (TBC)
      val fullReturn = completeFullReturn.copy(
        returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
        returnAgent = Some(Seq(ReturnAgent(agentType = Some("VENDOR")))),
        vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("false"))))
      )
      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.GenericErrorController.onPageLoad().url
      }
    }

    "must redirect to error page when there is no return agent and main vendor is represented by agent" in { // Change to actual error page (TBC)
      val fullReturn = completeFullReturn.copy(
        returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
        returnAgent = None,
        vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("true"))))
      )
      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.GenericErrorController.onPageLoad().url
      }
    }

    "must redirect to error page when agent is not type VENDOR and main vendor is represented by agent" in { // Change to actual error page (TBC)
      val fullReturn = completeFullReturn.copy(
        returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
        returnAgent = Some(Seq(ReturnAgent(agentType = Some("SOLICITOR")))),
        vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("true"))))
      )
      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.GenericErrorController.onPageLoad().url
      }
    }

    "must redirect to error page when agent is type VENDOR and there are no vendors" in { // Change to actual error page (TBC)
      val fullReturn = completeFullReturn.copy(
        returnAgent = Some(Seq(ReturnAgent(agentType = Some("VENDOR")))),
        vendor = None
      )
      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.GenericErrorController.onPageLoad().url
      }
    }

    "must redirect to journey recovery page when full return doesn't exist" in {
      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = None)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(completeFullReturn.copy(vendor = None)))
        .set(AgentNamePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val view = application.injector.instanceOf[AgentNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

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
          FakeRequest(POST, agentNameRoute)
            .withFormUrlEncodedBody((formKey, "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, agentNameRoute)
            .withFormUrlEncodedBody((formKey, ""))

        val boundForm = form.bind(Map(formKey -> ""))

        val view = application.injector.instanceOf[AgentNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, agentNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, agentNameRoute)
            .withFormUrlEncodedBody((formKey, "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
