/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package freehold

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import test.base.BaseSpec

class CalculationControllerFreeholdTaxReliefISpec extends BaseSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

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
      |""".stripMargin)

  "Hitting the /calculate route" should {

    "return a 200 and valid result for freehold property type" when {

      "with tax relief code: PartExchange(8)" when {

        "residential" in {
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{"holdingType":"Freehold",
                  |"propertyType":"Residential",
                  |"effectiveDateDay":1,
                  |"effectiveDateMonth":1,
                  |"effectiveDateYear":2020,
                  |"highestRent":0,"premium":"750000",
                  |"propertyDetails":{"individual":"Yes",
                  |"twoOrMoreProperties":"No"},
                  |"firstTimeBuyer":"No",
                  |"isLinked": false,
                  |"taxReliefDetails": { "taxReliefCode": 8,
                  |"isPartialRelief": false }
                  |}""".stripMargin
              )
            )

          val responseJson = Json.parse(
            """
              |{"result":[{"totalTax":0,
              |"resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |"taxCalcs":[{"taxType":"premium",
              |"calcType":"slab",
              |"taxDue":0,
              |"rate":0}]}]}
              |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "non-residential" in {
          def request: WSResponse = ws.url(
              calculateUrl)
            .post(
              Json.parse(
                """
                  |{"holdingType":"Freehold",
                  |"propertyType":"Non-residential",
                  |"effectiveDateDay":1,
                  |"effectiveDateMonth":1,
                  |"effectiveDateYear":2020,
                  |"highestRent":0,"premium":"750000",
                  |"propertyDetails":{"individual":"Yes",
                  |"twoOrMoreProperties":"No"},
                  |"firstTimeBuyer":"No",
                  |"isLinked": false,
                  |"taxReliefDetails": { "taxReliefCode": 8,
                  |"isPartialRelief": false }
                  |}""".stripMargin
              )
            )

          val responseJson = Json.parse(
            """
              |{"result":[{"totalTax":0,
              |"resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |"taxCalcs":[{"taxType":"premium",
              |"calcType":"slab",
              |"taxDue":0,
              |"rate":0}]}]}
              |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }
      }

      "with tax relief code: PartExchange(8) / SelfAssessed" when {

        "residential" in {

          def request: WSResponse = ws.url(
              calculateUrl)
            .post(
              Json.parse(
                """
                  |{"holdingType": "Freehold",
                  |"propertyType": "Residential",
                  |"effectiveDateDay":22,
                  |"effectiveDateMonth":2,
                  |"effectiveDateYear":2013,
                  |"premium": "10000",
                  |"highestRent": 0,
                  |"taxReliefDetails":{
                  |"taxReliefCode": 8
                  |},
                  |"isLinked": true
                  |}""".stripMargin
              )
            )

          val responseJson = Json.parse(
            """
              |{
              | "result":[ {
              |   "totalTax": 0,
              |   "resultHeading": "Self-assessed",
              |   "taxCalcs": []
              |  }
              | ]
              |}""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

      }

      "with TaxReliefCode: PreCompletionTransaction(34)" when {

        "Property Type is Residential and the date is on or after 6th April 2013" in {
          def request: WSResponse = ws.url(
              calculateUrl)
            .post(
              Json.parse(
                """
                  |{"holdingType":"Freehold",
                  |"propertyType":"Residential",
                  |"effectiveDateDay":6,
                  |"effectiveDateMonth":4,
                  |"effectiveDateYear":2013,
                  |"highestRent":0,
                  |"premium":"750000",
                  |"isLinked": false,
                  |"taxReliefDetails": { "taxReliefCode": 34,
                  |"isPartialRelief": false }
                  |}""".stripMargin
              )
            )

          val responseJson = Json.parse(
            """
              |{"result":[{"totalTax":0,
              |"resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |"taxCalcs":[{"taxType":"premium",
              |"calcType":"slab",
              |"taxDue":0,
              |"rate":0}]}]}
              |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "Property Type is Non-residential and the date is on or after 6th April 2013" in {
          def request: WSResponse = ws.url(
              calculateUrl)
            .post(
              Json.parse(
                """
                  |{"holdingType":"Freehold",
                  |"propertyType":"Non-residential",
                  |"effectiveDateDay":6,
                  |"effectiveDateMonth":4,
                  |"effectiveDateYear":2013,
                  |"highestRent":0,
                  |"premium":"750000",
                  |"isLinked": false,
                  |"taxReliefDetails": { "taxReliefCode": 34,
                  |"isPartialRelief": false }
                  |}""".stripMargin
              )
            )

          val responseJson = Json.parse(
            """
              |{"result":[{"totalTax":0,
              |"resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |"taxCalcs":[{"taxType":"premium",
              |"calcType":"slab",
              |"taxDue":0,
              |"rate":0}]}]}
              |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "Property Type is Mixed and the date is on or after 6th April 2013" in {
          def request: WSResponse = ws.url(
              calculateUrl)
            .post(
              Json.parse(
                """
                  |{"holdingType":"Freehold",
                  |"propertyType":"Mixed",
                  |"effectiveDateDay":6,
                  |"effectiveDateMonth":4,
                  |"effectiveDateYear":2013,
                  |"highestRent":0,
                  |"premium":"750000",
                  |"isLinked": false,
                  |"taxReliefDetails": { "taxReliefCode": 34,
                  |"isPartialRelief": false }
                  |}""".stripMargin
              )
            )

          val responseJson = Json.parse(
            """
              |{"result":[{"totalTax":0,
              |"resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |"taxCalcs":[{"taxType":"premium",
              |"calcType":"slab",
              |"taxDue":0,
              |"rate":0}]}]}
              |""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "Property Type is Residential with an additional property and the date is on or after 1st April 2016" in {
          def request: WSResponse = ws.url(
              calculateUrl)
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
                  |    "taxReliefCode": 34,
                  |    "isPartialRelief": false
                  |  }
                  |}""".stripMargin
              )
            )

          val responseJson = Json.parse(
            """
              |{
              |  "result": [
              |    {
              |      "totalTax": 0,
              |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
              |      "taxCalcs": [
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
          request.json shouldBe responseJson
        }
      }

      "with tax relief code: RightToBuy(22)" when {
        "Property Type is Residential with an additional property and replacing main residence and the date is on or after 1st April 2016" in {
          def request: WSResponse = ws.url(
              calculateUrl)
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

      "with tax relief code: FreeportsTaxSiteRelief(36)" in {
        def request: WSResponse = ws.url(
            calculateUrl)
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

        val responseJson = Json.parse(
          """
            |{
            |  "result": [
            |    {
            |      "totalTax": 0,
            |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
            |      "taxCalcs": [
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
        request.json shouldBe responseJson
      }

      "with tax relief code: InvestmentZonesTaxSiteRelief(37) " in {
        def request: WSResponse = ws.url(
            calculateUrl)
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

        val responseJson = Json.parse(
          """
            |{
            |  "result": [
            |    {
            |      "totalTax": 0,
            |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
            |      "taxCalcs": [
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
        request.json shouldBe responseJson
      }

      "with tax relief code: AcquisitionRelief(14) for self assessed" in {
        def request: WSResponse = ws.url(
            calculateUrl)
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
                | "isLinked": true
                |}
                |""".stripMargin
            )
          )

        request.status shouldBe OK
        request.json shouldBe selfAssessedResponse
      }
    }

    "return Fallback result" when {
      "tax relief code: AcquisitionRelief(14)" when {
        "holding type is leasehold" in {

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
                  |  "taxReliefDetails": {
                  |    "taxReliefCode": 14
                  |  },
                  |  "isLinked": true
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

          request.json should not be selfAssessedResponse
          request.status shouldBe OK
          request.json shouldBe fallBackResult
        }
        "isLinked is false" in {
          def request: WSResponse = ws.url(
              calculateUrl)
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

          val fallBackResult = Json.parse(
            s"""{
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

          request.json should not be selfAssessedResponse
          request.status shouldBe OK
          request.json shouldBe fallBackResult
        }
        "effective date is after 04/12/2014" in {
          def request: WSResponse = ws.url(
              calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  | "holdingType": "Freehold",
                  | "propertyType": "Residential",
                  | "effectiveDateDay": 1,
                  | "effectiveDateMonth": 4,
                  | "effectiveDateYear": 2015,
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

          val fallBackResult = Json.parse(
            s"""{
               |  "result": [
               |    {
               |      "totalTax": 43750,
               |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
               |      "taxCalcs": [
               |        {
               |          "taxType": "premium",
               |          "calcType": "slice",
               |          "taxDue": 43750,
               |          "detailHeading": "This is a breakdown of how the total amount of SDLT was calculated",
               |          "bandHeading": "Purchase price bands (£)",
               |          "detailFooter": "Total SDLT due",
               |          "slices": [
               |            {
               |              "from": 0,
               |              "to": 125000,
               |              "rate": 0,
               |              "taxDue": 0
               |            },
               |            {
               |              "from": 125000,
               |              "to": 250000,
               |              "rate": 2,
               |              "taxDue": 2500
               |            },
               |            {
               |              "from": 250000,
               |              "to": 925000,
               |              "rate": 5,
               |              "taxDue": 33750
               |            },
               |            {
               |              "from": 925000,
               |              "to": 1500000,
               |              "rate": 10,
               |              "taxDue": 7500
               |            },
               |            {
               |              "from": 1500000,
               |              "to": -1,
               |              "rate": 12,
               |              "taxDue": 0
               |            }
               |          ]
               |        }
               |      ]
               |    }
               |  ]
               |}
               |""".stripMargin
          )

          request.json should not be selfAssessedResponse
          request.status shouldBe OK
          request.json shouldBe fallBackResult
        }
      }
    }
  }
}
