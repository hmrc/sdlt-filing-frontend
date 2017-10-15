package calculation.validators.api

import java.time.LocalDate

import calculation.models.{LeaseDetails, LeaseTerm}
import play.api.data.validation.ValidationError
import play.api.libs.json._
import testutils.JsonValidation
import uk.gov.hmrc.play.test.UnitSpec

class RequestValidatorsSpec extends UnitSpec with JsonValidation {

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

    "Fail to read a date" when {
      "day is not provided" in {
        val testJson = Json.parse(
          """
            |{
            |  "specificMonth":6,
            |  "specificYear":2003
            |}
          """.stripMargin)

        val result = Json.fromJson[LocalDate](testJson)(RequestValidators.multiFieldDateReads("specific"))

        shouldHaveErrors(result, __ \ "specificDay", ValidationError(List("error.path.missing")))
      }

      "month is not provided" in {
        val testJson = Json.parse(
          """
            |{
            |  "otherDay":21,
            |  "otherYear":2003
            |}
          """.stripMargin)

        val result = Json.fromJson[LocalDate](testJson)(RequestValidators.multiFieldDateReads("other"))

        shouldHaveErrors(result, __ \ "otherMonth", ValidationError(List("error.path.missing")))
      }

      "year is not provided" in {
        val testJson = Json.parse(
          """
            |{
            |  "differentDay":21,
            |  "differentMonth":6
            |}
          """.stripMargin)

        val result = Json.fromJson[LocalDate](testJson)(RequestValidators.multiFieldDateReads("different"))

        shouldHaveErrors(result, __ \ "differentYear", ValidationError(List("error.path.missing")))
      }

      "the wrong data types are provided" in {
        val testJson = Json.parse(
          """
            |{
            |  "memorableDay":"21",
            |  "memorableMonth":{"name": "January"},
            |  "memorableYear":[2013]
            |}
          """.stripMargin)

        val date = LocalDate.of(2003, 6, 21)

        val result = Json.fromJson[LocalDate](testJson)(RequestValidators.multiFieldDateReads("memorable"))

        shouldHaveErrors(result, Map(
          __ \ "memorableDay" -> Seq(ValidationError(List("error.expected.jsnumber"))),
          __ \ "memorableMonth" -> Seq(ValidationError(List("error.expected.jsnumber"))),
          __ \ "memorableYear" -> Seq(ValidationError(List("error.expected.jsnumber")))
        ))
      }

      "a full, invalid date is provided" in {
        val testJson = Json.parse(
          """
            |{
            |  "memorableDay":29,
            |  "memorableMonth":2,
            |  "memorableYear":2017
            |}
          """.stripMargin)

        val date = LocalDate.of(2003, 6, 21)

        val result = Json.fromJson[LocalDate](testJson)(RequestValidators.multiFieldDateReads("memorable"))

        shouldHaveErrors(result, __, ValidationError(List("'memorable' date could not be parsed. Error: Invalid date 'February 29' as '2017' is not a leap year")))
      }
    }
  }

  "yesNoToBooleanReads" should {
    "successfully read 'Yes'" in {
      Json.fromJson[Boolean](JsString("Yes"))(RequestValidators.yesNoToBooleanReads) shouldBe JsSuccess(true)
    }
    "successfully read 'No'" in {
      Json.fromJson[Boolean](JsString("No"))(RequestValidators.yesNoToBooleanReads) shouldBe JsSuccess(false)
    }
    "fail" when {
      "an invalid string is provided" in {
        val result = Json.fromJson[Boolean](JsString("notBool"))(RequestValidators.yesNoToBooleanReads)
        shouldHaveErrors(result, __, ValidationError(List("'notBool' could not be converted to Boolean")))
      }
      "a non-string value is provided" in {
        val result = Json.fromJson[Boolean](JsNumber(3))(RequestValidators.yesNoToBooleanReads)
        shouldHaveErrors(result, __, ValidationError(List("error.expected.jsstring")))
      }
    }
  }

}
