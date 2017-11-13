package component

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import play.api.http.Status._
import play.api.libs.ws.{WSClient, WSResponse}


class ComponentTestSpec extends UnitSpec with GuiceOneAppPerSuite {
  lazy val ws = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 and valid result for leasehold" when {
      "residential, notIndividual, 2012-2014" in {
        def request: WSResponse = ws.url("http://localhost/calculate-stamp-duty-land-tax/calculate")
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
        "http://localhost/calculate-stamp-duty-land-tax/calculate")
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
          "http://localhost/calculate-stamp-duty-land-tax/calculate")
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
      "http://localhost/calculate-stamp-duty-land-tax/calculate")
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
          "http://localhost/calculate-stamp-duty-land-tax/calculate")
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

      "non-residential, notIndividual, 2012-2016" in {
      def request: WSResponse = ws.url("http://localhost/calculate-stamp-duty-land-tax/calculate")
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
        "http://localhost/calculate-stamp-duty-land-tax/calculate")
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
    }

    "return a 200 and valid result for freehold" when {
      "residential, notIndividual, 2012-2014" in{
      def request: WSResponse = ws.url(
        "http://localhost/calculate-stamp-duty-land-tax/calculate")
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
          "http://localhost/calculate-stamp-duty-land-tax/calculate")
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
          "http://localhost/calculate-stamp-duty-land-tax/calculate")
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

      "residential, Individual, 2016+" in{
        def request: WSResponse = ws.url(
          "http://localhost/calculate-stamp-duty-land-tax/calculate")
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
            |   "totalTax":30000,
            |   "resultHeading":"Results based on SDLT rules from 1 April 2016",
            |   "resultHint":"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £15,000.",
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
          "http://localhost/calculate-stamp-duty-land-tax/calculate")
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

      "non-residential, notIndividual, 2012-2016" in{
        def request: WSResponse = ws.url(
          "http://localhost/calculate-stamp-duty-land-tax/calculate")
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
          "http://localhost/calculate-stamp-duty-land-tax/calculate")
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
    }
  }
}