/*
 * Copyright 2026 HM Revenue & Customs
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

package services.pdf

import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar
import services.pdf.PdfFormSupport.*

class PdfFormSupportSpec extends SpecBase with MockitoSugar {

  "PdfFormSupport" - {

    ".splitPostcode" - {

      "must return postcode string in 2 parts when input contains a space" in {
        val result = splitPostcode(Some("AA00 0BB"))
        result mustBe (Some("AA00"), Some("0BB"))
      }

      "must return postcode string in 1 part when input does not contain a space" in {
        val result = splitPostcode(Some("AA000BB"))
        result mustBe(Some("AA000BB"), None)
      }

      "must return a tuple with None when empty input provided" in {
        val result = splitPostcode(Some(""))
        result mustBe(None, None)
      }

      "must return a tuple with None when no input provided" in {
        val result = splitPostcode(None)
        result mustBe(None, None)
      }
    }

    ".splitLines" - {

      "must return a string in 2 parts when input is greater than max length" in {
        val result = splitLines(Some("first second"), 7)
        result mustBe(Some("first"), Some("second"))
      }

      "must return a string in 2 parts when input is greater than max length and there is a space at the max length" in {
        val result = splitLines(Some("first second"), 6)
        result mustBe(Some("first"), Some("second"))
      }

      "must return a string in 1 part when input is less than max length" in {
        val result = splitLines(Some("first second"), 12)
        result mustBe(Some("first second"), None)
      }

      "must return a string in 2 parts when input contains no spaces and input is greater than max length" in {
        val result = splitLines(Some("first"), 3)
        result mustBe(Some("fir"), Some("st"))
      }

      "must return a string in 1 part when input contains no spaces and word length exceeds max length" in {
        val result = splitLines(Some("first"), 6)
        result mustBe(Some("first"), None)
      }

      "must return tuple with None when input is empty" in {
        val result = splitLines(Some(""), 6)
        result mustBe(None, None)
      }

      "must return tuple with None when input is None" in {
        val result = splitLines(None, 6)
        result mustBe(None, None)
      }

      "must correctly split a string with multiple spaces" in {
        val result = splitLines(Some("first second third fourth"), 15)
        result mustBe(Some("first second"), Some("third fourth"))
      }
    }

    ".splitNino" - {

      "must split a standard NINO with spaces into five boxes (2/2/2/2/1)" in {
        val result = splitNino(Some("AB 123456 C"))
        result mustBe (Some("AB"), Some("12"), Some("34"), Some("56"), Some("C"))
      }

      "must strip spaces before splitting" in {
        val result = splitNino(Some("AB123456C"))
        result mustBe (Some("AB"), Some("12"), Some("34"), Some("56"), Some("C"))
      }

      "must uppercase the input before splitting" in {
        val result = splitNino(Some("ab 123456 c"))
        result mustBe (Some("AB"), Some("12"), Some("34"), Some("56"), Some("C"))
      }

      "must return all Nones when nino is None" in {
        val result = splitNino(None)
        result mustBe (None, None, None, None, None)
      }

      "must return all Nones when nino is an empty string" in {
        val result = splitNino(Some(""))
        result mustBe (None, None, None, None, None)
      }

      "must return all Nones when nino is whitespace only" in {
        val result = splitNino(Some("   "))
        result mustBe (None, None, None, None, None)
      }

      "must handle a short string without throwing" in {
        val result = splitNino(Some("AB"))
        result mustBe (Some("AB"), None, None, None, None)
      }
    }
  }
}
