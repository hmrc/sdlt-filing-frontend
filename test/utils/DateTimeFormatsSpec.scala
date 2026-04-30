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

package utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessages
import utils.DateTimeFormats.{asDate, dateTimeFormat, dateTimeHintFormat}

import java.time.LocalDate

class DateTimeFormatsSpec extends AnyFreeSpec with Matchers {

  private implicit val messages: Messages = stubMessages()

  ".dateTimeFormat" - {

    "must format dates in English" in {
      val formatter = dateTimeFormat()(Lang("en"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 January 2023"
    }

    "must format dates in Welsh" in {
      val formatter = dateTimeFormat()(Lang("cy"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 Ionawr 2023"
    }

    "must default to English format" in {
      val formatter = dateTimeFormat()(Lang("de"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 January 2023"
    }
  }

  ".dateTimeHintFormat" - {

    "renders single-digit day and month without padding" in {
      LocalDate.of(2024, 3, 7).format(dateTimeHintFormat) mustEqual "7 3 2024"
    }
  }

  ".asDate" - {

    "formats an ISO date string into the long English form" in {
      asDate("2024-07-15") mustEqual "15 July 2024"
    }

    "returns the input unchanged when it isn't a parseable date" in {
      asDate("not-a-date") mustEqual "not-a-date"
    }

    "passes empty strings straight through" in {
      asDate("") mustBe ""
    }
  }
}
