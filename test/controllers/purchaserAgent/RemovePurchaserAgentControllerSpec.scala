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

package controllers.purchaserAgent

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import controllers.routes
import forms.purchaserAgent.RemovePurchaserAgentFormProvider
import models.purchaserAgent.RemovePurchaserAgent
import models.{FullReturn, NormalMode, Purchaser, ReturnAgent, UserAnswers}
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaserAgent.RemovePurchaserAgentPage
import play.api.data.Form
import play.api.inject
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import views.html.purchaserAgent.RemovePurchaserAgentView

import scala.concurrent.Future

class RemovePurchaserAgentControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val removePurchaserAgentRoute: String = controllers.purchaserAgent.routes.RemovePurchaserAgentController.onPageLoad(NormalMode).url

  val formProvider = new RemovePurchaserAgentFormProvider()
  val form: Form[RemovePurchaserAgent] = formProvider()

  val mockConnector: StampDutyLandTaxConnector = mock[StampDutyLandTaxConnector]

  val testStorn = "TESTSTORN"

  private val testFullReturn = FullReturn(
    stornId = testStorn,
    returnResourceRef = "REF001",
    returnAgent = Some(Seq(
      ReturnAgent(
        name = Some("Test return agent"),
        agentType = Some("PURCHASER")
      )
    ))
  )

  "RemovePurchaserAgent Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removePurchaserAgentRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemovePurchaserAgentView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, "Test return agent")(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn))
          .set(RemovePurchaserAgentPage, RemovePurchaserAgent.Yes).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removePurchaserAgentRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Test return agent")
          contentAsString(result) must include(RemovePurchaserAgent.Yes.toString)
          contentAsString(result) must include("checked")
        }
      }

      "must redirect to PurchaserAgentName page if full return does not contain return agent for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removePurchaserAgentRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removePurchaserAgentRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

    }

    "onSubmit" - {

      "must redirect to PurchaserAgentName page if full return does not contain return agent for a POST" in {
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(testFullReturn.copy(returnAgent = None)))

        val application = applicationBuilder(Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, removePurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", RemovePurchaserAgent.Yes.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, removePurchaserAgentRoute)
              .withFormUrlEncodedBody(("value", RemovePurchaserAgent.Yes.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, removePurchaserAgentRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[RemovePurchaserAgentView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, "Test return agent")(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, removePurchaserAgentRoute)
              .withFormUrlEncodedBody(("value", RemovePurchaserAgent.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to purchaser overview when no is selected" in {
        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, removePurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", RemovePurchaserAgent.No.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }

      "must redirect to purchaser overview when backend fails" in {
        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn))

        when(mockConnector.updateReturnVersion(any())(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(Future.failed(new RuntimeException("simulated backend failure")))

        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[StampDutyLandTaxConnector].toInstance(mockConnector)
          ).build()

        running(application) {
          val request = FakeRequest(POST, removePurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", RemovePurchaserAgent.Yes.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }

      //TODO add this test back in once purchaser overview page has been created DTR-1835
//      "must set flash message when purchaser agent is deleted" in {
//        val userAnswers = emptyUserAnswers.copy(storn = testStorn, fullReturn = Some(testFullReturn))
//
//        val application = applicationBuilder(Some(userAnswers)).build()
//
//        running(application) {
//          val request = FakeRequest(POST, removePurchaserAgentRoute)
//            .withFormUrlEncodedBody(("value", RemovePurchaserAgent.Yes.toString))
//
//          val result = route(application, request).value
//
//          status(result) mustEqual SEE_OTHER
//          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
//          flash(result).get("purchaserAgentDeleted").value mustEqual "Test return agent"
//        }
//      }

    }

  }
}
