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

package services

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.*
import models.{FullReturn, GetReturnByRefRequest}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class FullReturnServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: FakeRequest[_] = FakeRequest()
  val testReturnId = "123456"
  val testStorn = "12345690"
  val testGetReturnByRefRequest: GetReturnByRefRequest = GetReturnByRefRequest(returnResourceRef = testReturnId, storn = testStorn)

  "FullReturnService" - {

    "getFullReturn" - {

      "must return FullReturn when returnId is provided" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(mockBackendConnector.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockBackendConnector)
        val result = service.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe completeFullReturn
        result.stornId mustBe "STORN123456"
        result.returnResourceRef mustBe "RRF-2024-001"
        verify(mockBackendConnector, times(1)).getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any())
      }

      "must handle connector failure gracefully when returnId is provided" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(mockBackendConnector.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        val service = new FullReturnService(mockBackendConnector)

        whenReady(service.getFullReturn(testGetReturnByRefRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }

        verify(mockBackendConnector, times(1)).getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any())
      }

      "must call.getFullReturn() with correct returnId" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(mockBackendConnector.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockBackendConnector)
        service.getFullReturn(testGetReturnByRefRequest).futureValue

        verify(mockBackendConnector, times(1)).getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any())
      }

      "must return FullReturn with complete data from connector" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(mockBackendConnector.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockBackendConnector)
        val result = service.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe completeFullReturn
        result.sdltOrganisation mustBe defined
        result.returnInfo mustBe defined
        result.purchaser mustBe defined
        result.vendor mustBe defined
      }

      "must return FullReturn with minimal data from connector" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(mockBackendConnector.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.successful(minimalFullReturn))

        val service = new FullReturnService(mockBackendConnector)
        val result = service.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe minimalFullReturn
        result.sdltOrganisation mustBe defined
        result.returnInfo mustBe defined
      }

      "must return incomplete FullReturn from connector" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(mockBackendConnector.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.successful(incompleteFullReturn))

        val service = new FullReturnService(mockBackendConnector)
        val result = service.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe incompleteFullReturn
        result.returnInfo must not be defined
      }

      "must handle different returnId formats" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val returnIds = List(
          testGetReturnByRefRequest.copy(returnResourceRef = "123"),
          testGetReturnByRefRequest.copy(returnResourceRef = "ABC-123"),
          testGetReturnByRefRequest.copy(returnResourceRef = "test-return-id"))

        returnIds.foreach { testReturnId =>
          when(mockBackendConnector.getFullReturn(eqTo(testReturnId))(any(), any()))
            .thenReturn(Future.successful(completeFullReturn))

          val service = new FullReturnService(mockBackendConnector)
          val result = service.getFullReturn(testReturnId).futureValue

          result mustBe completeFullReturn
          verify(mockBackendConnector, times(1)).getFullReturn(eqTo(testReturnId))(any(), any())
          reset(mockBackendConnector)
        }
      }

      "must pass HeaderCarrier to connector" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val testHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

        when(mockBackendConnector.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockBackendConnector)
        service.getFullReturn(testGetReturnByRefRequest)(testHc, request).futureValue

        verify(mockBackendConnector, times(1)).getFullReturn(eqTo(testGetReturnByRefRequest))(eqTo(testHc), any())
      }

      "must pass Request to connector" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val testRequest = FakeRequest("GET", "/test")

        when(mockBackendConnector.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockBackendConnector)
        service.getFullReturn(testGetReturnByRefRequest)(hc, testRequest).futureValue

        verify(mockBackendConnector, times(1)).getFullReturn(eqTo(testGetReturnByRefRequest))(any(), eqTo(testRequest))
      }

      "must handle empty FullReturn from connector" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(mockBackendConnector.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.successful(emptyFullReturn))

        val service = new FullReturnService(mockBackendConnector)
        val result = service.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe emptyFullReturn
        result.returnInfo must not be defined
        result.sdltOrganisation must not be defined
      }

      "must propagate connector exceptions" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val testException = new RuntimeException("Connection timeout")

        when(mockBackendConnector.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.failed(testException))

        val service = new FullReturnService(mockBackendConnector)

        whenReady(service.getFullReturn(testGetReturnByRefRequest).failed) { exception =>
          exception mustBe testException
        }

        verify(mockBackendConnector, times(1)).getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any())
      }

      "must call connector exactly once per invocation" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]

        when(mockBackendConnector.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockBackendConnector)
        service.getFullReturn(testGetReturnByRefRequest).futureValue

        verify(mockBackendConnector, times(1)).getFullReturn(any())(any(), any())
        verifyNoMoreInteractions(mockBackendConnector)
      }

      "must handle multiple sequential calls" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val testReturnId1 = testGetReturnByRefRequest.copy(returnResourceRef = "123")
        val testReturnId2 = testGetReturnByRefRequest.copy(returnResourceRef = "1234")

        when(mockBackendConnector.getFullReturn(eqTo(testReturnId1))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockBackendConnector.getFullReturn(eqTo(testReturnId2))(any(), any()))
          .thenReturn(Future.successful(minimalFullReturn))

        val service = new FullReturnService(mockBackendConnector)

        val result1 = service.getFullReturn(testReturnId1).futureValue
        val result2 = service.getFullReturn(testReturnId2).futureValue

        result1 mustBe completeFullReturn
        result2 mustBe minimalFullReturn
        verify(mockBackendConnector, times(1)).getFullReturn(eqTo(testReturnId1))(any(), any())
        verify(mockBackendConnector, times(1)).getFullReturn(eqTo(testReturnId2))(any(), any())
      }
    }
  }
}