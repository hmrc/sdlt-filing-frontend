/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package leasehold

import base.BaseSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}

class CalculationControllerLeaseholdOct21ISpec extends BaseSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 and valid result for leasehold property type" when {

      "the effective date is OCT 1 2021" when {

        "non-residential, company 500K Premium" in {
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Non-residential",
                  |  "effectiveDateDay": 1,
                  |  "effectiveDateMonth": 10,
                  |  "effectiveDateYear": 2021,
                  |  "premium": 500000,
                  |  "highestRent": 99000,
                  |  "propertyDetails": {
                  |     "individual": "No"
                  |   },
                  |  "leaseDetails": {
                  |    "startDateDay": 1,
                  |    "startDateMonth": 4,
                  |    "startDateYear": 2021,
                  |    "endDateDay": 30,
                  |    "endDateMonth": 9,
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
                  |}""".stripMargin)
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
              |}""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "the purchasers are all UK resident" when {

          "the purchaser is a company" when {
            "residential 300K Premium" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |   "totalTax":40128,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":26128,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":26128
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":14000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":3,
                  |      "taxDue":3750
                  |      },{
                  |      "from":125000,
                  |      "to":250000,
                  |      "rate":5,
                  |      "taxDue":6250
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }
          }

          "the purchaser(s) is an individual" when {

            "residential 300K Premium" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |{
                  |    "totalTax":31128,
                  |    "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":26128,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":26128
                  |      }
                  |     ]
                  |    },
                  |   {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":5000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 300K Premium + FTB" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |"result":[{
                  |    "totalTax":31128,
                  |    "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":26128,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate": 1,
                  |      "taxDue":26128
                  |      }
                  |     ]
                  |    },
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 300K Premium + FTB + Shared Ownership" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "Yes",
                      |     "currentValue": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |{
                  |    "totalTax":0,
                  |    "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
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
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":0,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    },
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, FTB, Shared Ownership, Premium less than 40K, Rent < 1K" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 5000,
                      |  "highestRent": 999,
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "Yes",
                      |     "currentValue": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2025,
                      |    "leaseTerm":  {
                      |      "years":4,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999,
                      |    "year3Rent": 999,
                      |    "year4Rent": 999
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, FTB, Shared Ownership, Premium less than 40K, Rent > 1K, > 7yr lease" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 5000,
                      |  "highestRent": 1001,
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "Yes",
                      |     "currentValue": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years":100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 1001,
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
                  |   "npv":27675,
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, FTB, Shared Ownership, Premium less than 40K, Rent > 1K, < 7yr lease" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 5000,
                      |  "highestRent": 1001,
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "Yes",
                      |     "currentValue": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2022,
                      |    "leaseTerm":  {
                      |      "years":1,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 1001
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
                  |   "npv":967,
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, FTB, Premium less than 40K, Rent > 1K, < 7yr lease" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 5000,
                      |  "highestRent": 1001,
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2022,
                      |    "leaseTerm":  {
                      |      "years":1,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 1001
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
                  |   "npv":967,
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, Premium 500K + one year rent input" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2022,
                      |    "leaseTerm":  {
                      |      "years":1,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000
                      |  }
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |{
                  |    "totalTax":15000,
                  |    "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
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
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
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
                  |}""".stripMargin)

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
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |{
                  |    "totalTax":31128,
                  |    "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":26128,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":26128
                  |      }
                  |     ]
                  |    },
                  |   {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":5000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, Additional Property" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
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
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |{
                  |  "totalTax":38128,
                  |  "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |  "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £8,250.",
                  |  "npv":2737887,
                  |  "taxCalcs":[
                  |  {
                  |    "taxType":"rent",
                  |    "calcType":"slice",
                  |    "taxDue":26128,
                  |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |    "bandHeading":"Rent bands (£)",
                  |    "detailFooter":"SDLT due on the rent",
                  |    "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |     },
                  |     {
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":26128
                  |     }
                  |    ]
                  |     },{
                  |      "taxType":"premium",
                  |      "calcType":"slice",
                  |      "taxDue":12000,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |      "bandHeading":"Premium bands (£)",
                  |      "detailFooter":"SDLT due on the premium",
                  |      "slices":[
                  |       {
                  |        "from":0,
                  |        "to":125000,
                  |        "rate":3,
                  |        "taxDue":3750
                  |       },
                  |       {
                  |        "from":125000,
                  |        "to":250000,
                  |        "rate":5,
                  |        "taxDue":6250
                  |       },
                  |       {
                  |        "from":250000,
                  |        "to":925000,
                  |        "rate":8,
                  |        "taxDue":2000
                  |       },
                  |       {
                  |        "from":925000,
                  |        "to":1500000,
                  |        "rate":13,
                  |        "taxDue":0
                  |       },
                  |       {
                  |        "from":1500000,
                  |        "to":-1,
                  |        "rate":15,
                  |        "taxDue":0
                  |       }
                  |      ]
                  |     }
                  |    ]
                  | },{
                  |  "totalTax":29878,
                  |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.",
                  |  "npv":2737887,
                  |  "taxCalcs":[
                  |   {
                  |    "taxType":"rent",
                  |    "calcType":"slice",
                  |    "taxDue":26128,
                  |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |    "bandHeading":"Rent bands (£)",
                  |    "detailFooter":"SDLT due on the rent",
                  |    "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":26128
                  |      }
                  |     ]
                  |     },{
                  |      "taxType":"premium",
                  |      "calcType":"slice",
                  |      "taxDue":3750,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |      "bandHeading":"Premium bands (£)",
                  |      "detailFooter":"SDLT due on the premium",
                  |      "slices":[
                  |       {
                  |        "from":0,
                  |        "to":125000,
                  |        "rate":0,
                  |        "taxDue":0
                  |       },{
                  |        "from":125000,
                  |        "to":250000,
                  |        "rate":2,
                  |        "taxDue":2500
                  |       },{
                  |        "from":250000,
                  |        "to":925000,
                  |        "rate":5,
                  |        "taxDue":1250
                  |       },{
                  |        "from":925000,
                  |        "to":1500000,
                  |        "rate":10,
                  |        "taxDue":0
                  |       },{
                  |        "from":1500000,
                  |        "to":-1,
                  |        "rate":12,
                  |        "taxDue":0
                  |       }
                  |      ]
                  |     }
                  |    ]
                  |   }
                  | ]
                  |}""".stripMargin)
              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, Additional Property premium < 40K HRAD out of scope" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 30000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |   "totalTax":26128,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":26128,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":26128
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
                  |}
            """.stripMargin)
              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, Additional Property, 7Y lease HRAD out of scope" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "No",
                      |  "premium": 30000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "Yes",
                      |     "replaceMainResidence": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2028,
                      |    "leaseTerm":  {
                      |      "years": 7,
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
                  |   "totalTax":4803,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":605339,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":4803,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":4803
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
          }
        }

        "one or more purchasers are non-UK resident" when {
          "the purchaser is a company" when {
            "residential" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "propertyDetails": {
                      |     "individual": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |   "totalTax":100886,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":80886,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":2,
                  |       "taxDue":2500
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":78386
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":20000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":5,
                  |      "taxDue":6250
                  |      },{
                  |      "from":125000,
                  |      "to":250000,
                  |      "rate":7,
                  |      "taxDue":8750
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
          }

          "the purchaser(s) is an individual" when {
            "residential 300K Premium 100Y Lease + NRSDLT (Scenario 1)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |   "totalTax":91886,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":80886,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":2,
                  |       "taxDue":2500
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":78386
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":11000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
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
                  |    "totalTax":31128,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":26128,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":26128
                  |      }
                  |     ]
                  |    },
                  |   {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":5000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
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
                  |}""".stripMargin)

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
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2022,
                      |    "leaseTerm":  {
                      |      "years":1,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000
                      |  }
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":15000,
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
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
                  |}""".stripMargin)

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
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |   "totalTax":81486,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":80886,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":2,
                  |       "taxDue":2500
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":78386
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
                  |      "to":125000,
                  |      "rate":2,
                  |      "taxDue":600
                  |      },{
                  |      "from":125000,
                  |      "to":250000,
                  |      "rate":4,
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
                  |    "totalTax":26128,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":26128,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":26128
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
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |   "totalTax":80886,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":80886,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":2,
                  |       "taxDue":2500
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":78386
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
                  |      "to":125000,
                  |      "rate":2,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":250000,
                  |      "rate":4,
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
                  |    "totalTax":26128,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":26128,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":26128
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
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
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
                  |}""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 550K Premium 100Y Lease <1K Rent + NRSDLT (Scenario 6)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |   "totalTax":29052,
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
                  |       "to":125000,
                  |       "rate":2,
                  |       "taxDue":552
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":28500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
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
                  |    "totalTax":17500,
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
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    },
                  |   {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":17500,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
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
                  |}""".stripMargin)

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
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateDay": 15,
                      |    "startDateMonth": 1,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":100886,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £9,000.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":80886,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":2,
                  |       "taxDue":2500
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":78386
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":20000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":5,
                  |      "taxDue":6250
                  |      },{
                  |      "from":125000,
                  |      "to":250000,
                  |      "rate":7,
                  |      "taxDue":8750
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
                  |  "totalTax":91886,
                  |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
                  |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
                  |  "npv":2737887,
                  |  "taxCalcs":[
                  |   {
                  |    "taxType":"rent",
                  |    "calcType":"slice",
                  |    "taxDue":80886,
                  |    "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |    "bandHeading":"Rent bands (£)",
                  |    "detailFooter":"SDLT due on the rent",
                  |    "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":2,
                  |      "taxDue":2500
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":3,
                  |      "taxDue":78386
                  |      }
                  |      ]
                  |     },{
                  |      "taxType":"premium",
                  |      "calcType":"slice",
                  |      "taxDue":11000,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |      "bandHeading":"Premium bands (£)",
                  |      "detailFooter":"SDLT due on the premium",
                  |      "slices":[
                  |       {
                  |        "from":0,
                  |        "to":125000,
                  |        "rate":2,
                  |        "taxDue":2500
                  |        },{
                  |        "from":125000,
                  |        "to":250000,
                  |        "rate":4,
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential 500K Premium 1Y Lease + NRSDLT but HRAD out of scope (Scenario 8)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2022,
                      |    "leaseTerm":  {
                      |      "years": 1,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 99000
                      |  }
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":15000,
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":1,
                  |       "taxDue":0
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
                  |}""".stripMargin)

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
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |   "totalTax":81486,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":80886,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":2,
                  |       "taxDue":2500
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":78386
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
                  |      "to":125000,
                  |      "rate":2,
                  |      "taxDue":600
                  |      },{
                  |      "from":125000,
                  |      "to":250000,
                  |      "rate":4,
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
                  |    "totalTax":26128,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":26128,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":26128
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
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |   "totalTax":80886,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":80886,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":2,
                  |       "taxDue":2500
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":78386
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
                  |      "to":125000,
                  |      "rate":2,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":250000,
                  |      "rate":4,
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
                  |    "totalTax":26128,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":26128,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":1,
                  |      "taxDue":26128
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

            "residential 0 Premium 100Y Lease <1K Rent + NRSDLT and HRAD both out of scope (Scenario 11)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
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

            "residential 550K Premium 100Y Lease < 1K Rent + NRSDLT and HRAD (Scenario 12)" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
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
                      |    "startDateDay": 15,
                      |    "startDateMonth": 1,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                      |}""".stripMargin)
                )

              val responseJson = Json.parse(
                """
                  |{
                  |"result":[
                  |  {
                  |   "totalTax":45552,
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
                  |       "to":125000,
                  |       "rate":2,
                  |       "taxDue":552
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":0
                  |       }
                  |      ]
                  |     },
                  |    {
                  |     "taxType":"premium",
                  |     "calcType":"slice",
                  |     "taxDue":45000,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |     "bandHeading":"Premium bands (£)",
                  |     "detailFooter":"SDLT due on the premium",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":5,
                  |      "taxDue":6250
                  |      },{
                  |      "from":125000,
                  |      "to":250000,
                  |      "rate":7,
                  |      "taxDue":8750
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
                  |  "totalTax":29052,
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
                  |      "to":125000,
                  |      "rate":2,
                  |      "taxDue":552
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":3,
                  |      "taxDue":0
                  |      }
                  |      ]
                  |     },{
                  |      "taxType":"premium",
                  |      "calcType":"slice",
                  |      "taxDue":28500,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
                  |      "bandHeading":"Premium bands (£)",
                  |      "detailFooter":"SDLT due on the premium",
                  |      "slices":[
                  |       {
                  |        "from":0,
                  |        "to":125000,
                  |        "rate":2,
                  |        "taxDue":2500
                  |        },{
                  |        "from":125000,
                  |        "to":250000,
                  |        "rate":4,
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 300K Premium + FTB" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |   "totalTax":86886,
                  |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
                  |   "resultHint":"The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.",
                  |   "npv":2737887,
                  |   "taxCalcs":[
                  |     {
                  |      "taxType":"rent",
                  |      "calcType":"slice",
                  |      "taxDue":80886,
                  |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |      "bandHeading":"Rent bands (£)",
                  |      "detailFooter":"SDLT due on the rent",
                  |      "slices":[
                  |      {
                  |       "from":0,
                  |       "to":125000,
                  |       "rate":2,
                  |       "taxDue":2500
                  |       },{
                  |       "from":125000,
                  |       "to":-1,
                  |       "rate":3,
                  |       "taxDue":78386
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
                  |    "totalTax":26128,
                  |    "resultHeading":"Result if you become eligible for a repayment of the non-resident rate of SDLT",
                  |    "resultHint":"If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.",
                  |    "npv":2737887,
                  |    "taxCalcs":[
                  |    {
                  |     "taxType":"rent",
                  |     "calcType":"slice",
                  |     "taxDue":26128,
                  |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
                  |     "bandHeading":"Rent bands (£)",
                  |     "detailFooter":"SDLT due on the rent",
                  |     "slices":[
                  |     {
                  |      "from":0,
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate": 1,
                  |      "taxDue":26128
                  |      }
                  |     ]
                  |    },
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential, 300K Premium + FTB + Shared Ownership" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 300000,
                      |  "highestRent": 99000,
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "Yes",
                      |     "currentValue": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
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
                  |      "to":125000,
                  |      "rate":0,
                  |      "taxDue":0
                  |      },{
                  |      "from":125000,
                  |      "to":-1,
                  |      "rate":0,
                  |      "taxDue":0
                  |      }
                  |     ]
                  |    },
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential Premium < 40K + FTB + Shared Ownership + Rent < 1K" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 5000,
                      |  "highestRent": 999,
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "Yes",
                      |     "currentValue": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2025,
                      |    "leaseTerm":  {
                      |      "years":4,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999,
                      |    "year3Rent": 999,
                      |    "year4Rent": 999
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential Premium < 40K + FTB + Shared Ownership + Rent > 1K + > 7yr lease" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 5000,
                      |  "highestRent": 1001,
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "Yes",
                      |     "currentValue": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2121,
                      |    "leaseTerm":  {
                      |      "years":100,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 1001,
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
                  |   "npv":27675,
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
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

            "residential Premium < 40K + FTB, Shared Ownership + Rent > 1K + < 7yr lease" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 5000,
                      |  "highestRent": 1001,
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "Yes",
                      |     "currentValue": "Yes"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2022,
                      |    "leaseTerm":  {
                      |      "years":1,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 1001
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
                  |   "npv":967,
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
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
                  |}""".stripMargin)

              request.status shouldBe OK
              request.json shouldBe responseJson
            }

            "residential Premium < 40K + FTB + Rent > 1K, < 7yr lease" in {
              def request: WSResponse = ws.url(
                calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 1,
                      |  "effectiveDateMonth": 10,
                      |  "effectiveDateYear": 2021,
                      |  "nonUKResident": "Yes",
                      |  "premium": 5000,
                      |  "highestRent": 1001,
                      |  "firstTimeBuyer": "Yes",
                      |  "propertyDetails": {
                      |     "individual": "Yes",
                      |     "twoOrMoreProperties": "No",
                      |     "sharedOwnership": "No"
                      |   },
                      |  "leaseDetails": {
                      |    "startDateDay": 1,
                      |    "startDateMonth": 10,
                      |    "startDateYear": 2021,
                      |    "endDateDay": 30,
                      |    "endDateMonth": 9,
                      |    "endDateYear": 2022,
                      |    "leaseTerm":  {
                      |      "years":1,
                      |      "days": 0,
                      |      "daysInPartialYear": 0
                      |     },
                      |    "year1Rent": 1001
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
                  |   "npv":967,
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
                  |       "to":125000,
                  |       "rate":0,
                  |       "taxDue":0
                  |       },{
                  |       "from":125000,
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
