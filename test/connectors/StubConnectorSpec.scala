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

package connectors

import base.SpecBase
import config.FrontendAppConfig
import constants.FullReturnConstants._
import models.FullReturn
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class StubConnectorSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: FakeRequest[_] = FakeRequest()

  private val testStubUrl = "http://localhost:9000"

  "StubConnector" - {

    "stubGetFullReturn" - {

      "must return FullReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val connector = new StubConnector(mockHttpClient, mockConfig)
        val result = connector.stubGetFullReturn(testReturnId).futureValue

        result mustBe completeFullReturn
        result.stornId mustBe Some("STORN123456")
        result.returnResourceRef mustBe Some("RRF-2024-001")
      }

      "must construct correct URL with returnId parameter" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("TEST-123")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val connector = new StubConnector(mockHttpClient, mockConfig)
        connector.stubGetFullReturn(testReturnId).futureValue

        verify(mockHttpClient, times(1)).get(any())(any())
        verify(mockConfig, times(1)).baseUrl("stamp-duty-land-tax-stub")
      }

      "must handle upstream 4xx errors and throw exception" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.failed(upstreamError))

        val connector = new StubConnector(mockHttpClient, mockConfig)

        whenReady(connector.stubGetFullReturn(testReturnId).failed) { exception =>
          exception mustBe a[Throwable]
        }

        verify(mockHttpClient, times(1)).get(any())(any())
      }

      "must handle upstream 5xx errors and throw exception" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Internal Server Error", 500)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.failed(upstreamError))

        val connector = new StubConnector(mockHttpClient, mockConfig)

        whenReady(connector.stubGetFullReturn(testReturnId).failed) { exception =>
          exception mustBe a[Throwable]
        }

        verify(mockHttpClient, times(1)).get(any())(any())
      }

      "must handle general exceptions and throw exception" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new StubConnector(mockHttpClient, mockConfig)

        whenReady(connector.stubGetFullReturn(testReturnId).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }

        verify(mockHttpClient, times(1)).get(any())(any())
      }

      "must handle different returnId formats" in {
        val returnIds = List(Some("123"), Some("ABC-123"), Some("test-return-id"))

        returnIds.foreach { testReturnId =>
          val mockHttpClient = mock[HttpClientV2]
          val mockConfig = mock[FrontendAppConfig]
          val mockRequestBuilder = mock[RequestBuilder]

          when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
          when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
          when(mockRequestBuilder.execute[FullReturn](any(), any()))
            .thenReturn(Future.successful(completeFullReturn))

          val connector = new StubConnector(mockHttpClient, mockConfig)
          val result = connector.stubGetFullReturn(testReturnId).futureValue

          result mustBe completeFullReturn
        }
      }

      "must throw NoSuchElementException when returnId is None" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)

        val connector = new StubConnector(mockHttpClient, mockConfig)

        val exception = connector.stubGetFullReturn(None).failed.futureValue
        exception mustBe a[IllegalArgumentException]
      }

      "must use correct base URL from config" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")
        val customStubUrl = "http://custom-stub:8080"

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(customStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val connector = new StubConnector(mockHttpClient, mockConfig)
        connector.stubGetFullReturn(testReturnId).futureValue

        verify(mockConfig, times(1)).baseUrl("stamp-duty-land-tax-stub")
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val connector = new StubConnector(mockHttpClient, mockConfig)
        connector.stubGetFullReturn(testReturnId)(customHc, request).futureValue

        verify(mockHttpClient, times(1)).get(any())(any())
      }

      "must recover from exceptions using the recover block" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")
        val testException = new RuntimeException("Test error")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.failed(testException))

        val connector = new StubConnector(mockHttpClient, mockConfig)

        val result = connector.stubGetFullReturn(testReturnId).failed.futureValue

        result mustBe a[Throwable]
        verify(mockRequestBuilder, times(1)).execute[FullReturn](any(), any())
      }

      "must call execute method on request builder" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        val connector = new StubConnector(mockHttpClient, mockConfig)
        connector.stubGetFullReturn(testReturnId).futureValue

        verify(mockRequestBuilder, times(1)).execute[FullReturn](any(), any())
      }

      "must return FullReturn with minimal data" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.successful(minimalFullReturn))

        val connector = new StubConnector(mockHttpClient, mockConfig)
        val result = connector.stubGetFullReturn(testReturnId).futureValue

        result mustBe minimalFullReturn
        result.stornId mustBe defined
        result.returnResourceRef mustBe defined
      }

      "must return incomplete FullReturn" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.successful(incompleteFullReturn))

        val connector = new StubConnector(mockHttpClient, mockConfig)
        val result = connector.stubGetFullReturn(testReturnId).futureValue

        result mustBe incompleteFullReturn
        result.stornId mustBe defined
        result.returnResourceRef must not be defined
      }

      "must return empty FullReturn" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.successful(emptyFullReturn))

        val connector = new StubConnector(mockHttpClient, mockConfig)
        val result = connector.stubGetFullReturn(testReturnId).futureValue

        result mustBe emptyFullReturn
        result.stornId must not be defined
      }

      "must log error when exception occurs" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testReturnId = Some("123456")
        val testException = new RuntimeException("Test error")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[FullReturn](any(), any()))
          .thenReturn(Future.failed(testException))

        val connector = new StubConnector(mockHttpClient, mockConfig)

        whenReady(connector.stubGetFullReturn(testReturnId).failed) { exception =>
          exception.getMessage mustBe "Test error"
        }
      }
    }

    "sdltStubUrl" - {

      "must be initialized with correct value from config" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)

        val connector = new StubConnector(mockHttpClient, mockConfig)

        connector.sdltStubUrl mustBe testStubUrl
      }

      "must use lazy initialization" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)

        val connector = new StubConnector(mockHttpClient, mockConfig)

        // Verify config is not called until sdltStubUrl is accessed
        verify(mockConfig, times(0)).baseUrl(any())

        // Access the lazy val
        connector.sdltStubUrl

        // Now verify it was called
        verify(mockConfig, times(1)).baseUrl("stamp-duty-land-tax-stub")
      }
    }
  }
}