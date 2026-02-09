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

package controllers.vendorAgent

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.{completeFullReturn, completeReturnAgent}
import controllers.routes
import forms.vendorAgent.RemoveVendorAgentFormProvider
import models.{DeleteReturnAgentReturn, FullReturn, Vendor, ReturnAgent, ReturnInfo, ReturnVersionUpdateReturn, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.vendorAgent.{RemoveVendorAgentPage, VendorAgentOverviewPage}
import play.api.data.Form
import play.api.inject
import play.api.inject.bind
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import views.html.vendorAgent.RemoveVendorAgentView

import scala.concurrent.Future

class RemoveVendorAgentControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val removeVendorAgentRoute: String = controllers.vendorAgent.routes.RemoveVendorAgentController.onPageLoad().url

  val formProvider = new RemoveVendorAgentFormProvider()
  val form: Form[Boolean] = formProvider()

  val mockConnector: StampDutyLandTaxConnector = mock[StampDutyLandTaxConnector]

  val testStorn = "TESTSTORN"
  private val testFullReturn = FullReturn(
    stornId = testStorn,
    returnResourceRef = "REF001",
    returnInfo = Some(
      ReturnInfo(
        version = Some("2")
      )),
    returnAgent = Some(Seq(
      ReturnAgent(
        returnAgentID = Some("RA001"),
        returnID = Some("RET-PA-001"),
        name = Some("Test return agent"),
        agentType = Some("VENDOR")
      )
    ))
  )

  "RemoveVendorAgent Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val agentId =
          testFullReturn.returnAgent.value.head.returnAgentID.value

        val userAnswers = emptyUserAnswers.set(VendorAgentOverviewPage, agentId)
          .success
          .value
          .copy(storn = testStorn, fullReturn = Some(completeFullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removeVendorAgentRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemoveVendorAgentView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, completeReturnAgent.name.get)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val agentId =
          testFullReturn.returnAgent.value.head.returnAgentID.value

        val userAnswers = emptyUserAnswers.set(VendorAgentOverviewPage, agentId)
          .success
          .value
          .set(RemoveVendorAgentPage, true).success.value
          .copy(storn = testStorn, fullReturn = Some(completeFullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removeVendorAgentRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include(HtmlFormat.escape(completeReturnAgent.name.get).toString)
          contentAsString(result) must include("Yes")
          contentAsString(result) must include("checked")
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removeVendorAgentRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

    }

    "onSubmit" - {

      "must redirect to vendor agent overview page if full return does not contain return agent for a POST" in {
        val agentId = "RA001"

        val userAnswers = emptyUserAnswers.set(VendorAgentOverviewPage, agentId)
          .success
          .value
          .copy(fullReturn = Some(testFullReturn.copy(returnAgent = None)))

        val application = applicationBuilder(Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, removeVendorAgentRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val singleVendorAgent = ReturnAgent(
          returnAgentID = Some("RA001"),
          returnID = Some("RET-PA-001"),
          name = Some("Test return agent"),
          agentType = Some("VENDOR")
        )

        val fullReturnWithSingleVendor = completeFullReturn.copy(
          returnAgent = Some(Seq(singleVendorAgent))
        )

        val agentId = singleVendorAgent.returnAgentID.value

        val userAnswers = emptyUserAnswers.set(VendorAgentOverviewPage, agentId)
          .success
          .value
          .copy(storn = testStorn, fullReturn = Some(fullReturnWithSingleVendor))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, removeVendorAgentRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[RemoveVendorAgentView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, singleVendorAgent.name.get)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, removeVendorAgentRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to vendor agent overview when no is selected" in {
        val singleVendorAgent = ReturnAgent(
          returnAgentID = Some("RA001"),
          returnID = Some("RET-PA-001"),
          name = Some("Test return agent"),
          agentType = Some("VENDOR")
        )

        val fullReturnWithSingleVendor = completeFullReturn.copy(
          returnAgent = Some(Seq(singleVendorAgent))
        )

        val agentId = singleVendorAgent.returnAgentID.value

        val userAnswers = emptyUserAnswers.set(VendorAgentOverviewPage, agentId)
          .success
          .value
          .copy(storn = testStorn, fullReturn = Some(fullReturnWithSingleVendor))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, removeVendorAgentRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
        }
      }

      "must redirect to vendor agent overview when backend fails" in {
        val singleVendorAgent = ReturnAgent(
          returnAgentID = Some("RA001"),
          returnID = Some("RET-PA-001"),
          name = Some("Test return agent"),
          agentType = Some("VENDOR")
        )

        val fullReturnWithSingleVendor = completeFullReturn.copy(
          returnAgent = Some(Seq(singleVendorAgent))
        )

        val agentId = singleVendorAgent.returnAgentID.value

        val userAnswers = emptyUserAnswers.set(VendorAgentOverviewPage, agentId)
          .success
          .value
          .copy(storn = testStorn, fullReturn = Some(fullReturnWithSingleVendor))

        when(mockConnector.updateReturnVersion(any())(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(Future.failed(new RuntimeException("simulated backend failure")))

        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[StampDutyLandTaxConnector].toInstance(mockConnector)
          ).build()

        running(application) {
          val request = FakeRequest(POST, removeVendorAgentRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
        }
      }

      "must redirect to Vendor agent overview and set flash message when vendor agent is deleted" in {
        val singleVendorAgent = ReturnAgent(
          returnAgentID = Some("RA001"),
          returnID = Some("RET-PA-001"),
          name = Some("Test return agent"),
          agentType = Some("VENDOR")
        )

        val fullReturnWithSingleVendor = completeFullReturn.copy(
          returnAgent = Some(Seq(singleVendorAgent))
        )

        val agentId = singleVendorAgent.returnAgentID.value

        val userAnswers = emptyUserAnswers.set(VendorAgentOverviewPage, agentId)
          .success
          .value
          .copy(storn = testStorn, fullReturn = Some(fullReturnWithSingleVendor))

        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(
          mockBackendConnector.updateReturnVersion(any())(any(), any())
        ).thenReturn(
          Future.successful(ReturnVersionUpdateReturn(Some(2)))
        )

        when(
          mockBackendConnector.deleteReturnAgent(any())(any(), any())
        ).thenReturn(
          Future.successful(DeleteReturnAgentReturn(true))
        )

        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          ).build()

        running(application) {
          val request = FakeRequest(POST, removeVendorAgentRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
          flash(result).get("vendorAgentDeleted").value mustEqual singleVendorAgent.name.get
        }
      }
    }
  }
}
