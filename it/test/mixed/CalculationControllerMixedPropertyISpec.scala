/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package mixed

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import test.base.BaseSpec

class CalculationControllerMixedPropertyISpec extends BaseSpec with GuiceOneServerPerSuite {

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

    "return a 200 and valid result" when {
      //SDLT - Tax Calc Case 42 - Self Assessed
      "return the self assessed response" when {
        "Holding type is Leasehold" in {
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  | "holdingType": "Leasehold",
                  | "propertyType": "Mixed",
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
        "Holding type is Freehold" in{
          val request: WSResponse = ws
            .url(calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  | "holdingType": "Freehold",
                  | "propertyType": "Mixed",
                  | "effectiveDateDay": 24,
                  | "effectiveDateMonth": 3,
                  | "effectiveDateYear": 2012,
                  | "premium": 1000000,
                  | "highestRent": 0,
                  | "isLinked": true,
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
      }

    }
  }
}