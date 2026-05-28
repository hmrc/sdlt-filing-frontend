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

import scala.util.Try

trait CurrencyFormatter {
  def currencyFormat(amt: BigDecimal): String = f"£$amt%,1.2f".replace(".00","")

  implicit class IntToCurrency(amt: Int) {
    def toCurrency: String = currencyFormat(BigDecimal(amt))
  }

  implicit class BigDecimalToCurrency(amt: BigDecimal) {
    def toCurrency: String = currencyFormat(amt)
  }

  implicit class StringToCurrency(amt: String) {
    def toCurrency: Option[String] = Try(BigDecimal(amt)).toOption.map(currencyFormat)
  }

  implicit class IntToPercentage(amt: Int) {
    def toPercentage: String = s"$amt%"
  }
}

object CurrencyFormatter extends CurrencyFormatter
