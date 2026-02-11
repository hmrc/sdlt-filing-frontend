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

package services.vendor

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants
import models.*
import models.vendor.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsNull, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class VendorCreateOrUpdateServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: FakeRequest[_] = FakeRequest()

  val testReturnId = "test-return-id"
  val testStorn = "test-storn"
  val testVendorId = "VE002"
  val testVendorResourceRef = "vendor-ref-123"
  val testNextVendorId = Some("next-vendor-456")

  val userAnswers = UserAnswers(
    id = "12345",
    storn = "TESTSTORN",
    returnId = Some("12313"),
    fullReturn = None,
    data = Json.obj(
      "vendorCurrent" -> Json.obj(
        "vendorID" -> "VEN001",
        "whoIsTheVendor" -> "Individual",
        "vendorOrCompanyName" -> Json.obj(
          "forename1" -> "Jane",
          "forename2" -> "Elizabeth",
          "name" -> "Johnson",
        ),
        "vendorAddress" -> Json.obj(
          "houseNumber" -> "15",
          "line1" -> "Park Lane",
          "line2" -> "Mayfair",
          "line3" -> "London",
          "line4" -> JsNull,
          "line5" -> JsNull,
          "postcode" -> "W1K 1LB",
          "country" -> Json.obj(
            "code" -> "GB",
            "name" -> "UK"
          ),
          "addressValidated" -> true
        ),
        "representedByAnAgent" -> "true"
      )),
    lastUpdated = Instant.now
  )

  private def createUserAnswers(
                                 returnId: Option[String] = Some(testReturnId),
                                 storn: String = testStorn,
                                 fullReturn: Option[FullReturn] = None
                               ): UserAnswers = {
    userAnswers.copy(
      returnId = returnId,
      storn = storn,
      fullReturn = fullReturn
    )
  }

  private def createVendor(
                            vendorID: Option[String] = Some(testVendorId),
                            vendorResourceRef: Option[String] = Some(testVendorResourceRef),
                            nextVendorID: Option[String] = testNextVendorId
                          ): Vendor = {
    Vendor(
      vendorID = vendorID,
      forename1 = Some("John"),
      forename2 = Some("Peter"),
      name = Some("Doe"),
      address1 = Some("123 Street"),
      vendorResourceRef = vendorResourceRef,
      nextVendorID = nextVendorID
    )
  }

  private def createSessionData(vendorId: Option[String] = None): VendorSessionQuestions = {
    VendorSessionQuestions(VendorCurrent(
      vendorID = vendorId,
      whoIsTheVendor = "Company",
      vendorOrCompanyName = VendorName(None, None, "Samsung"),
      vendorAddress = VendorSessionAddress(
        houseNumber = Some("1"),
        line1 = Some("Street 1"),
        line2 = Some("Street 2"),
        line3 = Some("Street 3"),
        line4 = Some("Street 4"),
        line5 = Some("Street 5"),
        postcode = Some("CR7 8LU"),
        country = Some(VendorSessionCountry(
          code = Some("GB"),
          name = Some("UK")
        )),
        addressValidated = Some(true)
      )
    ))
  }

  private def createFullReturn(
                                vendors: Seq[Vendor] = Seq.empty,
                                purchasers: Seq[models.Purchaser] = Seq.empty
                              ): FullReturn = {
    FullReturnConstants.completeFullReturn.copy(
      vendor = Some(vendors),
      purchaser = Some(purchasers)
    )
  }

  "VendorCreateOrUpdateService" - {

    "result" - {

      "when updating an existing vendor" - {

        "must successfully update vendor when version update succeeds and returns new version" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val vendor = createVendor()
          val fullReturn = createFullReturn(vendors = Seq(vendor))
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = Some(testVendorId))

          val mockUpdateRequest = mock[UpdateVendorRequest]
          val returnVersionResponse = ReturnVersionUpdateReturn(
            newVersion = Some(2)
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))
          when(mockVendorRequestService.convertToUpdateVendorRequest(
            any(), any(), any(), any(), any()
          )).thenReturn(mockUpdateRequest)
          when(mockBackendConnector.updateVendor(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.vendor.routes.VendorOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector, times(1)).updateVendor(eqTo(mockUpdateRequest))(any(), any())
        }

        "must fail when version update does not return a new version" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val vendor = createVendor()
          val fullReturn = createFullReturn(vendors = Seq(vendor))
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = Some(testVendorId))

          val returnVersionResponse = ReturnVersionUpdateReturn(
            newVersion = None
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))

          whenReady(service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).failed) { exception =>
            exception mustBe an[IllegalStateException]
            exception.getMessage mustBe "Return version update did not produce a new version"
          }
        }

        "must fail when vendor is not found in full return" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val fullReturn = createFullReturn(vendors = Seq.empty)
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = Some(testVendorId))

          val returnVersionResponse = ReturnVersionUpdateReturn(
            newVersion = Some(2)
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))

          whenReady(service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).failed) { exception =>
            exception mustBe an[IllegalStateException]
            exception.getMessage mustBe "Vendor not found in full return"
          }
        }

        "must fail when vendor resource ref is not found" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val vendorWithoutRef = createVendor(vendorResourceRef = None)
          val fullReturn = createFullReturn(vendors = Seq(vendorWithoutRef))
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = Some(testVendorId))

          val returnVersionResponse = ReturnVersionUpdateReturn(
            newVersion = Some(2))

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))

          whenReady(service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).failed) { exception =>
            exception mustBe an[IllegalStateException]
            exception.getMessage mustBe "Vendor not found in full return"
          }
        }
        
        "must call updateReturnVersion exactly once" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val vendor = createVendor()
          val fullReturn = createFullReturn(vendors = Seq(vendor))
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = Some(testVendorId))

          val mockUpdateRequest = mock[UpdateVendorRequest]
          val returnVersionResponse = ReturnVersionUpdateReturn(
            newVersion = Some(2)
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))
          when(mockVendorRequestService.convertToUpdateVendorRequest(any(), any(), any(), any(), any()))
            .thenReturn(mockUpdateRequest)
          when(mockBackendConnector.updateVendor(any())(any(), any()))
            .thenReturn(Future.successful(()))

          service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).futureValue

          verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector, times(1)).updateVendor(any())(any(), any())
        }

        "must handle nextVendorID being None" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val vendorWithoutNextId = createVendor(nextVendorID = None)
          val fullReturn = createFullReturn(vendors = Seq(vendorWithoutNextId))
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = Some(testVendorId))

          val mockUpdateRequest = mock[UpdateVendorRequest]
          val returnVersionResponse = ReturnVersionUpdateReturn(
            newVersion = Some(2)
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))
          when(mockVendorRequestService.convertToUpdateVendorRequest(
            any(),
            eqTo(testStorn),
            eqTo(testReturnId),
            eqTo(testVendorResourceRef),
            eqTo(None)
          )).thenReturn(mockUpdateRequest)
          when(mockBackendConnector.updateVendor(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.vendor.routes.VendorOverviewController.onPageLoad().url
          verify(mockVendorRequestService, times(1)).convertToUpdateVendorRequest(
            any(),
            eqTo(testStorn),
            eqTo(testReturnId),
            eqTo(testVendorResourceRef),
            eqTo(None)
          )
        }

        "must propagate backend connector updateReturnVersion failures" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val vendor = createVendor()
          val fullReturn = createFullReturn(vendors = Seq(vendor))
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = Some(testVendorId))

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.failed(new RuntimeException("Backend failure")))

          whenReady(service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustBe "Backend failure"
          }
        }

        "must propagate backend connector updateVendor failures" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val vendor = createVendor()
          val fullReturn = createFullReturn(vendors = Seq(vendor))
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = Some(testVendorId))

          val mockUpdateRequest = mock[UpdateVendorRequest]
          val returnVersionResponse = ReturnVersionUpdateReturn(
            newVersion = Some(2)
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))
          when(mockVendorRequestService.convertToUpdateVendorRequest(any(), any(), any(), any(), any()))
            .thenReturn(mockUpdateRequest)
          when(mockBackendConnector.updateVendor(any())(any(), any()))
            .thenReturn(Future.failed(new RuntimeException("Update vendor failed")))

          whenReady(service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustBe "Update vendor failed"
          }
        }

        "must pass HeaderCarrier to connector" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()
          val testHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

          val vendor = createVendor()
          val fullReturn = createFullReturn(vendors = Seq(vendor))
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = Some(testVendorId))

          val mockUpdateRequest = mock[UpdateVendorRequest]
          val returnVersionResponse = ReturnVersionUpdateReturn(
            newVersion = Some(2)
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))
          when(mockVendorRequestService.convertToUpdateVendorRequest(any(), any(), any(), any(), any()))
            .thenReturn(mockUpdateRequest)
          when(mockBackendConnector.updateVendor(any())(any(), any()))
            .thenReturn(Future.successful(()))

          service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService)(ec, testHc, request).futureValue

          verify(mockBackendConnector, times(1)).updateReturnVersion(any())(eqTo(testHc), any())
          verify(mockBackendConnector, times(1)).updateVendor(any())(eqTo(testHc), any())
        }
      }

      "when creating a new vendor" - {

        "must successfully create vendor when errorCalc is true (less than 99 entities)" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val userAnswers = createUserAnswers(fullReturn = Some(createFullReturn()))
          val sessionData = createSessionData(vendorId = None)

          val mockVendorRequest = mock[CreateVendorRequest]

          when(mockVendorRequestService.convertToVendorRequest(any(), any(), any()))
            .thenReturn(mockVendorRequest)
          when(mockBackendConnector.createVendor(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.vendor.routes.VendorOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createVendor(eqTo(mockVendorRequest))(any(), any())
        }

        "must skip creation when errorCalc is false (99 or more entities)" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val vendors = (1 to 99).map(i => createVendor(vendorID = Some(s"vendor-$i")))
          val fullReturn = createFullReturn(vendors = vendors)
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = None)

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.vendor.routes.VendorOverviewController.onPageLoad().url
          verify(mockBackendConnector, never).createVendor(any())(any(), any())
        }

        "must calculate errorCalc correctly with vendors and purchasers combined (98 total)" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val vendors = (1 to 50).map(i => createVendor(vendorID = Some(s"vendor-$i")))
          val purchasers = (1 to 48).map(i => mock[models.Purchaser])
          val fullReturn = createFullReturn(vendors = vendors, purchasers = purchasers)
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = None)

          val mockVendorRequest = mock[CreateVendorRequest]

          when(mockVendorRequestService.convertToVendorRequest(any(), any(), any()))
            .thenReturn(mockVendorRequest)
          when(mockBackendConnector.createVendor(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.vendor.routes.VendorOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createVendor(any())(any(), any())
        }

        "must skip creation when vendors and purchasers combined equal 99" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val vendors = (1 to 50).map(i => createVendor(vendorID = Some(s"vendor-$i")))
          val purchasers = (1 to 49).map(i => mock[models.Purchaser])
          val fullReturn = createFullReturn(vendors = vendors, purchasers = purchasers)
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = None)

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.vendor.routes.VendorOverviewController.onPageLoad().url
          verify(mockBackendConnector, never).createVendor(any())(any(), any())
        }

        "must handle empty full return correctly for errorCalc" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val userAnswers = createUserAnswers(fullReturn = None)
          val sessionData = createSessionData(vendorId = None)

          val mockVendorRequest = mock[CreateVendorRequest]

          when(mockVendorRequestService.convertToVendorRequest(any(), any(), any()))
            .thenReturn(mockVendorRequest)
          when(mockBackendConnector.createVendor(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.vendor.routes.VendorOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createVendor(any())(any(), any())
        }

        "must handle full return with None vendor list correctly" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val fullReturn = createFullReturn(vendors = Seq.empty, purchasers = Seq.empty)
          val userAnswers = createUserAnswers(fullReturn = Some(fullReturn))
          val sessionData = createSessionData(vendorId = None)

          val mockVendorRequest = mock[CreateVendorRequest]

          when(mockVendorRequestService.convertToVendorRequest(any(), any(), any()))
            .thenReturn(mockVendorRequest)
          when(mockBackendConnector.createVendor(any())(any(), any()))
            .thenReturn(Future.successful(()))

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.vendor.routes.VendorOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createVendor(any())(any(), any())
        }

        "must propagate backend connector createVendor failures" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val userAnswers = createUserAnswers(fullReturn = Some(createFullReturn()))
          val sessionData = createSessionData(vendorId = None)

          val mockVendorRequest = mock[CreateVendorRequest]

          when(mockVendorRequestService.convertToVendorRequest(any(), any(), any()))
            .thenReturn(mockVendorRequest)
          when(mockBackendConnector.createVendor(any())(any(), any()))
            .thenReturn(Future.failed(new RuntimeException("Create vendor failed")))

          whenReady(service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustBe "Create vendor failed"
          }
        }

        "must call createVendor exactly once" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val userAnswers = createUserAnswers(fullReturn = Some(createFullReturn()))
          val sessionData = createSessionData(vendorId = None)

          val mockVendorRequest = mock[CreateVendorRequest]

          when(mockVendorRequestService.convertToVendorRequest(any(), any(), any()))
            .thenReturn(mockVendorRequest)
          when(mockBackendConnector.createVendor(any())(any(), any()))
            .thenReturn(Future.successful(()))

          service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).futureValue

          verify(mockBackendConnector, times(1)).createVendor(any())(any(), any())
        }
      }

      "when handling errors" - {

        "must fail when returnId is missing" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockVendorRequestService = mock[VendorRequestService]
          val service = new VendorCreateOrUpdateService()

          val userAnswers = createUserAnswers(returnId = None)
          val sessionData = createSessionData()

          whenReady(service.result(userAnswers, sessionData, mockBackendConnector, mockVendorRequestService).failed) { exception =>
            exception mustBe an[NotFoundException]
            exception.getMessage mustBe "Return ID is required"
          }
        }
      }
    }
  }
}