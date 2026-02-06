/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package test.leasehold

import base.ResponseHelper
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import test.base.BaseSpec

class CalculationControllerLeaseholdNoTaxReliefISpec extends BaseSpec with GuiceOneServerPerSuite with ResponseHelper {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 and valid result for leasehold property type" when {

      // SDLT - Tax Calc Case - 19 - Self Assessed
      "transaction is linked" when {
        "date is on or after 22nd November 2017" must {
          "return the zero rate response" in {
            def request: WSResponse = ws.url(
                calculateUrl)
              .post(
                Json.parse(
                  """
                    |{
                    |  "holdingType": "Leasehold",
                    |  "propertyType": "Residential",
                    |  "effectiveDateDay": 22,
                    |  "effectiveDateMonth": 11,
                    |  "effectiveDateYear": 2017,
                    |  "premium": 1000000,
                    |  "highestRent": 0,
                    |  "leaseDetails": {
                    |    "startDateDay": 22,
                    |    "startDateMonth": 11,
                    |    "startDateYear": 2017,
                    |    "endDateDay": 22,
                    |    "endDateMonth": 11,
                    |    "endDateYear": 2018,
                    |    "leaseTerm": {
                    |      "years": 1,
                    |      "days": 1,
                    |      "daysInPartialYear": 365
                    |    },
                    |    "year1Rent": 999,
                    |    "year2Rent": 999
                    |  },
                    |  "propertyDetails": {
                    |   "individual": "No",
                    |   "twoOrMoreProperties": "No",
                    |   "replaceMainResidence": "No"
                    | },
                    |    "isLinked": true
                    |}
                    |""".stripMargin
                )
              )

            request.status shouldBe OK
            request.json shouldBe selfAssessedResponse
          }
        }
      }
    }
  }
}
