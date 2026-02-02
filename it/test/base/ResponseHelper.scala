/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package base

import play.api.libs.json.{JsValue, Json}

trait ResponseHelper {

  lazy val selfAssessedResponse: JsValue = Json.parse(
    """{
      |  "result": [
      |    {
      |      "totalTax": 0,
      |      "resultHeading": "Self-assessed",
      |      "taxCalcs": []
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  lazy val freeholdZeroRateResponse: JsValue = Json.parse(
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

  lazy val leaseholdZeroRateResponse: JsValue = Json.parse(
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
}
