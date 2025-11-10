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

import models.vendor.VendorReturn
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json.*

class VendorReturnSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  private val validVendorReturnJsonComplete = Json.obj(
    "stornId" -> "12345",
    "returnResourceRef" -> "45678",
    "title" -> "Mr",
    "forename1" -> "A",
    "forename2" -> "B",
    "surName" -> "Test",
    "houseNumber" -> 23,
    "addressLine1" -> "Test Street",
    "addressLine2" -> "Apartment 5",
    "addressLine3" -> "Building A",
    "addressLine4" -> "District B",
    "postcode" -> "TE23 5TT",
    "isRepresentedByAgent" -> "YES"
  )

  private val validVendorReturnJsonMinimal = Json.obj(
    "stornId" -> "12345",
    "returnResourceRef" -> "45678",
    "title" -> "Mr",
    "forename1" -> "A",
    "surName" -> "Test",
    "addressLine1" -> "Test Street",
    "isRepresentedByAgent" -> "YES"
  )

  private val completeVendorReturn = VendorReturn(
    stornId = "12345",
    returnResourceRef = "45678",
    title = "Mr",
    forename1 = "A",
    forename2 = Some("B"),
    surName = "Test",
    houseNumber = Some(23),
    addressLine1 = "Test Street",
    addressLine2 = Some("Apartment 5"),
    addressLine3 = Some("Building A"),
    addressLine4 = Some("District B"),
    postcode = Some("TE23 5TT"),
    isRepresentedByAgent = "YES"
  )

  private val minimalVendorReturn = VendorReturn(
    stornId = "12345",
    returnResourceRef = "45678",
    title = "Mr",
    forename1 = "A",
    forename2 = None,
    surName = "Test",
    houseNumber = None,
    addressLine1 = "Test Street",
    addressLine2 = None,
    addressLine3 = None,
    addressLine4 = None,
    postcode = None,
    isRepresentedByAgent = "YES"
  )

  "VendorReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[VendorReturn]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[VendorReturn](validVendorReturnJsonComplete).asEither.value

        result.stornId mustBe "12345"
        result.returnResourceRef mustBe "45678"
        result.title mustBe "Mr"
        result.forename1 mustBe "A"
        result.forename2 mustBe Some("B")
        result.surName mustBe "Test"
        result.houseNumber mustBe Some(23)
        result.addressLine1 mustBe "Test Street"
        result.addressLine2 mustBe Some("Apartment 5")
        result.addressLine3 mustBe Some("Building A")
        result.addressLine4 mustBe Some("District B")
        result.postcode mustBe Some("TE23 5TT")
        result.isRepresentedByAgent mustBe "YES"
      }

      "must deserialize valid JSON with only required fields" in {
        val result = Json.fromJson[VendorReturn](validVendorReturnJsonMinimal).asEither.value

        result.stornId mustBe "12345"
        result.returnResourceRef mustBe "45678"
        result.title mustBe "Mr"
        result.forename1 mustBe "A"
        result.forename2 must not be defined
        result.surName mustBe "Test"
        result.houseNumber must not be defined
        result.addressLine1 mustBe "Test Street"
        result.addressLine2 must not be defined
        result.addressLine3 must not be defined
        result.addressLine4 must not be defined
        result.postcode must not be defined
        result.isRepresentedByAgent mustBe "YES"
      }

      "must deserialize JSON with null optional fields" in {
        val json = Json.obj(
          "stornId" -> "12345",
          "returnResourceRef" -> "45678",
          "title" -> "Mr",
          "forename1" -> "A",
          "forename2" -> "B",
          "surName" -> "Test",
          "houseNumber" -> JsNull,
          "addressLine1" -> "Test Street",
          "addressLine2" -> JsNull,
          "addressLine3" -> JsNull,
          "addressLine4" -> JsNull,
          "postcode" -> JsNull,
          "isRepresentedByAgent" -> "YES"
        )

        val result = Json.fromJson[VendorReturn](json).asEither.value

        result.houseNumber must not be defined
        result.addressLine2 must not be defined
        result.postcode must not be defined
      }

      "must fail to deserialize when stornId is missing" in {
        val json = validVendorReturnJsonComplete - "stornId"

        val result = Json.fromJson[VendorReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when isRepresentedByAgent is missing" in {
        val json = validVendorReturnJsonComplete - "isRepresentedByAgent"

        val result = Json.fromJson[VendorReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when surName is missing" in {
        val json = validVendorReturnJsonComplete - "surName"

        val result = Json.fromJson[VendorReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when addressLine1 is missing" in {
        val json = validVendorReturnJsonComplete - "addressLine1"

        val result = Json.fromJson[VendorReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when title is missing" in {
        val json = validVendorReturnJsonComplete - "title"

        val result = Json.fromJson[VendorReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when forename1 is missing" in {
        val json = validVendorReturnJsonComplete - "forename1"

        val result = Json.fromJson[VendorReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when houseNumber has invalid type" in {
        val json = validVendorReturnJsonComplete ++ Json.obj("houseNumber" -> "invalid")

        val result = Json.fromJson[VendorReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when required field has invalid type" in {
        val json = validVendorReturnJsonComplete ++ Json.obj("stornId" -> 123)

        val result = Json.fromJson[VendorReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize completely invalid JSON structure" in {
        val json = Json.obj("invalidField" -> "value")

        val result = Json.fromJson[VendorReturn](json).asEither

        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[VendorReturn]]
      }

      "must serialize VendorReturn with all fields" in {
        val json = Json.toJson(completeVendorReturn)

        (json \ "stornId").as[String] mustBe "12345"
        (json \ "returnResourceRef").as[String] mustBe "45678"
        (json \ "title").as[String] mustBe "Mr"
        (json \ "forename1").as[String] mustBe "A"
        (json \ "forename2").as[String] mustBe "B"
        (json \ "surName").as[String] mustBe "Test"
        (json \ "houseNumber").asOpt[Int] mustBe Some(23)
        (json \ "addressLine1").as[String] mustBe "Test Street"
        (json \ "addressLine2").asOpt[String] mustBe Some("Apartment 5")
        (json \ "addressLine3").asOpt[String] mustBe Some("Building A")
        (json \ "addressLine4").asOpt[String] mustBe Some("District B")
        (json \ "postcode").asOpt[String] mustBe Some("TE23 5TT")
        (json \ "isRepresentedByAgent").as[String] mustBe "YES"
      }

      "must serialize VendorReturn with only required fields" in {
        val json = Json.toJson(minimalVendorReturn)

        (json \ "stornId").as[String] mustBe "12345"
        (json \ "returnResourceRef").as[String] mustBe "45678"
        (json \ "title").as[String] mustBe "Mr"
        (json \ "forename1").as[String] mustBe "A"
        (json \ "surName").as[String] mustBe "Test"
        (json \ "addressLine1").as[String] mustBe "Test Street"
        (json \ "isRepresentedByAgent").as[String] mustBe "YES"
      }

      "must serialize None optional fields correctly" in {
        val json = Json.toJson(minimalVendorReturn)

        val deserialized = Json.fromJson[VendorReturn](json).asEither.value
        deserialized.houseNumber must not be defined
        deserialized.addressLine2 must not be defined
        deserialized.postcode must not be defined
      }

      "must produce valid JSON structure" in {
        val json = Json.toJson(completeVendorReturn)

        json mustBe a[JsObject]
        json.as[JsObject].keys must contain allOf("stornId", "returnResourceRef", "title", "forename1", "forename2", "surName", "houseNumber", "addressLine1", "addressLine2", "addressLine3", "addressLine4", "postcode", "isRepresentedByAgent")
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[VendorReturn]]
      }

      "must round-trip serialize and deserialize with all fields" in {
        val json = Json.toJson(completeVendorReturn)
        val result = Json.fromJson[VendorReturn](json).asEither.value

        result mustEqual completeVendorReturn
      }

      "must round-trip serialize and deserialize with only required fields" in {
        val json = Json.toJson(minimalVendorReturn)
        val result = Json.fromJson[VendorReturn](json).asEither.value

        result mustEqual minimalVendorReturn
      }

      "must round-trip with mixed optional fields" in {
        val mixedVendorReturn = VendorReturn(
          stornId = "12345",
          returnResourceRef = "45678",
          title = "Mr",
          forename1 = "A",
          forename2 = None,
          surName = "Test",
          houseNumber = None,
          addressLine1 = "Test Street",
          addressLine2 = Some("Apartment 5"),
          addressLine3 = None,
          addressLine4 = Some("District B"),
          postcode = Some("TE23 5TT"),
          isRepresentedByAgent = "YES"
        )

        val json = Json.toJson(mixedVendorReturn)
        val result = Json.fromJson[VendorReturn](json).asEither.value

        result mustEqual mixedVendorReturn
      }
    }

    "case class" - {

      "must create instance with all fields" in {
        completeVendorReturn.stornId mustBe "12345"
        completeVendorReturn.houseNumber mustBe Some(23)
        completeVendorReturn.addressLine2 mustBe Some("Apartment 5")
      }

      "must create instance with only required fields" in {
        minimalVendorReturn.stornId mustBe "12345"
        minimalVendorReturn.houseNumber must not be defined
        minimalVendorReturn.addressLine2 must not be defined
      }

      "must support equality" in {
        val vendorReturn1 = minimalVendorReturn
        val vendorReturn2 = minimalVendorReturn.copy()

        vendorReturn1 mustEqual vendorReturn2
      }

      "must support equality with all fields" in {
        val vendorReturn1 = completeVendorReturn
        val vendorReturn2 = completeVendorReturn.copy()

        vendorReturn1 mustEqual vendorReturn2
      }

      "must support copy with modifications" in {
        val modified = minimalVendorReturn.copy(
          houseNumber = Some(99),
          postcode = Some("AB12 3CD")
        )

        modified.houseNumber mustBe Some(99)
        modified.postcode mustBe Some("AB12 3CD")
        modified.stornId mustBe minimalVendorReturn.stornId
      }

      "must not be equal when required fields differ" in {
        val vendorReturn1 = minimalVendorReturn
        val vendorReturn2 = minimalVendorReturn.copy(stornId = "DIFFERENT")

        vendorReturn1 must not equal vendorReturn2
      }

      "must not be equal when optional fields differ" in {
        val vendorReturn1 = minimalVendorReturn
        val vendorReturn2 = minimalVendorReturn.copy(houseNumber = Some(100))

        vendorReturn1 must not equal vendorReturn2
      }
    }
  }
}