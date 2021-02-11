/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package leasehold

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.play.test.UnitSpec

class CalculationControllerLeaseholdApril21ISpec extends UnitSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 and valid result for leasehold property type" when {

      "the effective date is APR 1 2021 [NRB500 Leasehold Business Threads]" when {

        "non-residential, company, Additional Property" in{
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Non-residential",
                  |  "effectiveDateDay": 1,
                  |  "effectiveDateMonth": 4,
                  |  "effectiveDateYear": 2021,
                  |  "premium": 500000,
                  |  "highestRent": 99000,
                  |  "propertyDetails": {
                  |     "individual": "No",
                  |     "twoOrMoreProperties": "No"
                  |   },
                  |  "leaseDetails": {
                  |    "startDateDay": 1,
                  |    "startDateMonth": 4,
                  |    "startDateYear": 2021,
                  |    "endDateDay": 31,
                  |    "endDateMonth": 3,
                  |    "endDateYear": 2121,
                  |    "leaseTerm":  {
                  |      "years": 100,
                  |      "days": 0,
                  |      "daysInPartialYear": 0
                  |     },
                  |    "year1Rent": 99000,
                  |    "year2Rent": 99000,
                  |    "year3Rent": 99000,
                  |    "year4Rent": 99000,
                  |    "year5Rent": 99000
                  |  }
                  |}
              """. stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              |"result":[
              |  {
              |   "totalTax":40378,
              |   "resultHeading":"Results based on SDLT rules from 17 March 2016",
              |   "npv":2737887,
              |   "taxCalcs":[
              |     {
              |      "taxType":"rent",
              |      "calcType":"slice",
              |      "taxDue":25878,
              |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 17 March 2016",
              |      "bandHeading":"Rent bands (£)",
              |      "detailFooter":"SDLT due on the rent",
              |      "slices":[
              |      {
              |       "from":0,
              |       "to":150000,
              |       "rate":0,
              |       "taxDue":0
              |       },{
              |       "from":150000,
              |       "to":5000000,
              |       "rate":1,
              |       "taxDue":25878
              |       },{
              |       "from":5000000,
              |       "to":-1,
              |       "rate":2,
              |       "taxDue":0
              |       }
              |      ]
              |     },
              |    {
              |     "taxType":"premium",
              |     "calcType":"slice",
              |     "taxDue":14500,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 17 March 2016",
              |     "bandHeading":"Premium bands (£)",
              |     "detailFooter":"SDLT due on the premium",
              |     "slices":[
              |     {
              |      "from":0,
              |      "to":150000,
              |      "rate":0,
              |      "taxDue":0
              |      },{
              |      "from":150000,
              |      "to":250000,
              |      "rate":2,
              |      "taxDue":2000
              |      },{
              |      "from":250000,
              |      "to":-1,
              |      "rate":5,
              |      "taxDue":12500
              |      }
              |     ]
              |    }
              |   ]
              |  },{
              |   "totalTax":40878,
              |   "resultHeading":"Results based on SDLT rules before 17 March 2016",
              |   "resultHint":"You may be entitled to pay SDLT using the old rules if you exchanged contracts before 17 March 2016.",
              |   "npv":2737887,
              |   "taxCalcs":[
              |    {
              |     "taxType":"rent",
              |     "calcType":"slice",
              |     "taxDue":25878,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules before 17 March 2016",
              |     "bandHeading":"Rent bands (£)",
              |     "detailFooter":"SDLT due on the rent",
              |     "slices":[
              |      {
              |       "from":0,
              |       "to":150000,
              |       "rate":0,
              |       "taxDue":0
              |       },{
              |       "from":150000,
              |       "to":-1,
              |       "rate":1,
              |       "taxDue":25878
              |      }
              |     ]
              |     },{
              |      "taxType":"premium",
              |      "calcType":"slab",
              |      "taxDue":15000,
              |      "rate":3
              |     }
              |    ]
              |   }
              | ]
              |}
          """.stripMargin)
          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "the purchasers are all UK resident" when {
          "the purchaser is a company" when {
            "residential (NRB500 Leasehold Business Thread 5)" in{
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 1000000,
                      |  "highestRent": 9100,
                      |  "propertyDetails": {
                      |     "individual": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years":100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 9100,
                      |    "year2Rent": 9100,
                      |    "year3Rent": 9100,
                      |    "year4Rent": 9100,
                      |    "year5Rent": 9100
                      |  }
                      |}""". stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":58750,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":251664,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":0,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":58750,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":3,
                  |      "taxDue":15000
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":8,
                  |      "taxDue":34000
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":13,
                  |      "taxDue":9750
                  |      },{
                  |      "from":1500000,
                  |      "to":-1,
                  |      "rate":15,
                  |      "taxDue":0
                  |      }
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

            "residential 300K" in{
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years":100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000,
                      |    "year2Rent": 99000,
                      |    "year3Rent": 99000,
                      |    "year4Rent": 99000,
                      |    "year5Rent": 99000
                      |  }
                      |}""". stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":31378,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":22378,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":22378
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":9000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":3,
                  |      "taxDue":9000
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":8,
                  |      "taxDue":0
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":13,
                  |      "taxDue":0
                  |      },{
                  |      "from":1500000,
                  |      "to":-1,
                  |      "rate":15,
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

          "the purchaser(s) is an individual" when {
            "residential 550K Premium (NRB500 Leasehold Business Thread 1)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 550000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years":100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000,
                      |    "year2Rent": 99000,
                      |    "year3Rent": 99000,
                      |    "year4Rent": 99000,
                      |    "year5Rent": 99000
                      |  }
                      |}
              """.stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":24878,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":22378,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":22378
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":2500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":5,
                  |      "taxDue":2500
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 1M Premium + HRAD (NRB500 Leasehold Business Thread 2)" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 4,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "No",
                        |  "premium": 1000000,
                        |  "highestRent": 9100,
                        |  "propertyDetails": {
                        |     "individual": "Yes",
                        |     "twoOrMoreProperties": "Yes",
                        |     "replaceMainResidence": "No"
                        |   },
                        |  "leaseDetails": {
                        |    "startDateDay": 1,
                        |    "startDateMonth": 4,
                        |    "startDateYear": 2021,
                        |    "endDateDay": 31,
                        |    "endDateMonth": 3,
                        |    "endDateYear": 2121,
                        |    "leaseTerm":  {
                        |      "years":100,
                        |      "days": 0,
                        |      "daysInPartialYear": 0
                        |     },
                        |    "year1Rent": 9100,
                        |    "year2Rent": 9100,
                        |    "year3Rent": 9100,
                        |    "year4Rent": 9100,
                        |    "year5Rent": 9100
                        |  }
                        |}""".stripMargin
                    )
                  )

                val responseJson = Json.parse(
                  """
                    |{
                    |"result":[
                    |  {
                    |   "totalTax":58750,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £30,000.",
                    |   "npv":251664,
                    |   "taxCalcs":[
                    |     {
                    |      "taxType":"rent",
                    |      "calcType":"slice",
                    |      "taxDue":0,
                    |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                    |      "bandHeading":"Rent bands (£)",
                    |      "detailFooter":"SDLT due on the rent",
                    |      "slices":[
                    |      {
                    |       "from":0,
                    |       "to":500000,
                    |       "rate":0,
                    |       "taxDue":0
                    |       },{
                    |       "from":500000,
                    |       "to":-1,
                    |       "rate":1,
                    |       "taxDue":0
                    |       }
                    |      ]
                    |     },
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":58750,
                    |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                    |     "bandHeading":"Premium bands (£)",
                    |     "detailFooter":"SDLT due on the premium",
                    |     "slices":[
                    |     {
                    |      "from":0,
                    |      "to":500000,
                    |      "rate":3,
                    |      "taxDue":15000
                    |      },{
                    |      "from":500000,
                    |      "to":925000,
                    |      "rate":8,
                    |      "taxDue":34000
                    |      },{
                    |      "from":925000,
                    |      "to":1500000,
                    |      "rate":13,
                    |      "taxDue":9750
                    |      },{
                    |      "from":1500000,
                    |      "to":-1,
                    |      "rate":15,
                    |      "taxDue":0
                    |      }
                    |     ]
                    |    }
                    |   ]
                    |  },{
                    |  "totalTax":28750,
                    |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                    |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.",
                    |  "npv":251664,
                    |  "taxCalcs":[
                    |   {
                    |    "taxType":"rent",
                    |    "calcType":"slice",
                    |    "taxDue":0,
                    |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                    |    "bandHeading":"Rent bands (£)",
                    |    "detailFooter":"SDLT due on the rent",
                    |    "slices":[
                    |     {
                    |      "from":0,
                    |      "to":500000,
                    |      "rate":0,
                    |      "taxDue":0
                    |      },{
                    |      "from":500000,
                    |      "to":-1,
                    |      "rate":1,
                    |      "taxDue":0
                    |      }
                    |     ]
                    |     },{
                    |      "taxType":"premium",
                    |      "calcType":"slice",
                    |      "taxDue":28750,
                    |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                    |      "bandHeading":"Premium bands (£)",
                    |      "detailFooter":"SDLT due on the premium",
                    |      "slices":[
                    |       {
                    |        "from":0,
                    |        "to":500000,
                    |        "rate":0,
                    |        "taxDue":0
                    |        },{
                    |        "from":500000,
                    |        "to":925000,
                    |        "rate":5,
                    |        "taxDue":21250
                    |        },{
                    |        "from":925000,
                    |        "to":1500000,
                    |        "rate":10,
                    |        "taxDue":7500
                    |        },{
                    |        "from":1500000,
                    |        "to":-1,
                    |        "rate":12,
                    |        "taxDue":0
                    |        }
                    |       ]
                    |      }
                    |    ]
                    |   }
                    | ]
                    |}
                    |""".stripMargin
                )

                request.status shouldBe OK
                request.json shouldBe responseJson
              }

          }
        }

        "one or more purchasers are non-UK resident" when {

          "the purchaser is a company" when {

            "residential 300K Premium (NRB500 Leasehold Business Thread 6)" in{
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "No",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years":100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000,
                      |    "year2Rent": 99000,
                      |    "year3Rent": 99000,
                      |    "year4Rent": 99000,
                      |    "year5Rent": 99000
                      |  }
                      |}
              """. stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":92136,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":77136,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":2,
                  |       "taxDue":10000
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":67136
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":15000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":5,
                  |      "taxDue":15000
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":0
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":15,
                  |      "taxDue":0
                  |      },{
                  |      "from":1500000,
                  |      "to":-1,
                  |      "rate":17,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  }
                  | ]
                  |}
          """.stripMargin)
              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 2M Premium" in{
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 2000000,
                      |  "highestRent": 9100,
                      |  "propertyDetails": {
                      |     "individual": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years":100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 9100,
                      |    "year2Rent": 9100,
                      |    "year3Rent": 9100,
                      |    "year4Rent": 9100,
                      |    "year5Rent": 9100
                      |  }
                      |}""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":243783,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":251664,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":5033,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":2,
                  |       "taxDue":5033
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":238750,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":5,
                  |      "taxDue":25000
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":42500
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":15,
                  |      "taxDue":86250
                  |      },{
                  |      "from":1500000,
                  |      "to":-1,
                  |      "rate":17,
                  |      "taxDue":85000
                  |      }
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
          }

          "the purchaser(s) is an individual" when {

            "residential 1M Premium, NRSDLT (NRB500 Leasehold Business Thread 3)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 1000000,
                      |  "highestRent": 9100,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years":100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 9100,
                      |    "year2Rent": 9100,
                      |    "year3Rent": 9100,
                      |    "year4Rent": 9100,
                      |    "year5Rent": 9100
                      |  }
                      |}
              """.stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":53783,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":251664,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":5033,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":2,
                  |       "taxDue":5033
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":48750,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":2,
                  |      "taxDue":10000
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":7,
                  |      "taxDue":29750
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":12,
                  |      "taxDue":9000
                  |      },{
                  |      "from":1500000,
                  |      "to":-1,
                  |      "rate":14,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  },{
                  |    "totalTax":28750,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":251664,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":28750,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":5,
                  |      "taxDue":21250
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":10,
                  |      "taxDue":7500
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 550K Premium (NRB500 Leasehold Business Thread 9)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 550000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years":100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000,
                      |    "year2Rent": 99000,
                      |    "year3Rent": 99000,
                      |    "year4Rent": 99000,
                      |    "year5Rent": 99000
                      |  }
                      |}
              """.stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":90636,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":77136,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":2,
                  |       "taxDue":10000
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":67136
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":13500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":2,
                  |      "taxDue":10000
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":7,
                  |      "taxDue":3500
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
                  |  },{
                  |    "totalTax":24878,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":22378,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":22378
                  |      }
                  |     ]
                  |    },
                  |   {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":2500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":5,
                  |      "taxDue":2500
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
                  |}
          """.stripMargin)
              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 300K Premium" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years":100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000,
                      |    "year2Rent": 99000,
                      |    "year3Rent": 99000,
                      |    "year4Rent": 99000,
                      |    "year5Rent": 99000
                      |  }
                      |}
              """.stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":83136,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":77136,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":2,
                  |       "taxDue":10000
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":67136
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":6000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":2,
                  |      "taxDue":6000
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
                  |  },{
                  |    "totalTax":22378,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":22378,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":22378
                  |      }
                  |     ]
                  |    },
                  |   {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
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
                  |}
          """.stripMargin)
              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 30K Premium + Rent < 1K (NRSDLT out of scope)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 30000,
                      |  "highestRent": 999,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years":100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999,
                      |    "year3Rent": 999,
                      |    "year4Rent": 999,
                      |    "year5Rent": 999
                      |  }
                      |}""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":0,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":27627,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":0,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, one year rent input" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 500000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2022,
                      |    "leaseTerm":  {
                      |      "years":1,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000
                      |  }
                      |}
              """.stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":11913,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":95652,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":1913,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":2,
                  |       "taxDue":1913
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":10000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":2,
                  |      "taxDue":10000
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
                  |  },{
                  |    "totalTax":0,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":95652,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    },
                  |   {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
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
                  |}
          """.stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 275K Premium + HRAD" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 275000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 15,
                      |    "startDateMonth": 1,
                      |    "startDateYear": 1949,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years": 100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000,
                      |    "year2Rent": 99000,
                      |    "year3Rent": 99000,
                      |    "year4Rent": 99000,
                      |    "year5Rent": 99000
                      |  }
                      |}
              """.stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":90886,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £8,250.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":77136,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":2,
                  |       "taxDue":10000
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":67136
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":13750,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":5,
                  |      "taxDue":13750
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":0
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":15,
                  |      "taxDue":0
                  |      },{
                  |      "from":1500000,
                  |      "to":-1,
                  |      "rate":17,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  },{
                  |  "totalTax":82636,
                  |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |  "npv":2737887,
                  |  "taxCalcs":[
                  |   {
                  |    "taxType":"rent",
                  |    "calcType":"slice",
                  |    "taxDue":77136,
                  |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |    "bandHeading":"Rent bands (£)",
                  |    "detailFooter":"SDLT due on the rent",
                  |    "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":2,
                  |      "taxDue":10000
                  |      },{
                  |      "from":500000,
                  |      "to":-1,
                  |      "rate":3,
                  |      "taxDue":67136}
                  |      ]
                  |     },{
                  |      "taxType":"premium",
                  |      "calcType":"slice",
                  |      "taxDue":5500,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |      "bandHeading":"Premium bands (£)",
                  |      "detailFooter":"SDLT due on the premium",
                  |      "slices":[
                  |       {
                  |        "from":0,
                  |        "to":500000,
                  |        "rate":2,
                  |        "taxDue":5500
                  |        },{
                  |        "from":500000,
                  |        "to":925000,
                  |        "rate":7,
                  |        "taxDue":0
                  |        },{
                  |        "from":925000,
                  |        "to":1500000,
                  |        "rate":12,
                  |        "taxDue":0
                  |        },{
                  |        "from":1500000,
                  |        "to":-1,
                  |        "rate":14,
                  |        "taxDue":0
                  |        }
                  |       ]
                  |      }
                  |    ]
                  |   }
                  | ]
                  |}
          """.stripMargin)
              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 2M Premium + HRAD (NRB500 Leasehold Business Thread 4)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 2000000,
                      |  "highestRent": 9100,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years": 100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 9100,
                      |    "year2Rent": 9100,
                      |    "year3Rent": 9100,
                      |    "year4Rent": 9100,
                      |    "year5Rent": 9100
                      |  }
                      |}""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":243783,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £60,000.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |   "npv":251664,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":5033,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":2,
                  |       "taxDue":5033
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":238750,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":5,
                  |      "taxDue":25000
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":42500
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":15,
                  |      "taxDue":86250
                  |      },{
                  |      "from":1500000,
                  |      "to":-1,
                  |      "rate":17,
                  |      "taxDue":85000
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  },{
                  |  "totalTax":183783,
                  |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |  "npv":251664,
                  |  "taxCalcs":[
                  |   {
                  |    "taxType":"rent",
                  |    "calcType":"slice",
                  |    "taxDue":5033,
                  |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |    "bandHeading":"Rent bands (£)",
                  |    "detailFooter":"SDLT due on the rent",
                  |    "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":2,
                  |      "taxDue":5033
                  |      },{
                  |      "from":500000,
                  |      "to":-1,
                  |      "rate":3,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |     },{
                  |      "taxType":"premium",
                  |      "calcType":"slice",
                  |      "taxDue":178750,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |      "bandHeading":"Premium bands (£)",
                  |      "detailFooter":"SDLT due on the premium",
                  |      "slices":[
                  |       {
                  |        "from":0,
                  |        "to":500000,
                  |        "rate":2,
                  |        "taxDue":10000
                  |        },{
                  |        "from":500000,
                  |        "to":925000,
                  |        "rate":7,
                  |        "taxDue":29750
                  |        },{
                  |        "from":925000,
                  |        "to":1500000,
                  |        "rate":12,
                  |        "taxDue":69000
                  |        },{
                  |        "from":1500000,
                  |        "to":-1,
                  |        "rate":14,
                  |        "taxDue":70000
                  |        }
                  |       ]
                  |      }
                  |    ]
                  |   }
                  | ]
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 30K Premium + HRAD" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 30000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years": 100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000,
                      |    "year2Rent": 99000,
                      |    "year3Rent": 99000,
                      |    "year4Rent": 99000,
                      |    "year5Rent": 99000
                      |  }
                      |}
                """.stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":77136,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":77136,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":2,
                  |       "taxDue":10000
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":67136
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":5,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":0
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":15,
                  |      "taxDue":0
                  |      },{
                  |      "from":1500000,
                  |      "to":-1,
                  |      "rate":17,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  },
                  |  {
                  |   "totalTax":77136,
                  |   "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |   "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":77136,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":2,
                  |       "taxDue":10000
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":67136
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":2,
                  |      "taxDue":0
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
                  |}
            """.stripMargin)
              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 30K Premium + HRAD + less than 7y lease (NRSDLT Exempt on Rent)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 30000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2025,
                      |    "leaseTerm":  {
                      |      "years": 4,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000,
                      |    "year2Rent": 99000,
                      |    "year3Rent": 99000,
                      |    "year4Rent": 99000
                      |  }
                      |}
              """.stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":0,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":363634,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":0,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":5,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":0
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":15,
                  |      "taxDue":0
                  |      },{
                  |      "from":1500000,
                  |      "to":-1,
                  |      "rate":17,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  },
                  |  {
                  |   "totalTax":0,
                  |   "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |   "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |   "npv":363634,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":0,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":2,
                  |      "taxDue":0
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
                  |}
          """.stripMargin)
              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 30K Premium + HRAD + less than 7y lease + rent < 1000 (NRSDLT Exempt on Rent)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 30000,
                      |  "highestRent": 999,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2025,
                      |    "leaseTerm":  {
                      |      "years": 4,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999,
                      |    "year3Rent": 999,
                      |    "year4Rent": 999
                      |  }
                      |}
              """.stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":0,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":3669,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":0,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":5,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":0
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":15,
                  |      "taxDue":0
                  |      },{
                  |      "from":1500000,
                  |      "to":-1,
                  |      "rate":17,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  },
                  |  {
                  |   "totalTax":0,
                  |   "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |   "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |   "npv":3669,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":0,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":2,
                  |      "taxDue":0
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
                  |}
          """.stripMargin)
              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 30K Premiums + HRAD + Rent < 1000 (NRSDLT Exempt on Rent)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 30000,
                      |  "highestRent": 999,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years": 100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999,
                      |    "year3Rent": 999,
                      |    "year4Rent": 999,
                      |    "year5Rent": 999
                      |  }
                      |}
                """.stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":0,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":27627,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":0,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":5,
                  |      "taxDue":0
                  |      },{
                  |      "from":500000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":0
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":15,
                  |      "taxDue":0
                  |      },{
                  |      "from":1500000,
                  |      "to":-1,
                  |      "rate":17,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  },
                  |  {
                  |   "totalTax":0,
                  |   "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |   "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |   "npv":27627,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":0,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":500000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":500000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":0,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":500000,
                  |      "rate":2,
                  |      "taxDue":0
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
                  |}
            """.stripMargin)
              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential negative test - missing non-uk resident answer" in {

              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 4,
                      |  "effectiveDateYear": 2021,
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 4,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 31,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years": 100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000,
                      |    "year2Rent": 99000,
                      |    "year3Rent": 99000,
                      |    "year4Rent": 99000,
                      |    "year5Rent": 99000
                      |  }
                      |}
              """.stripMargin)
                )

              request.status shouldBe BAD_REQUEST
              request.body shouldBe "\"Validation error: List(ValidationFailure(Non UK resident question not answered for effective date after 31 March 2021))\""
            }

          }
        }

      }
    }
  }
}
