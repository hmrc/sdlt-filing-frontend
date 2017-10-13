package calculation.models

import java.time.LocalDate

import play.api.libs.json.{JsString, JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class RequestSpec extends UnitSpec {

  "LeaseTerm" should {
    "read from Json" in {
      val testJson = Json.parse(
        """{
          |  "years": 48,
          |  "days": 11,
          |  "daysInPartialYear": 5
          |}""".stripMargin)

      val model = LeaseTerm(years = 48, days = 11, daysInPartialYear = 5)

      Json.fromJson[LeaseTerm](testJson) shouldBe JsSuccess(model)
    }
  }

  "LeaseDetails" should {
    "read from Json" in {
      val testJson = Json.parse(
        """
          |{
          |  "startDateDay": 15,
          |  "startDateMonth": 1,
          |  "startDateYear": 1949,
          |  "endDateDay": 31,
          |  "endDateMonth": 12,
          |  "endDateYear": 2049,
          |  "leaseTerm":  {
          |    "years": 33,
          |    "days": 0,
          |    "daysInPartialYear": 0
          |   },
          |  "year1Rent": 10000,
          |  "year2Rent": 20000,
          |  "year3Rent": 30000,
          |  "year4Rent": 40000,
          |  "year5Rent": 50000
          |}
        """.stripMargin)

      val model = LeaseDetails(
        startDate = LocalDate.of(1949, 1, 15),
        endDate = LocalDate.of(2049, 12,  31),
        leaseTerm = LeaseTerm(
          years = 33,
          days = 0,
          daysInPartialYear = 0
        ),
        year1Rent = 10000,
        year2Rent = 20000,
        year3Rent = 30000,
        year4Rent = 40000,
        year5Rent = 50000
      )

      Json.fromJson[LeaseDetails](testJson) shouldBe JsSuccess(model)
    }
  }
}
