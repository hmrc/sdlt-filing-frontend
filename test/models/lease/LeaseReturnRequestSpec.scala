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

package models.lease

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json.*


class LeaseReturnRequestSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {
  
  private val validCreateLeaseRequestJsonComplete = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "lease" -> Json.obj(
      "leaseType" -> "R",
      "contractStartDate" -> "2000-01-01",
      "contractEndDate" -> "2026-01-01",
      "rentFreePeriod" -> "50",
      "startingRent" -> "50.00",
      "startingRentEndDate" -> "2024-01-01",
      "laterRentKnown" -> "yes",
      "isAnnualRentOver1000" -> "yes",
      "vatAmount" -> "50.00",
      "totalPremiumPayable" -> "50.00",
      "netPresentValue" -> "50.00"
    )
  )

  private val validCreateLeaseRequestJsonMinimal = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "lease" -> Json.obj(
      "leaseType" -> "R",
      "contractStartDate" -> "2000-01-01",
      "contractEndDate" -> "2026-01-01",
      "rentFreePeriod" -> "50",
      "startingRent" -> "50.00",
      "startingRentEndDate" -> "2024-01-01",
      "laterRentKnown" -> "yes",
      "isAnnualRentOver1000" -> "yes",
      "vatAmount" -> "50.00"
    )
  )

  private val completeCreateLeaseRequest = CreateLeaseRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    lease = LeasePayload(
      isAnnualRentOver1000 = Some("yes"),
      contractEndDate = Some("2026-01-01"),
      contractStartDate = Some("2000-01-01"),
      leaseType = Some("R"),
      netPresentValue = Some("50.00"),
      totalPremiumPayable = Some("50.00"),
      rentFreePeriod = Some("50"),
      startingRent = Some("50.00"),
      startingRentEndDate = Some("2024-01-01"),
      laterRentKnown = Some("yes"),
      vatAmount = Some("50.00")
    )
  )

  private val minimalCreateLeaseRequest = CreateLeaseRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    lease = LeasePayload(
      isAnnualRentOver1000 = Some("yes"),
      contractEndDate = Some("2026-01-01"),
      contractStartDate = Some("2000-01-01"),
      leaseType = Some("R"),
      netPresentValue = None,
      totalPremiumPayable = None,
      rentFreePeriod = Some("50"),
      startingRent = Some("50.00"),
      startingRentEndDate = Some("2024-01-01"),
      laterRentKnown = Some("yes"),
      vatAmount = Some("50.00")
    )
  )

  private val validCreateLeaseReturnJson = Json.obj(
    "created" -> true
  )

  private val createLeaseReturn = CreateLeaseReturn(
    created = true
  )

  private val validUpdateLeaseRequestJsonComplete = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "lease" -> Json.obj(
      "leaseType" -> "R",
      "contractStartDate" -> "2022-01-01",
      "contractEndDate" -> "2026-01-01",
      "rentFreePeriod" -> "50",
      "startingRent" -> "50.00",
      "startingRentEndDate" -> "2024-01-01",
      "laterRentKnown" -> "yes",
      "isAnnualRentOver1000" -> "yes",
      "vatAmount" -> "50.00",
      "totalPremiumPayable" -> "50.00",
      "netPresentValue" -> "50.00"
    )
  )

  private val completeUpdateLeaseRequest = UpdateLeaseRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    lease = LeasePayload(
      isAnnualRentOver1000 = Some("yes"),
      contractEndDate = Some("2026-01-01"),
      contractStartDate = Some("2022-01-01"),
      leaseType = Some("R"),
      netPresentValue = Some("50.00"),
      totalPremiumPayable = Some("50.00"),
      rentFreePeriod = Some("50"),
      startingRent = Some("50.00"),
      startingRentEndDate = Some("2024-01-01"),
      laterRentKnown = Some("yes"),
      vatAmount = Some("50.00")
    )
  )

  private val validDeleteLeaseRequestJson = Json.obj(
    "storn" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001"
  )

  private val deleteLeaseRequest = DeleteLeaseRequest(
    storn = "STORN12345",
    returnResourceRef = "RRF-2024-001"
  )

  private val validDeleteLeaseReturnJsonTrue = Json.obj("deleted" -> true)
  private val validDeleteLeaseReturnJsonFalse = Json.obj("deleted" -> false)
  private val deleteLeaseReturnTrue = DeleteLeaseReturn(deleted = true)
  private val deleteLeaseReturnFalse = DeleteLeaseReturn(deleted = false)

  "CreateLeaseRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[CreateLeaseRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[CreateLeaseRequest](validCreateLeaseRequestJsonComplete).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.lease.leaseType mustBe Some("R")
        result.lease.contractStartDate mustBe Some("2000-01-01")
        result.lease.contractEndDate mustBe Some("2026-01-01")
        result.lease.rentFreePeriod mustBe Some("50")
        result.lease.startingRent mustBe Some("50.00")
        result.lease.startingRentEndDate mustBe Some("2024-01-01")
        result.lease.laterRentKnown mustBe Some("yes")
        result.lease.isAnnualRentOver1000 mustBe Some("yes")
        result.lease.vatAmount mustBe Some("50.00")
        result.lease.totalPremiumPayable mustBe Some("50.00")
        result.lease.netPresentValue mustBe Some("50.00")
      }

      "must deserialize valid JSON with only required fields" in {
        val result = Json.fromJson[CreateLeaseRequest](validCreateLeaseRequestJsonMinimal).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.lease.leaseType mustBe Some("R")
        result.lease.contractStartDate mustBe Some("2000-01-01")
        result.lease.contractEndDate mustBe Some("2026-01-01")
        result.lease.rentFreePeriod mustBe Some("50")
        result.lease.startingRent mustBe Some("50.00")
        result.lease.startingRentEndDate mustBe Some("2024-01-01")
        result.lease.laterRentKnown mustBe Some("yes")
        result.lease.isAnnualRentOver1000 mustBe Some("yes")
        result.lease.vatAmount mustBe Some("50.00")
      }

      "must fail to deserialize when stornId is missing" in {
        val json = validCreateLeaseRequestJsonComplete - "stornId"
        val result = Json.fromJson[CreateLeaseRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when returnResourceRef is missing" in {
        val json = validCreateLeaseRequestJsonComplete - "returnResourceRef"
        val result = Json.fromJson[CreateLeaseRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when required field has invalid type" in {
        val json = validCreateLeaseRequestJsonComplete ++ Json.obj("stornId" -> 123)
        val result = Json.fromJson[CreateLeaseRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize completely invalid JSON structure" in {
        val json = Json.obj("invalidField" -> "value")
        val result = Json.fromJson[CreateLeaseRequest](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[CreateLeaseRequest]]
      }

      "must serialize CreateLeaseRequest with all fields" in {
        val json = Json.toJson(completeCreateLeaseRequest)

        (json \ "stornId").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"

        val leaseJson = json \ "lease"

        (leaseJson \ "leaseType").as[String] mustBe "R"
        (leaseJson \ "contractEndDate").as[String] mustBe "2026-01-01"
        (leaseJson \ "contractStartDate").as[String] mustBe "2000-01-01"
        (leaseJson \ "netPresentValue").as[String] mustBe "50.00"
        (leaseJson \ "totalPremiumPayable").as[String] mustBe "50.00"
        (leaseJson \ "rentFreePeriod").as[String] mustBe "50"
        (leaseJson \ "startingRent").as[String] mustBe "50.00"
        (leaseJson \ "startingRentEndDate").as[String] mustBe "2024-01-01"
        (leaseJson \ "laterRentKnown").as[String] mustBe "yes"
        (leaseJson \ "vatAmount").as[String] mustBe "50.00"
      }

      "must serialize CreateLeaseRequest with only required fields" in {
        val json = Json.toJson(minimalCreateLeaseRequest)

        (json \ "stornId").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"

        val leaseJson = json \ "lease"

        (leaseJson \ "leaseType").as[String] mustBe "R"
        (leaseJson \ "contractEndDate").as[String] mustBe "2026-01-01"
        (leaseJson \ "contractStartDate").as[String] mustBe "2000-01-01"
        (leaseJson \ "rentFreePeriod").as[String] mustBe "50"
        (leaseJson \ "startingRent").as[String] mustBe "50.00"
        (leaseJson \ "startingRentEndDate").as[String] mustBe "2024-01-01"
        (leaseJson \ "laterRentKnown").as[String] mustBe "yes"
        (leaseJson \ "vatAmount").as[String] mustBe "50.00"
        (leaseJson \ "netPresentValue").toOption mustBe None
        (leaseJson \ "totalPremiumPayable").toOption mustBe None
      }

      "must produce valid JSON structure" in {
        val json = Json.toJson(completeCreateLeaseRequest)

        json mustBe a[JsObject]
        (json \ "lease").asOpt[JsObject].value.keys must contain allOf(
          "leaseType",
          "contractEndDate",
          "contractStartDate",
          "rentFreePeriod",
          "startingRent",
          "startingRentEndDate",
          "netPresentValue",
          "totalPremiumPayable"
        )
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[CreateLeaseRequest]]
      }

      "must round-trip serialize and deserialize with all fields" in {
        val json = Json.toJson(completeCreateLeaseRequest)
        val result = Json.fromJson[CreateLeaseRequest](json).asEither.value

        result mustEqual completeCreateLeaseRequest
      }

      "must round-trip serialize and deserialize with only required fields" in {
        val json = Json.toJson(minimalCreateLeaseRequest)
        val result = Json.fromJson[CreateLeaseRequest](json).asEither.value

        result mustEqual minimalCreateLeaseRequest
      }
    }
  }

  "CreateLeaseReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[CreateLeaseReturn]]
      }

      "must deserialize valid JSON" in {
        val result = Json.fromJson[CreateLeaseReturn](validCreateLeaseReturnJson).asEither.value

        result.created mustBe true
      }

      "must fail to deserialize when return field is missing" in {
        val json = Json.obj()
        val result = Json.fromJson[CreateLeaseReturn](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[CreateLeaseReturn]]
      }

      "must serialize CreateLeaseReturn" in {
        val json = Json.toJson(createLeaseReturn)

        (json \ "created").as[Boolean] mustBe true
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[CreateLeaseReturn]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(createLeaseReturn)
        val result = Json.fromJson[CreateLeaseReturn](json).asEither.value

        result mustEqual createLeaseReturn
      }
    }
  }

  "UpdateLeaseRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[UpdateLeaseRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[UpdateLeaseRequest](validUpdateLeaseRequestJsonComplete).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.lease.leaseType mustBe Some("R")
        result.lease.contractStartDate mustBe Some("2022-01-01")
        result.lease.contractEndDate mustBe Some("2026-01-01")
        result.lease.rentFreePeriod mustBe Some("50")
        result.lease.startingRent mustBe Some("50.00")
        result.lease.startingRentEndDate mustBe Some("2024-01-01")
        result.lease.laterRentKnown mustBe Some("yes")
        result.lease.isAnnualRentOver1000 mustBe Some("yes")
        result.lease.vatAmount mustBe Some("50.00")
        result.lease.totalPremiumPayable mustBe Some("50.00")
        result.lease.netPresentValue mustBe Some("50.00")
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[UpdateLeaseRequest]]
      }

      "must serialize UpdateLeaseRequest with all fields" in {
        val json = Json.toJson(completeUpdateLeaseRequest)

        (json \ "stornId").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"

        val leaseJson = json \ "lease"

        (leaseJson \ "leaseType").as[String] mustBe "R"
        (leaseJson \ "contractEndDate").as[String] mustBe "2026-01-01"
        (leaseJson \ "contractStartDate").as[String] mustBe "2022-01-01"
        (leaseJson \ "netPresentValue").as[String] mustBe "50.00"
        (leaseJson \ "totalPremiumPayable").as[String] mustBe "50.00"
        (leaseJson \ "rentFreePeriod").as[String] mustBe "50"
        (leaseJson \ "startingRent").as[String] mustBe "50.00"
        (leaseJson \ "startingRentEndDate").as[String] mustBe "2024-01-01"
        (leaseJson \ "laterRentKnown").as[String] mustBe "yes"
        (leaseJson \ "vatAmount").as[String] mustBe "50.00"
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[UpdateLeaseRequest]]
      }

      "must round-trip serialize and deserialize with all fields" in {
        val json = Json.toJson(completeUpdateLeaseRequest)
        val result = Json.fromJson[UpdateLeaseRequest](json).asEither.value

        result mustEqual completeUpdateLeaseRequest
      }
    }
  }

  "DeleteLeaseRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[DeleteLeaseRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[DeleteLeaseRequest](validDeleteLeaseRequestJson).asEither.value

        result.storn mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
      }

      "must fail to deserialize when storn is missing" in {
        val json = validDeleteLeaseRequestJson - "storn"
        val result = Json.fromJson[DeleteLeaseRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when returnResourceRef is missing" in {
        val json = validDeleteLeaseRequestJson - "returnResourceRef"
        val result = Json.fromJson[DeleteLeaseRequest](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[DeleteLeaseRequest]]
      }

      "must serialize DeleteLeaseRequest with all fields" in {
        val json = Json.toJson(deleteLeaseRequest)

        (json \ "storn").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[DeleteLeaseRequest]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(deleteLeaseRequest)
        val result = Json.fromJson[DeleteLeaseRequest](json).asEither.value

        result mustEqual deleteLeaseRequest
      }
    }
  }

  "DeleteLeaseReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[DeleteLeaseReturn]]
      }

      "must deserialize valid JSON with deleted true" in {
        val result = Json.fromJson[DeleteLeaseReturn](validDeleteLeaseReturnJsonTrue).asEither.value
        result.deleted mustBe true
      }

      "must deserialize valid JSON with deleted false" in {
        val result = Json.fromJson[DeleteLeaseReturn](validDeleteLeaseReturnJsonFalse).asEither.value
        result.deleted mustBe false
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[DeleteLeaseReturn]]
      }

      "must serialize DeleteLeaseReturn with deleted true" in {
        val json = Json.toJson(deleteLeaseReturnTrue)
        (json \ "deleted").as[Boolean] mustBe true
      }

      "must serialize DeleteLeaseReturn with deleted false" in {
        val json = Json.toJson(deleteLeaseReturnFalse)
        (json \ "deleted").as[Boolean] mustBe false
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[DeleteLeaseReturn]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(deleteLeaseReturnTrue)
        val result = Json.fromJson[DeleteLeaseReturn](json).asEither.value

        result mustEqual deleteLeaseReturnTrue
      }
    }
    
  }
}