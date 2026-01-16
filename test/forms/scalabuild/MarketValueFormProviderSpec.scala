/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import models.scalabuild.MarketValueChoice.{PayInStages, PayUpfront}
import models.scalabuild.MarketValue
import org.scalatest.freespec.AnyFreeSpec
import play.api.data.{Form, FormError}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class MarketValueFormProviderSpec extends AnyFreeSpec with ScalaSpecBase {

  val highLimit = BigDecimal(appConfig.highValue)
  val lowLimit = BigDecimal(appConfig.lowValue)

  val formWithHighFtb: Form[MarketValue] = new MarketValueFormProvider().apply(highLimit)
  val formWithLowFtb: Form[MarketValue] = new MarketValueFormProvider().apply(lowLimit)

  val requiredError = "marketValue.error.required"
  val nonNumericError = "marketValue.error.nonNumeric"
  val maxValueError = "marketValue.error.maxValue"

  "MarketValueFormProvider" - {
    "should bind PayUpfront" in {
      val data = Map(("value", "PayUpfront"), ("paySDLTUpfront", "1234"))
      val result = formWithHighFtb.bind(data)
      result.value mustBe Some(MarketValue(PayUpfront, Some(BigDecimal(1234)), None))
      result.errors mustBe empty
    }

    "should return required error when PayUpfront selected but amount is missing" in {
      val result = formWithHighFtb.bind(
        Map("value" -> "PayUpfront")
      )
      result.errors mustBe Seq(FormError("paySDLTUpfront", requiredError))
    }

    "should return non-numeric error for PayUpfront invalid input" in {
      val result = formWithHighFtb.bind(
        Map(
          "value" -> "PayUpfront",
          "paySDLTUpfront" -> "ABC"
        )
      )
      result.errors mustBe Seq(FormError("paySDLTUpfront", nonNumericError))
    }

    "should return max value error for PayUpfront when over limit" in {
      val result = formWithHighFtb.bind(
        Map(
          "value" -> "PayUpfront",
          "paySDLTUpfront" -> (highLimit + 1).toString
        )
      )
      result.errors mustBe Seq(FormError("paySDLTUpfront", maxValueError, Seq(highLimit)))
    }

    "should bind successfully when PayInStages selected with valid amount" in {
      val result = formWithLowFtb.bind(
        Map(
          "value" -> "PayInStages",
          "marketPropValue" -> "2000"
        )
      )
      result.errors mustBe Nil
      result.value mustBe Some(MarketValue(PayInStages, None, Some(2000)))
    }

    "should return required error when PayInStages selected but amount is missing" in {
      val result = formWithLowFtb.bind(
        Map("value" -> "PayInStages")
      )
      result.errors mustBe Seq(FormError("marketPropValue", requiredError))
    }

    "should return non-numeric error for PayInStages invalid input" in {
      val result = formWithLowFtb.bind(
        Map(
          "value" -> "PayInStages",
          "marketPropValue" -> "!!!"
        )
      )
      result.errors mustBe Seq(FormError("marketPropValue", nonNumericError))
    }

    "should return max value error for PayInStages when over limit" in {
      val result = formWithLowFtb.bind(
        Map(
          "value" -> "PayInStages",
          "marketPropValue" -> (lowLimit + 1).toString
        )
      )
      result.errors mustBe Seq(FormError("marketPropValue", maxValueError, Seq(lowLimit)))
    }

    "should return required error when no radio selected" in {
      val result = formWithLowFtb.bind(Map.empty[String, String])
      result.errors mustBe Seq(FormError("value", requiredError))
    }

    "return invalid error when radio value is unrecognised" in {
      val result = formWithLowFtb.bind(Map("value" -> "BAD_VALUE"))
      result.errors mustBe Seq(FormError("value", "error.invalid"))
    }

    "should ignore the PayUpfront field when PayInStages selected" in {
      val result = formWithLowFtb.bind(
        Map(
          "value" -> "PayInStages",
          "marketPropValue" -> "5000",
          "paySDLTUpfront" -> "999999"
        )
      )
      result.value mustBe Some(MarketValue(PayInStages, None, Some(5000)))
    }

    "should ignore the marketPropValue field when PayUpfront selected" in {
      val result = formWithHighFtb.bind(
        Map(
          "value" -> "PayUpfront",
          "paySDLTUpfront" -> "5000",
          "marketPropValue" -> "123456"
        )
      )
      result.value mustBe Some(MarketValue(PayUpfront, Some(5000), None))
    }

    "should return error if PayUpfront amount has more than 2 decimal places" in {
      val data = Map(
        "value" -> "PayUpfront",
        "paySDLTUpfront" -> "123.456"
      )
      val result = formWithLowFtb.bind(data)
      result.errors must contain(FormError("paySDLTUpfront", "marketValue.error.nonNumeric"))
    }

    "should return error if PayInStages amount has more than 2 decimal places" in {
      val data = Map(
        "value" -> "PayInStages",
        "marketPropValue" -> "987.654"
      )
      val result = formWithHighFtb.bind(data)
      result.errors must contain(FormError("marketPropValue", "marketValue.error.nonNumeric"))
    }

    "unapply should populate the form fields correctly" in {
      val model = MarketValue(value = PayUpfront, paySDLTUpfront = Some(BigDecimal(100)), marketPropValue = None)
      val filledForm = formWithHighFtb.fill(model)
      filledForm("value").value mustBe Some(PayUpfront.toString)
      filledForm("paySDLTUpfront").value mustBe Some("100")
      filledForm("marketPropValue").value mustBe None
    }
  }
}