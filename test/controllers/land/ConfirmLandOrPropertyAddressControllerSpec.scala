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

package controllers.land

import base.SpecBase
import controllers.routes
import forms.land.ConfirmLandOrPropertyAddressFormProvider
import models.land.ConfirmLandOrPropertyAddress
import models.{FullReturn, Land, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.land.ConfirmLandOrPropertyAddressPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.land.ConfirmLandOrPropertyAddressView

import scala.concurrent.Future

class ConfirmLandOrPropertyAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val confirmLandOrPropertyAddressRoute = controllers.land.routes.ConfirmLandOrPropertyAddressController.onPageLoad(NormalMode).url

  val formProvider = new ConfirmLandOrPropertyAddressFormProvider()
  val form = formProvider()

  val testAddress1 = "123 Test Street"
  val testAddress2 = Some("Test Town")
  val testPostcode = "AB12 3CD"
  val testStorn = "TESTSTORN"
  val testReturnRef = "REF001"

  private val testLand = Land(
    address1 = Some(testAddress1),
    address2 = testAddress2,
    postcode = Some(testPostcode),
    willSendPlanByPost = None,
    localAuthorityNumber = None,
    interestCreatedTransferred = None
  )

  private val testLandWithWillSendPlanByPost = Land(
    address1 = Some(testAddress1),
    address2 = testAddress2,
    postcode = Some(testPostcode),
    willSendPlanByPost = Some("true"),
    localAuthorityNumber = None,
    interestCreatedTransferred = None
  )

  private val testLandWithLocalAuthorityNumber = Land(
    address1 = Some(testAddress1),
    address2 = testAddress2,
    postcode = Some(testPostcode),
    willSendPlanByPost = None,
    localAuthorityNumber = Some("LA123"),
    interestCreatedTransferred = None
  )

  private val testLandWithInterestCreatedTransferred = Land(
    address1 = Some(testAddress1),
    address2 = testAddress2,
    postcode = Some(testPostcode),
    willSendPlanByPost = None,
    localAuthorityNumber = None,
    interestCreatedTransferred = Some("created")
  )

  private val testLandMissingAddress1 = Land(
    address1 = None,
    address2 = testAddress2,
    postcode = Some(testPostcode),
    willSendPlanByPost = None,
    localAuthorityNumber = None,
    interestCreatedTransferred = None
  )

  private val testLandMissingAddress2 = Land(
    address1 = Some(testAddress1),
    address2 = None,
    postcode = Some(testPostcode),
    willSendPlanByPost = None,
    localAuthorityNumber = None,
    interestCreatedTransferred = None
  )

  private val testLandMissingPostcode = Land(
    address1 = Some(testAddress1),
    address2 = testAddress2,
    postcode = None,
    willSendPlanByPost = None,
    localAuthorityNumber = None,
    interestCreatedTransferred = None
  )

  private val testFullReturn = FullReturn(
    stornId = testStorn,
    returnResourceRef = testReturnRef,
    vendor = None,
    land = Some(Seq(testLand))
  )

  private val testUserAnswers = UserAnswers(
    id = userAnswersId,
    returnId = Some("test-return-id"),
    storn = testStorn,
    fullReturn = Some(testFullReturn)
  )

  "ConfirmLandOrPropertyAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmLandOrPropertyAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, testAddress1, testAddress2.get, testPostcode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = testUserAnswers
        .set(ConfirmLandOrPropertyAddressPage, ConfirmLandOrPropertyAddress.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val view = application.injector.instanceOf[ConfirmLandOrPropertyAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(ConfirmLandOrPropertyAddress.values.head), NormalMode, testAddress1, testAddress2.get, testPostcode)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when address2 is missing" in {

      val fullReturnMissingAddress2 = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq(testLandMissingAddress2))
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturnMissingAddress2))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmLandOrPropertyAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, testAddress1, "", testPostcode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly when address2 is missing and question has previously been answered" in {

      val fullReturnMissingAddress2 = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq(testLandMissingAddress2))
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturnMissingAddress2))
        .set(ConfirmLandOrPropertyAddressPage, ConfirmLandOrPropertyAddress.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val view = application.injector.instanceOf[ConfirmLandOrPropertyAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(ConfirmLandOrPropertyAddress.values.head), NormalMode, testAddress1, "", testPostcode)(request, messages(application)).toString
      }
    }

    "must redirect to LandAddressController when address1 is missing" in {

      val fullReturnMissingAddress1 = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq(testLandMissingAddress1))
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturnMissingAddress1))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandAddressController.redirectToAddressLookupLand().url
      }
    }

    "must redirect to LandAddressController when postcode is missing" in {

      val fullReturnMissingPostcode = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq(testLandMissingPostcode))
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturnMissingPostcode))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandAddressController.redirectToAddressLookupLand().url
      }
    }

    "must redirect to LandAddressController when willSendPlanByPost is present" in {

      val fullReturnWithWillSendPlanByPost = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq(testLandWithWillSendPlanByPost))
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturnWithWillSendPlanByPost))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandAddressController.redirectToAddressLookupLand().url
      }
    }

    "must redirect to LandAddressController when localAuthorityNumber is present" in {

      val fullReturnWithLocalAuthorityNumber = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq(testLandWithLocalAuthorityNumber))
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturnWithLocalAuthorityNumber))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandAddressController.redirectToAddressLookupLand().url
      }
    }

    "must redirect to LandAddressController when interestCreatedTransferred is present" in {

      val fullReturnWithInterestCreatedTransferred = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq(testLandWithInterestCreatedTransferred))
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturnWithInterestCreatedTransferred))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandAddressController.redirectToAddressLookupLand().url
      }
    }

    "must redirect to LandAddressController when there are multiple land entries" in {

      val fullReturnWithMultipleLand = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq(testLand, testLand))
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturnWithMultipleLand))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandAddressController.redirectToAddressLookupLand().url
      }
    }

    "must redirect to LandAddressController when land list is empty" in {

      val emptyFullReturn = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq.empty)
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(emptyFullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandAddressController.redirectToAddressLookupLand().url
      }
    }

    "must redirect to the next page when valid data is submitted with value 'yes'" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmLandOrPropertyAddressRoute)
            .withFormUrlEncodedBody(("value", "yes"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to LandAddressController when valid data is submitted with value 'no'" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmLandOrPropertyAddressRoute)
            .withFormUrlEncodedBody(("value", "no"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandAddressController.redirectToAddressLookupLand().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, confirmLandOrPropertyAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ConfirmLandOrPropertyAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, testAddress1, testAddress2.get, testPostcode)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted and address2 is missing" in {

      val fullReturnMissingAddress2 = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq(testLandMissingAddress2))
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturnMissingAddress2))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, confirmLandOrPropertyAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ConfirmLandOrPropertyAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, testAddress1, "", testPostcode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted with value 'yes' and address2 is missing" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val fullReturnMissingAddress2 = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq(testLandMissingAddress2))
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturnMissingAddress2))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmLandOrPropertyAddressRoute)
            .withFormUrlEncodedBody(("value", "yes"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to LandAddressController when valid data is submitted with value 'no' and address2 is missing" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val fullReturnMissingAddress2 = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq(testLandMissingAddress2))
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(fullReturnMissingAddress2))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmLandOrPropertyAddressRoute)
            .withFormUrlEncodedBody(("value", "no"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandAddressController.redirectToAddressLookupLand().url
      }
    }

    "must return a Bad Request with empty strings when invalid data is submitted and no valid land exists" in {

      val emptyFullReturn = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq.empty)
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(emptyFullReturn))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, confirmLandOrPropertyAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ConfirmLandOrPropertyAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "", "", "")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted with value 'yes' and no valid land exists" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val emptyFullReturn = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq.empty)
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(emptyFullReturn))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmLandOrPropertyAddressRoute)
            .withFormUrlEncodedBody(("value", "yes"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to LandAddressController when valid data is submitted with value 'no' and no valid land exists" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val emptyFullReturn = FullReturn(
        stornId = testStorn,
        returnResourceRef = testReturnRef,
        vendor = None,
        land = Some(Seq.empty)
      )

      val userAnswers = testUserAnswers.copy(fullReturn = Some(emptyFullReturn))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmLandOrPropertyAddressRoute)
            .withFormUrlEncodedBody(("value", "no"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.land.routes.LandAddressController.redirectToAddressLookupLand().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, confirmLandOrPropertyAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, confirmLandOrPropertyAddressRoute)
            .withFormUrlEncodedBody(("value", "yes"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}