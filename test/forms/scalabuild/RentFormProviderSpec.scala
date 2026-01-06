/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import models.scalabuild.RentPeriods
import play.api.data.{Form, FormError}

class RentFormProviderSpec extends ScalaSpecBase {
  val count = 3
  val form: Form[RentPeriods] = new RentFormProvider().apply(count)
  "RentFormProvider" - {
    "should bind a valid list of BigDecimal rents" in {
      val data = Map(
        "rents[0]" -> "100.00",
        "rents[1]" -> "200.50",
        "rents[2]" -> "300"
      )
      val result = form.bind(data)
      result.errors mustBe empty
      result.value.value.rents mustBe List(BigDecimal(100.00), BigDecimal(200.50), BigDecimal(300))
    }
  }

  "should fail when a rent value is missing" in {
    val data = Map(
      "rents[0]" -> "100.00",
      "rents[1]" -> "200.50"
    )
    val result = form.bind(data)
    result.errors mustBe Seq(FormError("rents", "rents.error.required"))
  }

  "should fail when a rent value is non-numeric" in {
    val data = Map(
      "rents[0]" -> "100.00",
      "rents[1]" -> "abc",
      "rents[2]" -> "300.00"
    )
    val result = form.bind(data)
    result.errors mustBe Seq(FormError("rents[1]", "rents.error.nonNumeric"))
  }

  "should fail when the list length does not match count" in {
    val data = Map(
      "rents[0]" -> "100.00",
      "rents[1]" -> "200.00",
      "rents[2]" -> "300.00",
      "rents[3]" -> "400.00"
    )
    val result = form.bind(data)
    result.errors mustBe Seq(FormError("rents", "rents.error.required"))
  }

  "should fail when a value has too many decimal places" in {
    val data = Map(
      "rents[0]" -> "100.123",
      "rents[1]" -> "200.00",
      "rents[2]" -> "300.00"
    )
    val result = form.bind(data)
    result.errors mustBe Seq(FormError("rents[0]", "rents.error.nonNumeric"))
  }

  "should bind minimum allowed values" in {
    val data = Map(
      "rents[0]" -> "0.00",
      "rents[1]" -> "0.00",
      "rents[2]" -> "0.00"
    )
    val result = form.bind(data)
    result.errors mustBe empty
    result.value.value.rents mustBe List(BigDecimal(0.00), BigDecimal(0.00), BigDecimal(0.00))
  }

  "should bind maximum allowed values" in {
    val data = Map(
      "rents[0]" -> "9999999999.99",
      "rents[1]" -> "9999999999.99",
      "rents[2]" -> "9999999999.99"
    )
    val result = form.bind(data)
    result.errors mustBe empty
    result.value.value.rents mustBe List(
      BigDecimal("9999999999.99"),
      BigDecimal("9999999999.99"),
      BigDecimal("9999999999.99")
    )
  }

  "should unbind (fill) correctly using unapply" in {
    val form = new RentFormProvider().apply(count)
    val model = RentPeriods(
      rents = List(BigDecimal(100.00), BigDecimal(200.50), BigDecimal(300.00))
    )
    val filledForm = form.fill(model)
    filledForm.data mustBe Map(
      "rents[0]" -> "100.0",
      "rents[1]" -> "200.5",
      "rents[2]" -> "300.0"
    )
  }
}
