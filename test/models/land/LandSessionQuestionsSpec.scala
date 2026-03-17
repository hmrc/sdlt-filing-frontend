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

package models.land

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json.{JsNull, JsObject, Json, Reads}

class LandSessionQuestionsSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  object TestData {
    def landSessionQuestionsJsonComplete: JsObject = Json.obj(
      "landCurrent" -> Json.obj(
        "landId"                           -> "LAND001",
        "propertyType"                     -> "NonResidential",
        "landInterestTransferredOrCreated" -> "Transfer",
        "landAddress" -> Json.obj(
          "houseNumber"      -> "1",
          "line1"            -> "1 Test Street",
          "line2"            -> "Test Area",
          "line3"            -> "Test Town",
          "line4"            -> "Test County",
          "line5"            -> "Test Region",
          "postcode"         -> "AB1 2CD",
          "country" -> Json.obj(
            "code" -> "GB",
            "name" -> "UK"
          ),
          "addressValidated" -> true
        ),
        "localAuthorityCode"               -> "1234",
        "titleNumber"                      -> "TN123456",
        "landNlpgUprn"                     -> "100012345678",
        "landSendingPlanByPost"            -> false,
        "landMineralsOrMineralRights"      -> false,
        "agriculturalOrDevelopmentalLand"  -> true,
        "areaOfLand"                       -> "500"
      )
    )

    def landSessionQuestionsJsonMinimal: JsObject = Json.obj(
      "landCurrent" -> Json.obj(
        "landId"                           -> JsNull,
        "propertyType"                     -> JsNull,
        "landInterestTransferredOrCreated" -> JsNull,
        "landAddress" -> Json.obj(
          "houseNumber"      -> JsNull,
          "line1"            -> "1 Test Street",
          "line2"            -> JsNull,
          "line3"            -> JsNull,
          "line4"            -> JsNull,
          "line5"            -> JsNull,
          "postcode"         -> "AB1 2CD",
          "country"          -> JsNull,
          "addressValidated" -> false
        ),
        "localAuthorityCode"              -> "1234",
        "titleNumber"                     -> JsNull,
        "landNlpgUprn"                    -> JsNull,
        "landSendingPlanByPost"           -> false,
        "landMineralsOrMineralRights"     -> false,
        "agriculturalOrDevelopmentalLand" -> JsNull,
        "areaOfLand"                      -> JsNull
      )
    )
  }

  "LandSessionQuestions" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[LandSessionQuestions]]
      }

      "must deserialize valid JSON with all fields" in {
        val json = TestData.landSessionQuestionsJsonComplete \ "landCurrent"
        val result = Json.fromJson[LandSessionQuestions](json.get).asEither.value

        val expectedResult = LandSessionQuestions(
          landId                           = Some("LAND001"),
          propertyType                     = Some("NonResidential"),
          landInterestTransferredOrCreated = Some("Transfer"),
          landAddress = LandSessionAddress(
            houseNumber      = Some("1"),
            line1            = "1 Test Street",
            line2            = Some("Test Area"),
            line3            = Some("Test Town"),
            line4            = Some("Test County"),
            line5            = Some("Test Region"),
            postcode         = "AB1 2CD",
            country          = Some(LandSessionCountry(code = Some("GB"), name = Some("UK"))),
            addressValidated = true
          ),
          localAuthorityCode              = "1234",
          titleNumber                     = Some("TN123456"),
          landNlpgUprn                    = Some("100012345678"),
          landSendingPlanByPost           = false,
          landMineralsOrMineralRights     = false,
          agriculturalOrDevelopmentalLand = Some(true),
          areaOfLand                      = Some("500")
        )

        result shouldBe expectedResult
      }

      "must deserialize valid JSON with minimal fields" in {
        val json = TestData.landSessionQuestionsJsonMinimal \ "landCurrent"
        val result = Json.fromJson[LandSessionQuestions](json.get).asEither.value

        val expectedResult = LandSessionQuestions(
          landId                           = None,
          propertyType                     = None,
          landInterestTransferredOrCreated = None,
          landAddress = LandSessionAddress(
            houseNumber      = None,
            line1            = "1 Test Street",
            line2            = None,
            line3            = None,
            line4            = None,
            line5            = None,
            postcode         = "AB1 2CD",
            country          = None,
            addressValidated = false
          ),
          localAuthorityCode              = "1234",
          titleNumber                     = None,
          landNlpgUprn                    = None,
          landSendingPlanByPost           = false,
          landMineralsOrMineralRights     = false,
          agriculturalOrDevelopmentalLand = None,
          areaOfLand                      = None
        )

        result shouldBe expectedResult
      }
    }
  }

  "LandSessionAddress" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[LandSessionAddress]]
      }
    }
  }

  "LandSessionCountry" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[LandSessionCountry]]
      }

      "must deserialize valid JSON with all fields" in {
        val json = Json.obj("code" -> "GB", "name" -> "UK")
        val result = Json.fromJson[LandSessionCountry](json).asEither.value

        result shouldBe LandSessionCountry(code = Some("GB"), name = Some("UK"))
      }

      "must deserialize valid JSON with no fields" in {
        val json = Json.obj("code" -> JsNull, "name" -> JsNull)
        val result = Json.fromJson[LandSessionCountry](json).asEither.value

        result shouldBe LandSessionCountry(code = None, name = None)
      }
    }
  }
}