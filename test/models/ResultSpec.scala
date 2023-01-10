/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models

import enums.{CalcTypes, TaxTypes}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json

class ResultSpec extends PlaySpec {

  "SliceDetails" must {
    "write to Json successfully" when {
      "'to' field is defined" in {
        val testJson = Json.parse(
          """
            |{
            |  "from":125000,
            |  "to":250000,
            |  "rate":5,
            |  "taxDue":300
            |}
          """.stripMargin)

        val model = SliceDetails(
          from = 125000,
          to = Some(250000),
          rate = 5,
          taxDue = 300
        )

        Json.toJson(model) shouldBe testJson
      }

      "'to' field is not defined" in {
        val testJson = Json.parse(
          """
            |{
            |  "from":500000,
            |  "to":-1,
            |  "rate":10,
            |  "taxDue":3000
            |}
          """.stripMargin)

        val model = SliceDetails(
          from = 500000,
          to = None,
          rate = 10,
          taxDue = 3000
        )

        Json.toJson(model) shouldBe testJson
      }
    }
  }

  "CalculationDetails" must {
    "write to Json successfully" when {
      "result helper texts are not defined" in {
        val testJson = Json.parse(
          """
            |{
            |  "taxType":"premium",
            |  "calcType":"slice",
            |  "taxDue":3750,
            |  "slices":[
            |    {
            |      "from":0,
            |      "to":125000,
            |      "rate":3,
            |      "taxDue":3750
            |    }
            |  ]
            |}
          """.stripMargin)

        val model = CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          taxDue = 3750,
          slices = Some(Seq(
            SliceDetails(
              from = 0,
              to = Some(125000),
              rate = 3,
              taxDue = 3750
            )
          ))
        )

        Json.toJson(model) shouldBe testJson
      }
    }
  }

  "CalculationResponse" must {
    "write to Json successfully" when {

      "there is a single slab result" in {
        val testJson = Json.parse("""{
                                |  "result":[
                                |    {
                                |      "totalTax":32000,
                                |      "taxCalcs":[
                                |        {
                                |          "taxType":"premium",
                                |          "calcType":"slab",
                                |          "taxDue":32000,
                                |          "rate":4
                                |        }
                                |      ]
                                |    }
                                |  ]
                                |}""".stripMargin)

        val model =  CalculationResponse(
          result = Seq(
            Result(
              totalTax = 32000,
              npv = None,
              taxCalcs = Seq(
                CalculationDetails(
                  taxType = TaxTypes.premium,
                  calcType = CalcTypes.slab,
                  taxDue = 32000,
                  rate = Some(4)
                )
              )
            )
          )
        )

        Json.toJson(model) shouldBe testJson
      }

      "there are two premium slice results" in {
        val testJson = Json.parse(
          """
            |{
            |  "result":[
            |    {
            |      "resultHeading":"Results based on SDLT rules from 1 April 2016",
            |      "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £15,000.",
            |      "totalTax":30000,
            |      "taxCalcs":[
            |        {
            |          "taxType":"premium",
            |          "calcType":"slice",
            |          "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated based on the rules from 1 April 2016",
            |          "bandHeading":"Purchase price bands (£)",
            |          "detailFooter":"Total SDLT due",
            |          "taxDue":30000,
            |          "slices":[
            |            {
            |              "from":0,
            |              "to":125000,
            |              "rate":3,
            |              "taxDue":3750
            |            },
            |            {
            |              "from":125000,
            |              "to":250000,
            |              "rate":5,
            |              "taxDue":6250
            |            },
            |            {
            |              "from":250000,
            |              "to":925000,
            |              "rate":8,
            |              "taxDue":20000
            |            },
            |            {
            |              "from":925000,
            |              "to":1500000,
            |              "rate":13,
            |              "taxDue":0
            |            },
            |            {
            |              "from":1500000,
            |              "to":-1,
            |              "rate":15,
            |              "taxDue":0
            |            }
            |          ]
            |        }
            |      ]
            |    },
            |    {
            |      "totalTax":15000,
            |      "taxCalcs":[
            |        {
            |          "taxType":"premium",
            |          "calcType":"slice",
            |          "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated based on the rules before 1 April 2016",
            |          "bandHeading":"Purchase price bands (£)",
            |          "detailFooter":"Total SDLT due",
            |          "taxDue":15000,
            |          "slices":[
            |            {
            |              "from":0,
            |              "to":125000,
            |              "rate":0,
            |              "taxDue":0
            |            },
            |            {
            |              "from":125000,
            |              "to":250000,
            |              "rate":2,
            |              "taxDue":2500
            |            },
            |            {
            |              "from":250000,
            |              "to":925000,
            |              "rate":5,
            |              "taxDue":12500
            |            },
            |            {
            |              "from":925000,
            |              "to":1500000,
            |              "rate":10,
            |              "taxDue":0
            |            },
            |            {
            |              "from":1500000,
            |              "to":-1,
            |              "rate":12,
            |              "taxDue":0
            |            }
            |          ]
            |        }
            |      ],
            |      "resultHeading":"Results based on SDLT rules before 1 April 2016",
            |      "resultHint":"You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015."
            |    }
            |  ]
            |}
          """.stripMargin)

        val model = CalculationResponse(
          result = Seq(
            Result(
              totalTax = 30000,
              resultHeading = Some("Results based on SDLT rules from 1 April 2016"),
              resultHint = Some("If you dispose of your previous main residence within 3 years you may be eligible for a refund of £15,000."),
              npv = None,
              taxCalcs = Seq(
                CalculationDetails(
                  taxType = TaxTypes.premium,
                  calcType = CalcTypes.slice,
                  detailHeading = Some("This is a breakdown of how the total amount of SDLT was calculated based on the rules from 1 April 2016"),
                  bandHeading = Some("Purchase price bands (£)"),
                  detailFooter = Some("Total SDLT due"),
                  taxDue = 30000,
                  slices = Some(Seq(
                    SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
                    SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 6250),
                    SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 20000),
                    SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
                    SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
                  ))
                )
              )
            ),
            Result(
              totalTax = 15000,
              resultHeading = Some("Results based on SDLT rules before 1 April 2016"),
              resultHint = Some("You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015."),
              npv = None,
              taxCalcs = Seq(
                CalculationDetails(
                  taxType = TaxTypes.premium,
                  calcType = CalcTypes.slice,
                  detailHeading = Some("This is a breakdown of how the total amount of SDLT was calculated based on the rules before 1 April 2016"),
                  bandHeading = Some("Purchase price bands (£)"),
                  detailFooter = Some("Total SDLT due"),
                  taxDue = 15000,
                  slices = Some(Seq(
                    SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
                    SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
                    SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 12500),
                    SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
                    SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
                  ))
                )
              )
            )
          )
        )

        Json.toJson(model) shouldBe testJson
      }

      "there is a result with both premium and rent calculations" in {
        val testJson = Json.parse(
          """
            |{
            |  "result":[
            |    {
            |      "resultHeading":"Results based on SDLT rules from 1 April 2016",
            |      "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £15,000.",
            |      "totalTax":30689,
            |      "npv":193902,
            |      "taxCalcs":[
            |        {
            |          "taxType":"rent",
            |          "calcType":"slice",
            |          "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 1 April 2016",
            |          "bandHeading":"Rent bands (£)",
            |          "detailFooter":"SDLT due on the rent",
            |          "taxDue":689,
            |          "slices":[
            |            {
            |              "from":0,
            |              "to":125000,
            |              "rate":0,
            |              "taxDue":0
            |            },
            |            {
            |              "from":125000,
            |              "to":-1,
            |              "rate":1,
            |              "taxDue":689
            |            }
            |          ]
            |        },
            |        {
            |          "taxType":"premium",
            |          "calcType":"slice",
            |          "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 1 April 2016",
            |          "bandHeading":"Premium bands (£)",
            |          "detailFooter":"SDLT due on the premium",
            |          "taxDue":30000,
            |          "slices":[
            |            {
            |              "from":0,
            |              "to":125000,
            |              "rate":3,
            |              "taxDue":3750
            |            },
            |            {
            |              "from":125000,
            |              "to":250000,
            |              "rate":5,
            |              "taxDue":6250
            |            },
            |            {
            |              "from":250000,
            |              "to":925000,
            |              "rate":8,
            |              "taxDue":20000
            |            },
            |            {
            |              "from":925000,
            |              "to":1500000,
            |              "rate":13,
            |              "taxDue":0
            |            },
            |            {
            |              "from":1500000,
            |              "to":-1,
            |              "rate":15,
            |              "taxDue":0
            |            }
            |          ]
            |        }
            |      ]
            |    }
            |  ]
            |}
          """.stripMargin)

        val model = CalculationResponse(
          result = Seq(
            Result(
              totalTax = 30689,
              resultHeading = Some("Results based on SDLT rules from 1 April 2016"),
              resultHint = Some("If you dispose of your previous main residence within 3 years you may be eligible for a refund of £15,000."),
              npv = Some(193902),
              taxCalcs = Seq(
                CalculationDetails(
                  taxType = TaxTypes.rent,
                  calcType = CalcTypes.slice,
                  detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 1 April 2016"),
                  bandHeading = Some("Rent bands (£)"),
                  detailFooter = Some("SDLT due on the rent"),
                  taxDue = 689,
                  slices = Some(Seq(
                    SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
                    SliceDetails(from = 125000, to = None, rate = 1, taxDue = 689)
                  ))
                ),
                CalculationDetails(
                  taxType = TaxTypes.premium,
                  calcType = CalcTypes.slice,
                  detailHeading = Some("This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 1 April 2016"),
                  bandHeading = Some("Premium bands (£)"),
                  detailFooter = Some("SDLT due on the premium"),
                  taxDue = 30000,
                  slices = Some(Seq(
                    SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
                    SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 6250),
                    SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 20000),
                    SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
                    SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
                  ))
                )
              )
            )
          )
        )

        Json.toJson(model) shouldBe testJson
      }
    }
  }
}
