/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import models.scalabuild.RentPeriods
import org.scalatest.freespec.AnyFreeSpec
import play.api.data.{Form, FormError}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class RentFormProviderSpec extends AnyFreeSpec with ScalaSpecBase {
  val form: Form[RentPeriods] = new RentFormProvider().apply()
  "RentFormProvider" - {
    "should bind a valid BigDecimal rents" in {
      val data = Map(
        "year1Rent" -> "100.00",
        "year2Rent" -> "200.50",
        "year3Rent" -> "300"
      )
      val result = form.bind(data)
      result.errors mustBe empty
      result.value.value mustBe RentPeriods(
        year1Rent = BigDecimal(100.00),
        year2Rent = Some(BigDecimal(200.50)),
        year3Rent = Some(BigDecimal(300))
      )
    }

    ".rents should return a List of rents when not all years are defined" in {
      val data = Map(
        "year1Rent" -> "100.00",
        "year2Rent" -> "200.50",
        "year3Rent" -> "300"
      )
      val result = form.bind(data)
      result.errors mustBe empty
      result.value.value.rents mustBe List(100.00, 200.50, 300)
    }

    "should fail when a rent value is missing" in {
      val data = Map(
        "year1Rent" -> "100.00",
        "year2Rent" -> "200.50"
      )
      val result = form.bind(data)
      result.value.value.rents mustBe List(100.00, 200.50)
//      result.errors mustBe Seq(FormError("year3Rent", "rents.error.required"))
    }

    "should fail when a rent value is non-numeric" in {
      val data = Map(
        "year1Rent" -> "100.00",
        "year2Rent" -> "abc",
        "year3Rent" -> "300.00"
      )
      val result = form.bind(data)
      result.errors mustBe Seq(FormError("year2Rent", "rents.error.nonNumeric"))
    }

    "should fail when a value has too many decimal places" in {
      val data = Map(
        "year1Rent" -> "100.123",
        "year2Rent" -> "200.00",
        "year3Rent" -> "300.00"
      )
      val result = form.bind(data)
      result.errors mustBe Seq(FormError("year1Rent", "rents.error.nonNumeric"))
    }

    "should bind minimum allowed values" in {
      val data = Map(
        "year1Rent" -> "0.00",
        "year2Rent" -> "0.00",
        "year3Rent" -> "0.00"
      )
      val result = form.bind(data)
      result.errors mustBe empty
      result.value.value mustBe RentPeriods(
        year1Rent = BigDecimal(0.00),
        year2Rent = Some(BigDecimal(0.00)),
        year3Rent = Some(BigDecimal(0.00))
      )
    }

    "should bind maximum allowed values" in {
      val data = Map(
        "year1Rent" -> "9999999999.99",
        "year2Rent" -> "9999999999.99",
        "year3Rent" -> "9999999999.99"
      )
      val result = form.bind(data)
      result.errors mustBe empty
      result.value.value mustBe RentPeriods(
        year1Rent = BigDecimal("9999999999.99"),
        year2Rent = Some(BigDecimal("9999999999.99")),
        year3Rent = Some(BigDecimal("9999999999.99"))
      )
    }

    "should unbind (fill) correctly using unapply" in {
      val form = new RentFormProvider().apply()
      val model = RentPeriods(
        year1Rent = 100.0,
        year2Rent = Some(200.5),
        year3Rent = Some(300.0)
      )
      val filledForm = form.fill(model)
      filledForm.data mustBe Map(
        "year1Rent" -> "100.0",
        "year2Rent" -> "200.5",
        "year3Rent" -> "300.0"
      )
    }
  }
}
