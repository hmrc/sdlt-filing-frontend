/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import play.api.data.{Form, FormError}

class RelevantRentFormProviderSpec extends ScalaSpecBase {

  val form: Form[BigDecimal] = new RelevantRentFormProvider().apply()

  "RelevantRentFormProvider" - {
    "should bind a valid whole number" in {
      val data = Map("relevantRent" -> "100000")
      val result = form.bind(data)
      result.errors mustBe empty
      result.value.value mustBe BigDecimal(100000)
    }

    "should bind a valid decimal number with 2 decimal places" in {
      val data = Map("relevantRent" -> "12345.67")
      val result = form.bind(data)
      result.errors mustBe empty
      result.value.value mustBe BigDecimal(12345.67)
    }

    "should fail when value is missing" in {
      val result = form.bind(Map.empty[String, String])
      result.errors mustBe Seq(FormError("relevantRent", "relevantRent.error.required"))
    }

    "should fail when value is non-numeric" in {
      val data = Map("relevantRent" -> "abc123")
      val result = form.bind(data)
      result.errors mustBe Seq(FormError("relevantRent", "relevantRent.error.nonNumeric"))
    }

    "should fail when value has too many decimal places" in {
      val data = Map("relevantRent" -> "100.123")
      val result = form.bind(data)
      result.errors mustBe Seq(FormError("relevantRent", "relevantRent.error.nonNumeric"))
    }

    "fail when value is below minimum (0.00)" in {
      val data = Map("relevantRent" -> "-1")
      val result = form.bind(data)
      result.errors must have length 1
      result.errors.head.key mustBe "relevantRent"
      result.errors.head.message mustBe "relevantRent.error.nonNumeric"
    }

    "fail when value exceeds maximum 9999999999.99" in {
      val data = Map("relevantRent" -> "10000000000.00")
      val result = form.bind(data)
      result.errors must have length 1
      result.errors.head.key mustBe "relevantRent"
      result.errors.head.message mustBe "relevantRent.error.nonNumeric"
    }

    "should bind the maximum allowed value 9999999999.99" in {
      val data = Map("relevantRent" -> "9999999999.99")
      val result = form.bind(data)
      result.errors mustBe empty
      result.value.value mustBe BigDecimal("9999999999.99")
    }
  }
}