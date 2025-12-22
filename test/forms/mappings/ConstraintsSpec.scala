/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.mappings

import config.CurrencyFormatter
import generators.Generators
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.validation.{Invalid, Valid}

import java.time.LocalDate

class ConstraintsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators  with Constraints {


  "firstError" - {

    "must return Valid when all constraints pass" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("foo")
      result mustEqual Valid
    }

    "must return Invalid when the first constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }

    "must return Invalid when the second constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.regexp", """^\w+$""")
    }

    "must return Invalid for the first error when both constraints fail" in {
      val result = firstError(maxLength(-1, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.length", -1)
    }
  }

  "minimumValue" - {

    "must return Valid for a number greater than the threshold" in {
      val result = minimumValue(1, "error.min").apply(2)
      result mustEqual Valid
    }

    "must return Valid for a number equal to the threshold" in {
      val result = minimumValue(1, "error.min").apply(1)
      result mustEqual Valid
    }

    "must return Invalid for a number below the threshold" in {
      val result = minimumValue(1, "error.min").apply(0)
      result mustEqual Invalid("error.min", 1)
    }
  }

  "maximumValue" - {

    "must return Valid for a number less than the threshold" in {
      val result = maximumValue(1, "error.max").apply(0)
      result mustEqual Valid
    }

    "must return Valid for a number equal to the threshold" in {
      val result = maximumValue(1, "error.max").apply(1)
      result mustEqual Valid
    }

    "must return Invalid for a number above the threshold" in {
      val result = maximumValue(1, "error.max").apply(2)
      result mustEqual Invalid("error.max", 1)
    }
  }

  "inRange" - {

    "must return valid for value within in range" in {
      val result = inRange(1, 10, "error.range").apply(5)
      result mustEqual(Valid)
    }

    "must return invalid for value below minimum" in {
      val result = inRange(1, 10, "error.range").apply(0)
      result mustEqual Invalid("error.range", 1, 10)
    }

    "must return invalid for value above maximum" in {
      val result = inRange(1, 10, "error.range").apply(11)
      result mustEqual Invalid("error.range", 1, 10)
    }

  }

  "regexp" - {

    "must return Valid for an input that matches the expression" in {
      val result = regexp("""^\w+$""", "error.invalid")("foo")
      result mustEqual Valid
    }

    "must return Invalid for an input that does not match the expression" in {
      val result = regexp("""^\d+$""", "error.invalid")("foo")
      result mustEqual Invalid("error.invalid", """^\d+$""")
    }
  }

  "optionalRegexp" - {

    "must return Valid for an input that matches the expression" in {
      val result = optionalRegexp("""^\w+$""", "error.invalid")(Some("foo"))
      result mustEqual Valid
    }

    "must return Valid for an empty input" in {
      val result = optionalRegexp("""^\w+$""", "error.invalid")(None)
      result mustEqual Valid
    }

    "must return Invalid for an input that does not match the expression" in {
      val result = optionalRegexp("""^\d+$""", "error.invalid")(Some("foo"))
      result mustEqual Invalid("error.invalid", """^\d+$""")
    }
  }

  "maxLength" - {

    "must return Valid for a string shorter than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 9)
      result mustEqual Valid
    }

    "must return Valid for an empty string" in {
      val result = maxLength(10, "error.length")("")
      result mustEqual Valid
    }

    "must return Valid for a string equal to the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 10)
      result mustEqual Valid
    }

    "must return Invalid for a string longer than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }
  }

  "maxDate" - {

    "must return Valid for a date before or equal to the maximum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        max  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(LocalDate.of(2000, 1, 1), max)
      } yield (max, date)

      forAll(gen) {
        case (max, date) =>

          val result = maxDate(max, "error.future")(date)
          result mustEqual Valid
      }
    }

    "must return Invalid for a date after the maximum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        max  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(max.plusDays(1), LocalDate.of(3000, 1, 2))
      } yield (max, date)

      forAll(gen) {
        case (max, date) =>

          val result = maxDate(max, "error.future", "foo")(date)
          result mustEqual Invalid("error.future", "foo")
      }
    }
  }

  "minDate" - {

    "must return Valid for a date after or equal to the minimum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        min  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(min, LocalDate.of(3000, 1, 1))
      } yield (min, date)

      forAll(gen) {
        case (min, date) =>

          val result = minDate(min, "error.past", "foo")(date)
          result mustEqual Valid
      }
    }

    "must return Invalid for a date before the minimum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        min  <- datesBetween(LocalDate.of(2000, 1, 2), LocalDate.of(3000, 1, 1))
        date <- datesBetween(LocalDate.of(2000, 1, 1), min.minusDays(1))
      } yield (min, date)

      forAll(gen) {
        case (min, date) =>

          val result = minDate(min, "error.past", "foo")(date)
          result mustEqual Invalid("error.past", "foo")
      }
    }
  }


  "minimumCurrency" - {

    "must return Valid for a number greater than the threshold" in {
      val result = minimumCurrency(1, "error.min").apply(BigDecimal(1.01))
      result mustEqual Valid
    }

    "must return Valid for a number equal to the threshold" in {
      val result = minimumCurrency(1, "error.min").apply(1)
      result mustEqual Valid
    }

    "must return Invalid for a number below the threshold" in {
      val result = minimumCurrency(1, "error.min").apply(0.99)
      result mustEqual Invalid("error.min", CurrencyFormatter.currencyFormat(1))
    }
  }

  "maximumCurrency" - {

    "must return Valid for a number less than the threshold" in {
      val result = maximumCurrency(1, "error.max").apply(0)
      result mustEqual Valid
    }

    "must return Valid for a number equal to the threshold" in {
      val result = maximumCurrency(1, "error.max").apply(1)
      result mustEqual Valid
    }

    "must return Invalid for a number above the threshold" in {
      val result = maximumCurrency(1, "error.max").apply(1.01)
      result mustEqual Invalid("error.max", CurrencyFormatter.currencyFormat(1))
    }
  }

  "validUtr" - {
    val validUtrString: Seq[String] = Seq("1111111111","9570845180")
    val invalidUtrStrings: Seq[String] = Seq("1234567899","5570845180")
    val invalidUtrLength: Seq[String] = Seq("12134", "2343243", "3123123123123123", "42342342342342342323423423")
    val invalidUtrRegex: Seq[String] = Seq("23423khjbh", "agagagagag", "12--------", "          ")
    val invalidUtrMultipleErrors: String = "12ab"

    "must return Valid for a valid UTR number" in {
      validUtrString.foreach { utr =>
        val result = validUtr("error.utr")(utr)
        result mustBe Valid
      }
    }

    "must return Invalid for an invalid UTR number" in {
      invalidUtrStrings.foreach{ utr =>
        val result = validUtr("error.utr")(utr)
        result mustBe Invalid("error.utr.invalid")
      }
    }

    "must return Invalid for numbers more or less than 10 digits" in {
      invalidUtrLength.foreach { utr =>
        val result = validUtr("error.utr")(utr)
        result mustBe Invalid("error.utr.length", 10)
      }
    }

    "must return Invalid for UTR values with no digits" in {
      invalidUtrRegex.foreach { utr =>
        val result = validUtr("error.utr")(utr)
        result mustBe Invalid("error.utr.regex.invalid", "^[0-9]*$")
      }
    }

    "must return Invalid and first error message for UTR values with multiple errors" in {
      val result = validUtr("error.utr")(invalidUtrMultipleErrors)
      result mustBe Invalid("error.utr.regex.invalid", "^[0-9]*$")
    }
  }

  "vatChecksumMod97Len9" -  {
    val validVATString: Seq[String] = Seq("438573857","438573857", "438573857")
    val invalidVatStrings: Seq[String] = Seq("123456789","987654321")
    val invalidVatLength: Seq[String] = Seq("12134", "2343243", "3123123123123123", "42342342342342342323423423")
    val invalidVatRegex: Seq[String] = Seq("12345678H", "ZXCVBNMLK", "12-------", "          ")
    val invalidVatMultipleErrors: String = "12ab"

    "must return Valid for a valid vat number" in {
      validVATString.foreach { vat =>
        val result = vatCheckF16Validation("purchaser.registrationNumber.error")(vat)
        result mustBe Valid
      }
    }

    "must return Invalid for an invalid vat number" in {
      invalidVatStrings.foreach{ vat =>
        val result = vatCheckF16Validation("purchaser.registrationNumber.error")(vat)
        result mustBe Invalid("purchaser.registrationNumber.error.invalid")
      }
    }

    "must return Invalid for numbers more or less than 9 digits" in {
      invalidVatLength.foreach { vat =>
        val result = vatCheckF16Validation("purchaser.registrationNumber.error")(vat)
        result mustBe Invalid("purchaser.registrationNumber.error.length", 9)
      }
    }

    "must return Invalid for VAT values with no digits" in {
      invalidVatRegex.foreach { vat =>
        val result = vatCheckF16Validation("purchaser.registrationNumber.error")(vat)
        result mustBe Invalid("purchaser.registrationNumber.error.regex.invalid", "^[0-9]*$")
      }
    }

    "must return Invalid and first error message for VAT values with multiple errors" in {
      val result = validUtr("purchaser.registrationNumber.error")(invalidVatMultipleErrors)
      result mustBe Invalid("purchaser.registrationNumber.error.regex.invalid", "^[0-9]*$")
    }
  }
  
  "maxCheckboxes" - {
    "must return Valid for a number less than the threshold" in {
      val result = maxCheckboxes(4, "error.max").apply(Set(1, 2, 3))
      result mustEqual Valid
    }

    "must return Invalid for a number more than the threshold" in {
      val result = maxCheckboxes(2, "error.max").apply(Set(1, 2, 3))
        result mustBe Invalid("error.max")
    }
  }
}
