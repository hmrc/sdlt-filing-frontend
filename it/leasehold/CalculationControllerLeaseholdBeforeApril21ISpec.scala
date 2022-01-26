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

class CalculationControllerLeaseholdBeforeApril21ISpec extends BaseSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 and valid result for leasehold property type" when {

      "the effective date is NOV 2018" when {
        "residential, Individual && notTwoOrMoreProperties, FTB" in {
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Residential",
                  |  "effectiveDateDay": 23,
                  |  "effectiveDateMonth": 11,
                  |  "effectiveDateYear": 2017,
                  |  "premium": 500000,
                  |  "highestRent": 50000,
                  |  "propertyDetails": {
                  |     "individual": "Yes",
                  |     "twoOrMoreProperties": "No",
                  |     "sharedOwnership": "No"
                  |   },
                  |  "leaseDetails": {
                  |    "startDateDay": 15,
                  |    "startDateMonth": 1,
                  |    "startDateYear": 1949,
                  |    "endDateDay": 31,
                  |    "endDateMonth": 12,
                  |    "endDateYear": 2049,
                  |    "leaseTerm":  {
                  |      "years": 32,
                  |      "days": 39,
                  |      "daysInPartialYear": 365
                  |     },
                  |    "year1Rent": 10000,
                  |    "year2Rent": 20000,
                  |    "year3Rent": 30000,
                  |    "year4Rent": 40000,
                  |    "year5Rent": 50000
                  |  },
                  |  "firstTimeBuyer": "Yes"
                  |}""".stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              | "result":[
              |  {
              |   "totalTax":17367,
              |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |   "npv":861753,
              |   "taxCalcs":[
              |    {
              |     "taxType":"rent",
              |     "calcType":"slice",
              |     "taxDue":7367,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
              |     "bandHeading":"Rent bands (£)",
              |     "detailFooter":"SDLT due on the rent",
              |     "slices":[
              |      {
              |       "from":0,
              |       "to":125000,
              |       "rate":0,
              |       "taxDue":0
              |       },{
              |       "from":125000,
              |       "to":-1,
              |       "rate":1,
              |       "taxDue":7367
              |      }
              |     ]
              |    },{
              |     "taxType":"premium",
              |     "calcType":"slice",
              |     "taxDue":10000,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
              |     "bandHeading":"Premium bands (£)",
              |     "detailFooter":"SDLT due on the premium",
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
              |       "taxDue":10000
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

        "residential, Individual && notTwoOrMoreProperties, FTB, Shared" in {
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Residential",
                  |  "effectiveDateDay": 1,
                  |  "effectiveDateMonth": 11,
                  |  "effectiveDateYear": 2018,
                  |  "premium": 315000,
                  |  "highestRent": 15000,
                  |  "propertyDetails": {
                  |     "individual": "Yes",
                  |     "twoOrMoreProperties": "No",
                  |     "sharedOwnership": "Yes",
                  |     "currentValue": "Yes"
                  |   },
                  |  "leaseDetails": {
                  |    "startDateDay": 1,
                  |    "startDateMonth": 11,
                  |    "startDateYear": 2018,
                  |    "endDateDay": 1,
                  |    "endDateMonth": 11,
                  |    "endDateYear": 3007,
                  |    "leaseTerm":  {
                  |      "years": 989,
                  |      "days": 1,
                  |      "daysInPartialYear": 365
                  |     },
                  |    "year1Rent": 15000,
                  |    "year2Rent": 15000,
                  |    "year3Rent": 15000,
                  |    "year4Rent": 15000,
                  |    "year5Rent": 15000
                  |  },
                  |  "firstTimeBuyer": "Yes"
                  |}""".stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              | "result":[
              |  {
              |   "totalTax":750,
              |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |   "npv":428571,
              |   "taxCalcs":[
              |    {
              |     "taxType":"rent",
              |     "calcType":"slice",
              |     "taxDue":0,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
              |     "bandHeading":"Rent bands (£)",
              |     "detailFooter":"SDLT due on the rent",
              |     "slices":[
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
              |      }
              |     ]
              |    },{
              |     "taxType":"premium",
              |     "calcType":"slice",
              |     "taxDue":750,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
              |     "bandHeading":"Premium bands (£)",
              |     "detailFooter":"SDLT due on the premium",
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
              |       "taxDue":750
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

        "residential, Individual && notTwoOrMoreProperties, FTB, Shared (NPV proof)" in {
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Residential",
                  |  "effectiveDateDay": 1,
                  |  "effectiveDateMonth": 11,
                  |  "effectiveDateYear": 2018,
                  |  "premium": 195000,
                  |  "highestRent": 2500,
                  |  "propertyDetails": {
                  |     "individual": "Yes",
                  |     "twoOrMoreProperties": "No",
                  |     "sharedOwnership": "Yes",
                  |     "currentValue": "Yes"
                  |   },
                  |  "leaseDetails": {
                  |    "startDateDay": 1,
                  |    "startDateMonth": 11,
                  |    "startDateYear": 2018,
                  |    "endDateDay": 1,
                  |    "endDateMonth": 11,
                  |    "endDateYear": 3007,
                  |    "leaseTerm":  {
                  |      "years": 989,
                  |      "days": 1,
                  |      "daysInPartialYear": 365
                  |     },
                  |    "year1Rent": 2500,
                  |    "year2Rent": 2500,
                  |    "year3Rent": 2500,
                  |    "year4Rent": 2500,
                  |    "year5Rent": 2500
                  |  },
                  |  "firstTimeBuyer": "Yes"
                  |}""".stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              | "result":[
              |  {
              |   "totalTax":0,
              |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |   "npv":71428,
              |   "taxCalcs":[
              |    {
              |     "taxType":"rent",
              |     "calcType":"slice",
              |     "taxDue":0,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
              |     "bandHeading":"Rent bands (£)",
              |     "detailFooter":"SDLT due on the rent",
              |     "slices":[
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
              |      }
              |     ]
              |    },{
              |     "taxType":"premium",
              |     "calcType":"slice",
              |     "taxDue":0,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
              |     "bandHeading":"Premium bands (£)",
              |     "detailFooter":"SDLT due on the premium",
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

      "the effective date is JUL 2017" when {
        "non-residential, company" in {
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Non-residential",
                  |  "effectiveDateDay": 13,
                  |  "effectiveDateMonth": 7,
                  |  "effectiveDateYear": 2017,
                  |  "premium": 500000,
                  |  "highestRent": 50000,
                  |  "leaseDetails": {
                  |    "startDateDay": 15,
                  |    "startDateMonth": 1,
                  |    "startDateYear": 1949,
                  |    "endDateDay": 31,
                  |    "endDateMonth": 12,
                  |    "endDateYear": 2049,
                  |    "leaseTerm":  {
                  |      "years": 32,
                  |      "days": 172,
                  |      "daysInPartialYear": 365
                  |     },
                  |    "year1Rent": 10000,
                  |    "year2Rent": 20000,
                  |    "year3Rent": 30000,
                  |    "year4Rent": 40000,
                  |    "year5Rent": 50000
                  |  }
                  |}""".stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              | "result":[
              |  {
              |    "totalTax":21676,
              |    "resultHeading":"Results based on SDLT rules from 17 March 2016",
              |    "npv":867608,
              |    "taxCalcs":[
              |      {
              |       "taxType":"rent",
              |       "calcType":"slice",
              |       "taxDue":7176,
              |       "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 17 March 2016",
              |       "bandHeading":"Rent bands (£)",
              |       "detailFooter":"SDLT due on the rent",
              |       "slices":[
              |         {
              |          "from":0,
              |          "to":150000,
              |          "rate":0,
              |          "taxDue":0
              |          },{
              |          "from":150000,
              |          "to":5000000,
              |          "rate":1,
              |          "taxDue":7176
              |          },{
              |          "from":5000000,
              |          "to":-1,
              |          "rate":2,
              |          "taxDue":0
              |          }
              |         ]
              |        },
              |       {
              |       "taxType":"premium",
              |       "calcType":"slice",
              |       "taxDue":14500,
              |       "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 17 March 2016",
              |       "bandHeading":"Premium bands (£)",
              |       "detailFooter":"SDLT due on the premium",
              |       "slices":[
              |         {
              |          "from":0,
              |          "to":150000,
              |          "rate":0,
              |          "taxDue":0
              |          },{
              |          "from":150000,
              |          "to":250000,
              |          "rate":2,
              |          "taxDue":2000
              |          },{
              |          "from":250000,
              |          "to":-1,
              |          "rate":5,
              |          "taxDue":12500
              |          }
              |         ]
              |        }
              |       ]
              |      },
              |     {
              |      "totalTax":22176,
              |      "resultHeading":"Results based on SDLT rules before 17 March 2016",
              |      "resultHint":"You may be entitled to pay SDLT using the old rules if you exchanged contracts before 17 March 2016.",
              |      "npv":867608,
              |      "taxCalcs":[
              |        {
              |         "taxType":"rent",
              |         "calcType":"slice",
              |         "taxDue":7176,
              |         "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules before 17 March 2016",
              |         "bandHeading":"Rent bands (£)",
              |         "detailFooter":"SDLT due on the rent",
              |         "slices":[
              |           {
              |           "from":0,
              |           "to":150000,
              |           "rate":0,
              |           "taxDue":0
              |           },{
              |           "from":150000,
              |           "to":-1,
              |           "rate":1,
              |           "taxDue":7176
              |           }
              |          ]
              |         },
              |        {
              |         "taxType":"premium",
              |         "calcType":"slab",
              |         "taxDue":15000,
              |         "rate":3
              |       }
              |     ]
              |   }
              | ]
              |}""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, company" in {
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Residential",
                  |  "effectiveDateDay": 13,
                  |  "effectiveDateMonth": 7,
                  |  "effectiveDateYear": 2017,
                  |  "premium": 500000,
                  |  "highestRent": 50000,
                  |  "propertyDetails": {
                  |     "individual": "No"
                  |   },
                  |  "leaseDetails": {
                  |    "startDateDay": 15,
                  |    "startDateMonth": 1,
                  |    "startDateYear": 1949,
                  |    "endDateDay": 31,
                  |    "endDateMonth": 12,
                  |    "endDateYear": 2049,
                  |    "leaseTerm":  {
                  |      "years": 32,
                  |      "days": 172,
                  |      "daysInPartialYear": 365
                  |     },
                  |    "year1Rent": 10000,
                  |    "year2Rent": 20000,
                  |    "year3Rent": 30000,
                  |    "year4Rent": 40000,
                  |    "year5Rent": 50000
                  |  }
                  |}""".stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              |"result":[
              |  {
              |   "totalTax":37426,
              |   "resultHeading":"Results based on SDLT rules from 1 April 2016",
              |   "npv":867608,
              |   "taxCalcs":[
              |     {
              |      "taxType":"rent",
              |      "calcType":"slice",
              |      "taxDue":7426,
              |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 1 April 2016",
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
              |       "taxDue":7426
              |       }
              |      ]
              |     },
              |    {
              |     "taxType":"premium",
              |     "calcType":"slice",
              |     "taxDue":30000,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 1 April 2016",
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
              |      "taxDue":20000
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
              |    "totalTax":22426,
              |    "resultHeading":"Results based on SDLT rules before 1 April 2016",
              |    "resultHint":"You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015.",
              |    "npv":867608,
              |    "taxCalcs":[
              |    {
              |     "taxType":"rent",
              |     "calcType":"slice",
              |     "taxDue":7426,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules before 1 April 2016",
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
              |      "taxDue":7426
              |      }
              |     ]
              |    },
              |   {
              |     "taxType":"premium",
              |     "calcType":"slice",
              |     "taxDue":15000,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules before 1 April 2016",
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

        "residential, Individual" in {
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Residential",
                  |  "effectiveDateDay": 13,
                  |  "effectiveDateMonth": 7,
                  |  "effectiveDateYear": 2017,
                  |  "premium": 500000,
                  |  "highestRent": 50000,
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
                  |    "endDateMonth": 12,
                  |    "endDateYear": 2049,
                  |    "leaseTerm":  {
                  |      "years": 32,
                  |      "days": 172,
                  |      "daysInPartialYear": 365
                  |     },
                  |    "year1Rent": 10000,
                  |    "year2Rent": 20000,
                  |    "year3Rent": 30000,
                  |    "year4Rent": 40000,
                  |    "year5Rent": 50000
                  |  }
                  |}""".stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              |"result":[
              |  {
              |   "totalTax":37426,
              |   "resultHeading":"Results based on SDLT rules from 1 April 2016",
              |   "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £15,000.",
              |   "npv":867608,
              |   "taxCalcs":[
              |     {
              |      "taxType":"rent",
              |      "calcType":"slice",
              |      "taxDue":7426,
              |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 1 April 2016",
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
              |       "taxDue":7426
              |       }
              |      ]
              |     },
              |    {
              |     "taxType":"premium",
              |     "calcType":"slice",
              |     "taxDue":30000,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 1 April 2016",
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
              |      "taxDue":20000
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
              |    "totalTax":22426,
              |    "resultHeading":"Results based on SDLT rules before 1 April 2016",
              |    "resultHint":"You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015.",
              |    "npv":867608,
              |    "taxCalcs":[
              |    {
              |     "taxType":"rent",
              |     "calcType":"slice",
              |     "taxDue":7426,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules before 1 April 2016",
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
              |      "taxDue":7426
              |      }
              |     ]
              |    },
              |   {
              |     "taxType":"premium",
              |     "calcType":"slice",
              |     "taxDue":15000,
              |     "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules before 1 April 2016",
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

        "residential, Individual && notTwoOrMoreProperties" in {
          def request: WSResponse = ws.url(
            calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Residential",
                  |  "effectiveDateDay": 13,
                  |  "effectiveDateMonth": 7,
                  |  "effectiveDateYear": 2017,
                  |  "premium": 500000,
                  |  "highestRent": 50000,
                  |  "propertyDetails": {
                  |     "individual": "Yes",
                  |     "twoOrMoreProperties": "No",
                  |     "sharedOwnership" : "No"
                  |   },
                  |  "leaseDetails": {
                  |    "startDateDay": 15,
                  |    "startDateMonth": 1,
                  |    "startDateYear": 1949,
                  |    "endDateDay": 31,
                  |    "endDateMonth": 12,
                  |    "endDateYear": 2049,
                  |    "leaseTerm":  {
                  |      "years": 32,
                  |      "days": 172,
                  |      "daysInPartialYear": 365
                  |     },
                  |    "year1Rent": 10000,
                  |    "year2Rent": 20000,
                  |    "year3Rent": 30000,
                  |    "year4Rent": 40000,
                  |    "year5Rent": 50000
                  |  }
                  |}""".stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              |"result":[
              |  {
              |   "totalTax":22426,
              |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |   "npv":867608,
              |   "taxCalcs":[
              |     {
              |      "taxType":"rent",
              |      "calcType":"slice",
              |      "taxDue":7426,
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
              |       "taxDue":7426
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
      }

      "residential, company, JUL 2015" in {
        def request: WSResponse = ws.url(
          calculateUrl)
          .post(
            Json.parse(

              """
                |{
                |  "holdingType": "Leasehold",
                |  "propertyType": "Residential",
                |  "effectiveDateDay": 13,
                |  "effectiveDateMonth": 7,
                |  "effectiveDateYear": 2015,
                |  "premium": 500000,
                |  "highestRent": 50000,
                |  "leaseDetails": {
                |    "startDateDay": 15,
                |    "startDateMonth": 1,
                |    "startDateYear": 1949,
                |    "endDateDay": 31,
                |    "endDateMonth": 12,
                |    "endDateYear": 2049,
                |    "leaseTerm":  {
                |      "years": 34,
                |      "days": 172,
                |      "daysInPartialYear": 365
                |     },
                |    "year1Rent": 10000,
                |    "year2Rent": 20000,
                |    "year3Rent": 30000,
                |    "year4Rent": 40000,
                |    "year5Rent": 50000
                |  }
                |}""".stripMargin)
          )

        val
        responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":22736,
            |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
            |   "npv":898695,
            |   "taxCalcs":[
            |    {
            |     "taxType":"rent",
            |     "calcType":"slice",
            |     "taxDue":7736,
            |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
            |     "bandHeading":"Rent bands (£)",
            |     "detailFooter":"SDLT due on the rent",
            |     "slices":[
            |      {
            |        "from":0,
            |        "to":125000,
            |        "rate":0,
            |        "taxDue":0
            |      },{
            |        "from":125000,
            |        "to":-1,
            |        "rate":1,
            |        "taxDue":7736
            |      }
            |    ]
            |   },
            |   {
            |    "taxType":"premium",
            |    "calcType":"slice",
            |    "taxDue":15000,
            |    "detailHeading":"This is a breakdown of how the amount of SDLT on the premium was calculated",
            |    "bandHeading":"Premium bands (£)",
            |    "detailFooter":"SDLT due on the premium",
            |    "slices":[
            |      {
            |        "from":0,
            |        "to":125000,
            |        "rate":0,
            |        "taxDue":0
            |      },{
            |        "from":125000,
            |        "to":250000,
            |        "rate":2,
            |        "taxDue":2500
            |      },{
            |        "from":250000,
            |        "to":925000,
            |        "rate":5,
            |        "taxDue":12500
            |      },{
            |       "from":925000,
            |       "to":1500000,
            |       "rate":10,
            |       "taxDue":0
            |      },{
            |       "from":1500000,
            |       "to":-1,
            |       "rate":12,"taxDue":0
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

      "non-residential, company, JUL 2014" in {
        def request: WSResponse = ws.url(calculateUrl)
          .post(
            Json.parse(
              """
                |{
                |  "holdingType": "Leasehold",
                |  "propertyType": "Non-residential",
                |  "effectiveDateDay": 13,
                |  "effectiveDateMonth": 7,
                |  "effectiveDateYear": 2014,
                |  "premium": 500000,
                |  "highestRent": 50000,
                |  "leaseDetails": {
                |    "startDateDay": 15,
                |    "startDateMonth": 1,
                |    "startDateYear": 1949,
                |    "endDateDay": 31,
                |    "endDateMonth": 12,
                |    "endDateYear": 2049,
                |    "leaseTerm":  {
                |      "years": 35,
                |      "days": 172,
                |      "daysInPartialYear": 365
                |     },
                |    "year1Rent": 10000,
                |    "year2Rent": 20000,
                |    "year3Rent": 30000,
                |    "year4Rent": 40000,
                |    "year5Rent": 50000
                |  }
                |}
              """.
                stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            | "result":[
            |   {
            |    "totalTax":22634,
            |    "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
            |    "npv":913455,
            |    "taxCalcs":[
            |     {
            |      "taxType":"rent",
            |      "calcType":"slice",
            |      "taxDue":7634,
            |      "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
            |      "bandHeading":"Rent bands (£)",
            |      "detailFooter":"SDLT due on the rent",
            |      "slices":[
            |      {
            |       "from":0,
            |       "to":150000,
            |       "rate":0,
            |       "taxDue":0
            |      },{
            |       "from":150000,
            |       "to":-1,
            |       "rate":1,
            |       "taxDue":7634
            |      }
            |     ]
            |    },{
            |      "taxType":"premium",
            |      "calcType":"slab",
            |      "taxDue":15000,
            |      "rate":3
            |    }
            |   ]
            |  }
            | ]
            |}""".stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "residential, company, JUL 2013" in {
        def request: WSResponse = ws.url(calculateUrl)
          .post(
            Json.parse(
              """
                |{
                |  "holdingType": "Leasehold",
                |  "propertyType": "Residential",
                |  "effectiveDateDay": 13,
                |  "effectiveDateMonth": 7,
                |  "effectiveDateYear": 2013,
                |  "premium": 500000,
                |  "highestRent": 50000,
                |  "leaseDetails": {
                |    "startDateDay": 15,
                |    "startDateMonth": 1,
                |    "startDateYear": 1949,
                |    "endDateDay": 31,
                |    "endDateMonth": 12,
                |    "endDateYear": 2049,
                |    "leaseTerm":  {
                |      "years": 36,
                |      "days": 172,
                |      "daysInPartialYear": 365
                |     },
                |    "year1Rent": 10000,
                |    "year2Rent": 20000,
                |    "year3Rent": 30000,
                |    "year4Rent": 40000,
                |    "year5Rent": 50000
                |  }
                |}
              """.
                stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":23027,
            |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
            |   "npv":927716,
            |   "taxCalcs":[
            |    {
            |     "taxType":"rent",
            |     "calcType":"slice",
            |     "taxDue":8027,
            |     "detailHeading":"This is a breakdown of how the amount of SDLT on the rent was calculated",
            |     "bandHeading":"Rent bands (£)",
            |     "detailFooter":"SDLT due on the rent",
            |     "slices":[
            |      {
            |       "from":0,
            |       "to":125000,
            |       "rate":0,
            |       "taxDue":0
            |      },{
            |       "from":125000,
            |       "to":-1,
            |       "rate":1,
            |       "taxDue":8027
            |      }
            |     ]
            |    },
            |   {
            |    "taxType":"premium",
            |    "calcType":"slab",
            |    "taxDue":15000,
            |    "rate":3
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
