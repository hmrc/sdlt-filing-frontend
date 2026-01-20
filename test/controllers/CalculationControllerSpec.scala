/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package controllers

import base.{BaseSpec, ScalaSpecBase}
import enums.sdltRebuild._
import enums.{CalcTypes, TaxTypes}
import models.{CalculationDetails, CalculationResponse, Result}
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.scalacheck.Gen
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.Application
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import services.CalculationService


class CalculationControllerSpec extends BaseSpec with ScalaSpecBase {

  val app: Application = application()
  val mockComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val mockCalculationService: CalculationService = mock[CalculationService]
  val testCalculationController = new CalculationController(mockCalculationService, mockComponents)
  val materializer: Materializer = app.materializer

  val calcDetails: CalculationDetails = CalculationDetails(
    taxType = TaxTypes.premium,
    calcType = CalcTypes.slab,
    taxDue = 0,
    detailHeading = None,
    bandHeading = None,
    detailFooter = None,
    rate = None,
    slices = None
  )

  def createResult(msg: String): Result = Result(
    totalTax = 0,
    resultHeading = Some(msg),
    resultHint = None,
    npv = None,
    taxCalcs = Seq(calcDetails)
  )

  def createSelfAssessedResult(msg: String): Result = Result(
    totalTax = 0,
    resultHeading = Some(msg),
    resultHint = None,
    npv = None,
    taxCalcs = Seq.empty
  )

  private val zeroRateFreePortReliefGen:Gen[TaxReliefCode with ZeroRate] = Gen.oneOf(FreeportsTaxSiteRelief,InvestmentZonesTaxSiteRelief)
  private val zeroRateWithoutFreePortReliefGen: Gen[TaxReliefCode with StandardZeroRate] = Gen.oneOf(PartExchange, ReLocationEmployment,
    CompulsoryPurchaseFacilitatingDevelopment, ComplianceWithPlanningObligations,
    GroupRelief, ReConstructionRelief, DemutualisationOfInsuranceCompany,
    DemutualisationOfBuildingSociety, IncorporationOfLimitedLiabilityPartnership,
    TransfersInvolvingPublicBodies, TransferInConsequenceOfReorganisationOfParliamentaryConstituencies,
    CharitiesTaxReliefs, AcquisitionByBodiesEstablishedForNationalPurposes, RegisteredSocialLandlords,
    AlternativePropertyFinance, CroftingCommunityRightToBuy, DiplomaticPrivileges, OtherTaxReliefs,
    CombinationOfReliefs, AlternativeFinanceInvestmentBondsRelief, SeedingRelief)

  "CalculateSDLTC" should{
    "throw a BadRequest(400)" when{
      "no Json data has been received" in{
        val fakeRequest = FakeRequest()
        val result = testCalculationController.calculateSDLTC(fakeRequest)
        status(result) mustBe BAD_REQUEST
        jsonBodyOf(await(result))(materializer) mustBe Json.toJson("No json data received.")
      }

      "there wasn't enough json data for it to match the request model" in {
        val incompleteJsonRequest: JsValue = Json.parse(
          """
                                         {
                                         "holdingType": "Leasehold",
                                         "propertyType": "Residential",
                                         "effectiveDateDay": 13,
                                         "effectiveDateMonth": 7,
                                         "effectiveDateYear": 2011,
                                         "premium": 500000
                                         }
                                        """)

        val fakeRequest = FakeRequest().withJsonBody(incompleteJsonRequest)
        val result = testCalculationController.calculateSDLTC(fakeRequest)
        status(result) mustBe BAD_REQUEST
        jsonBodyOf(await(result))(materializer) mustBe Json.toJson("Incorrect Json request body format supplied: JsError(List((/highestRent,List(JsonValidationError(List(error.path.missing),List())))))")
      }

      "model is invalid and contains errors" in{
        val jsonRequest: JsValue = Json.parse("""
                                                |{
                                                |  "holdingType": "Leasehold",
                                                |  "propertyType": "Residential",
                                                |  "effectiveDateDay": 13,
                                                |  "effectiveDateMonth": 7,
                                                |  "effectiveDateYear": 2011,
                                                |  "premium": 500000,
                                                |  "highestRent": 50000,
                                                |  "propertyDetails": {
                                                |    "individual": "Yes",
                                                |    "twoOrMoreProperties": "Yes",
                                                |    "replaceMainResidence": "Yes"
                                                |  },
                                                |  "leaseDetails": {
                                                |    "startDateDay": 15,
                                                |    "startDateMonth": 1,
                                                |    "startDateYear": 1949,
                                                |    "endDateDay": 31,
                                                |    "endDateMonth": 12,
                                                |    "endDateYear": 2049,
                                                |    "leaseTerm":  {
                                                |      "years": 33,
                                                |      "days": 0,
                                                |      "daysInPartialYear": 0
                                                |     },
                                                |    "year1Rent": 10000,
                                                |    "year2Rent": 20000,
                                                |    "year3Rent": 30000,
                                                |    "year4Rent": 40000,
                                                |    "year5Rent": 50000
                                                |  },
                                                |  "relevantRentDetails": {
                                                |    "contractPre201603": "Yes",
                                                |    "contractVariedPost201603": "No",
                                                |    "relevantRent": 1000
                                                |  }
                                                |}
                                                      """.stripMargin)

        val fakeRequest = FakeRequest().withJsonBody(jsonRequest)
        val result = testCalculationController.calculateSDLTC(fakeRequest)
        status(result) mustBe BAD_REQUEST
        jsonBodyOf(await(result))(materializer) mustBe Json.toJson("Validation error: List(ValidationFailure(Effective date of '2011-07-13' is before 22 March, 2012), ValidationFailure(Lease term year: 33, Lease term day: 0, comparisonDate: 2044-07-12 does not match the difference between 2011-07-13 and 2049-12-31))")
      }
    }

    "return a 200" when{
      val completeJsonRequest: JsValue = Json.parse("""
                                              |{
                                              |  "holdingType": "Leasehold",
                                              |  "propertyType": "Residential",
                                              |  "effectiveDateDay": 13,
                                              |  "effectiveDateMonth": 7,
                                              |  "effectiveDateYear": 2015,
                                              |  "premium": 500000,
                                              |  "highestRent": 50000,
                                              |  "propertyDetails": {
                                              |    "individual": "Yes",
                                              |    "twoOrMoreProperties": "Yes",
                                              |    "replaceMainResidence": "Yes"
                                              |  },
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
                                              |      "daysInPartialYear": 0
                                              |     },
                                              |    "year1Rent": 10000,
                                              |    "year2Rent": 20000,
                                              |    "year3Rent": 30000,
                                              |    "year4Rent": 40000,
                                              |    "year5Rent": 50000
                                              |  },
                                              |  "relevantRentDetails": {
                                              |    "contractPre201603": "Yes",
                                              |    "contractVariedPost201603": "No",
                                              |    "relevantRent": 1000
                                              |  }
                                              |}
                                            """.stripMargin)

      "given a valid json" in{
        val response = CalculationResponse(Seq(createResult("given a valid json")))

        when(mockCalculationService.calculateTax(any())).thenReturn(response)

        val fakeRequest = FakeRequest().withJsonBody(completeJsonRequest)
        val result = testCalculationController.calculateSDLTC(fakeRequest)
        status(result) mustBe OK
        println(jsonBodyOf(await(result))(materializer))
        jsonBodyOf(await(result))(materializer) mustBe Json.toJson(response)
        verify(mockCalculationService, times(1)).calculateTax(any())
      }
    }

    "taxReliefDetails are given " when{
      "throw a BadRequest" when {
        "TaxReliefCode is FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief" when {
          "isPartialRelief is not defined(None)" in {
            forAll(zeroRateFreePortReliefGen) {
              value =>
                val jsonRequestWithoutIsPartialReliefNotDefined:JsValue = Json.parse(
                  s"""
                     |{
                     |  "holdingType": "Leasehold",
                     |  "propertyType": "Non-residential",
                     |  "effectiveDateDay": 23,
                     |  "effectiveDateMonth": 3,
                     |  "effectiveDateYear": 2012,
                     |  "premium": 1000000,
                     |  "highestRent": 0,
                     |  "leaseDetails": {
                     |    "startDateDay": 23,
                     |    "startDateMonth": 3,
                     |    "startDateYear": 2012,
                     |    "endDateDay": 23,
                     |    "endDateMonth": 3,
                     |    "endDateYear": 2013,
                     |    "leaseTerm": {
                     |      "years": 1,
                     |      "days": 1,
                     |      "daysInPartialYear": 365
                     |    },
                     |    "year1Rent": 999,
                     |    "year2Rent": 999
                     |  },
                     |  "isLinked": false,
                     |  "taxReliefDetails": {
                     |   "taxReliefCode": ${value.code}
                     | }
                     |}
                     |""".stripMargin)

                val fakeRequest = FakeRequest().withJsonBody(jsonRequestWithoutIsPartialReliefNotDefined)
                val result = testCalculationController.calculateSDLTC(fakeRequest)
                status(result) mustBe  BAD_REQUEST
                jsonBodyOf(result.futureValue)(materializer) mustBe Json.toJson("Validation error: List(ValidationFailure(No partial relief type defined.))")
            }
          }
        }
      }

      "return a 200" when {
        "TaxReliefCode is not  FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief" when {
          "isPartialRelief is true for self assessed" in {
            forAll(zeroRateWithoutFreePortReliefGen) {
              value =>
                reset(mockCalculationService)
                val jsonRequestWithoutIsPartialReliefNotDefined:JsValue = Json.parse(
                  s"""
                     |{
                     |  "holdingType": "Leasehold",
                     |  "propertyType": "Non-residential",
                     |  "effectiveDateDay": 23,
                     |  "effectiveDateMonth": 3,
                     |  "effectiveDateYear": 2012,
                     |  "premium": 1000000,
                     |  "highestRent": 0,
                     |  "leaseDetails": {
                     |    "startDateDay": 23,
                     |    "startDateMonth": 3,
                     |    "startDateYear": 2012,
                     |    "endDateDay": 23,
                     |    "endDateMonth": 3,
                     |    "endDateYear": 2013,
                     |    "leaseTerm": {
                     |      "years": 1,
                     |      "days": 1,
                     |      "daysInPartialYear": 365
                     |    },
                     |    "year1Rent": 999,
                     |    "year2Rent": 999
                     |  },
                     |  "isLinked": false,
                     |  "taxReliefDetails": {
                     |   "taxReliefCode": ${value.code},
                     |   "isPartialRelief": true
                     | }
                     |}
                     |""".stripMargin)
                val response = CalculationResponse(Seq(createSelfAssessedResult("given a valid json")))

                when(mockCalculationService.calculateTax(any())).thenReturn(response)

                val fakeRequest = FakeRequest().withJsonBody(jsonRequestWithoutIsPartialReliefNotDefined)
                val result = testCalculationController.calculateSDLTC(fakeRequest)
                status(result) mustBe OK
                jsonBodyOf(result.futureValue)(materializer) mustBe Json.toJson(response)
                verify(mockCalculationService, times(1)).calculateTax(any())
            }
          }

          "isPartialRelief is not defined " in {
            forAll(zeroRateWithoutFreePortReliefGen) {
              value =>
                reset(mockCalculationService)
                val jsonRequestWithoutIsPartialReliefNotDefined:JsValue = Json.parse(
                  s"""
                     |{
                     |  "holdingType": "Leasehold",
                     |  "propertyType": "Non-residential",
                     |  "effectiveDateDay": 23,
                     |  "effectiveDateMonth": 3,
                     |  "effectiveDateYear": 2012,
                     |  "premium": 1000000,
                     |  "highestRent": 0,
                     |  "leaseDetails": {
                     |    "startDateDay": 23,
                     |    "startDateMonth": 3,
                     |    "startDateYear": 2012,
                     |    "endDateDay": 23,
                     |    "endDateMonth": 3,
                     |    "endDateYear": 2013,
                     |    "leaseTerm": {
                     |      "years": 1,
                     |      "days": 1,
                     |      "daysInPartialYear": 365
                     |    },
                     |    "year1Rent": 999,
                     |    "year2Rent": 999
                     |  },
                     |  "isLinked": false,
                     |  "taxReliefDetails": {
                     |   "taxReliefCode": ${value.code}
                     | }
                     |}
                     |""".stripMargin)
                val response = CalculationResponse(Seq(createResult("given a valid json")))

                when(mockCalculationService.calculateTax(any())).thenReturn(response)

                val fakeRequest = FakeRequest().withJsonBody(jsonRequestWithoutIsPartialReliefNotDefined)
                val result = testCalculationController.calculateSDLTC(fakeRequest)
                status(result) mustBe OK
                jsonBodyOf(result.futureValue)(materializer) mustBe Json.toJson(response)
                verify(mockCalculationService, times(1)).calculateTax(any())
            }
          }
        }

        "TaxReliefCode is FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief" when {
          "isPartialRelief is false " in {
            forAll(zeroRateFreePortReliefGen) {
              value =>
                reset(mockCalculationService)
                val jsonRequestWithoutIsPartialReliefNotDefined:JsValue = Json.parse(
                  s"""
                     |{
                     |  "holdingType": "Leasehold",
                     |  "propertyType": "Non-residential",
                     |  "effectiveDateDay": 23,
                     |  "effectiveDateMonth": 3,
                     |  "effectiveDateYear": 2012,
                     |  "premium": 1000000,
                     |  "highestRent": 0,
                     |  "leaseDetails": {
                     |    "startDateDay": 23,
                     |    "startDateMonth": 3,
                     |    "startDateYear": 2012,
                     |    "endDateDay": 23,
                     |    "endDateMonth": 3,
                     |    "endDateYear": 2013,
                     |    "leaseTerm": {
                     |      "years": 1,
                     |      "days": 1,
                     |      "daysInPartialYear": 365
                     |    },
                     |    "year1Rent": 999,
                     |    "year2Rent": 999
                     |  },
                     |  "isLinked": false,
                     |  "taxReliefDetails": {
                     |   "taxReliefCode": ${value.code},
                     |   "isPartialRelief": false
                     | }
                     |}
                     |""".stripMargin)
                val response = CalculationResponse(Seq(createResult("given a valid json")))

                when(mockCalculationService.calculateTax(any())).thenReturn(response)

                val fakeRequest = FakeRequest().withJsonBody(jsonRequestWithoutIsPartialReliefNotDefined)
                val result = testCalculationController.calculateSDLTC(fakeRequest)
                status(result) mustBe OK
                jsonBodyOf(result.futureValue)(materializer) mustBe Json.toJson(response)
                verify(mockCalculationService, times(1)).calculateTax(any())
            }
          }
          "isPartialRelief is true for self assessed" in {
            forAll(zeroRateFreePortReliefGen) {
              value =>
                reset(mockCalculationService)
                val jsonRequestIsPartialReliefTrue:JsValue = Json.parse(
                  s"""
                     |{
                     |  "holdingType": "Leasehold",
                     |  "propertyType": "Non-residential",
                     |  "effectiveDateDay": 23,
                     |  "effectiveDateMonth": 3,
                     |  "effectiveDateYear": 2012,
                     |  "premium": 1000000,
                     |  "highestRent": 0,
                     |  "leaseDetails": {
                     |    "startDateDay": 23,
                     |    "startDateMonth": 3,
                     |    "startDateYear": 2012,
                     |    "endDateDay": 23,
                     |    "endDateMonth": 3,
                     |    "endDateYear": 2013,
                     |    "leaseTerm": {
                     |      "years": 1,
                     |      "days": 1,
                     |      "daysInPartialYear": 365
                     |    },
                     |    "year1Rent": 999,
                     |    "year2Rent": 999
                     |  },
                     |  "isLinked": false,
                     |  "taxReliefDetails": {
                     |   "taxReliefCode": ${value.code},
                     |   "isPartialRelief": true
                     | }
                     |}
                     |""".stripMargin)

                val response = CalculationResponse(Seq(createSelfAssessedResult("given a valid json")))

                when(mockCalculationService.calculateTax(any())).thenReturn(response)

                val fakeRequest = FakeRequest().withJsonBody(jsonRequestIsPartialReliefTrue)
                val result = testCalculationController.calculateSDLTC(fakeRequest)
                status(result) mustBe OK
                jsonBodyOf(result.futureValue)(materializer) mustBe Json.toJson(response)
                verify(mockCalculationService, times(1)).calculateTax(any())
            }
          }
        }
      }
    }
  }
}
