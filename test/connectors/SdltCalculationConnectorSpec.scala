/*
 * Copyright 2026 HM Revenue & Customs
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
import models.taxCalculation.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

class SdltCalculationConnectorSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier     = HeaderCarrier()
  implicit val ec: ExecutionContext  = scala.concurrent.ExecutionContext.Implicits.global

  private val testCalculationUrl = "http://localhost:10100/calculate"

  private val testRequest = SdltCalculationRequest(
    holdingType         = HoldingTypes.leasehold,
    propertyType        = PropertyTypes.residential,
    effectiveDateDay    = 1,
    effectiveDateMonth  = 2,
    effectiveDateYear   = 2015,
    nonUKResident       = None,
    premium             = BigDecimal(100000),
    highestRent         = BigDecimal(0),
    propertyDetails     = None,
    leaseDetails        = None,
    relevantRentDetails = None,
    firstTimeBuyer      = None,
    isLinked            = None,
    interestTransferred = None,
    taxReliefDetails    = None,
    isMultipleLand      = None
  )

  private val testResult = Result(
    totalTax      = 5000,
    resultHeading = None,
    resultHint    = None,
    npv           = None,
    taxCalcs      = Nil
  )

  "SdltCalculationConnector" - {

    "calculateStampDutyLandTax" - {

      "must return the multiple results when the response contains multiple results" in {
        val mockHttpClient      = mock[HttpClientV2]
        val mockConfig          = mock[FrontendAppConfig]
        val mockRequestBuilder  = mock[RequestBuilder]
        val secondResult        = testResult.copy(totalTax = 9999)
        val response            = CalculationResponse(result = Seq(testResult, secondResult))

        when(mockConfig.sdltCalculationUrl).thenReturn(testCalculationUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[CalculationResponse](any(), any())).thenReturn(Future.successful(response))

        val connector = new SdltCalculationConnector(mockHttpClient, mockConfig)
        val result    = connector.calculateStampDutyLandTax(testRequest).futureValue

        result mustBe CalculationResponse(result = Seq(testResult, secondResult))
      }

      "must return an empty result sequence when the response contains no results" in {
        val mockHttpClient     = mock[HttpClientV2]
        val mockConfig         = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val response           = CalculationResponse(result = Nil)

        when(mockConfig.sdltCalculationUrl).thenReturn(testCalculationUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[CalculationResponse](any(), any())).thenReturn(Future.successful(response))

        val connector = new SdltCalculationConnector(mockHttpClient, mockConfig)
        val result    = connector.calculateStampDutyLandTax(testRequest).futureValue

        result mustBe CalculationResponse(result = Nil)
      }

      "must propagate an UpstreamErrorResponse when the downstream call fails" in {
        val mockHttpClient     = mock[HttpClientV2]
        val mockConfig         = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val upstreamError      = UpstreamErrorResponse("Internal Server Error", 500)

        when(mockConfig.sdltCalculationUrl).thenReturn(testCalculationUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[CalculationResponse](any(), any())).thenReturn(Future.failed(upstreamError))

        val connector = new SdltCalculationConnector(mockHttpClient, mockConfig)

        whenReady(connector.calculateStampDutyLandTax(testRequest).failed) { exception =>
          exception mustBe a[UpstreamErrorResponse]
          exception.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
        }
      }

      "must propagate a general exception when an unexpected error occurs" in {
        val mockHttpClient     = mock[HttpClientV2]
        val mockConfig         = mock[FrontendAppConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val runtimeException   = new RuntimeException("Connection failed")

        when(mockConfig.sdltCalculationUrl).thenReturn(testCalculationUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[CalculationResponse](any(), any())).thenReturn(Future.failed(runtimeException))

        val connector = new SdltCalculationConnector(mockHttpClient, mockConfig)

        whenReady(connector.calculateStampDutyLandTax(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }
    }
  }
}
