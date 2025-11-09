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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json.*

class GetReturnByRefRequestSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  private val validJson = Json.obj(
    "returnResourceRef" -> "RRF-2024-001",
    "storn" -> "STORN123456"
  )

  private val validRequest = GetReturnByRefRequest(
    returnResourceRef = "RRF-2024-001",
    storn = "STORN123456"
  )

  "GetReturnByRefRequest" - {

    ".reads" - {

      "must deserialize valid JSON" in {
        val result = Json.fromJson[GetReturnByRefRequest](validJson).asEither.value

        result.returnResourceRef mustBe "RRF-2024-001"
        result.storn mustBe "STORN123456"
      }

      "must fail when returnResourceRef is missing" in {
        val json = Json.obj("storn" -> "STORN123456")

        val result = Json.fromJson[GetReturnByRefRequest](json).asEither

        result.isLeft mustBe true
      }

      "must fail when storn is missing" in {
        val json = Json.obj("returnResourceRef" -> "RRF-2024-001")

        val result = Json.fromJson[GetReturnByRefRequest](json).asEither

        result.isLeft mustBe true
      }

      "must fail when fields have invalid types" in {
        val json = Json.obj(
          "returnResourceRef" -> 123456,
          "storn" -> 789012
        )

        val result = Json.fromJson[GetReturnByRefRequest](json).asEither

        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must serialize GetReturnByRefRequest" in {
        val json = Json.toJson(validRequest)

        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"
        (json \ "storn").as[String] mustBe "STORN123456"
      }
    }

    ".format" - {

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(validRequest)
        val result = Json.fromJson[GetReturnByRefRequest](json).asEither.value

        result mustEqual validRequest
      }
    }

    "case class" - {

      "must support equality" in {
        val request1 = validRequest
        val request2 = validRequest.copy()

        request1 mustEqual request2
      }

      "must support copy with modifications" in {
        val modified = validRequest.copy(returnResourceRef = "RRF-2024-999")

        modified.returnResourceRef mustBe "RRF-2024-999"
        modified.storn mustBe validRequest.storn
      }

      "must not be equal when fields differ" in {
        val request1 = validRequest
        val request2 = validRequest.copy(storn = "DIFFERENT")

        request1 must not equal request2
      }
    }
  }
}
