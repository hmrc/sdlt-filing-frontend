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
import models._
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
    
    "createVendor()" - {

      val createVendorRequestJson = Json.obj(
        "stornId" -> "STORN12345",
        "returnResourceRef" -> "RRF-2024-001",
        "title" -> "Mr",
        "forename1" -> "John",
        "name" -> "Smith",
        "addressLine1" -> "Main Street",
        "isRepresentedByAgent" -> "YES"
      )

      val createVendorReturnJson = Json.obj(
        "vendorResourceRef" -> "VRF-001",
        "vendorId" -> "VID-001"
      )

      "must return CreateVendorReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/vendor"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createVendorReturnJson.toString())
            )
        )

        val request = createVendorRequestJson.as[vendor.CreateVendorRequest]
        val result = connector.createVendor(request).futureValue

        result.vendorResourceRef mustBe "VRF-001"
        result.vendorId mustBe "VID-001"

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/vendor"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/vendor"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = createVendorRequestJson.as[vendor.CreateVendorRequest]
        val result = connector.createVendor(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/vendor"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = createVendorRequestJson.as[vendor.CreateVendorRequest]
        val result = connector.createVendor(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/vendor"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createVendorReturnJson.toString())
            )
        )

        val request = createVendorRequestJson.as[vendor.CreateVendorRequest]
        connector.createVendor(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/vendor"))
        )
      }

      "must include correct headers in the request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/vendor"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createVendorReturnJson.toString())
            )
        )

        val request = createVendorRequestJson.as[vendor.CreateVendorRequest]
        connector.createVendor(request).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/vendor"))
            .withHeader("Content-Type", containing("application/json"))
        )
      }
    }

    "updateVendor()" - {

      val updateVendorRequestJson = Json.obj(
        "stornId" -> "STORN12345",
        "returnResourceRef" -> "RRF-2024-001",
        "vendorResourceRef" -> "VRF-001",
        "name" -> "Smith Updated",
        "addressLine1" -> "Main Street",
        "isRepresentedByAgent" -> "YES"
      )

      val updateVendorReturnJson = Json.obj("updated" -> true)

      "must return UpdateVendorReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/vendor"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateVendorReturnJson.toString())
            )
        )

        val request = updateVendorRequestJson.as[vendor.UpdateVendorRequest]
        val result = connector.updateVendor(request).futureValue

        result.updated mustBe true

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/vendor"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/vendor"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = updateVendorRequestJson.as[vendor.UpdateVendorRequest]
        val result = connector.updateVendor(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/vendor"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody("Not Found")
            )
        )

        val request = updateVendorRequestJson.as[vendor.UpdateVendorRequest]
        val result = connector.updateVendor(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/vendor"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = updateVendorRequestJson.as[vendor.UpdateVendorRequest]
        val result = connector.updateVendor(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/vendor"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateVendorReturnJson.toString())
            )
        )

        val request = updateVendorRequestJson.as[vendor.UpdateVendorRequest]
        connector.updateVendor(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/vendor"))
        )
      }
    }

    "deleteVendor()" - {

      val deleteVendorRequestJson = Json.obj(
        "storn" -> "STORN12345",
        "vendorResourceRef" -> "VRF-001",
        "returnResourceRef" -> "VID-001"
      )

      val deleteVendorReturnJson = Json.obj("deleted" -> true)

      "must return DeleteVendorReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/vendor"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(deleteVendorReturnJson.toString())
            )
        )

        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        val result = connector.deleteVendor(request).futureValue

        result.deleted mustBe true

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/vendor"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/vendor"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        val result = connector.deleteVendor(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/vendor"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody("Not Found")
            )
        )

        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        val result = connector.deleteVendor(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/vendor"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        val result = connector.deleteVendor(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/vendor"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(deleteVendorReturnJson.toString())
            )
        )

        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        connector.deleteVendor(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/vendor"))
        )
      }

      "must handle malformed JSON response" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/vendor"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{invalid json}")
            )
        )

        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        val result = connector.deleteVendor(request).failed.futureValue

        result mustBe a[Throwable]
      }
    }

    "createReturnAgent()" - {

      val createReturnAgentRequestJson = Json.obj(
        "stornId" -> "STORN12345",
        "returnResourceRef" -> "RRF-2024-001",
        "agentType" -> "SOLICITOR",
        "name" -> "Agent Company Ltd",
        "addressLine1" -> "Agent Street",
        "postcode" -> "AG1 2NT"
      )

      val createReturnAgentReturnJson = Json.obj(
        "returnAgentId" -> "RAID-001"
      )

      "must return CreateReturnAgentReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createReturnAgentReturnJson.toString())
            )
        )

        val request = createReturnAgentRequestJson.as[CreateReturnAgentRequest]
        val result = connector.createReturnAgent(request).futureValue

        result.returnAgentId mustBe "RAID-001"

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return-agent"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = createReturnAgentRequestJson.as[CreateReturnAgentRequest]
        val result = connector.createReturnAgent(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = createReturnAgentRequestJson.as[CreateReturnAgentRequest]
        val result = connector.createReturnAgent(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createReturnAgentReturnJson.toString())
            )
        )

        val request = createReturnAgentRequestJson.as[CreateReturnAgentRequest]
        connector.createReturnAgent(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return-agent"))
        )
      }

      "must send correct request body with agentType and required fields" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createReturnAgentReturnJson.toString())
            )
        )

        val request = createReturnAgentRequestJson.as[CreateReturnAgentRequest]
        connector.createReturnAgent(request).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/return-agent"))
            .withRequestBody(matchingJsonPath("$.agentType", equalTo("SOLICITOR")))
            .withRequestBody(matchingJsonPath("$.name", equalTo("Agent Company Ltd")))
            .withRequestBody(matchingJsonPath("$.postcode", equalTo("AG1 2NT")))
        )
      }
    }

    "updateReturnAgent()" - {

      val updateReturnAgentRequestJson = Json.obj(
        "stornId" -> "STORN12345",
        "returnResourceRef" -> "RRF-2024-001",
        "agentType" -> "SOLICITOR",
        "name" -> "Updated Agent Company Ltd",
        "addressLine1" -> "Agent Street",
        "postcode" -> "AG1 2NT"
      )

      val updateReturnAgentReturnJson = Json.obj("updated" -> true)

      "must return UpdateReturnAgentReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateReturnAgentReturnJson.toString())
            )
        )

        val request = updateReturnAgentRequestJson.as[UpdateReturnAgentRequest]
        val result = connector.updateReturnAgent(request).futureValue

        result.updated mustBe true

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-agent"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = updateReturnAgentRequestJson.as[UpdateReturnAgentRequest]
        val result = connector.updateReturnAgent(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody("Not Found")
            )
        )

        val request = updateReturnAgentRequestJson.as[UpdateReturnAgentRequest]
        val result = connector.updateReturnAgent(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = updateReturnAgentRequestJson.as[UpdateReturnAgentRequest]
        val result = connector.updateReturnAgent(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateReturnAgentReturnJson.toString())
            )
        )

        val request = updateReturnAgentRequestJson.as[UpdateReturnAgentRequest]
        connector.updateReturnAgent(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-agent"))
        )
      }
    }

    "deleteReturnAgent()" - {

      val deleteReturnAgentRequestJson = Json.obj(
        "storn" -> "STORN12345",
        "returnResourceRef" -> "RRF-2024-001",
        "agentType" -> "SOLICITOR"
      )

      val deleteReturnAgentReturnJson = Json.obj("deleted" -> true)

      "must return DeleteReturnAgentReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(deleteReturnAgentReturnJson.toString())
            )
        )

        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        val result = connector.deleteReturnAgent(request).futureValue

        result.deleted mustBe true

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/return-agent"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        val result = connector.deleteReturnAgent(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody("Not Found")
            )
        )

        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        val result = connector.deleteReturnAgent(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        val result = connector.deleteReturnAgent(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(deleteReturnAgentReturnJson.toString())
            )
        )

        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        connector.deleteReturnAgent(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/return-agent"))
        )
      }

      "must send correct request body with agentType field" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/return-agent"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(deleteReturnAgentReturnJson.toString())
            )
        )

        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        connector.deleteReturnAgent(request).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/return-agent"))
            .withRequestBody(matchingJsonPath("$.agentType", equalTo("SOLICITOR")))
        )
      }
    }

    "createPurchaser()" - {

      val createPurchaserRequestJson = Json.obj(
        "stornId" -> "STORN12345",
        "returnResourceRef" -> "RRF-2024-001",
        "isCompany" -> "NO",
        "isTrustee" -> "NO",
        "isConnectedToVendor" -> "NO",
        "isRepresentedByAgent" -> "YES",
        "title" -> "Mr",
        "surname" -> "Jones",
        "forename1" -> "David",
        "address1" -> "Park Avenue"
      )

      val createPurchaserReturnJson = Json.obj(
        "purchaserResourceRef" -> "PRF-001",
        "purchaserId" -> "PID-001"
      )

      "must return CreatePurchaserReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createPurchaserReturnJson.toString())
            )
        )

        val request = createPurchaserRequestJson.as[purchaser.CreatePurchaserRequest]
        val result = connector.createPurchaser(request).futureValue

        result.purchaserResourceRef mustBe "PRF-001"
        result.purchaserId mustBe "PID-001"

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/purchaser"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = createPurchaserRequestJson.as[purchaser.CreatePurchaserRequest]
        val result = connector.createPurchaser(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = createPurchaserRequestJson.as[purchaser.CreatePurchaserRequest]
        val result = connector.createPurchaser(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createPurchaserReturnJson.toString())
            )
        )

        val request = createPurchaserRequestJson.as[purchaser.CreatePurchaserRequest]
        connector.createPurchaser(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/purchaser"))
        )
      }
    }

    "updatePurchaser()" - {

      val updatePurchaserRequestJson = Json.obj(
        "stornId" -> "STORN12345",
        "returnResourceRef" -> "RRF-2024-001",
        "purchaserResourceRef" -> "PRF-001",
        "isCompany" -> "NO",
        "isTrustee" -> "NO",
        "isConnectedToVendor" -> "NO",
        "isRepresentedByAgent" -> "YES",
        "address1" -> "Park Avenue"
      )

      val updatePurchaserReturnJson = Json.obj("updated" -> true)

      "must return UpdatePurchaserReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updatePurchaserReturnJson.toString())
            )
        )

        val request = updatePurchaserRequestJson.as[purchaser.UpdatePurchaserRequest]
        val result = connector.updatePurchaser(request).futureValue

        result.updated mustBe true

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/purchaser"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = updatePurchaserRequestJson.as[purchaser.UpdatePurchaserRequest]
        val result = connector.updatePurchaser(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = updatePurchaserRequestJson.as[purchaser.UpdatePurchaserRequest]
        val result = connector.updatePurchaser(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updatePurchaserReturnJson.toString())
            )
        )

        val request = updatePurchaserRequestJson.as[purchaser.UpdatePurchaserRequest]
        connector.updatePurchaser(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/purchaser"))
        )
      }
    }

    "deletePurchaser()" - {

      val deletePurchaserRequestJson = Json.obj(
        "storn" -> "STORN12345",
        "purchaserResourceRef" -> "PUR001",
        "returnResourceRef" -> "RRF-2024-001"
      )

      val deletePurchaserReturnJson = Json.obj("deleted" -> true)

      "must return DeletePurchaserReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(deletePurchaserReturnJson.toString())
            )
        )

        val request = deletePurchaserRequestJson.as[purchaser.DeletePurchaserRequest]
        val result = connector.deletePurchaser(request).futureValue

        result.deleted mustBe true

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/purchaser"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = deletePurchaserRequestJson.as[purchaser.DeletePurchaserRequest]
        val result = connector.deletePurchaser(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody("Not Found")
            )
        )

        val request = deletePurchaserRequestJson.as[purchaser.DeletePurchaserRequest]
        val result = connector.deletePurchaser(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = deletePurchaserRequestJson.as[purchaser.DeletePurchaserRequest]
        val result = connector.deletePurchaser(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/purchaser"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(deletePurchaserReturnJson.toString())
            )
        )

        val request = deletePurchaserRequestJson.as[purchaser.DeletePurchaserRequest]
        connector.deletePurchaser(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/purchaser"))
        )
      }
    }

    "createCompanyDetails()" - {

      val createCompanyDetailsRequestJson = Json.obj(
        "stornId" -> "STORN12345",
        "returnResourceRef" -> "RRF-2024-001",
        "purchaserResourceRef" -> "PRF-001",
        "utr" -> "1234567890",
        "vatReference" -> "GB123456789"
      )

      val createCompanyDetailsReturnJson = Json.obj("companyDetailsId" -> "CID-001")

      "must return CreateCompanyDetailsReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/company-details"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createCompanyDetailsReturnJson.toString())
            )
        )

        val request = createCompanyDetailsRequestJson.as[purchaser.CreateCompanyDetailsRequest]
        val result = connector.createCompanyDetails(request).futureValue

        result.companyDetailsId mustBe "CID-001"

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/company-details"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/company-details"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = createCompanyDetailsRequestJson.as[purchaser.CreateCompanyDetailsRequest]
        val result = connector.createCompanyDetails(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/company-details"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = createCompanyDetailsRequestJson.as[purchaser.CreateCompanyDetailsRequest]
        val result = connector.createCompanyDetails(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/company-details"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createCompanyDetailsReturnJson.toString())
            )
        )

        val request = createCompanyDetailsRequestJson.as[purchaser.CreateCompanyDetailsRequest]
        connector.createCompanyDetails(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/create/company-details"))
        )
      }
    }

    "updateCompanyDetails()" - {

      val updateCompanyDetailsRequestJson = Json.obj(
        "stornId" -> "STORN12345",
        "returnResourceRef" -> "RRF-2024-001",
        "purchaserResourceRef" -> "PRF-001",
        "utr" -> "9876543210"
      )

      val updateCompanyDetailsReturnJson = Json.obj("updated" -> true)

      "must return UpdateCompanyDetailsReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/company-details"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateCompanyDetailsReturnJson.toString())
            )
        )

        val request = updateCompanyDetailsRequestJson.as[purchaser.UpdateCompanyDetailsRequest]
        val result = connector.updateCompanyDetails(request).futureValue

        result.updated mustBe true

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/company-details"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/company-details"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = updateCompanyDetailsRequestJson.as[purchaser.UpdateCompanyDetailsRequest]
        val result = connector.updateCompanyDetails(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/company-details"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = updateCompanyDetailsRequestJson.as[purchaser.UpdateCompanyDetailsRequest]
        val result = connector.updateCompanyDetails(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/company-details"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateCompanyDetailsReturnJson.toString())
            )
        )

        val request = updateCompanyDetailsRequestJson.as[purchaser.UpdateCompanyDetailsRequest]
        connector.updateCompanyDetails(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/company-details"))
        )
      }
    }

    "deleteCompanyDetails()" - {

      val deleteCompanyDetailsRequestJson = Json.obj(
        "storn" -> "STORN12345",
        "returnResourceRef" -> "RRF-2024-001"
      )

      val deleteCompanyDetailsReturnJson = Json.obj("deleted" -> true)

      "must return DeleteCompanyDetailsReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/company-details"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(deleteCompanyDetailsReturnJson.toString())
            )
        )

        val request = deleteCompanyDetailsRequestJson.as[purchaser.DeleteCompanyDetailsRequest]
        val result = connector.deleteCompanyDetails(request).futureValue

        result.deleted mustBe true

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/company-details"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/company-details"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = deleteCompanyDetailsRequestJson.as[purchaser.DeleteCompanyDetailsRequest]
        val result = connector.deleteCompanyDetails(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/company-details"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody("Not Found")
            )
        )

        val request = deleteCompanyDetailsRequestJson.as[purchaser.DeleteCompanyDetailsRequest]
        val result = connector.deleteCompanyDetails(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/company-details"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = deleteCompanyDetailsRequestJson.as[purchaser.DeleteCompanyDetailsRequest]
        val result = connector.deleteCompanyDetails(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/company-details"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(deleteCompanyDetailsReturnJson.toString())
            )
        )

        val request = deleteCompanyDetailsRequestJson.as[purchaser.DeleteCompanyDetailsRequest]
        connector.deleteCompanyDetails(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/delete/company-details"))
        )
      }
    }

    "updateReturnInfo()" - {

      val updateReturnInfoRequestJson = Json.obj(
        "returnResourceRef" -> "RRF-2024-001",
        "storn" -> "STORN12345",
        "mainPurchaserID" -> "PUR-001",
        "mainVendorID" -> "VEN-001",
        "mainLandID" -> "LAND-001"
      )

      val updateReturnInfoReturnJson = Json.obj("updated" -> true)

      "must return ReturnInfoReturn when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateReturnInfoReturnJson.toString())
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).futureValue

        result.updated mustBe true

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
        )
      }

      "must return ReturnInfoReturn with minimal request data" in {
        val minimalRequestJson = Json.obj(
          "returnResourceRef" -> "RRF-2024-001",
          "storn" -> "STORN12345"
        )

        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateReturnInfoReturnJson.toString())
            )
        )

        val request = minimalRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).futureValue

        result.updated mustBe true

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
        )
      }

      "must send correct request body with mainPurchaserID and other fields" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateReturnInfoReturnJson.toString())
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        connector.updateReturnInfo(request).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo("RRF-2024-001")))
            .withRequestBody(matchingJsonPath("$.storn", equalTo("STORN12345")))
            .withRequestBody(matchingJsonPath("$.mainPurchaserID", equalTo("PUR-001")))
            .withRequestBody(matchingJsonPath("$.mainVendorID", equalTo("VEN-001")))
            .withRequestBody(matchingJsonPath("$.mainLandID", equalTo("LAND-001")))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody("Not Found")
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must throw UpstreamErrorResponse when stub returns 503 Service Unavailable" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(503)
                .withBody("Service Unavailable")
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 503
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateReturnInfoReturnJson.toString())
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        connector.updateReturnInfo(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
        )
      }

      "must not make multiple requests for a single call" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateReturnInfoReturnJson.toString())
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        connector.updateReturnInfo(request).futureValue

        server.verify(
          1,
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
        )
      }

      "must include correct headers in the request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateReturnInfoReturnJson.toString())
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        connector.updateReturnInfo(request).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .withHeader("Content-Type", containing("application/json"))
        )
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue

        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{invalid json}")
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue

        result mustBe a[Throwable]
      }

      "must handle timeout exception" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withFixedDelay(10000)
                .withStatus(200)
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue

        result mustBe a[Throwable]
      }

      "must handle request with all optional fields populated" in {
        val completeRequestJson = Json.obj(
          "returnResourceRef" -> "RRF-2024-001",
          "storn" -> "STORN12345",
          "mainPurchaserID" -> "PUR-001",
          "mainVendorID" -> "VEN-001",
          "mainLandID" -> "LAND-001",
          "IRMarkGenerated" -> "YES",
          "landCertForEachProp" -> "NO",
          "declaration" -> "YES"
        )

        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateReturnInfoReturnJson.toString())
            )
        )

        val request = completeRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).futureValue

        result.updated mustBe true

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .withRequestBody(matchingJsonPath("$.IRMarkGenerated", equalTo("YES")))
            .withRequestBody(matchingJsonPath("$.landCertForEachProp", equalTo("NO")))
            .withRequestBody(matchingJsonPath("$.declaration", equalTo("YES")))
        )
      }

      "must use stub URL when stubBool is true" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(updateReturnInfoReturnJson.toString())
            )
        )

        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        connector.updateReturnInfo(request).futureValue

        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/filing/update/return-info"))
        )
      }
    }

  }
}