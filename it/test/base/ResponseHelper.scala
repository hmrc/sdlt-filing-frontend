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

  lazy val budgetReliefReasonsWithoutTaxReliefNonLeased2016Response: JsValue = Json.parse(
    """
      |{
      |  "result": [
      |    {
      |      "totalTax": 1000,
      |      "resultHeading": "Results based on SDLT rules from 17 March 2016",
      |      "taxCalcs": [
      |        {
      |          "taxType": "premium",
      |          "calcType": "slice",
      |          "taxDue": 1000,
      |          "detailHeading": "This is a breakdown of how the total amount of SDLT was calculated based on the rules from 17 March 2016",
      |          "bandHeading": "Purchase price bands (£)",
      |          "detailFooter": "Total SDLT due",
      |          "slices": [
      |            {
      |              "from": 0,
      |              "to": 150000,
      |              "rate": 0,
      |              "taxDue": 0
      |            },
      |            {
      |              "from": 150000,
      |              "to": 250000,
      |              "rate": 2,
      |              "taxDue": 1000
      |            },
      |            {
      |              "from": 250000,
      |              "to": -1,
      |              "rate": 5,
      |              "taxDue": 0
      |            }
      |          ]
      |        }
      |      ]
      |    },
      |    {
      |      "totalTax": 2000,
      |      "resultHeading": "Results based on SDLT rules before 17 March 2016",
      |      "resultHint": "You may be entitled to pay SDLT using the old rules if you exchanged contracts before 17 March 2016.",
      |      "taxCalcs": [
      |        {
      |          "taxType": "premium",
      |          "calcType": "slab",
      |          "taxDue": 2000,
      |          "rate": 1
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin
  )
}
