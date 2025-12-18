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
import constants.FullReturnConstants
import controllers.routes
import forms.vendor.VendorAgentsContactDetailsFormProvider
import models.vendor.VendorAgentsContactDetails
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.vendor.*
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.vendor.VendorAgentsContactDetailsView

import scala.concurrent.Future

class VendorAgentsContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new VendorAgentsContactDetailsFormProvider()
  val form: Form[VendorAgentsContactDetails] = formProvider()

  lazy val vendorAgentsContactDetailsRoute: String = controllers.vendor.routes.VendorAgentsContactDetailsController.onPageLoad(NormalMode).url

  val userAnswersWithAgentDetails: UserAnswers = emptyUserAnswers
    .set(AgentNamePage, "Jones & Co, Leeds").success.value
    .set(VendorRepresentedByAgentPage, true).success.value
    .set(AddVendorAgentContactDetailsPage, true).success.value


  "VendorAgentsContactDetails Controller" - {

    //TODO update test when CYA page created
    "must redirect to index controller if knowsAgentDetails is false" in {

      val userAnswers = emptyUserAnswers
        .set(AgentNamePage, "Jones & Co, Leeds").success.value
        .set(VendorRepresentedByAgentPage, true).success.value
        .set(AddVendorAgentContactDetailsPage, false).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, vendorAgentsContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad().url
      }

    "must return OK and the correct view for a GET when agent name exists and is represented by agent is true" in {

      val fullReturnWithNonVendorAgent = FullReturnConstants.completeFullReturn.copy(
        returnAgent = None,
        vendor = None,
      )

      val userAnswers = userAnswersWithAgentDetails.copy(fullReturn = Some(fullReturnWithNonVendorAgent))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, vendorAgentsContactDetailsRoute)

        val view = application.injector.instanceOf[VendorAgentsContactDetailsView]

        val agentName: Option[String] = userAnswers.get(AgentNamePage)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, agentName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered no return agent or vendors" in {

      val fullReturnWithNonVendorAgent = FullReturnConstants.completeFullReturn.copy(
        returnAgent = None,
        vendor = None
      )

      val userAnswers = userAnswersWithAgentDetails
        .set(VendorAgentsContactDetailsPage, VendorAgentsContactDetails(Some("value1"), Some("value2"))).success.value
        .copy(fullReturn = Some(fullReturnWithNonVendorAgent))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, vendorAgentsContactDetailsRoute)

        val view = application.injector.instanceOf[VendorAgentsContactDetailsView]

        val agentName: Option[String] = userAnswers.get(AgentNamePage)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(VendorAgentsContactDetails(Some("value1"), Some("value2"))), NormalMode, agentName
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithAgentDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentsContactDetailsRoute)
            .withFormUrlEncodedBody(("phoneNumber", "12345678912345"), ("emailAddress", "example-test@test.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect when no data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentsContactDetailsRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", ""),
              ("emailAddress", "")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.vendor.routes.DoYouKnowYourAgentReferenceController.onPageLoad(NormalMode).url

      }
    }

    "must return a Bad Request and errors when invalid phone number data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentsContactDetailsRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", "123456789#"),
              ("emailAddress", "example-test@test.com")
            )

        val boundForm = form.bind(Map(
          "phoneNumber" -> "123456789#",
          "emailAddress" -> "example-test@test.com"
        ))

        val view = application.injector.instanceOf[VendorAgentsContactDetailsView]

        val agentName: Option[String] = userAnswersWithAgentDetails.get(AgentNamePage)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, agentName)(request, messages(application)).toString
      }
    }

    "must redirect to return task list if agent name is not found" in {

      val fullReturnWithNonVendorAgent = FullReturnConstants.completeFullReturn.copy(
        returnAgent = None,
        vendor = None,
      )

      val userAnswers = emptyUserAnswers
        .set(VendorRepresentedByAgentPage, true).success.value
        .set(AddVendorAgentContactDetailsPage, true).success.value
        .copy(fullReturn = Some(fullReturnWithNonVendorAgent))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, vendorAgentsContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, vendorAgentsContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentsContactDetailsRoute)
            .withFormUrlEncodedBody(("phoneNumber", "value 1"), ("emailAddress", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must pass the agentName to view for a GET" in {

      val fullReturnWithNonVendorAgent = FullReturnConstants.completeFullReturn.copy(
        returnAgent = None,
        vendor = None
      )

      val userAnswers = userAnswersWithAgentDetails.copy(fullReturn = Some(fullReturnWithNonVendorAgent))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, vendorAgentsContactDetailsRoute)

        val view = application.injector.instanceOf[VendorAgentsContactDetailsView]

        val agentName: Option[String] = userAnswersWithAgentDetails.get(AgentNamePage)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, agentName)(request, messages(application)).toString
        agentName.value mustEqual "Jones & Co, Leeds"
      }
    }

    "must pass the agentName to view for a POST when BAD REQUEST returned" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentsContactDetailsRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", ""),
              ("emailAddress", "notandemail")
            )

        val boundForm = form.bind(Map(
          "phoneNumber" -> "",
          "emailAddress" -> "notandemail"
        ))

        val view = application.injector.instanceOf[VendorAgentsContactDetailsView]

        val agentName: Option[String] = userAnswersWithAgentDetails.get(AgentNamePage)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, agentName)(request, messages(application)).toString
        agentName.value mustEqual "Jones & Co, Leeds"
      }
    }
  }
}
