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

package controllers.purchaser

import base.SpecBase
import controllers.routes
import forms.purchaser.PurchaserRemoveFormProvider
import models.purchaser.PurchaserAndCompanyId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.PurchaserOverviewRemovePage
import play.api.inject.bind
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.twirl.api.Html
import services.purchaser.PurchaserRemoveService

import scala.concurrent.Future

class PurchaserRemoveControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new PurchaserRemoveFormProvider()
  val form = formProvider()

  lazy val purchaserRemoveRoute = controllers.purchaser.routes.PurchaserRemoveController.onPageLoad().url

  "PurchaserRemove Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockPurchaserRemoveService = mock[PurchaserRemoveService]

      val purchaserID = "PUR-001"

      val userAnswers = emptyUserAnswers
        .set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(purchaserID, None)).success.value

      when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
        .thenReturn(Right(Html("<div>Test View</div>")))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService))
        .build()

      running(application) {
        val request = FakeRequest(GET, purchaserRemoveRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual "<div>Test View</div>"
      }
    }

    "must redirect to Journey Recovery when service returns Left for a GET" in {

      val mockPurchaserRemoveService = mock[PurchaserRemoveService]

      val purchaserID = "PUR-001"

      val userAnswers = emptyUserAnswers
        .set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(purchaserID, None)).success.value

      when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
        .thenReturn(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService))
        .build()

      running(application) {
        val request = FakeRequest(GET, purchaserRemoveRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must handle form submission and redirect when valid data is submitted" in {

      val mockPurchaserRemoveService = mock[PurchaserRemoveService]

      val purchaserID = "PUR-001"

      val userAnswers = emptyUserAnswers
        .set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(purchaserID, None)).success.value

      when(mockPurchaserRemoveService.handleRemoval(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService))
        .build()

      running(application) {
        val request = FakeRequest(POST, purchaserRemoveRoute)
          .withFormUrlEncodedBody(("value", s"REMOVE-$purchaserID"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must handle SelectNewMain form submission and redirect" in {

      val mockPurchaserRemoveService = mock[PurchaserRemoveService]

      val oldmainPurchaserID = "PUR-001"
      val newmainPurchaserID = "PUR-002"

      val userAnswers = emptyUserAnswers
        .set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(oldmainPurchaserID, None)).success.value

      when(mockPurchaserRemoveService.handleRemoval(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService))
        .build()

      running(application) {
        val request = FakeRequest(POST, purchaserRemoveRoute)
          .withFormUrlEncodedBody(("value", s"PROMOTE-$newmainPurchaserID"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must handle No selection and redirect" in {

      val mockPurchaserRemoveService = mock[PurchaserRemoveService]

      val purchaserID = "PUR-001"

      val userAnswers = emptyUserAnswers
        .set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(purchaserID, None)).success.value

      when(mockPurchaserRemoveService.handleRemoval(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad())))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService))
        .build()

      running(application) {
        val request = FakeRequest(POST, purchaserRemoveRoute)
          .withFormUrlEncodedBody(("value", "no"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
      }
    }

    "must handle Keep selection and redirect" in {

      val mockPurchaserRemoveService = mock[PurchaserRemoveService]

      val purchaserID = "PUR-001"

      val userAnswers = emptyUserAnswers
        .set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(purchaserID, None)).success.value

      when(mockPurchaserRemoveService.handleRemoval(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad())))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService))
        .build()

      running(application) {
        val request = FakeRequest(POST, purchaserRemoveRoute)
          .withFormUrlEncodedBody(("value", "keep"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when empty data is submitted" in {

      val mockPurchaserRemoveService = mock[PurchaserRemoveService]

      val purchaserID = "PUR-001"

      val userAnswers = emptyUserAnswers
        .set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(purchaserID, None)).success.value

      when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
        .thenReturn(Right(Html("<div>Error View</div>")))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService))
        .build()

      running(application) {
        val request = FakeRequest(POST, purchaserRemoveRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual "<div>Error View</div>"
      }
    }

    "must return a Bad Request and errors when invalid value is submitted" in {

      val mockPurchaserRemoveService = mock[PurchaserRemoveService]

      val purchaserID = "PUR-001"

      val userAnswers = emptyUserAnswers
        .set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(purchaserID, None)).success.value

      when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
        .thenReturn(Right(Html("<div>Error View</div>")))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService))
        .build()

      running(application) {
        val request = FakeRequest(POST, purchaserRemoveRoute)
          .withFormUrlEncodedBody(("value", "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual "<div>Error View</div>"
      }
    }

    "must redirect when service returns Left for invalid form submission" in {

      val mockPurchaserRemoveService = mock[PurchaserRemoveService]

      val purchaserID = "PUR-001"

      val userAnswers = emptyUserAnswers
        .set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(purchaserID, None)).success.value

      when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
        .thenReturn(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService))
        .build()

      running(application) {
        val request = FakeRequest(POST, purchaserRemoveRoute)
          .withFormUrlEncodedBody(("value", "invalid"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, purchaserRemoveRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, purchaserRemoveRoute)
          .withFormUrlEncodedBody(("value", "REMOVE-PUR-001"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}