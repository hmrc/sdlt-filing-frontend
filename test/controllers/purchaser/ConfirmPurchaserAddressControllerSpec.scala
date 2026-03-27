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

package controllers.purchaser

import base.SpecBase
import controllers.routes
import forms.purchaser.ConfirmPurchaserAddressFormProvider
import models.purchaser.NameOfPurchaser
import models.{FullReturn, Land, NormalMode, Purchaser, ReturnInfo, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.{ConfirmPurchaserAddressPage, NameOfPurchaserPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.land.LandService
import services.purchaser.PurchaserService
import views.html.purchaser.ConfirmPurchaserAddressView

import scala.concurrent.Future

class ConfirmPurchaserAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val confirmPurchaserAddressRoute = controllers.purchaser.routes.ConfirmPurchaserAddressController.onPageLoad(NormalMode).url

  val formProvider = new ConfirmPurchaserAddressFormProvider()
  val form = formProvider()

  val testPurchaserName = NameOfPurchaser(Some("John"), None, "Doe")
  val testFullName = testPurchaserName.fullName

  val testAddress1 = Some("1 Test Street")
  val testAddress2 = Some("Test Town")
  val testAddress3 = Some("Test County")
  val testAddress4 = Some("Test City")
  val testPostcode = Some("AB1 2CD")
  val testPurchaserId = Some("PURCH001")
  val testLandId = Some("LAND001")

  private val testPurchaser = Purchaser(
    purchaserID = testPurchaserId,
    address1 = testAddress1,
    address2 = testAddress2,
    address3 = testAddress3,
    address4 = testAddress4,
    postcode = testPostcode,
    surname = Some("Doe"),
    companyName = None
  )

  private val testPurchaserNoAddress = Purchaser(
    purchaserID = testPurchaserId,
    address1 = None,
    address2 = None,
    address3 = None,
    address4 = None,
    postcode = None,
    surname = Some("Doe"),
    companyName = None
  )

  private val testLand = Land(
    landID = testLandId,
    address1 = testAddress1,
    address2 = testAddress2,
    address3 = testAddress3,
    address4 = testAddress4,
    postcode = testPostcode,
    interestCreatedTransferred = None,
    willSendPlanByPost = None,
    localAuthorityNumber = None
  )

  private val returnInfoWithPurchaser = ReturnInfo(
    mainPurchaserID = testPurchaserId,
    mainLandID = None
  )

  private val returnInfoWithLand = ReturnInfo(
    mainPurchaserID = None,
    mainLandID = testLandId
  )

  private def userAnswersWithReturnInfo(returnInfo: ReturnInfo) =
    emptyUserAnswers
      .set(NameOfPurchaserPage, testPurchaserName).success.value
      .copy(fullReturn = Some(FullReturn(
        stornId = "TESTSTORN",
        returnResourceRef = "REF001",
        returnInfo = Some(returnInfo),
        purchaser = None,
        land = None
      )))

  private def userAnswersWithNoReturnInfo =
    emptyUserAnswers
      .set(NameOfPurchaserPage, testPurchaserName).success.value
      .copy(fullReturn = Some(FullReturn(
        stornId = "TESTSTORN",
        returnResourceRef = "REF001",
        returnInfo = None,
        purchaser = None,
        land = None
      )))

  "ConfirmPurchaserAddress Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view when address is generated" in {

        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(Some(testPurchaser))
        when(mockLandService.getMainLand(any())).thenReturn(None)

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithPurchaser)
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmPurchaserAddressRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[ConfirmPurchaserAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, testFullName, testAddress1, testAddress2, testAddress3, testAddress4, testPostcode)(request, messages(application)).toString
        }
      }

      "must return OK with land address when purchaser has surname but no address" in {

        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        val purchaserWithSurnameNoAddress = testPurchaserNoAddress.copy(surname = Some("Doe"), companyName = None)
        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(Some(purchaserWithSurnameNoAddress))
        when(mockLandService.getMainLand(any())).thenReturn(Some(testLand))

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithPurchaser)
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmPurchaserAddressRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[ConfirmPurchaserAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, testFullName, testAddress1, testAddress2, testAddress3, testAddress4, testPostcode)(request, messages(application)).toString
        }
      }

      "must return OK with land address when purchaser has companyName but no address" in {

        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        val purchaserWithCompanyNoAddress = testPurchaserNoAddress.copy(surname = None, companyName = Some("Test Co"))
        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(Some(purchaserWithCompanyNoAddress))
        when(mockLandService.getMainLand(any())).thenReturn(Some(testLand))

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithPurchaser)
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmPurchaserAddressRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[ConfirmPurchaserAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, testFullName, testAddress1, testAddress2, testAddress3, testAddress4, testPostcode)(request, messages(application)).toString
        }
      }

      "must return OK with land address when no purchaser but land has address" in {

        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(None)
        when(mockLandService.getMainLand(any())).thenReturn(Some(testLand))

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithLand)
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmPurchaserAddressRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[ConfirmPurchaserAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, testFullName, testAddress1, testAddress2, testAddress3, testAddress4, testPostcode)(request, messages(application)).toString
        }
      }

      "must redirect to PurchaserAddressController when no address is generated" in {

        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(None)
        when(mockLandService.getMainLand(any())).thenReturn(None)

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithPurchaser)
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmPurchaserAddressRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser().url
        }
      }

      "must redirect to JourneyRecovery when returnInfo is not present" in {

        val userAnswers = userAnswersWithNoReturnInfo
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, confirmPurchaserAddressRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to NameOfPurchaserController when purchaser name is not present" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, confirmPurchaserAddressRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(Some(testPurchaser))
        when(mockLandService.getMainLand(any())).thenReturn(None)

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithPurchaser)
          .set(ConfirmPurchaserAddressPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmPurchaserAddressRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[ConfirmPurchaserAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), NormalMode, testFullName, testAddress1, testAddress2, testAddress3, testAddress4, testPostcode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, confirmPurchaserAddressRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to next page when yes is submitted with purchaser address" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(Some(testPurchaser))
        when(mockLandService.getMainLand(any())).thenReturn(None)

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithPurchaser)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmPurchaserAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must redirect to PurchaserAddressController when no is submitted" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(Some(testPurchaser))
        when(mockLandService.getMainLand(any())).thenReturn(None)

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithPurchaser)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmPurchaserAddressRoute)
            .withFormUrlEncodedBody(("value", "false"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser().url
        }
      }

      "must redirect to next page when yes is submitted with land address" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(None)
        when(mockLandService.getMainLand(any())).thenReturn(Some(testLand))

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithLand)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmPurchaserAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must redirect to next page when yes submitted with surname no address and land has address" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val purchaserWithSurnameNoAddress = testPurchaserNoAddress.copy(surname = Some("Doe"), companyName = None)
        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(Some(purchaserWithSurnameNoAddress))
        when(mockLandService.getMainLand(any())).thenReturn(Some(testLand))

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithPurchaser)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmPurchaserAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must redirect to next page when yes submitted with companyName no address and land has address" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val purchaserWithCompanyNoAddress = testPurchaserNoAddress.copy(surname = None, companyName = Some("Test Co"))
        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(Some(purchaserWithCompanyNoAddress))
        when(mockLandService.getMainLand(any())).thenReturn(Some(testLand))

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithPurchaser)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmPurchaserAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return BadRequest when invalid data is submitted" in {

        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(Some(testPurchaser))
        when(mockLandService.getMainLand(any())).thenReturn(None)

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithPurchaser)
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmPurchaserAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid"))
          val boundForm = form.bind(Map("value" -> "invalid"))
          val view = application.injector.instanceOf[ConfirmPurchaserAddressView]
          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, testFullName, testAddress1, testAddress2, testAddress3, testAddress4, testPostcode)(request, messages(application)).toString
        }
      }

      "must redirect to PurchaserAddressController when no address is generated on submit" in {

        val mockPurchaserService = mock[PurchaserService]
        val mockLandService = mock[LandService]

        when(mockPurchaserService.getMainPurchaser(any())).thenReturn(None)
        when(mockLandService.getMainLand(any())).thenReturn(None)

        val userAnswers = userAnswersWithReturnInfo(returnInfoWithPurchaser)
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[PurchaserService].toInstance(mockPurchaserService),
            bind[LandService].toInstance(mockLandService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, confirmPurchaserAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser().url
        }
      }

      "must redirect to JourneyRecovery when returnInfo is not present on submit" in {

        val userAnswers = userAnswersWithNoReturnInfo
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, confirmPurchaserAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to NameOfPurchaserController when purchaser name is not present on submit" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, confirmPurchaserAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, confirmPurchaserAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}