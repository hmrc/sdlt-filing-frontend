package calculation.controllers

import akka.actor.ActorSystem
import calculation.services.CalculationService
import org.scalamock.scalatest.MockFactory
import play.api.http.Status._
import play.api.libs.json._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import akka.stream.ActorMaterializer
import calculation.enums.{CalcTypes, TaxTypes}
import calculation.models.{CalculationDetails, CalculationResponse, Result}

class CalculationControllerSpec extends UnitSpec with MockFactory {

  val mockCalculationService = mock[CalculationService]
  val testCalculationController = new CalculationController(mockCalculationService)
  val actorSystem = ActorSystem("actorSystem")
  val materializer = ActorMaterializer()(actorSystem)

  val calcDetails = CalculationDetails(
    taxType = TaxTypes.premium,
    calcType = CalcTypes.slab,
    taxDue = 0,
    detailHeading = None,
    bandHeading = None,
    detailFooter = None,
    rate = None,
    slices = None
  )

  def createResult(msg: String) = Result(
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
        status(result) shouldBe BAD_REQUEST
        jsonBodyOf(await(result))(materializer) shouldBe Json.toJson("No json data received.")
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
        status(result) shouldBe BAD_REQUEST
        jsonBodyOf(await(result))(materializer) shouldBe Json.toJson("Incorrect Json request body format supplied: JsError(List((/highestRent,List(ValidationError(List(error.path.missing),WrappedArray())))))")
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
        status(result) shouldBe BAD_REQUEST
        jsonBodyOf(await(result))(materializer) shouldBe Json.toJson("Validation error: List(ValidationFailure(Effective date of '2011-07-13' is before 22 March, 2012), ValidationFailure(Lease term year: 33 or Lease term date: 0 does not match the difference between 2011-07-13 and 2049-12-31))")
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
                                              |      "days": 171,
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

        (mockCalculationService.CalculateTax _)
          .expects(*)
          .returns(response)
          .noMoreThanOnce()

        val fakeRequest = FakeRequest().withJsonBody(completeJsonRequest)
        val result = testCalculationController.calculateSDLTC(fakeRequest)
        status(result) shouldBe OK
        println(jsonBodyOf(await(result))(materializer))
        jsonBodyOf(await(result))(materializer) shouldBe Json.toJson(response)
      }
    }
  }
}
