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

package models.purchaser

import models.purchaser.NameOfPurchaser
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsObject, Json, Reads, Writes}

class NameOfPurchaserSpec extends AnyFreeSpec with Matchers with EitherValues {

  "NameOfPurchaser" - {

    def nameOfPurchaserWithOptionalJson: JsObject = Json.obj(
      "forename1" -> "John",
      "forename2" -> "Michael",
      "name" -> "Smith"
    )

    def nameOfPurchaserWithNoOptionalJson: JsObject = Json.obj(
      "name" -> "Smith"
    )

    def nameOfPurchaserWithPartialOptionalJson: JsObject = Json.obj(
      "forename1" -> "John",
      "name" -> "Smith"
    )

    def invalidNameOfPurchaserJson: JsObject = Json.obj(
      "name" -> true
    )

    def missingRequiredFieldJson: JsObject = Json.obj(
      "forename1" -> "John",
      "forename2" -> "Michael"
    )

    def nameOfPurchaserWithOptional = NameOfPurchaser(
      forename1 = Some("John"),
      forename2 = Some("Michael"),
      name = "Smith"
    )

    def nameOfPurchaserWithNoOptional = NameOfPurchaser(
      forename1 = None,
      forename2 = None,
      name = "Smith"
    )

    def nameOfPurchaserWithPartialOptional = NameOfPurchaser(
      forename1 = Some("John"),
      forename2 = None,
      name = "Smith"
    )

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[NameOfPurchaser]]
      }

      "must deserialize valid JSON" - {

        "with all optional values" in {
          val result = Json.fromJson[NameOfPurchaser](nameOfPurchaserWithOptionalJson).asEither.value

          result mustBe NameOfPurchaser(
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            name = "Smith"
          )
        }

        "with no optional values" in {
          val result = Json.fromJson[NameOfPurchaser](nameOfPurchaserWithNoOptionalJson).asEither.value

          result mustBe NameOfPurchaser(
            forename1 = None,
            forename2 = None,
            name = "Smith"
          )
        }

        "with partial optional values" in {
          val result = Json.fromJson[NameOfPurchaser](nameOfPurchaserWithPartialOptionalJson).asEither.value

          result mustBe NameOfPurchaser(
            forename1 = Some("John"),
            forename2 = None,
            name = "Smith"
          )
        }
      }

      "must fail when field has wrong type" in {
        val result = Json.fromJson[NameOfPurchaser](invalidNameOfPurchaserJson).asEither

        result.isLeft mustBe true
      }

      "must fail when required field is missing" in {
        val result = Json.fromJson[NameOfPurchaser](missingRequiredFieldJson).asEither

        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[NameOfPurchaser]]
      }

      "must serialize NameOfPurchaser" - {

        "with all optional values" in {
          val json = Json.toJson(nameOfPurchaserWithOptional)

          (json \ "forename1").as[String] mustBe "John"
          (json \ "forename2").as[String] mustBe "Michael"
          (json \ "name").as[String] mustBe "Smith"
        }

        "with no optional values" in {
          val json = Json.toJson(nameOfPurchaserWithNoOptional)

          (json \ "forename1").asOpt[String] mustBe None
          (json \ "forename2").asOpt[String] mustBe None
          (json \ "name").as[String] mustBe "Smith"
        }

        "with partial optional values" in {
          val json = Json.toJson(nameOfPurchaserWithPartialOptional)

          (json \ "forename1").as[String] mustBe "John"
          (json \ "forename2").asOpt[String] mustBe None
          (json \ "name").as[String] mustBe "Smith"
        }
      }
    }

    ".formats" - {

      "must round-trip" - {

        "with all optional values" in {
          val json = Json.toJson(nameOfPurchaserWithOptional)
          val result = Json.fromJson[NameOfPurchaser](json).asEither.value

          result mustEqual nameOfPurchaserWithOptional
        }

        "with no optional values" in {
          val json = Json.toJson(nameOfPurchaserWithNoOptional)
          val result = Json.fromJson[NameOfPurchaser](json).asEither.value

          result mustEqual nameOfPurchaserWithNoOptional
        }

        "with partial optional values" in {
          val json = Json.toJson(nameOfPurchaserWithPartialOptional)
          val result = Json.fromJson[NameOfPurchaser](json).asEither.value

          result mustEqual nameOfPurchaserWithPartialOptional
        }
      }
    }
  }
}