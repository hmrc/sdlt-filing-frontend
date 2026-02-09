/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package freehold

import base.ResponseHelper
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import test.base.BaseSpec


class CalculationControllerFreeholdTaxNoReliefISpec extends BaseSpec with GuiceOneServerPerSuite with ResponseHelper {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 response for a freehold holding type" when {
      // SDLT - Tax Calc Case - 21 - Self Assessed
      "transaction is linked, effective date is before 17/03/2017 and property type is mixed" in {
        val request: WSResponse = ws
          .url(calculateUrl)
          .post(
            Json.parse(
              """
                |{
                |  "holdingType": "Freehold",
                |  "propertyType": "Mixed",
                |  "effectiveDateDay": 6,
                |  "effectiveDateMonth": 3,
                |  "effectiveDateYear": 2016,
                |  "highestRent": 0,
                |  "premium": 750000,
                |  "isLinked": true
                |}
                |""".stripMargin
            )
          )

        request.status shouldBe OK
        request.json shouldBe selfAssessedResponse

      }
      "transaction is linked, effective date is before 17/03/2017 and property type is non-residential" in {
        val request: WSResponse = ws
          .url(calculateUrl)
          .post(
            Json.parse(
              """
                |{
                |  "holdingType": "Freehold",
                |  "propertyType": "Non-residential",
                |  "effectiveDateDay": 6,
                |  "effectiveDateMonth": 3,
                |  "effectiveDateYear": 2016,
                |  "highestRent": 0,
                |  "premium": 750000,
                |  "isLinked": true
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
