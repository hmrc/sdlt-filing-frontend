/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package test.leasehold

import base.ResponseHelper
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import test.base.BaseSpec

class CalculationControllerLeaseholdNoTaxReliefISpec extends BaseSpec with GuiceOneServerPerSuite with ResponseHelper {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  lazy val calculateUrl = s"http://localhost:$port/calculate-stamp-duty-land-tax/calculate"

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  "Hitting the /calculate route" should {
    "return a 200 and valid result for leasehold property type" when {

      // SDLT - Tax Calc Case - 59 - Self Assessed
      "transaction is linked " when {
        "date is on or after 22nd November 2017 " must {
          "return the zero rate response " when {
            "transaction is Residential " in {
              def request: WSResponse = ws.url(
                  calculateUrl)
                .post(
                  Json.parse(
                    """
                      {
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 22,
                      |  "effectiveDateMonth": 11,
                      |  "effectiveDateYear": 2017,
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "leaseDetails": {
                      |    "startDateDay": 22,
                      |    "startDateMonth": 11,
                      |    "startDateYear": 2017,
                      |    "endDateDay": 22,
                      |    "endDateMonth": 11,
                      |    "endDateYear": 2018,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999
                      |  },
                      |  "propertyDetails": {
                      |   "individual": "No",
                      |   "twoOrMoreProperties": "No",
                      |   "replaceMainResidence": "No"
                      | },
                      |    "isLinked": true
                      |}
                      |""".stripMargin
                  )
                )

              request.status shouldBe OK
              request.json shouldBe selfAssessedResponse
            }
            "transaction is ResidentialAdditionalProperty " in {
              def request: WSResponse = ws.url(
                  calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Residential",
                      |  "effectiveDateDay": 22,
                      |  "effectiveDateMonth": 11,
                      |  "effectiveDateYear": 2017,
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "leaseDetails": {
                      |    "startDateDay": 22,
                      |    "startDateMonth": 11,
                      |    "startDateYear": 2017,
                      |    "endDateDay": 22,
                      |    "endDateMonth": 11,
                      |    "endDateYear": 2018,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999
                      |  },
                      |  "propertyDetails": {
                      |   "individual": "Yes",
                      |    "twoOrMoreProperties": "Yes",
                      |    "replaceMainResidence": "No"
                      | },
                      |    "isLinked": true
                      |}
                      |""".stripMargin
                  )
                )

              request.status shouldBe OK
              request.json shouldBe selfAssessedResponse
            }
            "transaction is Non-residential " in {
              def request: WSResponse = ws.url(
                  calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Non-residential",
                      |  "effectiveDateDay": 22,
                      |  "effectiveDateMonth": 11,
                      |  "effectiveDateYear": 2017,
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "leaseDetails": {
                      |    "startDateDay": 22,
                      |    "startDateMonth": 11,
                      |    "startDateYear": 2017,
                      |    "endDateDay": 22,
                      |    "endDateMonth": 11,
                      |    "endDateYear": 2018,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999
                      |  },
                      |  "propertyDetails": {
                      |   "individual": "No",
                      |   "twoOrMoreProperties": "No",
                      |   "replaceMainResidence": "No"
                      | },
                      |    "isLinked": true
                      |}
                      |""".stripMargin
                  )
                )

              request.status shouldBe OK
              request.json shouldBe selfAssessedResponse
            }
            "transaction is Mixed " in {
              def request: WSResponse = ws.url(
                  calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Mixed",
                      |  "effectiveDateDay": 22,
                      |  "effectiveDateMonth": 11,
                      |  "effectiveDateYear": 2017,
                      |  "premium": 1000000,
                      |  "highestRent": 0,
                      |  "leaseDetails": {
                      |    "startDateDay": 22,
                      |    "startDateMonth": 11,
                      |    "startDateYear": 2017,
                      |    "endDateDay": 22,
                      |    "endDateMonth": 11,
                      |    "endDateYear": 2018,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 999,
                      |    "year2Rent": 999
                      |  },
                      |  "propertyDetails": {
                      |   "individual": "No",
                      |   "twoOrMoreProperties": "No",
                      |   "replaceMainResidence": "No"
                      | },
                      |    "isLinked": true
                      |}
                      |""".stripMargin
                  )
                )

              request.status shouldBe OK
              request.json shouldBe selfAssessedResponse
            }


          }
        }
      }


      // SDLT - Tax Calc Case - 19 - Self Assessed
      "transaction is linked" when {
        "date is on or after 22nd November 2017" must {
          "return the zero rate response" in {
            def request: WSResponse = ws.url(
                calculateUrl)
              .post(
                Json.parse(
                  """
                    |{
                    |  "holdingType": "Leasehold",
                    |  "propertyType": "Residential",
                    |  "effectiveDateDay": 22,
                    |  "effectiveDateMonth": 11,
                    |  "effectiveDateYear": 2017,
                    |  "premium": 1000000,
                    |  "highestRent": 0,
                    |  "leaseDetails": {
                    |    "startDateDay": 22,
                    |    "startDateMonth": 11,
                    |    "startDateYear": 2017,
                    |    "endDateDay": 22,
                    |    "endDateMonth": 11,
                    |    "endDateYear": 2018,
                    |    "leaseTerm": {
                    |      "years": 1,
                    |      "days": 1,
                    |      "daysInPartialYear": 365
                    |    },
                    |    "year1Rent": 999,
                    |    "year2Rent": 999
                    |  },
                    |  "propertyDetails": {
                    |   "individual": "No",
                    |   "twoOrMoreProperties": "No",
                    |   "replaceMainResidence": "No"
                    | },
                    |    "isLinked": true
                    |}
                    |""".stripMargin
                )
              )

            request.status shouldBe OK
            request.json shouldBe selfAssessedResponse
          }
        }

      }

      "NPV is supplied in the Request" must {
        "return a calculation result with the supplied NPV" in {
          def request: WSResponse = ws.url(
              calculateUrl)
            .post(
              Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Residential",
                  |  "effectiveDateDay": 22,
                  |  "effectiveDateMonth": 11,
                  |  "effectiveDateYear": 2012,
                  |  "premium": 1000000,
                  |  "highestRent": 0,
                  |  "leaseDetails": {
                  |    "startDateDay": 22,
                  |    "startDateMonth": 11,
                  |    "startDateYear": 2012,
                  |    "endDateDay": 22,
                  |    "endDateMonth": 11,
                  |    "endDateYear": 2013,
                  |    "leaseTerm": {
                  |      "years": 1,
                  |      "days": 1,
                  |      "daysInPartialYear": 365
                  |    },
                  |    "year1Rent": 999,
                  |    "year2Rent": 999
                  |  },
                  |  "declaredNpv": 150900,
                  |  "propertyDetails": {
                  |   "individual": "No",
                  |   "twoOrMoreProperties": "No",
                  |   "replaceMainResidence": "No"
                  | },
                  |    "isLinked": true
                  |}
                  |""".stripMargin
              )
            )

          val result = Json.parse(
            """
              |{
              |  "result": [
              |    {
              |      "totalTax": 40259,
              |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
              |      "npv": 150900,
              |      "taxCalcs": [
              |        {
              |          "taxType": "rent",
              |          "calcType": "slice",
              |          "taxDue": 259,
              |          "detailHeading": "This is a breakdown of how the amount of SDLT on the rent was calculated",
              |          "bandHeading": "Rent bands (£)",
              |          "detailFooter": "SDLT due on the rent",
              |          "slices": [
              |            {
              |              "from": 0,
              |              "to": 125000,
              |              "rate": 0,
              |              "taxDue": 0
              |            },
              |            {
              |              "from": 125000,
              |              "to": -1,
              |              "rate": 1,
              |              "taxDue": 259
              |            }
              |          ]
              |        },
              |        {
              |          "taxType": "premium",
              |          "calcType": "slab",
              |          "taxDue": 40000,
              |          "rate": 4
              |        }
              |      ]
              |    }
              |  ]
              |}""".stripMargin)

          request.status shouldBe OK
          request.json shouldBe result
        }
      }

      // SDLT - Tax Calc Case - 2016 budget no relief leased - Add Mixed Logic
      "transaction is not linked" when {
        "date is on or after 17th March 2016" when {
          "property type is Non-residential" must {
            "return 0 lease tax and 0 premium tax when npv is 280 and premium is 100000" in {
              def request: WSResponse = ws.url(calculateUrl).post(Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Non-residential",
                  |  "effectiveDateDay": 17,
                  |  "effectiveDateMonth": 3,
                  |  "effectiveDateYear": 2016,
                  |  "premium": 100000,
                  |  "highestRent": 0,
                  |  "leaseDetails": {
                  |    "startDateDay": 17,
                  |    "startDateMonth": 3,
                  |    "startDateYear": 2016,
                  |    "endDateDay": 16,
                  |    "endDateMonth": 3,
                  |    "endDateYear": 2019,
                  |    "leaseTerm": {
                  |      "years": 3,
                  |      "days": 0,
                  |      "daysInPartialYear": 0
                  |    },
                  |    "year1Rent": 100,
                  |    "year2Rent": 100,
                  |    "year3Rent": 100
                  |  },
                  |  "isLinked": false,
                  |  "relevantRentDetails": {
                  |    "contractPre201603": "Yes",
                  |    "contractVariedPost201603": "No",
                  |    "relevantRent": 0
                  |  }
                  |}
                  |""".stripMargin))

              request.status shouldBe OK
              request.json shouldBe Json.parse(
                """
                  {
                  |  "result": [
                  |    {
                  |      "totalTax": 0,
                  |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                  |      "npv": 280,
                  |      "taxCalcs": [
                  |        {
                  |          "taxType": "rent",
                  |          "calcType": "slice",
                  |          "taxDue": 0,
                  |          "detailHeading": "Results based on SDLT rules from 17 March 2016",
                  |          "bandHeading": "Rent bands (£)",
                  |          "detailFooter": "SDLT due on the rent",
                  |          "slices": [
                  |            {
                  |              "from": 0,
                  |              "to": 150000,
                  |              "rate": 0,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 150000,
                  |              "to": 5000000,
                  |              "rate": 1,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 5000000,
                  |              "to": -1,
                  |              "rate": 2,
                  |              "taxDue": 0
                  |            }
                  |          ]
                  |        },
                  |        {
                  |          "taxType": "premium",
                  |          "calcType": "slice",
                  |          "taxDue": 0,
                  |          "detailHeading": "Results based on SDLT rules from 17 March 2016",
                  |          "bandHeading": "Premium bands (£)",
                  |          "detailFooter": "SDLT due on the premium",
                  |          "slices": [
                  |            {
                  |              "from": 0,
                  |              "to": 150000,
                  |              "rate": 0,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 150000,
                  |              "to": 250000,
                  |              "rate": 2,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 250000,
                  |              "to": -1,
                  |              "rate": 5,
                  |              "taxDue": 0
                  |            }
                  |          ]
                  |        }
                  |      ]
                  |    }
                  |  ]
                  |}
                  |""".stripMargin
              )
            }
          }
          "property type is Mixed" must {
            "return 0 lease tax and 0 premium tax when npv is 280 and premium is 100000" in {
              def request: WSResponse = ws.url(calculateUrl).post(Json.parse(
                """
                  |{
                  |  "holdingType": "Leasehold",
                  |  "propertyType": "Mixed",
                  |  "effectiveDateDay": 17,
                  |  "effectiveDateMonth": 3,
                  |  "effectiveDateYear": 2016,
                  |  "premium": 100000,
                  |  "highestRent": 0,
                  |  "leaseDetails": {
                  |    "startDateDay": 17,
                  |    "startDateMonth": 3,
                  |    "startDateYear": 2016,
                  |    "endDateDay": 16,
                  |    "endDateMonth": 3,
                  |    "endDateYear": 2019,
                  |    "leaseTerm": {
                  |      "years": 3,
                  |      "days": 0,
                  |      "daysInPartialYear": 0
                  |    },
                  |    "year1Rent": 100,
                  |    "year2Rent": 100,
                  |    "year3Rent": 100
                  |  },
                  |  "isLinked": false,
                  |  "relevantRentDetails": {
                  |    "contractPre201603": "Yes",
                  |    "contractVariedPost201603": "No",
                  |    "relevantRent": 0
                  |  }
                  |}
                  |""".stripMargin))

              request.status shouldBe OK
              request.json shouldBe Json.parse(
                """
                  {
                  |  "result": [
                  |    {
                  |      "totalTax": 0,
                  |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                  |      "npv": 280,
                  |      "taxCalcs": [
                  |        {
                  |          "taxType": "rent",
                  |          "calcType": "slice",
                  |          "taxDue": 0,
                  |          "detailHeading": "Results based on SDLT rules from 17 March 2016",
                  |          "bandHeading": "Rent bands (£)",
                  |          "detailFooter": "SDLT due on the rent",
                  |          "slices": [
                  |            {
                  |              "from": 0,
                  |              "to": 150000,
                  |              "rate": 0,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 150000,
                  |              "to": 5000000,
                  |              "rate": 1,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 5000000,
                  |              "to": -1,
                  |              "rate": 2,
                  |              "taxDue": 0
                  |            }
                  |          ]
                  |        },
                  |        {
                  |          "taxType": "premium",
                  |          "calcType": "slice",
                  |          "taxDue": 0,
                  |          "detailHeading": "Results based on SDLT rules from 17 March 2016",
                  |          "bandHeading": "Premium bands (£)",
                  |          "detailFooter": "SDLT due on the premium",
                  |          "slices": [
                  |            {
                  |              "from": 0,
                  |              "to": 150000,
                  |              "rate": 0,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 150000,
                  |              "to": 250000,
                  |              "rate": 2,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 250000,
                  |              "to": -1,
                  |              "rate": 5,
                  |              "taxDue": 0
                  |            }
                  |          ]
                  |        }
                  |      ]
                  |    }
                  |  ]
                  |}
                  |""".stripMargin
              )
            }
          }
        }
      }

      // SDLT - Tax Calc Case - 11 - Add Mixed Logic
      "transaction is not linked and relevantRent >= 1000" when {
        "date is before 12th March 2008" must {
          "return the correct response" when {
            "property type is Mixed and premium is £0" in {
              def request: WSResponse = ws.url(
                  calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Mixed",
                      |  "effectiveDateDay": 11,
                      |  "effectiveDateMonth": 3,
                      |  "effectiveDateYear": 2008,
                      |  "premium": 0,
                      |  "highestRent": 0,
                      |  "leaseDetails": {
                      |    "startDateDay": 11,
                      |    "startDateMonth": 3,
                      |    "startDateYear": 2008,
                      |    "endDateDay": 11,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2009,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 1000,
                      |    "year2Rent": 1000
                      |  },
                      |    "isLinked": false,
                      |    "relevantRentDetails": {
                      |    "relevantRent": 1000
                      |    }
                      |}
                      |""".stripMargin
                  )
                )

              lazy val expectedResponse: JsValue = Json.parse(
                """
                  |{
                  |  "result": [
                  |    {
                  |      "totalTax": 0,
                  |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                  |      "npv": 1899,
                  |      "taxCalcs": [
                  |        {
                  |          "taxType": "rent",
                  |          "calcType": "slice",
                  |          "taxDue": 0,
                  |          "slices": [
                  |            {
                  |              "from": 0,
                  |              "to": 150000,
                  |              "rate": 0,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 150000,
                  |              "to": -1,
                  |              "rate": 1,
                  |              "taxDue": 0
                  |            }
                  |          ]
                  |        },
                  |        {
                  |          "taxType": "premium",
                  |          "calcType": "slab",
                  |          "taxDue": 0,
                  |          "rate": 0
                  |        }
                  |      ]
                  |    }
                  |  ]
                  |}
                  |""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe expectedResponse
            }

            "property type is Mixed and premium is £250000" in {
              def request: WSResponse = ws.url(
                  calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Mixed",
                      |  "effectiveDateDay": 11,
                      |  "effectiveDateMonth": 3,
                      |  "effectiveDateYear": 2008,
                      |  "premium": 250000,
                      |  "highestRent": 0,
                      |  "leaseDetails": {
                      |    "startDateDay": 11,
                      |    "startDateMonth": 3,
                      |    "startDateYear": 2008,
                      |    "endDateDay": 11,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2009,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 1000,
                      |    "year2Rent": 1000
                      |  },
                      |    "isLinked": false,
                      |    "relevantRentDetails": {
                      |    "relevantRent": 1000
                      |    }
                      |}
                      |""".stripMargin
                  )
                )

              lazy val expectedResponse: JsValue = Json.parse(
                """
                  |{
                  |  "result": [
                  |    {
                  |      "totalTax": 2500,
                  |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                  |      "npv": 1899,
                  |      "taxCalcs": [
                  |        {
                  |          "taxType": "rent",
                  |          "calcType": "slice",
                  |          "taxDue": 0,
                  |          "slices": [
                  |            {
                  |              "from": 0,
                  |              "to": 150000,
                  |              "rate": 0,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 150000,
                  |              "to": -1,
                  |              "rate": 1,
                  |              "taxDue": 0
                  |            }
                  |          ]
                  |        },
                  |        {
                  |          "taxType": "premium",
                  |          "calcType": "slab",
                  |          "taxDue": 2500,
                  |          "rate": 1
                  |        }
                  |      ]
                  |    }
                  |  ]
                  |}
                  |""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe expectedResponse
            }

            "property type is Mixed and premium is £250001" in {
              def request: WSResponse = ws.url(
                  calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Mixed",
                      |  "effectiveDateDay": 11,
                      |  "effectiveDateMonth": 3,
                      |  "effectiveDateYear": 2008,
                      |  "premium": 250001,
                      |  "highestRent": 0,
                      |  "leaseDetails": {
                      |    "startDateDay": 11,
                      |    "startDateMonth": 3,
                      |    "startDateYear": 2008,
                      |    "endDateDay": 11,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2009,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 1000,
                      |    "year2Rent": 1000
                      |  },
                      |    "isLinked": false,
                      |    "relevantRentDetails": {
                      |    "relevantRent": 1000
                      |    }
                      |}
                      |""".stripMargin
                  )
                )

              lazy val expectedResponse: JsValue = Json.parse(
                """
                  |{
                  |  "result": [
                  |    {
                  |      "totalTax": 7500,
                  |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                  |      "npv": 1899,
                  |      "taxCalcs": [
                  |        {
                  |          "taxType": "rent",
                  |          "calcType": "slice",
                  |          "taxDue": 0,
                  |          "slices": [
                  |            {
                  |              "from": 0,
                  |              "to": 150000,
                  |              "rate": 0,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 150000,
                  |              "to": -1,
                  |              "rate": 1,
                  |              "taxDue": 0
                  |            }
                  |          ]
                  |        },
                  |        {
                  |          "taxType": "premium",
                  |          "calcType": "slab",
                  |          "taxDue": 7500,
                  |          "rate": 3
                  |        }
                  |      ]
                  |    }
                  |  ]
                  |}
                  |""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe expectedResponse
            }

            "property type is Mixed and premium is £500001" in {
              def request: WSResponse = ws.url(
                  calculateUrl)
                .post(
                  Json.parse(
                    """
                      |{
                      |  "holdingType": "Leasehold",
                      |  "propertyType": "Mixed",
                      |  "effectiveDateDay": 11,
                      |  "effectiveDateMonth": 3,
                      |  "effectiveDateYear": 2008,
                      |  "premium": 500001,
                      |  "highestRent": 0,
                      |  "leaseDetails": {
                      |    "startDateDay": 11,
                      |    "startDateMonth": 3,
                      |    "startDateYear": 2008,
                      |    "endDateDay": 11,
                      |    "endDateMonth": 3,
                      |    "endDateYear": 2009,
                      |    "leaseTerm": {
                      |      "years": 1,
                      |      "days": 1,
                      |      "daysInPartialYear": 365
                      |    },
                      |    "year1Rent": 1000,
                      |    "year2Rent": 1000
                      |  },
                      |    "isLinked": false,
                      |    "relevantRentDetails": {
                      |    "relevantRent": 1000
                      |    }
                      |}
                      |""".stripMargin
                  )
                )

              lazy val expectedResponse: JsValue = Json.parse(
                """
                  |{
                  |  "result": [
                  |    {
                  |      "totalTax": 20000,
                  |      "resultHeading": "Results of calculation based on SDLT rules for the effective date entered",
                  |      "npv": 1899,
                  |      "taxCalcs": [
                  |        {
                  |          "taxType": "rent",
                  |          "calcType": "slice",
                  |          "taxDue": 0,
                  |          "slices": [
                  |            {
                  |              "from": 0,
                  |              "to": 150000,
                  |              "rate": 0,
                  |              "taxDue": 0
                  |            },
                  |            {
                  |              "from": 150000,
                  |              "to": -1,
                  |              "rate": 1,
                  |              "taxDue": 0
                  |            }
                  |          ]
                  |        },
                  |        {
                  |          "taxType": "premium",
                  |          "calcType": "slab",
                  |          "taxDue": 20000,
                  |          "rate": 4
                  |        }
                  |      ]
                  |    }
                  |  ]
                  |}
                  |""".stripMargin
              )

              request.status shouldBe OK
              request.json shouldBe expectedResponse
            }
          }
        }
      }
    }
  }
}
