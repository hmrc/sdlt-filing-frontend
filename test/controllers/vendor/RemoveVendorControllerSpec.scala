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
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.completeFullReturn
import controllers.routes
import forms.vendor.RemoveVendorFormProvider
import models.vendor.DeleteVendorReturn
import models.{ReturnInfo, ReturnVersionUpdateReturn, UserAnswers, Vendor}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.UpstreamErrorResponse
import views.html.vendor.RemoveVendorView

import scala.concurrent.Future

class RemoveVendorControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new RemoveVendorFormProvider()
  val form = formProvider()

  lazy val removeVendorRoute = controllers.vendor.routes.RemoveVendorController.onPageLoad().url

  "RemoveVendor Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeVendorRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveVendorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, None)(request, messages(application)).toString
      }
    }

    "must redirect to vendor overview for a GET when removeVendorId is not found" ignore {
      // TODO DTR-1028 add this test when id is retrieved from vendorCurrent.removeVendorId
    }

    "must delete vendor and redirect to vendor overview when Yes is selected and required data is found" in {

      val mockBackendConnector = mock[StampDutyLandTaxConnector]
      val mockSessionRepository = mock[SessionRepository]

      val vendorId = "VEN001"
      val fullReturn = completeFullReturn.copy(
        returnInfo = Some(ReturnInfo(version = Some("1.00"))),
        vendor = Some(Seq(Vendor(vendorID = Some(vendorId), vendorResourceRef = Some("VEN-REF-001"))))
      )
      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

      when(mockBackendConnector.updateReturnVersion(any)(any(), any()))
        .thenReturn(Future.successful(ReturnVersionUpdateReturn(true)))
      when(mockBackendConnector.deleteVendor(any)(any(), any()))
        .thenReturn(Future.successful(DeleteVendorReturn(true)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeVendorRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        flash(result).get("vendorDeleted") mustBe Some("true")
      }
    }

    "must fail to delete and redirect to vendor overview" - {
      "when Yes is selected and version is not found" in {

        val mockSessionRepository = mock[SessionRepository]

        val vendorId = "VEN-001"
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(version = None)),
          vendor = Some(Seq(Vendor(vendorID = Some(vendorId), vendorResourceRef = Some("VEN-REF-001"))))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removeVendorRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "when Yes is selected and vendor is not found" in {

        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val mockSessionRepository = mock[SessionRepository]

        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(version = Some("1.00"))),
          vendor = None
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        when(mockBackendConnector.updateReturnVersion(any)(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(true)))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removeVendorRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "when Yes is selected and updateReturnVersion fails" in {

        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val mockSessionRepository = mock[SessionRepository]

        val vendorId = "VEN-001"
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(version = Some("1.00"))),
          vendor = Some(Seq(Vendor(vendorID = Some(vendorId), vendorResourceRef = Some("VEN-REF-001"))))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        when(mockBackendConnector.updateReturnVersion(any)(any(), any()))
          .thenReturn(Future.failed(UpstreamErrorResponse("Bad Request", 400)))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removeVendorRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "when Yes is selected and deleteVendor fails" in {

        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val mockSessionRepository = mock[SessionRepository]

        val vendorId = "VEN-001"
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(version = Some("1.00"))),
          vendor = Some(Seq(Vendor(vendorID = Some(vendorId), vendorResourceRef = Some("VEN-REF-001"))))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        when(mockBackendConnector.updateReturnVersion(any)(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(true)))
        when(mockBackendConnector.deleteVendor(any)(any(), any()))
          .thenReturn(Future.failed(UpstreamErrorResponse("Bad Request", 400)))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removeVendorRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }
    }

    "must redirect to vendor overview when No is selected" in {

      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(completeFullReturn))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeVendorRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to vendor overview for a POST when removeVendorId is not found" ignore {
      // TODO DTR-1028 add this test when id is retrieved from vendorCurrent.removeVendorId
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeVendorRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveVendorView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, None)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removeVendorRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removeVendorRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
