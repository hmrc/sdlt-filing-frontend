package calculation.validators.api

import java.time.LocalDate

import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class RequestValidatorsSpec extends UnitSpec {

  "dateReads" should {
    "successfully read a date" when {
      "a full date is provided with a prefix" in {
        val testJson = Json.parse(
          """
            |{
            |  "memorableDay":21,
            |  "memorableMonth":6,
            |  "memorableYear":2003
            |}
          """.stripMargin)

        val date = LocalDate.of(2003, 6, 21)

        val result = Json.fromJson[LocalDate](testJson)(RequestValidators.multiFieldDateReads("memorable"))

        result shouldBe JsSuccess(date)
      }
    }
  }

}
