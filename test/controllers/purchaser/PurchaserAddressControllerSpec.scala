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
import models.{NormalMode, UserAnswers}
import models.address.{Address, Country}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.PurchaserAddressPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.AddressLookupService

import java.time.Instant
import scala.concurrent.Future

class PurchaserAddressControllerSpec extends SpecBase with MockitoSugar {

  val testAddress: Address = Address(
    "123 Test Street",
    Some("Test Area"),
    None,
    None,
    None,
    Some("TE1 1ST"),
    Some(Country(Some("GB"), Some("United Kingdom"))),
    false
  )

  val testAddressLookupCall: Call = Call("GET", "http://localhost:9028/lookup-address/journey")

  lazy val redirectToAddressLookupRoute = controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser().url
  lazy val redirectToAddressLookupChangeRoute = controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser(changeRoute = Some("change")).url
  lazy val addressLookupCallbackRoute = controllers.purchaser.routes.PurchaserAddressController.addressLookupCallbackPurchaser(id = "test-id").url
  lazy val addressLookupCallbackChangeRoute = controllers.purchaser.routes.PurchaserAddressController.addressLookupCallbackChangePurchaser("test-id").url

  val testUserAnswersIndividual = UserAnswers(
    id = "test-session-id",
    storn = "test-storn-123",
    returnId = Some("test-return-id"),
    fullReturn = None,
    data = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "nameOfPurchaser" -> Json.obj(
          "forename1" -> "John",
          "forename2" -> "Middle",
          "name" -> "Doe"
        )
      )
    ),
    lastUpdated = Instant.now
  )

  val testUserAnswersCompany = UserAnswers(
    id = "test-session-id",
    storn = "test-storn-123",
    returnId = Some("test-return-id"),
    fullReturn = None,
    data = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "nameOfPurchaser" -> Json.obj(
          "name" -> "ACME Corporation"
        )
      )
    ),
    lastUpdated = Instant.now
  )

  "PurchaserAddressController" - {

    "redirectToAddressLookupPurchaser" - {

      "must redirect to address lookup service when no changeRoute is provided" in {
        val mockAddressLookupService = mock[AddressLookupService]
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(testUserAnswersIndividual)))

        when(mockAddressLookupService.getJourneyUrl(any(), any(), any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(testAddressLookupCall))

        val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, redirectToAddressLookupRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual testAddressLookupCall.url

          verify(mockAddressLookupService, times(1))
            .getJourneyUrl(any(), eqTo(controllers.purchaser.routes.PurchaserAddressController.addressLookupCallbackPurchaser()), eqTo(true), any(), any())(any(), any(), any())
        }
      }

      "must redirect to address lookup service with change callback when changeRoute is provided" in {
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getJourneyUrl(any(), any(), any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(testAddressLookupCall))
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(testUserAnswersIndividual)))

        val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, redirectToAddressLookupChangeRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual testAddressLookupCall.url

          verify(mockAddressLookupService, times(1))
            .getJourneyUrl(any(), eqTo(controllers.purchaser.routes.PurchaserAddressController.addressLookupCallbackChangePurchaser()), eqTo(true), any(), any())(any(), any(), any())
        }
      }

      "must handle individual purchaser with full name" in {
        val mockAddressLookupService = mock[AddressLookupService]
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(testUserAnswersIndividual)))

        when(mockAddressLookupService.getJourneyUrl(any(), any(), any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(testAddressLookupCall))

        val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, redirectToAddressLookupRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual testAddressLookupCall.url
        }
      }

      "must handle company purchaser with company name" in {
        val mockAddressLookupService = mock[AddressLookupService]
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(testUserAnswersCompany)))

        when(mockAddressLookupService.getJourneyUrl(any(), any(), any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(testAddressLookupCall))

        val application = applicationBuilder(userAnswers = Some(testUserAnswersCompany))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, redirectToAddressLookupRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual testAddressLookupCall.url
        }
      }

      "must redirect to Journey Recovery when no purchaser name is found" in {
        val mockAddressLookupService = mock[AddressLookupService]
        val mockSessionRepository = mock[SessionRepository]

        val userAnswersWithoutName = UserAnswers(
          id = "test-session-id",
          storn = "test-storn-123",
          data = Json.obj(
            "purchaserCurrent" -> Json.obj(
              "nameOfPurchaser" -> Json.obj()
            )
          )
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswersWithoutName)))

        val application = applicationBuilder(userAnswers = Some(userAnswersWithoutName))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, redirectToAddressLookupRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when session repository returns None" in {
        val mockAddressLookupService = mock[AddressLookupService]
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, redirectToAddressLookupRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {
        val mockAddressLookupService = mock[AddressLookupService]

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, redirectToAddressLookupRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must handle service failure when getting journey URL" in {
        val mockAddressLookupService = mock[AddressLookupService]
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(testUserAnswersIndividual)))

        when(mockAddressLookupService.getJourneyUrl(any(), any(), any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Service unavailable")))

        val application = applicationBuilder(userAnswers = Some(testUserAnswersIndividual))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, redirectToAddressLookupRoute)

          val result = route(application, request).value

          whenReady(result.failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustBe "Service unavailable"
          }
        }
      }
    }

    "addressLookupCallbackPurchaser" - {

      "must redirect to VendorRepresentedByAgent when address is successfully saved" in {
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(eqTo("test-id"))(any()))
          .thenReturn(Future.successful(testAddress))
        when(mockAddressLookupService.saveAddressDetails(eqTo(testAddress), eqTo(PurchaserAddressPage))(any(), any()))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, addressLookupCallbackRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(NormalMode).url

          verify(mockAddressLookupService, times(1)).getAddressById(eqTo("test-id"))(any())
          verify(mockAddressLookupService, times(1)).saveAddressDetails(eqTo(testAddress), eqTo(PurchaserAddressPage))(any(), any())
        }
      }

      "must redirect to Journey Recovery when address save fails" in {
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(eqTo("test-id"))(any()))
          .thenReturn(Future.successful(testAddress))
        when(mockAddressLookupService.saveAddressDetails(eqTo(testAddress), eqTo(PurchaserAddressPage))(any(), any()))
          .thenReturn(Future.successful(false))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, addressLookupCallbackRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must handle failure when getting address by ID" in {
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(eqTo("test-id"))(any()))
          .thenReturn(Future.failed(new RuntimeException("Address not found")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, addressLookupCallbackRoute)

          val result = route(application, request).value

          whenReady(result.failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustBe "Address not found"
          }
        }
      }

      "must handle failure when saving address details" in {
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(eqTo("test-id"))(any()))
          .thenReturn(Future.successful(testAddress))
        when(mockAddressLookupService.saveAddressDetails(eqTo(testAddress), eqTo(PurchaserAddressPage))(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Database error")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, addressLookupCallbackRoute)

          val result = route(application, request).value

          whenReady(result.failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustBe "Database error"
          }
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {
        val mockAddressLookupService = mock[AddressLookupService]

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, addressLookupCallbackRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must handle different address IDs" in {
        val addressIds = List("id-123", "ABC-XYZ", "test-address-id")

        addressIds.foreach { addressId =>
          val mockAddressLookupService = mock[AddressLookupService]

          when(mockAddressLookupService.getAddressById(eqTo(addressId))(any()))
            .thenReturn(Future.successful(testAddress))
          when(mockAddressLookupService.saveAddressDetails(eqTo(testAddress), eqTo(PurchaserAddressPage))(any(), any()))
            .thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AddressLookupService].toInstance(mockAddressLookupService)
            )
            .build()

          running(application) {
            val callbackRoute = controllers.purchaser.routes.PurchaserAddressController.addressLookupCallbackPurchaser(addressId).url
            val request = FakeRequest(GET, callbackRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(NormalMode).url

            verify(mockAddressLookupService, times(1)).getAddressById(eqTo(addressId))(any())
          }
        }
      }
    }

    "addressLookupCallbackChangePurchaser" - {

      "must redirect to Return Task List when address is successfully saved" in {
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(eqTo("test-id"))(any()))
          .thenReturn(Future.successful(testAddress))
        when(mockAddressLookupService.saveAddressDetails(eqTo(testAddress), eqTo(PurchaserAddressPage))(any(), any()))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, addressLookupCallbackChangeRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url

          verify(mockAddressLookupService, times(1)).getAddressById(eqTo("test-id"))(any())
          verify(mockAddressLookupService, times(1)).saveAddressDetails(eqTo(testAddress), eqTo(PurchaserAddressPage))(any(), any())
        }
      }

      "must redirect to Journey Recovery when address save fails" in {
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(eqTo("test-id"))(any()))
          .thenReturn(Future.successful(testAddress))
        when(mockAddressLookupService.saveAddressDetails(eqTo(testAddress), eqTo(PurchaserAddressPage))(any(), any()))
          .thenReturn(Future.successful(false))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, addressLookupCallbackChangeRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must handle failure when getting address by ID" in {
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(eqTo("test-id"))(any()))
          .thenReturn(Future.failed(new RuntimeException("Address not found")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, addressLookupCallbackChangeRoute)

          val result = route(application, request).value

          whenReady(result.failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustBe "Address not found"
          }
        }
      }

      "must handle failure when saving address details" in {
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(eqTo("test-id"))(any()))
          .thenReturn(Future.successful(testAddress))
        when(mockAddressLookupService.saveAddressDetails(eqTo(testAddress), eqTo(PurchaserAddressPage))(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Database error")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, addressLookupCallbackChangeRoute)

          val result = route(application, request).value

          whenReady(result.failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustBe "Database error"
          }
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {
        val mockAddressLookupService = mock[AddressLookupService]

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, addressLookupCallbackChangeRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must handle different address IDs" in {
        val addressIds = List("id-123", "ABC-XYZ", "test-address-id")

        addressIds.foreach { addressId =>
          val mockAddressLookupService = mock[AddressLookupService]

          when(mockAddressLookupService.getAddressById(eqTo(addressId))(any()))
            .thenReturn(Future.successful(testAddress))
          when(mockAddressLookupService.saveAddressDetails(eqTo(testAddress), eqTo(PurchaserAddressPage))(any(), any()))
            .thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AddressLookupService].toInstance(mockAddressLookupService)
            )
            .build()

          running(application) {
            val callbackChangeRoute = controllers.purchaser.routes.PurchaserAddressController.addressLookupCallbackChangePurchaser(addressId).url
            val request = FakeRequest(GET, callbackChangeRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url

            verify(mockAddressLookupService, times(1)).getAddressById(eqTo(addressId))(any())
          }
        }
      }
    }
  }
}