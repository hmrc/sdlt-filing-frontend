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

package models

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class FullReturnSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  private val validPrelimReturnJson = Json.obj(
    "stornId" -> "12345",
    "purchaserIsCompany" -> "YES",
    "surNameOrCompanyName" -> "Test Company",
    "houseNumber" -> 23,
    "addressLine1" -> "Test Street",
    "addressLine2" -> JsNull,
    "addressLine3" -> JsNull,
    "addressLine4" -> JsNull,
    "postcode" -> "TE23 5TT",
    "transactionType" -> "O"
  )

  private val validVendorReturnJson = Json.obj(
    "stornId" -> "12345",
    "purchaserIsCompany" -> "YES",
    "surNameOrCompanyName" -> "Test Company",
    "houseNumber" -> 23,
    "addressLine1" -> "Test Street",
    "addressLine2" -> JsNull,
    "addressLine3" -> JsNull,
    "addressLine4" -> JsNull,
    "postcode" -> "TE23 5TT",
    "transactionType" -> "O"
  )

  private val validPrelimReturn = Json.fromJson[PrelimReturn](validPrelimReturnJson).asOpt.get
  private val validVendorReturn = Json.fromJson[VendorReturn](validVendorReturnJson).asOpt.get

  "FullReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[FullReturn]]
      }

      "must deserialize valid JSON with Some(prelimReturn)" in {
        val json = Json.obj(
          "prelimReturn" -> validPrelimReturnJson
        )

        val result = Json.fromJson[FullReturn](json).asEither.value

        result.prelimReturn mustBe defined
      }

      "must deserialize valid JSON with None prelimReturn" in {
        val json = Json.obj(
          "prelimReturn" -> JsNull
        )

        val result = Json.fromJson[FullReturn](json).asEither.value

        result.prelimReturn must not be defined
      }

      "must deserialize successfully when prelimReturn field is missing and set to None" in {
        val json = Json.obj()

        val result = Json.fromJson[FullReturn](json).asEither.value

        result.prelimReturn must not be defined
      }

      "must deserialize successfully when only invalid fields present and set prelimReturn to None" in {
        val json = Json.obj(
          "invalidField" -> "value"
        )

        val result = Json.fromJson[FullReturn](json).asEither.value

        result.prelimReturn must not be defined
      }

      "must fail to deserialize when prelimReturn is invalid type" in {
        val json = Json.obj(
          "prelimReturn" -> "invalid"
        )

        val result = Json.fromJson[FullReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when prelimReturn has invalid structure" in {
        val json = Json.obj(
          "prelimReturn" -> Json.obj("invalidField" -> "value")
        )

        val result = Json.fromJson[FullReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when prelimReturn is missing required fields" in {
        val json = Json.obj(
          "prelimReturn" -> Json.obj(
            "stornId" -> "12345"
          )
        )

        val result = Json.fromJson[FullReturn](json).asEither

        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[FullReturn]]
      }

      "must serialize FullReturn with Some(prelimReturn)" in {
        val fullReturn = FullReturn(Some(validPrelimReturn), Some(validVendorReturn))

        val json = Json.toJson(fullReturn)

        (json \ "prelimReturn").isDefined mustBe true
        (json \ "prelimReturn").get must not equal JsNull
      }

      "must serialize FullReturn with None prelimReturn" in {
        val fullReturn = FullReturn(None, None)

        val json = Json.toJson(fullReturn)

        val prelimReturnValue = (json \ "prelimReturn").toOption

        prelimReturnValue match {
          case Some(JsNull) => succeed
          case None => succeed
          case Some(other) => fail(s"Expected JsNull or missing field, but got $other")
        }
      }

      "must produce valid JSON structure" in {
        val fullReturn = FullReturn(Some(validPrelimReturn), Some(validVendorReturn))

        val json = Json.toJson(fullReturn)

        json mustBe a[JsObject]
        (json \ "prelimReturn").isDefined mustBe true
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[FullReturn]]
      }

      "must serialize and deserialize with Some(prelimReturn)" in {
        val fullReturn = FullReturn(Some(validPrelimReturn), Some(validVendorReturn))

        val json = Json.toJson(fullReturn)
        val result = Json.fromJson[FullReturn](json).asEither.value

        result.prelimReturn mustBe defined
        result mustEqual fullReturn
      }

      "must serialize and deserialize with None prelimReturn" in {
        val fullReturn = FullReturn(None, None)

        val json = Json.toJson(fullReturn)
        val result = Json.fromJson[FullReturn](json).asEither.value

        result.prelimReturn must not be defined
        result mustEqual fullReturn
      }
    }

    "case class" - {

      "must create instance with Some(prelimReturn)" in {
        val fullReturn = FullReturn(Some(validPrelimReturn), Some(validVendorReturn))

        fullReturn.prelimReturn mustBe Some(validPrelimReturn)
      }

      "must create instance with None prelimReturn" in {
        val fullReturn = FullReturn(None, None)

        fullReturn.prelimReturn mustBe None
      }

      "must support equality" in {
        val fullReturn1 = FullReturn(None, None)
        val fullReturn2 = FullReturn(None, None)

        fullReturn1 mustEqual fullReturn2
      }

      "must support equality with Some values" in {
        val fullReturn1 = FullReturn(Some(validPrelimReturn), Some(validVendorReturn))
        val fullReturn2 = FullReturn(Some(validPrelimReturn), Some(validVendorReturn))

        fullReturn1 mustEqual fullReturn2
      }

      "must support copy" in {
        val fullReturn1 = FullReturn(None, None)
        val fullReturn2 = fullReturn1.copy(prelimReturn = Some(validPrelimReturn), vendorReturn = Some(validVendorReturn))

        fullReturn2.prelimReturn mustBe defined
        fullReturn1.prelimReturn must not be defined
      }

      "must not be equal when prelimReturn differs" in {
        val fullReturn1 = FullReturn(None, None)
        val fullReturn2 = FullReturn(Some(validPrelimReturn), Some(validVendorReturn))

        fullReturn1 must not equal fullReturn2
      }
    }
  }
}