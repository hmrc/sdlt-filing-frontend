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

class CalculationControllerLeaseholdJuly21ISpec extends UnitSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 and valid result for leasehold property type" when {

      "the effective date is JUL 1 2021" when {

        "non-residential, company, 500K Premium + HRAD" in{
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Non-residential",
                  |  "effectiveDateDay": 1,
                  |  "effectiveDateMonth": 7,
                  |  "effectiveDateYear": 2021,
                  |  "premium": 500000,
                  |  "highestRent": 99000,
                  |  "propertyDetails": {
                  |     "individual": "No",
                  |     "twoOrMoreProperties": "No"
                  |   },
                  |  "leaseDetails": {
                  |    "startDateDay": 1,
                  |    "startDateMonth": 7,
                  |    "startDateYear": 2021,
                  |    "endDateDay": 30,
                  |    "endDateMonth": 6,
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
                  |}""".stripMargin
              )
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
              |}""".stripMargin
          )

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "the purchasers are all UK resident" when {

          "the purchaser is a company" when {
            "residential 1M Premium (NRB250 Leasehold Business Thread 7)" in{
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 1000000,
                      |  "highestRent": 9100,
                      |  "propertyDetails": {
                      |     "individual": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                  |   "totalTax":71266,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":251664,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":16,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":16
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":71250,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":3,
                  |      "taxDue":7500
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":8,
                  |      "taxDue":54000
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
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                  |   "totalTax":36378,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":24878,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":24878
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":11500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":3,
                  |      "taxDue":7500
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":8,
                  |      "taxDue":4000
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

            "residential 300K Premium (NRB250 Leasehold Business Thread 1)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":27378,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":24878,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":24878
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
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
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

            "residential 275K Premium + FTB (NRB250 Leasehold Business Thread 4)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 275000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "No",
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin
                  )
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
                  |      "taxDue":24878,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":24878
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 275K Premium + FTB + Shared Ownership (NRB250 Leasehold Business Thread 5)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 275000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "No",
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "Yes",
                      |     "currentValue": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                  |   "npv":2737887,
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
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":0,
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 350K Premium (NRB250 Leasehold Business Thread 10)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 350000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":29878,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":24878,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":24878
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":5000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":5,
                  |      "taxDue":5000
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

            "additional property" when {
              "residential 275K Premium + HRAD (NRB250 Leasehold Business Thread 2)" in {
                def request: WSResponse = ws.url(
                  calculateUrl)
                  .post(
                    Json.parse(
                      """
                        |{
                        |  "holdingType": "Leasehold",
                        |  "propertyType": "Residential",
                        |  "effectiveDateDay": 1,
                        |  "effectiveDateMonth": 7,
                        |  "effectiveDateYear": 2021,
                        |  "nonUKResident": "No",
                        |  "premium": 275000,
                        |  "highestRent": 9100,
                        |  "propertyDetails": {
                        |     "individual": "Yes",
                        |     "twoOrMoreProperties": "Yes",
                        |     "replaceMainResidence": "No"
                        |   },
                        |  "leaseDetails": {
                        |    "startDateDay": 1,
                        |    "startDateMonth": 7,
                        |    "startDateYear": 2021,
                        |    "endDateDay": 30,
                        |    "endDateMonth": 6,
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
                    |   "totalTax":9516,
                    |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                    |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £8,250.",
                    |   "npv":251664,
                    |   "taxCalcs":[
                    |     {
                    |      "taxType":"rent",
                    |      "calcType":"slice",
                    |      "taxDue":16,
                    |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                    |      "bandHeading":"Rent bands (£)",
                    |      "detailFooter":"SDLT due on the rent",
                    |      "slices":[
                    |      {
                    |       "from":0,
                    |       "to":250000,
                    |       "rate":0,
                    |       "taxDue":0
                    |       },{
                    |       "from":250000,
                    |       "to":-1,
                    |       "rate":1,
                    |       "taxDue":16
                    |       }
                    |      ]
                    |     },
                    |    {
                    |     "taxType":"premium",
                    |     "calcType":"slice",
                    |     "taxDue":9500,
                    |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                    |     "bandHeading":"Premium bands (£)",
                    |     "detailFooter":"SDLT due on the premium",
                    |     "slices":[
                    |     {
                    |      "from":0,
                    |      "to":250000,
                    |      "rate":3,
                    |      "taxDue":7500
                    |      },{
                    |      "from":250000,
                    |      "to":925000,
                    |      "rate":8,
                    |      "taxDue":2000
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
                    |  },{
                    |  "totalTax":1266,
                    |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                    |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.",
                    |  "npv":251664,
                    |  "taxCalcs":[
                    |   {
                    |    "taxType":"rent",
                    |    "calcType":"slice",
                    |    "taxDue":16,
                    |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                    |    "bandHeading":"Rent bands (£)",
                    |    "detailFooter":"SDLT due on the rent",
                    |    "slices":[
                    |     {
                    |      "from":0,
                    |      "to":250000,
                    |      "rate":0,
                    |      "taxDue":0
                    |      },{
                    |      "from":250000,
                    |      "to":-1,
                    |      "rate":1,
                    |      "taxDue":16
                    |      }
                    |     ]
                    |     },{
                    |      "taxType":"premium",
                    |      "calcType":"slice",
                    |      "taxDue":1250,
                    |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                    |      "bandHeading":"Premium bands (£)",
                    |      "detailFooter":"SDLT due on the premium",
                    |      "slices":[
                    |       {
                    |        "from":0,
                    |        "to":250000,
                    |        "rate":0,
                    |        "taxDue":0
                    |        },{
                    |        "from":250000,
                    |        "to":925000,
                    |        "rate":5,
                    |        "taxDue":1250
                    |        },{
                    |        "from":925000,
                    |        "to":1500000,
                    |        "rate":10,
                    |        "taxDue":0
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
        }

        "one or more purchasers are non-UK resident" when {

          "the purchaser is a company" when {

            "residential 300K Premium" in{
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
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
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""". stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":97136,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":79636,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":5000
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":74636
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":17500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":5,
                  |      "taxDue":12500
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":5000
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 1M Premium (NRB250 Leasehold Business Thread 8)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 1000000,
                      |  "highestRent": 9100,
                      |  "propertyDetails": {
                      |     "individual": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                  |   "totalTax":96299,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":251664,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":5049,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":5000
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":49
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":91250,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":5,
                  |      "taxDue":12500
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":67500
                  |      },{
                  |      "from":925000,
                  |      "to":1500000,
                  |      "rate":15,
                  |      "taxDue":11250
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }
          }

          "the purchaser(s) is an individual" when {

            "residential 275K Premium + FTB (NRB250 Leasehold Business Thread 6)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 275000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "No",
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":85136,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":79636,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":5000
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":74636
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":5500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":300000,
                  |      "rate":2,
                  |      "taxDue":5500
                  |      },{
                  |      "from":300000,
                  |      "to":500000,
                  |      "rate":7,
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
                  |     "taxDue":24878,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":24878
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 39K Premium + FTB + Rent < 1K (NRSDLT out of scope)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 39000,
                      |  "highestRent": 999,
                      |  "ownedOtherProperties": "No",
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 30K Premium + Rent < 1K (NRSDLT out of scope)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 30000,
                      |  "highestRent": 999,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
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
                  |      "to":250000,
                  |      "rate":0,
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 275K Premium + HRAD (NRB250 Leasehold Business Thread 3)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 275000,
                      |  "highestRent": 9100,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                  |   "totalTax":20049,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £8,250.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |   "npv":251664,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":5049,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":5000
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":49
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
                  |      "to":250000,
                  |      "rate":5,
                  |      "taxDue":12500
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":2500
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
                  |  "totalTax":11799,
                  |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |  "npv":251664,
                  |  "taxCalcs":[
                  |   {
                  |    "taxType":"rent",
                  |    "calcType":"slice",
                  |    "taxDue":5049,
                  |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |    "bandHeading":"Rent bands (£)",
                  |    "detailFooter":"SDLT due on the rent",
                  |    "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":2,
                  |      "taxDue":5000
                  |      },{
                  |      "from":250000,
                  |      "to":-1,
                  |      "rate":3,
                  |      "taxDue":49
                  |      }
                  |     ]
                  |     },{
                  |      "taxType":"premium",
                  |      "calcType":"slice",
                  |      "taxDue":6750,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |      "bandHeading":"Premium bands (£)",
                  |      "detailFooter":"SDLT due on the premium",
                  |      "slices":[
                  |       {
                  |        "from":0,
                  |        "to":250000,
                  |        "rate":2,
                  |        "taxDue":5000
                  |        },{
                  |        "from":250000,
                  |        "to":925000,
                  |        "rate":7,
                  |        "taxDue":1750
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 300K Premium FTB + Shared Ownership (NRB250 Leasehold Business Thread 12)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "No",
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "Yes",
                      |     "currentValue": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
              """.stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":6000,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
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
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":0,
                  |       "taxDue":0
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
                  |      "to":300000,
                  |      "rate":2,
                  |      "taxDue":6000
                  |      },{
                  |      "from":300000,
                  |      "to":500000,
                  |      "rate":7,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    }
                  |   ]
                  |  },{
                  |    "totalTax":0,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
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
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":-1,
                  |      "rate":0,
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 300K Premium + NRSDLT (Scenario 1)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":88136,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":79636,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":5000
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":74636
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":8500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":2,
                  |      "taxDue":5000
                  |      },{
                  |      "from":250000,
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
                  |    "totalTax":27378,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":24878,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":24878
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
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
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

            "residential 500K Premium 1Y Lease + NRSDLT out of scope (Scenario 2)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
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
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
                      |    "endDateYear": 2022,
                      |    "leaseTerm":  {
                      |      "years":1,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": "99000"
                      |  }
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":12500,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":95652,
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
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":12500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":5,
                  |      "taxDue":12500
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

            "residential 30K Premium 100Y Lease + NRSDLT (Scenario 3)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 30000,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":80236,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":79636,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":5000
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":74636
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":600,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":2,
                  |      "taxDue":600
                  |      },{
                  |      "from":250000,
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
                  |    "totalTax":24878,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":24878,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":24878
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
                  |      "to":250000,
                  |      "rate":0,
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 0 Premium 100Y Lease + NRSDLT (Scenario 4)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 0,
                      |  "highestRent": 99000,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":79636,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":79636,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":5000
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":74636
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
                  |      "to":250000,
                  |      "rate":2,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
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
                  |    "totalTax":24878,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":24878,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":24878
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
                  |      "to":250000,
                  |      "rate":0,
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 0 Premium 100Y Lease <1K Rent + NRSDLT out of scope (Scenario 5)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 0,
                      |  "highestRent": 999,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin)
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
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
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
                  |      "to":250000,
                  |      "rate":0,
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 550K Premium + NRSDLT (Scenario 6)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 550000,
                      |  "highestRent": 999,
                      |  "ownedOtherProperties": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":26552,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":27627,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":552,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":552
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":26000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":2,
                  |      "taxDue":5000
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":7,
                  |      "taxDue":21000
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
                  |    "totalTax":15000,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":27627,
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
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    },
                  |   {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":15000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":5,
                  |      "taxDue":15000
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

            "residential 300K Premium 100Y Lease + NRSDLT and HRAD (Scenario 7)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":97136,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £9,000.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":79636,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":5000
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":74636
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":17500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":5,
                  |      "taxDue":12500
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":5000
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
                  |  "totalTax":88136,
                  |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |  "npv":2737887,
                  |  "taxCalcs":[
                  |   {
                  |    "taxType":"rent",
                  |    "calcType":"slice",
                  |    "taxDue":79636,
                  |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |    "bandHeading":"Rent bands (£)",
                  |    "detailFooter":"SDLT due on the rent",
                  |    "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":2,
                  |      "taxDue":5000
                  |      },{
                  |      "from":250000,
                  |      "to":-1,
                  |      "rate":3,
                  |      "taxDue":74636
                  |      }
                  |     ]
                  |     },{
                  |      "taxType":"premium",
                  |      "calcType":"slice",
                  |      "taxDue":8500,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |      "bandHeading":"Premium bands (£)",
                  |      "detailFooter":"SDLT due on the premium",
                  |      "slices":[
                  |       {
                  |        "from":0,
                  |        "to":250000,
                  |        "rate":2,
                  |        "taxDue":5000
                  |        },{
                  |        "from":250000,
                  |        "to":925000,
                  |        "rate":7,
                  |        "taxDue":3500
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 500K Premium 1Y Lease + HRAD and NRSDLT both out of scope (Scenario 8)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 500000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
                      |    "endDateYear": 2022,
                      |    "leaseTerm":  {
                      |      "years": 1,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000
                      |  }
                      |}""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":12500,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":95652,
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
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":12500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":5,
                  |      "taxDue":12500
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

            "residential 30K Premium 100Y Lease + NRSDLT but HRAD out of scope (Scenario 9)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
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
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":80236,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":79636,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":5000
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":74636
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":600,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":2,
                  |      "taxDue":600
                  |      },{
                  |      "from":250000,
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
                  |  "totalTax":24878,
                  |  "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |  "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |  "npv":2737887,
                  |  "taxCalcs":[
                  |   {
                  |    "taxType":"rent",
                  |    "calcType":"slice",
                  |    "taxDue":24878,
                  |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |    "bandHeading":"Rent bands (£)",
                  |    "detailFooter":"SDLT due on the rent",
                  |    "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":24878
                  |      }
                  |     ]
                  |     },{
                  |      "taxType":"premium",
                  |      "calcType":"slice",
                  |      "taxDue":0,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |      "bandHeading":"Premium bands (£)",
                  |      "detailFooter":"SDLT due on the premium",
                  |      "slices":[
                  |       {
                  |        "from":0,
                  |        "to":250000,
                  |        "rate":0,
                  |        "taxDue":0
                  |        },{
                  |        "from":250000,
                  |        "to":925000,
                  |        "rate":5,
                  |        "taxDue":0
                  |        },{
                  |        "from":925000,
                  |        "to":1500000,
                  |        "rate":10,
                  |        "taxDue":0
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 0 Premium 100Y Lease + NRSDLT but HRAD out of scope (Scenario 10)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 0,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":79636,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":79636,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":5000
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":74636
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
                  |      "to":250000,
                  |      "rate":2,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
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
                  |  "totalTax":24878,
                  |  "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |  "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |  "npv":2737887,
                  |  "taxCalcs":[
                  |   {
                  |    "taxType":"rent",
                  |    "calcType":"slice",
                  |    "taxDue":24878,
                  |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |    "bandHeading":"Rent bands (£)",
                  |    "detailFooter":"SDLT due on the rent",
                  |    "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":250000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":24878
                  |      }
                  |     ]
                  |     },{
                  |      "taxType":"premium",
                  |      "calcType":"slice",
                  |      "taxDue":0,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |      "bandHeading":"Premium bands (£)",
                  |      "detailFooter":"SDLT due on the premium",
                  |      "slices":[
                  |       {
                  |        "from":0,
                  |        "to":250000,
                  |        "rate":0,
                  |        "taxDue":0
                  |        },{
                  |        "from":250000,
                  |        "to":925000,
                  |        "rate":5,
                  |        "taxDue":0
                  |        },{
                  |        "from":925000,
                  |        "to":1500000,
                  |        "rate":10,
                  |        "taxDue":0
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 0 Premium 100Y Lease <1K Rent NRSDLT and HRAD both out of scope (Scenario 11)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 0,
                      |  "highestRent": 999,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                  |       "to":250000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":250000,
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
                  |      "to":250000,
                  |      "rate":0,
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 550K Premium 100Y Lease <1K Rent + NRSDLT and HRAD (Scenario 12)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 7,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 550000,
                      |  "highestRent": 999,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 7,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 6,
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
                      |}""".stripMargin
                  )
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":43052,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £16,500.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |   "npv":27627,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":552,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":250000,
                  |       "rate":2,
                  |       "taxDue":552
                  |       },{
                  |       "from":250000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":42500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":5,
                  |      "taxDue":12500
                  |      },{
                  |      "from":250000,
                  |      "to":925000,
                  |      "rate":10,
                  |      "taxDue":30000
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
                  |  "totalTax":26552,
                  |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |  "npv":27627,
                  |  "taxCalcs":[
                  |   {
                  |    "taxType":"rent",
                  |    "calcType":"slice",
                  |    "taxDue":552,
                  |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |    "bandHeading":"Rent bands (£)",
                  |    "detailFooter":"SDLT due on the rent",
                  |    "slices":[
                  |     {
                  |      "from":0,
                  |      "to":250000,
                  |      "rate":2,
                  |      "taxDue":552
                  |      },{
                  |      "from":250000,
                  |      "to":-1,
                  |      "rate":3,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |     },{
                  |      "taxType":"premium",
                  |      "calcType":"slice",
                  |      "taxDue":26000,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |      "bandHeading":"Premium bands (£)",
                  |      "detailFooter":"SDLT due on the premium",
                  |      "slices":[
                  |       {
                  |        "from":0,
                  |        "to":250000,
                  |        "rate":2,
                  |        "taxDue":5000
                  |        },{
                  |        "from":250000,
                  |        "to":925000,
                  |        "rate":7,
                  |        "taxDue":21000
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

          }
        }
      }
    }
  }
}
