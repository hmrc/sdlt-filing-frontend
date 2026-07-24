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
import models.transaction.*

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
          post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(fullReturnJson.toString()))
        )
        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue
        result mustBe a[FullReturn]
        result.stornId mustBe "STORN123456"
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")))
      }

      "must send correct request body with returnResourceRef and storn" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(fullReturnJson.toString()))
        )
        connector.getFullReturn(testGetReturnByRefRequest).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return"))
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
            post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return"))
              .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(fullReturnJson.toString()))
          )
          val result = connector.getFullReturn(request).futureValue
          result mustBe a[FullReturn]
          server.verify(
            postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return"))
              .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo(request.returnResourceRef)))
              .withRequestBody(matchingJsonPath("$.storn", equalTo(request.storn)))
          )
        }
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")))
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")))
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")))
      }

      "must throw UpstreamErrorResponse when stub returns 502 Bad Gateway" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")).willReturn(aResponse().withStatus(502).withBody("Bad Gateway")))
        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 502
      }

      "must throw UpstreamErrorResponse when stub returns 503 Service Unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")).willReturn(aResponse().withStatus(503).withBody("Service Unavailable")))
        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 503
      }

      "must include correct headers in the request" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(fullReturnJson.toString()))
        )
        connector.getFullReturn(testGetReturnByRefRequest).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")).withHeader("Content-Type", containing("application/json")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue
        result mustBe a[Throwable]
      }

      "must correctly parse JSON response into FullReturn model" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(fullReturnJson.toString()))
        )
        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue
        result mustBe a[FullReturn]
        result.stornId mustBe "STORN123456"
        result.returnResourceRef mustBe "RRF-2024-001"
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val result = connector.getFullReturn(testGetReturnByRefRequest).failed.futureValue
        result mustBe a[Throwable]
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(fullReturnJson.toString()))
        )
        connector.getFullReturn(testGetReturnByRefRequest).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")))
      }

      "must not make multiple requests for a single call" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(fullReturnJson.toString()))
        )
        connector.getFullReturn(testGetReturnByRefRequest).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")))
      }

      "must use stub URL when stubBool is true" in {
        server.stubFor(
          post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(fullReturnJson.toString()))
        )
        connector.getFullReturn(testGetReturnByRefRequest).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")))
      }

      "must handle minimal FullReturn response" in {
        val minimalFullReturnJson = Json.toJson(FullReturnConstants.minimalFullReturn)
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(minimalFullReturnJson.toString())))
        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue
        result mustBe a[FullReturn]
      }

      "must handle empty FullReturn response" in {
        val emptyFullReturnJson = Json.toJson(FullReturnConstants.emptyFullReturn)
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/receive/full-return")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(emptyFullReturnJson.toString())))
        val result = connector.getFullReturn(testGetReturnByRefRequest).futureValue
        result mustBe a[FullReturn]
      }
    }
    "createReturn()" - {

      "must return CreateReturnResult when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createReturnResultJson.toString())))
        val result = connector.createReturn(completePrelimReturn).futureValue
        result mustBe a[CreateReturnResult]
        result.returnResourceRef mustBe "RRF-2024-001"
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")))
      }

      "must send correct PrelimReturn in request body" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createReturnResultJson.toString())))
        connector.createReturn(completePrelimReturn).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).withRequestBody(containing("purchaserIsCompany")))
      }

      "must handle minimal PrelimReturn" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createReturnResultJson.toString())))
        val result = connector.createReturn(minimalPrelimReturn).futureValue
        result mustBe a[CreateReturnResult]
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val result = connector.createReturn(completePrelimReturn).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")))
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val result = connector.createReturn(completePrelimReturn).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val result = connector.createReturn(completePrelimReturn).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")))
      }

      "must throw UpstreamErrorResponse when stub returns 502 Bad Gateway" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(502).withBody("Bad Gateway")))
        val result = connector.createReturn(completePrelimReturn).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 502
      }

      "must throw UpstreamErrorResponse when stub returns 503 Service Unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(503).withBody("Service Unavailable")))
        val result = connector.createReturn(completePrelimReturn).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 503
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createReturnResultJson.toString())))
        connector.createReturn(completePrelimReturn).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).withHeader("Content-Type", containing("application/json")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val result = connector.createReturn(completePrelimReturn).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val result = connector.createReturn(completePrelimReturn).failed.futureValue
        result mustBe a[Throwable]
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createReturnResultJson.toString())))
        connector.createReturn(completePrelimReturn).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")))
      }

      "must not make multiple requests for a single call" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createReturnResultJson.toString())))
        connector.createReturn(completePrelimReturn).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")))
      }

      "must use stub URL when stubBool is true" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createReturnResultJson.toString())))
        connector.createReturn(completePrelimReturn).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")))
      }

      "must handle timeout exception" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return")).willReturn(aResponse().withFixedDelay(10000).withStatus(200)))
        val result = connector.createReturn(completePrelimReturn).failed.futureValue
        result mustBe a[Throwable]
      }
    }
    "createVendor()" - {

      val createVendorRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "title" -> "Mr",
        "forename1" -> "John", "name" -> "Smith", "addressLine1" -> "Main Street", "isRepresentedByAgent" -> "YES"
      )
      val createVendorReturnJson = Json.obj("vendorResourceRef" -> "VRF-001", "vendorId" -> "VID-001")

      "must return CreateVendorReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/vendor")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createVendorReturnJson.toString())))
        val request = createVendorRequestJson.as[vendor.CreateVendorRequest]
        val result = connector.createVendor(request).futureValue
        result.vendorResourceRef mustBe "VRF-001"
        result.vendorId mustBe "VID-001"
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/vendor")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/vendor")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = createVendorRequestJson.as[vendor.CreateVendorRequest]
        val result = connector.createVendor(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/vendor")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = createVendorRequestJson.as[vendor.CreateVendorRequest]
        val result = connector.createVendor(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/vendor")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createVendorReturnJson.toString())))
        val request = createVendorRequestJson.as[vendor.CreateVendorRequest]
        connector.createVendor(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/vendor")))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/vendor")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createVendorReturnJson.toString())))
        val request = createVendorRequestJson.as[vendor.CreateVendorRequest]
        connector.createVendor(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/vendor")).withHeader("Content-Type", containing("application/json")))
      }
    }

    "updateVendor()" - {

      val updateVendorRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "vendorResourceRef" -> "VRF-001",
        "name" -> "Smith Updated", "addressLine1" -> "Main Street", "isRepresentedByAgent" -> "YES"
      )
      val updateVendorReturnJson = Json.obj("updated" -> true)

      "must return UpdateVendorReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/vendor")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateVendorReturnJson.toString())))
        val request = updateVendorRequestJson.as[vendor.UpdateVendorRequest]
        val result = connector.updateVendor(request).futureValue
        result.updated mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/vendor")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/vendor")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = updateVendorRequestJson.as[vendor.UpdateVendorRequest]
        val result = connector.updateVendor(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/vendor")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = updateVendorRequestJson.as[vendor.UpdateVendorRequest]
        val result = connector.updateVendor(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/vendor")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = updateVendorRequestJson.as[vendor.UpdateVendorRequest]
        val result = connector.updateVendor(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/vendor")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateVendorReturnJson.toString())))
        val request = updateVendorRequestJson.as[vendor.UpdateVendorRequest]
        connector.updateVendor(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/vendor")))
      }
    }

    "deleteVendor()" - {

      val deleteVendorRequestJson = Json.obj("storn" -> "STORN12345", "vendorResourceRef" -> "VRF-001", "returnResourceRef" -> "VID-001")
      val deleteVendorReturnJson = Json.obj("deleted" -> true)

      "must return DeleteVendorReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/vendor")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteVendorReturnJson.toString())))
        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        val result = connector.deleteVendor(request).futureValue
        result.deleted mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/vendor")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/vendor")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        val result = connector.deleteVendor(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/vendor")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        val result = connector.deleteVendor(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/vendor")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        val result = connector.deleteVendor(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/vendor")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteVendorReturnJson.toString())))
        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        connector.deleteVendor(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/vendor")))
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/vendor")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = deleteVendorRequestJson.as[vendor.DeleteVendorRequest]
        val result = connector.deleteVendor(request).failed.futureValue
        result mustBe a[Throwable]
      }
    }
    "createReturnAgent()" - {

      val createReturnAgentRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "agentType" -> "SOLICITOR",
        "name" -> "Agent Company Ltd", "addressLine1" -> "Agent Street", "postcode" -> "AG1 2NT"
      )
      val createReturnAgentReturnJson = Json.obj("returnAgentID" -> "RAID-001")

      "must return CreateReturnAgentReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return-agent")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createReturnAgentReturnJson.toString())))
        val request = createReturnAgentRequestJson.as[CreateReturnAgentRequest]
        val result = connector.createReturnAgent(request).futureValue
        result.returnAgentID mustBe "RAID-001"
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return-agent")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return-agent")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = createReturnAgentRequestJson.as[CreateReturnAgentRequest]
        val result = connector.createReturnAgent(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return-agent")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = createReturnAgentRequestJson.as[CreateReturnAgentRequest]
        val result = connector.createReturnAgent(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return-agent")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createReturnAgentReturnJson.toString())))
        val request = createReturnAgentRequestJson.as[CreateReturnAgentRequest]
        connector.createReturnAgent(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return-agent")))
      }

      "must send correct request body with agentType and required fields" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return-agent")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createReturnAgentReturnJson.toString())))
        val request = createReturnAgentRequestJson.as[CreateReturnAgentRequest]
        connector.createReturnAgent(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/return-agent"))
            .withRequestBody(matchingJsonPath("$.agentType", equalTo("SOLICITOR")))
            .withRequestBody(matchingJsonPath("$.name", equalTo("Agent Company Ltd")))
            .withRequestBody(matchingJsonPath("$.postcode", equalTo("AG1 2NT")))
        )
      }
    }

    "updateReturnAgent()" - {

      val updateReturnAgentRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "agentType" -> "SOLICITOR",
        "name" -> "Updated Agent Company Ltd", "addressLine1" -> "Agent Street", "postcode" -> "AG1 2NT"
      )
      val updateReturnAgentReturnJson = Json.obj("updated" -> true)

      "must return UpdateReturnAgentReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-agent")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateReturnAgentReturnJson.toString())))
        val request = updateReturnAgentRequestJson.as[UpdateReturnAgentRequest]
        val result = connector.updateReturnAgent(request).futureValue
        result.updated mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-agent")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-agent")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = updateReturnAgentRequestJson.as[UpdateReturnAgentRequest]
        val result = connector.updateReturnAgent(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-agent")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = updateReturnAgentRequestJson.as[UpdateReturnAgentRequest]
        val result = connector.updateReturnAgent(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-agent")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = updateReturnAgentRequestJson.as[UpdateReturnAgentRequest]
        val result = connector.updateReturnAgent(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-agent")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateReturnAgentReturnJson.toString())))
        val request = updateReturnAgentRequestJson.as[UpdateReturnAgentRequest]
        connector.updateReturnAgent(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-agent")))
      }
    }

    "deleteReturnAgent()" - {

      val deleteReturnAgentRequestJson = Json.obj("storn" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "agentType" -> "SOLICITOR")
      val deleteReturnAgentReturnJson = Json.obj("deleted" -> true)

      "must return DeleteReturnAgentReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/return-agent")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteReturnAgentReturnJson.toString())))
        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        val result = connector.deleteReturnAgent(request).futureValue
        result.deleted mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/return-agent")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/return-agent")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        val result = connector.deleteReturnAgent(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/return-agent")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        val result = connector.deleteReturnAgent(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/return-agent")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        val result = connector.deleteReturnAgent(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/return-agent")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteReturnAgentReturnJson.toString())))
        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        connector.deleteReturnAgent(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/return-agent")))
      }

      "must send correct request body with agentType field" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/return-agent")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteReturnAgentReturnJson.toString())))
        val request = deleteReturnAgentRequestJson.as[DeleteReturnAgentRequest]
        connector.deleteReturnAgent(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/return-agent")).withRequestBody(matchingJsonPath("$.agentType", equalTo("SOLICITOR"))))
      }
    }
    "createPurchaser()" - {

      val createPurchaserRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "isCompany" -> "NO", "isTrustee" -> "NO",
        "isConnectedToVendor" -> "NO", "isRepresentedByAgent" -> "YES", "title" -> "Mr", "surname" -> "Jones",
        "forename1" -> "David", "address1" -> "Park Avenue"
      )
      val createPurchaserReturnJson = Json.obj("purchaserResourceRef" -> "PRF-001", "purchaserId" -> "PID-001")

      "must return CreatePurchaserReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/purchaser")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createPurchaserReturnJson.toString())))
        val request = createPurchaserRequestJson.as[purchaser.CreatePurchaserRequest]
        val result = connector.createPurchaser(request).futureValue
        result.purchaserResourceRef mustBe "PRF-001"
        result.purchaserId mustBe "PID-001"
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/purchaser")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/purchaser")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = createPurchaserRequestJson.as[purchaser.CreatePurchaserRequest]
        val result = connector.createPurchaser(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/purchaser")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = createPurchaserRequestJson.as[purchaser.CreatePurchaserRequest]
        val result = connector.createPurchaser(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/purchaser")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createPurchaserReturnJson.toString())))
        val request = createPurchaserRequestJson.as[purchaser.CreatePurchaserRequest]
        connector.createPurchaser(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/purchaser")))
      }
    }

    "updatePurchaser()" - {

      val updatePurchaserRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "purchaserResourceRef" -> "PRF-001",
        "isCompany" -> "NO", "isTrustee" -> "NO", "isConnectedToVendor" -> "NO", "isRepresentedByAgent" -> "YES",
        "address1" -> "Park Avenue"
      )
      val updatePurchaserReturnJson = Json.obj("updated" -> true)

      "must return UpdatePurchaserReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/purchaser")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updatePurchaserReturnJson.toString())))
        val request = updatePurchaserRequestJson.as[purchaser.UpdatePurchaserRequest]
        val result = connector.updatePurchaser(request).futureValue
        result.updated mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/purchaser")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/purchaser")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = updatePurchaserRequestJson.as[purchaser.UpdatePurchaserRequest]
        val result = connector.updatePurchaser(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/purchaser")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = updatePurchaserRequestJson.as[purchaser.UpdatePurchaserRequest]
        val result = connector.updatePurchaser(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/purchaser")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updatePurchaserReturnJson.toString())))
        val request = updatePurchaserRequestJson.as[purchaser.UpdatePurchaserRequest]
        connector.updatePurchaser(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/purchaser")))
      }
    }

    "deletePurchaser()" - {

      val deletePurchaserRequestJson = Json.obj("storn" -> "STORN12345", "purchaserResourceRef" -> "PUR001", "returnResourceRef" -> "RRF-2024-001")
      val deletePurchaserReturnJson = Json.obj("deleted" -> true)

      "must return DeletePurchaserReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/purchaser")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deletePurchaserReturnJson.toString())))
        val request = deletePurchaserRequestJson.as[purchaser.DeletePurchaserRequest]
        val result = connector.deletePurchaser(request).futureValue
        result.deleted mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/purchaser")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/purchaser")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = deletePurchaserRequestJson.as[purchaser.DeletePurchaserRequest]
        val result = connector.deletePurchaser(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/purchaser")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = deletePurchaserRequestJson.as[purchaser.DeletePurchaserRequest]
        val result = connector.deletePurchaser(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/purchaser")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = deletePurchaserRequestJson.as[purchaser.DeletePurchaserRequest]
        val result = connector.deletePurchaser(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/purchaser")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deletePurchaserReturnJson.toString())))
        val request = deletePurchaserRequestJson.as[purchaser.DeletePurchaserRequest]
        connector.deletePurchaser(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/purchaser")))
      }
    }
    "createCompanyDetails()" - {

      val createCompanyDetailsRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "purchaserResourceRef" -> "PRF-001",
        "utr" -> "1234567890", "vatReference" -> "GB123456789"
      )
      val createCompanyDetailsReturnJson = Json.obj("companyDetailsId" -> "CID-001")

      "must return CreateCompanyDetailsReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/company-details")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createCompanyDetailsReturnJson.toString())))
        val request = createCompanyDetailsRequestJson.as[purchaser.CreateCompanyDetailsRequest]
        val result = connector.createCompanyDetails(request).futureValue
        result.companyDetailsId mustBe "CID-001"
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/company-details")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/company-details")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = createCompanyDetailsRequestJson.as[purchaser.CreateCompanyDetailsRequest]
        val result = connector.createCompanyDetails(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/company-details")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = createCompanyDetailsRequestJson.as[purchaser.CreateCompanyDetailsRequest]
        val result = connector.createCompanyDetails(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/company-details")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createCompanyDetailsReturnJson.toString())))
        val request = createCompanyDetailsRequestJson.as[purchaser.CreateCompanyDetailsRequest]
        connector.createCompanyDetails(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/company-details")))
      }
    }

    "updateCompanyDetails()" - {

      val updateCompanyDetailsRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "purchaserResourceRef" -> "PRF-001", "utr" -> "9876543210"
      )
      val updateCompanyDetailsReturnJson = Json.obj("updated" -> true)

      "must return UpdateCompanyDetailsReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/company-details")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateCompanyDetailsReturnJson.toString())))
        val request = updateCompanyDetailsRequestJson.as[purchaser.UpdateCompanyDetailsRequest]
        val result = connector.updateCompanyDetails(request).futureValue
        result.updated mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/company-details")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/company-details")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = updateCompanyDetailsRequestJson.as[purchaser.UpdateCompanyDetailsRequest]
        val result = connector.updateCompanyDetails(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/company-details")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = updateCompanyDetailsRequestJson.as[purchaser.UpdateCompanyDetailsRequest]
        val result = connector.updateCompanyDetails(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/company-details")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateCompanyDetailsReturnJson.toString())))
        val request = updateCompanyDetailsRequestJson.as[purchaser.UpdateCompanyDetailsRequest]
        connector.updateCompanyDetails(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/company-details")))
      }
    }

    "deleteCompanyDetails()" - {

      val deleteCompanyDetailsRequestJson = Json.obj("storn" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001")
      val deleteCompanyDetailsReturnJson = Json.obj("deleted" -> true)

      "must return DeleteCompanyDetailsReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/company-details")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteCompanyDetailsReturnJson.toString())))
        val request = deleteCompanyDetailsRequestJson.as[purchaser.DeleteCompanyDetailsRequest]
        val result = connector.deleteCompanyDetails(request).futureValue
        result.deleted mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/company-details")))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/company-details")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = deleteCompanyDetailsRequestJson.as[purchaser.DeleteCompanyDetailsRequest]
        val result = connector.deleteCompanyDetails(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/company-details")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = deleteCompanyDetailsRequestJson.as[purchaser.DeleteCompanyDetailsRequest]
        val result = connector.deleteCompanyDetails(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/company-details")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = deleteCompanyDetailsRequestJson.as[purchaser.DeleteCompanyDetailsRequest]
        val result = connector.deleteCompanyDetails(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/company-details")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteCompanyDetailsReturnJson.toString())))
        val request = deleteCompanyDetailsRequestJson.as[purchaser.DeleteCompanyDetailsRequest]
        connector.deleteCompanyDetails(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/company-details")))
      }
    }
    "updateReturnInfo()" - {

      val updateReturnInfoRequestJson = Json.obj(
        "returnResourceRef" -> "RRF-2024-001", "storn" -> "STORN12345", "mainPurchaserID" -> "PUR-001",
        "mainVendorID" -> "VEN-001", "mainLandID" -> "LAND-001"
      )
      val updateReturnInfoReturnJson = Json.obj("updated" -> true)

      "must return ReturnInfoReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateReturnInfoReturnJson.toString())))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).futureValue
        result.updated mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")))
      }

      "must return ReturnInfoReturn with minimal request data" in {
        val minimalRequestJson = Json.obj("returnResourceRef" -> "RRF-2024-001", "storn" -> "STORN12345")
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateReturnInfoReturnJson.toString())))
        val request = minimalRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).futureValue
        result.updated mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")))
      }

      "must send correct request body with mainPurchaserID and other fields" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateReturnInfoReturnJson.toString())))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        connector.updateReturnInfo(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info"))
            .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo("RRF-2024-001")))
            .withRequestBody(matchingJsonPath("$.storn", equalTo("STORN12345")))
            .withRequestBody(matchingJsonPath("$.mainPurchaserID", equalTo("PUR-001")))
            .withRequestBody(matchingJsonPath("$.mainVendorID", equalTo("VEN-001")))
            .withRequestBody(matchingJsonPath("$.mainLandID", equalTo("LAND-001")))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must throw UpstreamErrorResponse when stub returns 503 Service Unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(503).withBody("Service Unavailable")))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 503
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateReturnInfoReturnJson.toString())))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        connector.updateReturnInfo(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")))
      }

      "must not make multiple requests for a single call" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateReturnInfoReturnJson.toString())))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        connector.updateReturnInfo(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateReturnInfoReturnJson.toString())))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        connector.updateReturnInfo(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).withHeader("Content-Type", containing("application/json")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle timeout exception" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withFixedDelay(10000).withStatus(200)))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle request with all optional fields populated" in {
        val completeRequestJson = Json.obj(
          "returnResourceRef" -> "RRF-2024-001", "storn" -> "STORN12345", "mainPurchaserID" -> "PUR-001",
          "mainVendorID" -> "VEN-001", "mainLandID" -> "LAND-001", "IRMarkGenerated" -> "YES",
          "landCertForEachProp" -> "NO", "declaration" -> "YES"
        )
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateReturnInfoReturnJson.toString())))
        val request = completeRequestJson.as[ReturnInfoRequest]
        val result = connector.updateReturnInfo(request).futureValue
        result.updated mustBe true
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info"))
            .withRequestBody(matchingJsonPath("$.IRMarkGenerated", equalTo("YES")))
            .withRequestBody(matchingJsonPath("$.landCertForEachProp", equalTo("NO")))
            .withRequestBody(matchingJsonPath("$.declaration", equalTo("YES")))
        )
      }

      "must use stub URL when stubBool is true" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateReturnInfoReturnJson.toString())))
        val request = updateReturnInfoRequestJson.as[ReturnInfoRequest]
        connector.updateReturnInfo(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/return-info")))
      }
    }
    "createLand()" - {

      val createLandRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "100001", "propertyType" -> "RESIDENTIAL",
        "interestTransferredCreated" -> "FREEHOLD", "houseNumber" -> "42", "addressLine1" -> "High Street",
        "addressLine2" -> "Kensington", "addressLine3" -> "London", "postcode" -> "SW1A 1AA", "landArea" -> "500",
        "areaUnit" -> "SQUARE_METERS", "localAuthorityNumber" -> "LA12345", "mineralRights" -> "YES",
        "nlpgUprn" -> "100012345678", "willSendPlansByPost" -> "NO", "titleNumber" -> "TN123456"
      )
      val createLandReturnJson = Json.obj("landResourceRef" -> "100001", "landId" -> "1")

      "must return CreateLandReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLandReturnJson.toString())))
        val request = createLandRequestJson.as[land.CreateLandRequest]
        val result = connector.createLand(request).futureValue
        result.landResourceRef mustBe "100001"
        result.landId mustBe "1"
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")))
      }

      "must return CreateLandReturn for minimal request" in {
        val minimalRequestJson = Json.obj(
          "stornId" -> "STORN12345", "returnResourceRef" -> "100001", "propertyType" -> "RESIDENTIAL",
          "interestTransferredCreated" -> "FREEHOLD", "addressLine1" -> "High Street"
        )
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLandReturnJson.toString())))
        val request = minimalRequestJson.as[land.CreateLandRequest]
        val result = connector.createLand(request).futureValue
        result.landResourceRef mustBe "100001"
        result.landId mustBe "1"
      }

      "must send correct request body with propertyType and interestTransferredCreated" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLandReturnJson.toString())))
        val request = createLandRequestJson.as[land.CreateLandRequest]
        connector.createLand(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land"))
            .withRequestBody(matchingJsonPath("$.propertyType", equalTo("RESIDENTIAL")))
            .withRequestBody(matchingJsonPath("$.interestTransferredCreated", equalTo("FREEHOLD")))
            .withRequestBody(matchingJsonPath("$.addressLine1", equalTo("High Street")))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = createLandRequestJson.as[land.CreateLandRequest]
        val result = connector.createLand(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = createLandRequestJson.as[land.CreateLandRequest]
        val result = connector.createLand(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLandReturnJson.toString())))
        val request = createLandRequestJson.as[land.CreateLandRequest]
        connector.createLand(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLandReturnJson.toString())))
        val request = createLandRequestJson.as[land.CreateLandRequest]
        connector.createLand(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).withHeader("Content-Type", containing("application/json")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val request = createLandRequestJson.as[land.CreateLandRequest]
        val result = connector.createLand(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = createLandRequestJson.as[land.CreateLandRequest]
        val result = connector.createLand(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle different property types" in {
        val residentialRequestJson = createLandRequestJson ++ Json.obj("propertyType" -> "RESIDENTIAL")
        val nonResidentialRequestJson = createLandRequestJson ++ Json.obj("propertyType" -> "NON_RESIDENTIAL")
        val mixedRequestJson = createLandRequestJson ++ Json.obj("propertyType" -> "MIXED")
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLandReturnJson.toString())))
        val request1 = residentialRequestJson.as[land.CreateLandRequest]
        val request2 = nonResidentialRequestJson.as[land.CreateLandRequest]
        val request3 = mixedRequestJson.as[land.CreateLandRequest]
        connector.createLand(request1).futureValue.landId mustBe "1"
        connector.createLand(request2).futureValue.landId mustBe "1"
        connector.createLand(request3).futureValue.landId mustBe "1"
      }

      "must handle different interest types" in {
        val freeholdRequestJson = createLandRequestJson ++ Json.obj("interestTransferredCreated" -> "FREEHOLD")
        val leaseholdRequestJson = createLandRequestJson ++ Json.obj("interestTransferredCreated" -> "LEASEHOLD")
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLandReturnJson.toString())))
        val request1 = freeholdRequestJson.as[land.CreateLandRequest]
        val request2 = leaseholdRequestJson.as[land.CreateLandRequest]
        connector.createLand(request1).futureValue.landId mustBe "1"
        connector.createLand(request2).futureValue.landId mustBe "1"
      }
    }
    "updateLand()" - {

      val updateLandRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "100001", "landResourceRef" -> "100001",
        "propertyType" -> "RESIDENTIAL", "interestTransferredCreated" -> "FREEHOLD", "houseNumber" -> "42",
        "addressLine1" -> "High Street", "addressLine2" -> "Kensington", "addressLine3" -> "London",
        "postcode" -> "SW1A 1AA", "landArea" -> "500", "areaUnit" -> "SQUARE_METERS",
        "localAuthorityNumber" -> "LA12345", "mineralRights" -> "YES", "nlpgUprn" -> "100012345678",
        "willSendPlansByPost" -> "NO", "titleNumber" -> "TN123456", "nextLandId" -> "100002"
      )
      val updateLandReturnJson = Json.obj("updated" -> true)

      "must return UpdateLandReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateLandReturnJson.toString())))
        val request = updateLandRequestJson.as[land.UpdateLandRequest]
        val result = connector.updateLand(request).futureValue
        result.updated mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")))
      }

      "must return UpdateLandReturn for minimal request" in {
        val minimalRequestJson = Json.obj(
          "stornId" -> "STORN12345", "returnResourceRef" -> "100001", "landResourceRef" -> "100001",
          "propertyType" -> "RESIDENTIAL", "interestTransferredCreated" -> "FREEHOLD", "addressLine1" -> "High Street"
        )
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateLandReturnJson.toString())))
        val request = minimalRequestJson.as[land.UpdateLandRequest]
        val result = connector.updateLand(request).futureValue
        result.updated mustBe true
      }

      "must send correct request body with landResourceRef field" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateLandReturnJson.toString())))
        val request = updateLandRequestJson.as[land.UpdateLandRequest]
        connector.updateLand(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land"))
            .withRequestBody(matchingJsonPath("$.landResourceRef", equalTo("100001")))
            .withRequestBody(matchingJsonPath("$.propertyType", equalTo("RESIDENTIAL")))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = updateLandRequestJson.as[land.UpdateLandRequest]
        val result = connector.updateLand(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = updateLandRequestJson.as[land.UpdateLandRequest]
        val result = connector.updateLand(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = updateLandRequestJson.as[land.UpdateLandRequest]
        val result = connector.updateLand(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateLandReturnJson.toString())))
        val request = updateLandRequestJson.as[land.UpdateLandRequest]
        connector.updateLand(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateLandReturnJson.toString())))
        val request = updateLandRequestJson.as[land.UpdateLandRequest]
        connector.updateLand(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).withHeader("Content-Type", containing("application/json")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val request = updateLandRequestJson.as[land.UpdateLandRequest]
        val result = connector.updateLand(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = updateLandRequestJson.as[land.UpdateLandRequest]
        val result = connector.updateLand(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle different property types" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateLandReturnJson.toString())))
        val residentialRequestJson = updateLandRequestJson ++ Json.obj("propertyType" -> "RESIDENTIAL")
        val nonResidentialRequestJson = updateLandRequestJson ++ Json.obj("propertyType" -> "NON_RESIDENTIAL")
        val mixedRequestJson = updateLandRequestJson ++ Json.obj("propertyType" -> "MIXED")
        val request1 = residentialRequestJson.as[land.UpdateLandRequest]
        val request2 = nonResidentialRequestJson.as[land.UpdateLandRequest]
        val request3 = mixedRequestJson.as[land.UpdateLandRequest]
        connector.updateLand(request1).futureValue.updated mustBe true
        connector.updateLand(request2).futureValue.updated mustBe true
        connector.updateLand(request3).futureValue.updated mustBe true
      }
    }
    "deleteLand()" - {

      val deleteLandRequestJson = Json.obj("storn" -> "STORN12345", "returnResourceRef" -> "100001", "landResourceRef" -> "100001")
      val deleteLandReturnJson = Json.obj("deleted" -> true)

      "must return DeleteLandReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteLandReturnJson.toString())))
        val request = deleteLandRequestJson.as[land.DeleteLandRequest]
        val result = connector.deleteLand(request).futureValue
        result.deleted mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")))
      }

      "must send correct request body with all required fields" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteLandReturnJson.toString())))
        val request = deleteLandRequestJson.as[land.DeleteLandRequest]
        connector.deleteLand(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land"))
            .withRequestBody(matchingJsonPath("$.storn", equalTo("STORN12345")))
            .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo("100001")))
            .withRequestBody(matchingJsonPath("$.landResourceRef", equalTo("100001")))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = deleteLandRequestJson.as[land.DeleteLandRequest]
        val result = connector.deleteLand(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = deleteLandRequestJson.as[land.DeleteLandRequest]
        val result = connector.deleteLand(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = deleteLandRequestJson.as[land.DeleteLandRequest]
        val result = connector.deleteLand(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteLandReturnJson.toString())))
        val request = deleteLandRequestJson.as[land.DeleteLandRequest]
        connector.deleteLand(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")))
      }

      "must not make multiple requests for a single call" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteLandReturnJson.toString())))
        val request = deleteLandRequestJson.as[land.DeleteLandRequest]
        connector.deleteLand(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteLandReturnJson.toString())))
        val request = deleteLandRequestJson.as[land.DeleteLandRequest]
        connector.deleteLand(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).withHeader("Content-Type", containing("application/json")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val request = deleteLandRequestJson.as[land.DeleteLandRequest]
        val result = connector.deleteLand(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = deleteLandRequestJson.as[land.DeleteLandRequest]
        val result = connector.deleteLand(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle different resource reference formats" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteLandReturnJson.toString())))
        val request1Json = deleteLandRequestJson ++ Json.obj("landResourceRef" -> "100001")
        val request2Json = deleteLandRequestJson ++ Json.obj("landResourceRef" -> "999999")
        val request3Json = deleteLandRequestJson ++ Json.obj("landResourceRef" -> "LRF-2024-001")
        val request1 = request1Json.as[land.DeleteLandRequest]
        val request2 = request2Json.as[land.DeleteLandRequest]
        val request3 = request3Json.as[land.DeleteLandRequest]
        connector.deleteLand(request1).futureValue.deleted mustBe true
        connector.deleteLand(request2).futureValue.deleted mustBe true
        connector.deleteLand(request3).futureValue.deleted mustBe true
      }

      "must use stub URL when stubBool is true" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteLandReturnJson.toString())))
        val request = deleteLandRequestJson.as[land.DeleteLandRequest]
        connector.deleteLand(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/land")))
      }
    }
    "createResidency()" - {

      val createResidencyRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001",
        "residency" -> Json.obj("isNonUkResidents" -> "YES", "isCompany" -> "NO", "isCrownRelief" -> "NO")
      )
      val createResidencyReturnJson = Json.obj("created" -> true)

      "must return CreateResidencyReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createResidencyReturnJson.toString())))
        val request = createResidencyRequestJson.as[ukResidency.CreateResidencyRequest]
        val result = connector.createResidency(request).futureValue
        result.created mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")))
      }

      "must send correct request body with nested residency fields" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createResidencyReturnJson.toString())))
        val request = createResidencyRequestJson.as[ukResidency.CreateResidencyRequest]
        connector.createResidency(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency"))
            .withRequestBody(matchingJsonPath("$.stornId", equalTo("STORN12345")))
            .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo("RRF-2024-001")))
            .withRequestBody(matchingJsonPath("$.residency.isNonUkResidents", equalTo("YES")))
            .withRequestBody(matchingJsonPath("$.residency.isCompany", equalTo("NO")))
            .withRequestBody(matchingJsonPath("$.residency.isCrownRelief", equalTo("NO")))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = createResidencyRequestJson.as[ukResidency.CreateResidencyRequest]
        val result = connector.createResidency(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = createResidencyRequestJson.as[ukResidency.CreateResidencyRequest]
        val result = connector.createResidency(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = createResidencyRequestJson.as[ukResidency.CreateResidencyRequest]
        val result = connector.createResidency(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createResidencyReturnJson.toString())))
        val request = createResidencyRequestJson.as[ukResidency.CreateResidencyRequest]
        connector.createResidency(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createResidencyReturnJson.toString())))
        val request = createResidencyRequestJson.as[ukResidency.CreateResidencyRequest]
        connector.createResidency(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")).withHeader("Content-Type", containing("application/json")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val request = createResidencyRequestJson.as[ukResidency.CreateResidencyRequest]
        val result = connector.createResidency(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = createResidencyRequestJson.as[ukResidency.CreateResidencyRequest]
        val result = connector.createResidency(request).failed.futureValue
        result mustBe a[Throwable]
      }
    }

    "updateResidency()" - {

      val updateResidencyRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001",
        "residency" -> Json.obj("isNonUkResidents" -> "NO", "isCompany" -> "YES", "isCrownRelief" -> "YES")
      )
      val updateResidencyReturnJson = Json.obj("updated" -> true)

      "must return UpdateResidencyReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateResidencyReturnJson.toString())))
        val request = updateResidencyRequestJson.as[ukResidency.UpdateResidencyRequest]
        val result = connector.updateResidency(request).futureValue
        result.updated mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/residency")))
      }

      "must send correct request body with nested residency fields" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateResidencyReturnJson.toString())))
        val request = updateResidencyRequestJson.as[ukResidency.UpdateResidencyRequest]
        connector.updateResidency(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/residency"))
            .withRequestBody(matchingJsonPath("$.stornId", equalTo("STORN12345")))
            .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo("RRF-2024-001")))
            .withRequestBody(matchingJsonPath("$.residency.isNonUkResidents", equalTo("NO")))
            .withRequestBody(matchingJsonPath("$.residency.isCompany", equalTo("YES")))
            .withRequestBody(matchingJsonPath("$.residency.isCrownRelief", equalTo("YES")))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/residency")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = updateResidencyRequestJson.as[ukResidency.UpdateResidencyRequest]
        val result = connector.updateResidency(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/residency")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = updateResidencyRequestJson.as[ukResidency.UpdateResidencyRequest]
        val result = connector.updateResidency(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/residency")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = updateResidencyRequestJson.as[ukResidency.UpdateResidencyRequest]
        val result = connector.updateResidency(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateResidencyReturnJson.toString())))
        val request = updateResidencyRequestJson.as[ukResidency.UpdateResidencyRequest]
        connector.updateResidency(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/residency")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/residency")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val request = updateResidencyRequestJson.as[ukResidency.UpdateResidencyRequest]
        val result = connector.updateResidency(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = updateResidencyRequestJson.as[ukResidency.UpdateResidencyRequest]
        val result = connector.updateResidency(request).failed.futureValue
        result mustBe a[Throwable]
      }
    }

    "deleteResidency()" - {

      val deleteResidencyRequestJson = Json.obj("storn" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001")
      val deleteResidencyReturnJson = Json.obj("deleted" -> true)

      "must return DeleteResidencyReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteResidencyReturnJson.toString())))
        val request = deleteResidencyRequestJson.as[ukResidency.DeleteResidencyRequest]
        val result = connector.deleteResidency(request).futureValue
        result.deleted mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/residency")))
      }

      "must send correct request body with storn and returnResourceRef" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteResidencyReturnJson.toString())))
        val request = deleteResidencyRequestJson.as[ukResidency.DeleteResidencyRequest]
        connector.deleteResidency(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/residency"))
            .withRequestBody(matchingJsonPath("$.storn", equalTo("STORN12345")))
            .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo("RRF-2024-001")))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/residency")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = deleteResidencyRequestJson.as[ukResidency.DeleteResidencyRequest]
        val result = connector.deleteResidency(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/residency")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = deleteResidencyRequestJson.as[ukResidency.DeleteResidencyRequest]
        val result = connector.deleteResidency(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/residency")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = deleteResidencyRequestJson.as[ukResidency.DeleteResidencyRequest]
        val result = connector.deleteResidency(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteResidencyReturnJson.toString())))
        val request = deleteResidencyRequestJson.as[ukResidency.DeleteResidencyRequest]
        connector.deleteResidency(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/residency")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/residency")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val request = deleteResidencyRequestJson.as[ukResidency.DeleteResidencyRequest]
        val result = connector.deleteResidency(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/residency")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = deleteResidencyRequestJson.as[ukResidency.DeleteResidencyRequest]
        val result = connector.deleteResidency(request).failed.futureValue
        result mustBe a[Throwable]
      }
    }
    "updateTransaction()" - {

      val updateTransactionRequestJson = Json.obj(
        "storn" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001",
        "transaction" -> Json.obj(
          "claimingRelief" -> "NO", "totalConsider" -> "250000", "considerCash" -> "YES",
          "contractDate" -> "2025-01-15", "effectiveDate" -> "2025-02-01", "transactionDescription" -> "RESIDENTIAL"
        )
      )
      val updateTransactionReturnJson = Json.obj("updated" -> true)

      "must return UpdateTransactionReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateTransactionReturnJson.toString())))
        val request = updateTransactionRequestJson.as[transaction.UpdateTransactionRequest]
        val result = connector.updateTransaction(request).futureValue
        result.updated mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")))
      }

      "must return UpdateTransactionReturn for minimal request (empty transaction payload)" in {
        val minimalRequestJson = Json.obj("storn" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "transaction" -> Json.obj())
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateTransactionReturnJson.toString())))
        val request = minimalRequestJson.as[transaction.UpdateTransactionRequest]
        val result = connector.updateTransaction(request).futureValue
        result.updated mustBe true
      }

      "must send correct request body with storn and returnResourceRef" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateTransactionReturnJson.toString())))
        val request = updateTransactionRequestJson.as[transaction.UpdateTransactionRequest]
        connector.updateTransaction(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction"))
            .withRequestBody(matchingJsonPath("$.storn", equalTo("STORN12345")))
            .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo("RRF-2024-001")))
            .withRequestBody(matchingJsonPath("$.transaction.totalConsider", equalTo("250000")))
            .withRequestBody(matchingJsonPath("$.transaction.contractDate", equalTo("2025-01-15")))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = updateTransactionRequestJson.as[transaction.UpdateTransactionRequest]
        val result = connector.updateTransaction(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = updateTransactionRequestJson.as[transaction.UpdateTransactionRequest]
        val result = connector.updateTransaction(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = updateTransactionRequestJson.as[transaction.UpdateTransactionRequest]
        val result = connector.updateTransaction(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateTransactionReturnJson.toString())))
        val request = updateTransactionRequestJson.as[transaction.UpdateTransactionRequest]
        connector.updateTransaction(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateTransactionReturnJson.toString())))
        val request = updateTransactionRequestJson.as[transaction.UpdateTransactionRequest]
        connector.updateTransaction(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).withHeader("Content-Type", containing("application/json")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val request = updateTransactionRequestJson.as[transaction.UpdateTransactionRequest]
        val result = connector.updateTransaction(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = updateTransactionRequestJson.as[transaction.UpdateTransactionRequest]
        val result = connector.updateTransaction(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle request with all transaction fields populated" in {
        val completeTransactionJson = Json.obj(
          "storn" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001",
          "transaction" -> Json.obj(
            "claimingRelief" -> "YES", "reliefAmount" -> "5000", "reliefReason" -> "CHARITY",
            "reliefSchemeNumber" -> "CIS123456", "isLinked" -> "NO", "totalConsider" -> "250000",
            "considerCash" -> "YES", "contractDate" -> "2025-01-15", "effectiveDate" -> "2025-02-01",
            "transactionDescription" -> "RESIDENTIAL", "newTransactionDescription" -> "RESIDENTIAL",
            "isLandExchanged" -> "NO", "agreedDeferPay" -> "NO", "isPartOfSaleOfBusiness" -> "NO"
          )
        )
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateTransactionReturnJson.toString())))
        val request = completeTransactionJson.as[transaction.UpdateTransactionRequest]
        val result = connector.updateTransaction(request).futureValue
        result.updated mustBe true
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/transaction"))
            .withRequestBody(matchingJsonPath("$.transaction.claimingRelief", equalTo("YES")))
            .withRequestBody(matchingJsonPath("$.transaction.totalConsider", equalTo("250000")))
            .withRequestBody(matchingJsonPath("$.transaction.transactionDescription", equalTo("RESIDENTIAL")))
        )
      }
    }
    "createLease()" - {

      val createLeaseRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001",
        "lease" -> Json.obj(
          "isAnnualRentOver1000" -> "YES", "contractEndDate" -> "2030-12-31", "contractStartDate" -> "2025-01-01",
          "leaseType" -> "COMMERCIAL", "netPresentValue" -> "50000", "totalPremiumPayable" -> "10000",
          "rentFreePeriod" -> "NO", "startingRent" -> "12000", "startingRentEndDate" -> "2026-01-01",
          "laterRentKnown" -> "YES", "vatAmount" -> "2400"
        )
      )
      val createLeaseReturnJson = Json.obj("created" -> true)

      "must return CreateLeaseReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLeaseReturnJson.toString())))
        val request = createLeaseRequestJson.as[lease.CreateLeaseRequest]
        val result = connector.createLease(request).futureValue
        result.created mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")))
      }

      "must send correct request body with nested lease fields" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLeaseReturnJson.toString())))
        val request = createLeaseRequestJson.as[lease.CreateLeaseRequest]
        connector.createLease(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease"))
            .withRequestBody(matchingJsonPath("$.stornId", equalTo("STORN12345")))
            .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo("RRF-2024-001")))
            .withRequestBody(matchingJsonPath("$.lease.isAnnualRentOver1000", equalTo("YES")))
            .withRequestBody(matchingJsonPath("$.lease.leaseType", equalTo("COMMERCIAL")))
            .withRequestBody(matchingJsonPath("$.lease.startingRent", equalTo("12000")))
            .withRequestBody(matchingJsonPath("$.lease.vatAmount", equalTo("2400")))
        )
      }

      "must return CreateLeaseReturn for minimal request (empty lease payload)" in {
        val minimalRequestJson = Json.obj("stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "lease" -> Json.obj())
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLeaseReturnJson.toString())))
        val request = minimalRequestJson.as[lease.CreateLeaseRequest]
        val result = connector.createLease(request).futureValue
        result.created mustBe true
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = createLeaseRequestJson.as[lease.CreateLeaseRequest]
        val result = connector.createLease(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = createLeaseRequestJson.as[lease.CreateLeaseRequest]
        val result = connector.createLease(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = createLeaseRequestJson.as[lease.CreateLeaseRequest]
        val result = connector.createLease(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLeaseReturnJson.toString())))
        val request = createLeaseRequestJson.as[lease.CreateLeaseRequest]
        connector.createLease(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createLeaseReturnJson.toString())))
        val request = createLeaseRequestJson.as[lease.CreateLeaseRequest]
        connector.createLease(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")).withHeader("Content-Type", containing("application/json")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val request = createLeaseRequestJson.as[lease.CreateLeaseRequest]
        val result = connector.createLease(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/create/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = createLeaseRequestJson.as[lease.CreateLeaseRequest]
        val result = connector.createLease(request).failed.futureValue
        result mustBe a[Throwable]
      }
    }

    "updateLease()" - {

      val updateLeaseRequestJson = Json.obj(
        "stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001",
        "lease" -> Json.obj(
          "isAnnualRentOver1000" -> "YES", "contractEndDate" -> "2030-12-31", "contractStartDate" -> "2025-01-01",
          "leaseType" -> "COMMERCIAL", "netPresentValue" -> "60000", "totalPremiumPayable" -> "15000",
          "rentFreePeriod" -> "YES", "startingRent" -> "13000", "startingRentEndDate" -> "2026-01-01",
          "laterRentKnown" -> "NO", "vatAmount" -> "2600"
        )
      )
      val updateLeaseReturnJson = Json.obj("updated" -> true)

      "must return UpdateLeaseReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateLeaseReturnJson.toString())))
        val request = updateLeaseRequestJson.as[lease.UpdateLeaseRequest]
        val result = connector.updateLease(request).futureValue
        result.updated mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease")))
      }

      "must send correct request body with nested lease fields" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateLeaseReturnJson.toString())))
        val request = updateLeaseRequestJson.as[lease.UpdateLeaseRequest]
        connector.updateLease(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease"))
            .withRequestBody(matchingJsonPath("$.stornId", equalTo("STORN12345")))
            .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo("RRF-2024-001")))
            .withRequestBody(matchingJsonPath("$.lease.isAnnualRentOver1000", equalTo("YES")))
            .withRequestBody(matchingJsonPath("$.lease.leaseType", equalTo("COMMERCIAL")))
            .withRequestBody(matchingJsonPath("$.lease.netPresentValue", equalTo("60000")))
            .withRequestBody(matchingJsonPath("$.lease.rentFreePeriod", equalTo("YES")))
        )
      }

      "must return UpdateLeaseReturn for minimal request (empty lease payload)" in {
        val minimalRequestJson = Json.obj("stornId" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001", "lease" -> Json.obj())
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateLeaseReturnJson.toString())))
        val request = minimalRequestJson.as[lease.UpdateLeaseRequest]
        val result = connector.updateLease(request).futureValue
        result.updated mustBe true
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = updateLeaseRequestJson.as[lease.UpdateLeaseRequest]
        val result = connector.updateLease(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = updateLeaseRequestJson.as[lease.UpdateLeaseRequest]
        val result = connector.updateLease(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = updateLeaseRequestJson.as[lease.UpdateLeaseRequest]
        val result = connector.updateLease(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(updateLeaseReturnJson.toString())))
        val request = updateLeaseRequestJson.as[lease.UpdateLeaseRequest]
        connector.updateLease(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val request = updateLeaseRequestJson.as[lease.UpdateLeaseRequest]
        val result = connector.updateLease(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/update/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = updateLeaseRequestJson.as[lease.UpdateLeaseRequest]
        val result = connector.updateLease(request).failed.futureValue
        result mustBe a[Throwable]
      }
    }

    "deleteLease()" - {

      val deleteLeaseRequestJson = Json.obj("storn" -> "STORN12345", "returnResourceRef" -> "RRF-2024-001")
      val deleteLeaseReturnJson = Json.obj("deleted" -> true)

      "must return DeleteLeaseReturn when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteLeaseReturnJson.toString())))
        val request = deleteLeaseRequestJson.as[lease.DeleteLeaseRequest]
        val result = connector.deleteLease(request).futureValue
        result.deleted mustBe true
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/lease")))
      }

      "must send correct request body with storn and returnResourceRef" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteLeaseReturnJson.toString())))
        val request = deleteLeaseRequestJson.as[lease.DeleteLeaseRequest]
        connector.deleteLease(request).futureValue
        server.verify(
          postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/lease"))
            .withRequestBody(matchingJsonPath("$.storn", equalTo("STORN12345")))
            .withRequestBody(matchingJsonPath("$.returnResourceRef", equalTo("RRF-2024-001")))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/lease")).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = deleteLeaseRequestJson.as[lease.DeleteLeaseRequest]
        val result = connector.deleteLease(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/lease")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val request = deleteLeaseRequestJson.as[lease.DeleteLeaseRequest]
        val result = connector.deleteLease(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/lease")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = deleteLeaseRequestJson.as[lease.DeleteLeaseRequest]
        val result = connector.deleteLease(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(deleteLeaseReturnJson.toString())))
        val request = deleteLeaseRequestJson.as[lease.DeleteLeaseRequest]
        connector.deleteLease(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/lease")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/lease")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val request = deleteLeaseRequestJson.as[lease.DeleteLeaseRequest]
        val result = connector.deleteLease(request).failed.futureValue
        result mustBe a[Throwable]
      }

      "must handle malformed JSON response" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/delete/lease")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{invalid json}")))
        val request = deleteLeaseRequestJson.as[lease.DeleteLeaseRequest]
        val result = connector.deleteLease(request).failed.futureValue
        result mustBe a[Throwable]
      }
    }
    "submit()" - {

      val submitRequest: submission.SubmitRequest = submission.SubmitRequest(
        email = None,
        fullReturn = FullReturn(
          stornId = "STORN123456",
          returnResourceRef = "submitted",
          sdltOrganisation = Some(SdltOrganisation(
            doNotDisplayWelcomePage = Some("no"), storn = Some("STORN123456"), version = Some("10")
          )),
          returnInfo = Some(ReturnInfo(
            returnID = Some("382966898"), storn = Some("STORN123456"), purchaserCounter = Some("1"),
            vendorCounter = Some("1"), landCounter = Some("1"), purgeDate = Some("2026-09-07 10:16:28"),
            version = Some("26"), mainPurchaserID = Some("382966899"), mainVendorID = Some("382966907"),
            mainLandID = Some("382966901"), landCertForEachProp = Some("YES"),
            returnResourceRef = Some("submitted"), status = Some("STARTED")
          )),
          purchaser = Some(Seq(Purchaser(
            purchaserID = Some("382966899"), returnID = Some("382966898"), isCompany = Some("NO"),
            isTrustee = Some("YES"), isConnectedToVendor = Some("YES"), isRepresentedByAgent = Some("NO"),
            title = Some("MR"), surname = Some("Jon"), forename1 = Some("Bone"), forename2 = Some("Jones"),
            houseNumber = Some("4"), address1 = Some("4 Purchaser Street"), address2 = Some("PurchaserTown"),
            address3 = Some("TestCounty"), address4 = Some("Address 4"), postcode = Some("AA00 0BB"),
            phone = Some("01234566"), purchaserResourceRef = Some("1"), createDate = Some("2026-06-09 10:06:49"),
            lastUpdateDate = Some("2026-06-09 10:09:31"), isUkCompany = Some("NO"),
            registrationNumber = Some("123"), placeOfRegistration = Some("Germany")
          ))),
          companyDetails = Some(CompanyDetails(
            companyDetailsID = Some("382966900"), returnID = Some("382966898"), purchaserID = Some("382966899"),
            companyTypeBank = Some("no"), companyTypeBuilder = Some("yes"), companyTypeBuildsoc = Some("no"),
            companyTypeCentgov = Some("no"), companyTypeIndividual = Some("no"), companyTypeInsurance = Some("no"),
            companyTypeLocalauth = Some("no"), companyTypeOthercharity = Some("no"),
            companyTypeOtherfinancial = Some("no"), companyTypePartnership = Some("yes"),
            companyTypeProperty = Some("no"), companyTypePubliccorp = Some("no"),
            companyTypeSoletrader = Some("yes"), companyTypePensionfund = Some("no")
          )),
          vendor = Some(Seq(Vendor(
            vendorID = Some("382966907"), returnID = Some("382966898"), title = Some("Mr"),
            forename1 = Some("John"), forename2 = Some("James"), name = Some("Smith"), houseNumber = Some("1"),
            address1 = Some("1 Test Lane"), address2 = Some("TestTown"), address3 = Some("TestCounty"),
            postcode = Some("AA00 0AA"), isRepresentedByAgent = Some("YES"), vendorResourceRef = Some("1"),
            lastUpdateDate = Some("2026-06-09 10:11:28")
          ))),
          land = Some(Seq(Land(
            landID = Some("382966901"), returnID = Some("382966898"), propertyType = Some("01"),
            interestCreatedTransferred = Some("FP"), houseNumber = Some("1"), address1 = Some("1 Test Lane"),
            address2 = Some("TestTown"), address3 = Some("TestCounty"), postcode = Some("AA00 0AA"),
            landArea = Some("3805.000"), areaUnit = Some("SquareMetres"), localAuthorityNumber = Some("1210"),
            mineralRights = Some("yes"), NLPGUPRN = Some("10012345678"), willSendPlanByPost = Some("yes"),
            titleNumber = Some("1234"), landResourceRef = Some("1"), lastUpdateDate = Some("2026-06-09 10:13:11")
          ))),
          transaction = Some(Transaction(
            transactionID = Some("382966902"), returnID = Some("382966898"), claimingRelief = Some("yes"),
            reliefAmount = Some("123.00"), reliefReason = Some("08"), reliefSchemeNumber = Some("123456"),
            isLinked = Some("yes"), totalConsiderationLinked = Some("1,234.00"), totalConsideration = Some("100.00"),
            considerationBuild = Some("yes"), considerationCash = Some("yes"), considerationContingent = Some("no"),
            considerationDebt = Some("no"), considerationEmploy = Some("yes"), considerationOther = Some("no"),
            considerationLand = Some("no"), considerationServices = Some("no"), considerationSharesQTD = Some("no"),
            considerationSharesUNQTD = Some("no"), considerationVAT = Some("10.00"), includesChattel = Some("yes"),
            includesGoodwill = Some("yes"), includesOther = Some("no"), includesStock = Some("yes"),
            usedAsFactory = Some("no"), usedAsHotel = Some("no"), usedAsIndustrial = Some("no"),
            usedAsOffice = Some("no"), usedAsOther = Some("no"), usedAsShop = Some("no"),
            usedAsWarehouse = Some("no"), contractDate = Some("01/01/2024"), isDependantOnFutureEvent = Some("yes"),
            transactionDescription = Some("A"), newTransactionDescription = Some("A"),
            effectiveDate = Some("01/02/2024"), isLandExchanged = Some("yes"), exchangedLandHouseNumber = Some("1"),
            exchangedLandAddress1 = Some("1 Test Lane"), exchangedLandAddress2 = Some("TestTown"),
            exchangedLandAddress3 = Some("TestCounty"), exchangedLandPostcode = Some("AA00 0AA"),
            agreedToDeferPayment = Some("yes"), postTransRulingApplied = Some("yes"),
            isPursuantToPreviousOption = Some("yes"), restrictionsAffectInterest = Some("yes"),
            restrictionDetails = Some("Some restriction details"), postTransRulingFollowed = Some("yes"),
            isPartOfSaleOfBusiness = Some("yes"), totalConsiderationBusiness = Some("123.00")
          )),
          returnAgent = Some(Seq(ReturnAgent(
            returnAgentID = Some("382966906"), returnID = Some("382966898"), agentType = Some("VENDOR"),
            name = Some("Jones & Co"), houseNumber = Some("56"), address1 = Some("56 Agent Lane"),
            address2 = Some("TestTown"), address3 = Some("TestCounty"), postcode = Some("AA00 1AA"),
            phone = Some("016345435"), reference = Some("123456"), isAuthorised = Some("no")
          ))),
          lease = Some(Lease(
            leaseID = Some("382966903"), returnID = Some("382966898"), isAnnualRentOver1000 = Some("yes"),
            contractEndDate = Some("01/05/2027"), contractStartDate = Some("01/05/2020"), leaseType = Some("R"),
            netPresentValue = Some("12.00"), totalPremiumPayable = Some("456.00"), rentFreePeriod = Some("5"),
            startingRent = Some("567.00"), startingRentEndDate = Some("01/05/2022"), laterRentKnown = Some("yes"),
            VATAmount = Some("123.00")
          )),
          taxCalculation = Some(TaxCalculation(
            taxCalculationID = Some("382966905"), returnID = Some("382966898"), amountPaid = Some("345.00"),
            includesPenalty = Some("yes"), taxDue = Some("1,234.00"), calcPenaltyDue = Some("200")
          )),
          submission = Some(Submission(
            submissionID = Some("SUB001"), returnID = Some("382966898"), storn = Some("STORN123456"),
            submissionStatus = Some("SUBMITTED"), govtalkMessageClass = Some("HMRC-STAMP-SDLT"),
            UTRN = Some("23456789MCe"), irmarkReceived = Some("IRMARK-RCV-001"),
            submissionReceipt = Some("RECEIPT-001"), numPolls = Some("3"),
            createDate = Some("2024-10-15T10:30:00"), lastUpdateDate = Some("2024-10-15T11:00:00"),
            acceptedDate = Some("2024-10-15T11:00:00"), submittedDate = Some("2024-10-15T10:30:00"),
            email = Some("john.smith@email.com"), submissionRequestDate = Some("2024-10-15T10:15:00Z"),
            irmarkSent = Some("IRMARK-SENT-001")
          )),
          residency = Some(Residency(
            residencyID = Some("382966904"), isNonUkResidents = Some("yes"),
            isCloseCompany = Some("yes"), isCrownRelief = Some("yes")
          ))
        )
      )

      val submissionResponseJson: JsValue = Json.obj("_type" -> "acknowledged", "returnId" -> "382966898")

      "must return a SubmissionResponse when the stub returns 200 OK with a parseable body" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(submissionResponseJson.toString())))
        val result = connector.submit(submitRequest).futureValue
        result mustBe a[submission.SubmissionResponse]
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")))
      }

      "must return a SubmissionResponse when the stub returns 202 Accepted with a parseable body" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")).willReturn(aResponse().withStatus(202).withHeader("Content-Type", "application/json").withBody(submissionResponseJson.toString())))
        val result = connector.submit(submitRequest).futureValue
        result mustBe a[submission.SubmissionResponse]
      }

      "must return a SubmissionResponse when the stub returns 400 Bad Request with a parseable body" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")).willReturn(aResponse().withStatus(400).withHeader("Content-Type", "application/json").withBody(submissionResponseJson.toString())))
        val result = connector.submit(submitRequest).futureValue
        result mustBe a[submission.SubmissionResponse]
      }

      "must fail with a RuntimeException when a 200 OK body cannot be parsed as SubmissionResponse" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{}")))
        val result = connector.submit(submitRequest).failed.futureValue
        result mustBe a[RuntimeException]
        result.getMessage must include("Unparseable submission response")
      }

      "must throw UpstreamErrorResponse when the stub returns 404 Not Found" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")).willReturn(aResponse().withStatus(404).withBody("Not Found")))
        val result = connector.submit(submitRequest).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
      }

      "must throw UpstreamErrorResponse when the stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val result = connector.submit(submitRequest).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must throw UpstreamErrorResponse when the stub returns 503 Service Unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")).willReturn(aResponse().withStatus(503).withBody("Service Unavailable")))
        val result = connector.submit(submitRequest).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 503
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(submissionResponseJson.toString())))
        connector.submit(submitRequest).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(submissionResponseJson.toString())))
        connector.submit(submitRequest).futureValue
        server.verify(postRequestedFor(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")).withHeader("Content-Type", containing("application/json")))
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(post(urlPathEqualTo("/stamp-duty-land-tax/filing/chris/submission")).willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)))
        val result = connector.submit(submitRequest).failed.futureValue
        result mustBe a[Throwable]
      }
    }
  }
}