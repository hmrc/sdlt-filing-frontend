/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import models.scalabuild.LeaseDates
import org.scalatest.freespec.AnyFreeSpec
import play.api.data.FormError
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

import java.time.LocalDate

class LeaseDatesFormProviderSpec extends AnyFreeSpec with ScalaSpecBase {

  val effectiveDate: LocalDate = LocalDate.of(2022, 2, 1)
  val form = new LeaseDatesFormProvider()(effectiveDate)
  val validStart = LocalDate.of(2022, 3, 1)
  val validEnd = LocalDate.of(2022, 4, 1)

  def data(start: LocalDate, end: LocalDate) = Map(
    "leaseStartDate.day" -> start.getDayOfMonth.toString,
    "leaseStartDate.month" -> start.getMonthValue.toString,
    "leaseStartDate.year" -> start.getYear.toString,
    "leaseEndDate.day" -> end.getDayOfMonth.toString,
    "leaseEndDate.month" -> end.getMonthValue.toString,
    "leaseEndDate.year" -> end.getYear.toString
  )

  "LeaseDatesFormProvider" - {
    "bind valid data" in {
      val result = form.bind(data(validStart, validEnd))
      result.errors mustBe empty
      result.value.value mustEqual LeaseDates(validStart, validEnd)
    }

    "return an error when lease start date fields are missing" in {
      val result = form.bind(
        Map(
          "leaseStartDate.day" -> "",
          "leaseStartDate.month" -> "",
          "leaseStartDate.year" -> "",
          "leaseEndDate.day" -> "1",
          "leaseEndDate.month" -> "4",
          "leaseEndDate.year" -> "2022"
        )
      )
      result.errors must contain(FormError("leaseStartDate", "leaseStartDate.error.required.all"))
    }

    "return an error when lease end date fields are missing" in {
      val result = form.bind(
        Map(
          "leaseStartDate.day" -> "1",
          "leaseStartDate.month" -> "3",
          "leaseStartDate.year" -> "2022",
          "leaseEndDate.day" -> "",
          "leaseEndDate.month" -> "",
          "leaseEndDate.year" -> ""
        )
      )
      result.errors must contain(FormError("leaseEndDate", "leaseEndDate.error.required.all"))
    }

    "return errors for invalid date formats" in {
      val invalid = Map(
        "leaseStartDate.day"   -> "x",
        "leaseStartDate.month" -> "13",
        "leaseStartDate.year"  -> "yyyy",
        "leaseEndDate.day"     -> "45",
        "leaseEndDate.month"   -> "0",
        "leaseEndDate.year"    -> "test"
      )
      val result = form.bind(invalid)
      result.errors must contain(FormError("leaseStartDate", "leaseDate.error.invalid"))
      result.errors must contain(FormError("leaseEndDate",   "leaseDate.error.invalid"))
    }

    "return an error when the end date is before the start date" in {
      val result = form.bind(data(
        start = LocalDate.of(2022, 4, 1),
        end   = LocalDate.of(2022, 3, 1)
      ))
      result.errors must contain(FormError("", "leaseEndDate.error.beforeStartDate"))
    }

    "return an error when the end date is before the effective date" in {
      val result = form.bind(
        data(
          start = LocalDate.of(2022, 2, 10),
          end   = LocalDate.of(2022, 1, 20)
        )
      )
      result.errors must contain(FormError("", "leaseEndDate.error.beforeEffectiveDate"))
    }

    "allow end date that is equal to the effective date" in {
      val start = LocalDate.of(2022, 1, 10)
      val end   = effectiveDate
      val result = form.bind(data(start, end))
      result.errors mustBe empty
      result.value.value mustEqual LeaseDates(start, end)
    }

    "allow start date equals the end date" in {
      val startEnd = LocalDate.of(2022, 3, 5)
      val result = form.bind(data(startEnd, startEnd))
      result.errors mustBe empty
      result.value.value mustEqual LeaseDates(startEnd, startEnd)
    }

    "form.fill must correctly unapply and populate data" in {
      val leaseDates = LeaseDates(
        startDate = LocalDate.of(2022, 3, 1),
        endDate   = LocalDate.of(2022, 4, 1)
      )
      val filledForm = form.fill(leaseDates)
      filledForm.data mustBe Map(
        "leaseStartDate.day"   -> "1",
        "leaseStartDate.month" -> "3",
        "leaseStartDate.year"  -> "2022",
        "leaseEndDate.day"     -> "1",
        "leaseEndDate.month"   -> "4",
        "leaseEndDate.year"    -> "2022"
      )
    }
  }
}
