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

package models.vendor

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json.{JsNull, JsObject, Json, Reads}

class VendorSessionQuestionsSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  val companyName = VendorName(None, None, "Samsung")

  object TestData {
    def VendorSessionQuestionsJsonComplete: JsObject = Json.obj(
      "vendorCurrent" -> Json.obj(
        "vendorID" -> "VE002",
        "whoIsTheVendor" -> "Company",
        "vendorOrCompanyName" -> Json.obj(
          "forename1" -> JsNull,
          "forename2" -> JsNull,
          "name" -> "Samsung",
        ),
        "vendorAddress" -> Json.obj(
          "houseNumber" -> "1",
          "line1" -> "Street 1",
          "line2" -> "Street 2",
          "line3" -> "Street 3",
          "line4" -> "Street 4",
          "line5" -> "Street 5" ,
          "postcode" ->  "CR7 8LU",
          "country" -> Json.obj(
            "code" -> "GB",
            "name" -> "UK"
          ),
          "addressValidated" -> true
        ),
        "representedByAnAgent" -> "Yes"
      ))

    def VendorSessionQuestionsJsonWithNoOptional: JsObject = Json.obj(
      "vendorCurrent" -> Json.obj(
      "vendorID" -> JsNull,
      "whoIsTheVendor" -> "Company",
      "vendorOrCompanyName" -> Json.obj(
        "forename1" -> JsNull,
        "forename2" -> JsNull,
        "name" -> "Samsung",
      ),
      "vendorAddress" -> Json.obj(
        "houseNumber" -> None,
        "line1" -> "Street 1",
        "line2" -> JsNull,
        "line3" -> JsNull,
        "line4" -> JsNull,
        "line5" -> JsNull ,
        "postcode" ->  JsNull,
        "country" -> JsNull,
        "addressValidated" -> JsNull
      ),
      "representedByAnAgent" -> JsNull
    ))
  }

  "VendorSessionQuestions" - {

    ".reads" -{

      "must be found implicitly" in {
        implicitly[Reads[VendorSessionQuestions]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[VendorSessionQuestions](TestData.VendorSessionQuestionsJsonComplete).asEither.value

        val expectedResult: VendorSessionQuestions =
          VendorSessionQuestions(VendorCurrent(
            vendorID = Some("VE002"),
            whoIsTheVendor = "Company",
            vendorOrCompanyName = companyName,
            vendorAddress = VendorSessionAddress(
              houseNumber = Some("1"),
              line1 = Some("Street 1"),
              line2 = Some("Street 2"),
              line3 = Some("Street 3"),
              line4 = Some("Street 4"),
              line5 = Some("Street 5"),
              postcode = Some("CR7 8LU"),
              country = Some(VendorSessionCountry(
                code = Some("GB"),
                name = Some("UK")
              )),
              addressValidated = Some(true)
            )
          ))

        result shouldBe expectedResult
      }

      "must deserialize valid JSON with minimal fields" in {
        val result = Json.fromJson[VendorSessionQuestions](TestData.VendorSessionQuestionsJsonWithNoOptional).asEither.value

        val expectedResult: VendorSessionQuestions =
          VendorSessionQuestions(VendorCurrent(
            vendorID = None,
            whoIsTheVendor = "Company",
            vendorOrCompanyName = companyName,
            vendorAddress = VendorSessionAddress(
              houseNumber = None,
              line1 = Some("Street 1"),
              line2 = None,
              line3 = None,
              line4 = None,
              line5 = None,
              postcode = None,
              country = None,
              addressValidated = None
            )
          ))

        result shouldBe expectedResult
      }

      "must deserialize valid JSON for createVendorReturn" in {
        val validVendorReturnJson: JsObject = Json.obj(
          "vendorResourceRef" -> "VEN-REF-001",
          "vendorId" -> "VEN001"
        )
        
        val result = Json.fromJson[CreateVendorReturn](validVendorReturnJson).asEither.value

        result.vendorResourceRef mustBe "VEN-REF-001"
        result.vendorId mustBe "VEN001"
      }

      "must deserialize valid JSON for updateVendorReturn" in {
        val validUpdateVendorReturnJson: JsObject = Json.obj(
          "updated" -> true
        )

        val result = Json.fromJson[UpdateVendorReturn](validUpdateVendorReturnJson).asEither.value

        result.updated mustBe true
      }
    }
  }

}

