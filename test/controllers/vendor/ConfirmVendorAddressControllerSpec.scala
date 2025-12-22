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
import constants.FullReturnConstants.{completeFullReturn, completeFullReturnMultipleVendors, completeVendor, minimalFullReturn}
import controllers.routes
import forms.vendor.ConfirmVendorAddressFormProvider
import models.vendor.{ConfirmVendorAddress, VendorName}
import models.{NormalMode, UserAnswers, Vendor}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.vendor.{ConfirmVendorAddressPage, VendorOrCompanyNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.vendor.ConfirmVendorAddressView

import java.time.Instant
import scala.concurrent.Future

class ConfirmVendorAddressControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val confirmVendorAddressRoute: String =
    controllers.vendor.routes.ConfirmVendorAddressController.onPageLoad(NormalMode).url

  private val formProvider = new ConfirmVendorAddressFormProvider()
  private val form = formProvider()

  private val testReturnId = "123456"
  private val testStorn = "TESTSTORN"

  private def uaWithVendorName(
                                vn: VendorName,
                                extraAnswers: UserAnswers => UserAnswers = identity
                              ): UserAnswers = {
    val base = emptyUserAnswers.copy(storn = testStorn, returnId = Some(testReturnId))
    extraAnswers(base).set(VendorOrCompanyNamePage, vn).get
  }

  private val expectedName = "Jane Elizabeth Johnson"
  private val testVendorName = VendorName(
    forename1 = Some("Jane"),
    forename2 = Some("Elizabeth"),
    name = "Johnson"
  )

  "ConfirmVendorAddressController.onPageLoad" - {

    "when there are no complete vendors" - {
      "must redirect to AddressLookup for current vendor" in {
        val minimalFullReturnWithNoVendors = minimalFullReturn.copy(vendor = None)

        val userAnswersWithName = emptyUserAnswers
          .copy(
            storn = testStorn,
            returnId = Some(testReturnId),
            fullReturn = Some(minimalFullReturnWithNoVendors)
          )
          .set(VendorOrCompanyNamePage, VendorName(None, None, "Acme Ltd")).get

        val application = applicationBuilder(userAnswers = Some(userAnswersWithName))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmVendorAddressRoute)
          val result = route(application, request).get

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.vendor.routes.VendorAddressController.redirectToAddressLookupVendor().url
        }
      }
    }

    "when there are complete vendors" - {

      "must return OK and the correct view for a GET" in {
        val userAnswers = uaWithVendorName(testVendorName)
          .copy(fullReturn = Some(completeFullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmVendorAddressRoute)
          val result = route(application, request).get

          val view = application.injector.instanceOf[ConfirmVendorAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(
              form,
              expectedName,
              completeVendor.address1,
              completeVendor.address2,
              completeVendor.address3,
              None,
              completeVendor.postcode,
              NormalMode
            )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET where there are multiple vendors" in {
        val userAnswers = uaWithVendorName(testVendorName)
          .copy(fullReturn = Some(completeFullReturnMultipleVendors))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmVendorAddressRoute)
          val result = route(application, request).get

          val view = application.injector.instanceOf[ConfirmVendorAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(
              form,
              expectedName,
              completeVendor.address1,
              completeVendor.address2,
              completeVendor.address3,
              None,
              completeVendor.postcode,
              NormalMode
            )(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        val userAnswers = uaWithVendorName(testVendorName, _.set(ConfirmVendorAddressPage, ConfirmVendorAddress.Yes).get)
          .copy(fullReturn = Some(completeFullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmVendorAddressRoute)
          val result = route(application, request).get

          val view = application.injector.instanceOf[ConfirmVendorAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(
              form.fill(ConfirmVendorAddress.Yes),
              expectedName,
              completeVendor.address1,
              completeVendor.address2,
              completeVendor.address3,
              None,
              completeVendor.postcode,
              NormalMode
            )(request, messages(application)).toString
        }
      }
    }

    "must redirect to vendor name page when no VendorOrCompanyNamePage is present" in {
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(completeFullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, confirmVendorAddressRoute)
        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to Journey Recovery when no fullReturn is present" in {
      val userAnswers = uaWithVendorName(testVendorName)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, confirmVendorAddressRoute)
        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "ConfirmVendorAddressController.onSubmit" - {

    "when there are complete vendors" - {

      "must redirect to the next page when user selects Yes and Address line 1 is present" in {
        val userAnswers = uaWithVendorName(testVendorName)
          .copy(fullReturn = Some(completeFullReturn))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmVendorAddressRoute)
            .withFormUrlEncodedBody("value" -> ConfirmVendorAddress.Yes.toString)

          val result = route(application, request).get

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must redirect to vendor name page when no VendorOrCompanyNamePage is present for a POST" in {
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(completeFullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmVendorAddressRoute)
            .withFormUrlEncodedBody("value" -> ConfirmVendorAddress.Yes.toString)
          val result = route(application, request).get

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to address lookup when user selects Yes and Address line 1 is not present" in {

        val noAddressLine1Vendor: Vendor = Vendor(
          vendorID = Some("VEN001"),
          returnID = Some("RET123456789"),
          title = Some("Mrs"),
          forename1 = Some("Jane"),
          forename2 = Some("Elizabeth"),
          name = Some("Johnson"),
          houseNumber = Some("15"),
          address2 = Some("Mayfair"),
          address3 = Some("London"),
          address4 = None,
          postcode = Some("W1K 1LB"),
          isRepresentedByAgent = Some("true"),
          vendorResourceRef = Some("VEN-REF-001"),
          nextVendorID = None
        )

        val userAnswers = uaWithVendorName(testVendorName)
          .copy(fullReturn = Some(completeFullReturn.copy(vendor = Some(Seq(noAddressLine1Vendor)))))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmVendorAddressRoute)
            .withFormUrlEncodedBody("value" -> ConfirmVendorAddress.Yes.toString)

          val result = route(application, request).get

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorAddressController.redirectToAddressLookupVendor().url
        }
      }

      "must redirect to Address Lookup when user selects No" in {
        val userAnswers = uaWithVendorName(testVendorName)
          .copy(fullReturn = Some(completeFullReturn))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmVendorAddressRoute)
            .withFormUrlEncodedBody("value" -> ConfirmVendorAddress.No.toString)

          val result = route(application, request).get

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.vendor.routes.VendorAddressController.redirectToAddressLookupVendor().url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val userAnswers = uaWithVendorName(testVendorName)
          .copy(fullReturn = Some(completeFullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmVendorAddressRoute)
            .withFormUrlEncodedBody("value" -> "invalid value")

          val boundForm = form.bind(Map("value" -> "invalid value"))
          val view = application.injector.instanceOf[ConfirmVendorAddressView]

          val result = route(application, request).get

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual
            view(
              boundForm,
              expectedName,
              completeVendor.address1,
              completeVendor.address2,
              completeVendor.address3,
              completeVendor.address4,
              completeVendor.postcode,
              NormalMode
            )(request, messages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery when fullReturn is missing for a POST" in {

      val userAnswers = emptyUserAnswers
        .set(VendorOrCompanyNamePage, VendorName(Some("John"), None, "Doe")).get
        .copy(fullReturn = None)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(POST, confirmVendorAddressRoute)
          .withFormUrlEncodedBody("value" -> ConfirmVendorAddress.Yes.toString)

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, confirmVendorAddressRoute)
        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, confirmVendorAddressRoute)
          .withFormUrlEncodedBody(("value", ConfirmVendorAddress.values.head.toString))

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}