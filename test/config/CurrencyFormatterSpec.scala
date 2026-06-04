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

package config

import config.CurrencyFormatter.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class CurrencyFormatterSpec extends AnyFreeSpec with Matchers {

  ".currencyFormat" - {

    "drops the .00 for whole-pound amounts" in {
      currencyFormat(BigDecimal(125000)) mustEqual "£125,000"
    }

    "keeps pence when present" in {
      currencyFormat(BigDecimal("1499.50")) mustEqual "£1,499.50"
    }

    "groups thousands with commas" in {
      currencyFormat(BigDecimal(1234567)) mustEqual "£1,234,567"
    }

    "rounds to two decimal places" in {
      currencyFormat(BigDecimal("10.005")) mustEqual "£10.01"
    }

    "handles zero" in {
      currencyFormat(BigDecimal(0)) mustBe "£0"
    }

    "handles negatives" in {
      currencyFormat(BigDecimal(-250)) mustBe "£-250"
    }
  }

  ".toCurrency" - {

    "works on Int" in {
      125000.toCurrency mustEqual "£125,000"
    }

    "works on BigDecimal" in {
      BigDecimal("999.99").toCurrency mustEqual "£999.99"
    }

    "strips the .00 suffix from a decimal string" in {
      "500000.00".toCurrency mustEqual "£500,000"
    }

    "formats a whole-number string" in {
      "500000".toCurrency mustEqual "£500,000"
    }

    "groups thousands with commas" in {
      "1234567.00".toCurrency mustEqual "£1,234,567"
    }

    "returns the original string for a non-numeric string" in {
      "my-name-is-jeff".toCurrency mustEqual "my-name-is-jeff"
    }

    "returns the original string for an empty string" in {
      "".toCurrency mustEqual ""
    }
  }

  ".toPercentage" - {

    "appends a percent sign" in {
      5.toPercentage mustBe "5%"
    }

    "handles zero" in {
      0.toPercentage mustBe "0%"
    }
  }
}
