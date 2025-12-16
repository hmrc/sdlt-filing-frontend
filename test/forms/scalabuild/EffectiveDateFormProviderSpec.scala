/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import base.ScalaSpecBase
import play.api.data.FormError

import java.time.LocalDate


class EffectiveDateFormProviderSpec extends ScalaSpecBase {

  val form = new EffectiveDateFormProvider()()

    "bind valid data" in {
      val date  = LocalDate.of(2022,2,11)

      val data = Map(
        "effectiveDateDay"   -> "11",
        "effectiveDateMonth" -> "2",
        "effectiveDateYear"  -> "2022"
      )
      val result = form.bind(data)
      result.value.value mustEqual date
      result.errors mustBe empty
    }

    "return errors when attempting to bind invalid date formats" in {

      val invalidDates = Seq(("33", "3", "2022"),("2", "14", "2022"),("10", "3", "yutu"))

      invalidDates.map { invalidDate =>

      val data = Map(
        "effectiveDateDay"   -> invalidDate._1,
        "effectiveDateMonth" -> invalidDate._2,
        "effectiveDateYear"  -> invalidDate._3
      )

      val result = form.bind(data)
        result.errors must contain only FormError("effectiveDate", "effectiveDate.error.invalid", List())
      }
    }
}