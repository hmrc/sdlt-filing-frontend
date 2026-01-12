/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package freehold

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import test.base.BaseSpec

class CalculationControllerFreeholdTaxReliefISpec extends BaseSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

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
                  |"highestRent":0,"premium":"750000",
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
                  |"highestRent":0,"premium":"750000",
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
      }

      "with tax relief code: PreCompletionTransaction(34)" when {
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

          val responseJson = Json.parse(
            """
              |{
              |  "result": [
              |    {
              |      "totalTax": 0,
              |      "resultHeading": "Self-assessed",
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
    }
  }
}
