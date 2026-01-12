/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package leasehold

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
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
                |  "linked": "No",
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
                |  "linked": "No",
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
      "isPartialRelief is true and self assessment needed for Freeport Relief" in {
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
            |   "npv": 0,
            |   "resultHeading": "Self-assessed",
            |   "taxCalcs": [
            |     {
            |      "taxType": "premium",
            |      "calcType": "slab",
            |      "taxDue": 0,
            |      "rate": 0
            |    },
            |    {
            |     "taxType": "rent",
            |     "calcType": "slab",
            |     "taxDue": 0,
            |     "rate": 0
            |    }
            |   ]
            |  }
            | ]
            |}
            |""".stripMargin
        )
        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "with TaxReliefCode: PreCompletionTransaction(34)" when {

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
      }
    }

    "return a 200 and valid result for leaseholdResidential additional property  of an individual " when {
      "effective date is on or after 1/4/2016, replace main residence is true and the TaxRelief Code is PreCompletionTransaction " in {
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
  }
}
