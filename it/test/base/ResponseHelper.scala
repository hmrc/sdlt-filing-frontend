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

  lazy val leaseholdMixedNonResidentialRightToBuyBeforeMarch16Response: JsValue = Json.parse(
    """
      |{
      |  "result": [
      |    {
      |      "totalTax": 7500,
      |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
      |      "npv": 18995,
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
      |              "to": 150000,
      |              "rate": 0,
      |              "taxDue": 0
      |            },
      |            {
      |              "from": 150000,
      |              "to": -1,
      |              "rate": 1,
      |              "taxDue": 0
      |            }
      |          ]
      |        },
      |        {
      |          "taxType": "premium",
      |          "calcType": "slab",
      |          "taxDue": 7500,
      |          "rate": 3
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin
  )
}
