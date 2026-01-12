/*
 * Copyright 2025 HM Revenue & Customs
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

class CalculationControllerFreeholdFreePortReliefISpec extends BaseSpec with GuiceOneServerPerSuite{

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route " should {
    "return a 200 and valid result for freehold property type " when {
      "the TaxRelief Code is FreeportsTaxSiteRelief " in {
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
      "the TaxRelief Code is InvestmentZonesTaxSiteRelief " in {
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
      "isPartialRelief is true and self assessment needed for Freeport Relief" in {
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
                |   "isPartialRelief": true
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
  }
}
