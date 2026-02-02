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
                      |    "replaceMainResidence": "Yes"
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
                        |    "replaceMainResidence": "Yes"
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
                        |    "replaceMainResidence": "Yes"
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
                        |    "replaceMainResidence": "Yes"
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
                        |    "replaceMainResidence": "Yes"
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
                        |    "replaceMainResidence": "Yes"
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
                      |    "replaceMainResidence": "Yes"
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
    }
  }
}
