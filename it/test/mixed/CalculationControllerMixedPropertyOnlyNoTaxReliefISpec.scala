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

class CalculationControllerMixedPropertyOnlyNoTaxReliefISpec extends BaseSpec with GuiceOneServerPerSuite {

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

      "date within the range 12/03/2008 to 17/03/2016 and the property is Mixed & Leasehold" in {
        def request: WSResponse = ws.url(
            calculateUrl)
          .post(
            Json.parse(
              """{
                |  "holdingType": "Leasehold",
                |  "propertyType": "Mixed",
                |  "effectiveDateDay": 16,
                |  "effectiveDateMonth": 3,
                |  "effectiveDateYear": 2015,
                |  "premium": 1000000,
                |  "highestRent": 0,
                |  "leaseDetails": {
                |    "startDateDay": 16,
                |    "startDateMonth": 3,
                |    "startDateYear": 2015,
                |    "endDateDay": 16,
                |    "endDateMonth": 3,
                |    "endDateYear": 2016,
                |    "leaseTerm": {
                |      "years": 1,
                |      "days": 1,
                |      "daysInPartialYear": 365
                |    },
                |    "year1Rent": 1001,
                |    "year2Rent": 1001
                |  }
                |}
                |""".stripMargin
            )
          )

        request.status shouldBe OK
        request.json shouldBe selfAssessedResponse
      }
    }

    "not return a Self-assessed result valid result" when {

      "property type is Residential (ie not Mixed)" in {
        def request: WSResponse = ws.url(
            calculateUrl)
          .post(
            Json.parse(
              """{
                |  "holdingType": "Leasehold",
                |  "propertyType": "Residential",
                |  "effectiveDateDay": 16,
                |  "effectiveDateMonth": 3,
                |  "effectiveDateYear": 2015,
                |  "premium": 1000000,
                |  "highestRent": 0,
                |  "leaseDetails": {
                |    "startDateDay": 16,
                |    "startDateMonth": 3,
                |    "startDateYear": 2015,
                |    "endDateDay": 16,
                |    "endDateMonth": 3,
                |    "endDateYear": 2016,
                |    "leaseTerm": {
                |      "years": 1,
                |      "days": 1,
                |      "daysInPartialYear": 365
                |    },
                |    "year1Rent": 1001,
                |    "year2Rent": 1001
                |  }
                |}
                |""".stripMargin
            )
          )

        request.json should not be selfAssessedResponse
      }

      "property type is Non-residential (ie not Mixed)" in {
        def request: WSResponse = ws.url(
            calculateUrl)
          .post(
            Json.parse(
              """{
                |  "holdingType": "Leasehold",
                |  "propertyType": "Non-residential",
                |  "effectiveDateDay": 16,
                |  "effectiveDateMonth": 3,
                |  "effectiveDateYear": 2015,
                |  "premium": 1000000,
                |  "highestRent": 0,
                |  "leaseDetails": {
                |    "startDateDay": 16,
                |    "startDateMonth": 3,
                |    "startDateYear": 2015,
                |    "endDateDay": 16,
                |    "endDateMonth": 3,
                |    "endDateYear": 2016,
                |    "leaseTerm": {
                |      "years": 1,
                |      "days": 1,
                |      "daysInPartialYear": 365
                |    },
                |    "year1Rent": 1001,
                |    "year2Rent": 1001
                |  }
                |}
                |""".stripMargin
            )
          )

        request.json should not be selfAssessedResponse
      }

      "holding type is Freehold (ie not Leasehold)" in {
        def request: WSResponse = ws.url(
            calculateUrl)
          .post(
            Json.parse(
              """{
                |  "holdingType": "Freehold",
                |  "propertyType": "Non-residential",
                |  "effectiveDateDay": 16,
                |  "effectiveDateMonth": 3,
                |  "effectiveDateYear": 2015,
                |  "premium": 1000000,
                |  "highestRent": 0
                |}
                |""".stripMargin
            )
          )

        request.json should not be selfAssessedResponse
      }

      "Effective date is outside of required range" in {
        def request: WSResponse = ws.url(
            calculateUrl)
          .post(
            Json.parse(
              """{
                |  "holdingType": "Freehold",
                |  "propertyType": "Non-residential",
                |  "effectiveDateDay": 16,
                |  "effectiveDateMonth": 3,
                |  "effectiveDateYear": 2017,
                |  "premium": 1000000,
                |  "highestRent": 0,
                |  "leaseDetails": {
                |    "startDateDay": 16,
                |    "startDateMonth": 3,
                |    "startDateYear": 2015,
                |    "endDateDay": 16,
                |    "endDateMonth": 3,
                |    "endDateYear": 2016,
                |    "leaseTerm": {
                |      "years": 1,
                |      "days": 1,
                |      "daysInPartialYear": 365
                |    },
                |    "year1Rent": 1001,
                |    "year2Rent": 1001
                |  }
                |}
                |""".stripMargin
            )
          )

        request.json should not be selfAssessedResponse
      }
    }
  }
}