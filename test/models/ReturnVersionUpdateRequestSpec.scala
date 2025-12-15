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

import scala.concurrent.ExecutionContext.Implicits.global

class ReturnVersionUpdateRequestSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  private val validReturnVersionUpdateRequestJson = Json.obj(
    "storn" -> "12345",
    "returnResourceRef" -> "RRF-2024-001",
    "currentVersion" -> "1.0"
  )

  private val returnVersionUpdateRequest = ReturnVersionUpdateRequest(
    storn = "12345",
    returnResourceRef = "RRF-2024-001",
    currentVersion = "1.0"
  )

  private val minimalFullReturn = FullReturn(
    stornId = "12345",
    returnResourceRef = "RRF-2024-001",
    returnInfo = Some(
      ReturnInfo(
        version = Some("1.0")
      )
    )
  )

  private val validReturnVersionUpdateReturnJson1 = Json.obj(
    "newVersion" -> Some(1)
  )

  private val validReturnVersionUpdateReturnJson2 = Json.obj(
    "newVersion" -> Some(2)
  )

  private val returnVersionUpdateReturnTrue = ReturnVersionUpdateReturn(newVersion = Some(1))
  private val returnVersionUpdateReturnFalse = ReturnVersionUpdateReturn(newVersion = None)

  "ReturnVersionUpdateRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[ReturnVersionUpdateRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[ReturnVersionUpdateRequest](validReturnVersionUpdateRequestJson).asEither.value

        result.storn mustBe "12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.currentVersion mustBe "1.0"
      }

      "must deserialize JSON with different version formats" in {
        val jsonV2 = Json.obj(
          "storn" -> "12345",
          "returnResourceRef" -> "RRF-2024-001",
          "currentVersion" -> "2.5.1"
        )

        val result = Json.fromJson[ReturnVersionUpdateRequest](jsonV2).asEither.value

        result.currentVersion mustBe "2.5.1"
      }

      "must deserialize JSON with numeric version string" in {
        val json = Json.obj(
          "storn" -> "12345",
          "returnResourceRef" -> "RRF-2024-001",
          "currentVersion" -> "3"
        )

        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither.value

        result.currentVersion mustBe "3"
      }

      "must fail to deserialize when storn is missing" in {
        val json = validReturnVersionUpdateRequestJson - "storn"

        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when returnResourceRef is missing" in {
        val json = validReturnVersionUpdateRequestJson - "returnResourceRef"

        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when currentVersion is missing" in {
        val json = validReturnVersionUpdateRequestJson - "currentVersion"

        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when storn has invalid type" in {
        val json = validReturnVersionUpdateRequestJson ++ Json.obj("storn" -> 123)

        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when returnResourceRef has invalid type" in {
        val json = validReturnVersionUpdateRequestJson ++ Json.obj("returnResourceRef" -> true)

        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when currentVersion has invalid type" in {
        val json = validReturnVersionUpdateRequestJson ++ Json.obj("currentVersion" -> 456)

        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize completely invalid JSON structure" in {
        val json = Json.obj("invalidField" -> "value")

        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when all fields are missing" in {
        val json = Json.obj()

        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither

        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[ReturnVersionUpdateRequest]]
      }

      "must serialize ReturnVersionUpdateRequest with all fields" in {
        val json = Json.toJson(returnVersionUpdateRequest)

        (json \ "storn").as[String] mustBe "12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"
        (json \ "currentVersion").as[String] mustBe "1.0"
      }

      "must serialize ReturnVersionUpdateRequest with different version" in {
        val request = returnVersionUpdateRequest.copy(currentVersion = "2.5.1")
        val json = Json.toJson(request)

        (json \ "currentVersion").as[String] mustBe "2.5.1"
      }

      "must produce valid JSON structure" in {
        val json = Json.toJson(returnVersionUpdateRequest)

        json mustBe a[JsObject]
        json.as[JsObject].keys must contain allOf("storn", "returnResourceRef", "currentVersion")
      }

      "must produce JSON with exactly three fields" in {
        val json = Json.toJson(returnVersionUpdateRequest)

        json.as[JsObject].keys.size mustBe 3
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[ReturnVersionUpdateRequest]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(returnVersionUpdateRequest)
        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither.value

        result mustEqual returnVersionUpdateRequest
      }

      "must round-trip with version 2.0" in {
        val request = returnVersionUpdateRequest.copy(currentVersion = "2.0")
        val json = Json.toJson(request)
        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither.value

        result mustEqual request
      }

      "must round-trip with complex version string" in {
        val request = returnVersionUpdateRequest.copy(currentVersion = "3.14.159")
        val json = Json.toJson(request)
        val result = Json.fromJson[ReturnVersionUpdateRequest](json).asEither.value

        result mustEqual request
        result.currentVersion mustBe "3.14.159"
      }
    }

    "case class" - {

      "must create instance with all fields" in {
        returnVersionUpdateRequest.storn mustBe "12345"
        returnVersionUpdateRequest.returnResourceRef mustBe "RRF-2024-001"
        returnVersionUpdateRequest.currentVersion mustBe "1.0"
      }

      "must support equality" in {
        val request1 = returnVersionUpdateRequest
        val request2 = returnVersionUpdateRequest.copy()

        request1 mustEqual request2
      }

      "must support copy with modifications" in {
        val modified = returnVersionUpdateRequest.copy(storn = "54321")

        modified.storn mustBe "54321"
        modified.returnResourceRef mustBe returnVersionUpdateRequest.returnResourceRef
        modified.currentVersion mustBe returnVersionUpdateRequest.currentVersion
      }

      "must support copy with version modification" in {
        val modified = returnVersionUpdateRequest.copy(currentVersion = "2.0")

        modified.currentVersion mustBe "2.0"
        modified.storn mustBe returnVersionUpdateRequest.storn
        modified.returnResourceRef mustBe returnVersionUpdateRequest.returnResourceRef
      }

      "must not be equal when storn differs" in {
        val request1 = returnVersionUpdateRequest
        val request2 = returnVersionUpdateRequest.copy(storn = "99999")

        request1 must not equal request2
      }

      "must not be equal when returnResourceRef differs" in {
        val request1 = returnVersionUpdateRequest
        val request2 = returnVersionUpdateRequest.copy(returnResourceRef = "RRF-2025-999")

        request1 must not equal request2
      }

      "must not be equal when currentVersion differs" in {
        val request1 = returnVersionUpdateRequest
        val request2 = returnVersionUpdateRequest.copy(currentVersion = "2.0")

        request1 must not equal request2
      }

      "must not be equal when multiple fields differ" in {
        val request1 = returnVersionUpdateRequest
        val request2 = returnVersionUpdateRequest.copy(
          storn = "99999",
          returnResourceRef = "RRF-2025-999",
          currentVersion = "3.0"
        )

        request1 must not equal request2
      }

      "must support creating with different version formats" in {
        val v1 = ReturnVersionUpdateRequest("12345", "RRF-001", "1.0")
        val v2 = ReturnVersionUpdateRequest("12345", "RRF-001", "2.5.1")
        val v3 = ReturnVersionUpdateRequest("12345", "RRF-001", "10")

        v1.currentVersion mustBe "1.0"
        v2.currentVersion mustBe "2.5.1"
        v3.currentVersion mustBe "10"
      }
    }

    ".from" - {

      "must convert into ReturnVersionUpdateRequest when required data is present" in {
        val userAnswers = UserAnswers(
          id = "12345",
          storn = "12345",
          fullReturn = Some(minimalFullReturn)
        )

        val result = ReturnVersionUpdateRequest.from(userAnswers)
        result.map { value =>
          value mustBe returnVersionUpdateRequest
        }
      }

      "must fail to convert when version is not found" in {
        val userAnswers = UserAnswers(
          id = "12345",
          storn = "12345",
          fullReturn = Some(minimalFullReturn.copy(
            returnInfo = Some(ReturnInfo(version = None))
          ))
        )

        val result = ReturnVersionUpdateRequest.from(userAnswers)
        result.map { value =>
          value mustBe a[NoSuchElementException]
        }
      }

      "must fail to convert when full return is not found" in {
        val userAnswers = UserAnswers(
          id = "12345",
          storn = "12345",
          fullReturn = None
        )

        val result = ReturnVersionUpdateRequest.from(userAnswers)
        result.map { value =>
          value mustBe a[NoSuchElementException]
        }
      }
    }
  }

  "ReturnVersionUpdateReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[ReturnVersionUpdateReturn]]
      }

      "must deserialize valid JSON with updated to 1" in {
        val result = Json.fromJson[ReturnVersionUpdateReturn](validReturnVersionUpdateReturnJson1).asEither.value

        result.newVersion mustBe Some(1)
      }

      "must deserialize valid JSON with newVersion 2" in {
        val result = Json.fromJson[ReturnVersionUpdateReturn](validReturnVersionUpdateReturnJson2).asEither.value

        result.newVersion mustBe Some(2)
      }

      "must fail to deserialize when newVersion has invalid type" in {
        val json = Json.obj("newVersion" -> "invalid")

        val result = Json.fromJson[ReturnVersionUpdateReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when newVersion is a boolean" in {
        val json = Json.obj("newVersion" -> true)

        val result = Json.fromJson[ReturnVersionUpdateReturn](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize completely invalid JSON structure" in {
        val json = Json.obj("newVersion" -> "Not an int")

        val result = Json.fromJson[ReturnVersionUpdateReturn](json).asEither

        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[ReturnVersionUpdateReturn]]
      }

      "must serialize ReturnVersionUpdateReturn with updated true" in {
        val json = Json.toJson(returnVersionUpdateReturnTrue)

        (json \ "newVersion").as[Int] mustBe 1
      }

      "must serialize ReturnVersionUpdateReturn with updated false" in {
        val json = Json.toJson(returnVersionUpdateReturnFalse)

        (json \ "newVersion").asOpt[Int] mustBe None
      }

      "must produce valid JSON structure" in {
        val json = Json.toJson(returnVersionUpdateReturnTrue)

        json mustBe a[JsObject]
        json.as[JsObject].keys must contain("newVersion")
      }

      "must produce JSON with exactly one field" in {
        val json = Json.toJson(returnVersionUpdateReturnTrue)

        json.as[JsObject].keys.size mustBe 1
      }

      "must produce boolean value not string" in {
        val json = Json.toJson(returnVersionUpdateReturnTrue)

        (json \ "newVersion").get mustBe a[JsNumber]
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[ReturnVersionUpdateReturn]]
      }

      "must round-trip serialize and deserialize with updated true" in {
        val json = Json.toJson(returnVersionUpdateReturnTrue)
        val result = Json.fromJson[ReturnVersionUpdateReturn](json).asEither.value

        result mustEqual returnVersionUpdateReturnTrue
      }

      "must round-trip serialize and deserialize with updated false" in {
        val json = Json.toJson(returnVersionUpdateReturnFalse)
        val result = Json.fromJson[ReturnVersionUpdateReturn](json).asEither.value

        result mustEqual returnVersionUpdateReturnFalse
      }

      "must maintain boolean type through round-trip" in {
        val json = Json.toJson(returnVersionUpdateReturnTrue)
        (json \ "newVersion").asOpt[Int] mustBe Some(1)

        val result = Json.fromJson[ReturnVersionUpdateReturn](json).asEither.value
        result.newVersion mustBe Some(1)
      }
    }

    "case class" - {

      "must create instance with updated true" in {
        returnVersionUpdateReturnTrue.newVersion mustBe Some(1)
      }

      "must create instance with updated false" in {
        returnVersionUpdateReturnFalse.newVersion mustBe None
      }

      "must support equality with true value" in {
        val versionReturn1 = returnVersionUpdateReturnTrue
        val versionReturn2 = returnVersionUpdateReturnTrue.copy()

        versionReturn1 mustEqual versionReturn2
      }

      "must support equality with false value" in {
        val versionReturn1 = returnVersionUpdateReturnFalse
        val versionReturn2 = returnVersionUpdateReturnFalse.copy()

        versionReturn1 mustEqual versionReturn2
      }

      "must support copy with modifications" in {
        val modified = returnVersionUpdateReturnTrue.copy(newVersion = None)

        modified.newVersion mustBe None
      }

      "must support copy from false to true" in {
        val modified = returnVersionUpdateReturnFalse.copy(newVersion = Some(1))

        modified.newVersion mustBe Some(1)
      }

      "must not be equal when fields differ" in {
        val versionReturn1 = returnVersionUpdateReturnTrue
        val versionReturn2 = returnVersionUpdateReturnFalse

        versionReturn1 must not equal versionReturn2
      }

      "must not be equal when modified" in {
        val versionReturn1 = returnVersionUpdateReturnTrue
        val versionReturn2 = returnVersionUpdateReturnTrue.copy(newVersion = None)

        versionReturn1 must not equal versionReturn2
      }
    }
  }
}