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

import models.Enumerable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.FormError


class FormatterSpec extends AnyFreeSpec with Matchers with Formatters {

  "stringFormatter" - {

    val result = stringFormatter("error")

    "must bind a non-empty string" in {
      result.bind("key", Map("key" -> "value")) mustEqual Right("value")
    }

    "must return error when key is missing" in {
      result.bind("key", Map.empty) mustEqual Left(Seq(FormError("key", "error", Seq.empty)))
    }

    "must return error when value is empty" in {
      result.bind("key", Map("key" -> "")) mustEqual Left(Seq(FormError("key", "error", Seq.empty)))
    }
  }

  "optionalStringFormat" - {

    val optionalString = optionalStringFormat

    "mustReturn Some for a non-empty string" in {
      optionalString.bind("key", Map("key" -> "value")) mustEqual Right(Some("value"))
    }

    "must return None for empty string" in {
      optionalString.bind("key", Map("key" -> "")) mustEqual Right(None)
    }

    "must return None if key is missing" in {
      optionalString.bind("key", Map.empty) mustEqual Right(None)
    }
  }

  "booleanFormatter" - {

    val boolean = booleanFormatter("required", "invalid")

    "must bind 'true' correctly" in {
      boolean.bind("key", Map("key" -> "true")) mustEqual Right(true)
    }

    "must bind 'false' correctly" in {
      boolean.bind("key", Map("key" -> "false")) mustEqual Right(false)
    }

    "must return error for invalid string" in {
      boolean.bind("key", Map("key" -> "foo")) mustEqual Left(Seq(FormError("key", "invalid", Seq.empty)))
    }

    "intFormatter" - {

      val intFmt = intFormatter("required", "wholeNumber", "nonNumeric")

      "must bind valid integers" in {
        intFmt.bind("key", Map("key" -> "123")) mustEqual Right(123)
      }

      "must return error for decimals" in {
        intFmt.bind("key", Map("key" -> "12.3")) mustEqual Left(Seq(FormError("key", "wholeNumber", Seq.empty)))
      }

      "must return error for non-numeric" in {
        intFmt.bind("key", Map("key" -> "abc")) mustEqual Left(Seq(FormError("key", "nonNumeric", Seq.empty)))
      }
    }

    "enumerableFormatter" - {

      sealed trait TestEnum
      case object A extends TestEnum
      case object B extends TestEnum
      implicit val ev: Enumerable[TestEnum] = Enumerable(
        "A" -> A,
        "B" -> B
      )

      val enumFmt = enumerableFormatter[TestEnum]("required", "invalid")

      "must bind valid enum values" in {
        enumFmt.bind("key", Map("key" -> "A")) mustEqual Right(A)
      }

      "must return error for invalid enum value" in {
        enumFmt.bind("key", Map("key" -> "C")) mustEqual Left(Seq(FormError("key", "invalid", Seq.empty)))
      }
    }

    "currencyFormatter" - {

      val currencyFmt = currencyFormatter("required", "invalidNumeric", "nonNumeric")

      "must bind valid currency with pound sign" in {
        currencyFmt.bind("key", Map("key" -> "£123.45")) mustEqual Right(BigDecimal(123.45))
      }

      "must bind valid numeric string without pound sign" in {
        currencyFmt.bind("key", Map("key" -> "123")) mustEqual Right(BigDecimal(123))
      }

      "must return error for malformed string" in {
        currencyFmt.bind("key", Map("key" -> "abc")) mustEqual Left(Seq(FormError("key", "nonNumeric", Seq.empty)))
      }

      "must return error for invalid decimal (more than 2 places)" in {
        currencyFmt.bind("key", Map("key" -> "123.456")) mustEqual Left(Seq(FormError("key", "invalidNumeric", Seq.empty)))
      }
    }

    "areaOfLandFormatter" - {

      "when unit type is square metres" - {

        val areaOfLandFmt = areaOfLandFormatter("SQMETRE", "required", "invalid", "invalidLength")

        "must bind valid numeric string with trailing zeros" in {
          areaOfLandFmt.bind("key", Map("key" -> "123.00")) mustEqual Right("123.000")
        }

        "must bind valid integer string" in {
          areaOfLandFmt.bind("key", Map("key" -> "123")) mustEqual Right("123.000")
        }

        "must return error for malformed string" in {
          areaOfLandFmt.bind("key", Map("key" -> "abc")) mustEqual Left(Seq(FormError("key", "invalid", Seq.empty)))
        }

        "must return error for numeric string with non zero decimal places" in {
          areaOfLandFmt.bind("key", Map("key" -> "123.1")) mustEqual Left(Seq(FormError("key", "invalid", Seq.empty)))
        }

        "must return error for invalid string length" in {
          areaOfLandFmt.bind("key", Map("key" -> "123456789012345")) mustEqual Left(Seq(FormError("key", "invalidLength", Seq.empty)))
        }
      }

      "when unit type is hectares" - {

        val areaOfLandFmt = areaOfLandFormatter("HECTARES", "required", "invalid", "invalidLength")

        "must bind valid numeric string with 3 decimal places" in {
          val result = areaOfLandFmt.bind("key", Map("key" -> "123.456"))
          result mustEqual Right("123.456")
        }

        "must bind valid numeric string with 2 decimal places" in {
          val result = areaOfLandFmt.bind("key", Map("key" -> "123.45"))
          result mustEqual Right("123.450")
        }

        "must bind valid integer string" in {
          val result = areaOfLandFmt.bind("key", Map("key" -> "123"))
          result mustEqual Right("123.000")
        }

        "must return error for malformed string" in {
          areaOfLandFmt.bind("key", Map("key" -> "abc")) mustEqual Left(Seq(FormError("key", "invalid", Seq.empty)))
        }

        "must return error for numeric string with more than 3 decimal places" in {
          areaOfLandFmt.bind("key", Map("key" -> "123.4567")) mustEqual Left(Seq(FormError("key", "invalid", Seq.empty)))
        }

        "must return error for invalid string length" in {
          areaOfLandFmt.bind("key", Map("key" -> "1234567890123.456")) mustEqual Left(Seq(FormError("key", "invalidLength", Seq.empty)))
        }
      }
    }
  }
}
