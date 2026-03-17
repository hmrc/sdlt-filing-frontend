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

import base.SpecBase
import constants.FullReturnConstants.incompleteFullReturn
import models.Land
import org.scalatest.RecoverMethods.recoverToExceptionIf
import play.api.libs.json.{JsSuccess, Json}

import scala.concurrent.ExecutionContext

class LandModelsSpec extends SpecBase {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val completeLand = Land(
    landID = Some("LAND001"),
    landResourceRef = Some("LAND-REF-001"),
    propertyType = Some("NonResidential"),
    interestCreatedTransferred = Some("Transfer"),
    address1 = Some("1 Test Street"),
    address2 = Some("Test Town"),
    houseNumber = None,
    address3 = None,
    address4 = None,
    postcode = Some("AB1 2CD"),
    landArea = None,
    areaUnit = None,
    localAuthorityNumber = Some("1234"),
    mineralRights = None,
    NLPGUPRN = None,
    willSendPlanByPost = None,
    titleNumber = None,
    nextLandID = None
  )

  private val completeLandFullReturn = incompleteFullReturn.copy(
    land = Some(Seq(completeLand))
  )

  private val userAnswersWithFullReturn = emptyUserAnswers.copy(
    fullReturn = Some(completeLandFullReturn)
  )

  "CreateLandRequest" - {

    "must serialize to JSON correctly with all fields populated for residential property" in {
      val request = CreateLandRequest(
        stornId = "STORN12345",
        returnResourceRef = "100001",
        propertyType = "RESIDENTIAL",
        interestTransferredCreated = "FREEHOLD",
        houseNumber = Some("123"),
        addressLine1 = "Main Street",
        addressLine2 = Some("Apartment 4B"),
        addressLine3 = Some("City Center"),
        addressLine4 = Some("Greater London"),
        postcode = Some("SW1A 1AA"),
        landArea = Some("500"),
        areaUnit = Some("SQUARE_METERS"),
        localAuthorityNumber = Some("LA12345"),
        mineralRights = Some("YES"),
        nlpgUprn = Some("100012345678"),
        willSendPlansByPost = Some("NO"),
        titleNumber = Some("TN123456")
      )

      val json = Json.toJson(request)

      (json \ "stornId").as[String] mustBe "STORN12345"
      (json \ "returnResourceRef").as[String] mustBe "100001"
      (json \ "propertyType").as[String] mustBe "RESIDENTIAL"
      (json \ "interestTransferredCreated").as[String] mustBe "FREEHOLD"
      (json \ "houseNumber").as[String] mustBe "123"
      (json \ "addressLine1").as[String] mustBe "Main Street"
      (json \ "addressLine2").as[String] mustBe "Apartment 4B"
      (json \ "addressLine3").as[String] mustBe "City Center"
      (json \ "addressLine4").as[String] mustBe "Greater London"
      (json \ "postcode").as[String] mustBe "SW1A 1AA"
      (json \ "landArea").as[String] mustBe "500"
      (json \ "areaUnit").as[String] mustBe "SQUARE_METERS"
      (json \ "localAuthorityNumber").as[String] mustBe "LA12345"
      (json \ "mineralRights").as[String] mustBe "YES"
      (json \ "nlpgUprn").as[String] mustBe "100012345678"
      (json \ "willSendPlansByPost").as[String] mustBe "NO"
      (json \ "titleNumber").as[String] mustBe "TN123456"
    }

    "must serialize to JSON correctly with all fields populated for non-residential property" in {
      val request = CreateLandRequest(
        stornId = "STORN99999",
        returnResourceRef = "100002",
        propertyType = "NON_RESIDENTIAL",
        interestTransferredCreated = "LEASEHOLD",
        houseNumber = None,
        addressLine1 = "Business Park",
        addressLine2 = None,
        addressLine3 = None,
        addressLine4 = None,
        postcode = Some("EC1A 1BB"),
        landArea = Some("1000"),
        areaUnit = Some("SQUARE_FEET"),
        localAuthorityNumber = Some("LA99999"),
        mineralRights = Some("NO"),
        nlpgUprn = None,
        willSendPlansByPost = Some("YES"),
        titleNumber = Some("TN999888")
      )

      val json = Json.toJson(request)

      (json \ "stornId").as[String] mustBe "STORN99999"
      (json \ "returnResourceRef").as[String] mustBe "100002"
      (json \ "propertyType").as[String] mustBe "NON_RESIDENTIAL"
      (json \ "interestTransferredCreated").as[String] mustBe "LEASEHOLD"
      (json \ "addressLine1").as[String] mustBe "Business Park"
      (json \ "postcode").as[String] mustBe "EC1A 1BB"
      (json \ "landArea").as[String] mustBe "1000"
      (json \ "areaUnit").as[String] mustBe "SQUARE_FEET"
      (json \ "localAuthorityNumber").as[String] mustBe "LA99999"
      (json \ "mineralRights").as[String] mustBe "NO"
      (json \ "willSendPlansByPost").as[String] mustBe "YES"
      (json \ "titleNumber").as[String] mustBe "TN999888"
    }

    "must serialize to JSON correctly with all fields populated for mixed property" in {
      val request = CreateLandRequest(
        stornId = "STORN88888",
        returnResourceRef = "100003",
        propertyType = "MIXED",
        interestTransferredCreated = "FREEHOLD",
        houseNumber = Some("99"),
        addressLine1 = "High Street",
        addressLine2 = Some("Town Centre"),
        addressLine3 = Some("Manchester"),
        addressLine4 = None,
        postcode = Some("M1 1AA"),
        landArea = Some("750"),
        areaUnit = Some("SQUARE_METERS"),
        localAuthorityNumber = Some("LA88888"),
        mineralRights = Some("YES"),
        nlpgUprn = Some("100099887766"),
        willSendPlansByPost = Some("NO"),
        titleNumber = Some("TN888777")
      )

      val json = Json.toJson(request)

      (json \ "stornId").as[String] mustBe "STORN88888"
      (json \ "returnResourceRef").as[String] mustBe "100003"
      (json \ "propertyType").as[String] mustBe "MIXED"
      (json \ "interestTransferredCreated").as[String] mustBe "FREEHOLD"
      (json \ "houseNumber").as[String] mustBe "99"
      (json \ "addressLine1").as[String] mustBe "High Street"
      (json \ "addressLine2").as[String] mustBe "Town Centre"
      (json \ "addressLine3").as[String] mustBe "Manchester"
      (json \ "postcode").as[String] mustBe "M1 1AA"
      (json \ "landArea").as[String] mustBe "750"
      (json \ "areaUnit").as[String] mustBe "SQUARE_METERS"
    }

    "must serialize to JSON correctly with only required fields" in {
      val request = CreateLandRequest(
        stornId = "STORN12345",
        returnResourceRef = "100001",
        propertyType = "RESIDENTIAL",
        interestTransferredCreated = "FREEHOLD",
        houseNumber = None,
        addressLine1 = "Main Street",
        addressLine2 = None,
        addressLine3 = None,
        addressLine4 = None,
        postcode = None,
        landArea = None,
        areaUnit = None,
        localAuthorityNumber = None,
        mineralRights = None,
        nlpgUprn = None,
        willSendPlansByPost = None,
        titleNumber = None
      )

      val json = Json.toJson(request)

      (json \ "stornId").as[String] mustBe "STORN12345"
      (json \ "returnResourceRef").as[String] mustBe "100001"
      (json \ "propertyType").as[String] mustBe "RESIDENTIAL"
      (json \ "interestTransferredCreated").as[String] mustBe "FREEHOLD"
      (json \ "addressLine1").as[String] mustBe "Main Street"
      (json \ "houseNumber").toOption mustBe None
      (json \ "addressLine2").toOption mustBe None
      (json \ "postcode").toOption mustBe None
      (json \ "landArea").toOption mustBe None
    }

    "must deserialize from JSON correctly with all fields populated" in {
      val json = Json.obj(
        "stornId"                    -> "STORN12345",
        "returnResourceRef"          -> "100001",
        "propertyType"               -> "RESIDENTIAL",
        "interestTransferredCreated" -> "FREEHOLD",
        "houseNumber"                -> "123",
        "addressLine1"               -> "Main Street",
        "addressLine2"               -> "Apartment 4B",
        "addressLine3"               -> "City Center",
        "addressLine4"               -> "Greater London",
        "postcode"                   -> "SW1A 1AA",
        "landArea"                   -> "500",
        "areaUnit"                   -> "SQUARE_METERS",
        "localAuthorityNumber"       -> "LA12345",
        "mineralRights"              -> "YES",
        "nlpgUprn"                   -> "100012345678",
        "willSendPlansByPost"        -> "NO",
        "titleNumber"                -> "TN123456"
      )

      val result = json.validate[CreateLandRequest]

      result mustBe a[JsSuccess[_]]
      val request = result.get

      request.stornId mustBe "STORN12345"
      request.returnResourceRef mustBe "100001"
      request.propertyType mustBe "RESIDENTIAL"
      request.interestTransferredCreated mustBe "FREEHOLD"
      request.houseNumber mustBe Some("123")
      request.addressLine1 mustBe "Main Street"
      request.addressLine2 mustBe Some("Apartment 4B")
      request.addressLine3 mustBe Some("City Center")
      request.addressLine4 mustBe Some("Greater London")
      request.postcode mustBe Some("SW1A 1AA")
      request.landArea mustBe Some("500")
      request.areaUnit mustBe Some("SQUARE_METERS")
      request.localAuthorityNumber mustBe Some("LA12345")
      request.mineralRights mustBe Some("YES")
      request.nlpgUprn mustBe Some("100012345678")
      request.willSendPlansByPost mustBe Some("NO")
      request.titleNumber mustBe Some("TN123456")
    }

    "must deserialize from JSON correctly with only required fields" in {
      val json = Json.obj(
        "stornId"                    -> "STORN12345",
        "returnResourceRef"          -> "100001",
        "propertyType"               -> "RESIDENTIAL",
        "interestTransferredCreated" -> "FREEHOLD",
        "addressLine1"               -> "Main Street"
      )

      val result = json.validate[CreateLandRequest]

      result mustBe a[JsSuccess[_]]
      val request = result.get

      request.stornId mustBe "STORN12345"
      request.returnResourceRef mustBe "100001"
      request.propertyType mustBe "RESIDENTIAL"
      request.interestTransferredCreated mustBe "FREEHOLD"
      request.addressLine1 mustBe "Main Street"
      request.houseNumber mustBe None
      request.addressLine2 mustBe None
      request.postcode mustBe None
      request.landArea mustBe None
    }

    "must fail to deserialize when required field stornId is missing" in {
      val json = Json.obj(
        "returnResourceRef"          -> "100001",
        "propertyType"               -> "RESIDENTIAL",
        "interestTransferredCreated" -> "FREEHOLD",
        "addressLine1"               -> "Main Street"
      )

      json.validate[CreateLandRequest].isError mustBe true
    }

    "must fail to deserialize when required field returnResourceRef is missing" in {
      val json = Json.obj(
        "stornId"                    -> "STORN12345",
        "propertyType"               -> "RESIDENTIAL",
        "interestTransferredCreated" -> "FREEHOLD",
        "addressLine1"               -> "Main Street"
      )

      json.validate[CreateLandRequest].isError mustBe true
    }

    "must fail to deserialize when required field addressLine1 is missing" in {
      val json = Json.obj(
        "stornId"                    -> "STORN12345",
        "returnResourceRef"          -> "100001",
        "propertyType"               -> "RESIDENTIAL",
        "interestTransferredCreated" -> "FREEHOLD"
      )

      json.validate[CreateLandRequest].isError mustBe true
    }

    "must fail to deserialize when required field propertyType is missing" in {
      val json = Json.obj(
        "stornId"                    -> "STORN12345",
        "returnResourceRef"          -> "100001",
        "interestTransferredCreated" -> "FREEHOLD",
        "addressLine1"               -> "Main Street"
      )

      json.validate[CreateLandRequest].isError mustBe true
    }

    "must fail to deserialize when required field interestTransferredCreated is missing" in {
      val json = Json.obj(
        "stornId"           -> "STORN12345",
        "returnResourceRef" -> "100001",
        "propertyType"      -> "RESIDENTIAL",
        "addressLine1"      -> "Main Street"
      )

      json.validate[CreateLandRequest].isError mustBe true
    }

    "from" - {

      "must return a CreateLandRequest when all required fields are present" in {
        CreateLandRequest.from(userAnswersWithFullReturn, completeLand).futureValue mustBe a[CreateLandRequest]
      }

      "must fail with NoSuchElementException when fullReturn is None" in {
        recoverToExceptionIf[NoSuchElementException] {
          CreateLandRequest.from(emptyUserAnswers, completeLand)
        }.map { ex =>
          ex.getMessage mustBe "Full return not found"
        }
      }

      "must fail with NoSuchElementException when land mandatory fields are missing" in {
        val landMissingFields = completeLand.copy(propertyType = None)
        recoverToExceptionIf[NoSuchElementException] {
          CreateLandRequest.from(userAnswersWithFullReturn, landMissingFields)
        }.map { ex =>
          ex.getMessage mustBe "Land mandatory fields not found"
        }
      }
    }
  }

  "CreateLandReturn" - {

    "must serialize to JSON correctly" in {
      val response = CreateLandReturn(landResourceRef = "L100001", landId = "LID123")
      val json = Json.toJson(response)

      (json \ "landResourceRef").as[String] mustBe "L100001"
      (json \ "landId").as[String] mustBe "LID123"
    }

    "must deserialize from JSON correctly" in {
      val json = Json.obj("landResourceRef" -> "L100001", "landId" -> "LID123")
      val result = json.validate[CreateLandReturn]

      result mustBe a[JsSuccess[_]]
      result.get.landResourceRef mustBe "L100001"
      result.get.landId mustBe "LID123"
    }

    "must fail to deserialize when landResourceRef is missing" in {
      Json.obj("landId" -> "LID123").validate[CreateLandReturn].isError mustBe true
    }

    "must fail to deserialize when landId is missing" in {
      Json.obj("landResourceRef" -> "L100001").validate[CreateLandReturn].isError mustBe true
    }
  }

  "UpdateLandRequest" - {

    "must serialize to JSON correctly with all fields populated" in {
      val request = UpdateLandRequest(
        stornId = "STORN12345",
        returnResourceRef = "100001",
        landResourceRef = "L100001",
        propertyType = "RESIDENTIAL",
        interestTransferredCreated = "FREEHOLD",
        houseNumber = Some("456"),
        addressLine1 = "Oak Avenue",
        addressLine2 = Some("Suite 10"),
        addressLine3 = Some("Updated City"),
        addressLine4 = Some("Greater Manchester"),
        postcode = Some("W1A 1AA"),
        landArea = Some("750"),
        areaUnit = Some("SQUARE_METERS"),
        localAuthorityNumber = Some("LA54321"),
        mineralRights = Some("NO"),
        nlpgUprn = Some("100087654321"),
        willSendPlansByPost = Some("YES"),
        titleNumber = Some("TN654321"),
        nextLandId = Some("100002")
      )

      val json = Json.toJson(request)

      (json \ "stornId").as[String] mustBe "STORN12345"
      (json \ "returnResourceRef").as[String] mustBe "100001"
      (json \ "landResourceRef").as[String] mustBe "L100001"
      (json \ "propertyType").as[String] mustBe "RESIDENTIAL"
      (json \ "interestTransferredCreated").as[String] mustBe "FREEHOLD"
      (json \ "houseNumber").as[String] mustBe "456"
      (json \ "addressLine1").as[String] mustBe "Oak Avenue"
      (json \ "addressLine2").as[String] mustBe "Suite 10"
      (json \ "addressLine3").as[String] mustBe "Updated City"
      (json \ "addressLine4").as[String] mustBe "Greater Manchester"
      (json \ "postcode").as[String] mustBe "W1A 1AA"
      (json \ "landArea").as[String] mustBe "750"
      (json \ "areaUnit").as[String] mustBe "SQUARE_METERS"
      (json \ "localAuthorityNumber").as[String] mustBe "LA54321"
      (json \ "mineralRights").as[String] mustBe "NO"
      (json \ "nlpgUprn").as[String] mustBe "100087654321"
      (json \ "willSendPlansByPost").as[String] mustBe "YES"
      (json \ "titleNumber").as[String] mustBe "TN654321"
      (json \ "nextLandId").as[String] mustBe "100002"
    }

    "must serialize to JSON correctly with only required fields" in {
      val request = UpdateLandRequest(
        stornId = "STORN99999",
        returnResourceRef = "100002",
        landResourceRef = "L100002",
        propertyType = "NON_RESIDENTIAL",
        interestTransferredCreated = "LEASEHOLD",
        houseNumber = None,
        addressLine1 = "Updated Business Park",
        addressLine2 = None,
        addressLine3 = None,
        addressLine4 = None,
        postcode = Some("EC2A 2BB"),
        landArea = None,
        areaUnit = None,
        localAuthorityNumber = None,
        mineralRights = None,
        nlpgUprn = None,
        willSendPlansByPost = None,
        titleNumber = None,
        nextLandId = None
      )

      val json = Json.toJson(request)

      (json \ "stornId").as[String] mustBe "STORN99999"
      (json \ "returnResourceRef").as[String] mustBe "100002"
      (json \ "landResourceRef").as[String] mustBe "L100002"
      (json \ "propertyType").as[String] mustBe "NON_RESIDENTIAL"
      (json \ "interestTransferredCreated").as[String] mustBe "LEASEHOLD"
      (json \ "addressLine1").as[String] mustBe "Updated Business Park"
      (json \ "postcode").as[String] mustBe "EC2A 2BB"
      (json \ "nextLandId").toOption mustBe None
    }

    "must serialize to JSON correctly for mixed property with partial fields" in {
      val request = UpdateLandRequest(
        stornId = "STORN77777",
        returnResourceRef = "100003",
        landResourceRef = "L100003",
        propertyType = "MIXED",
        interestTransferredCreated = "FREEHOLD",
        houseNumber = Some("1"),
        addressLine1 = "New Street",
        addressLine2 = None,
        addressLine3 = None,
        addressLine4 = None,
        postcode = Some("NE1 1AA"),
        landArea = Some("600"),
        areaUnit = Some("SQUARE_FEET"),
        localAuthorityNumber = None,
        mineralRights = None,
        nlpgUprn = None,
        willSendPlansByPost = None,
        titleNumber = None,
        nextLandId = None
      )

      val json = Json.toJson(request)

      (json \ "propertyType").as[String] mustBe "MIXED"
      (json \ "houseNumber").as[String] mustBe "1"
      (json \ "addressLine1").as[String] mustBe "New Street"
      (json \ "landArea").as[String] mustBe "600"
      (json \ "areaUnit").as[String] mustBe "SQUARE_FEET"
    }

    "must deserialize from JSON correctly with all fields populated" in {
      val json = Json.obj(
        "stornId"                    -> "STORN12345",
        "returnResourceRef"          -> "100001",
        "landResourceRef"            -> "L100001",
        "propertyType"               -> "RESIDENTIAL",
        "interestTransferredCreated" -> "FREEHOLD",
        "houseNumber"                -> "456",
        "addressLine1"               -> "Oak Avenue",
        "addressLine2"               -> "Suite 10",
        "addressLine3"               -> "Updated City",
        "postcode"                   -> "W1A 1AA",
        "landArea"                   -> "750",
        "areaUnit"                   -> "SQUARE_METERS",
        "localAuthorityNumber"       -> "LA54321",
        "mineralRights"              -> "NO",
        "nlpgUprn"                   -> "100087654321",
        "willSendPlansByPost"        -> "YES",
        "titleNumber"                -> "TN654321",
        "nextLandId"                 -> "100002"
      )

      val result = json.validate[UpdateLandRequest]

      result mustBe a[JsSuccess[_]]
      val request = result.get

      request.stornId mustBe "STORN12345"
      request.returnResourceRef mustBe "100001"
      request.landResourceRef mustBe "L100001"
      request.propertyType mustBe "RESIDENTIAL"
      request.interestTransferredCreated mustBe "FREEHOLD"
      request.houseNumber mustBe Some("456")
      request.addressLine1 mustBe "Oak Avenue"
      request.addressLine2 mustBe Some("Suite 10")
      request.nextLandId mustBe Some("100002")
    }

    "must deserialize from JSON correctly with only required fields" in {
      val json = Json.obj(
        "stornId"                    -> "STORN99999",
        "returnResourceRef"          -> "100002",
        "landResourceRef"            -> "L100002",
        "propertyType"               -> "NON_RESIDENTIAL",
        "interestTransferredCreated" -> "LEASEHOLD",
        "addressLine1"               -> "Updated Business Park"
      )

      val result = json.validate[UpdateLandRequest]

      result mustBe a[JsSuccess[_]]
      val request = result.get

      request.stornId mustBe "STORN99999"
      request.landResourceRef mustBe "L100002"
      request.nextLandId mustBe None
    }

    "must fail to deserialize when required field landResourceRef is missing" in {
      val json = Json.obj(
        "stornId"                    -> "STORN12345",
        "returnResourceRef"          -> "100001",
        "propertyType"               -> "RESIDENTIAL",
        "interestTransferredCreated" -> "FREEHOLD",
        "addressLine1"               -> "Oak Avenue"
      )

      json.validate[UpdateLandRequest].isError mustBe true
    }

    "must fail to deserialize when required field propertyType is missing" in {
      val json = Json.obj(
        "stornId"                    -> "STORN12345",
        "returnResourceRef"          -> "100001",
        "landResourceRef"            -> "L100001",
        "interestTransferredCreated" -> "FREEHOLD",
        "addressLine1"               -> "Oak Avenue"
      )

      json.validate[UpdateLandRequest].isError mustBe true
    }

    "from" - {

      "must return an UpdateLandRequest when all required fields are present" in {
        UpdateLandRequest.from(userAnswersWithFullReturn, completeLand).futureValue mustBe a[UpdateLandRequest]
      }

      "must fail with NoSuchElementException when fullReturn is None" in {
        recoverToExceptionIf[NoSuchElementException] {
          UpdateLandRequest.from(emptyUserAnswers, completeLand)
        }.map { ex =>
          ex.getMessage mustBe "Full return not found"
        }
      }

      "must fail with NoSuchElementException when land mandatory fields are missing" in {
        val landMissingFields = completeLand.copy(landResourceRef = None)
        recoverToExceptionIf[NoSuchElementException] {
          UpdateLandRequest.from(userAnswersWithFullReturn, landMissingFields)
        }.map { ex =>
          ex.getMessage mustBe "Land mandatory fields not found"
        }
      }
    }
  }

  "UpdateLandReturn" - {

    "must serialize to JSON correctly when updated is true" in {
      val json = Json.toJson(UpdateLandReturn(updated = true))
      (json \ "updated").as[Boolean] mustBe true
    }

    "must serialize to JSON correctly when updated is false" in {
      val json = Json.toJson(UpdateLandReturn(updated = false))
      (json \ "updated").as[Boolean] mustBe false
    }

    "must deserialize from JSON correctly when updated is true" in {
      val result = Json.obj("updated" -> true).validate[UpdateLandReturn]
      result mustBe a[JsSuccess[_]]
      result.get.updated mustBe true
    }

    "must fail to deserialize when updated field is missing" in {
      Json.obj().validate[UpdateLandReturn].isError mustBe true
    }
  }

  "DeleteLandRequest" - {

    val request = DeleteLandRequest(
      storn = "STORN12345",
      returnResourceRef = "100001",
      landResourceRef = "L100001"
    )

    "must serialize to JSON correctly" in {
      val json = Json.toJson(request)

      (json \ "storn").as[String] mustBe "STORN12345"
      (json \ "returnResourceRef").as[String] mustBe "100001"
      (json \ "landResourceRef").as[String] mustBe "L100001"
    }

    "from" - {

      "must return a DeleteLandRequest when land is found" in {
        val userAnswers = emptyUserAnswers.copy(
          fullReturn = Some(completeLandFullReturn.copy(
            land = Some(Seq(completeLand.copy(landResourceRef = Some("L100001"))))
          ))
        )

        DeleteLandRequest.from(userAnswers, "L100001").futureValue mustBe DeleteLandRequest(
          storn = completeLandFullReturn.stornId,
          returnResourceRef = completeLandFullReturn.returnResourceRef,
          landResourceRef = "L100001"
        )
      }

      "must fail with NoSuchElementException when land is not found" in {
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(completeLandFullReturn))

        recoverToExceptionIf[NoSuchElementException] {
          DeleteLandRequest.from(userAnswers, "L-NOT-FOUND")
        }.map { ex =>
          ex.getMessage mustBe "Land not found"
        }
      }

      "must fail with NoSuchElementException when fullReturn is None" in {
        recoverToExceptionIf[NoSuchElementException] {
          DeleteLandRequest.from(emptyUserAnswers, "L100001")
        }.map { ex =>
          ex.getMessage mustBe "Full return not found"
        }
      }
    }

    "must deserialize from JSON correctly" in {
      val json = Json.obj(
        "storn"             -> "STORN12345",
        "returnResourceRef" -> "100001",
        "landResourceRef"   -> "L100001"
      )

      val result = json.validate[DeleteLandRequest]

      result mustBe a[JsSuccess[_]]
      result.get.storn mustBe "STORN12345"
      result.get.returnResourceRef mustBe "100001"
      result.get.landResourceRef mustBe "L100001"
    }

    "must fail to deserialize when storn is missing" in {
      Json.obj("returnResourceRef" -> "100001", "landResourceRef" -> "L100001")
        .validate[DeleteLandRequest].isError mustBe true
    }

    "must fail to deserialize when returnResourceRef is missing" in {
      Json.obj("storn" -> "STORN12345", "landResourceRef" -> "L100001")
        .validate[DeleteLandRequest].isError mustBe true
    }

    "must fail to deserialize when landResourceRef is missing" in {
      Json.obj("storn" -> "STORN12345", "returnResourceRef" -> "100001")
        .validate[DeleteLandRequest].isError mustBe true
    }
  }

  "DeleteLandReturn" - {

    "must serialize to JSON correctly when deleted is true" in {
      val json = Json.toJson(DeleteLandReturn(deleted = true))
      (json \ "deleted").as[Boolean] mustBe true
    }

    "must serialize to JSON correctly when deleted is false" in {
      val json = Json.toJson(DeleteLandReturn(deleted = false))
      (json \ "deleted").as[Boolean] mustBe false
    }

    "must deserialize from JSON correctly when deleted is true" in {
      val result = Json.obj("deleted" -> true).validate[DeleteLandReturn]
      result mustBe a[JsSuccess[_]]
      result.get.deleted mustBe true
    }

    "must fail to deserialize when deleted field is missing" in {
      Json.obj().validate[DeleteLandReturn].isError mustBe true
    }
  }
}