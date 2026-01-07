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

class CalculationControllerFreeholdAcquisitionTaxReliefISpec extends BaseSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {

    "return a 200 and valid result for freehold property type" when {

      "with tax relief code: AcquisitionRelief(14)" in {

        def request: WSResponse = ws.url(
            calculateUrl)
          .post(
            Json.parse(
              """
                |{"holdingType": "Freehold",
                | "propertyType": "Residential",
                | "effectiveDateDay": 6,
                | "effectiveDateMonth": 4,
                | "effectiveDateYear": 2013,
                | "premium": 1000000,
                | "highestRent": 0,
                | "isLinked": false,
                | "taxReliefDetails": {
                |   "taxReliefCode": 14
                |   }
                |}""".stripMargin
            )
          )

        val responseJson = Json.parse(
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
            |""".stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson


      }
    }
  }
}
