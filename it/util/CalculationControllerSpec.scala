/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package util

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSResponse}

class CalculationControllerSpec extends UnitSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 and valid result for leasehold" when {
      "residential, notIndividual, 2012-2014" in {
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
            |}
          """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "residential, notIndividual, 2014-2016" in {
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
                |}
              """.stripMargin)
          )

        val
        responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":22736,
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
            |}
          """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "residential, notIndividual, 2016+" in{
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
                |     "individual": "No",
                |     "twoOrMoreProperties": "No",
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
                |}
              """. stripMargin)
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
            |}
          """.stripMargin)
        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "residential, Individual, 2016+" in{
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
                |}
              """. stripMargin)
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
            |}
          """.stripMargin)
        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "residential, Individual && notTwoOrMoreProperties, 2016+" in{
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
                |}
              """. stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            |"result":[
            |  {
            |   "totalTax":22426,
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
            |}
          """.stripMargin)
        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "residential, Individual && notTwoOrMoreProperties, FTB, NOV2017+" in{
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
                |}
              """. stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":17367,
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
            |}
          """.stripMargin)
        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "non-residential, notIndividual, 2012-2016" in {
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
            |}
          """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "non-residential, notIndividual, 2016+" in {
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
                |}
              """.stripMargin)
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
            |}
          """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "residential, Individual && notTwoOrMoreProperties, FTB, NOV2017+, Shared" in{
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
                |}
              """. stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":750,
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
            |}
          """.stripMargin)
        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "residential, Individual && notTwoOrMoreProperties, FTB, NOV2017+, Shared (NPV proof) " in{
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
                |}
              """. stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":0,
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
            |}
          """.stripMargin)
        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "the effective date is 2021+" when{

        "residential, Non UK Resident, Individual (Business Threads Scenario 9)" in{
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
              |   "totalTax":91886,
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
              |    "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT.",
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
              |}
          """.stripMargin)
          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Non UK Resident, Individual, Shared Ownership (Business Threads Scenario 10)" in{
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
                  |  "firstTimeBuyer": "Yes",
                  |  "propertyDetails": {
                  |     "individual": "Yes",
                  |     "twoOrMoreProperties": "No",
                  |     "sharedOwnership": "Yes",
                  |     "currentValue": "Yes"
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
              |   "totalTax":6000,
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
              |    "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT.",
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
              |}
          """.stripMargin)
          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Non UK Resident, Individual, one year rent input (Business Threads Scenario 11)" in{
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
              """. stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              |"result":[
              |  {
              |   "totalTax":26913,
              |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
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
              |       "to":125000,
              |       "rate":2,
              |       "taxDue":1913
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
              |     "taxDue":25000,
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
              |      "taxDue":17500
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
              |    "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT.",
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
              |}
          """.stripMargin)
          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Non UK Resident, Individual, 300K Premium (Business Threads Scenario 12)" in{
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
              """. stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              |"result":[
              |  {
              |   "totalTax":91886,
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
              |    "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT.",
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
              |}
          """.stripMargin)
          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Non UK Resident, Individual, Additional Property (Business Threads Scenario 13)" in{
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
              """. stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              |"result":[
              |  {
              |   "totalTax":98386,
              |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £8,250.",
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
              |     "taxDue":17500,
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
              |  "totalTax":90136,
              |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
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
              |      "taxDue":78386}
              |      ]
              |     },{
              |      "taxType":"premium",
              |      "calcType":"slice",
              |      "taxDue":9250,
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
              |        "to":925000,"rate":7,
              |        "taxDue":1750},{
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

        "non residential, notIndividual, Additional Property (Business Threads Scenario 14)" in{
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
      }
    }

    "return a 200 and valid result for freehold" when {
      "residential, notIndividual, 2012-2014" in{
        def request: WSResponse = ws.url(
          calculateUrl)
          .post(
            Json.parse(
              """
                |{
                |  "holdingType": "Freehold",
                |  "propertyType": "Residential",
                |  "effectiveDateDay": 13,
                |  "effectiveDateMonth": 7,
                |  "effectiveDateYear": 2013,
                |  "premium": 500000,
                |  "highestRent": 0
                |}
              """.
                stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":15000,
            |   "taxCalcs":[
            |   {
            |    "taxType":"premium",
            |    "calcType":"slab",
            |    "taxDue":15000,
            |    "rate":3
            |    }
            |   ]
            |  }
            | ]
            |}
          """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson

      }

      "residential, notIndividual, 2014-2016" in{
        def request: WSResponse = ws.url(
          calculateUrl)
          .post(
            Json.parse(
              """
                |{
                |  "holdingType": "Freehold",
                |  "propertyType": "Residential",
                |  "effectiveDateDay": 13,
                |  "effectiveDateMonth": 7,
                |  "effectiveDateYear": 2015,
                |  "premium": 500000,
                |  "highestRent": 0
                |}
              """.
                stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":15000,
            |   "taxCalcs":[
            |    {
            |     "taxType":"premium",
            |     "calcType":"slice",
            |     "taxDue":15000,
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
            |       "taxDue":12500
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
            |      ]
            |     }
            |    ]
            |   }
            |  ]
            | }
          """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "residential, notIndividual, 2016+" in{
        def request: WSResponse = ws.url(
          calculateUrl)
          .post(
            Json.parse(
              """
                |{
                |  "holdingType": "Freehold",
                |  "propertyType": "Residential",
                |  "effectiveDateDay": 13,
                |  "effectiveDateMonth": 7,
                |  "effectiveDateYear": 2017,
                |  "premium": 500000,
                |  "highestRent": 0,
                |  "propertyDetails": {
                |   "individual": "No",
                |   "twoOrMoreProperties": "No",
                |   "replaceMainResidence": "No"
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
            |   "totalTax":30000,
            |   "resultHeading":"Results based on SDLT rules from 1 April 2016",
            |   "taxCalcs":[
            |    {
            |     "taxType":"premium",
            |     "calcType":"slice",
            |     "taxDue":30000,
            |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated based on the rules from 1 April 2016",
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
            |  },
            | {
            |  "totalTax":15000,
            |  "resultHeading":"Results based on SDLT rules before 1 April 2016",
            |  "resultHint":"You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015.",
            |  "taxCalcs":[
            |   {
            |    "taxType":"premium",
            |    "calcType":"slice",
            |    "taxDue":15000,
            |    "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated based on the rules before 1 April 2016",
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
            |}
          """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "residential, Individual && notTwoOrMoreProperties, 2016+" in{
        def request: WSResponse = ws.url(
          calculateUrl)
          .post(
            Json.parse(
              """
                |{
                |  "holdingType": "Freehold",
                |  "propertyType": "Residential",
                |  "effectiveDateDay": 13,
                |  "effectiveDateMonth": 7,
                |  "effectiveDateYear": 2017,
                |  "premium": 500000,
                |  "highestRent": 0,
                |  "propertyDetails": {
                |   "individual": "Yes",
                |   "twoOrMoreProperties": "No",
                |   "replaceMainResidence": "No"
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
            |  "totalTax":15000,
            |  "taxCalcs":[
            |   {
            |    "taxType":"premium",
            |    "calcType":"slice",
            |    "taxDue":15000,
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
            |}
          """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "residential, Individual && notTwoOrMoreProperties, FTB, NOV2017+" in{
        def request: WSResponse = ws.url(
          calculateUrl)
          .post(
            Json.parse(
              """
                |{
                |  "holdingType": "Freehold",
                |  "propertyType": "Residential",
                |  "effectiveDateDay": 23,
                |  "effectiveDateMonth": 11,
                |  "effectiveDateYear": 2017,
                |  "premium": 500000,
                |  "highestRent": 0,
                |  "propertyDetails": {
                |   "individual": "Yes",
                |   "twoOrMoreProperties": "No",
                |   "replaceMainResidence": "Yes"
                | },
                | "firstTimeBuyer": "Yes"
                |}
              """.
                stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":10000,
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
            |}
          """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "non-residential, notIndividual, 2012-2016" in{
        def request: WSResponse = ws.url(
          calculateUrl)
          .post(
            Json.parse(
              """
                |{
                |  "holdingType": "Freehold",
                |  "propertyType": "Non-residential",
                |  "effectiveDateDay": 13,
                |  "effectiveDateMonth": 7,
                |  "effectiveDateYear": 2013,
                |  "premium": 500000,
                |  "highestRent": 0
                |}
              """.
                stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":15000,
            |   "taxCalcs":[
            |   {
            |    "taxType":"premium",
            |    "calcType":"slab",
            |    "taxDue":15000,
            |    "rate":3
            |    }
            |   ]
            |  }
            | ]
            |}
          """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson

      }

      "non-residential, notIndividual, 2016+" in{
        def request: WSResponse = ws.url(
          calculateUrl)
          .post(
            Json.parse(
              """
                |{
                |  "holdingType": "Freehold",
                |  "propertyType": "Non-residential",
                |  "effectiveDateDay": 13,
                |  "effectiveDateMonth": 7,
                |  "effectiveDateYear": 2017,
                |  "premium": 500000,
                |  "highestRent": 0,
                |  "propertyDetails": {
                |   "individual": "Yes",
                |   "twoOrMoreProperties": "Yes",
                |   "replaceMainResidence": "No"
                | }
                |}
              """.stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":14500,
            |   "resultHeading":"Results based on SDLT rules from 17 March 2016",
            |   "taxCalcs":[
            |    {
            |     "taxType":"premium",
            |     "calcType":"slice",
            |     "taxDue":14500,
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
            |       "taxDue":12500
            |       }
            |      ]
            |     }
            |    ]
            |   },
            |  {
            |   "totalTax":15000,
            |   "resultHeading":"Results based on SDLT rules before 17 March 2016",
            |   "resultHint":"You may be entitled to pay SDLT using the old rules if you exchanged contracts before 17 March 2016.",
            |   "taxCalcs":[
            |    {
            |     "taxType":"premium",
            |     "calcType":"slab",
            |     "taxDue":15000,
            |     "rate":3
            |    }
            |   ]
            |  }
            | ]
            |}
          """.stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "the effective date is 2021+" when{
        "residential, notIndividual" in{
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
                  |  "premium": 500000,
                  |  "highestRent": 0,
                  |  "propertyDetails": {
                  |   "individual": "No",
                  |   "twoOrMoreProperties": "No",
                  |   "replaceMainResidence": "No"
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "non-residential, notIndividual (Business Threads Scenario 1)" in{
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
                  |}
              """.stripMargin)
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Individual, Non UK Resident 100K premium (Business Threads Scenario 2)" in{
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
              |  "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT.",
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Individual, Non UK Resident £1.6M premium (Business Threads Scenario 3)" in{
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
              |  "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT.",
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, notIndividual, Non UK Resident, Additional Property (Business Threads Scenario 4)" in{
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
                  |   "twoOrMoreProperties": "Yes",
                  |   "replaceMainResidence": "No"
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Individual, Non UK Resident, Additional Property (Business Threads Scenario 5)" in{
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
                  |}
              """.
                  stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              | "result":[
              |  {
              |   "totalTax":35000,
              |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |   "resultHint":"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. If you dispose of your previous main residence within 3 years you may be eligible for a refund of £13,500.",
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Individual, Non UK Resident, FTB, 250K premium (Business Threads Scenario 6)" in{
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
              |  "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT.",
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Individual, Non UK Resident, FTB, 350K premium (Business Threads Scenario 7)" in{
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
                  |  "premium": 350000,
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
              |   "totalTax":9500,
              |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
              |   "taxCalcs":[
              |    {
              |     "taxType":"premium",
              |     "calcType":"slice",
              |     "taxDue":9500,
              |     "detailHeading":"This is a breakdown of how the total amount of SDLT was calculated",
              |     "bandHeading":"Purchase price bands (£)",
              |     "detailFooter":"Total SDLT due",
              |     "slices":[
              |      {
              |       "from":0,
              |       "to":300000,
              |       "rate":2,
              |       "taxDue":6000
              |       },{
              |       "from":300000,
              |       "to":500000,
              |       "rate":7,
              |       "taxDue":3500
              |       }
              |     ]
              |    }
              |   ]
              |  },
              | {
              |  "totalTax":2500,
              |  "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT.",
              |  "taxCalcs":[
              |   {
              |    "taxType":"premium",
              |    "calcType":"slice",
              |    "taxDue":2500,
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
              |      "taxDue":2500
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

        "residential, Individual, Non UK Resident, 250K premium, 2021+ (Business Threads Scenario 8)" in{
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
                  |  "premium": 250000,
                  |  "highestRent": 0,
                  |  "firstTimeBuyer": "No",
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
              |   "totalTax":7500,
              |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
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
              |  "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT.",
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

      }
    }
  }
}
