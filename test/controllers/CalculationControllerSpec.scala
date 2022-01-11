/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package controllers

import akka.stream.Materializer
import base.BaseSpec
import enums.{CalcTypes, TaxTypes}
import models.{CalculationDetails, CalculationResponse, Result}
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import services.CalculationService

class CalculationControllerSpec extends BaseSpec with MockFactory with GuiceOneAppPerSuite {

  val mockComponents: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
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
        jsonBodyOf(await(result))(materializer) mustBe Json.toJson("Incorrect Json request body format supplied: JsError(List((/highestRent,List(JsonValidationError(List(error.path.missing),WrappedArray())))))")
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

        (mockCalculationService.calculateTax _)
          .expects(*)
          .returns(response)
          .noMoreThanOnce()

        val fakeRequest = FakeRequest().withJsonBody(completeJsonRequest)
        val result = testCalculationController.calculateSDLTC(fakeRequest)
        status(result) mustBe OK
        println(jsonBodyOf(await(result))(materializer))
        jsonBodyOf(await(result))(materializer) mustBe Json.toJson(response)
      }
    }
  }
}
