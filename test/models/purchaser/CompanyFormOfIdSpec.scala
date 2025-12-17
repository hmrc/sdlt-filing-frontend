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

import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsObject, Json, Reads, Writes}

class CompanyFormOfIdSpec extends AnyFreeSpec with Matchers with EitherValues {

  "CompanyFormOfId" - {

    val validJson: JsObject = Json.obj(
      "referenceId" -> "123456",
      "countryIssued" -> "Germany"
    )

    val invalidJson: JsObject = Json.obj(
      "referenceId" -> true,
      "countryIssued" -> "Germany"
    )

    val missingRequiredFieldJson: JsObject = Json.obj(
      "countryIssued" -> "Germany"
    )

    val example: CompanyFormOfId = CompanyFormOfId (
      referenceId = "123456",
      countryIssued = "Germany"
    )

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[CompanyFormOfId]]
      }

      "must deserialize valid JSON" - {

        "with all required values" in {
          val result = Json.fromJson[CompanyFormOfId](validJson).asEither.value

          result mustBe CompanyFormOfId(
            referenceId = "123456",
            countryIssued = "Germany"
          )
        }
      }

      "must fail when field has wrong type" in {
        val result = Json.fromJson[CompanyFormOfId](invalidJson).asEither

        result.isLeft mustBe true
      }

      "must fail when a required field is missing" in {
        val result = Json.fromJson[CompanyFormOfId](missingRequiredFieldJson).asEither

        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[CompanyFormOfId]]
      }

      "must serialize CompanyFormOfId" - {

        "with all values" in {
          val json = Json.toJson(example)

          (json \ "referenceId").as[String] mustBe "123456"
          (json \ "countryIssued").as[String] mustBe "Germany"
        }
      }
    }

    ".formats" - {

      "must round-trip" - {

        "with all required values" in {
          val json = Json.toJson(validJson)
          val result = Json.fromJson[CompanyFormOfId](json).asEither.value

          result mustEqual example
        }
      }
    }
  }
}
