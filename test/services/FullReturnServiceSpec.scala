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
import connectors.StubConnector
import constants.FullReturnConstants._
import models.FullReturn
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

  "FullReturnService" - {

    "getFullReturn" - {

      "must return FullReturn when returnId is provided" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = Some("123456")

        when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockStubConnector)
        val result = service.getFullReturn(testReturnId).futureValue

        result mustBe completeFullReturn
        result.stornId mustBe Some("STORN123456")
        result.returnResourceRef mustBe Some("RRF-2024-001")
        verify(mockStubConnector, times(1)).stubGetFullReturn(eqTo(testReturnId))(any(), any())
      }

      "must return FullReturn when returnId is None" in {
        val mockStubConnector = mock[StubConnector]

        when(mockStubConnector.stubGetFullReturn(eqTo(None))(any(), any()))
          .thenReturn(Future.successful(emptyFullReturn))

        val service = new FullReturnService(mockStubConnector)
        val result = service.getFullReturn(None).futureValue

        result mustBe emptyFullReturn
        verify(mockStubConnector, times(1)).stubGetFullReturn(eqTo(None))(any(), any())
      }

      "must handle connector failure gracefully when returnId is provided" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = Some("123456")

        when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        val service = new FullReturnService(mockStubConnector)

        whenReady(service.getFullReturn(testReturnId).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }

        verify(mockStubConnector, times(1)).stubGetFullReturn(eqTo(testReturnId))(any(), any())
      }

      "must call stubGetFullReturn with correct returnId" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = Some("TEST-ID-789")

        when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockStubConnector)
        service.getFullReturn(testReturnId).futureValue

        verify(mockStubConnector, times(1)).stubGetFullReturn(eqTo(testReturnId))(any(), any())
      }

      "must return FullReturn with complete data from connector" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = Some("TEST-123")

        when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockStubConnector)
        val result = service.getFullReturn(testReturnId).futureValue

        result mustBe completeFullReturn
        result.sdltOrganisation mustBe defined
        result.returnInfo mustBe defined
        result.purchaser mustBe defined
        result.vendor mustBe defined
      }

      "must return FullReturn with minimal data from connector" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = Some("MIN-123")

        when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(minimalFullReturn))

        val service = new FullReturnService(mockStubConnector)
        val result = service.getFullReturn(testReturnId).futureValue

        result mustBe minimalFullReturn
        result.stornId mustBe defined
        result.returnResourceRef mustBe defined
        result.sdltOrganisation mustBe defined
        result.returnInfo mustBe defined
      }

      "must return incomplete FullReturn from connector" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = Some("INC-123")

        when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(incompleteFullReturn))

        val service = new FullReturnService(mockStubConnector)
        val result = service.getFullReturn(testReturnId).futureValue

        result mustBe incompleteFullReturn
        result.stornId mustBe defined
        result.returnResourceRef must not be defined
      }

      "must handle different returnId formats" in {
        val mockStubConnector = mock[StubConnector]
        val returnIds = List(Some("123"), Some("ABC-123"), Some("test-return-id"), None)

        returnIds.foreach { testReturnId =>
          when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId))(any(), any()))
            .thenReturn(Future.successful(completeFullReturn))

          val service = new FullReturnService(mockStubConnector)
          val result = service.getFullReturn(testReturnId).futureValue

          result mustBe completeFullReturn
          verify(mockStubConnector, times(1)).stubGetFullReturn(eqTo(testReturnId))(any(), any())
          reset(mockStubConnector)
        }
      }

      "must pass HeaderCarrier to connector" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = Some("123456")
        val testHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

        when(mockStubConnector.stubGetFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockStubConnector)
        service.getFullReturn(testReturnId)(testHc, request).futureValue

        verify(mockStubConnector, times(1)).stubGetFullReturn(eqTo(testReturnId))(eqTo(testHc), any())
      }

      "must pass Request to connector" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = Some("123456")
        val testRequest = FakeRequest("GET", "/test")

        when(mockStubConnector.stubGetFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockStubConnector)
        service.getFullReturn(testReturnId)(hc, testRequest).futureValue

        verify(mockStubConnector, times(1)).stubGetFullReturn(eqTo(testReturnId))(any(), eqTo(testRequest))
      }

      "must handle empty FullReturn from connector" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = Some("EMPTY-123")

        when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(emptyFullReturn))

        val service = new FullReturnService(mockStubConnector)
        val result = service.getFullReturn(testReturnId).futureValue

        result mustBe emptyFullReturn
        result.stornId must not be defined
        result.returnResourceRef must not be defined
        result.sdltOrganisation must not be defined
      }

      "must propagate connector exceptions" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = Some("123456")
        val testException = new RuntimeException("Connection timeout")

        when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.failed(testException))

        val service = new FullReturnService(mockStubConnector)

        whenReady(service.getFullReturn(testReturnId).failed) { exception =>
          exception mustBe testException
        }

        verify(mockStubConnector, times(1)).stubGetFullReturn(eqTo(testReturnId))(any(), any())
      }

      "must call connector exactly once per invocation" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = Some("123456")

        when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val service = new FullReturnService(mockStubConnector)
        service.getFullReturn(testReturnId).futureValue

        verify(mockStubConnector, times(1)).stubGetFullReturn(any())(any(), any())
        verifyNoMoreInteractions(mockStubConnector)
      }

      "must handle multiple sequential calls" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId1 = Some("123456")
        val testReturnId2 = Some("789012")

        when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId1))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockStubConnector.stubGetFullReturn(eqTo(testReturnId2))(any(), any()))
          .thenReturn(Future.successful(minimalFullReturn))

        val service = new FullReturnService(mockStubConnector)

        val result1 = service.getFullReturn(testReturnId1).futureValue
        val result2 = service.getFullReturn(testReturnId2).futureValue

        result1 mustBe completeFullReturn
        result2 mustBe minimalFullReturn
        verify(mockStubConnector, times(1)).stubGetFullReturn(eqTo(testReturnId1))(any(), any())
        verify(mockStubConnector, times(1)).stubGetFullReturn(eqTo(testReturnId2))(any(), any())
      }
    }
  }
}