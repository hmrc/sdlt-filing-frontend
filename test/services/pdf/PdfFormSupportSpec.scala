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

      "must return a string in 1 part when input contains no spaces" in {
        val result = splitLines(Some("first"), 5)
        result mustBe(Some("first"), None)
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
  }
}
