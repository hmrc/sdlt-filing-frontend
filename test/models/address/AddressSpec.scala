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

package models.address

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

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class AddressSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  private val validAddressJsonComplete = Json.obj(
    "line1" -> "16 Coniston Court",
    "line2" -> "Holland road",
    "line3" -> "Building A",
    "line4" -> "District B",
    "line5" -> "County C",
    "postcode" -> "BN3 1JU",
    "country" -> Json.obj(
      "code" -> "UK",
      "name" -> "United Kingdom"
    ),
    "addressValidated" -> true
  )

  private val validAddressJsonMinimal = Json.obj(
    "line1" -> "16 Coniston Court"
  )

  private val completeAddress = Address(
    line1 = "16 Coniston Court",
    line2 = Some("Holland road"),
    line3 = Some("Building A"),
    line4 = Some("District B"),
    line5 = Some("County C"),
    postcode = Some("BN3 1JU"),
    country = Some(Country(Some("UK"), Some("United Kingdom"))),
    addressValidated = true
  )

  private val minimalAddress = Address(
    line1 = "16 Coniston Court",
    line2 = None,
    line3 = None,
    line4 = None,
    line5 = None,
    postcode = None,
    country = None,
    addressValidated = false
  )

  private val validAlfAddressJson = Json.obj(
    "auditRef" -> "bed4bd24-72da-42a7-9338-f43431b7ed72",
    "id" -> "GB990091234524",
    "address" -> Json.obj(
      "lines" -> Json.arr("10 Other Place", "Some District", "Anytown"),
      "postcode" -> "ZZ1 1ZZ",
      "country" -> Json.obj(
        "code" -> "GB",
        "name" -> "United Kingdom"
      )
    )
  )

  private val validAlfAddressJsonNoId = Json.obj(
    "auditRef" -> "bed4bd24-72da-42a7-9338-f43431b7ed72",
    "address" -> Json.obj(
      "lines" -> Json.arr("10 Other Place", "Some District", "Anytown"),
      "postcode" -> "ZZ1 1ZZ",
      "country" -> Json.obj(
        "code" -> "GB",
        "name" -> "United Kingdom"
      )
    )
  )

  private val validAlfAddressJsonFiveLines = Json.obj(
    "auditRef" -> "bed4bd24-72da-42a7-9338-f43431b7ed72",
    "id" -> "GB990091234524",
    "address" -> Json.obj(
      "lines" -> Json.arr("Line 1", "Line 2", "Line 3", "Line 4", "Line 5"),
      "postcode" -> "SW1A 1AA",
      "country" -> Json.obj(
        "code" -> "GB",
        "name" -> "United Kingdom"
      )
    )
  )

  "Address" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[Address]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[Address](validAddressJsonComplete).asEither.value

        result.line1 mustBe "16 Coniston Court"
        result.line2 mustBe Some("Holland road")
        result.line3 mustBe Some("Building A")
        result.line4 mustBe Some("District B")
        result.line5 mustBe Some("County C")
        result.postcode mustBe Some("BN3 1JU")
        result.country mustBe Some(Country(Some("UK"), Some("United Kingdom")))
        result.addressValidated mustBe true
      }

      "must deserialize valid JSON with only required fields" in {
        val result = Json.fromJson[Address](validAddressJsonMinimal).asEither.value

        result.line1 mustBe "16 Coniston Court"
        result.line2 must not be defined
        result.line3 must not be defined
        result.line4 must not be defined
        result.line5 must not be defined
        result.postcode must not be defined
        result.country must not be defined
        result.addressValidated mustBe false
      }

      "must deserialize JSON with null optional fields" in {
        val json = Json.obj(
          "line1" -> "16 Coniston Court",
          "line2" -> JsNull,
          "line3" -> JsNull,
          "line4" -> JsNull,
          "line5" -> JsNull,
          "postcode" -> JsNull,
          "country" -> JsNull,
          "addressValidated" -> false
        )

        val result = Json.fromJson[Address](json).asEither.value

        result.line2 must not be defined
        result.line3 must not be defined
        result.postcode must not be defined
        result.country must not be defined
      }

      "must fail to deserialize when line1 is missing" in {
        val json = validAddressJsonComplete - "line1"

        val result = Json.fromJson[Address](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when required field has invalid type" in {
        val json = validAddressJsonComplete ++ Json.obj("line1" -> 123)

        val result = Json.fromJson[Address](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize completely invalid JSON structure" in {
        val json = Json.obj("invalidField" -> "value")

        val result = Json.fromJson[Address](json).asEither

        result.isLeft mustBe true
      }

      "must deserialize address with country but no postcode" in {
        val json = Json.obj(
          "line1" -> "Test Street",
          "country" -> Json.obj(
            "code" -> "US",
            "name" -> "United States"
          )
        )

        val result = Json.fromJson[Address](json).asEither.value

        result.line1 mustBe "Test Street"
        result.postcode must not be defined
        result.country mustBe Some(Country(Some("US"), Some("United States")))
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[Address]]
      }

      "must serialize Address with all fields" in {
        val json = Json.toJson(completeAddress)

        (json \ "line1").as[String] mustBe "16 Coniston Court"
        (json \ "line2").asOpt[String] mustBe Some("Holland road")
        (json \ "line3").asOpt[String] mustBe Some("Building A")
        (json \ "line4").asOpt[String] mustBe Some("District B")
        (json \ "line5").asOpt[String] mustBe Some("County C")
        (json \ "postcode").asOpt[String] mustBe Some("BN3 1JU")
        (json \ "country" \ "code").asOpt[String] mustBe Some("UK")
        (json \ "country" \ "name").asOpt[String] mustBe Some("United Kingdom")
        (json \ "addressValidated").as[Boolean] mustBe true
      }

      "must serialize Address with only required fields" in {
        val json = Json.toJson(minimalAddress)

        (json \ "line1").as[String] mustBe "16 Coniston Court"
        (json \ "addressValidated").as[Boolean] mustBe false
      }

      "must serialize None optional fields correctly" in {
        val json = Json.toJson(minimalAddress)

        val deserialized = Json.fromJson[Address](json).asEither.value
        deserialized.line2 must not be defined
        deserialized.line3 must not be defined
        deserialized.postcode must not be defined
      }

      "must produce valid JSON structure" in {
        val json = Json.toJson(completeAddress)

        json mustBe a[JsObject]
        json.as[JsObject].keys must contain allOf("line1", "addressValidated")
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[Address]]
      }

      "must round-trip serialize and deserialize with all fields" in {
        val json = Json.toJson(completeAddress)
        val result = Json.fromJson[Address](json).asEither.value

        result mustEqual completeAddress
      }

      "must round-trip serialize and deserialize with only required fields" in {
        val json = Json.toJson(minimalAddress)
        val result = Json.fromJson[Address](json).asEither.value

        result mustEqual minimalAddress
      }

      "must round-trip with mixed optional fields" in {
        val mixedAddress = Address(
          line1 = "Main Street",
          line2 = Some("Apt 10"),
          line3 = None,
          line4 = Some("West Wing"),
          line5 = None,
          postcode = Some("AB12 3CD"),
          country = None,
          addressValidated = true
        )

        val json = Json.toJson(mixedAddress)
        val result = Json.fromJson[Address](json).asEither.value

        result mustEqual mixedAddress
      }
    }

    ".addressLookupReads" - {

      "must deserialize valid ALF JSON with id" in {
        val result = Address.addressLookupReads.reads(validAlfAddressJson).asEither.value

        result.line1 mustBe "10 Other Place"
        result.line2 mustBe Some("Some District")
        result.line3 mustBe Some("Anytown")
        result.line4 must not be defined
        result.line5 must not be defined
        result.postcode mustBe Some("ZZ1 1ZZ")
        result.country mustBe Some(Country(Some("GB"), Some("United Kingdom")))
        result.addressValidated mustBe true
      }

      "must deserialize valid ALF JSON without id" in {
        val result = Address.addressLookupReads.reads(validAlfAddressJsonNoId).asEither.value

        result.line1 mustBe "10 Other Place"
        result.line2 mustBe Some("Some District")
        result.line3 mustBe Some("Anytown")
        result.postcode mustBe Some("ZZ1 1ZZ")
        result.addressValidated mustBe false
      }

      "must deserialize ALF JSON with country but no postcode" in {
        val json = Json.obj(
          "auditRef" -> "bed4bd24-72da-42a7-9338-f43431b7ed72",
          "id" -> "US990091234524",
          "address" -> Json.obj(
            "lines" -> Json.arr("Test Street"),
            "country" -> Json.obj(
              "code" -> "US",
              "name" -> "United States"
            )
          )
        )

        val result = Address.addressLookupReads.reads(json).asEither.value

        result.line1 mustBe "Test Street"
        result.postcode must not be defined
        result.country mustBe Some(Country(Some("US"), Some("United States")))
        result.addressValidated mustBe true
      }

      "must deserialize ALF JSON with up to 5 address lines" in {
        val result = Address.addressLookupReads.reads(validAlfAddressJsonFiveLines).asEither.value

        result.line1 mustBe "Line 1"
        result.line2 mustBe Some("Line 2")
        result.line3 mustBe Some("Line 3")
        result.line4 mustBe Some("Line 4")
        result.line5 mustBe Some("Line 5")
        result.postcode mustBe Some("SW1A 1AA")
        result.addressValidated mustBe true
      }

      "must deserialize ALF JSON with single address line" in {
        val json = Json.obj(
          "auditRef" -> "bed4bd24-72da-42a7-9338-f43431b7ed72",
          "id" -> "GB990091234524",
          "address" -> Json.obj(
            "lines" -> Json.arr("Test Street"),
            "postcode" -> "AB12 3CD"
          )
        )

        val result = Address.addressLookupReads.reads(json).asEither.value

        result.line1 mustBe "Test Street"
        result.line2 must not be defined
        result.line3 must not be defined
        result.postcode mustBe Some("AB12 3CD")
      }

      "must deserialize ALF JSON with two address lines" in {
        val json = Json.obj(
          "auditRef" -> "bed4bd24-72da-42a7-9338-f43431b7ed72",
          "id" -> "GB990091234524",
          "address" -> Json.obj(
            "lines" -> Json.arr("16 Coniston Court", "Holland road"),
            "postcode" -> "BN3 1JU",
            "country" -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          )
        )

        val result = Address.addressLookupReads.reads(json).asEither.value

        result.line1 mustBe "16 Coniston Court"
        result.line2 mustBe Some("Holland road")
        result.line3 must not be defined
        result.postcode mustBe Some("BN3 1JU")
      }

      "must fail when neither postcode nor country is present" in {
        val json = Json.obj(
          "auditRef" -> "bed4bd24-72da-42a7-9338-f43431b7ed72",
          "id" -> "GB990091234524",
          "address" -> Json.obj(
            "lines" -> Json.arr("Test Street")
          )
        )

        val result = Address.addressLookupReads.reads(json).asEither

        result.isLeft mustBe true
      }

      "must fail when no address lines are provided" in {
        val json = Json.obj(
          "auditRef" -> "bed4bd24-72da-42a7-9338-f43431b7ed72",
          "id" -> "GB990091234524",
          "address" -> Json.obj(
            "lines" -> Json.arr(),
            "postcode" -> "AB12 3CD"
          )
        )

        val result = Address.addressLookupReads.reads(json).asEither

        result.isLeft mustBe true
      }

      "must fail when address object is missing" in {
        val json = Json.obj(
          "auditRef" -> "bed4bd24-72da-42a7-9338-f43431b7ed72",
          "id" -> "GB990091234524"
        )

        val exception = intercept[JsResultException] {
          Address.addressLookupReads.reads(json)
        }

        exception mustBe a[JsResultException]
      }

      "must fail when lines array is missing" in {
        val json = Json.obj(
          "auditRef" -> "bed4bd24-72da-42a7-9338-f43431b7ed72",
          "id" -> "GB990091234524",
          "address" -> Json.obj(
            "postcode" -> "AB12 3CD"
          )
        )

        val exception = intercept[JsResultException] {
          Address.addressLookupReads.reads(json)
        }

        exception mustBe a[JsResultException]
      }

      "must handle ALF response with auditRef but no id (addressValidated should be false)" in {
        val json = Json.obj(
          "auditRef" -> "bed4bd24-72da-42a7-9338-f43431b7ed72",
          "address" -> Json.obj(
            "lines" -> Json.arr("Test Street"),
            "postcode" -> "AB12 3CD"
          )
        )

        val result = Address.addressLookupReads.reads(json).asEither.value

        result.addressValidated mustBe false
      }
    }

    "normalise" - {

      "must trim whitespace from all fields" in {
        val addressWithSpaces = Address(
          line1 = "  16 Coniston Court  ",
          line2 = Some("  Holland road  "),
          line3 = Some("  Building A  "),
          line4 = Some("  District B  "),
          line5 = Some("  County C  "),
          postcode = Some("  BN3 1JU  "),
          country = Some(Country(Some("  UK  "), Some("  United Kingdom  "))),
          addressValidated = true
        )

        val result = addressWithSpaces.normalise()

        result.line1 mustBe "16 Coniston Court"
        result.line2 mustBe Some("Holland road")
        result.line3 mustBe Some("Building A")
        result.line4 mustBe Some("District B")
        result.line5 mustBe Some("County C")
        result.postcode mustBe Some("BN3 1JU")
        result.country.value.code mustBe Some("UK")
        result.country.value.name mustBe Some("United Kingdom")
      }

      "must convert empty strings to None" in {
        val addressWithEmpty = Address(
          line1 = "16 Coniston Court",
          line2 = Some("   "),
          line3 = Some(""),
          line4 = Some("District B"),
          line5 = Some("  "),
          postcode = Some(""),
          country = Some(Country(Some(""), Some("   "))),
          addressValidated = false
        )

        val result = addressWithEmpty.normalise()

        result.line1 mustBe "16 Coniston Court"
        result.line2 must not be defined
        result.line3 must not be defined
        result.line4 mustBe Some("District B")
        result.line5 must not be defined
        result.postcode must not be defined
        result.country must not be defined
      }

      "must handle country with only code" in {
        val address = Address(
          line1 = "Test Street",
          country = Some(Country(Some("UK"), Some("   ")))
        )

        val result = address.normalise()

        result.country.value.code mustBe Some("UK")
        result.country.value.name must not be defined
      }

      "must handle country with only name" in {
        val address = Address(
          line1 = "Test Street",
          country = Some(Country(Some("  "), Some("United Kingdom")))
        )

        val result = address.normalise()

        result.country.value.code must not be defined
        result.country.value.name mustBe Some("United Kingdom")
      }

      "must not modify already normalized address" in {
        val normalized = completeAddress.normalise()

        normalized mustEqual completeAddress
      }
    }

    "normalisedSeq" - {

      "must return sequence with all address lines" in {
        val result = Address.normalisedSeq(completeAddress)

        result must contain inOrder(
          "16 Coniston Court",
          "Holland Road",
          "Building A",
          "District B",
          "County C",
          "BN3 1JU",
          "United Kingdom"
        )
      }

      "must capitalize words in address lines" in {
        val address = Address(
          line1 = "main street",
          line2 = Some("apartment building")
        )

        val result = Address.normalisedSeq(address)

        result must contain("Main Street")
        result must contain("Apartment Building")
      }

      "must uppercase postcodes" in {
        val address = Address(
          line1 = "Test Street",
          postcode = Some("bn3 1ju")
        )

        val result = Address.normalisedSeq(address)

        result must contain("BN3 1JU")
      }

      "must exclude None fields" in {
        val result = Address.normalisedSeq(minimalAddress)

        result.size mustBe 1
        result must contain only "16 Coniston Court"
      }

      "must include country name when present" in {
        val address = Address(
          line1 = "Test Street",
          country = Some(Country(Some("US"), Some("United States")))
        )

        val result = Address.normalisedSeq(address)

        result must contain("United States")
      }

      "must handle multiple words with proper capitalization" in {
        val address = Address(
          line1 = "the old farm house"
        )

        val result = Address.normalisedSeq(address)

        result must contain("The Old Farm House")
      }
    }

    "toHtml" - {

      "must convert address to HTML with line breaks" in {
        val result = Address.toHtml(completeAddress)

        result.toString must include("<br>")
        result.toString must include("16 Coniston Court")
        result.toString must include("Holland Road")
        result.toString must include("BN3 1JU")
        result.toString must include("United Kingdom")
      }

      "must escape HTML characters" in {
        val address = Address(
          line1 = "<script>alert('test')</script>",
          line2 = Some("Test & Street")
        )

        val result = Address.toHtml(address)

        result.toString must not include "<script>"
        result.toString must include("&lt;")
        result.toString must include("&amp;")
      }

      "must handle minimal address" in {
        val result = Address.toHtml(minimalAddress)

        result.toString must include("16 Coniston Court")
        result.toString must not include "<br>"
      }
    }

    "id" - {

      "must generate id from line1 and postcode" in {
        val address = Address(
          line1 = "16 Coniston Court",
          postcode = Some("BN3 1JU")
        )

        address.id mustBe "16ConistonCourtBN31JU"
      }

      "must generate id from line1 and country when postcode absent" in {
        val address = Address(
          line1 = "16 Coniston Court",
          country = Some(Country(Some("UK"), Some("United Kingdom")))
        )

        address.id must include("16ConistonCourt")
      }

      "must remove spaces from id" in {
        val address = Address(
          line1 = "Test Street Name",
          postcode = Some("AB 12 3CD")
        )

        address.id must not include " "
      }

      "must generate id with only line1 when no postcode or country" in {
        val result = minimalAddress.id

        result mustBe "16ConistonCourt"
      }
    }

    "case class" - {

      "must create instance with all fields" in {
        completeAddress.line1 mustBe "16 Coniston Court"
        completeAddress.line2 mustBe Some("Holland road")
        completeAddress.postcode mustBe Some("BN3 1JU")
        completeAddress.addressValidated mustBe true
      }

      "must create instance with only required fields" in {
        minimalAddress.line1 mustBe "16 Coniston Court"
        minimalAddress.line2 must not be defined
        minimalAddress.addressValidated mustBe false
      }

      "must support equality" in {
        val address1 = minimalAddress
        val address2 = minimalAddress.copy()

        address1 mustEqual address2
      }

      "must support equality with all fields" in {
        val address1 = completeAddress
        val address2 = completeAddress.copy()

        address1 mustEqual address2
      }

      "must support copy with modifications" in {
        val modified = minimalAddress.copy(
          line2 = Some("New Street"),
          postcode = Some("AB12 3CD"),
          addressValidated = true
        )

        modified.line2 mustBe Some("New Street")
        modified.postcode mustBe Some("AB12 3CD")
        modified.addressValidated mustBe true
        modified.line1 mustBe minimalAddress.line1
      }

      "must not be equal when required fields differ" in {
        val address1 = minimalAddress
        val address2 = minimalAddress.copy(line1 = "Different Street")

        address1 must not equal address2
      }

      "must not be equal when optional fields differ" in {
        val address1 = minimalAddress
        val address2 = minimalAddress.copy(line2 = Some("New Line"))

        address1 must not equal address2
      }

      "must have default value for addressValidated" in {
        val address = Address(line1 = "Test")

        address.addressValidated mustBe false
      }
    }
  }

  "Country" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[Country]]
      }

      "must deserialize valid JSON with all fields" in {
        val json = Json.obj(
          "code" -> "UK",
          "name" -> "United Kingdom"
        )

        val result = Json.fromJson[Country](json).asEither.value

        result.code mustBe Some("UK")
        result.name mustBe Some("United Kingdom")
      }

      "must deserialize valid JSON with only code" in {
        val json = Json.obj("code" -> "UK")

        val result = Json.fromJson[Country](json).asEither.value

        result.code mustBe Some("UK")
        result.name must not be defined
      }

      "must deserialize valid JSON with only name" in {
        val json = Json.obj("name" -> "United Kingdom")

        val result = Json.fromJson[Country](json).asEither.value

        result.code must not be defined
        result.name mustBe Some("United Kingdom")
      }

      "must deserialize empty JSON to Country with no fields" in {
        val json = Json.obj()

        val result = Json.fromJson[Country](json).asEither.value

        result.code must not be defined
        result.name must not be defined
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[Country]]
      }

      "must serialize Country with all fields" in {
        val country = Country(Some("UK"), Some("United Kingdom"))
        val json = Json.toJson(country)

        (json \ "code").asOpt[String] mustBe Some("UK")
        (json \ "name").asOpt[String] mustBe Some("United Kingdom")
      }

      "must serialize Country with only code" in {
        val country = Country(Some("UK"), None)
        val json = Json.toJson(country)

        (json \ "code").asOpt[String] mustBe Some("UK")
        (json \ "name").asOpt[String] must not be defined
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[Country]]
      }

      "must round-trip serialize and deserialize" in {
        val country = Country(Some("UK"), Some("United Kingdom"))
        val json = Json.toJson(country)
        val result = Json.fromJson[Country](json).asEither.value

        result mustEqual country
      }
    }

    "case class" - {

      "must create instance with all fields" in {
        val country = Country(Some("UK"), Some("United Kingdom"))

        country.code mustBe Some("UK")
        country.name mustBe Some("United Kingdom")
      }

      "must create instance with no fields" in {
        val country = Country(None, None)

        country.code must not be defined
        country.name must not be defined
      }

      "must support equality" in {
        val country1 = Country(Some("UK"), Some("United Kingdom"))
        val country2 = Country(Some("UK"), Some("United Kingdom"))

        country1 mustEqual country2
      }

      "must not be equal when fields differ" in {
        val country1 = Country(Some("UK"), Some("United Kingdom"))
        val country2 = Country(Some("US"), Some("United States"))

        country1 must not equal country2
      }
    }
  }
}
