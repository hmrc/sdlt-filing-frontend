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
import models.*
import models.prelimQuestions.PrelimReturn
import models.purchaserAgent.SdltOrganisationResponse
import models.vendor.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.net.URL
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
        result.stornId mustBe "STORN123456"
        result.returnResourceRef mustBe "RRF-2024-001"
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
        result.returnInfo mustBe defined
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
        result.returnInfo must not be defined
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
        result.returnInfo must not be defined
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

    "createVendor" - {

      "must return CreateVendorReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateVendorRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          title = Some("Mr"),
          forename1 = Some("John"),
          forename2 = Some("Paul"),
          name = "Smith",
          houseNumber = Some("10"),
          addressLine1 = "Main Street",
          addressLine2 = Some("Apt 5"),
          addressLine3 = Some("Building A"),
          addressLine4 = Some("District B"),
          postcode = Some("TE23 5TT"),
          isRepresentedByAgent = "YES"
        )
        val expectedResult = CreateVendorReturn(vendorResourceRef = "VRF-001", vendorId = "VID-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateVendorReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.createVendor(testRequest).futureValue

        result mustBe expectedResult
        result.vendorResourceRef mustBe "VRF-001"
        result.vendorId mustBe "VID-001"
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateVendorRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          name = "Smith",
          addressLine1 = "Main Street",
          isRepresentedByAgent = "YES"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateVendorReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createVendor(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateVendorRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          name = "Smith",
          addressLine1 = "Main Street",
          isRepresentedByAgent = "YES"
        )
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateVendorReturn]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createVendor(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateVendorRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          name = "Smith",
          addressLine1 = "Main Street",
          isRepresentedByAgent = "YES"
        )
        val expectedResult = CreateVendorReturn(vendorResourceRef = "VRF-001", vendorId = "VID-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateVendorReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createVendor(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val testRequest = CreateVendorRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          name = "Smith",
          addressLine1 = "Main Street",
          isRepresentedByAgent = "YES"
        )
        val expectedResult = CreateVendorReturn(vendorResourceRef = "VRF-001", vendorId = "VID-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateVendorReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createVendor(testRequest)(customHc, request).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "updateVendor" - {

      "must return UpdateVendorReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = UpdateVendorRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          title = Some("Mr"),
          forename1 = Some("John"),
          forename2 = Some("Paul"),
          name = "Smith",
          houseNumber = Some("10"),
          addressLine1 = "Main Street",
          addressLine2 = Some("Apt 5"),
          addressLine3 = Some("Building A"),
          addressLine4 = Some("District B"),
          postcode = Some("TE23 5TT"),
          isRepresentedByAgent = "YES",
          vendorResourceRef = "VRF-001",
          nextVendorId = Some("VID-002")
        )
        val expectedResult = UpdateVendorReturn(updated = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, UpdateVendorReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.updateVendor(testRequest).futureValue

        result mustBe expectedResult
        result.updated mustBe true
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = UpdateVendorRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          name = "Smith",
          addressLine1 = "Main Street",
          isRepresentedByAgent = "YES",
          vendorResourceRef = "VRF-001"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Not Found", 404)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, UpdateVendorReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.updateVendor(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must use backend URL when stubBool is false" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = UpdateVendorRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          name = "Smith",
          addressLine1 = "Main Street",
          isRepresentedByAgent = "YES",
          vendorResourceRef = "VRF-001"
        )
        val expectedResult = UpdateVendorReturn(updated = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, UpdateVendorReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.updateVendor(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "deleteVendor" - {

      "must return DeleteVendorReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = DeleteVendorRequest(
          storn = "12345",
          vendorResourceRef = "VRF-001",
          returnResourceRef = "RRF-2024-001"
        )
        val expectedResult = DeleteVendorReturn(deleted = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, DeleteVendorReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.deleteVendor(testRequest).futureValue

        result mustBe expectedResult
        result.deleted mustBe true
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = DeleteVendorRequest(
          storn = "12345",
          vendorResourceRef = "VRF-001",
          returnResourceRef = "RRF-2024-001"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Internal Server Error", 500)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, DeleteVendorReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.deleteVendor(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = DeleteVendorRequest(
          storn = "12345",
          vendorResourceRef = "VRF-001",
          returnResourceRef = "RRF-2024-001"
        )
        val expectedResult = DeleteVendorReturn(deleted = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, DeleteVendorReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.deleteVendor(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "createReturnAgent" - {

      "must return CreateReturnAgentReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateReturnAgentRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR",
          name = "Smith & Partners",
          houseNumber = Some(10),
          addressLine1 = "Main Street",
          addressLine2 = Some("Suite 5"),
          addressLine3 = Some("Building A"),
          addressLine4 = Some("District B"),
          postcode = "TE23 5TT",
          phoneNumber = Some("01234567890"),
          email = Some("agent@example.com"),
          agentReference = Some("AGT-001"),
          isAuthorised = Some("YES")
        )
        val expectedResult = CreateReturnAgentReturn(returnAgentId = "AGID-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnAgentReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.createReturnAgent(testRequest).futureValue

        result mustBe expectedResult
        result.returnAgentId mustBe "AGID-001"
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateReturnAgentRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR",
          name = "Smith & Partners",
          addressLine1 = "Main Street",
          postcode = "TE23 5TT"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnAgentReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createReturnAgent(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateReturnAgentRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR",
          name = "Smith & Partners",
          addressLine1 = "Main Street",
          postcode = "TE23 5TT"
        )
        val expectedResult = CreateReturnAgentReturn(returnAgentId = "AGID-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnAgentReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createReturnAgent(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateReturnAgentRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR",
          name = "Smith & Partners",
          addressLine1 = "Main Street",
          postcode = "TE23 5TT"
        )
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateReturnAgentReturn]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createReturnAgent(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }
    }

    "updateReturnAgent" - {

      "must return UpdateReturnAgentReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = UpdateReturnAgentRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR",
          name = "Smith & Partners Updated",
          houseNumber = Some(10),
          addressLine1 = "Main Street",
          addressLine2 = Some("Suite 5"),
          addressLine3 = Some("Building A"),
          addressLine4 = Some("District B"),
          postcode = "TE23 5TT",
          phoneNumber = Some("01234567890"),
          email = Some("agent@example.com"),
          agentReference = Some("AGT-001"),
          isAuthorised = Some("YES")
        )
        val expectedResult = UpdateReturnAgentReturn(updated = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, UpdateReturnAgentReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.updateReturnAgent(testRequest).futureValue

        result mustBe expectedResult
        result.updated mustBe true
      }

      "must handle Left response with UpstreamErrorResponse (404)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = UpdateReturnAgentRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR",
          name = "Smith & Partners",
          addressLine1 = "Main Street",
          postcode = "TE23 5TT"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Not Found", 404)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, UpdateReturnAgentReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.updateReturnAgent(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = UpdateReturnAgentRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR",
          name = "Smith & Partners",
          addressLine1 = "Main Street",
          postcode = "TE23 5TT"
        )
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, UpdateReturnAgentReturn]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.updateReturnAgent(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must use backend URL when stubBool is false" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = UpdateReturnAgentRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR",
          name = "Smith & Partners",
          addressLine1 = "Main Street",
          postcode = "TE23 5TT"
        )
        val expectedResult = UpdateReturnAgentReturn(updated = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, UpdateReturnAgentReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.updateReturnAgent(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "deleteReturnAgent" - {

      "must return DeleteReturnAgentReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = DeleteReturnAgentRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR"
        )
        val expectedResult = DeleteReturnAgentReturn(deleted = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, DeleteReturnAgentReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.deleteReturnAgent(testRequest).futureValue

        result mustBe expectedResult
        result.deleted mustBe true
      }

      "must handle Left response with UpstreamErrorResponse (500)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = DeleteReturnAgentRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Internal Server Error", 500)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, DeleteReturnAgentReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.deleteReturnAgent(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val testRequest = DeleteReturnAgentRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR"
        )
        val expectedResult = DeleteReturnAgentReturn(deleted = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, DeleteReturnAgentReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.deleteReturnAgent(testRequest)(customHc, request).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = DeleteReturnAgentRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          agentType = "SOLICITOR"
        )
        val expectedResult = DeleteReturnAgentReturn(deleted = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, DeleteReturnAgentReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.deleteReturnAgent(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "updateReturnVersion" - {

      "must return ReturnVersionUpdateReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = ReturnVersionUpdateRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          currentVersion = "1.0"
        )
        val expectedResult = ReturnVersionUpdateReturn(newVersion = Some(1))

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, ReturnVersionUpdateReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.updateReturnVersion(testRequest).futureValue

        result mustBe expectedResult
        result.newVersion must contain(1)
      }

      "must handle Left response with UpstreamErrorResponse (400)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = ReturnVersionUpdateRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          currentVersion = "1.0"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, ReturnVersionUpdateReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.updateReturnVersion(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle Left response with UpstreamErrorResponse (404)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = ReturnVersionUpdateRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          currentVersion = "1.0"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Not Found", 404)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, ReturnVersionUpdateReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.updateReturnVersion(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle Left response with UpstreamErrorResponse (503)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = ReturnVersionUpdateRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          currentVersion = "1.0"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Service Unavailable", 503)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, ReturnVersionUpdateReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.updateReturnVersion(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions and throw exception" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = ReturnVersionUpdateRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          currentVersion = "1.0"
        )
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, ReturnVersionUpdateReturn]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.updateReturnVersion(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = ReturnVersionUpdateRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          currentVersion = "1.0"
        )
        val expectedResult = ReturnVersionUpdateReturn(newVersion = Some(1))

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, ReturnVersionUpdateReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.updateReturnVersion(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must use backend URL when stubBool is false" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = ReturnVersionUpdateRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          currentVersion = "1.0"
        )
        val expectedResult = ReturnVersionUpdateReturn(newVersion = Some(1))

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, ReturnVersionUpdateReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.updateReturnVersion(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val testRequest = ReturnVersionUpdateRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          currentVersion = "1.0"
        )
        val expectedResult = ReturnVersionUpdateReturn(newVersion = Some(1))

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, ReturnVersionUpdateReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.updateReturnVersion(testRequest)(customHc, request).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must call withBody on request builder with correct JSON" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = ReturnVersionUpdateRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001",
          currentVersion = "1.0"
        )
        val expectedResult = ReturnVersionUpdateReturn(newVersion = Some(1))

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, ReturnVersionUpdateReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.updateReturnVersion(testRequest).futureValue

        verify(mockRequestBuilder, times(1)).withBody(any())(any(), any(), any())
      }
    }
    
    "createPurchaser" - {

      "must return CreatePurchaserReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.CreatePurchaserRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          isCompany = "NO",
          isTrustee = "NO",
          isConnectedToVendor = "NO",
          isRepresentedByAgent = "YES",
          title = Some("Mr"),
          surname = Some("Jones"),
          forename1 = Some("David"),
          address1 = "Park Avenue"
        )
        val expectedResult = models.purchaser.CreatePurchaserReturn(
          purchaserResourceRef = "PRF-001",
          purchaserId = "PID-001"
        )

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.CreatePurchaserReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.createPurchaser(testRequest).futureValue

        result mustBe expectedResult
        result.purchaserResourceRef mustBe "PRF-001"
        result.purchaserId mustBe "PID-001"
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.CreatePurchaserRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          isCompany = "NO",
          isTrustee = "NO",
          isConnectedToVendor = "NO",
          isRepresentedByAgent = "YES",
          address1 = "Park Avenue"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.CreatePurchaserReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createPurchaser(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.CreatePurchaserRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          isCompany = "NO",
          isTrustee = "NO",
          isConnectedToVendor = "NO",
          isRepresentedByAgent = "YES",
          address1 = "Park Avenue"
        )
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.CreatePurchaserReturn]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createPurchaser(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.CreatePurchaserRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          isCompany = "NO",
          isTrustee = "NO",
          isConnectedToVendor = "NO",
          isRepresentedByAgent = "YES",
          address1 = "Park Avenue"
        )
        val expectedResult = models.purchaser.CreatePurchaserReturn(
          purchaserResourceRef = "PRF-001",
          purchaserId = "PID-001"
        )

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.CreatePurchaserReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createPurchaser(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val testRequest = models.purchaser.CreatePurchaserRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          isCompany = "NO",
          isTrustee = "NO",
          isConnectedToVendor = "NO",
          isRepresentedByAgent = "YES",
          address1 = "Park Avenue"
        )
        val expectedResult = models.purchaser.CreatePurchaserReturn(
          purchaserResourceRef = "PRF-001",
          purchaserId = "PID-001"
        )

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.CreatePurchaserReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createPurchaser(testRequest)(customHc, request).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "updatePurchaser" - {

      "must return UpdatePurchaserReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.UpdatePurchaserRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001",
          isCompany = "NO",
          isTrustee = "NO",
          isConnectedToVendor = "NO",
          isRepresentedByAgent = "YES",
          title = Some("Mr"),
          surname = Some("Jones Updated"),
          address1 = "Park Avenue",
          nextPurchaserId = Some("PID-002")
        )
        val expectedResult = models.purchaser.UpdatePurchaserReturn(updated = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.UpdatePurchaserReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.updatePurchaser(testRequest).futureValue

        result mustBe expectedResult
        result.updated mustBe true
      }

      "must handle Left response with UpstreamErrorResponse (404)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.UpdatePurchaserRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001",
          isCompany = "NO",
          isTrustee = "NO",
          isConnectedToVendor = "NO",
          isRepresentedByAgent = "YES",
          address1 = "Park Avenue"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Not Found", 404)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.UpdatePurchaserReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.updatePurchaser(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.UpdatePurchaserRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001",
          isCompany = "NO",
          isTrustee = "NO",
          isConnectedToVendor = "NO",
          isRepresentedByAgent = "YES",
          address1 = "Park Avenue"
        )
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.UpdatePurchaserReturn]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.updatePurchaser(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must use backend URL when stubBool is false" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.UpdatePurchaserRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001",
          isCompany = "NO",
          isTrustee = "NO",
          isConnectedToVendor = "NO",
          isRepresentedByAgent = "YES",
          address1 = "Park Avenue"
        )
        val expectedResult = models.purchaser.UpdatePurchaserReturn(updated = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.UpdatePurchaserReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.updatePurchaser(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "deletePurchaser" - {

      "must return DeletePurchaserReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.DeletePurchaserRequest(
          storn = "12345",
          purchaserResourceRef = "PRF-001",
          returnResourceRef = "RRF-2024-001"
        )
        val expectedResult = models.purchaser.DeletePurchaserReturn(deleted = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.DeletePurchaserReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.deletePurchaser(testRequest).futureValue

        result mustBe expectedResult
        result.deleted mustBe true
      }

      "must handle Left response with UpstreamErrorResponse (500)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.DeletePurchaserRequest(
          storn = "12345",
          purchaserResourceRef = "PRF-001",
          returnResourceRef = "RRF-2024-001"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Internal Server Error", 500)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.DeletePurchaserReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.deletePurchaser(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val testRequest = models.purchaser.DeletePurchaserRequest(
          storn = "12345",
          purchaserResourceRef = "PRF-001",
          returnResourceRef = "RRF-2024-001"
        )
        val expectedResult = models.purchaser.DeletePurchaserReturn(deleted = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.DeletePurchaserReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.deletePurchaser(testRequest)(customHc, request).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.DeletePurchaserRequest(
          storn = "12345",
          purchaserResourceRef = "PRF-001",
          returnResourceRef = "RRF-2024-001"
        )
        val expectedResult = models.purchaser.DeletePurchaserReturn(deleted = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.DeletePurchaserReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.deletePurchaser(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "createCompanyDetails" - {

      "must return CreateCompanyDetailsReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.CreateCompanyDetailsRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001",
          utr = Some("1234567890"),
          vatReference = Some("GB123456789"),
          compTypeBank = Some("YES")
        )
        val expectedResult = models.purchaser.CreateCompanyDetailsReturn(companyDetailsId = "CID-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.CreateCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.createCompanyDetails(testRequest).futureValue

        result mustBe expectedResult
        result.companyDetailsId mustBe "CID-001"
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.CreateCompanyDetailsRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.CreateCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createCompanyDetails(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.CreateCompanyDetailsRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001"
        )
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.CreateCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.createCompanyDetails(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.CreateCompanyDetailsRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001"
        )
        val expectedResult = models.purchaser.CreateCompanyDetailsReturn(companyDetailsId = "CID-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.CreateCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createCompanyDetails(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val testRequest = models.purchaser.CreateCompanyDetailsRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001"
        )
        val expectedResult = models.purchaser.CreateCompanyDetailsReturn(companyDetailsId = "CID-001")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.CreateCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.createCompanyDetails(testRequest)(customHc, request).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "updateCompanyDetails" - {

      "must return UpdateCompanyDetailsReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.UpdateCompanyDetailsRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001",
          utr = Some("9876543210"),
          vatReference = Some("GB987654321"),
          compTypeBuilder = Some("YES")
        )
        val expectedResult = models.purchaser.UpdateCompanyDetailsReturn(updated = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.UpdateCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.updateCompanyDetails(testRequest).futureValue

        result mustBe expectedResult
        result.updated mustBe true
      }

      "must handle Left response with UpstreamErrorResponse (404)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.UpdateCompanyDetailsRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Not Found", 404)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.UpdateCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.updateCompanyDetails(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.UpdateCompanyDetailsRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001"
        )
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.UpdateCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.updateCompanyDetails(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must use backend URL when stubBool is false" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.UpdateCompanyDetailsRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          purchaserResourceRef = "PRF-001"
        )
        val expectedResult = models.purchaser.UpdateCompanyDetailsReturn(updated = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.UpdateCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.updateCompanyDetails(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "deleteCompanyDetails" - {

      "must return DeleteCompanyDetailsReturn when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.DeleteCompanyDetailsRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001"
        )
        val expectedResult = models.purchaser.DeleteCompanyDetailsReturn(deleted = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.DeleteCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.deleteCompanyDetails(testRequest).futureValue

        result mustBe expectedResult
        result.deleted mustBe true
      }

      "must handle Left response with UpstreamErrorResponse (500)" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.DeleteCompanyDetailsRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001"
        )
        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Internal Server Error", 500)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.DeleteCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.deleteCompanyDetails(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val testRequest = models.purchaser.DeleteCompanyDetailsRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001"
        )
        val expectedResult = models.purchaser.DeleteCompanyDetailsReturn(deleted = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.DeleteCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.deleteCompanyDetails(testRequest)(customHc, request).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = models.purchaser.DeleteCompanyDetailsRequest(
          storn = "12345",
          returnResourceRef = "RRF-2024-001"
        )
        val expectedResult = models.purchaser.DeleteCompanyDetailsReturn(deleted = true)

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaser.DeleteCompanyDetailsReturn]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.deleteCompanyDetails(testRequest).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
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

    "getSdltOrganisation" - {

      "must return SdltOrganisationResponse when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val response = models.purchaserAgent.SdltOrganisationResponse(
          storn = testStorn,
          version = None,
          agents = Nil
        )

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(false)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaserAgent.SdltOrganisationResponse]](any(), any()))
          .thenReturn(Future.successful(Right(response)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        val result = connector.getSdltOrganisation(testStorn).futureValue

        result mustBe response
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
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, models.purchaserAgent.SdltOrganisationResponse]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)

        whenReady(connector.getSdltOrganisation(testStorn).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must use stub URL when stubBool is true" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val response = models.purchaserAgent.SdltOrganisationResponse(
          storn = testStorn,
          version = None,
          agents = Nil
        )

        when(mockConfig.baseUrl("stamp-duty-land-tax-stub")).thenReturn(testStubUrl)
        when(mockConfig.baseUrl("stamp-duty-land-tax")).thenReturn(testBackendUrl)
        when(mockConfig.stubBool).thenReturn(true)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, SdltOrganisationResponse]](any(), any()))
          .thenReturn(Future.successful(Right(response)))

        val connector = new StampDutyLandTaxConnector(mockHttpClient, mockConfig)
        connector.getSdltOrganisation(testStorn).futureValue

        val urlCaptor: ArgumentCaptor[URL] = ArgumentCaptor.forClass(classOf[URL])
        verify(mockHttpClient).post(urlCaptor.capture())(any())

        urlCaptor.getValue.toString must startWith(testStubUrl)
      }
    }
  }
}