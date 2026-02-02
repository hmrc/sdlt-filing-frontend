/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package test.freehold

import base.ResponseHelper
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import test.base.BaseSpec

class CalculationControllerFreeholdTaxReliefISpec extends BaseSpec with GuiceOneServerPerSuite with ResponseHelper {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 response for a freehold property type" when {

      // SDLT - Tax Calc Case - Tax Relief - Non Leased
      "the TaxReliefCode is PartExchange: 8" when {
        "and transaction is not linked" must {
          "return the zero rate response" when {
            "Property Type is Residential" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Freehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 1,
                      |  "effectiveDateYear": 2013,
                      |  "highestRent": 0,
                      |  "premium": 750000,
                      |  "isLinked": false,
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 8
                      |  }
                      |}
                      |""".stripMargin
                  )
                )

              request.status shouldBe OK
              request.json shouldBe freeholdZeroRateResponse
            }
            "Property Type is Residential with additional property" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Freehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 1,
                      |  "effectiveDateYear": 2013,
                      |  "highestRent": 0,
                      |  "premium": 750000,
                      |  "propertyDetails": {
                      |    "individual": "Yes",
                      |    "twoOrMoreProperties": "Yes",
                      |    "replaceMainResidence": "Yes"
                      |  },
                      |  "isLinked": false,
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 8
                      |  }
                      |}
                      |""".stripMargin
                  )
                )

              request.status shouldBe OK
              request.json shouldBe freeholdZeroRateResponse
            }
            "Property Type is Non-residential" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Freehold",
                      |  "propertyType": "Non-residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 1,
                      |  "effectiveDateYear": 2020,
                      |  "highestRent": 0,
                      |  "premium": 750000,
                      |  "propertyDetails": {
                      |    "individual": "Yes",
                      |    "twoOrMoreProperties": "No"
                      |  },
                      |  "isLinked": false,
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 8
                      |  }
                      |}
                      |""".stripMargin
                  )
                )

              request.status shouldBe OK
              request.json shouldBe freeholdZeroRateResponse
            }
            "Property Type is Mixed" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Freehold",
                      |  "propertyType": "Mixed",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 1,
                      |  "effectiveDateYear": 2020,
                      |  "highestRent": 0,
                      |  "premium": 750000,
                      |  "propertyDetails": {
                      |    "individual": "Yes",
                      |    "twoOrMoreProperties": "No"
                      |  },
                      |  "isLinked": false,
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 8
                      |  }
                      |}
                      |""".stripMargin
                  )
                )

              request.status shouldBe OK
              request.json shouldBe freeholdZeroRateResponse
            }
          }
        }
      }

      // SDLT - Tax Calc Case - Acquisition tax relief - Non Leased
      "the TaxReliefCode is AcquisitionTaxRelief: 14" when {
        "the transaction is not linked" must {
          "return the 0.5% slab rate response" when {
            "Property Type is Residential" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      | "holdingType": "Freehold",
                      | "propertyType": "Residential",
                      | "effectiveDateDay": 1,
                      | "effectiveDateMonth": 4,
                      | "effectiveDateYear": 2013,
                      | "premium": 1000000,
                      | "highestRent": 0,
                      | "taxReliefDetails": {
                      |   "taxReliefCode": 14
                      | },
                      | "isLinked": false
                      |}
                      |""".stripMargin
                  )
                )

              val expectedResult: JsValue =
                Json.parse(
                  """
                    |{
                    |  "result": [
                    |    {
                    |      "totalTax": 5000,
                    |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                    |      "taxCalcs": [
                    |        {
                    |          "taxType": "premium",
                    |          "calcType": "slab",
                    |          "taxDue": 5000,
                    |          "rate": 0,
                    |          "rateFraction": 5
                    |        }
                    |      ]
                    |    }
                    |  ]
                    |}
                    |""".stripMargin
                )

              request.status shouldBe OK
              request.json shouldBe expectedResult
            }
            "Property Type is Residential with additional property" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      | "holdingType": "Freehold",
                      | "propertyType": "Residential",
                      | "effectiveDateDay": 1,
                      | "effectiveDateMonth": 4,
                      | "effectiveDateYear": 2013,
                      | "premium": 1000000,
                      | "highestRent": 0,
                      | "propertyDetails": {
                      |   "individual": "Yes",
                      |   "twoOrMoreProperties": "Yes",
                      |   "replaceMainResidence": "Yes"
                      |},
                      | "taxReliefDetails": {
                      |   "taxReliefCode": 14
                      | },
                      | "isLinked": false
                      |}
                      |""".stripMargin
                  )
                )

              val expectedResult: JsValue =
                Json.parse(
                  """
                    |{
                    |  "result": [
                    |    {
                    |      "totalTax": 5000,
                    |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                    |      "taxCalcs": [
                    |        {
                    |          "taxType": "premium",
                    |          "calcType": "slab",
                    |          "taxDue": 5000,
                    |          "rate": 0,
                    |          "rateFraction": 5
                    |        }
                    |      ]
                    |    }
                    |  ]
                    |}
                    |""".stripMargin
                )

              request.status shouldBe OK
              request.json shouldBe expectedResult
            }
            "Property Type is Non-residential" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      | "holdingType": "Freehold",
                      | "propertyType": "Non-residential",
                      | "effectiveDateDay": 1,
                      | "effectiveDateMonth": 4,
                      | "effectiveDateYear": 2013,
                      | "premium": 1000000,
                      | "highestRent": 0,
                      | "taxReliefDetails": {
                      |   "taxReliefCode": 14
                      | },
                      | "isLinked": false
                      |}
                      |""".stripMargin
                  )
                )

              val expectedResult: JsValue =
                Json.parse(
                  """
                    |{
                    |  "result": [
                    |    {
                    |      "totalTax": 5000,
                    |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                    |      "taxCalcs": [
                    |        {
                    |          "taxType": "premium",
                    |          "calcType": "slab",
                    |          "taxDue": 5000,
                    |          "rate": 0,
                    |          "rateFraction": 5
                    |        }
                    |      ]
                    |    }
                    |  ]
                    |}
                    |""".stripMargin
                )

              request.status shouldBe OK
              request.json shouldBe expectedResult
            }
            "Property Type is Mixed" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      | "holdingType": "Freehold",
                      | "propertyType": "Mixed",
                      | "effectiveDateDay": 1,
                      | "effectiveDateMonth": 4,
                      | "effectiveDateYear": 2013,
                      | "premium": 1000000,
                      | "highestRent": 0,
                      | "taxReliefDetails": {
                      |   "taxReliefCode": 14
                      | },
                      | "isLinked": false
                      |}
                      |""".stripMargin
                  )
                )

              val expectedResult: JsValue =
                Json.parse(
                  """
                    |{
                    |  "result": [
                    |    {
                    |      "totalTax": 5000,
                    |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                    |      "taxCalcs": [
                    |        {
                    |          "taxType": "premium",
                    |          "calcType": "slab",
                    |          "taxDue": 5000,
                    |          "rate": 0,
                    |          "rateFraction": 5
                    |        }
                    |      ]
                    |    }
                    |  ]
                    |}
                    |""".stripMargin
                )

              request.status shouldBe OK
              request.json shouldBe expectedResult
            }
          }
        }
      }

      // SDLT - Tax Calc Case - 2013 Budget Tax Relief - Non Leased
      "the TaxReliefCode is PreCompletionTransaction: 34" when {
        "the date is on or after 6th April 2013" when {
          "and the transaction is not linked" must {
            "return the zero rate response" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 6,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2013,
                        |  "highestRent": 0,
                        |  "premium": 750000,
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 34
                        |  }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Non-residential",
                        |  "effectiveDateDay": 6,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2013,
                        |  "highestRent": 0,
                        |  "premium": 750000,
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 34
                        |  }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
              "Property Type is Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 6,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2013,
                        |  "highestRent": 0,
                        |  "premium": 750000,
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 34
                        |  }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
            }
          }
        }
      }

      // SDLT - Tax Calc Case - 2016 budget tax relief - Non Leased
      "the TaxReliefCode is PreCompletionTransaction: 34" when {
        "the date is on or after 1st April 2016" when {
          "the transaction is not linked" must {
            "return the zero rate response" when {
              "Property Type is Residential with an additional property" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2016,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "Yes",
                        |    "replaceMainResidence": "Yes"
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 34
                        |  }
                        |}""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
            }
          }
        }
      }

      // SDLT - Tax Calc Case - Freeport Relief - Non Leased
      "the transaction is not linked" when {
        "relief is not partial" must {
          "return the zero rate tax response" when {
            "the TaxReliefCode is FreeportsTaxSiteRelief: 36" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 36,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
              "Property Type is Residential with additional property" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "Yes",
                        |   "replaceMainResidence": "Yes"
                        | },
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 36,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Non-residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 36,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
              "Property Type is Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Mixed",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 36,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
            }
            "TaxReliefCode is InvestmentZonesTaxSiteRelief: 37" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 37,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
              "Property Type is Residential with additional property" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "Yes",
                        |   "replaceMainResidence": "Yes"
                        | },
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 37,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Non-residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 37,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
              "Property Type is Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Mixed",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 37,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe freeholdZeroRateResponse
              }
            }
          }
        }
      }

      // SDLT - Tax Calc Case - Freeport Partial Relief - Self Assessed
      "the transaction is not linked" when {
        "relief is partial" must {
          "return the self assessed response" when {
            "TaxReliefCode is FreeportsTaxSiteRelief: 36" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 36,
                        |   "isPartialRelief": true
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Residential with additional property" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "Yes",
                        |   "replaceMainResidence": "Yes"
                        | },
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 36,
                        |   "isPartialRelief": true
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Non-residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 36,
                        |   "isPartialRelief": true
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Mixed",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 36,
                        |   "isPartialRelief": true
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
            }
            "TaxReliefCode is InvestmentZonesTaxSiteRelief: 37" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 37,
                        |   "isPartialRelief": true
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Residential with additional property" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "Yes",
                        |   "replaceMainResidence": "Yes"
                        | },
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 37,
                        |   "isPartialRelief": true
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Non-residential",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 37,
                        |   "isPartialRelief": true
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Mixed",
                        | "effectiveDateDay": 1,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": false,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 37,
                        |   "isPartialRelief": true
                        | }
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

      // SDLT - Tax Calc Case - 26 - Self Assessed
      "the TaxReliefCode is PartExchange: 8" when {
        "the date is before 4th December 2014" when {
          "transaction is linked" must {
            "return the self assessed response" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 2,
                        |  "effectiveDateMonth": 12,
                        |  "effectiveDateYear": 2013,
                        |  "highestRent": 0,
                        |  "premium": 750000,
                        |  "isLinked": true,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 8
                        |  }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Residential with additional property" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 2,
                        |  "effectiveDateMonth": 12,
                        |  "effectiveDateYear": 2013,
                        |  "highestRent": 0,
                        |  "premium": 750000,
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "Yes",
                        |    "replaceMainResidence": "Yes"
                        |  },
                        |  "isLinked": true,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 8
                        |  }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Non-residential",
                        |  "effectiveDateDay": 2,
                        |  "effectiveDateMonth": 12,
                        |  "effectiveDateYear": 2014,
                        |  "highestRent": 0,
                        |  "premium": 750000,
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "No"
                        |  },
                        |  "isLinked": true,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 8
                        |  }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 2,
                        |  "effectiveDateMonth": 12,
                        |  "effectiveDateYear": 2014,
                        |  "highestRent": 0,
                        |  "premium": 750000,
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "No"
                        |  },
                        |  "isLinked": true,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 8
                        |  }
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

      // SDLT - Tax Calc Case - 24g - Self Assessed
      "TaxReliefCode is RightToBuy: 22" when {
        "date is on or after 1st April 2016" when {
          "the transaction is linked" must {
            "return the self assessed response" when {
              "Property Type is Residential with an additional property" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2016,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "Yes",
                        |    "replaceMainResidence": "Yes"
                        |  },
                        |  "isLinked": true,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 22
                        |  }
                        |}""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
            }
          }
        }
      }

      // SDLT - Tax Calc Case - 28 - Self Assessed
      "TaxReliefCode is RightToBuy: 22" when {
        "date is before 17th March 2016" when {
          "the transaction is linked" must {
            "return the self assessed response" when {
              "Property Type Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 16,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2016,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "isLinked": true,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 22
                        |  }
                        |}""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type Non-residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Non-residential",
                        |  "effectiveDateDay": 16,
                        |  "effectiveDateMonth": 2,
                        |  "effectiveDateYear": 2016,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "isLinked": true,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 22
                        |  }
                        |}""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
            }
          }
        }
      }

      // SDLT - Tax Calc Case - 27 - Self Assessed
      "the transaction is linked" when {
        "date is before 4th December 2014" when {
          "TaxReliefCode is AcquisitionTaxRelief: 14" must {
            "return the self assessed response" when {
              "Property Type is Residential" in {
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
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 14
                        | },
                        | "isLinked": true
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Residential with additional property" in {
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
                        | "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "Yes",
                        |   "replaceMainResidence": "Yes"
                        | },
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 14
                        | },
                        | "isLinked": true
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Non-residential",
                        | "effectiveDateDay": 3,
                        | "effectiveDateMonth": 12,
                        | "effectiveDateYear": 2014,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 14
                        | },
                        | "isLinked": true
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Mixed",
                        | "effectiveDateDay": 3,
                        | "effectiveDateMonth": 12,
                        | "effectiveDateYear": 2014,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 14
                        | },
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

      // SDLT - Tax Calc Case - 44 - Self Assessed
      "the transaction is linked" when {
        "date is on or after 6th April 2013 and before 4th December 2014" when {
          "the TaxReliefCode is ReliefFrom15PercentRate: 14" must {
            "return the self assessed response" when {
              "Property type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        | "holdingType": "Freehold",
                        | "propertyType": "Residential",
                        | "effectiveDateDay": 6,
                        | "effectiveDateMonth": 4,
                        | "effectiveDateYear": 2013,
                        | "premium": 1000000,
                        | "highestRent": 0,
                        | "isLinked": true,
                        | "taxReliefDetails": {
                        |   "taxReliefCode": 35
                        | }
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

      // SDLT - Tax Calc Case - 41 - Self Assessed
      "date is on or after 23rd April 2009" must {
        "return the self assessed response" when {
          "Property type is Residential" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  """
                    |{
                    | "holdingType": "Freehold",
                    | "propertyType": "Residential",
                    | "effectiveDateDay": 23,
                    | "effectiveDateMonth": 4,
                    | "effectiveDateYear": 2012,
                    | "premium": 1000000,
                    | "highestRent": 0,
                    | "taxReliefDetails": {
                    |   "taxReliefCode": 25
                    | }
                    |}
                    |""".stripMargin
                )
              )

            request.status shouldBe OK
            request.json shouldBe selfAssessedResponse
          }
          "Property Type is Residential with additional property" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  """
                    |{
                    | "holdingType": "Freehold",
                    | "propertyType": "Residential",
                    | "effectiveDateDay": 23,
                    | "effectiveDateMonth": 4,
                    | "effectiveDateYear": 2012,
                    | "premium": 1000000,
                    | "highestRent": 0,
                    | "propertyDetails": {
                    |   "individual": "Yes",
                    |   "twoOrMoreProperties": "Yes",
                    |   "replaceMainResidence": "Yes"
                    | },
                    | "taxReliefDetails": {
                    |   "taxReliefCode": 25
                    | }
                    |}
                    |""".stripMargin
                )
              )

            request.status shouldBe OK
            request.json shouldBe selfAssessedResponse
          }
          "Property type is Non-residential" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  """
                    |{
                    | "holdingType": "Freehold",
                    | "propertyType": "Non-residential",
                    | "effectiveDateDay": 23,
                    | "effectiveDateMonth": 4,
                    | "effectiveDateYear": 2012,
                    | "premium": 1000000,
                    | "highestRent": 0,
                    | "taxReliefDetails": {
                    |   "taxReliefCode": 25
                    | }
                    |}
                    |""".stripMargin
                )
              )

            request.status shouldBe OK
            request.json shouldBe selfAssessedResponse
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
                    | "effectiveDateDay": 23,
                    | "effectiveDateMonth": 4,
                    | "effectiveDateYear": 2012,
                    | "premium": 1000000,
                    | "highestRent": 0,
                    | "taxReliefDetails": {
                    |   "taxReliefCode": 25
                    | }
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
