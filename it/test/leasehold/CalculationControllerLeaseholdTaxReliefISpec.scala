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
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import test.base.BaseSpec

class CalculationControllerLeaseholdTaxReliefISpec extends BaseSpec with GuiceOneServerPerSuite with ResponseHelper {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 and valid result for leasehold property type" when {

      // SDLT - Tax Calc Case - Tax Relief - Leased
      "the TaxReliefCode is Seeding relief: 38" when {
        "the transaction is not linked" must {
          "return the zero rate response" when {
            "Property Type is Mixed" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Mixed",
                      |  "effectiveDateDay": 23,
                      |  "effectiveDateMonth": 3,
                      |  "effectiveDateYear": 2012,
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "leaseDetails": {
                      |    "startDateDay": 23,
                      |    "startDateMonth": 3,
                      |    "startDateYear": 2012,
                      |    "endDateDay": 23,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2013,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999
                      |  },
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 38
                      |  },
                      |  "isLinked": false
                      |}
                      |""".stripMargin
                  )
                )

              request.status shouldBe OK
              request.json shouldBe leaseholdZeroRateResponse
            }
            "Property Type is Non-residential" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Non-residential",
                      |  "effectiveDateDay": 23,
                      |  "effectiveDateMonth": 3,
                      |  "effectiveDateYear": 2012,
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "leaseDetails": {
                      |    "startDateDay": 23,
                      |    "startDateMonth": 3,
                      |    "startDateYear": 2012,
                      |    "endDateDay": 23,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2013,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999
                      |  },
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 38
                      |  },
                      |  "isLinked": false
                      |}
                      |""".stripMargin
                  )
                )

              request.status shouldBe OK
              request.json shouldBe leaseholdZeroRateResponse
            }
          }
        }
      }

      // SDLT - Tax Calc Case - Acquisition tax relief - Leased
      "the TaxReliefCode is AcquisitionRelief: 14" when {
        "the transaction is not linked" must {
          "return 0.5% rate response applied to both premium and rent slabs" when {
            "Property Type is Residential" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 23,
                      |  "effectiveDateMonth": 3,
                      |  "effectiveDateYear": 2012,
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "isLinked": false,
                      |  "leaseDetails": {
                      |    "startDateDay": 23,
                      |    "startDateMonth": 3,
                      |    "startDateYear": 2012,
                      |    "endDateDay": 23,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2013,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999
                      |  },
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 14
                      |  }
                      |}
                      |""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |  "result": [
                  |    {
                  |      "totalTax": 5009,
                  |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                  |      "npv": 1897,
                  |      "taxCalcs": [
                  |        {
                  |          "taxType": "premium",
                  |          "calcType": "slab",
                  |          "taxDue": 5000,
                  |          "rate": 0,
                  |          "rateFraction": 5
                  |        },
                  |        {
                  |          "taxType": "rent",
                  |          "calcType": "slab",
                  |          "taxDue": 9,
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
              request.json shouldBe responseJson
            }
            "Property Type is Residential with additional property" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 23,
                      |  "effectiveDateMonth": 3,
                      |  "effectiveDateYear": 2012,
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "isLinked": false,
                      |  "leaseDetails": {
                      |    "startDateDay": 23,
                      |    "startDateMonth": 3,
                      |    "startDateYear": 2012,
                      |    "endDateDay": 23,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2013,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999
                      |  },
                      |  "propertyDetails": {
                      |    "individual": "Yes",
                      |    "twoOrMoreProperties": "Yes",
                      |    "replaceMainResidence": "No"
                      |  },
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 14
                      |  }
                      |}
                      |""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |  "result": [
                  |    {
                  |      "totalTax": 5009,
                  |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                  |      "npv": 1897,
                  |      "taxCalcs": [
                  |        {
                  |          "taxType": "premium",
                  |          "calcType": "slab",
                  |          "taxDue": 5000,
                  |          "rate": 0,
                  |          "rateFraction": 5
                  |        },
                  |        {
                  |          "taxType": "rent",
                  |          "calcType": "slab",
                  |          "taxDue": 9,
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
              request.json shouldBe responseJson
            }
            "Property Type is Non-residential" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Non-residential",
                      |  "effectiveDateDay": 23,
                      |  "effectiveDateMonth": 3,
                      |  "effectiveDateYear": 2012,
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "isLinked": false,
                      |  "leaseDetails": {
                      |    "startDateDay": 23,
                      |    "startDateMonth": 3,
                      |    "startDateYear": 2012,
                      |    "endDateDay": 23,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2013,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999
                      |  },
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 14
                      |  }
                      |}
                      |""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |  "result": [
                  |    {
                  |      "totalTax": 5009,
                  |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                  |      "npv": 1897,
                  |      "taxCalcs": [
                  |        {
                  |          "taxType": "premium",
                  |          "calcType": "slab",
                  |          "taxDue": 5000,
                  |          "rate": 0,
                  |          "rateFraction": 5
                  |        },
                  |        {
                  |          "taxType": "rent",
                  |          "calcType": "slab",
                  |          "taxDue": 9,
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
              request.json shouldBe responseJson
            }
            "Property Type is Mixed" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Mixed",
                      |  "effectiveDateDay": 23,
                      |  "effectiveDateMonth": 3,
                      |  "effectiveDateYear": 2012,
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "isLinked": false,
                      |  "leaseDetails": {
                      |    "startDateDay": 23,
                      |    "startDateMonth": 3,
                      |    "startDateYear": 2012,
                      |    "endDateDay": 23,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2013,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999
                      |  },
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 14
                      |  }
                      |}
                      |""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |  "result": [
                  |    {
                  |      "totalTax": 5009,
                  |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                  |      "npv": 1897,
                  |      "taxCalcs": [
                  |        {
                  |          "taxType": "premium",
                  |          "calcType": "slab",
                  |          "taxDue": 5000,
                  |          "rate": 0,
                  |          "rateFraction": 5
                  |        },
                  |        {
                  |          "taxType": "rent",
                  |          "calcType": "slab",
                  |          "taxDue": 9,
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
              request.json shouldBe responseJson
            }
          }
        }
      }

      // SDLT - Tax Calc Case - 2013 Budget Tax Relief - Leased
      "the TaxReliefCode is PreCompletionTransaction: 34" when {
        "the transaction is not linked" when {
          "date is on or after 6th April 2013" must {
            "return the zero rate response" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 6,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2013,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 6,
                        |    "startDateMonth": 4,
                        |    "startDateYear": 2013,
                        |    "endDateDay": 6,
                        |    "endDateMonth": 4,
                        |    "endDateYear": 2014,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |   "taxReliefCode": 34
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Non-residential",
                        |  "effectiveDateDay": 6,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2013,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 6,
                        |    "startDateMonth": 4,
                        |    "startDateYear": 2013,
                        |    "endDateDay": 6,
                        |    "endDateMonth": 4,
                        |    "endDateYear": 2014,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |   "taxReliefCode": 34
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
              "Property Type is Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 6,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2013,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 6,
                        |    "startDateMonth": 4,
                        |    "startDateYear": 2013,
                        |    "endDateDay": 6,
                        |    "endDateMonth": 4,
                        |    "endDateYear": 2014,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |   "taxReliefCode": 34
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
            }
          }
        }
      }

      // SDLT - Tax Calc Case - 2016 Budget Tax Relief - Leased
      "with TaxReliefCode - PreCompletionTransaction: 34" when {
        "transaction is not linked" when {
          "date is on or after 1st April 2016" must {
            "return the zero rate response" when {
              "Property Type is Residential with additional property" in {
                val request: WSResponse = ws.url(
                    calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2016,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 1,
                        |    "startDateMonth": 4,
                        |    "startDateYear": 2016,
                        |    "endDateDay": 1,
                        |    "endDateMonth": 4,
                        |    "endDateYear": 2017,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "Yes",
                        |    "replaceMainResidence": "No"
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |   "taxReliefCode": 34
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
            }
          }
        }
      }

      // SDLT - Tax Calc Case - Freeport Relief - Leased
      "transaction is not linked" when {
        "relief is not partial" must {
          "return the zero rate response" when {
            "the TaxReliefCode is FreeportsTaxSiteRelief: 36" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws.url(
                    calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |   "taxReliefCode": 36,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
              "Property Type is Residential with additional property" in {
                val request: WSResponse = ws.url(
                    calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "Yes",
                        |    "replaceMainResidence": "No"
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 36,
                        |    "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws.url(
                    calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Non-residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |   "taxReliefCode": 36,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
              "Property Type is Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |   "taxReliefCode": 36,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
            }
            "the TaxReliefCode is InvestmentZonesTaxSiteRelief: 37" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |   "taxReliefCode": 37,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
              "Property Type is Residential with additional property" in {
                val request: WSResponse = ws.url(
                    calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "Yes",
                        |    "replaceMainResidence": "No"
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 37,
                        |    "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws.url(
                    calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Non-residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |   "taxReliefCode": 37,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
              "Property Type is Mixed" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |   "taxReliefCode": 37,
                        |   "isPartialRelief": false
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe leaseholdZeroRateResponse
              }
            }
          }
        }
      }

      // SDLT - Tax Calc Case - Freeport Partial Relief - Self Assessed
      "the transaction is not linked" when {
        "the relief is partial" must {
          "return self assessed response" when {
            "the TaxReliefCode is FreeportsTaxSiteRelief: 36" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws.url(
                    calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 36,
                        |    "isPartialRelief": true
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Residential with additional property" in {
                val request: WSResponse = ws.url(
                    calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "Yes",
                        |    "replaceMainResidence": "No"
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 36,
                        |    "isPartialRelief": true
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws.url(
                    calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Non-residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 36,
                        |    "isPartialRelief": true
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
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 36,
                        |    "isPartialRelief": true
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
            }
            "the TaxReliefCode is InvestmentZonesTaxSiteRelief: 37" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
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
                val request: WSResponse = ws.url(
                    calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "Yes",
                        |    "replaceMainResidence": "No"
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 37,
                        |    "isPartialRelief": true
                        | }
                        |}
                        |""".stripMargin
                    )
                  )

                request.status shouldBe OK
                request.json shouldBe selfAssessedResponse
              }
              "Property Type is Non-residential" in {
                val request: WSResponse = ws.url(
                    calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Non-residential",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
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
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 23,
                        |  "effectiveDateMonth": 3,
                        |  "effectiveDateYear": 2012,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 23,
                        |    "startDateMonth": 3,
                        |    "startDateYear": 2012,
                        |    "endDateDay": 23,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2013,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "isLinked": false,
                        |  "taxReliefDetails": {
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

      // SDLT - Tax Calc Case - 39 - Self Assessed
      "the TaxReliefCode is FirstTimeBuyersRelief: 32" when {
        "date is on or after 25th March 2010 & before 25th March 2012" must {
          "return self assessed response" when {
            "Property Type is Residential" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
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
                      |  "taxReliefDetails": {
                      |   "taxReliefCode": 32
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
        // SDLT - Tax Calc Case - 60_2020 - Self Assessed
        "date is on or after 08/07/2020" must {
          "return self assessed response" when {
            "Property Type is Residential" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 27,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2020,
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "leaseDetails": {
                      |    "startDateDay": 27,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2020,
                      |    "endDateDay": 27,
                      |    "endDateMonth": 7,
                      |    "endDateYear": 2021,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999
                      |  },
                      |  "propertyDetails": {
                      |    "individual": "No",
                      |    "twoOrMoreProperties": "No",
                      |    "replaceMainResidence": "No"
                      |  },
                      |  "firstTimeBuyer": "Yes",
                      |  "isLinked": true,
                      |  "isMultipleLand": true,
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 32
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

      // SDLT - Tax Calc Case - 41 - Self Assessed
      "the TaxReliefCode is CollectiveEnfranchisementByLeaseholders: 25" when {
        "date is on or after 23rd April 2009" must {
          "return self assessed response" when {
            "Property Type is Residential" in {
              val request: WSResponse = ws
                .url(calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
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
                      |  "taxReliefDetails": {
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
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
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
                      |  "propertyDetails": {
                      |    "individual": "Yes",
                      |    "twoOrMoreProperties": "Yes",
                      |    "replaceMainResidence": "No"
                      |  },
                      |  "taxReliefDetails": {
                      |   "taxReliefCode": 25
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
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Non-residential",
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
                      |  "taxReliefDetails": {
                      |   "taxReliefCode": 25
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
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Mixed",
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
                      |  "taxReliefDetails": {
                      |    "taxReliefCode": 25
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

      // SDLT - Tax Calc Case - 19 - Self Assessed
      "interest transferred has value other: OT" must {
        "return the self assessed response" in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Residential",
                  |  "effectiveDateDay": 23,
                  |  "effectiveDateMonth": 3,
                  |  "effectiveDateYear": 2012,
                  |  "premium": 1000000,
                  |  "highestRent": 0,
                  |  "leaseDetails": {
                  |    "startDateDay": 23,
                  |    "startDateMonth": 3,
                  |    "startDateYear": 2012,
                  |    "endDateDay": 23,
                  |    "endDateMonth": 3,
                  |    "endDateYear": 2013,
                  |    "leaseTerm": {
                  |      "years": 1,
                  |      "days": 1,
                  |      "daysInPartialYear": 365
                  |    },
                  |    "year1Rent": 999,
                  |    "year2Rent": 999
                  |  },
                  |  "interestTransferred": "OT"
                  |}
                  |""".stripMargin
              )
            )

          request.status shouldBe OK
          request.json shouldBe selfAssessedResponse
        }
      }
      //SDLT - Tax Calc Case 42 - Self Assessed
      "return the self assessed response" when {
        "Property type is Residential" in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  | "holdingType": "Leasehold",
                  | "propertyType": "Residential",
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
        "Property type is Non-Residential" in{
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                """
                  {
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Non-residential",
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
                  |  "taxReliefDetails": {
                  |   "taxReliefCode": 33
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
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Residential",
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
                  |  "propertyDetails": {
                  |    "individual": "Yes",
                  |    "twoOrMoreProperties": "Yes",
                  |    "replaceMainResidence": "No"
                  |  },
                  |  "taxReliefDetails": {
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
      // SDLT - Tax Calc Case - 61 - self assessed
      "the TaxReliefCode is FirstTimeBuyersRelief: 32" when {
        "there are multiple lands" when {
          "Property Type is Residential" when {
            "the transaction is linked" must {
              "return self assessed response" when {
                "date is on or after 22nd Nov 2017" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
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
                          |    "individual": "Yes",
                          |    "twoOrMoreProperties": "No"
                          |  },
                          |  "firstTimeBuyer": "Yes",
                          |  "isLinked": true,
                          |  "isMultipleLand": true,
                          |  "taxReliefDetails": {
                          |    "taxReliefCode": 32
                          |  }
                          |}
                          |""".stripMargin
                      )
                    )

                  request.status shouldBe OK
                  request.json shouldBe selfAssessedResponse
                }
                "date is before 8th July 2020" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Residential",
                          |  "effectiveDateDay": 7,
                          |  "effectiveDateMonth": 7,
                          |  "effectiveDateYear": 2020,
                          |  "premium": 1000000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 7,
                          |    "startDateMonth": 7,
                          |    "startDateYear": 2020,
                          |    "endDateDay": 7,
                          |    "endDateMonth": 7,
                          |    "endDateYear": 2021,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "propertyDetails": {
                          |    "individual": "Yes",
                          |    "twoOrMoreProperties": "No"
                          |  },
                          |  "firstTimeBuyer": "Yes",
                          |  "isLinked": true,
                          |  "isMultipleLand": true,
                          |  "taxReliefDetails": {
                          |    "taxReliefCode": 32
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
      }
      // SDLT - Tax Calc Case - 59a - Self Assessed
      "the TaxReliefCode is one of: [8|9|10|11|12|13|15|16|17|18|19|20|21|23|24|26|27|28|29|31]" when {
        "the date is On or After 22/11/2017" when {
          "transaction is linked" must {
            "return the self assessed response" when {
              "Property Type is Residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 7,
                        |  "effectiveDateMonth": 7,
                        |  "effectiveDateYear": 2018,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 7,
                        |    "startDateMonth": 7,
                        |    "startDateYear": 2018,
                        |    "endDateDay": 7,
                        |    "endDateMonth": 7,
                        |    "endDateYear": 2019,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "No"
                        |  },
                        |  "firstTimeBuyer": "Yes",
                        |  "isLinked": true,
                        |  "isMultipleLand": true,
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
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 7,
                        |  "effectiveDateMonth": 7,
                        |  "effectiveDateYear": 2018,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 7,
                        |    "startDateMonth": 7,
                        |    "startDateYear": 2018,
                        |    "endDateDay": 7,
                        |    "endDateMonth": 7,
                        |    "endDateYear": 2019,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "Yes",
                        |    "replaceMainResidence": "No"
                        |  },
                        |  "firstTimeBuyer": "Yes",
                        |  "isLinked": true,
                        |  "isMultipleLand": true,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 11
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
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Non-residential",
                        |  "effectiveDateDay": 7,
                        |  "effectiveDateMonth": 7,
                        |  "effectiveDateYear": 2018,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 7,
                        |    "startDateMonth": 7,
                        |    "startDateYear": 2018,
                        |    "endDateDay": 7,
                        |    "endDateMonth": 7,
                        |    "endDateYear": 2019,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "No"
                        |  },
                        |  "firstTimeBuyer": "Yes",
                        |  "isLinked": true,
                        |  "isMultipleLand": true,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 15
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
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Mixed",
                        |  "effectiveDateDay": 7,
                        |  "effectiveDateMonth": 7,
                        |  "effectiveDateYear": 2018,
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "leaseDetails": {
                        |    "startDateDay": 7,
                        |    "startDateMonth": 7,
                        |    "startDateYear": 2018,
                        |    "endDateDay": 7,
                        |    "endDateMonth": 7,
                        |    "endDateYear": 2019,
                        |    "leaseTerm": {
                        |      "years": 1,
                        |      "days": 1,
                        |      "daysInPartialYear": 365
                        |    },
                        |    "year1Rent": 999,
                        |    "year2Rent": 999
                        |  },
                        |  "propertyDetails": {
                        |    "individual": "Yes",
                        |    "twoOrMoreProperties": "No"
                        |  },
                        |  "firstTimeBuyer": "Yes",
                        |  "isLinked": true,
                        |  "isMultipleLand": true,
                        |  "taxReliefDetails": {
                        |    "taxReliefCode": 31
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

      //SDLT - Tax Calc Case - 60a - self assessed
      "TaxRelief code is FirstTimeBuyersRelief(32) & Property type is Residential, isLinked = true, premium > 500000 ,isMultipleLand = false" must {
        "return the self assessed response when 2017/11/22 and 2020/07/08" when {
          "date is 2017/11/22 " in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  s"""
                     |{
                     | "holdingType": "Leasehold",
                     | "propertyType": "Residential",
                     | "effectiveDateDay": 22,
                     | "effectiveDateMonth": 11,
                     | "effectiveDateYear": 2017,
                     | "premium": 787659,
                     | "highestRent": 0,
                     | "leaseDetails": {
                     |   "startDateDay": 23,
                     |   "startDateMonth": 11,
                     |   "startDateYear": 2017,
                     |   "endDateDay": 23,
                     |   "endDateMonth": 11,
                     |   "endDateYear": 2018,
                     |   "leaseTerm": {
                     |      "years": 1,
                     |      "days": 1,
                     |      "daysInPartialYear": 365
                     |    },
                     |   "year1Rent": 1001,
                     |   "year2Rent": 1001
                     |  },
                     |  "firstTimeBuyer": "Yes",
                     |  "isMultipleLand": false,
                     |  "isLinked": true,
                     |  "taxReliefDetails": {
                     |   "taxReliefCode": 32
                     | },
                     |  "propertyDetails": {
                     |  "individual": "Yes",
                     |  "twoOrMoreProperties": "No"
                     |  }
                     |}
                     |""".stripMargin
                )
              )

            request.status shouldBe OK
            request.json shouldBe selfAssessedResponse
          }
          "date is 2018/8/22 " in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  s"""
                     |{
                     | "holdingType": "Leasehold",
                     | "propertyType": "Residential",
                     | "effectiveDateDay": 22,
                     | "effectiveDateMonth": 8,
                     | "effectiveDateYear": 2018,
                     | "premium": 600000,
                     | "highestRent": 0,
                     | "leaseDetails": {
                     |   "startDateDay": 22,
                     |   "startDateMonth": 8,
                     |   "startDateYear": 2018,
                     |   "endDateDay": 22,
                     |   "endDateMonth":8,
                     |   "endDateYear": 2019,
                     |   "leaseTerm": {
                     |      "years": 1,
                     |      "days": 1,
                     |      "daysInPartialYear": 365
                     |    },
                     |   "year1Rent": 1001,
                     |   "year2Rent": 1001
                     |  },
                     |  "firstTimeBuyer": "Yes",
                     |  "isMultipleLand": false,
                     |  "isLinked": true,
                     |  "taxReliefDetails": {
                     |   "taxReliefCode": 32
                     | },
                     |  "propertyDetails": {
                     |  "individual": "Yes",
                     |  "twoOrMoreProperties": "No"
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

      // SDLT - Tax Calc Case - 22
      "TaxReliefCode is Right to Buy: 22" when {
        "effective date is before 17/03/2016" when {
          "the transaction is not linked" must {
            "return the 4% premium rate when the premium is 500K" when {
              "Property Type Non-residential" in {
                val request: WSResponse = ws
                  .url(calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Non-residential",
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

      //SDLT - Tax Calc Case - Case 52 - Add Mixed Logic
      "TaxRelief code is ReliefFrom15PercentRate(35) and Property type is Mixed or Non-residential, isLinked = false, relevantRent < 1000 and between 2013/04/06 and 2016/03/17" must {
        "return the result with 0% Tax on premium and 0 % on rent when premium <= 150000 and npv <= 150000" in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                s"""
                   |{
                   | "holdingType": "Leasehold",
                   | "propertyType": "Mixed",
                   | "effectiveDateDay": 24,
                   | "effectiveDateMonth": 3,
                   | "effectiveDateYear": 2014,
                   | "premium": 150000,
                   | "highestRent": 0,
                   | "leaseDetails": {
                   | "startDateDay": 24,
                   | "startDateMonth": 3,
                   | "startDateYear": 2014,
                   | "endDateDay": 24,
                   | "endDateMonth": 3,
                   | "endDateYear": 2015,
                   | "leaseTerm": {
                   | "years": 1,
                   | "days": 1,
                   | "daysInPartialYear": 365
                   | },
                   | "year1Rent": 100000,
                   | "year2Rent": 999
                   | },
                   | "isLinked": false,
                   | "taxReliefDetails": {
                   | "taxReliefCode": 35
                   | },
                   | "relevantRentDetails" : {
                   | "relevantRent": 999
                   | }
                   |}
                   |""".stripMargin
              )
            )

          val expectedResponse: JsValue = Json.parse(
            s"""
               |{
               | "result": [
               | {
               | "totalTax": 0,
               | "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
               | "npv": 97550,
               | "taxCalcs": [
               | {
               | "taxType": "premium",
               | "calcType": "slab",
               | "taxDue": 0,
               | "rate": 0
               | },
               | {
               | "taxType": "rent",
               | "calcType": "slice",
               | "taxDue": 0,
               | "slices": [
               | {
               | "from": 0,
               | "to": 150000,
               | "rate": 0,
               | "taxDue": 0
               | },
               | {
               | "from": 150000,
               | "to": -1,
               | "rate": 1,
               | "taxDue": 0
               | }
               | ]
               | }
               | ]
               | }
               | ]
               |}
               |""".stripMargin)
          request.status shouldBe OK
          request.json shouldBe expectedResponse

        }
        "return the result with 1% Tax rate on premium and 1% tax rate on rent when premium > 150000 and npv > 150000" in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                s"""
                   |{
                   | "holdingType": "Leasehold",
                   | "propertyType": "Mixed",
                   | "effectiveDateDay": 24,
                   | "effectiveDateMonth": 3,
                   | "effectiveDateYear": 2014,
                   | "premium": 200000,
                   | "highestRent": 0,
                   | "leaseDetails": {
                   | "startDateDay": 24,
                   | "startDateMonth": 3,
                   | "startDateYear": 2014,
                   | "endDateDay": 24,
                   | "endDateMonth": 3,
                   | "endDateYear": 2015,
                   | "leaseTerm": {
                   | "years": 1,
                   | "days": 1,
                   | "daysInPartialYear": 365
                   | },
                   | "year1Rent": 160000,
                   | "year2Rent": 30000
                   | },
                   | "isLinked": false,
                   | "taxReliefDetails": {
                   | "taxReliefCode": 35
                   | },
                   | "relevantRentDetails" : {
                   | "relevantRent": 999
                   | }
                   |}
                   |""".stripMargin
              )
            )

          val expectedResponse: JsValue = Json.parse(
            s"""
               |{
               | "result": [
               | {
               | "totalTax": 2325,
               | "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
               | "npv": 182594,
               | "taxCalcs": [
               | {
               | "taxType": "premium",
               | "calcType": "slab",
               | "taxDue": 2000,
               | "rate": 1
               | },
               | {
               | "taxType": "rent",
               | "calcType": "slice",
               | "taxDue": 325,
               | "slices": [
               | {
               | "from": 0,
               | "to": 150000,
               | "rate": 0,
               | "taxDue": 0
               | },
               | {
               | "from": 150000,
               | "to": -1,
               | "rate": 1,
               | "taxDue": 325
               | }
               | ]
               | }
               | ]
               | }
               | ]
               |}
               |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe expectedResponse

        }
        "return the result with 3% Tax rate on premium and 1% tax rate on rent when premium > 250000 and npv > 150000" in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                s"""
                   |{
                   | "holdingType": "Leasehold",
                   | "propertyType": "Mixed",
                   | "effectiveDateDay": 24,
                   | "effectiveDateMonth": 3,
                   | "effectiveDateYear": 2014,
                   | "premium": 500000,
                   | "highestRent": 0,
                   | "leaseDetails": {
                   | "startDateDay": 24,
                   | "startDateMonth": 3,
                   | "startDateYear": 2014,
                   | "endDateDay": 24,
                   | "endDateMonth": 3,
                   | "endDateYear": 2015,
                   | "leaseTerm": {
                   | "years": 1,
                   | "days": 1,
                   | "daysInPartialYear": 365
                   | },
                   | "year1Rent": 160000,
                   | "year2Rent": 30000
                   | },
                   | "isLinked": false,
                   | "taxReliefDetails": {
                   | "taxReliefCode": 35
                   | },
                   | "relevantRentDetails" : {
                   | "relevantRent": 999
                   | }
                   |}
                   |""".stripMargin
              )
            )

          val expectedResponse: JsValue = Json.parse(
            s"""
               |{
               | "result": [
               | {
               | "totalTax": 15325,
               | "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
               | "npv": 182594,
               | "taxCalcs": [
               | {
               | "taxType": "premium",
               | "calcType": "slab",
               | "taxDue": 15000,
               | "rate": 3
               | },
               | {
               | "taxType": "rent",
               | "calcType": "slice",
               | "taxDue": 325,
               | "slices": [
               | {
               | "from": 0,
               | "to": 150000,
               | "rate": 0,
               | "taxDue": 0
               | },
               | {
               | "from": 150000,
               | "to": -1,
               | "rate": 1,
               | "taxDue": 325
               | }
               | ]
               | }
               | ]
               | }
               | ]
               |}
               |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe expectedResponse

        }
        "return the result with 4% Tax rate on premium and 1% tax rate on rent when (premium > 500000 and premium <= 9999999999) and npv > 150000 " in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                s"""
                   |{
                   | "holdingType": "Leasehold",
                   | "propertyType": "Non-residential",
                   | "effectiveDateDay": 24,
                   | "effectiveDateMonth": 3,
                   | "effectiveDateYear": 2014,
                   | "premium": 600000,
                   | "highestRent": 0,
                   | "leaseDetails": {
                   | "startDateDay": 24,
                   | "startDateMonth": 3,
                   | "startDateYear": 2014,
                   | "endDateDay": 24,
                   | "endDateMonth": 3,
                   | "endDateYear": 2015,
                   | "leaseTerm": {
                   | "years": 1,
                   | "days": 1,
                   | "daysInPartialYear": 365
                   | },
                   | "year1Rent": 160000,
                   | "year2Rent": 30000
                   | },
                   | "isLinked": false,
                   | "taxReliefDetails": {
                   | "taxReliefCode": 35
                   | },
                   | "relevantRentDetails" : {
                   | "relevantRent": 800
                   | }
                   |}
                   |""".stripMargin
              )
            )
          val expectedResponse: JsValue = Json.parse(
            s"""
               |{
               | "result": [
               | {
               | "totalTax": 24325,
               | "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
               | "npv": 182594,
               | "taxCalcs": [
               | {
               | "taxType": "premium",
               | "calcType": "slab",
               | "taxDue": 24000,
               | "rate": 4
               | },
               | {
               | "taxType": "rent",
               | "calcType": "slice",
               | "taxDue": 325,
               | "slices": [
               | {
               | "from": 0,
               | "to": 150000,
               | "rate": 0,
               | "taxDue": 0
               | },
               | {
               | "from": 150000,
               | "to": -1,
               | "rate": 1,
               | "taxDue": 325
               | }
               | ]
               | }
               | ]
               | }
               | ]
               |}
               |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe expectedResponse
        }
      }
      // SDLT - Tax Calc Case - 47 - Add Mixed Logic
      "the TaxReliefCode is ReliefFrom15PercentRate: 35" when {
        "the transaction is not linked" when {
          "the average rent is 1000 or more" when {
            "date is between the 6th April 2013 and the 17th March 2016 " must {
              "return tax relief response" when {
                "Property Type is Non-residential: premium 250000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Non-residential",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 250000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 35
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 1000
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 2500,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 2500,
                      |          "rate": 1
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Non-residential: premium 500000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Non-residential",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 500000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 35
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 1000
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 15000,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 15000,
                      |          "rate": 3
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Non-residential: premium 1000000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Non-residential",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 1000000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 35
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 1000
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 40000,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 40000,
                      |          "rate": 4
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Mixed: premium 250000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Mixed",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 250000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 35
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 1000
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 2500,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 2500,
                      |          "rate": 1
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Mixed: premium 500000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Mixed",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 500000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 35
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 1000
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 15000,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 15000,
                      |          "rate": 3
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Mixed: premium 1000000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Mixed",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 1000000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 35
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 1000
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 40000,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 40000,
                      |          "rate": 4
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Non-residential: npv > 150000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Non-residential",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 1000000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 160000,
                          |    "year2Rent": 160000
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 35
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 1000
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 41539,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 303951,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 40000,
                      |          "rate": 4
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 1539,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 1539
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Mixed: npv > 150000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Mixed",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 1000000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 160000,
                          |    "year2Rent": 160000
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 35
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 1000
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 41539,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 303951,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 40000,
                      |          "rate": 4
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 1539,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 1539
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
              }
            }
          }
        }
      }

      //SDLT - Tax Calc - 2016 budget relief reasons without tax relief - Add Mixed Logic
      "TaxRelief code is ReliefFrom15PercentRate(35) or RightToBuy(22) , isLinked = false, relevantRent < 1000 and from 2016/03/17" must {
        "return 0% tax on premium and 0% on rent when premium <= 150000 and npv <= 150000 and Property type is Mixed " in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                s"""
                   |{
                   | "holdingType": "Leasehold",
                   | "propertyType": "Mixed",
                   | "effectiveDateDay": 1,
                   | "effectiveDateMonth": 1,
                   | "effectiveDateYear": 2018,
                   | "premium": 150000,
                   | "highestRent": 0,
                   | "leaseDetails": {
                   | "startDateDay": 1,
                   | "startDateMonth": 1,
                   | "startDateYear": 2018,
                   | "endDateDay": 1,
                   | "endDateMonth": 1,
                   | "endDateYear": 2019,
                   | "leaseTerm": {
                   | "years": 1,
                   | "days": 1,
                   | "daysInPartialYear": 365
                   | },
                   | "year1Rent": 100000,
                   | "year2Rent": 999
                   | },
                   | "isLinked": false,
                   | "taxReliefDetails": {
                   | "taxReliefCode": 35
                   | },
                   | "relevantRentDetails" : {
                   | "contractPre201603": "Yes",
                   | "contractVariedPost201603": "No",
                   | "relevantRent": 999
                   | }
                   |}
                   |""".stripMargin
              )
            )
          val expectedResponse:JsValue = Json.parse(
            s"""
               |{
               |  "result": [
               |    {
               |      "totalTax": 0,
               |      "resultHeading": "Results based on SDLT rules from 17 March 2016",
               |      "npv": 97550,
               |      "taxCalcs": [
               |        {
               |          "taxType": "rent",
               |          "calcType": "slice",
               |          "taxDue": 0,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Rent bands (£)",
               |          "detailFooter": "SDLT due on the rent",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 5000000,
               |              "rate": 1,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 5000000,
               |              "to": -1,
               |              "rate": 2,
               |              "taxDue": 0
               |            }
               |          ]
               |        },
               |        {
               |          "taxType": "premium",
               |          "calcType": "slice",
               |          "taxDue": 0,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Premium bands (£)",
               |          "detailFooter": "SDLT due on the premium",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 250000,
               |              "rate": 2,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 250000,
               |              "to": -1,
               |              "rate": 5,
               |              "taxDue": 0
               |            }
               |          ]
               |        }
               |      ]
               |    }
               |  ]
               |}
               |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe expectedResponse

        }
        "return 0% tax on premium and 0% on rent when premium <= 150000 and npv <= 150000 and Property type is Non-residential " in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                s"""
                   |{
                   | "holdingType": "Leasehold",
                   | "propertyType": "Non-residential",
                   | "effectiveDateDay": 1,
                   | "effectiveDateMonth": 1,
                   | "effectiveDateYear": 2018,
                   | "premium": 150000,
                   | "highestRent": 0,
                   | "leaseDetails": {
                   | "startDateDay": 1,
                   | "startDateMonth": 1,
                   | "startDateYear": 2018,
                   | "endDateDay": 1,
                   | "endDateMonth": 1,
                   | "endDateYear": 2019,
                   | "leaseTerm": {
                   | "years": 1,
                   | "days": 1,
                   | "daysInPartialYear": 365
                   | },
                   | "year1Rent": 100000,
                   | "year2Rent": 999
                   | },
                   | "isLinked": false,
                   | "taxReliefDetails": {
                   | "taxReliefCode": 35
                   | },
                   | "relevantRentDetails" : {
                   | "contractPre201603": "Yes",
                   | "contractVariedPost201603": "No",
                   | "relevantRent": 999
                   | }
                   |}
                   |""".stripMargin
              )
            )
          val expectedResponse:JsValue = Json.parse(
            s"""
               |{
               |  "result": [
               |    {
               |      "totalTax": 0,
               |      "resultHeading": "Results based on SDLT rules from 17 March 2016",
               |      "npv": 97550,
               |      "taxCalcs": [
               |        {
               |          "taxType": "rent",
               |          "calcType": "slice",
               |          "taxDue": 0,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Rent bands (£)",
               |          "detailFooter": "SDLT due on the rent",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 5000000,
               |              "rate": 1,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 5000000,
               |              "to": -1,
               |              "rate": 2,
               |              "taxDue": 0
               |            }
               |          ]
               |        },
               |        {
               |          "taxType": "premium",
               |          "calcType": "slice",
               |          "taxDue": 0,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Premium bands (£)",
               |          "detailFooter": "SDLT due on the premium",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 250000,
               |              "rate": 2,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 250000,
               |              "to": -1,
               |              "rate": 5,
               |              "taxDue": 0
               |            }
               |          ]
               |        }
               |      ]
               |    }
               |  ]
               |}
               |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe expectedResponse

        }
        "return 2% tax on premium and 1% on rent when (150000 < premium <= 250000 ) and (150000 < npv <= 5000000) and Property type is Mixed" in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                s"""
                   |{
                   | "holdingType": "Leasehold",
                   | "propertyType": "Mixed",
                   | "effectiveDateDay": 1,
                   | "effectiveDateMonth": 1,
                   | "effectiveDateYear": 2018,
                   | "premium": 250000,
                   | "highestRent": 0,
                   | "leaseDetails": {
                   | "startDateDay": 1,
                   | "startDateMonth": 1,
                   | "startDateYear": 2018,
                   | "endDateDay": 1,
                   | "endDateMonth": 1,
                   | "endDateYear": 2019,
                   | "leaseTerm": {
                   | "years": 1,
                   | "days": 1,
                   | "daysInPartialYear": 365
                   | },
                   | "year1Rent": 100000,
                   | "year2Rent": 400000
                   | },
                   | "isLinked": false,
                   | "taxReliefDetails": {
                   | "taxReliefCode": 22
                   | },
                   | "relevantRentDetails" : {
                   | "contractPre201603": "Yes",
                   | "contractVariedPost201603": "No",
                   | "relevantRent": 999
                   | }
                   |}
                   |""".stripMargin
              )
            )
          val expectedResponse:JsValue = Json.parse(
            s"""{
               |  "result": [
               |    {
               |      "totalTax": 5200,
               |      "resultHeading": "Results based on SDLT rules from 17 March 2016",
               |      "npv": 470022,
               |      "taxCalcs": [
               |        {
               |          "taxType": "rent",
               |          "calcType": "slice",
               |          "taxDue": 3200,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Rent bands (£)",
               |          "detailFooter": "SDLT due on the rent",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 5000000,
               |              "rate": 1,
               |              "taxDue": 3200
               |            },
               |            {
               |              "from": 5000000,
               |              "to": -1,
               |              "rate": 2,
               |              "taxDue": 0
               |            }
               |          ]
               |        },
               |        {
               |          "taxType": "premium",
               |          "calcType": "slice",
               |          "taxDue": 2000,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Premium bands (£)",
               |          "detailFooter": "SDLT due on the premium",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 250000,
               |              "rate": 2,
               |              "taxDue": 2000
               |            },
               |            {
               |              "from": 250000,
               |              "to": -1,
               |              "rate": 5,
               |              "taxDue": 0
               |            }
               |          ]
               |        }
               |      ]
               |    }
               |  ]
               |}
               |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe expectedResponse

        }
        "return 2% tax on premium and 1% on rent when (150000 < premium <= 250000 ) and (150000 < npv <= 5000000) and Property type is Non-residential" in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                s"""
                   |{
                   | "holdingType": "Leasehold",
                   | "propertyType": "Non-residential",
                   | "effectiveDateDay": 1,
                   | "effectiveDateMonth": 1,
                   | "effectiveDateYear": 2018,
                   | "premium": 250000,
                   | "highestRent": 0,
                   | "leaseDetails": {
                   | "startDateDay": 1,
                   | "startDateMonth": 1,
                   | "startDateYear": 2018,
                   | "endDateDay": 1,
                   | "endDateMonth": 1,
                   | "endDateYear": 2019,
                   | "leaseTerm": {
                   | "years": 1,
                   | "days": 1,
                   | "daysInPartialYear": 365
                   | },
                   | "year1Rent": 100000,
                   | "year2Rent": 400000
                   | },
                   | "isLinked": false,
                   | "taxReliefDetails": {
                   | "taxReliefCode": 22
                   | },
                   | "relevantRentDetails" : {
                   | "contractPre201603": "Yes",
                   | "contractVariedPost201603": "No",
                   | "relevantRent": 999
                   | }
                   |}
                   |""".stripMargin
              )
            )
          val expectedResponse:JsValue = Json.parse(
            s"""{
               |  "result": [
               |    {
               |      "totalTax": 5200,
               |      "resultHeading": "Results based on SDLT rules from 17 March 2016",
               |      "npv": 470022,
               |      "taxCalcs": [
               |        {
               |          "taxType": "rent",
               |          "calcType": "slice",
               |          "taxDue": 3200,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Rent bands (£)",
               |          "detailFooter": "SDLT due on the rent",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 5000000,
               |              "rate": 1,
               |              "taxDue": 3200
               |            },
               |            {
               |              "from": 5000000,
               |              "to": -1,
               |              "rate": 2,
               |              "taxDue": 0
               |            }
               |          ]
               |        },
               |        {
               |          "taxType": "premium",
               |          "calcType": "slice",
               |          "taxDue": 2000,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Premium bands (£)",
               |          "detailFooter": "SDLT due on the premium",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 250000,
               |              "rate": 2,
               |              "taxDue": 2000
               |            },
               |            {
               |              "from": 250000,
               |              "to": -1,
               |              "rate": 5,
               |              "taxDue": 0
               |            }
               |          ]
               |        }
               |      ]
               |    }
               |  ]
               |}
               |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe expectedResponse

        }
        "return 5% tax on premium and 2% on rent when (250000 < premium <= 9999999999 ) and (5000000 < npv <= 9999999999) and Property type is Mixed" in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                s"""
                   |{
                   | "holdingType": "Leasehold",
                   | "propertyType": "Mixed",
                   | "effectiveDateDay": 1,
                   | "effectiveDateMonth": 1,
                   | "effectiveDateYear": 2018,
                   | "premium": 500000,
                   | "highestRent": 0,
                   | "leaseDetails": {
                   | "startDateDay": 1,
                   | "startDateMonth": 1,
                   | "startDateYear": 2018,
                   | "endDateDay": 1,
                   | "endDateMonth": 1,
                   | "endDateYear": 2019,
                   | "leaseTerm": {
                   | "years": 1,
                   | "days": 1,
                   | "daysInPartialYear": 365
                   | },
                   | "year1Rent": 5000000,
                   | "year2Rent": 4000000
                   | },
                   | "isLinked": false,
                   | "taxReliefDetails": {
                   | "taxReliefCode": 35
                   | },
                   | "relevantRentDetails" : {
                   |  "contractPre201603": "Yes",
                   |  "contractVariedPost201603": "No",
                   |  "relevantRent": 999
                   | }
                   |}
                   |""".stripMargin
              )
            )
          val expectedResponse:JsValue = Json.parse(
            s"""{
               |  "result": [
               |    {
               |      "totalTax": 134299,
               |      "resultHeading": "Results based on SDLT rules from 17 March 2016",
               |      "npv": 8564960,
               |      "taxCalcs": [
               |        {
               |          "taxType": "rent",
               |          "calcType": "slice",
               |          "taxDue": 119799,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Rent bands (£)",
               |          "detailFooter": "SDLT due on the rent",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 5000000,
               |              "rate": 1,
               |              "taxDue": 48500
               |            },
               |            {
               |              "from": 5000000,
               |              "to": -1,
               |              "rate": 2,
               |              "taxDue": 71299
               |            }
               |          ]
               |        },
               |        {
               |          "taxType": "premium",
               |          "calcType": "slice",
               |          "taxDue": 14500,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Premium bands (£)",
               |          "detailFooter": "SDLT due on the premium",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 250000,
               |              "rate": 2,
               |              "taxDue": 2000
               |            },
               |            {
               |              "from": 250000,
               |              "to": -1,
               |              "rate": 5,
               |              "taxDue": 12500
               |            }
               |          ]
               |        }
               |      ]
               |    }
               |  ]
               |}
               |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe expectedResponse

        }
        "return 5% tax on premium and 2% on rent when (250000 < premium <= 9999999999 ) and (5000000 < npv <= 9999999999) and Property type is Non-residential" in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                s"""
                   |{
                   | "holdingType": "Leasehold",
                   | "propertyType": "Non-residential",
                   | "effectiveDateDay": 1,
                   | "effectiveDateMonth": 1,
                   | "effectiveDateYear": 2018,
                   | "premium": 500000,
                   | "highestRent": 0,
                   | "leaseDetails": {
                   | "startDateDay": 1,
                   | "startDateMonth": 1,
                   | "startDateYear": 2018,
                   | "endDateDay": 1,
                   | "endDateMonth": 1,
                   | "endDateYear": 2019,
                   | "leaseTerm": {
                   | "years": 1,
                   | "days": 1,
                   | "daysInPartialYear": 365
                   | },
                   | "year1Rent": 5000000,
                   | "year2Rent": 4000000
                   | },
                   | "isLinked": false,
                   | "taxReliefDetails": {
                   | "taxReliefCode": 35
                   | },
                   | "relevantRentDetails" : {
                   |  "contractPre201603": "Yes",
                   |  "contractVariedPost201603": "No",
                   |  "relevantRent": 999
                   | }
                   |}
                   |""".stripMargin
              )
            )
          val expectedResponse:JsValue = Json.parse(
            s"""{
               |  "result": [
               |    {
               |      "totalTax": 134299,
               |      "resultHeading": "Results based on SDLT rules from 17 March 2016",
               |      "npv": 8564960,
               |      "taxCalcs": [
               |        {
               |          "taxType": "rent",
               |          "calcType": "slice",
               |          "taxDue": 119799,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Rent bands (£)",
               |          "detailFooter": "SDLT due on the rent",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 5000000,
               |              "rate": 1,
               |              "taxDue": 48500
               |            },
               |            {
               |              "from": 5000000,
               |              "to": -1,
               |              "rate": 2,
               |              "taxDue": 71299
               |            }
               |          ]
               |        },
               |        {
               |          "taxType": "premium",
               |          "calcType": "slice",
               |          "taxDue": 14500,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 17 March 2016",
               |          "bandHeading": "Premium bands (£)",
               |          "detailFooter": "SDLT due on the premium",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 150000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 150000,
               |              "to": 250000,
               |              "rate": 2,
               |              "taxDue": 2000
               |            },
               |            {
               |              "from": 250000,
               |              "to": -1,
               |              "rate": 5,
               |              "taxDue": 12500
               |            }
               |          ]
               |        }
               |      ]
               |    }
               |  ]
               |}
               |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe expectedResponse

        }
      }
      // SDLT - Tax Calc Case - 32c
      "the TaxReliefCode is RightToBuy: 22" when {
        "the transaction is not linked" when {
          "the average rent is less than 1000" when {
            "date is between the 12th March 2008 and the 17th March 2016 " must {
              "return tax relief response" when {
                "Property Type is Non-residential: premium 250000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Non-residential",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 250000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 22
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 999
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 2500,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 2500,
                      |          "rate": 1
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Non-residential: premium 500000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Non-residential",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 500000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 22
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 999
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 15000,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 15000,
                      |          "rate": 3
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Non-residential: premium 1000000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Non-residential",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 1000000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 22
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 999
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 40000,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 40000,
                      |          "rate": 4
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Mixed: premium 250000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Mixed",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 250000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 22
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 999
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 2500,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 2500,
                      |          "rate": 1
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Mixed: premium 500000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Mixed",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 500000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 22
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 999
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 15000,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 15000,
                      |          "rate": 3
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Mixed: premium 1000000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Mixed",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 1000000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 999,
                          |    "year2Rent": 999
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 22
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 999
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 40000,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 1897,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 40000,
                      |          "rate": 4
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 0,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 0
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Non-residential: npv > 150000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Non-residential",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 1000000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 160000,
                          |    "year2Rent": 160000
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 22
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 999
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 41539,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 303951,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 40000,
                      |          "rate": 4
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 1539,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 1539
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
                "Property Type is Mixed: npv > 150000" in {
                  val request: WSResponse = ws
                    .url(calculateUrl)
                    .post(
                      Json.parse(
                        """
                          |{
                          |  "holdingType": "Leasehold",
                          |  "propertyType": "Mixed",
                          |  "effectiveDateDay": 6,
                          |  "effectiveDateMonth": 4,
                          |  "effectiveDateYear": 2014,
                          |  "premium": 1000000,
                          |  "highestRent": 0,
                          |  "leaseDetails": {
                          |    "startDateDay": 6,
                          |    "startDateMonth": 4,
                          |    "startDateYear": 2014,
                          |    "endDateDay": 6,
                          |    "endDateMonth": 4,
                          |    "endDateYear": 2015,
                          |    "leaseTerm": {
                          |      "years": 1,
                          |      "days": 1,
                          |      "daysInPartialYear": 365
                          |    },
                          |    "year1Rent": 160000,
                          |    "year2Rent": 160000
                          |  },
                          |  "isLinked": false,
                          |  "taxReliefDetails": {
                          |   "taxReliefCode": 22
                          | },
                          | "relevantRentDetails": {
                          |    "relevantRent": 999
                          | }
                          |}
                          |""".stripMargin
                      )
                    )

                  val responseJson = Json.parse(
                    """
                      |{
                      |  "result": [
                      |    {
                      |      "totalTax": 41539,
                      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                      |      "npv": 303951,
                      |      "taxCalcs": [
                      |        {
                      |          "taxType": "premium",
                      |          "calcType": "slab",
                      |          "taxDue": 40000,
                      |          "rate": 4
                      |        },
                      |        {
                      |          "taxType": "rent",
                      |          "calcType": "slice",
                      |          "taxDue": 1539,
                      |          "slices": [
                      |            {
                      |              "from": 0,
                      |              "to": 150000,
                      |              "rate": 0,
                      |              "taxDue": 0
                      |            },
                      |            {
                      |              "from": 150000,
                      |              "to": -1,
                      |              "rate": 1,
                      |              "taxDue": 1539
                      |            }
                      |          ]
                      |        }
                      |      ]
                      |    }
                      |  ]
                      |}""".stripMargin)

                  request.status shouldBe OK
                  request.json shouldBe responseJson
                }
              }
            }
          }
        }
      }

      // SDLT - Tax Calc Case - 32 - Leased
      "TaxRelief Code is RightToBuy(22), isLinked = false, relevantRent < 1000, and date is before 12/03/08" must {
        "return tax relief response" when {
          "Property type is Non-residential & premium is £150000" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  s"""
                     |{
                     | "holdingType": "Leasehold",
                     | "propertyType": "Non-residential",
                     | "effectiveDateDay": 11,
                     | "effectiveDateMonth": 3,
                     | "effectiveDateYear": 2008,
                     | "premium": 150000,
                     | "highestRent": 0,
                     | "leaseDetails": {
                     | "startDateDay": 11,
                     | "startDateMonth": 3,
                     | "startDateYear": 2008,
                     | "endDateDay": 11,
                     | "endDateMonth": 3,
                     | "endDateYear": 2009,
                     | "leaseTerm": {
                     | "years": 1,
                     | "days": 1,
                     | "daysInPartialYear": 365
                     | },
                     | "year1Rent": 999,
                     | "year2Rent": 999
                     | },
                     | "isLinked": false,
                     | "taxReliefDetails": {
                     | "taxReliefCode": 22
                     | },
                     | "relevantRentDetails" : {
                     | "relevantRent": 999
                     | }
                     |}
                     |""".stripMargin
                )
              )

            val expectedResponse: JsValue = Json.parse(
              s"""
                 |{
                 |  "result": [
                 |    {
                 |      "totalTax": 0,
                 |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                 |      "npv": 1897,
                 |      "taxCalcs": [
                 |        {
                 |          "taxType": "rent",
                 |          "calcType": "slice",
                 |          "taxDue": 0,
                 |          "slices": [
                 |            {
                 |              "from": 0,
                 |              "to": 150000,
                 |              "rate": 0,
                 |              "taxDue": 0
                 |            },
                 |            {
                 |              "from": 150000,
                 |              "to": -1,
                 |              "rate": 1,
                 |              "taxDue": 0
                 |            }
                 |          ]
                 |        },
                 |        {
                 |          "taxType": "premium",
                 |          "calcType": "slab",
                 |          "taxDue": 0,
                 |          "rate": 0
                 |        }
                 |      ]
                 |    }
                 |  ]
                 |}
                 |""".stripMargin
            )

            request.status shouldBe OK
            request.json shouldBe expectedResponse
          }
          "Property type is Non-residential & premium is £250000" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  s"""
                     |{
                     | "holdingType": "Leasehold",
                     | "propertyType": "Non-residential",
                     | "effectiveDateDay": 11,
                     | "effectiveDateMonth": 3,
                     | "effectiveDateYear": 2008,
                     | "premium": 250000,
                     | "highestRent": 0,
                     | "leaseDetails": {
                     | "startDateDay": 11,
                     | "startDateMonth": 3,
                     | "startDateYear": 2008,
                     | "endDateDay": 11,
                     | "endDateMonth": 3,
                     | "endDateYear": 2009,
                     | "leaseTerm": {
                     | "years": 1,
                     | "days": 1,
                     | "daysInPartialYear": 365
                     | },
                     | "year1Rent": 999,
                     | "year2Rent": 999
                     | },
                     | "isLinked": false,
                     | "taxReliefDetails": {
                     | "taxReliefCode": 22
                     | },
                     | "relevantRentDetails" : {
                     | "relevantRent": 999
                     | }
                     |}
                     |""".stripMargin
                )
              )

            val expectedResponse: JsValue = Json.parse(
              s"""
                 |{
                 |  "result": [
                 |    {
                 |      "totalTax": 2500,
                 |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                 |      "npv": 1897,
                 |      "taxCalcs": [
                 |        {
                 |          "taxType": "rent",
                 |          "calcType": "slice",
                 |          "taxDue": 0,
                 |          "slices": [
                 |            {
                 |              "from": 0,
                 |              "to": 150000,
                 |              "rate": 0,
                 |              "taxDue": 0
                 |            },
                 |            {
                 |              "from": 150000,
                 |              "to": -1,
                 |              "rate": 1,
                 |              "taxDue": 0
                 |            }
                 |          ]
                 |        },
                 |        {
                 |          "taxType": "premium",
                 |          "calcType": "slab",
                 |          "taxDue": 2500,
                 |          "rate": 1
                 |        }
                 |      ]
                 |    }
                 |  ]
                 |}
                 |""".stripMargin
            )

            request.status shouldBe OK
            request.json shouldBe expectedResponse
          }
          "Property type is Non-residential & premium is £500000" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  s"""
                     |{
                     | "holdingType": "Leasehold",
                     | "propertyType": "Non-residential",
                     | "effectiveDateDay": 11,
                     | "effectiveDateMonth": 3,
                     | "effectiveDateYear": 2008,
                     | "premium": 500000,
                     | "highestRent": 0,
                     | "leaseDetails": {
                     | "startDateDay": 11,
                     | "startDateMonth": 3,
                     | "startDateYear": 2008,
                     | "endDateDay": 11,
                     | "endDateMonth": 3,
                     | "endDateYear": 2009,
                     | "leaseTerm": {
                     | "years": 1,
                     | "days": 1,
                     | "daysInPartialYear": 365
                     | },
                     | "year1Rent": 999,
                     | "year2Rent": 999
                     | },
                     | "isLinked": false,
                     | "taxReliefDetails": {
                     | "taxReliefCode": 22
                     | },
                     | "relevantRentDetails" : {
                     | "relevantRent": 999
                     | }
                     |}
                     |""".stripMargin
                )
              )

            val expectedResponse: JsValue = Json.parse(
              s"""
                 |{
                 |  "result": [
                 |    {
                 |      "totalTax": 15000,
                 |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                 |      "npv": 1897,
                 |      "taxCalcs": [
                 |        {
                 |          "taxType": "rent",
                 |          "calcType": "slice",
                 |          "taxDue": 0,
                 |          "slices": [
                 |            {
                 |              "from": 0,
                 |              "to": 150000,
                 |              "rate": 0,
                 |              "taxDue": 0
                 |            },
                 |            {
                 |              "from": 150000,
                 |              "to": -1,
                 |              "rate": 1,
                 |              "taxDue": 0
                 |            }
                 |          ]
                 |        },
                 |        {
                 |          "taxType": "premium",
                 |          "calcType": "slab",
                 |          "taxDue": 15000,
                 |          "rate": 3
                 |        }
                 |      ]
                 |    }
                 |  ]
                 |}
                 |""".stripMargin
            )

            request.status shouldBe OK
            request.json shouldBe expectedResponse
          }
          "Property type is Non-residential & premium is £500001" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  s"""
                     |{
                     | "holdingType": "Leasehold",
                     | "propertyType": "Non-residential",
                     | "effectiveDateDay": 11,
                     | "effectiveDateMonth": 3,
                     | "effectiveDateYear": 2008,
                     | "premium": 500001,
                     | "highestRent": 0,
                     | "leaseDetails": {
                     | "startDateDay": 11,
                     | "startDateMonth": 3,
                     | "startDateYear": 2008,
                     | "endDateDay": 11,
                     | "endDateMonth": 3,
                     | "endDateYear": 2009,
                     | "leaseTerm": {
                     | "years": 1,
                     | "days": 1,
                     | "daysInPartialYear": 365
                     | },
                     | "year1Rent": 999,
                     | "year2Rent": 999
                     | },
                     | "isLinked": false,
                     | "taxReliefDetails": {
                     | "taxReliefCode": 22
                     | },
                     | "relevantRentDetails" : {
                     | "relevantRent": 999
                     | }
                     |}
                     |""".stripMargin
                )
              )

            val expectedResponse: JsValue = Json.parse(
              s"""
                 |{
                 |  "result": [
                 |    {
                 |      "totalTax": 20000,
                 |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                 |      "npv": 1897,
                 |      "taxCalcs": [
                 |        {
                 |          "taxType": "rent",
                 |          "calcType": "slice",
                 |          "taxDue": 0,
                 |          "slices": [
                 |            {
                 |              "from": 0,
                 |              "to": 150000,
                 |              "rate": 0,
                 |              "taxDue": 0
                 |            },
                 |            {
                 |              "from": 150000,
                 |              "to": -1,
                 |              "rate": 1,
                 |              "taxDue": 0
                 |            }
                 |          ]
                 |        },
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
            request.json shouldBe expectedResponse
          }
          "Property type is Mixed & premium is £150000" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  s"""
                     |{
                     | "holdingType": "Leasehold",
                     | "propertyType": "Mixed",
                     | "effectiveDateDay": 11,
                     | "effectiveDateMonth": 3,
                     | "effectiveDateYear": 2008,
                     | "premium": 150000,
                     | "highestRent": 0,
                     | "leaseDetails": {
                     | "startDateDay": 11,
                     | "startDateMonth": 3,
                     | "startDateYear": 2008,
                     | "endDateDay": 11,
                     | "endDateMonth": 3,
                     | "endDateYear": 2009,
                     | "leaseTerm": {
                     | "years": 1,
                     | "days": 1,
                     | "daysInPartialYear": 365
                     | },
                     | "year1Rent": 999,
                     | "year2Rent": 999
                     | },
                     | "isLinked": false,
                     | "taxReliefDetails": {
                     | "taxReliefCode": 22
                     | },
                     | "relevantRentDetails" : {
                     | "relevantRent": 999
                     | }
                     |}
                     |""".stripMargin
                )
              )

            val expectedResponse: JsValue = Json.parse(
              s"""
                 |{
                 |  "result": [
                 |    {
                 |      "totalTax": 0,
                 |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                 |      "npv": 1897,
                 |      "taxCalcs": [
                 |        {
                 |          "taxType": "rent",
                 |          "calcType": "slice",
                 |          "taxDue": 0,
                 |          "slices": [
                 |            {
                 |              "from": 0,
                 |              "to": 150000,
                 |              "rate": 0,
                 |              "taxDue": 0
                 |            },
                 |            {
                 |              "from": 150000,
                 |              "to": -1,
                 |              "rate": 1,
                 |              "taxDue": 0
                 |            }
                 |          ]
                 |        },
                 |        {
                 |          "taxType": "premium",
                 |          "calcType": "slab",
                 |          "taxDue": 0,
                 |          "rate": 0
                 |        }
                 |      ]
                 |    }
                 |  ]
                 |}
                 |""".stripMargin
            )

            request.status shouldBe OK
            request.json shouldBe expectedResponse
          }
          "Property type is Mixed & premium is £250000" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  s"""
                     |{
                     | "holdingType": "Leasehold",
                     | "propertyType": "Mixed",
                     | "effectiveDateDay": 11,
                     | "effectiveDateMonth": 3,
                     | "effectiveDateYear": 2008,
                     | "premium": 250000,
                     | "highestRent": 0,
                     | "leaseDetails": {
                     | "startDateDay": 11,
                     | "startDateMonth": 3,
                     | "startDateYear": 2008,
                     | "endDateDay": 11,
                     | "endDateMonth": 3,
                     | "endDateYear": 2009,
                     | "leaseTerm": {
                     | "years": 1,
                     | "days": 1,
                     | "daysInPartialYear": 365
                     | },
                     | "year1Rent": 999,
                     | "year2Rent": 999
                     | },
                     | "isLinked": false,
                     | "taxReliefDetails": {
                     | "taxReliefCode": 22
                     | },
                     | "relevantRentDetails" : {
                     | "relevantRent": 999
                     | }
                     |}
                     |""".stripMargin
                )
              )

            val expectedResponse: JsValue = Json.parse(
              s"""
                 |{
                 |  "result": [
                 |    {
                 |      "totalTax": 2500,
                 |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                 |      "npv": 1897,
                 |      "taxCalcs": [
                 |        {
                 |          "taxType": "rent",
                 |          "calcType": "slice",
                 |          "taxDue": 0,
                 |          "slices": [
                 |            {
                 |              "from": 0,
                 |              "to": 150000,
                 |              "rate": 0,
                 |              "taxDue": 0
                 |            },
                 |            {
                 |              "from": 150000,
                 |              "to": -1,
                 |              "rate": 1,
                 |              "taxDue": 0
                 |            }
                 |          ]
                 |        },
                 |        {
                 |          "taxType": "premium",
                 |          "calcType": "slab",
                 |          "taxDue": 2500,
                 |          "rate": 1
                 |        }
                 |      ]
                 |    }
                 |  ]
                 |}
                 |""".stripMargin
            )

            request.status shouldBe OK
            request.json shouldBe expectedResponse
          }
          "Property type is Mixed & premium is £500000" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  s"""
                     |{
                     | "holdingType": "Leasehold",
                     | "propertyType": "Mixed",
                     | "effectiveDateDay": 11,
                     | "effectiveDateMonth": 3,
                     | "effectiveDateYear": 2008,
                     | "premium": 500000,
                     | "highestRent": 0,
                     | "leaseDetails": {
                     | "startDateDay": 11,
                     | "startDateMonth": 3,
                     | "startDateYear": 2008,
                     | "endDateDay": 11,
                     | "endDateMonth": 3,
                     | "endDateYear": 2009,
                     | "leaseTerm": {
                     | "years": 1,
                     | "days": 1,
                     | "daysInPartialYear": 365
                     | },
                     | "year1Rent": 999,
                     | "year2Rent": 999
                     | },
                     | "isLinked": false,
                     | "taxReliefDetails": {
                     | "taxReliefCode": 22
                     | },
                     | "relevantRentDetails" : {
                     | "relevantRent": 999
                     | }
                     |}
                     |""".stripMargin
                )
              )

            val expectedResponse: JsValue = Json.parse(
              s"""
                 |{
                 |  "result": [
                 |    {
                 |      "totalTax": 15000,
                 |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                 |      "npv": 1897,
                 |      "taxCalcs": [
                 |        {
                 |          "taxType": "rent",
                 |          "calcType": "slice",
                 |          "taxDue": 0,
                 |          "slices": [
                 |            {
                 |              "from": 0,
                 |              "to": 150000,
                 |              "rate": 0,
                 |              "taxDue": 0
                 |            },
                 |            {
                 |              "from": 150000,
                 |              "to": -1,
                 |              "rate": 1,
                 |              "taxDue": 0
                 |            }
                 |          ]
                 |        },
                 |        {
                 |          "taxType": "premium",
                 |          "calcType": "slab",
                 |          "taxDue": 15000,
                 |          "rate": 3
                 |        }
                 |      ]
                 |    }
                 |  ]
                 |}
                 |""".stripMargin
            )

            request.status shouldBe OK
            request.json shouldBe expectedResponse
          }
          "Property type is Mixed & premium is £500001" in {
            val request: WSResponse = ws
              .url(calculateUrl)
              .post(
                Json.parse(
                  s"""
                     |{
                     | "holdingType": "Leasehold",
                     | "propertyType": "Mixed",
                     | "effectiveDateDay": 11,
                     | "effectiveDateMonth": 3,
                     | "effectiveDateYear": 2008,
                     | "premium": 500001,
                     | "highestRent": 0,
                     | "leaseDetails": {
                     | "startDateDay": 11,
                     | "startDateMonth": 3,
                     | "startDateYear": 2008,
                     | "endDateDay": 11,
                     | "endDateMonth": 3,
                     | "endDateYear": 2009,
                     | "leaseTerm": {
                     | "years": 1,
                     | "days": 1,
                     | "daysInPartialYear": 365
                     | },
                     | "year1Rent": 999,
                     | "year2Rent": 999
                     | },
                     | "isLinked": false,
                     | "taxReliefDetails": {
                     | "taxReliefCode": 22
                     | },
                     | "relevantRentDetails" : {
                     | "relevantRent": 999
                     | }
                     |}
                     |""".stripMargin
                )
              )

            val expectedResponse: JsValue = Json.parse(
              s"""
                 |{
                 |  "result": [
                 |    {
                 |      "totalTax": 20000,
                 |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                 |      "npv": 1897,
                 |      "taxCalcs": [
                 |        {
                 |          "taxType": "rent",
                 |          "calcType": "slice",
                 |          "taxDue": 0,
                 |          "slices": [
                 |            {
                 |              "from": 0,
                 |              "to": 150000,
                 |              "rate": 0,
                 |              "taxDue": 0
                 |            },
                 |            {
                 |              "from": 150000,
                 |              "to": -1,
                 |              "rate": 1,
                 |              "taxDue": 0
                 |            }
                 |          ]
                 |        },
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
            request.json shouldBe expectedResponse
          }
        }
      }
    }
  }
}
