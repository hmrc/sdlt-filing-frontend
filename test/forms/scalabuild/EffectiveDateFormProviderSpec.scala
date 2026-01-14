/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import base.ScalaSpecBase
import org.scalatest.freespec.AnyFreeSpec
import play.api.data.FormError

import java.time.LocalDate
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class EffectiveDateFormProviderSpec extends AnyFreeSpec with ScalaSpecBase {

  val form = new EffectiveDateFormProvider()()

    "bind valid data" in {
      val date  = LocalDate.of(2022,2,11)

      val data = Map(
        "effectiveDate.day"   -> "11",
        "effectiveDate.month" -> "2",
        "effectiveDate.year"  -> "2022"
      )
      val result = form.bind(data)
      result.value.value mustEqual date
      result.errors mustBe empty
    }

    "return errors when attempting to bind invalid date formats" in {

      val invalidDates = Seq(("33", "3", "2022"),("2", "14", "2022"),("10", "3", "yutu"))

      invalidDates.map { invalidDate =>

      val data = Map(
        "effectiveDate.day"   -> invalidDate._1,
        "effectiveDate.month" -> invalidDate._2,
        "effectiveDate.year"  -> invalidDate._3
      )

      val result = form.bind(data)
        result.errors must contain only FormError("effectiveDate", "effectiveDate.error.invalid", List())
      }
    }
}