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

import com.github.tomakehurst.wiremock.client.WireMock.*
import models.taxCalculation.*
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.WireMockHelper

class SdltCalculationConnectorISpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with WireMockHelper {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure("sdltc-frontend.host" -> s"http://localhost:${server.port()}")
      .build()

  private lazy val connector = app.injector.instanceOf[SdltCalculationConnector]

  private val calculationPath = "/calculate-stamp-duty-land-tax/calculate"

  private val testRequest = SdltCalculationRequest(
    holdingType         = HoldingTypes.leasehold,
    propertyType        = PropertyTypes.residential,
    effectiveDateDay    = 1,
    effectiveDateMonth  = 2,
    effectiveDateYear   = 2015,
    nonUKResident       = Some("Yes"),
    premium             = BigDecimal(100000),
    highestRent         = BigDecimal(0),
    propertyDetails     = Some(PropertyDetails(
      individual = "Yes",
      twoOrMoreProperties = Some("Yes"),
      replaceMainResidence = Some("No"),
      sharedOwnership = None,             // field not used
      currentValue = None                 // field not used
    )),
    leaseDetails        = Some(LeaseDetails(
      startDateDay    = 1,
      startDateMonth  = 2,
      startDateYear   = 2015,
      endDateDay      = 31,
      endDateMonth    = 1,
      endDateYear     = 2020,
      leaseTerm       = LeaseTerm(
        years             = 5,
        days              = 0,
        daysInPartialYear = 0
      ),
      year1Rent = 0,        // field always set to zero - it is bypassed, we use declaredNpv
      year2Rent = Some(0),  // field always set to zero - it is bypassed, we use declaredNpv
      year3Rent = Some(0),  // field always set to zero - it is bypassed, we use declaredNpv
      year4Rent = Some(0),  // field always set to zero - it is bypassed, we use declaredNpv
      year5Rent = Some(0)   // field always set to zero - it is bypassed, we use declaredNpv
    )),
    relevantRentDetails = Some(RelevantRentDetails(
      contractPre201603        = Some("Yes"),
      contractVariedPost201603 = Some("No"),
      relevantRent             = Some(BigDecimal(0))  // field always set to 0 or 1000
    )),
    firstTimeBuyer      = Some("No"),
    isLinked            = Some(false),
    interestTransferred = Some("LG"),
    taxReliefDetails    = Some(TaxReliefDetails(
      taxReliefCode   = 30,
      isPartialRelief = Some(false)
    )),
    isMultipleLand      = Some(false),
    declaredNpv         = Some(BigDecimal(100000))
  )

  private val singleResultResponse = Json.obj(
    "result" -> Json.arr(
      Json.obj("totalTax" -> 5000, "taxCalcs" -> Json.arr())
    )
  )

  private val multiResultResponse = Json.obj(
    "result" -> Json.arr(
      Json.obj("totalTax" -> 5000, "taxCalcs" -> Json.arr()),
      Json.obj("totalTax" -> 9999, "taxCalcs" -> Json.arr())
    )
  )

  private val emptyResultResponse = Json.obj("result" -> Json.arr())

  "SdltCalculationConnector" - {

    "calculateStampDutyLandTax" - {

      "must return a single result when the calculation service returns a single result" in {
        server.stubFor(
          post(urlPathEqualTo(calculationPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(singleResultResponse.toString())
            )
        )

        val result = connector.calculateStampDutyLandTax(testRequest).futureValue

        server.getAllServeEvents.get(0).getResponse.getStatus mustBe OK
        result mustBe CalculationResponse(Seq(TaxCalculationResult(
          totalTax      = 5000,
          resultHeading = None,
          resultHint    = None,
          npv           = None,
          taxCalcs      = Nil
        )))

        server.verify(postRequestedFor(urlPathEqualTo(calculationPath)))
      }

      "must return multiple results when the calculation service returns multiple results" in {
        server.stubFor(
          post(urlPathEqualTo(calculationPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(multiResultResponse.toString())
            )
        )

        val result = connector.calculateStampDutyLandTax(testRequest).futureValue

        server.getAllServeEvents.get(0).getResponse.getStatus mustBe OK
        result.result must have length 2
        result.result.head.totalTax mustBe 5000
      }

      "must return an empty result sequence when the calculation service returns no results" in {
        server.stubFor(
          post(urlPathEqualTo(calculationPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(emptyResultResponse.toString())
            )
        )

        val result = connector.calculateStampDutyLandTax(testRequest).futureValue

        server.getAllServeEvents.get(0).getResponse.getStatus mustBe OK
        result mustBe CalculationResponse(result = Nil)
      }

      "must make a POST request to the correct endpoint" in {
        server.stubFor(
          post(urlPathEqualTo(calculationPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(singleResultResponse.toString())
            )
        )

        connector.calculateStampDutyLandTax(testRequest).futureValue

        server.getAllServeEvents.get(0).getResponse.getStatus mustBe OK
        server.verify(1, postRequestedFor(urlPathEqualTo(calculationPath)))
      }

      "must send JSON in the request body" in {
        server.stubFor(
          post(urlPathEqualTo(calculationPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(singleResultResponse.toString())
            )
        )

        connector.calculateStampDutyLandTax(testRequest).futureValue

        server.getAllServeEvents.get(0).getResponse.getStatus mustBe OK
        server.verify(
          postRequestedFor(urlPathEqualTo(calculationPath))
            .withHeader("Content-Type", containing("application/json"))
        )
      }

      "must throw UpstreamErrorResponse when the calculation service returns 400 Bad Request" in {
        server.stubFor(
          post(urlPathEqualTo(calculationPath))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withBody("Bad Request")
            )
        )

        val result = connector.calculateStampDutyLandTax(testRequest).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe BAD_REQUEST
      }

      "must throw UpstreamErrorResponse when the calculation service returns 500 Internal Server Error" in {
        server.stubFor(
          post(urlPathEqualTo(calculationPath))
            .willReturn(
              aResponse()
                .withStatus(INTERNAL_SERVER_ERROR)
                .withBody("Internal Server Error")
            )
        )

        val result = connector.calculateStampDutyLandTax(testRequest).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe INTERNAL_SERVER_ERROR
      }

      "must throw an exception when the response body contains malformed JSON" in {
        server.stubFor(
          post(urlPathEqualTo(calculationPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{invalid json}")
            )
        )

        connector.calculateStampDutyLandTax(testRequest).failed.futureValue mustBe a[Throwable]

        server.getAllServeEvents.get(0).getResponse.getStatus mustBe OK
      }
    }
  }
}
