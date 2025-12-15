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
import play.api.data.validation.Constraints.minLength
import play.api.data.validation.{Constraint, Invalid, Valid}

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

trait Constraints {

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev.*

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, minimum)
        }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev.*

        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, maximum)
        }
    }

  protected def inRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev.*

        if (input >= minimum && input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, minimum, maximum)
        }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def optionalRegexp(regex: String, errorKey: String): Constraint[Option[String]] =
    Constraint {
      case Some(str) if str.matches(regex) =>
        Valid
      case None => Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def optionalMaxLength(maximum: Int, errorKey: String): Constraint[Option[String]] =
    Constraint {
      case Some(str) if str.length > maximum => Invalid(errorKey, maximum)
      case _ => Valid
    }

  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def nonEmptySet(errorKey: String): Constraint[Set[_]] =
    Constraint {
      case set if set.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  protected def minimumCurrency(minimum: BigDecimal, errorKey: String)(implicit ev: Ordering[BigDecimal]): Constraint[BigDecimal] =
    Constraint {
      input =>
        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, CurrencyFormatter.currencyFormat(minimum))
        }
    }

  protected def maximumCurrency(maximum: BigDecimal, errorKey: String)(implicit ev: Ordering[BigDecimal]): Constraint[BigDecimal] = {
    Constraint {
      input =>
        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, CurrencyFormatter.currencyFormat(maximum))
        }
    }
  }

  protected def validUtr(errorKey: String): Constraint[String] = {

    def checkSum(errorKey: String): Constraint[String] = {
      val weights: Seq[Int] = Seq(6, 7, 8, 9, 10, 5, 4, 3, 2)

      def validateCheckSum(utr: String) = Try {
        val utrInts: Seq[Int] = utr.map(_.asDigit)
        val utrSum: Int = utrInts.slice(1, 10).zip(weights).map { case (x, y) => x * y }.sum
        val utrCalc = 11 - (utrSum % 11)
        val checkSum = if (utrCalc > 9) utrCalc - 9 else utrCalc
        checkSum == utrInts.head
      } match {
        case Success(s) => s
        case Failure(_) => false
      }

      Constraint { str =>
        if (validateCheckSum(str)) Valid else Invalid(errorKey)
      }
    }

    firstError(
      regexp("^[0-9]*$", s"${errorKey}.regex.invalid"),
      minLength(10, s"${errorKey}.length"),
      maxLength(10, s"${errorKey}.length"),
      checkSum(s"${errorKey}.invalid")
    )
  }

  protected def vatCheckF16Validation(errorKey: String): Constraint[String] = {

    def vatF16Check(errorKey: String): Constraint[String] = {

      def validateVAT(raw: String): Boolean = Try {
        // ---- Normalization requested: drop "GB" and spaces ----
        val normalized = {
          val up = Option(raw).getOrElse("").trim.toUpperCase
          val noGb = if (up.startsWith("GB")) up.drop(2) else up
          noGb.replaceAll("\\s+", "")
        }
        val bodyDigits       = normalized.substring(0, 7)
        val checkDigitsValue = normalized.substring(7, 9).toInt

        val weights = Array(8, 7, 6, 5, 4, 3, 2)
        val sum = bodyDigits
          .map(_.asDigit)
          .zip(weights)
          .map { case (digit, weight) => digit * weight }
          .sum

        val check1 = 97 - Math.floorMod(sum, 97)
        val check2 = 97 - Math.floorMod(sum + 55, 97)

        checkDigitsValue >= 0 &&
          checkDigitsValue <= 97 &&
          (checkDigitsValue == check1 || checkDigitsValue == check2)
      } match {
        case Success(isValid) => isValid
        case Failure(_)       => false
      }

      Constraint[String] { str =>
        // Only returns Valid/Invalid; no extra format/length checks here
        if (validateVAT(str)) Valid else Invalid(errorKey)
      }
    }

    firstError(
      regexp("^[0-9]*$", s"${errorKey}.regex.invalid"),
      minLength(9, s"${errorKey}.length"),
      maxLength(9, s"${errorKey}.length"),
      vatF16Check(s"${errorKey}.invalid")
    )
  }

}
