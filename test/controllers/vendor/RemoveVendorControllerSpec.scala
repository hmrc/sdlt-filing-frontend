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
import pages.vendor.VendorOverviewRemovePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.UpstreamErrorResponse
import views.html.vendor.RemoveVendorView

import scala.concurrent.Future

class RemoveVendorControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new RemoveVendorFormProvider()
  val form = formProvider()

  lazy val removeVendorRoute = controllers.vendor.routes.RemoveVendorController.onPageLoad().url

  "RemoveVendor Controller" - {

    "must return OK and the correct view for a GET" in {

      val vendorResourceRef = "VEN-REF-001"

      val fullReturn = completeFullReturn.copy(
        vendor = Some(Seq(Vendor(vendorResourceRef = Some("VEN-REF-001"), forename1 = Some("John"), forename2 = Some("Michael"), name = Some("Smith"))))
      )
      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))
        .set(VendorOverviewRemovePage, vendorResourceRef).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeVendorRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveVendorView]

        val fullName = "John Michael Smith"

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, Some(fullName))(request, messages(application)).toString
        contentAsString(result) must include(fullName)
      }
    }

    "must return OK and the correct view for a GET when vendor is not found" in {

      val fullReturn = completeFullReturn.copy(
        vendor = Some(Seq(Vendor(vendorResourceRef = Some("VEN-REF-000"))))
      )
      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))
        .set(VendorOverviewRemovePage, "VEN-REF-001").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeVendorRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveVendorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, None)(request, messages(application)).toString
      }
    }

    "must redirect to vendor overview for a GET when removeVendorId is not set" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, removeVendorRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
      }
    }

    "must delete vendor and redirect to vendor overview when Yes is selected and required data is found" in {

      val mockBackendConnector = mock[StampDutyLandTaxConnector]

      val vendorResourceRef = "VEN-REF-001"
      val fullReturn = completeFullReturn.copy(
        returnInfo = Some(ReturnInfo(version = Some("1.00"))),
        vendor = Some(Seq(Vendor(vendorResourceRef = Some("VEN-REF-001"), forename1 = Some("John"), name = Some("Smith"))))
      )
      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))
        .set(VendorOverviewRemovePage, vendorResourceRef).success.value

      when(mockBackendConnector.updateReturnVersion(any)(any(), any()))
        .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(1))))
      when(mockBackendConnector.deleteVendor(any)(any(), any()))
        .thenReturn(Future.successful(DeleteVendorReturn(true)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeVendorRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        flash(result).get("vendorDeleted") mustBe Some("John Smith")
      }
    }

    "must fail to delete and redirect to vendor overview" - {
      "when Yes is selected and version is not found" in {

        val vendorResourceRef = "VEN-REF-001"
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(version = None)),
          vendor = Some(Seq(Vendor(vendorResourceRef = Some("VEN-REF-001"))))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))
          .set(VendorOverviewRemovePage, vendorResourceRef).success.value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removeVendorRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }

      "when Yes is selected and vendor is not found" in {

        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(version = Some("1.00"))),
          vendor = Some(Seq(Vendor(vendorResourceRef = Some("VEN-REF-000"))))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))
          .set(VendorOverviewRemovePage, "VEN-REF-001").success.value

        when(mockBackendConnector.updateReturnVersion(any)(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(1))))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removeVendorRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }

      "when Yes is selected and updateReturnVersion fails" in {

        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        val vendorResourceRef = "VEN-REF-001"
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(version = Some("1.00"))),
          vendor = Some(Seq(Vendor(vendorResourceRef = Some("VEN-REF-001"))))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))
          .set(VendorOverviewRemovePage, vendorResourceRef).success.value

        when(mockBackendConnector.updateReturnVersion(any)(any(), any()))
          .thenReturn(Future.failed(UpstreamErrorResponse("Bad Request", 400)))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removeVendorRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }

      "when Yes is selected and deleteVendor fails" in {

        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        val vendorResourceRef = "VEN-REF-001"
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(version = Some("1.00"))),
          vendor = Some(Seq(Vendor(vendorResourceRef = Some("VEN-REF-001"))))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))
          .set(VendorOverviewRemovePage, vendorResourceRef).success.value

        when(mockBackendConnector.updateReturnVersion(any)(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(Some(1))))
        when(mockBackendConnector.deleteVendor(any)(any(), any()))
          .thenReturn(Future.failed(UpstreamErrorResponse("Bad Request", 400)))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removeVendorRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }
    }

    "must redirect to vendor overview when No is selected" in {

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(completeFullReturn))
        .set(VendorOverviewRemovePage, "VEN-REF-001").success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeVendorRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
      }
    }

    "must redirect to vendor overview for a POST when removeVendorId is not set" in {

      val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(completeFullReturn))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeVendorRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(
        userAnswers = Some(
          emptyUserAnswers
            .set(VendorOverviewRemovePage, "VEN-REF-001").success.value
        )
      ).build()

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
