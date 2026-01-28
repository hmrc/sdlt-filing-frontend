/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package leasehold

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import test.base.BaseSpec

class CalculationControllerLeaseHoldTaxReliefISpec extends BaseSpec with GuiceOneServerPerSuite{

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route " should {
    "return a 200 and valid result for leasehold property type " when {
      "the TaxRelief Code is FreeportsTaxSiteRelief " in {
        def request: WSResponse = ws.url(
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

        val responseJson = Json.parse(
          """
            |{
            |  "result": [
            |    {
            |      "totalTax": 0,
            |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
            |      "npv": 1897,
            |      "taxCalcs": [
            |        {
            |          "taxType": "premium",
            |          "calcType": "slab",
            |          "taxDue": 0,
            |          "rate": 0
            |        },
            |        {
            |          "taxType": "rent",
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
        request.json shouldBe responseJson
      }
      "The TaxRelief Code is FreeportsTaxSiteRelief and isPartialRelief is true" in {
        def request: WSResponse = ws.url(
            calculateUrl)
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
                |    "taxReliefCode": 36,
                |    "isPartialRelief": true
                |  }
                |}
                |""".stripMargin
            )
          )

        val responseJson = Json.parse(
          """
            |{
            | "result": [
            |  {
            |   "totalTax": 0,
            |   "resultHeading": "Self-assessed",
            |   "taxCalcs": []
            |  }
            | ]
            |}
            |""".stripMargin
        )
        request.status shouldBe OK
        request.json shouldBe responseJson
      }
      "the TaxRelief Code is InvestmentZonesTaxSiteRelief " in {
        def request: WSResponse = ws.url(
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

        val responseJson = Json.parse(
          """
            |{
            |  "result": [
            |    {
            |      "totalTax": 0,
            |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
            |      "npv": 1897,
            |      "taxCalcs": [
            |        {
            |          "taxType": "premium",
            |          "calcType": "slab",
            |          "taxDue": 0,
            |          "rate": 0
            |        },
            |        {
            |          "taxType": "rent",
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
        request.json shouldBe responseJson
      }
      "The TaxRelief Code is PreCompletionTransaction" when {
        "Property Type is Residential and the date is on or after 6th April 2013" in {
          def request: WSResponse = ws.url(
              calculateUrl)
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
                  |   "taxReliefCode": 34,
                  |   "isPartialRelief": false
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
              |      "totalTax": 0,
              |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
              |      "npv": 1897,
              |      "taxCalcs": [
              |        {
              |          "taxType": "premium",
              |          "calcType": "slab",
              |          "taxDue": 0,
              |          "rate": 0
              |        },
              |        {
              |          "taxType": "rent",
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
          request.json shouldBe responseJson
        }
        "Property Type is Non-Residential and the date is on or after 6th April 2013" in {
          def request: WSResponse = ws.url(
              calculateUrl)
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
                  |   "taxReliefCode": 34,
                  |   "isPartialRelief": false
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
              |      "totalTax": 0,
              |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
              |      "npv": 1897,
              |      "taxCalcs": [
              |        {
              |          "taxType": "premium",
              |          "calcType": "slab",
              |          "taxDue": 0,
              |          "rate": 0
              |        },
              |        {
              |          "taxType": "rent",
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
          request.json shouldBe responseJson
        }
        "Property Type is Mixed and the date is on or after 6th April 2013" in {
          def request: WSResponse = ws.url(
              calculateUrl)
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
                  |   "taxReliefCode": 34,
                  |   "isPartialRelief": false
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
              |      "totalTax": 0,
              |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
              |      "npv": 1897,
              |      "taxCalcs": [
              |        {
              |          "taxType": "premium",
              |          "calcType": "slab",
              |          "taxDue": 0,
              |          "rate": 0
              |        },
              |        {
              |          "taxType": "rent",
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
          request.json shouldBe responseJson
        }
        "Property Type is Residential with additional property and the date is on or after 1st April 2016" in {
          def request: WSResponse = ws.url(
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

          val responseJson = Json.parse(
            """
              |{
              |  "result": [
              |    {
              |      "totalTax": 0,
              |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
              |      "npv": 1897,
              |      "taxCalcs": [
              |        {
              |          "taxType": "premium",
              |          "calcType": "slab",
              |          "taxDue": 0,
              |          "rate": 0
              |        },
              |        {
              |          "taxType": "rent",
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
          request.json shouldBe responseJson
        }
      }
      "The TaxRelief Code is FirstTimeBuyersRelief" when {
        "Property Type is Residential and the date is on or after 25th March 2010 & before 25th March 2012" in {
          def request: WSResponse = ws.url(calculateUrl)
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

          val result = Json.parse(
            """{
              |  "result": [
              |    {
              |      "totalTax": 0,
              |      "resultHeading": "Self-assessed",
              |      "taxCalcs": []
              |    }
              |  ]
              |}
              |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe result
        }
      }
      "The TaxRelief Code is CollectiveEnfranchisementByLeaseholders(25)" when {

        "date within the range on or after 23/04/2009" in {
          def request: WSResponse = ws.url(
              calculateUrl)
            .post(
              Json.parse(
                """{
                  |  "holdingType": "leasehold",
                  |  "propertyType": "Non-residential",
                  |  "effectiveDateDay": 23,
                  |  "effectiveDateMonth": 3,
                  |  "effectiveDateYear": 2010,
                  |  "highestRent": "10000",
                  |  "premium": "0",
                  |  "leaseDetails": {
                  |    "startDateDay": 23,
                  |    "startDateMonth": 3,
                  |    "startDateYear": 2010,
                  |    "endDateDay": 23,
                  |    "endDateMonth": 3,
                  |    "endDateYear": 2015,
                  |    "leaseTerm": {
                  |      "years": 5,
                  |      "days": 1,
                  |      "daysInPartialYear": 366
                  |    },
                  |    "year1Rent": "3000",
                  |    "year2Rent": "5000",
                  |    "year3Rent": "10000",
                  |    "year4Rent": "4000",
                  |    "year5Rent": "6000"
                  |  },
                  |  "taxReliefDetails": {
                  |    "taxReliefCode": 25
                  |  }
                  |}
                  |""".stripMargin
              )
            )

          val selfAssessedResponse: JsValue = Json.parse(
            """{
              |  "result": [
              |    {
              |      "totalTax": 0,
              |      "resultHeading": "Self-assessed",
              |      "taxCalcs": []
              |    }
              |  ]
              |}
              |""".stripMargin
          )

          request.status shouldBe OK
          request.json shouldBe selfAssessedResponse
        }
      }

    }
    "return a 200 and and fall back to existing logic for leasehold property type" when {
      "The TaxRelief Code is FirstTimeBuyersRelief" when {
        "Property Type is Non-residential and the date is on or after 25th March 2010 & before 25th March 2012" in {
          def request: WSResponse = ws.url(calculateUrl)
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
                  |   "taxReliefCode": 32
                  | }
                  |}
                  |""".stripMargin
              )
            )

          val fallBackResult = Json.parse(
            s"""{
               |  "result": [
               |    {
               |      "totalTax": 40000,
               |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
               |      "npv": 1897,
               |      "taxCalcs": [
               |        {
               |          "taxType": "rent",
               |          "calcType": "slice",
               |          "taxDue": 0,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the rent was calculated",
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
               |              "to": -1,
               |              "rate": 1,
               |              "taxDue": 0
               |            }
               |          ]
               |        },
               |        {
               |          "taxType": "premium",
               |          "calcType": "slab",
               |          "taxDue": 40000,
               |          "rate": 4
               |        }
               |      ]
               |    }
               |  ]
               |}
               |""".stripMargin
          )

          request.status shouldBe OK
          request.json shouldBe fallBackResult
        }
        "Property Type is Residential with Additional Property and the date is on or after 25th March 2010 & before 25th March 2012" in {
          def request: WSResponse = ws.url(calculateUrl)
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
                  |   "individual": "Yes",
                  |   "twoOrMoreProperties": "Yes",
                  |   "replaceMainResidence": "Yes"
                  |  },
                  |  "taxReliefDetails": {
                  |   "taxReliefCode": 32
                  | }
                  |}
                  |""".stripMargin
              )
            )

          val fallBackResult = Json.parse(
            s"""{
               |  "result": [
               |    {
               |      "totalTax": 40000,
               |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
               |      "npv": 1897,
               |      "taxCalcs": [
               |        {
               |          "taxType": "rent",
               |          "calcType": "slice",
               |          "taxDue": 0,
               |          "detailHeading": "This is a breakdown of how the amount of SDLT on the rent was calculated",
               |          "bandHeading": "Rent bands (£)",
               |          "detailFooter": "SDLT due on the rent",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 125000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 125000,
               |              "to": -1,
               |              "rate": 1,
               |              "taxDue": 0
               |            }
               |          ]
               |        },
               |        {
               |          "taxType": "premium",
               |          "calcType": "slab",
               |          "taxDue": 40000,
               |          "rate": 4
               |        }
               |      ]
               |    }
               |  ]
               |}
               |""".stripMargin
          )

          request.status shouldBe OK
          request.json shouldBe fallBackResult
        }
      }
      "The TaxRelief Code is CollectiveEnfranchisementByLeaseholders(25)" when {

        "property type is Non-residential, Leasehold and before 22/03/2009" in {
          def request: WSResponse = ws.url(
              calculateUrl)
            .post(
              Json.parse(
                """{
                  |  "holdingType": "leasehold",
                  |  "propertyType": "Non-residential",
                  |  "effectiveDateDay": 1,
                  |  "effectiveDateMonth": 1,
                  |  "effectiveDateYear": 2007,
                  |  "highestRent": "10000",
                  |  "premium": "0",
                  |  "leaseDetails": {
                  |    "startDateDay": 1,
                  |    "startDateMonth": 1,
                  |    "startDateYear": 2007,
                  |    "endDateDay": 1,
                  |    "endDateMonth": 1,
                  |    "endDateYear": 2014,
                  |    "leaseTerm": {
                  |      "years": 7,
                  |      "days": 1,
                  |      "daysInPartialYear": 366
                  |    },
                  |    "year1Rent": "3000",
                  |    "year2Rent": "5000",
                  |    "year3Rent": "10000",
                  |    "year4Rent": "4000",
                  |    "year5Rent": "6000"
                  |  },
                  |  "taxReliefDetails": {
                  |    "taxReliefCode": 25
                  |  }
                  |}
                  |""".stripMargin
              )
            )

          val response: JsValue = Json.parse(
            """{
              |  "result": [
              |    {
              |      "totalTax": 0,
              |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
              |      "npv": 41138,
              |      "taxCalcs": [
              |        {
              |          "taxType": "rent",
              |          "calcType": "slice",
              |          "taxDue": 0,
              |          "detailHeading": "This is a breakdown of how the amount of SDLT on the rent was calculated",
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
          request.json shouldBe response
        }
      }
    }

  }
}
