/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package freehold

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSResponse}

class CalculationControllerFreeholdBeforeApril21ISpec extends UnitSpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {

    "return a 200 and valid result for freehold property type" when {

      "the effective date is in 2017" when {
        "the property is an individual" when {

          "residential, notTwoOrMoreProperties" in {
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
                    |}""".stripMargin))

            val responseJson = Json.parse(
              """
                |{
                | "result":[
                |  {
                |  "totalTax":15000,
                |  "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
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
                |}""".stripMargin)

            request.status shouldBe OK
            request.json shouldBe responseJson
          }

          "residential, notTwoOrMoreProperties, FTB" in {
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
                    |}""".stripMargin
                )
              )

            val responseJson = Json.parse(
              """
                |{
                | "result":[
                |  {
                |   "totalTax":10000,
                |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
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
                |}""".stripMargin)

            request.status shouldBe OK
            request.json shouldBe responseJson
          }
        }

        "the purchaser is a company" when {

          "residential" in {
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
                    |   "individual": "No"
                    | }
                    |}""".stripMargin))

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
                |}""".stripMargin)

            request.status shouldBe OK
            request.json shouldBe responseJson
          }

          "non-residential" in{
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
                    |   }
                    |}""".stripMargin))

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
                |}""".stripMargin)

            request.status shouldBe OK
            request.json shouldBe responseJson
          }
        }

      }

      "residential, company, 2015" in{
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
                |}""".stripMargin)
          )

        val responseJson = Json.parse(
          """
            |{
            | "result":[
            |  {
            |   "totalTax":15000,
            |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
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
            | }""".stripMargin)

        request.status shouldBe OK
        request.json shouldBe responseJson
      }

      "the effective date is 2013" when {
        "residential, notIndividual" in{
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
                  |}""".
                  stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              | "result":[
              |  {
              |   "totalTax":15000,
              |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
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
              |}""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson

        }

        "non-residential, notIndividual, 2013" in{
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
                  |}""".
                  stripMargin)
            )

          val responseJson = Json.parse(
            """
              |{
              | "result":[
              |  {
              |   "resultHeading":"Results of calculation based on SDLT rules for the effective date entered",
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
              |}""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe responseJson

        }
      }

    }
  }
}
