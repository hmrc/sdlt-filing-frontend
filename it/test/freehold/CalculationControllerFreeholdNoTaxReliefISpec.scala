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
    }
  }
}