/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package mixed

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import base.ResponseHelper
import test.base.BaseSpec

class CalculationControllerMixedPropertyISpec extends BaseSpec with GuiceOneServerPerSuite with ResponseHelper {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {

    "return a 200 and valid result" when {
      //SDLT - Tax Calc Case 42 - Self Assessed
      "return the self assessed response" when {
        "Holding type is Leasehold" in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  | "holdingType": "Leasehold",
                  | "propertyType": "Mixed",
                  |  "effectiveDateDay": 24,
                  |  "effectiveDateMonth": 3,
                  |  "effectiveDateYear": 2012,
                  |  "premium": 1000000,
                  |  "highestRent": 0,
                  |  "leaseDetails": {
                  |    "startDateDay": 6,
                  |    "startDateMonth": 4,
                  |    "startDateYear": 2012,
                  |    "endDateDay": 6,
                  |    "endDateMonth": 4,
                  |    "endDateYear": 2013,
                  |    "leaseTerm": {
                  |      "years": 1,
                  |      "days": 1,
                  |      "daysInPartialYear": 365
                  |    },
                  |    "year1Rent": 999,
                  |    "year2Rent": 999
                  |  },
                  | "taxReliefDetails": {
                  |   "taxReliefCode": 33
                  | }
                  |}
                  |""".stripMargin
              )
            )

          request.status shouldBe OK
          request.json shouldBe selfAssessedResponse
        }
        "Holding type is Freehold" in{
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  | "holdingType": "Freehold",
                  | "propertyType": "Mixed",
                  | "effectiveDateDay": 24,
                  | "effectiveDateMonth": 3,
                  | "effectiveDateYear": 2012,
                  | "premium": 1000000,
                  | "highestRent": 0,
                  | "isLinked": true,
                  | "taxReliefDetails": {
                  |   "taxReliefCode": 33
                  | }
                  |}
                  |""".stripMargin
              )
            )

          request.status shouldBe OK
          request.json shouldBe selfAssessedResponse
        }
      }

      // SDLT - Tax Calc Case - 22
      "TaxReliefCode is Right to Buy: 22" when {
        "effective date is before 17/03/2016" when {
          "the transaction is not linked" must {
            "return the 4% premium rate when the premium is 500K" when {
              "Property Type Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 16,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2015,
                        |  "premium": 250001,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 16,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2015,
                        |    "endDateDay": 16,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2016,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 9999,
                        |    "year2Rent": 9999
                        |  },
                        |    "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 22
                        |  },
                        |  "relevantRentDetails": {
                        |  "relevantRent": 1000
                        | }
                        |}""".stripMargin
                    )
                  )
                request.status shouldBe OK
                request.json shouldBe leaseholdMixedNonResidentialRightToBuyBeforeMarch16Response
              }
            }
          }
        }
      }

      // SDLT - Tax Calc Case - 2016 budget relief reasons without tax relief non leased
      "TaxReliefCode is Right to buy: 22 " when {
        "effective date is on or after 17/03/2016" when {
          "the transaction is not linked" must {
            "return a slice response" when {
              "Property Type mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 24,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2018,
                        |  "premium": 200000,
                        |  "highestRent": 0,
                        |    "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 22
                        |  }
                        |}""".stripMargin
                    )
                  )
                request.status shouldBe OK
                request.json shouldBe budgetReliefReasonsWithoutTaxReliefNonLeased2016Response
              }
            }
          }
        }
      }
      "TaxReliefCode is Right to buy: 35 " when {
        "effective date is on or after 17/03/2016" when {
          "the transaction is not linked" must {
            "return a slice response" when {
              "Property Type mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 24,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2018,
                        |  "premium": 200000,
                        |  "highestRent": 0,
                        |    "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 35
                        |  }
                        |}""".stripMargin
                    )
                  )
                request.status shouldBe OK
                request.json shouldBe budgetReliefReasonsWithoutTaxReliefNonLeased2016Response
              }
            }
          }
        }
      }


    }
  }
}