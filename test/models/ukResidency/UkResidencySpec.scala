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

package models.ukResidency

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json.*

class UkResidencySpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  private val validCreateResidencyRequestJson = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "residency" -> Json.obj(
      "isNonUkResidents" -> "yes",
      "isCompany" -> "no",
      "isCrownRelief" -> "yes"
    )
  )

  private val completeCreateResidencyRequest = CreateResidencyRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    residency = ResidencyPayload(
      isNonUkResidents = "yes",
      isCompany = "no",
      isCrownRelief = "yes"
    )
  )

  private val validCreateResidencyReturnJson = Json.obj(
    "created" -> true
  )

  private val createResidencyReturn = CreateResidencyReturn(
    created = true
  )

  private val validUpdateResidencyRequestJson = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "residency" -> Json.obj(
      "isNonUkResidents" -> "no",
      "isCompany" -> "yes",
      "isCrownRelief" -> "no"
    )
  )

  private val completeUpdateResidencyRequest = UpdateResidencyRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    residency = ResidencyPayload(
      isNonUkResidents = "no",
      isCompany = "yes",
      isCrownRelief = "no"
    )
  )

  private val validDeleteResidencyRequestJson = Json.obj(
    "storn" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001"
  )

  private val deleteResidencyRequest = DeleteResidencyRequest(
    storn = "STORN12345",
    returnResourceRef = "RRF-2024-001"
  )

  private val validDeleteResidencyReturnJsonTrue  = Json.obj("deleted" -> true)
  private val validDeleteResidencyReturnJsonFalse = Json.obj("deleted" -> false)
  private val deleteResidencyReturnTrue  = DeleteResidencyReturn(deleted = true)
  private val deleteResidencyReturnFalse = DeleteResidencyReturn(deleted = false)

  "CreateResidencyRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[CreateResidencyRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[CreateResidencyRequest](validCreateResidencyRequestJson).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.residency.isNonUkResidents mustBe "yes"
        result.residency.isCompany mustBe "no"
        result.residency.isCrownRelief mustBe "yes"
      }

      "must fail to deserialize when stornId is missing" in {
        val json = validCreateResidencyRequestJson - "stornId"
        val result = Json.fromJson[CreateResidencyRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when returnResourceRef is missing" in {
        val json = validCreateResidencyRequestJson - "returnResourceRef"
        val result = Json.fromJson[CreateResidencyRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when residency is missing" in {
        val json = validCreateResidencyRequestJson - "residency"
        val result = Json.fromJson[CreateResidencyRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when required field has invalid type" in {
        val json = validCreateResidencyRequestJson ++ Json.obj("stornId" -> 123)
        val result = Json.fromJson[CreateResidencyRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize completely invalid JSON structure" in {
        val json = Json.obj("invalidField" -> "value")
        val result = Json.fromJson[CreateResidencyRequest](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[CreateResidencyRequest]]
      }

      "must serialize CreateResidencyRequest with all fields" in {
        val json = Json.toJson(completeCreateResidencyRequest)

        (json \ "stornId").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"

        val residencyJson = json \ "residency"

        (residencyJson \ "isNonUkResidents").as[String] mustBe "yes"
        (residencyJson \ "isCompany").as[String] mustBe "no"
        (residencyJson \ "isCrownRelief").as[String] mustBe "yes"
      }

      "must produce valid JSON structure" in {
        val json = Json.toJson(completeCreateResidencyRequest)

        json mustBe a[JsObject]
        (json \ "residency").asOpt[JsObject].value.keys must contain allOf(
          "isNonUkResidents",
          "isCompany",
          "isCrownRelief"
        )
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[CreateResidencyRequest]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(completeCreateResidencyRequest)
        val result = Json.fromJson[CreateResidencyRequest](json).asEither.value

        result mustEqual completeCreateResidencyRequest
      }
    }
  }

  "CreateResidencyReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[CreateResidencyReturn]]
      }

      "must deserialize valid JSON" in {
        val result = Json.fromJson[CreateResidencyReturn](validCreateResidencyReturnJson).asEither.value

        result.created mustBe true
      }

      "must fail to deserialize when created field is missing" in {
        val json = Json.obj()
        val result = Json.fromJson[CreateResidencyReturn](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[CreateResidencyReturn]]
      }

      "must serialize CreateResidencyReturn" in {
        val json = Json.toJson(createResidencyReturn)

        (json \ "created").as[Boolean] mustBe true
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[CreateResidencyReturn]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(createResidencyReturn)
        val result = Json.fromJson[CreateResidencyReturn](json).asEither.value

        result mustEqual createResidencyReturn
      }
    }
  }

  "UpdateResidencyRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[UpdateResidencyRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[UpdateResidencyRequest](validUpdateResidencyRequestJson).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.residency.isNonUkResidents mustBe "no"
        result.residency.isCompany mustBe "yes"
        result.residency.isCrownRelief mustBe "no"
      }

      "must fail to deserialize when stornId is missing" in {
        val json = validUpdateResidencyRequestJson - "stornId"
        val result = Json.fromJson[UpdateResidencyRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when returnResourceRef is missing" in {
        val json = validUpdateResidencyRequestJson - "returnResourceRef"
        val result = Json.fromJson[UpdateResidencyRequest](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[UpdateResidencyRequest]]
      }

      "must serialize UpdateResidencyRequest with all fields" in {
        val json = Json.toJson(completeUpdateResidencyRequest)

        (json \ "stornId").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"

        val residencyJson = json \ "residency"

        (residencyJson \ "isNonUkResidents").as[String] mustBe "no"
        (residencyJson \ "isCompany").as[String] mustBe "yes"
        (residencyJson \ "isCrownRelief").as[String] mustBe "no"
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[UpdateResidencyRequest]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(completeUpdateResidencyRequest)
        val result = Json.fromJson[UpdateResidencyRequest](json).asEither.value

        result mustEqual completeUpdateResidencyRequest
      }
    }
  }

  "DeleteResidencyRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[DeleteResidencyRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[DeleteResidencyRequest](validDeleteResidencyRequestJson).asEither.value

        result.storn mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
      }

      "must fail to deserialize when storn is missing" in {
        val json = validDeleteResidencyRequestJson - "storn"
        val result = Json.fromJson[DeleteResidencyRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when returnResourceRef is missing" in {
        val json = validDeleteResidencyRequestJson - "returnResourceRef"
        val result = Json.fromJson[DeleteResidencyRequest](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[DeleteResidencyRequest]]
      }

      "must serialize DeleteResidencyRequest with all fields" in {
        val json = Json.toJson(deleteResidencyRequest)

        (json \ "storn").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[DeleteResidencyRequest]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(deleteResidencyRequest)
        val result = Json.fromJson[DeleteResidencyRequest](json).asEither.value

        result mustEqual deleteResidencyRequest
      }
    }
  }

  "DeleteResidencyReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[DeleteResidencyReturn]]
      }

      "must deserialize valid JSON with deleted true" in {
        val result = Json.fromJson[DeleteResidencyReturn](validDeleteResidencyReturnJsonTrue).asEither.value
        result.deleted mustBe true
      }

      "must deserialize valid JSON with deleted false" in {
        val result = Json.fromJson[DeleteResidencyReturn](validDeleteResidencyReturnJsonFalse).asEither.value
        result.deleted mustBe false
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[DeleteResidencyReturn]]
      }

      "must serialize DeleteResidencyReturn with deleted true" in {
        val json = Json.toJson(deleteResidencyReturnTrue)
        (json \ "deleted").as[Boolean] mustBe true
      }

      "must serialize DeleteResidencyReturn with deleted false" in {
        val json = Json.toJson(deleteResidencyReturnFalse)
        (json \ "deleted").as[Boolean] mustBe false
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[DeleteResidencyReturn]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(deleteResidencyReturnTrue)
        val result = Json.fromJson[DeleteResidencyReturn](json).asEither.value

        result mustEqual deleteResidencyReturnTrue
      }
    }
  }

  "ResidencyPayload" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[ResidencyPayload]]
      }

      "must deserialize valid JSON" in {
        val json = Json.obj(
          "isNonUkResidents" -> "yes",
          "isCompany" -> "no",
          "isCrownRelief" -> "yes"
        )
        val result = Json.fromJson[ResidencyPayload](json).asEither.value

        result.isNonUkResidents mustBe "yes"
        result.isCompany mustBe "no"
        result.isCrownRelief mustBe "yes"
      }

      "must fail to deserialize when isNonUkResidents is missing" in {
        val json = Json.obj("isCompany" -> "no", "isCrownRelief" -> "yes")
        val result = Json.fromJson[ResidencyPayload](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[ResidencyPayload]]
      }

      "must serialize ResidencyPayload" in {
        val payload = ResidencyPayload(isNonUkResidents = "yes", isCompany = "no", isCrownRelief = "yes")
        val json = Json.toJson(payload)

        (json \ "isNonUkResidents").as[String] mustBe "yes"
        (json \ "isCompany").as[String] mustBe "no"
        (json \ "isCrownRelief").as[String] mustBe "yes"
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[ResidencyPayload]]
      }

      "must round-trip serialize and deserialize" in {
        val payload = ResidencyPayload(isNonUkResidents = "yes", isCompany = "no", isCrownRelief = "yes")
        val json = Json.toJson(payload)
        val result = Json.fromJson[ResidencyPayload](json).asEither.value

        result mustEqual payload
      }
    }
  }

  "UpdateResidencyReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[UpdateResidencyReturn]]
      }

      "must deserialize valid JSON with updated true" in {
        val json = Json.obj("updated" -> true)
        val result = Json.fromJson[UpdateResidencyReturn](json).asEither.value
        result.updated mustBe true
      }

      "must deserialize valid JSON with updated false" in {
        val json = Json.obj("updated" -> false)
        val result = Json.fromJson[UpdateResidencyReturn](json).asEither.value
        result.updated mustBe false
      }

      "must fail to deserialize when updated field is missing" in {
        val json = Json.obj()
        val result = Json.fromJson[UpdateResidencyReturn](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[UpdateResidencyReturn]]
      }

      "must serialize UpdateResidencyReturn with updated true" in {
        val json = Json.toJson(UpdateResidencyReturn(updated = true))
        (json \ "updated").as[Boolean] mustBe true
      }

      "must serialize UpdateResidencyReturn with updated false" in {
        val json = Json.toJson(UpdateResidencyReturn(updated = false))
        (json \ "updated").as[Boolean] mustBe false
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[UpdateResidencyReturn]]
      }

      "must round-trip serialize and deserialize" in {
        val updateResidencyReturn = UpdateResidencyReturn(updated = true)
        val json = Json.toJson(updateResidencyReturn)
        val result = Json.fromJson[UpdateResidencyReturn](json).asEither.value

        result mustEqual updateResidencyReturn
      }
    }
  }
}