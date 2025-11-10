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
import constants.FullReturnConstants.*
import models.prelimQuestions.PrelimReturn
import models.{CreateReturnResult, FullReturn, GetReturnByRefRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class StampDutyLandTaxConnectorSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: FakeRequest[_] = FakeRequest()

  private val testStubUrl = "http://localhost:9000"
  private val testBackendUrl = "http://localhost:9001"

  val returnId = "123456"
  val testStorn = "12345690"
  private val testGetReturnByRefRequest: GetReturnByRefRequest = GetReturnByRefRequest(returnResourceRef = returnId, storn = testStorn)

  private val completePrelimReturn = PrelimReturn(
    stornId = "12345",
    purchaserIsCompany = "YES",
    surNameOrCompanyName = "Test Company",
    houseNumber = Some(23),
    addressLine1 = "Test Street",
    addressLine2 = Some("Apartment 5"),
    addressLine3 = Some("Building A"),
    addressLine4 = Some("District B"),
    postcode = Some("TE23 5TT"),
    transactionType = "O"
  )

  private val minimalPrelimReturn = PrelimReturn(
    stornId = "12345",
    purchaserIsCompany = "YES",
    surNameOrCompanyName = "Test Company",
    houseNumber = None,
    addressLine1 = "Test Street",
    addressLine2 = None,
    addressLine3 = None,
    addressLine4 = None,
    postcode = None,
    transactionType = "O"
  )
  
  "StampDutyLandTaxConnector" - {

    "getFullReturn" - {

      "must return FullReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.successful(Right(completeFullReturn)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe completeFullReturn
        result.stornId mustBe Some("STORN123456")
        result.returnResourceRef mustBe Some("RRF-2024-001")
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.successful(Right(completeFullReturn)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.getFullReturn(testGetReturnByRefRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must use backend URL when stubBool is false" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.successful(Right(completeFullReturn)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.getFullReturn(testGetReturnByRefRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must handle Left response with UpstreamErrorResponse (4xx)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.getFullReturn(testGetReturnByRefRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle Left response with UpstreamErrorResponse (5xx)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Internal Server Error", 500)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.getFullReturn(testGetReturnByRefRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions and throw exception" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.getFullReturn(testGetReturnByRefRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.successful(Right(completeFullReturn)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.getFullReturn(testGetReturnByRefRequest)(customHc, request).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must recover from exceptions using the recover block" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testException = new RuntimeException("Test error")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.failed(testException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue

        result mustBe a[Throwable]
        result.getMessage mustBe "Test error"
      }

      "must return FullReturn with minimal data" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.successful(Right(minimalFullReturn)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe minimalFullReturn
        result.stornId mustBe defined
        result.returnResourceRef mustBe defined
      }

      "must return incomplete FullReturn" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.successful(Right(incompleteFullReturn)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe incompleteFullReturn
        result.stornId mustBe defined
        result.returnResourceRef must not be defined
      }

      "must return empty FullReturn" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.successful(Right(emptyFullReturn)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe emptyFullReturn
        result.stornId must not be defined
      }

      "must call withBody on request builder with correct JSON" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, FullReturn]](any(), any()))
          .thenReturn(Future.successful(Right(completeFullReturn)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.getFullReturn(testGetReturnByRefRequest).futureValue

        verify(mockRequestBuilder, times(1)).withBody(any())(any(), any(), any())
      }
    }

    "createReturn" - {

      "must return CreateReturnResult when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val expectedResult = CreateReturnResult(returnResourceRef = "RRF-2024-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.createReturn(completePrelimReturn).futureValue

        result mustBe expectedResult
        result.returnResourceRef mustBe "RRF-2024-001"
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val expectedResult = CreateReturnResult(returnResourceRef = "RRF-2024-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createReturn(completePrelimReturn).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must use backend URL when stubBool is false" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val expectedResult = CreateReturnResult(returnResourceRef = "RRF-2024-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createReturn(completePrelimReturn).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must handle Left response with UpstreamErrorResponse (400)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createReturn(completePrelimReturn).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle Left response with UpstreamErrorResponse (404)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Not Found", 404)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createReturn(completePrelimReturn).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle Left response with UpstreamErrorResponse (500)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Internal Server Error", 500)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createReturn(completePrelimReturn).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle Left response with UpstreamErrorResponse (503)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Service Unavailable", 503)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createReturn(completePrelimReturn).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions and throw exception" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createReturn(completePrelimReturn).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val expectedResult = CreateReturnResult(returnResourceRef = "RRF-2024-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createReturn(completePrelimReturn)(customHc, request).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must recover from exceptions using the recover block" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testException = new RuntimeException("Test error")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.failed(testException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        val result = connector.createReturn(completePrelimReturn).failed.futureValue

        result mustBe a[Throwable]
        result.getMessage mustBe "Test error"
      }

      "must call withBody on request builder with correct JSON" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val expectedResult = CreateReturnResult(returnResourceRef = "RRF-2024-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createReturn(completePrelimReturn).futureValue

        verify(mockRequestBuilder, times(1)).withBody(any())(any(), any(), any())
      }

      "must call execute method on request builder" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val expectedResult = CreateReturnResult(returnResourceRef = "RRF-2024-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createReturn(completePrelimReturn).futureValue

        verify(mockRequestBuilder, times(1)).execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any())
      }

      "must handle minimal PrelimReturn" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val expectedResult = CreateReturnResult(returnResourceRef = "RRF-2024-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.createReturn(minimalPrelimReturn).futureValue

        result mustBe expectedResult
      }

      "must handle timeout exception" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val timeoutException = new java.util.concurrent.TimeoutException("Request timeout")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.failed(timeoutException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createReturn(completePrelimReturn).failed) { exception =>
          exception mustBe a[java.util.concurrent.TimeoutException]
          exception.getMessage mustBe "Request timeout"
        }
      }
    }

    "sdltStubUrl" - {

      "must be initialized with correct value from config" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        connector.sdltStubUrl mustBe testStubUrl + "/stamp-duty-land-tax-stub"
      }

      "must use lazy initialization" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        verify(mockConfig, times(0)).baseUrl(any())

        connector.sdltStubUrl

        verify(mockConfig, times(1)).baseUrl("stamp-duty-land-tax-stub")
      }
    }

    "backendUrl" - {

      "must be initialized with correct value from config" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]

        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        connector.backendUrl mustBe testBackendUrl + "/stamp-duty-land-tax"
      }

      "must use lazy initialization" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        verify(mockConfig, times(0)).baseUrl(any())

        connector.backendUrl

        verify(mockConfig, times(1)).baseUrl("stamp-duty-land-tax")
      }
    }

    "activeBase" - {

      "must return stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val expectedResult = CreateReturnResult(returnResourceRef = "RRF-2024-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createReturn(completePrelimReturn).futureValue

        verify(mockConfig, atLeastOnce()).stubBool
      }

      "must return backend URL when stubBool is false" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val expectedResult = CreateReturnResult(returnResourceRef = "RRF-2024-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnResult]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createReturn(completePrelimReturn).futureValue

        verify(mockConfig, atLeastOnce()).stubBool
      }
    }
  }
}