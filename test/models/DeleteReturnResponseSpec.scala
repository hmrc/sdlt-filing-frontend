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

package models

import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsObject, Json, Reads, Writes}

class DeleteReturnResponseSpec extends AnyFreeSpec with Matchers with EitherValues {

  "DeleteReturnResponseSpec" - {

    def validDeleteReturnResponseJson: JsObject = Json.obj("deleted" -> true)

    def inValidDeleteReturnResponseJson: JsObject = Json.obj("deleted" -> "true")

    def validDeleteReturnResponse = DeleteReturnResponse(deleted = true)

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[DeleteReturnResponse]]
      }

      "must deserialize valid JSON" in {
        val result = Json.fromJson[DeleteReturnResponse](validDeleteReturnResponseJson).asEither.value

        result mustBe DeleteReturnResponse(deleted = true)
      }

      "must fail when field has wrong type" in {
        val result = Json.fromJson[DeleteReturnResponse](inValidDeleteReturnResponseJson).asEither

        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[DeleteReturnResponse]]
      }

      "must serialize" in {
        val json = Json.toJson(validDeleteReturnResponse)

        (json \ "deleted").as[Boolean] mustBe true
      }
    }

    ".formats" - {

      "must round-trip" in {
        val json = Json.toJson(validDeleteReturnResponse)
        val result = Json.fromJson[DeleteReturnResponse](json).asEither.value

        result mustEqual validDeleteReturnResponse
      }
    }
  }
}