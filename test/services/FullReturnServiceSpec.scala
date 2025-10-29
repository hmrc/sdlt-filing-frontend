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
import models.{PrelimReturn, VendorReturn}
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

      "must return FullReturn with Some(prelimReturn) & Some(vendorReturn) when returnId is provided" in {
        val mockStubConnector = mock[StubConnector]
        val mockPrelimReturn = mock[PrelimReturn]
        val mockVendorReturn = mock[VendorReturn]
        val testReturnId = "123456"

        when(mockStubConnector.stubPremlimQuestions(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(mockPrelimReturn))

        when(mockStubConnector.stubVendorQuestions(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(mockVendorReturn))

        val service = new FullReturnService(mockStubConnector)
        val result = service.getFullReturn(Some(testReturnId)).futureValue

        result.prelimReturn mustBe Some(mockPrelimReturn)
        verify(mockStubConnector, times(1)).stubPremlimQuestions(eqTo(testReturnId))(any(), any())

        result.vendorReturn mustBe Some(mockVendorReturn)
        verify(mockStubConnector, times(1)).stubVendorQuestions(eqTo(testReturnId))(any(), any())
      }

      "must return FullReturn with None & None when returnId is not provided" in {
        val mockStubConnector = mock[StubConnector]

        val service = new FullReturnService(mockStubConnector)
        val result = service.getFullReturn(None).futureValue

        result.prelimReturn mustBe None
        verifyNoInteractions(mockStubConnector)
      }

      "must handle connector failure gracefully when returnId is provided" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = "123456"

        when(mockStubConnector.stubPremlimQuestions(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        val service = new FullReturnService(mockStubConnector)

        whenReady(service.getFullReturn(Some(testReturnId)).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }

        verify(mockStubConnector, times(1)).stubPremlimQuestions(eqTo(testReturnId))(any(), any())
      }

      "must call getPrelimQuestions & getVendorQuestions with correct returnId" in {
        val mockStubConnector = mock[StubConnector]
        val mockPrelimReturn = mock[PrelimReturn]
        val mockVendorReturn = mock[VendorReturn]
        val testReturnId = "TEST-ID-789"

        when(mockStubConnector.stubPremlimQuestions(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(mockPrelimReturn))

        when(mockStubConnector.stubVendorQuestions(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(mockVendorReturn))

        val service = new FullReturnService(mockStubConnector)
        service.getFullReturn(Some(testReturnId)).futureValue

        verify(mockStubConnector, times(1)).stubPremlimQuestions(eqTo(testReturnId))(any(), any())
        verify(mockStubConnector, times(1)).stubVendorQuestions(eqTo(testReturnId))(any(), any())
      }
    }

    "getPrelimQuestions" - {

      "must call stubConnector.stubPremlimQuestions with correct returnId" in {
        val mockStubConnector = mock[StubConnector]
        val mockPrelimReturn = mock[PrelimReturn]
        val testReturnId = "123456"

        when(mockStubConnector.stubPremlimQuestions(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(mockPrelimReturn))

        val service = new FullReturnService(mockStubConnector)
        val result = service.getPrelimQuestions(testReturnId).futureValue

        result mustBe mockPrelimReturn
        verify(mockStubConnector, times(1)).stubPremlimQuestions(eqTo(testReturnId))(any(), any())
      }

      "must return PrelimReturn from connector" in {
        val mockStubConnector = mock[StubConnector]
        val mockPrelimReturn = mock[PrelimReturn]
        val testReturnId = "TEST-123"

        when(mockStubConnector.stubPremlimQuestions(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(mockPrelimReturn))

        val service = new FullReturnService(mockStubConnector)
        val result = service.getPrelimQuestions(testReturnId).futureValue

        result mustBe mockPrelimReturn
      }

      "must handle connector failure" in {
        val mockStubConnector = mock[StubConnector]
        val testReturnId = "123456"

        when(mockStubConnector.stubPremlimQuestions(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Connection error")))

        val service = new FullReturnService(mockStubConnector)

        whenReady(service.getPrelimQuestions(testReturnId).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection error"
        }

        verify(mockStubConnector, times(1)).stubPremlimQuestions(eqTo(testReturnId))(any(), any())
      }

      "must handle different returnId formats" in {
        val mockStubConnector = mock[StubConnector]
        val mockPrelimReturn = mock[PrelimReturn]
        val returnIds = List("123", "ABC-123", "test-return-id")

        returnIds.foreach { testReturnId =>
          when(mockStubConnector.stubPremlimQuestions(eqTo(testReturnId))(any(), any()))
            .thenReturn(Future.successful(mockPrelimReturn))

          val service = new FullReturnService(mockStubConnector)
          val result = service.getPrelimQuestions(testReturnId).futureValue

          result mustBe mockPrelimReturn
          verify(mockStubConnector, times(1)).stubPremlimQuestions(eqTo(testReturnId))(any(), any())
          reset(mockStubConnector)
        }
      }
    }
  }
}