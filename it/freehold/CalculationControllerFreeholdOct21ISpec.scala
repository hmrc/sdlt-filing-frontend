/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package freehold

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.play.test.UnitSpec

class CalculationControllerFreeholdOct21ISpec extends UnitSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {

    "return a 200 and valid result for freehold property type" when {

      "the effective date is OCT 1 2021 (POST NRB HOLIDAY)" when {

        "non-residential, company" in {
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Freehold",
                  |  "propertyType": "Non-residential",
                  |  "effectiveDateDay": 1,
                  |  "effectiveDateMonth": 10,
                  |  "effectiveDateYear": 2021,
                  |  "premium": 300000,
                  |  "highestRent": 0,
                  |  "propertyDetails": {
                  |   "individual": "Yes",
                  |   "twoOrMoreProperties": "No",
                  |   "replaceMainResidence": "No"
                  | }
                  |}""".stripMargin
              )
            )

          val responseJson = Json.parse(
            """
              |{
              | "result":[
              |  {
              |   "totalTax":4500,
              |   "resultHeading":"Results based on SDLT rules from 17 March 2016",
              |   "taxCalcs":[
              |    {
              |     "taxType":"premium",
              |     "calcType":"slice",
              |     "taxDue":4500,
              |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated based on the rules from 17 March 2016",
              |     "bandHeading":"Purchase price bands (£)",
              |     "detailFooter":"Total SDLT due",
              |     "slices":[
              |      {
              |       "from":0,
              |       "to":150000,
              |       "rate":0,
              |       "taxDue":0
              |       },{
              |       "from":150000,
              |       "to":250000,
              |       "rate":2,
              |       "taxDue":2000
              |       },{
              |       "from":250000,
              |       "to":-1,
              |       "rate":5,
              |       "taxDue":2500
              |       }
              |      ]
              |     }
              |    ]
              |   },
              |   {
              |    "totalTax":9000,
              |    "resultHeading":"Results based on SDLT rules before 17 March 2016",
              |    "resultHint":"You may be entitled to pay SDLT using the old rules if you exchanged contracts before 17 March 2016.",
              |    "taxCalcs":[
              |     {
              |      "taxType":"premium",
              |      "calcType":"slab",
              |      "taxDue":9000,
              |      "rate":3
              |     }
              |    ]
              |   }
              | ]
              |}""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "the purchasers are all UK resident" when {
          "residential, company" in {
            def request: WSResponse = ws.url(
              calculateUrl)
              .post(
                Json.parse(
                  """
                    |{
                    |  "holdingType": "Freehold",
                    |  "propertyType": "Residential",
                    |  "effectiveDateDay": 1,
                    |  "effectiveDateMonth": 10,
                    |  "effectiveDateYear": 2021,
                    |  "nonUKResident": "No",
                    |  "premium": 500000,
                    |  "highestRent": 0,
                    |  "propertyDetails": {
                    |   "individual": "No"
                    | }
                    |}""".stripMargin)
              )

            val responseJson = Json.parse(
              """
                |{
                | "result":[
                |  {
                |   "totalTax":30000,
                |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                |   "taxCalcs":[
                |    {
                |     "taxType":"premium",
                |     "calcType":"slice",
                |     "taxDue":30000,
                |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                |     "bandHeading":"Purchase price bands (£)",
                |     "detailFooter":"Total SDLT due",
                |     "slices":[
                |      {
                |       "from":0,
                |       "to":125000,
                |       "rate":3,
                |       "taxDue":3750
                |       },{
                |       "from":125000,
                |       "to":250000,
                |       "rate":5,
                |       "taxDue":6250
                |       },{
                |       "from":250000,
                |       "to":925000,
                |       "rate":8,
                |       "taxDue":20000
                |       },{
                |       "from":925000,
                |       "to":1500000,
                |       "rate":13,
                |       "taxDue":0
                |       },{
                |       "from":1500000,
                |       "to":-1,
                |       "rate":15,
                |       "taxDue":0
                |      }
                |     ]
                |    }
                |   ]
                |  }
                | ]
                |}""".stripMargin)

            request.status shouldBe OK
            request.json shouldBe responseJson
          }

          "residential, individual" when {
            "501K premium" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Freehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 501000,
                      |  "highestRent": 0,
                      |  "propertyDetails": {
                      |   "individual": "Yes",
                      |   "twoOrMoreProperties": "No",
                      |   "ownedOtherProperties": "Yes",
                      |   "replaceMainResidence": "Yes"
                      | }
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  | "result":[
                  |  {
                  |   "totalTax":15050,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "taxCalcs":[
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":15050,
                  |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                  |     "bandHeading":"Purchase price bands (£)",
                  |     "detailFooter":"Total SDLT due",
                  |     "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":2500
                  |       },{
                  |       "from":250000,
                  |       "to":925000,
                  |       "rate":5,
                  |       "taxDue":12550
                  |       },{
                  |       "from":925000,
                  |       "to":1500000,
                  |       "rate":10,
                  |       "taxDue":0
                  |       },{
                  |       "from":1500000,
                  |       "to":-1,
                  |       "rate":12,
                  |       "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  }
                  | ]
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "Additional Property premium < 40K" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Freehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 30000,
                      |  "highestRent": 0,
                      |  "propertyDetails": {
                      |   "individual": "Yes",
                      |   "twoOrMoreProperties": "Yes",
                      |   "replaceMainResidence": "No"
                      | }
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  | "result":[
                  |  {
                  |   "totalTax":0,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "taxCalcs":[
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                  |     "bandHeading":"Purchase price bands (£)",
                  |     "detailFooter":"Total SDLT due",
                  |     "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
                  |       "to":925000,
                  |       "rate":5,
                  |       "taxDue":0
                  |       },{
                  |       "from":925000,
                  |       "to":1500000,
                  |       "rate":10,
                  |       "taxDue":0
                  |       },{
                  |       "from":1500000,
                  |       "to":-1,
                  |       "rate":12,
                  |       "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  }
                  | ]
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

          }
        }

        "one or more purchasers are non-UK resident" when {

          "the purchaser is a company" when {
            "residential, 100K premium" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Freehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 100000,
                      |  "highestRent": 0,
                      |  "propertyDetails": {
                      |   "individual": "No"
                      | }
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  | "result":[
                  |  {
                  |   "totalTax":5000,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "taxCalcs":[
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":5000,
                  |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                  |     "bandHeading":"Purchase price bands (£)",
                  |     "detailFooter":"Total SDLT due",
                  |     "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":5,
                  |       "taxDue":5000
                  |       },{
                  |       "from":125000,
                  |       "to":250000,
                  |       "rate":7,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
                  |       "to":925000,
                  |       "rate":10,
                  |       "taxDue":0
                  |       },{
                  |       "from":925000,
                  |       "to":1500000,
                  |       "rate":15,
                  |       "taxDue":0
                  |       },{
                  |       "from":1500000,
                  |       "to":-1,
                  |       "rate":17,
                  |       "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  }
                  | ]
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 1M Premium + HRAD" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Freehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "propertyDetails": {
                      |   "individual": "No"
                      | }
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  | "result":[
                  |  {
                  |   "totalTax":93750,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "taxCalcs":[
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":93750,
                  |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                  |     "bandHeading":"Purchase price bands (£)",
                  |     "detailFooter":"Total SDLT due",
                  |     "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":5,
                  |       "taxDue":6250
                  |       },{
                  |       "from":125000,
                  |       "to":250000,
                  |       "rate":7,
                  |       "taxDue":8750
                  |       },{
                  |       "from":250000,
                  |       "to":925000,
                  |       "rate":10,
                  |       "taxDue":67500
                  |       },{
                  |       "from":925000,
                  |       "to":1500000,
                  |       "rate":15,
                  |       "taxDue":11250
                  |       },{
                  |       "from":1500000,
                  |       "to":-1,
                  |       "rate":17,
                  |       "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  }
                  | ]
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }
          }

          "the purchaser(s) is an individual" when {

            "the property is residential" when {
              "100K premium" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 10,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 100000,
                        |  "highestRent": 0,
                        |  "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "Yes",
                        |   "replaceMainResidence": "Yes"
                        | }
                        |}
              """.
                        stripMargin)
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    | "result":[
                    |  {
                    |   "totalTax":2000,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":2000,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":125000,
                    |       "rate":2,
                    |       "taxDue":2000
                    |       },{
                    |       "from":125000,
                    |       "to":250000,
                    |       "rate":4,
                    |       "taxDue":0
                    |       },{
                    |       "from":250000,
                    |       "to":925000,
                    |       "rate":7,
                    |       "taxDue":0
                    |       },{
                    |       "from":925000,
                    |       "to":1500000,
                    |       "rate":12,
                    |       "taxDue":0
                    |       },{
                    |       "from":1500000,
                    |       "to":-1,
                    |       "rate":14,
                    |       "taxDue":0
                    |      }
                    |     ]
                    |    }
                    |   ]
                    |  },
                    | {
                    |  "totalTax":0,
                    |  "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                    |  "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                    |  "taxCalcs":[
                    |   {
                    |    "taxType":"premium",
                    |    "calcType":"slice",
                    |    "taxDue":0,
                    |    "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |    "bandHeading":"Purchase price bands (£)",
                    |    "detailFooter":"Total SDLT due",
                    |    "slices":[
                    |     {
                    |      "from":0,
                    |      "to":125000,
                    |      "rate":0,
                    |      "taxDue":0
                    |      },{
                    |      "from":125000,
                    |      "to":250000,
                    |      "rate":2,
                    |      "taxDue":0
                    |      },{
                    |      "from":250000,
                    |      "to":925000,
                    |      "rate":5,
                    |      "taxDue":0
                    |      },{
                    |      "from":925000,
                    |      "to":1500000,
                    |      "rate":10,
                    |      "taxDue":0
                    |      },{
                    |      "from":1500000,
                    |      "to":-1,
                    |      "rate":12,
                    |      "taxDue":0
                    |      }
                    |     ]
                    |    }
                    |   ]
                    |  }
                    | ]
                    |}""".stripMargin)

                request.status shouldBe OK
                request.json shouldBe responseJson
              }

              "39K premium (NRSDLT out of scope)" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 10,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 39000,
                        |  "highestRent": 0,
                        |  "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "Yes",
                        |   "replaceMainResidence": "Yes"
                        | }
                        |}""".stripMargin
                    )
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    | "result":[
                    |  {
                    |   "totalTax":0,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":0,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":125000,
                    |       "rate":0,
                    |       "taxDue":0
                    |       },{
                    |       "from":125000,
                    |       "to":250000,
                    |       "rate":2,
                    |       "taxDue":0
                    |       },{
                    |       "from":250000,
                    |       "to":925000,
                    |       "rate":5,
                    |       "taxDue":0
                    |       },{
                    |       "from":925000,
                    |       "to":1500000,
                    |       "rate":10,
                    |       "taxDue":0
                    |       },{
                    |       "from":1500000,
                    |       "to":-1,
                    |       "rate":12,
                    |       "taxDue":0
                    |      }
                    |     ]
                    |    }
                    |   ]
                    |  }
                    | ]
                    |}""".stripMargin)

                request.status shouldBe OK
                request.json shouldBe responseJson
              }

              "£1.6M premium" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 10,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 1600000,
                        |  "highestRent": 0,
                        |  "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "No",
                        |   "ownedOtherProperties": "Yes"
                        | }
                        |}
              """.
                        stripMargin)
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    | "result":[
                    |  {
                    |   "totalTax":137750,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":137750,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":125000,
                    |       "rate":2,
                    |       "taxDue":2500
                    |       },{
                    |       "from":125000,
                    |       "to":250000,
                    |       "rate":4,
                    |       "taxDue":5000
                    |       },{
                    |       "from":250000,
                    |       "to":925000,
                    |       "rate":7,
                    |       "taxDue":47250
                    |       },{
                    |       "from":925000,
                    |       "to":1500000,
                    |       "rate":12,
                    |       "taxDue":69000
                    |       },{
                    |       "from":1500000,
                    |       "to":-1,
                    |       "rate":14,
                    |       "taxDue":14000
                    |      }
                    |     ]
                    |    }
                    |   ]
                    |  },
                    | {
                    |  "totalTax":105750,
                    |  "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                    |  "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                    |  "taxCalcs":[
                    |   {
                    |    "taxType":"premium",
                    |    "calcType":"slice",
                    |    "taxDue":105750,
                    |    "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |    "bandHeading":"Purchase price bands (£)",
                    |    "detailFooter":"Total SDLT due",
                    |    "slices":[
                    |     {
                    |      "from":0,
                    |      "to":125000,
                    |      "rate":0,
                    |      "taxDue":0
                    |      },{
                    |      "from":125000,
                    |      "to":250000,
                    |      "rate":2,
                    |      "taxDue":2500
                    |      },{
                    |      "from":250000,
                    |      "to":925000,
                    |      "rate":5,
                    |      "taxDue":33750
                    |      },{
                    |      "from":925000,
                    |      "to":1500000,
                    |      "rate":10,
                    |      "taxDue":57500
                    |      },{
                    |      "from":1500000,
                    |      "to":-1,
                    |      "rate":12,
                    |      "taxDue":12000
                    |      }
                    |     ]
                    |    }
                    |   ]
                    |  }
                    | ]
                    |}""".stripMargin)

                request.status shouldBe OK
                request.json shouldBe responseJson
              }

              "FTB, 250K premium" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 10,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 250000,
                        |  "highestRent": 0,
                        |  "firstTimeBuyer": "Yes",
                        |  "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "No",
                        |   "replaceMainResidence": "Yes",
                        |   "ownedOtherProperties": "No"
                        | }
                        |}
              """.
                        stripMargin)
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    | "result":[
                    |  {
                    |   "totalTax":5000,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":5000,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":300000,
                    |       "rate":2,
                    |       "taxDue":5000
                    |       },{
                    |       "from":300000,
                    |       "to":500000,
                    |       "rate":7,
                    |       "taxDue":0
                    |       }
                    |     ]
                    |    }
                    |   ]
                    |  },
                    | {
                    |  "totalTax":0,
                    |  "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                    |  "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                    |  "taxCalcs":[
                    |   {
                    |    "taxType":"premium",
                    |    "calcType":"slice",
                    |    "taxDue":0,
                    |    "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |    "bandHeading":"Purchase price bands (£)",
                    |    "detailFooter":"Total SDLT due",
                    |    "slices":[
                    |     {
                    |      "from":0,
                    |      "to":300000,
                    |      "rate":0,
                    |      "taxDue":0
                    |      },{
                    |      "from":300000,
                    |      "to":500000,
                    |      "rate":5,
                    |      "taxDue":0
                    |      }
                    |     ]
                    |    }
                    |   ]
                    |  }
                    | ]
                    |}""".stripMargin)

                request.status shouldBe OK
                request.json shouldBe responseJson
              }

              "FTB, 20K premium" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 10,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 20000,
                        |  "highestRent": 0,
                        |  "firstTimeBuyer": "Yes",
                        |  "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "No",
                        |   "replaceMainResidence": "Yes",
                        |   "ownedOtherProperties": "No"
                        | }
                        |}""".stripMargin)
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    | "result":[
                    |  {
                    |   "totalTax":0,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":0,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":300000,
                    |       "rate":0,
                    |       "taxDue":0
                    |       },{
                    |       "from":300000,
                    |       "to":500000,
                    |       "rate":5,
                    |       "taxDue":0
                    |       }
                    |     ]
                    |    }
                    |   ]
                    |  }
                    | ]
                    |}""".stripMargin)

                request.status shouldBe OK
                request.json shouldBe responseJson
              }

              "250K premium" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 10,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 250000,
                        |  "highestRent": 0,
                        |  "firstTimeBuyer": "No",
                        |  "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "No",
                        |   "replaceMainResidence": "Yes",
                        |   "ownedOtherProperties": "No"
                        | }
                        |}""".stripMargin)
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    | "result":[
                    |  {
                    |   "totalTax":7500,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":7500,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":125000,
                    |       "rate":2,
                    |       "taxDue":2500
                    |       },{
                    |       "from":125000,
                    |       "to":250000,
                    |       "rate":4,
                    |       "taxDue":5000
                    |       },{
                    |       "from":250000,
                    |       "to":925000,
                    |       "rate":7,
                    |       "taxDue":0
                    |       },{
                    |       "from":925000,
                    |       "to":1500000,
                    |       "rate":12,
                    |       "taxDue":0
                    |       },{
                    |       "from":1500000,
                    |       "to":-1,
                    |       "rate":14,
                    |       "taxDue":0
                    |       }
                    |     ]
                    |    }
                    |   ]
                    |  },
                    | {
                    |  "totalTax":2500,
                    |  "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                    |  "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                    |  "taxCalcs":[
                    |   {
                    |    "taxType":"premium",
                    |    "calcType":"slice",
                    |    "taxDue":2500,
                    |    "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |    "bandHeading":"Purchase price bands (£)",
                    |    "detailFooter":"Total SDLT due",
                    |    "slices":[
                    |      {
                    |       "from":0,
                    |       "to":125000,
                    |       "rate":0,
                    |       "taxDue":0
                    |       },{
                    |       "from":125000,
                    |       "to":250000,
                    |       "rate":2,
                    |       "taxDue":2500
                    |       },{
                    |       "from":250000,
                    |       "to":925000,
                    |       "rate":5,
                    |       "taxDue":0
                    |       },{
                    |       "from":925000,
                    |       "to":1500000,
                    |       "rate":10,
                    |       "taxDue":0
                    |       },{
                    |       "from":1500000,
                    |       "to":-1,
                    |       "rate":12,
                    |       "taxDue":0
                    |       }
                    |     ]
                    |    }
                    |   ]
                    |  }
                    | ]
                    |}""".stripMargin)

                request.status shouldBe OK
                request.json shouldBe responseJson
              }

              "Additional Property" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 10,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 450000,
                        |  "highestRent": 0,
                        |  "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "Yes",
                        |   "replaceMainResidence": "No"
                        | }
                        |}""".stripMargin)
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    | "result":[
                    |  {
                    |   "totalTax":35000,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £13,500.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":35000,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":125000,
                    |       "rate":5,
                    |       "taxDue":6250
                    |       },{
                    |       "from":125000,
                    |       "to":250000,
                    |       "rate":7,
                    |       "taxDue":8750
                    |       },{
                    |       "from":250000,
                    |       "to":925000,
                    |       "rate":10,
                    |       "taxDue":20000
                    |       },{
                    |       "from":925000,
                    |       "to":1500000,
                    |       "rate":15,
                    |       "taxDue":0
                    |       },{
                    |       "from":1500000,
                    |       "to":-1,
                    |       "rate":17,
                    |       "taxDue":0
                    |      }
                    |     ]
                    |    }
                    |   ]
                    |  },
                    | {
                    |  "totalTax":21500,
                    |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                    |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                    |  "taxCalcs":[
                    |   {
                    |    "taxType":"premium",
                    |    "calcType":"slice",
                    |    "taxDue":21500,
                    |    "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |    "bandHeading":"Purchase price bands (£)",
                    |    "detailFooter":"Total SDLT due",
                    |    "slices":[
                    |     {
                    |      "from":0,
                    |      "to":125000,
                    |      "rate":2,
                    |      "taxDue":2500
                    |      },{
                    |      "from":125000,
                    |      "to":250000,
                    |      "rate":4,
                    |      "taxDue":5000
                    |      },{
                    |      "from":250000,
                    |      "to":925000,
                    |      "rate":7,
                    |      "taxDue":14000
                    |      },{
                    |      "from":925000,
                    |      "to":1500000,
                    |      "rate":12,
                    |      "taxDue":0
                    |      },{
                    |      "from":1500000,
                    |      "to":-1,
                    |      "rate":14,
                    |      "taxDue":0
                    |      }
                    |     ]
                    |    }
                    |   ]
                    |  }
                    | ]
                    |}""".stripMargin)

                request.status shouldBe OK
                request.json shouldBe responseJson
              }

              "Additional Property Premium < 40K and NRSDLT out of scope" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 10,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 30000,
                        |  "highestRent": 0,
                        |  "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "Yes",
                        |   "replaceMainResidence": "No"
                        | }
                        |}""".stripMargin)
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    | "result":[
                    |  {
                    |   "totalTax":0,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":0,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":125000,
                    |       "rate":0,
                    |       "taxDue":0
                    |       },{
                    |       "from":125000,
                    |       "to":250000,
                    |       "rate":2,
                    |       "taxDue":0
                    |       },{
                    |       "from":250000,
                    |       "to":925000,
                    |       "rate":5,
                    |       "taxDue":0
                    |       },{
                    |       "from":925000,
                    |       "to":1500000,
                    |       "rate":10,
                    |       "taxDue":0
                    |       },{
                    |       "from":1500000,
                    |       "to":-1,
                    |       "rate":12,
                    |       "taxDue":0
                    |      }
                    |     ]
                    |    }
                    |   ]
                    |  }
                    | ]
                    |}""".stripMargin)

                request.status shouldBe OK
                request.json shouldBe responseJson
              }
            }
          }
        }
      }
    }
  }
}
