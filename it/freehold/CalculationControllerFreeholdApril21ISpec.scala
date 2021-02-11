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

class CalculationControllerFreeholdApril21ISpec extends UnitSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {

    "return a 200 and valid result for freehold property type" when {

      "the effective date is APR 1 2021 [NRB500 Freehold Business Threads]" when {

        "the property is non-residential" when {
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
                    |  "effectiveDateMonth": 4,
                    |  "effectiveDateYear": 2021,
                    |  "premium": 300000,
                    |  "highestRent": 0,
                    |  "propertyDetails": {
                    |   "individual": "Yes",
                    |   "twoOrMoreProperties": "No",
                    |   "replaceMainResidence": "No"
                    | }
                    |}""".stripMargin)
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
        }

        "the property is residential" when {
          "the purchasers are all UK resident" when {
            "company 1M Premium (NRB500 Freehold Business Thread 5)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Freehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
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
                  |   "totalTax":58750,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "taxCalcs":[
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":58750,
                  |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                  |     "bandHeading":"Purchase price bands (£)",
                  |     "detailFooter":"Total SDLT due",
                  |     "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":3,
                  |       "taxDue":15000
                  |       },{
                  |       "from":500000,
                  |       "to":925000,
                  |       "rate":8,
                  |       "taxDue":34000
                  |       },{
                  |       "from":925000,
                  |       "to":1500000,
                  |       "rate":13,
                  |       "taxDue":9750
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

            "individual 501K Premium (NRB500 Freehold Business Thread 1)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Freehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 501000,
                      |  "highestRent": 0,
                      |  "propertyDetails": {
                      |   "individual": "Yes",
                      |   "twoOrMoreProperties": "No"
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
                  |   "totalTax":50,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "taxCalcs":[
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":50,
                  |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                  |     "bandHeading":"Purchase price bands (£)",
                  |     "detailFooter":"Total SDLT due",
                  |     "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":500000,
                  |       "to":925000,
                  |       "rate":5,
                  |       "taxDue":50
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "individual 450K Premium + HRAD (NRB500 Freehold Business Thread 7)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Freehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
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
                  |   "totalTax":13500,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £13,500.",
                  |   "taxCalcs":[
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":13500,
                  |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                  |     "bandHeading":"Purchase price bands (£)",
                  |     "detailFooter":"Total SDLT due",
                  |     "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":3,
                  |       "taxDue":13500
                  |       },{
                  |       "from":500000,
                  |       "to":925000,
                  |       "rate":8,
                  |       "taxDue":0
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
                  |  },
                  | {
                  |  "totalTax":0,
                  |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.",
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
                  |      "to":500000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
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
          }

          "one or more purchasers are non-UK resident" when {

            "the purchaser is a company" when {
              "1M premium (NRB500 Freehold Business Thread 4)" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 1000000,
                        |  "highestRent": 0,
                        |  "propertyDetails": {
                        |   "individual": "No",
                        |   "twoOrMoreProperties": "No",
                        |   "replaceMainResidence": "Yes"
                        | }
                        |}""".stripMargin)
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    | "result":[
                    |  {
                    |   "totalTax":78750,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":78750,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":500000,
                    |       "rate":5,
                    |       "taxDue":25000
                    |       },{
                    |       "from":500000,
                    |       "to":925000,
                    |       "rate":10,
                    |       "taxDue":42500
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

              "individual 500K Premium (NRB500 Freehold Business Thread 2)" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 500000,
                        |  "highestRent": 0,
                        |  "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "No"
                        | }
                        |}""".stripMargin)
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    | "result":[
                    |  {
                    |   "totalTax":10000,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":10000,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":500000,
                    |       "rate":2,
                    |       "taxDue":10000
                    |       },{
                    |       "from":500000,
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
                    |  {
                    |   "totalTax":0,
                    |   "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                    |   "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
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
                    |       "to":500000,
                    |       "rate":0,
                    |       "taxDue":0
                    |       },{
                    |       "from":500000,
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

              "individual because premium < 40K (NRSDLT Exempt)" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 39999,
                        |  "highestRent": 0,
                        |  "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "No"
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
                    |       "to":500000,
                    |       "rate":0,
                    |       "taxDue":0
                    |       },{
                    |       "from":500000,
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

              "individual 1.6M Premium (NRB500 Freehold Business Thread 3)" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "Yes",
                        |  "premium": 1600000,
                        |  "highestRent": 0,
                        |  "propertyDetails": {
                        |   "individual": "Yes",
                        |   "twoOrMoreProperties": "No"
                        | }
                        |}""".stripMargin)
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    | "result":[
                    |  {
                    |   "totalTax":122750,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":122750,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":500000,
                    |       "rate":2,
                    |       "taxDue":10000
                    |       },{
                    |       "from":500000,
                    |       "to":925000,
                    |       "rate":7,
                    |       "taxDue":29750
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
                    |       }
                    |     ]
                    |    }
                    |   ]
                    |  },
                    |  {
                    |   "totalTax":90750,
                    |   "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                    |   "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":90750,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":500000,
                    |       "rate":0,
                    |       "taxDue":0
                    |       },{
                    |       "from":500000,
                    |       "to":925000,
                    |       "rate":5,
                    |       "taxDue":21250
                    |       },{
                    |       "from":925000,
                    |       "to":1500000,
                    |       "rate":10,
                    |       "taxDue":57500
                    |       },{
                    |       "from":1500000,
                    |       "to":-1,
                    |       "rate":12,
                    |       "taxDue":12000
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

              "individual 450K Premium + HRAD (NRB500 Freehold Business Thread 6)" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Freehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 4,
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
                    |   "totalTax":22500,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £13,500.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                    |   "taxCalcs":[
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":22500,
                    |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |     "bandHeading":"Purchase price bands (£)",
                    |     "detailFooter":"Total SDLT due",
                    |     "slices":[
                    |      {
                    |       "from":0,
                    |       "to":500000,
                    |       "rate":5,
                    |       "taxDue":22500
                    |       },{
                    |       "from":500000,
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
                    |  },
                    | {
                    |  "totalTax":9000,
                    |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                    |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                    |  "taxCalcs":[
                    |   {
                    |    "taxType":"premium",
                    |    "calcType":"slice",
                    |    "taxDue":9000,
                    |    "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
                    |    "bandHeading":"Purchase price bands (£)",
                    |    "detailFooter":"Total SDLT due",
                    |    "slices":[
                    |     {
                    |      "from":0,
                    |      "to":500000,
                    |      "rate":2,
                    |      "taxDue":9000
                    |      },{
                    |      "from":500000,
                    |      "to":925000,
                    |      "rate":7,
                    |      "taxDue":0
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
            }
          }
        }
      }
    }
  }
}
