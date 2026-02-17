/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package freehold

import base.ResponseHelper
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import test.base.BaseSpec

class CalculationControllerFreeholdNoTaxReliefISpec extends BaseSpec with GuiceOneServerPerSuite with ResponseHelper {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 and valid result for freehold property type" when {

      //SDLT - Tax Calc Case -22e -Self Assessed
      "taxReliefDetails is not provided and isLinked = true" when {
        "Property type is Residential" when {
          "date is on or after 22th of March 2012 " in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  """
                    |{
                    | "holdingType": "Freehold",
                    | "propertyType": "Residential",
                    | "effectiveDateDay": 22,
                    | "effectiveDateMonth": 3,
                    | "effectiveDateYear": 2012,
                    | "premium": 1000000,
                    | "highestRent": 0,
                    | "isLinked": true
                    |}
                    |""".stripMargin
                )
              )

            request.status shouldBe OK
            request.json shouldBe selfAssessedResponse

          }
          "date is before 4th Dec 2014" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  """
                    |{
                    | "holdingType": "Freehold",
                    | "propertyType": "Residential",
                    | "effectiveDateDay": 3,
                    | "effectiveDateMonth": 12,
                    | "effectiveDateYear": 2014,
                    | "premium": 1000000,
                    | "highestRent": 0,
                    | "isLinked": true
                    |}
                    |""".stripMargin
                )
              )

            request.status shouldBe OK
            request.json shouldBe selfAssessedResponse
          }
        }
      }

      //SDLT - Tax Calc Case - 22f - Self Assessed
      "with no taxReliefDetails" when {
        "transaction is linked" when {
          "date is on or after 4th Dec 2014" must {
            "return the zero rate response" when {
              "Property type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Residential",
                        | "effectiveDateDay": 4,
                        | "effectiveDateMonth": 12,
                        | "effectiveDateYear": 2014,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": true
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
      // Case 1
      "with no taxReliefDetails" when {
        "transaction is not linked" when {
          "date is before 17th March 2016" must {
            "return a valid result" when {
              "Property type is Non-residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Non-residential",
                        | "effectiveDateDay": 16,
                        | "effectiveDateMonth": 3,
                        | "effectiveDateYear": 2016,
                        | "premium": 250001,
                        | "highestRent": 0,
                        | "isLinked": false
                        |}
                        |""".stripMargin
                    )
                  )

                val responseJson = Json.parse(
                  """{
                    |  "result": [
                    |    {
                    |      "totalTax": 7500,
                    |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                    |      "taxCalcs": [
                    |        {
                    |          "taxType": "premium",
                    |          "calcType": "slab",
                    |          "taxDue": 7500,
                    |          "rate": 3
                    |        }
                    |      ]
                    |    }
                    |  ]
                    |}
                    |""".stripMargin
                )

                request.status shouldBe OK
                request.json shouldBe responseJson
              }
              "Property type is Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Mixed",
                        | "effectiveDateDay": 16,
                        | "effectiveDateMonth": 3,
                        | "effectiveDateYear": 2016,
                        | "premium": 500001,
                        | "highestRent": 0,
                        | "isLinked": false
                        |}
                        |""".stripMargin
                    )
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    |  "result": [
                    |    {
                    |      "totalTax": 20000,
                    |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                    |      "taxCalcs": [
                    |        {
                    |          "taxType": "premium",
                    |          "calcType": "slab",
                    |          "taxDue": 20000,
                    |          "rate": 4
                    |        }
                    |      ]
                    |    }
                    |  ]
                    |}
                    |""".stripMargin
                )

                request.status shouldBe OK
                request.json shouldBe responseJson
              }
            }
          }
        }
      }
    }
  }
}
