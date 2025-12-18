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

import com.github.tomakehurst.wiremock.client.WireMock.*
import constants.FullReturnConstants
import models.prelimQuestions.PrelimReturn
import models.{CreateReturnResult, FullReturn, GetReturnByRefRequest}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.*
import utils.WireMockHelper

class StampDutyLandTaxConnectorISpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with WireMockHelper {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: FakeRequest[_] = FakeRequest()

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.stamp-duty-land-tax-stub.host" -> "localhost",
        "microservice.services.stamp-duty-land-tax-stub.port" -> server.port(),
        "microservice.services.stamp-duty-land-tax-stub.protocol" -> "http",
        "microservice.services.stamp-duty-land-tax.host" -> "localhost",
        "microservice.services.stamp-duty-land-tax.port" -> server.port(),
        "microservice.services.stamp-duty-land-tax.protocol" -> "http",
        "backend-feature-stub" -> true
      )
      .build()

  private lazy val connector = app.injector.instanceOf[StampDutyLandTaxConnector]

  private val testReturnId = "123456"
  private val testStorn = "TESTSTORN"

  private val testGetReturnByRefRequest = GetReturnByRefRequest(
    returnResourceRef = testReturnId,
    storn = testStorn
  )

  private val fullReturnJson: JsValue = Json.toJson(FullReturnConstants.completeFullReturn)

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

  private val createReturnResultJson: JsValue = Json.obj(
    "stornId" -> "STORN123456",
    "returnResourceRef" -> "RRF-2024-001"
  )

  "StampDutyLandTaxConnector Integration Tests" - {

    "getFullReturn()" - {

      "must return FullReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(fullReturnJson.toString())
            )
        )

        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe a[FullReturn]
        result.stornId mustBe "STORN123456"

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
        )
      }

      "must send correct request body with returnResourceRef and storn" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(fullReturnJson.toString())
            )
        )

        connector.getFullReturn(testGetReturnByRefRequest).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo(testReturnId)))
            .withRequestBody(matchingJsonPath("$.storn", equalTo(testStorn)))
        )
      }

      "must handle different GetReturnByRefRequest values" in {
        val differentRequests = List(
          GetReturnByRefRequest("ABC-123", "STORN-A"),
          GetReturnByRefRequest("TEST-789", "STORN-B"),
          GetReturnByRefRequest("12345", "STORN-C")
        )

        differentRequests.foreach { request =>
          server.stubFor(
            post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(fullReturnJson.toString())
              )
          )

          val result = connector.getFullReturn(request).futureValue

          result mustBe a[FullReturn]

          server.verify(
            postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
              .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo(request.returnResourceRef)))
              .withRequestBody(matchingJsonPath("$.storn", equalTo(request.storn)))
          )
        }
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody("Not Found")
            )
        )

        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 502 Bad Gateway" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(502)
                .withBody("Bad Gateway")
            )
        )

        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 502
      }

      "must throw UpstreamErrorResponse when stub returns 503 Service Unavailable" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(503)
                .withBody("Service Unavailable")
            )
        )

        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 503
      }

      "must include correct headers in the request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(fullReturnJson.toString())
            )
        )

        connector.getFullReturn(testGetReturnByRefRequest).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .withHeader("Content-Type", containing("application/json"))
        )
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)
            )
        )

        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue

        result mustBe a[Throwable]
      }

      "must correctly parse JSON response into FullReturn model" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(fullReturnJson.toString())
            )
        )

        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe a[FullReturn]
        result.stornId mustBe "STORN123456"
        result.returnResourceRef mustBe "RRF-2024-001"
      }

      "must handle malformed JSON response" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{invalid json}")
            )
        )

        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue

        result mustBe a[Throwable]
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(fullReturnJson.toString())
            )
        )

        connector.getFullReturn(testGetReturnByRefRequest).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
        )
      }

      "must not make multiple requests for a single call" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(fullReturnJson.toString())
            )
        )

        connector.getFullReturn(testGetReturnByRefRequest).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
        )
      }

      "must use stub URL when stubBool is true" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(fullReturnJson.toString())
            )
        )

        connector.getFullReturn(testGetReturnByRefRequest).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
        )
      }

      "must handle minimal FullReturn response" in {
        val minimalFullReturnJson = Json.toJson(FullReturnConstants.minimalFullReturn)

        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(minimalFullReturnJson.toString())
            )
        )

        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe a[FullReturn]
      }

      "must handle empty FullReturn response" in {
        val emptyFullReturnJson = Json.toJson(FullReturnConstants.emptyFullReturn)

        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/receive/full-return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(emptyFullReturnJson.toString())
            )
        )

        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue

        result mustBe a[FullReturn]
      }
    }

    "createReturn()" - {

      "must return CreateReturnResult when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createReturnResultJson.toString())
            )
        )

        val result = connector.createReturn(completePrelimReturn).futureValue

        result mustBe a[CreateReturnResult]
        result.returnResourceRef mustBe "RRF-2024-001"

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
        )
      }

      "must send correct PrelimReturn in request body" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createReturnResultJson.toString())
            )
        )

        connector.createReturn(completePrelimReturn).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .withRequestBody(containing("purchaserIsCompany"))
        )
      }

      "must handle minimal PrelimReturn" in {

        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createReturnResultJson.toString())
            )
        )

        val result = connector.createReturn(minimalPrelimReturn).futureValue

        result mustBe a[CreateReturnResult]

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val result = connector.createReturn(completePrelimReturn).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody("Not Found")
            )
        )

        val result = connector.createReturn(completePrelimReturn).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val result = connector.createReturn(completePrelimReturn).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 502 Bad Gateway" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(502)
                .withBody("Bad Gateway")
            )
        )

        val result = connector.createReturn(completePrelimReturn).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 502
      }

      "must throw UpstreamErrorResponse when stub returns 503 Service Unavailable" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(503)
                .withBody("Service Unavailable")
            )
        )

        val result = connector.createReturn(completePrelimReturn).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 503
      }

      "must include correct headers in the request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createReturnResultJson.toString())
            )
        )

        connector.createReturn(completePrelimReturn).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .withHeader("Content-Type", containing("application/json"))
        )
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)
            )
        )

        val result = connector.createReturn(completePrelimReturn).failed.futureValue

        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{invalid json}")
            )
        )

        val result = connector.createReturn(completePrelimReturn).failed.futureValue

        result mustBe a[Throwable]
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createReturnResultJson.toString())
            )
        )

        connector.createReturn(completePrelimReturn).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
        )
      }

      "must not make multiple requests for a single call" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createReturnResultJson.toString())
            )
        )

        connector.createReturn(completePrelimReturn).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
        )
      }

      "must use stub URL when stubBool is true" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createReturnResultJson.toString())
            )
        )

        connector.createReturn(completePrelimReturn).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
        )
      }

      "must handle timeout exception" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return"))
            .willReturn(
              aResponse()
                .withFixedDelay(10000)
                .withStatus(200)
            )
        )

        val result = connector.createReturn(completePrelimReturn).failed.futureValue

        result mustBe a[Throwable]
      }
    }
  }
}