/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import base.ScalaSpecBase
import models.scalabuild.PropertyType.{NonResidential, Residential}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.data.{Form, FormError}

import java.time.LocalDate

class EffectiveDateFormProviderSpec extends AnyFreeSpec with ScalaSpecBase {

  val formProvider = new EffectiveDateFormProvider()
  val formForResidential: Form[LocalDate] = formProvider(Residential)
  val formForNonResidential: Form[LocalDate] = formProvider(NonResidential)
    "bind valid data when Residential post 22/3/2012" in {
      val date  = LocalDate.of(2012,3,23)

      val data = Map(
        "effectiveDate.day"   -> "23",
        "effectiveDate.month" -> "3",
        "effectiveDate.year"  -> "2012"
      )
      val result = formForResidential.bind(data)
      result.value.value mustEqual date
      result.errors mustBe empty
    }

    "bind valid data when Non-Residential pre 22/3/2012" in {
      val date  = LocalDate.of(2012,3,21)

      val data = Map(
        "effectiveDate.day"   -> "21",
        "effectiveDate.month" -> "3",
        "effectiveDate.year"  -> "2012"
      )
      val result = formForNonResidential.bind(data)
      result.value.value mustEqual date
      result.errors mustBe empty
    }

    "bind valid data" in {
      val date  = LocalDate.of(2022,2,11)

      val data = Map(
        "effectiveDate.day"   -> "11",
        "effectiveDate.month" -> "2",
        "effectiveDate.year"  -> "2022"
      )
      val result = formForResidential.bind(data)
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

      val result = formForResidential.bind(data)
        result.errors must contain only FormError("effectiveDate", "effectiveDate.error.invalid", List())
      }
    }
    "return an error message when year is not 4 digits" in {

      val invalidDates = Seq(("33", "3", "202"),("2", "14", "20221"))

      invalidDates.map { invalidDate =>

      val data = Map(
        "effectiveDate.day"   -> invalidDate._1,
        "effectiveDate.month" -> invalidDate._2,
        "effectiveDate.year"  -> invalidDate._3
      )

      val result = formForResidential.bind(data)
        result.errors must contain(FormError("effectiveDate", "error.year.invalid", List()))
      }
    }
    "return an error message when year is before the 22/03/2012" in {

      val data = Map(
        "effectiveDate.day"   -> "21",
        "effectiveDate.month" -> "3",
        "effectiveDate.year"  -> "2012"
      )
      val result = formForResidential.bind(data)
      result.errors.exists { e =>
        e.key == "effectiveDate" &&
          e.messages.contains("effectiveDate.error.residentialMinDate") &&
          e.args.contains(LocalDate.of(2012, 3, 22))
      } mustBe true
    }
}