/*
 * Copyright 2026 HM Revenue & Customs
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

class CalculationControllerLeaseholdTaxReliefOthersISpec extends BaseSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 and valid result for leasehold property type" when {
      "tax relief code provided" in {
        def request: WSResponse = ws.url(
            calculateUrl)
          .post(
            Json.parse(
              """
                |{ "holdingType": "Leasehold",
                | "propertyType": "Non-residential",
                | "effectiveDateDay": 23,
                | "effectiveDateMonth": 3,
                | "effectiveDateYear": 2012,
                | "premium": 1000000,
                |  "highestRent": 0,
                |   "leaseDetails": {
                |     "startDateDay": 23,
                |     "startDateMonth": 3,
                |     "startDateYear": 2012,
                |     "endDateDay": 23,
                |     "endDateMonth": 3,
                |     "endDateYear": 2013,
                |     "leaseTerm": {
                |       "years": 1,
                |       "days": 1,
                |       "daysInPartialYear": 365
                |     },
                |   "year1Rent": 999,
                |   "year2Rent": 999
                |   },
                |   "taxReliefDetails": {
                |   "taxReliefCode": 38
                | } ,
                | "isLinked": false
                |} """.stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{ "result": [
            | {   "totalTax": 0,
            |     "npv" : 1897,
            |     "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
            |     "taxCalcs":
            |       [{
            |         "taxType": "premium",
            |         "calcType": "slab",
            |         "taxDue": 0,
            |         "rate": 0    },
            |        {     "taxType": "rent",
            |              "calcType": "slab",
            |              "taxDue": 0,
            |              "rate": 0    }
            |      ]
            |   }
            | ]
            |} """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }
    }
  }
}