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
import constants.FullReturnConstants.*
import models.*
import models.vendor.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsNull, JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class VendorCreateOrUpdateServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: FakeRequest[_] = FakeRequest()

  private val mockBackendConnector = mock[StampDutyLandTaxConnector]
  private val service = new VendorCreateOrUpdateService(mockBackendConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockBackendConnector)
  }

  private val testReturnId = "test-return-id"
  private val testStorn = "test-storn"
  private val vendorCurrentData: JsObject = Json.obj(
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
        "line1" -> "123 Test Street",
        "line2" -> "Test Area",
        "line3" -> "Test Town",
        "line4" -> JsNull,
        "line5" -> JsNull,
        "postcode" ->  "AA1 1AA",
        "country" -> Json.obj(
          "code" -> "GB",
          "name" -> "United Kingdom"
        ),
        "addressValidated" -> true
      ),
      "representedByAnAgent" -> "true"
    ))
  private val userAnswers = emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)
  private val userAnswersWithExistingVendor = userAnswers
    .copy(fullReturn = Some(completeFullReturn), data = vendorCurrentData)

  private val userAnswersWithNoVendors = userAnswersWithExistingVendor
    .copy(fullReturn = Some(incompleteFullReturn), data = vendorCurrentData)

  private val purchaserAgent = ReturnAgent(
    returnAgentID = Some("AGENT004"),
    agentType = Some("PURCHASER"),
    name = Some("Purchaser Agent Ltd")
  )

  private val vendorAgent = ReturnAgent(
    returnAgentID = Some("AGENT003"),
    agentType = Some("VENDOR"),
    name = Some("Vendor Agent Ltd")
  )

  private val userAnswersWithNoVendorAgent = userAnswers.copy(
    fullReturn = Some(completeFullReturn.copy(
      returnAgent = None)
    ))

  private val userAnswersWithVendorAgent = userAnswers.copy(
    fullReturn = Some(completeFullReturn.copy(
      returnAgent = Some(Seq(vendorAgent)))
    ))

  private val userAnswersWithPurchaserAgent = userAnswers.copy(
    fullReturn = Some(completeFullReturn.copy(
      returnAgent = Some(Seq(purchaserAgent)))
    ))

  "VendorCreateOrUpdateService" - {

    "updateVendor" - {

      "must update vendor and redirect to overview when version update returns new version and update vendor returns true" in {
        val userAnswers = userAnswersWithExistingVendor

        val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))
        val updateVendorReturn = UpdateVendorReturn(true)

        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))
        when(mockBackendConnector.updateVendor(any[UpdateVendorRequest])(any(), any()))
          .thenReturn(Future.successful(updateVendorReturn))

        val result = service.updateVendor(userAnswers).futureValue

        status(Future.successful(result)) mustEqual SEE_OTHER
        redirectLocation(Future.successful(result)).value mustEqual
          controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
        verify(mockBackendConnector, times(1)).updateVendor(any())(any(), any())
        flash(Future.successful(result)).get("vendorUpdated") mustBe Some("Jane Elizabeth Johnson")
      }

      "must redirect to CYA when update vendor returns false" in {
        val userAnswers = userAnswersWithExistingVendor

        val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))
        val updateVendorReturn = UpdateVendorReturn(false)

        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))
        when(mockBackendConnector.updateVendor(any[UpdateVendorRequest])(any(), any()))
          .thenReturn(Future.successful(updateVendorReturn))

        val result = service.updateVendor(userAnswers).futureValue

        status(Future.successful(result)) mustEqual SEE_OTHER
        redirectLocation(Future.successful(result)).value mustEqual
          controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url
        verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
        verify(mockBackendConnector, times(1)).updateVendor(any())(any(), any())
      }

      "must redirect to CYA when version update does not return a new version" in {
        val userAnswers = userAnswersWithExistingVendor

        val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = None)

        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))

        val result = service.updateVendor(userAnswers).futureValue

        status(Future.successful(result)) mustEqual SEE_OTHER
        redirectLocation(Future.successful(result)).value mustEqual
          controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url
        verify(mockBackendConnector, never()).updateVendor(any())(any(), any())
      }

      "must fail when vendor is not found in full return" in {
        val userAnswers = userAnswersWithExistingVendor.copy(fullReturn = Some(completeFullReturn.copy(vendor = Some(Seq(completeVendor.copy(vendorID = Some("VEN002")))))))
        val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))

        whenReady(service.updateVendor(userAnswers).failed) { exception =>
          exception mustBe a [NoSuchElementException]
        }
      }

      "must fail when vendor resource ref is not found" in {
        val userAnswers = userAnswersWithExistingVendor.copy(fullReturn = Some(completeFullReturn.copy(vendor = Some(Seq(completeVendor.copy(vendorResourceRef = None))))))

        val returnVersionResponse = ReturnVersionUpdateReturn(
          newVersion = Some(2))

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))

        whenReady(service.updateVendor(userAnswers).failed) { exception =>
          exception mustBe a [NoSuchElementException]
        }
      }

      "must redirect to the update return version error page when updateReturnVersion fails" in {
        val userAnswers = userAnswersWithExistingVendor

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Backend failure")))

        val result = service.updateVendor(userAnswers).futureValue

        status(Future.successful(result)) mustEqual SEE_OTHER
        redirectLocation(Future.successful(result)).value mustEqual
          controllers.routes.UpdateReturnVersionErrorController.onPageLoad().url
        verify(mockBackendConnector, never()).updateVendor(any())(any(), any())
      }

      "must propagate backend connector updateVendor failures" in {
        val userAnswers = userAnswersWithExistingVendor
        val returnVersionResponse = ReturnVersionUpdateReturn(
          newVersion = Some(2)
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))
        when(mockBackendConnector.updateVendor(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Update vendor failed")))

        whenReady(service.updateVendor(userAnswers).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Update vendor failed"
        }
      }

      "must pass HeaderCarrier to connector" in {
        val testHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

        val userAnswers = userAnswersWithExistingVendor
        val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))
        val updateVendorReturn = UpdateVendorReturn(true)

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))
        when(mockBackendConnector.updateVendor(any())(any(), any()))
          .thenReturn(Future.successful(updateVendorReturn))

        service.updateVendor(userAnswers)(testHc, request).futureValue

        verify(mockBackendConnector, times(1)).updateReturnVersion(any())(eqTo(testHc), any())
        verify(mockBackendConnector, times(1)).updateVendor(any())(eqTo(testHc), any())
      }
    }

    "updateIsRepresentedByAgent" - {

      "when answer is yes" - {

        "must update isRepresentedByAgent when update is successful" in {
          val userAnswers = userAnswersWithVendorAgent

          val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))
          val updateVendorReturn = UpdateVendorReturn(true)

          when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))
          when(mockBackendConnector.updateVendor(any[UpdateVendorRequest])(any(), any()))
            .thenReturn(Future.successful(updateVendorReturn))

          val result = service.updateIsRepresentedByAgent(true, userAnswers).futureValue

          result mustEqual Right(true)
          verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector, times(1)).updateVendor(any())(any(), any())
          verify(mockBackendConnector, never()).deleteReturnAgent(any())(any(), any())
        }
      }

      "when answer is no" - {

        "must update isRepresentedByAgent when update is successful" in {
          val userAnswers = userAnswersWithNoVendorAgent

          val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))
          val updateVendorReturn = UpdateVendorReturn(true)

          when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))
          when(mockBackendConnector.updateVendor(any[UpdateVendorRequest])(any(), any()))
            .thenReturn(Future.successful(updateVendorReturn))

          val result = service.updateIsRepresentedByAgent(false, userAnswers).futureValue

          result mustEqual Right(true)
          verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector, times(1)).updateVendor(any())(any(), any())
          verify(mockBackendConnector, never()).deleteReturnAgent(any())(any(), any())
        }

        "must update isRepresentedByAgent and remove vendor agent details if they exist" in {
          val userAnswers = userAnswersWithVendorAgent

          val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))
          val updateVendorReturn = UpdateVendorReturn(true)
          val deleteReturnAgentReturn = DeleteReturnAgentReturn(true)

          when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))
          when(mockBackendConnector.updateVendor(any[UpdateVendorRequest])(any(), any()))
            .thenReturn(Future.successful(updateVendorReturn))
          when(mockBackendConnector.deleteReturnAgent(any[DeleteReturnAgentRequest])(any(), any()))
            .thenReturn(Future.successful(deleteReturnAgentReturn))

          val result = service.updateIsRepresentedByAgent(false, userAnswers).futureValue

          result mustEqual Right(true)
          verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector, times(1)).updateVendor(any())(any(), any())
          verify(mockBackendConnector, times(1)).deleteReturnAgent(any())(any(), any())
        }

        "must update isRepresentedByAgent and not remove purchaser agent details if they exist" in {
          val userAnswers = userAnswersWithPurchaserAgent

          val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))
          val updateVendorReturn = UpdateVendorReturn(true)

          when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))
          when(mockBackendConnector.updateVendor(any[UpdateVendorRequest])(any(), any()))
            .thenReturn(Future.successful(updateVendorReturn))

          val result = service.updateIsRepresentedByAgent(false, userAnswers).futureValue

          result mustEqual Right(true)
          verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector, times(1)).updateVendor(any())(any(), any())
          verify(mockBackendConnector, never()).deleteReturnAgent(any())(any(), any())
        }
      }

      "must fail when vendor is not found in full return" in {
        val userAnswers = userAnswersWithVendorAgent.copy(fullReturn = Some(completeFullReturn.copy(vendor = Some(Seq(completeVendor.copy(vendorID = Some("VEN002")))))))
        val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))

        when(mockBackendConnector.updateVendor(any[UpdateVendorRequest])(any(), any()))
          .thenReturn(Future.failed(new NoSuchElementException))

        whenReady(service.updateIsRepresentedByAgent(true, userAnswers).failed) { exception =>
          exception mustBe a[NoSuchElementException]
        }
      }

      "must fail when vendor resource ref is not found" in {
        val userAnswers = userAnswersWithVendorAgent.copy(fullReturn = Some(completeFullReturn.copy(vendor = Some(Seq(completeVendor.copy(vendorResourceRef = None))))))

        val returnVersionResponse = ReturnVersionUpdateReturn(
          newVersion = Some(2))

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))

        when(mockBackendConnector.updateVendor(any[UpdateVendorRequest])(any(), any()))
          .thenReturn(Future.failed(new NoSuchElementException))

        whenReady(service.updateIsRepresentedByAgent(true, userAnswers).failed) { exception =>
          exception mustBe a[NoSuchElementException]
        }
      }

      "must return Right(false) when version update does not return a new version" in {
        val userAnswers = userAnswersWithVendorAgent

        val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = None)

        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))

        val result = service.updateIsRepresentedByAgent(true, userAnswers).futureValue

        result mustEqual Right(false)
        verify(mockBackendConnector, never()).updateVendor(any())(any(), any())
      }

      "must redirect to the update return version error page when updateReturnVersion fails" in {
        val userAnswers = userAnswersWithVendorAgent

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Backend failure")))

        val result = service.updateIsRepresentedByAgent(true, userAnswers).futureValue

        result match {
          case Left(redirect) =>
            redirectLocation(Future.successful(redirect)).value mustEqual
              controllers.routes.UpdateReturnVersionErrorController.onPageLoad().url
          case Right(_) => fail("expected Left with error redirect")
        }
        verify(mockBackendConnector, never()).updateVendor(any())(any(), any())
      }

      "must propagate backend connector updateVendor failures" in {
        val userAnswers = userAnswersWithVendorAgent
        val returnVersionResponse = ReturnVersionUpdateReturn(
          newVersion = Some(2)
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))
        when(mockBackendConnector.updateVendor(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Update vendor failed")))

        whenReady(service.updateIsRepresentedByAgent(true, userAnswers).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Update vendor failed"
        }
      }

      "must propagate backend connector deleteReturnAgent failures" in {
        val userAnswers = userAnswersWithVendorAgent
        val returnVersionResponse = ReturnVersionUpdateReturn(
          newVersion = Some(2)
        )
        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))
        when(mockBackendConnector.updateVendor(any())(any(), any()))
          .thenReturn(Future.successful(UpdateVendorReturn(true)))
        when(mockBackendConnector.deleteReturnAgent(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Delete vendor agent failed")))

        whenReady(service.updateIsRepresentedByAgent(false, userAnswers).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Delete vendor agent failed"
        }
      }
    }

    "createVendor" - {

      "must successfully create vendor when no vendors in full return" in {
        val userAnswers = userAnswersWithNoVendors

        val createVendorReturn = CreateVendorReturn(vendorResourceRef = "test-vendor-resource-ref", vendorId = "VEN001")
        when(mockBackendConnector.createVendor(any())(any(), any()))
          .thenReturn(Future.successful(createVendorReturn))

        val result = service.createVendor(userAnswers).futureValue

        status(Future.successful(result)) mustEqual SEE_OTHER
        redirectLocation(Future.successful(result)).value mustEqual
          controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        verify(mockBackendConnector, times(1)).createVendor(any())(any(), any())
        flash(Future.successful(result)).get("vendorCreated") mustBe Some("Jane Elizabeth Johnson")
      }

      "must successfully create vendor when vendor in full return at a different ID" in {
        val userAnswers = userAnswersWithExistingVendor.copy(fullReturn = Some(completeFullReturn.copy(vendor = Some(Seq(completeVendor.copy(vendorID = Some("VEN002")))))))

        val createVendorReturn = CreateVendorReturn(vendorResourceRef = "test-vendor-resource-ref", vendorId = "VEN001")
        when(mockBackendConnector.createVendor(any())(any(), any()))
          .thenReturn(Future.successful(createVendorReturn))

        val result = service.createVendor(userAnswers).futureValue

        status(Future.successful(result)) mustEqual SEE_OTHER
        redirectLocation(Future.successful(result)).value mustEqual
          controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        verify(mockBackendConnector, times(1)).createVendor(any())(any(), any())
        flash(Future.successful(result)).get("vendorCreated") mustBe Some("Jane Elizabeth Johnson")
      }

      "must redirect to CYA when create vendor return ID is empty" in {
        val userAnswers = userAnswersWithExistingVendor.copy(fullReturn = Some(completeFullReturn.copy(vendor = Some(Seq(completeVendor.copy(vendorID = Some("VEN002")))))))

        val createVendorReturn = CreateVendorReturn(vendorResourceRef = "test-vendor-resource-ref", vendorId = "")
        when(mockBackendConnector.createVendor(any())(any(), any()))
          .thenReturn(Future.successful(createVendorReturn))

        val result = service.createVendor(userAnswers).futureValue

        status(Future.successful(result)) mustEqual SEE_OTHER
        redirectLocation(Future.successful(result)).value mustEqual
          controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url
        verify(mockBackendConnector, times(1)).createVendor(any())(any(), any())
      }

      "must propagate backend connector createVendor failures" in {
        val userAnswers = userAnswersWithNoVendors

        when(mockBackendConnector.createVendor(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Create vendor failed")))

        whenReady(service.createVendor(userAnswers).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Create vendor failed"
        }
      }
    }

    "isVendorPurchaserCountBelowMaximum" - {

      "must return true when vendor and purchaser combined count is 98" in {
        val vendors = (1 to 50).map(i => mock[models.Vendor])
        val purchasers = (1 to 48).map(i => mock[models.Purchaser])
        val userAnswers = userAnswersWithNoVendors.copy(fullReturn = Some(incompleteFullReturn.copy(
          vendor = Some(vendors),
          purchaser = Some(purchasers)
        )))

        service.isVendorPurchaserCountBelowMaximum(userAnswers) mustBe true
      }

      "must return false when vendor and purchaser combined count is 99" in {
        val vendors = (1 to 50).map(i => mock[models.Vendor])
        val purchasers = (1 to 49).map(i => mock[models.Purchaser])
        val userAnswers = userAnswersWithNoVendors.copy(fullReturn = Some(incompleteFullReturn.copy(
          vendor = Some(vendors),
          purchaser = Some(purchasers)
        )))

        service.isVendorPurchaserCountBelowMaximum(userAnswers) mustBe false
      }

      "must return false when vendor and purchaser combined count is above 99" in {
        val vendors = (1 to 50).map(i => mock[models.Vendor])
        val purchasers = (1 to 50).map(i => mock[models.Purchaser])
        val userAnswers = userAnswersWithNoVendors.copy(fullReturn = Some(incompleteFullReturn.copy(
          vendor = Some(vendors),
          purchaser = Some(purchasers)
        )))

        service.isVendorPurchaserCountBelowMaximum(userAnswers) mustBe false
      }

      "must return true when there are no vendors or purchasers" in {
        val userAnswers = userAnswersWithNoVendors

        service.isVendorPurchaserCountBelowMaximum(userAnswers) mustBe true
      }

      "must return true when there is no fullReturn" in {
        val userAnswers = emptyUserAnswers

        service.isVendorPurchaserCountBelowMaximum(userAnswers) mustBe true
      }
    }
  }
}