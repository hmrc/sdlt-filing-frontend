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

class CalculationControllerFreeholdSpec extends UnitSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {

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

        "residential, Individual, Non UK Resident 100K premium (Business Threads Scenario 2a)" in{
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
              |   "resultHint":"The results are based on the answers you have provided and show that the non-UK residential rate applies",
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
              |  "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT",
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, notIndividual, Non UK Resident 100K premium (Business Threads Scenario 2b)" in{
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
                  |   "individual": "No",
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
              |   "resultHint":"The results are based on the answers you have provided and show that the non-UK residential rate applies",
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
              |  "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT",
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
              |   "resultHint":"The results are based on the answers you have provided and show that the non-UK residential rate applies",
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
              |  "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT",
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Individual, Non UK Resident, FTB, <40K premium" in{
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
                  |  "premium": 20000,
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Individual, Non UK Resident, 250K premium (Business Threads Scenario 8)" in{
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
              |   "resultHint":"The results are based on the answers you have provided and show that the non-UK residential rate applies",
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
              |  "resultHeading":"Result if you become eligible for a repayment of the Non-resident rate of SDLT",
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
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Individual, Non UK Resident, Additional Property (Business Threads Scenario 20a)" in{
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
                  |  "premium": 30000,
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
              |       "rate":5,
              |       "taxDue":0
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
              |  },
              | {
              |  "totalTax":0,
              |  "resultHeading":"Result if you become eligible for a repayment of the higher rate on additional dwellings",
              |  "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund. You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.",
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
              |  }
              | ]
              |}
          """.stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson
        }

        "residential, Individual, UK Resident, Additional Property (Business Threads Scenario 20b)" in{
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
                  |  "premium": 30000,
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
              |       "rate":3,
              |       "taxDue":0
              |       },{
              |       "from":125000,
              |       "to":250000,
              |       "rate":5,
              |       "taxDue":0
              |       },{
              |       "from":250000,
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

      }
    }
  }
}
