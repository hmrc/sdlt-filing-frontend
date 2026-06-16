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
import models.Purchaser
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
import services.purchaser.{PurchaserRemoveService, PurchaserService}

import scala.concurrent.Future

class PurchaserRemoveControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new PurchaserRemoveFormProvider()

  lazy val purchaserRemoveRoute = controllers.purchaser.routes.PurchaserRemoveController.onPageLoad().url

  val purchaserID = "PUR-001"
  val purchaserName = "John Doe"

  val individualPurchaser = Purchaser(
    purchaserID = Some(purchaserID),
    forename1 = Some("John"),
    forename2 = None,
    surname = Some("Doe"),
    companyName = None,
    address1 = None,
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = None
  )

  val companyPurchaser = Purchaser(
    purchaserID = Some(purchaserID),
    forename1 = None,
    forename2 = None,
    surname = None,
    companyName = Some("ACME Corporation"),
    address1 = None,
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = None
  )

  val baseUserAnswers = emptyUserAnswers
    .set(PurchaserOverviewRemovePage, PurchaserAndCompanyId(purchaserID, None)).success.value

  "PurchaserRemove Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view when purchaser is found" in {
        val mockPurchaserRemoveService = mock[PurchaserRemoveService]
        val mockPurchaserService = mock[PurchaserService]

        when(mockPurchaserService.allPurchasers(any())).thenReturn(Seq(individualPurchaser))
        when(mockPurchaserService.findById(any(), any())).thenReturn(Some(individualPurchaser))
        when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
          .thenReturn(Right(Html("<div>Test View</div>")))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserRemoveRoute)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual "<div>Test View</div>"
        }
      }

      "must return OK with company purchaser name when found" in {
        val mockPurchaserRemoveService = mock[PurchaserRemoveService]
        val mockPurchaserService = mock[PurchaserService]

        when(mockPurchaserService.allPurchasers(any())).thenReturn(Seq(companyPurchaser))
        when(mockPurchaserService.findById(any(), any())).thenReturn(Some(companyPurchaser))
        when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
          .thenReturn(Right(Html("<div>Company View</div>")))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserRemoveRoute)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual "<div>Company View</div>"
        }
      }

      "must redirect to Journey Recovery when service returns Left" in {
        val mockPurchaserRemoveService = mock[PurchaserRemoveService]
        val mockPurchaserService = mock[PurchaserService]

        when(mockPurchaserService.allPurchasers(any())).thenReturn(Seq(individualPurchaser))
        when(mockPurchaserService.findById(any(), any())).thenReturn(Some(individualPurchaser))
        when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
          .thenReturn(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserRemoveRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must handle when purchaser is not found" in {
        val mockPurchaserRemoveService = mock[PurchaserRemoveService]
        val mockPurchaserService = mock[PurchaserService]

        when(mockPurchaserService.allPurchasers(any())).thenReturn(Seq())
        when(mockPurchaserService.findById(any(), any())).thenReturn(None)
        when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
          .thenReturn(Right(Html("<div>Empty Name View</div>")))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserRemoveRoute)
          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must redirect to Journey Recovery when PurchaserOverviewRemovePage is missing" in {
        val mockPurchaserRemoveService = mock[PurchaserRemoveService]

        when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
          .thenReturn(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService))
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserRemoveRoute)
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
    }

    "onSubmit" - {

      "must handle form submission and redirect when valid data is submitted" in {
        val mockPurchaserRemoveService = mock[PurchaserRemoveService]
        val mockPurchaserService = mock[PurchaserService]

        when(mockPurchaserService.allPurchasers(any())).thenReturn(Seq(individualPurchaser))
        when(mockPurchaserService.findById(any(), any())).thenReturn(Some(individualPurchaser))
        when(mockPurchaserRemoveService.handleRemoval(any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
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
        val mockPurchaserService = mock[PurchaserService]
        val newmainPurchaserID = "PUR-002"

        when(mockPurchaserService.allPurchasers(any())).thenReturn(Seq(individualPurchaser))
        when(mockPurchaserService.findById(any(), any())).thenReturn(Some(individualPurchaser))
        when(mockPurchaserRemoveService.handleRemoval(any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
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
        val mockPurchaserService = mock[PurchaserService]

        when(mockPurchaserService.allPurchasers(any())).thenReturn(Seq(individualPurchaser))
        when(mockPurchaserService.findById(any(), any())).thenReturn(Some(individualPurchaser))
        when(mockPurchaserRemoveService.handleRemoval(any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad())))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
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
        val mockPurchaserService = mock[PurchaserService]

        when(mockPurchaserService.allPurchasers(any())).thenReturn(Seq(individualPurchaser))
        when(mockPurchaserService.findById(any(), any())).thenReturn(Some(individualPurchaser))
        when(mockPurchaserRemoveService.handleRemoval(any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad())))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
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
        val mockPurchaserService = mock[PurchaserService]

        when(mockPurchaserService.allPurchasers(any())).thenReturn(Seq(individualPurchaser))
        when(mockPurchaserService.findById(any(), any())).thenReturn(Some(individualPurchaser))
        when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
          .thenReturn(Right(Html("<div>Error View</div>")))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
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
        val mockPurchaserService = mock[PurchaserService]

        when(mockPurchaserService.allPurchasers(any())).thenReturn(Seq(individualPurchaser))
        when(mockPurchaserService.findById(any(), any())).thenReturn(Some(individualPurchaser))
        when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
          .thenReturn(Right(Html("<div>Error View</div>")))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
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
        val mockPurchaserService = mock[PurchaserService]

        when(mockPurchaserService.allPurchasers(any())).thenReturn(Seq(individualPurchaser))
        when(mockPurchaserService.findById(any(), any())).thenReturn(Some(individualPurchaser))
        when(mockPurchaserRemoveService.purchaserRemoveView(any(), any())(any(), any()))
          .thenReturn(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[PurchaserRemoveService].toInstance(mockPurchaserRemoveService),
            bind[PurchaserService].toInstance(mockPurchaserService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, purchaserRemoveRoute)
            .withFormUrlEncodedBody(("value", "invalid"))

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
}