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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json.*

class PurchaserReturnRequestsSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {
  
  private val validCreatePurchaserRequestJsonComplete = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "isCompany" -> "N",
    "isTrustee" -> "N",
    "isConnectedToVendor" -> "N",
    "isRepresentedByAgent" -> "Y",
    "title" -> "Mr",
    "surname" -> "Jones",
    "forename1" -> "David",
    "forename2" -> "Michael",
    "houseNumber" -> "25",
    "address1" -> "Park Avenue",
    "address2" -> "Flat 3",
    "address3" -> "Central District",
    "address4" -> "London",
    "postcode" -> "SW1A 2AA",
    "phone" -> "02012345678",
    "nino" -> "AB123456C",
    "hasNino" -> "Y",
    "dateOfBirth" -> "1980-01-15"
  )

  private val validCreatePurchaserRequestJsonMinimal = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "isCompany" -> "N",
    "isTrustee" -> "N",
    "isConnectedToVendor" -> "N",
    "isRepresentedByAgent" -> "Y",
    "address1" -> "Park Avenue"
  )

  private val validCreatePurchaserRequestJsonCompany = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "isCompany" -> "Y",
    "isTrustee" -> "N",
    "isConnectedToVendor" -> "N",
    "isRepresentedByAgent" -> "Y",
    "companyName" -> "XYZ Properties Ltd",
    "address1" -> "Park Avenue",
    "isUkCompany" -> "Y",
    "registrationNumber" -> "12345678",
    "placeOfRegistration" -> "England and Wales"
  )

  private val completeCreatePurchaserRequest = CreatePurchaserRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    isCompany = "N",
    isTrustee = "N",
    isConnectedToVendor = "N",
    isRepresentedByAgent = "Y",
    title = Some("Mr"),
    surname = Some("Jones"),
    forename1 = Some("David"),
    forename2 = Some("Michael"),
    companyName = None,
    houseNumber = Some("25"),
    address1 = "Park Avenue",
    address2 = Some("Flat 3"),
    address3 = Some("Central District"),
    address4 = Some("London"),
    postcode = Some("SW1A 2AA"),
    phone = Some("02012345678"),
    nino = Some("AB123456C"),
    isUkCompany = None,
    hasNino = Some("Y"),
    dateOfBirth = Some("1980-01-15"),
    registrationNumber = None,
    placeOfRegistration = None
  )

  private val minimalCreatePurchaserRequest = CreatePurchaserRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    isCompany = "N",
    isTrustee = "N",
    isConnectedToVendor = "N",
    isRepresentedByAgent = "Y",
    address1 = "Park Avenue"
  )

  private val validCreatePurchaserReturnJson = Json.obj(
    "purchaserResourceRef" -> "PRF-001",
    "purchaserId" -> "PID-001"
  )

  private val createPurchaserReturn = CreatePurchaserReturn(
    purchaserResourceRef = "PRF-001",
    purchaserId = "PID-001"
  )

  private val validUpdatePurchaserRequestJsonComplete = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "purchaserResourceRef" -> "PRF-001",
    "isCompany" -> "N",
    "isTrustee" -> "N",
    "isConnectedToVendor" -> "N",
    "isRepresentedByAgent" -> "Y",
    "title" -> "Mr",
    "surname" -> "Jones Updated",
    "forename1" -> "David",
    "forename2" -> "Michael",
    "houseNumber" -> "25",
    "address1" -> "Park Avenue",
    "address2" -> "Flat 3",
    "address3" -> "Central District",
    "address4" -> "London",
    "postcode" -> "SW1A 2AA",
    "phone" -> "02012345678",
    "nino" -> "AB123456C",
    "nextPurchaserId" -> "PID-002",
    "hasNino" -> "Y",
    "dateOfBirth" -> "1980-01-15"
  )

  private val validUpdatePurchaserRequestJsonMinimal = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "purchaserResourceRef" -> "PRF-001",
    "isCompany" -> "N",
    "isTrustee" -> "N",
    "isConnectedToVendor" -> "N",
    "isRepresentedByAgent" -> "Y",
    "address1" -> "Park Avenue"
  )

  private val completeUpdatePurchaserRequest = UpdatePurchaserRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    purchaserResourceRef = "PRF-001",
    isCompany = "N",
    isTrustee = "N",
    isConnectedToVendor = "N",
    isRepresentedByAgent = "Y",
    title = Some("Mr"),
    surname = Some("Jones Updated"),
    forename1 = Some("David"),
    forename2 = Some("Michael"),
    companyName = None,
    houseNumber = Some("25"),
    address1 = "Park Avenue",
    address2 = Some("Flat 3"),
    address3 = Some("Central District"),
    address4 = Some("London"),
    postcode = Some("SW1A 2AA"),
    phone = Some("02012345678"),
    nino = Some("AB123456C"),
    nextPurchaserId = Some("PID-002"),
    isUkCompany = None,
    hasNino = Some("Y"),
    dateOfBirth = Some("1980-01-15"),
    registrationNumber = None,
    placeOfRegistration = None
  )

  private val minimalUpdatePurchaserRequest = UpdatePurchaserRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    purchaserResourceRef = "PRF-001",
    isCompany = "N",
    isTrustee = "N",
    isConnectedToVendor = "N",
    isRepresentedByAgent = "Y",
    address1 = "Park Avenue"
  )

  private val validUpdatePurchaserReturnJsonTrue = Json.obj("updated" -> true)
  private val validUpdatePurchaserReturnJsonFalse = Json.obj("updated" -> false)
  private val updatePurchaserReturnTrue = UpdatePurchaserReturn(updated = true)
  private val updatePurchaserReturnFalse = UpdatePurchaserReturn(updated = false)

  private val validDeletePurchaserRequestJson = Json.obj(
    "storn" -> "STORN12345",
    "purchaserId" -> "PUR001",
    "returnResourceRef" -> "RRF-2024-001"
  )

  private val deletePurchaserRequest = DeletePurchaserRequest(
    storn = "STORN12345",
    purchaserId = "PUR001",
    returnResourceRef = "RRF-2024-001"
  )

  private val validDeletePurchaserReturnJsonTrue = Json.obj("deleted" -> true)
  private val validDeletePurchaserReturnJsonFalse = Json.obj("deleted" -> false)
  private val deletePurchaserReturnTrue = DeletePurchaserReturn(deleted = true)
  private val deletePurchaserReturnFalse = DeletePurchaserReturn(deleted = false)

  private val validCreateCompanyDetailsRequestJsonComplete = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "purchaserResourceRef" -> "PRF-001",
    "utr" -> "1234567890",
    "vatReference" -> "GB123456789",
    "compTypeBank" -> "Y",
    "compTypeBuilder" -> "N",
    "compTypeBuildsoc" -> "N",
    "compTypeCentgov" -> "N",
    "compTypeIndividual" -> "N",
    "compTypeInsurance" -> "N",
    "compTypeLocalauth" -> "N",
    "compTypeOcharity" -> "N",
    "compTypeOcompany" -> "N",
    "compTypeOfinancial" -> "N",
    "compTypePartship" -> "N",
    "compTypeProperty" -> "N",
    "compTypePubliccorp" -> "N",
    "compTypeSoletrader" -> "N",
    "compTypePenfund" -> "N"
  )

  private val validCreateCompanyDetailsRequestJsonMinimal = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "purchaserResourceRef" -> "PRF-001"
  )

  private val completeCreateCompanyDetailsRequest = CreateCompanyDetailsRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    purchaserResourceRef = "PRF-001",
    utr = Some("1234567890"),
    vatReference = Some("GB123456789"),
    compTypeBank = Some("Y"),
    compTypeBuilder = Some("N"),
    compTypeBuildsoc = Some("N"),
    compTypeCentgov = Some("N"),
    compTypeIndividual = Some("N"),
    compTypeInsurance = Some("N"),
    compTypeLocalauth = Some("N"),
    compTypeOcharity = Some("N"),
    compTypeOcompany = Some("N"),
    compTypeOfinancial = Some("N"),
    compTypePartship = Some("N"),
    compTypeProperty = Some("N"),
    compTypePubliccorp = Some("N"),
    compTypeSoletrader = Some("N"),
    compTypePenfund = Some("N")
  )

  private val minimalCreateCompanyDetailsRequest = CreateCompanyDetailsRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    purchaserResourceRef = "PRF-001"
  )
  
  private val validCreateCompanyDetailsReturnJson = Json.obj("companyDetailsId" -> "CID-001")
  private val createCompanyDetailsReturn = CreateCompanyDetailsReturn(companyDetailsId = "CID-001")
  
  private val validUpdateCompanyDetailsRequestJsonComplete = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "purchaserResourceRef" -> "PRF-001",
    "utr" -> "9876543210",
    "vatReference" -> "GB987654321",
    "compTypeBank" -> "N",
    "compTypeBuilder" -> "Y",
    "compTypeBuildsoc" -> "N",
    "compTypeCentgov" -> "N",
    "compTypeIndividual" -> "N",
    "compTypeInsurance" -> "N",
    "compTypeLocalauth" -> "N",
    "compTypeOcharity" -> "N",
    "compTypeOcompany" -> "N",
    "compTypeOfinancial" -> "N",
    "compTypePartship" -> "N",
    "compTypeProperty" -> "N",
    "compTypePubliccorp" -> "N",
    "compTypeSoletrader" -> "N",
    "compTypePenfund" -> "N"
  )

  private val validUpdateCompanyDetailsRequestJsonMinimal = Json.obj(
    "stornId" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001",
    "purchaserResourceRef" -> "PRF-001"
  )

  private val completeUpdateCompanyDetailsRequest = UpdateCompanyDetailsRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    purchaserResourceRef = "PRF-001",
    utr = Some("9876543210"),
    vatReference = Some("GB987654321"),
    compTypeBank = Some("N"),
    compTypeBuilder = Some("Y"),
    compTypeBuildsoc = Some("N"),
    compTypeCentgov = Some("N"),
    compTypeIndividual = Some("N"),
    compTypeInsurance = Some("N"),
    compTypeLocalauth = Some("N"),
    compTypeOcharity = Some("N"),
    compTypeOcompany = Some("N"),
    compTypeOfinancial = Some("N"),
    compTypePartship = Some("N"),
    compTypeProperty = Some("N"),
    compTypePubliccorp = Some("N"),
    compTypeSoletrader = Some("N"),
    compTypePenfund = Some("N")
  )

  private val minimalUpdateCompanyDetailsRequest = UpdateCompanyDetailsRequest(
    stornId = "STORN12345",
    returnResourceRef = "RRF-2024-001",
    purchaserResourceRef = "PRF-001"
  )
  
  private val validUpdateCompanyDetailsReturnJsonTrue = Json.obj("updated" -> true)
  private val validUpdateCompanyDetailsReturnJsonFalse = Json.obj("updated" -> false)
  private val updateCompanyDetailsReturnTrue = UpdateCompanyDetailsReturn(updated = true)
  private val updateCompanyDetailsReturnFalse = UpdateCompanyDetailsReturn(updated = false)
  
  private val validDeleteCompanyDetailsRequestJson = Json.obj(
    "storn" -> "STORN12345",
    "returnResourceRef" -> "RRF-2024-001"
  )

  private val deleteCompanyDetailsRequest = DeleteCompanyDetailsRequest(
    storn = "STORN12345",
    returnResourceRef = "RRF-2024-001"
  )
  
  private val validDeleteCompanyDetailsReturnJsonTrue = Json.obj("deleted" -> true)
  private val validDeleteCompanyDetailsReturnJsonFalse = Json.obj("deleted" -> false)
  private val deleteCompanyDetailsReturnTrue = DeleteCompanyDetailsReturn(deleted = true)
  private val deleteCompanyDetailsReturnFalse = DeleteCompanyDetailsReturn(deleted = false)

  "CreatePurchaserRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[CreatePurchaserRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[CreatePurchaserRequest](validCreatePurchaserRequestJsonComplete).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.isCompany mustBe "N"
        result.isTrustee mustBe "N"
        result.isConnectedToVendor mustBe "N"
        result.isRepresentedByAgent mustBe "Y"
        result.title mustBe Some("Mr")
        result.surname mustBe Some("Jones")
        result.forename1 mustBe Some("David")
        result.forename2 mustBe Some("Michael")
        result.houseNumber mustBe Some("25")
        result.address1 mustBe "Park Avenue"
        result.address2 mustBe Some("Flat 3")
        result.address3 mustBe Some("Central District")
        result.address4 mustBe Some("London")
        result.postcode mustBe Some("SW1A 2AA")
        result.phone mustBe Some("02012345678")
        result.nino mustBe Some("AB123456C")
        result.hasNino mustBe Some("Y")
        result.dateOfBirth mustBe Some("1980-01-15")
      }

      "must deserialize valid JSON with only required fields" in {
        val result = Json.fromJson[CreatePurchaserRequest](validCreatePurchaserRequestJsonMinimal).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.isCompany mustBe "N"
        result.isTrustee mustBe "N"
        result.isConnectedToVendor mustBe "N"
        result.isRepresentedByAgent mustBe "Y"
        result.address1 mustBe "Park Avenue"
        result.title must not be defined
        result.surname must not be defined
        result.forename1 must not be defined
        result.forename2 must not be defined
        result.companyName must not be defined
        result.houseNumber must not be defined
        result.address2 must not be defined
        result.address3 must not be defined
        result.address4 must not be defined
        result.postcode must not be defined
        result.phone must not be defined
        result.nino must not be defined
      }

      "must deserialize JSON with null optional fields" in {
        val json = validCreatePurchaserRequestJsonComplete ++ Json.obj(
          "title" -> JsNull,
          "surname" -> JsNull,
          "houseNumber" -> JsNull,
          "address2" -> JsNull
        )

        val result = Json.fromJson[CreatePurchaserRequest](json).asEither.value

        result.title must not be defined
        result.surname must not be defined
        result.houseNumber must not be defined
        result.address2 must not be defined
      }

      "must fail to deserialize when stornId is missing" in {
        val json = validCreatePurchaserRequestJsonComplete - "stornId"
        val result = Json.fromJson[CreatePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when returnResourceRef is missing" in {
        val json = validCreatePurchaserRequestJsonComplete - "returnResourceRef"
        val result = Json.fromJson[CreatePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when isCompany is missing" in {
        val json = validCreatePurchaserRequestJsonComplete - "isCompany"
        val result = Json.fromJson[CreatePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when isTrustee is missing" in {
        val json = validCreatePurchaserRequestJsonComplete - "isTrustee"
        val result = Json.fromJson[CreatePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when isConnectedToVendor is missing" in {
        val json = validCreatePurchaserRequestJsonComplete - "isConnectedToVendor"
        val result = Json.fromJson[CreatePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when isRepresentedByAgent is missing" in {
        val json = validCreatePurchaserRequestJsonComplete - "isRepresentedByAgent"
        val result = Json.fromJson[CreatePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when address1 is missing" in {
        val json = validCreatePurchaserRequestJsonComplete - "address1"
        val result = Json.fromJson[CreatePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when required field has invalid type" in {
        val json = validCreatePurchaserRequestJsonComplete ++ Json.obj("stornId" -> 123)
        val result = Json.fromJson[CreatePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize completely invalid JSON structure" in {
        val json = Json.obj("invalidField" -> "value")
        val result = Json.fromJson[CreatePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }

      "must deserialize valid JSON for company purchaser with company-specific fields" in {
        val result = Json.fromJson[CreatePurchaserRequest](validCreatePurchaserRequestJsonCompany).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.isCompany mustBe "Y"
        result.companyName mustBe Some("XYZ Properties Ltd")
        result.address1 mustBe "Park Avenue"
        result.isUkCompany mustBe Some("Y")
        result.registrationNumber mustBe Some("12345678")
        result.placeOfRegistration mustBe Some("England and Wales")
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[CreatePurchaserRequest]]
      }

      "must serialize CreatePurchaserRequest with all fields" in {
        val json = Json.toJson(completeCreatePurchaserRequest)

        (json \ "stornId").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"
        (json \ "isCompany").as[String] mustBe "N"
        (json \ "isTrustee").as[String] mustBe "N"
        (json \ "isConnectedToVendor").as[String] mustBe "N"
        (json \ "isRepresentedByAgent").as[String] mustBe "Y"
        (json \ "title").asOpt[String] mustBe Some("Mr")
        (json \ "surname").asOpt[String] mustBe Some("Jones")
        (json \ "forename1").asOpt[String] mustBe Some("David")
        (json \ "forename2").asOpt[String] mustBe Some("Michael")
        (json \ "houseNumber").asOpt[String] mustBe Some("25")
        (json \ "address1").as[String] mustBe "Park Avenue"
        (json \ "address2").asOpt[String] mustBe Some("Flat 3")
        (json \ "postcode").asOpt[String] mustBe Some("SW1A 2AA")
        (json \ "phone").asOpt[String] mustBe Some("02012345678")
        (json \ "nino").asOpt[String] mustBe Some("AB123456C")
      }

      "must serialize CreatePurchaserRequest with only required fields" in {
        val json = Json.toJson(minimalCreatePurchaserRequest)

        (json \ "stornId").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"
        (json \ "isCompany").as[String] mustBe "N"
        (json \ "address1").as[String] mustBe "Park Avenue"
      }

      "must produce valid JSON structure" in {
        val json = Json.toJson(completeCreatePurchaserRequest)

        json mustBe a[JsObject]
        json.as[JsObject].keys must contain allOf("stornId", "returnResourceRef", "isCompany", "isTrustee", "isConnectedToVendor", "isRepresentedByAgent", "address1")
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[CreatePurchaserRequest]]
      }

      "must round-trip serialize and deserialize with all fields" in {
        val json = Json.toJson(completeCreatePurchaserRequest)
        val result = Json.fromJson[CreatePurchaserRequest](json).asEither.value

        result mustEqual completeCreatePurchaserRequest
      }

      "must round-trip serialize and deserialize with only required fields" in {
        val json = Json.toJson(minimalCreatePurchaserRequest)
        val result = Json.fromJson[CreatePurchaserRequest](json).asEither.value

        result mustEqual minimalCreatePurchaserRequest
      }
    }

    "case class" - {

      "must create instance with all fields" in {
        completeCreatePurchaserRequest.stornId mustBe "STORN12345"
        completeCreatePurchaserRequest.title mustBe Some("Mr")
        completeCreatePurchaserRequest.houseNumber mustBe Some("25")
      }

      "must create instance with only required fields" in {
        minimalCreatePurchaserRequest.stornId mustBe "STORN12345"
        minimalCreatePurchaserRequest.title must not be defined
        minimalCreatePurchaserRequest.houseNumber must not be defined
      }

      "must support equality" in {
        val request1 = minimalCreatePurchaserRequest
        val request2 = minimalCreatePurchaserRequest.copy()

        request1 mustEqual request2
      }

      "must support copy with modifications" in {
        val modified = minimalCreatePurchaserRequest.copy(
          houseNumber = Some("99"),
          postcode = Some("AB12 3CD")
        )

        modified.houseNumber mustBe Some("99")
        modified.postcode mustBe Some("AB12 3CD")
      }

      "must not be equal when required fields differ" in {
        val request1 = minimalCreatePurchaserRequest
        val request2 = minimalCreatePurchaserRequest.copy(stornId = "DIFFERENT")

        request1 must not equal request2
      }
    }
  }

  "CreatePurchaserReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[CreatePurchaserReturn]]
      }

      "must deserialize valid JSON" in {
        val result = Json.fromJson[CreatePurchaserReturn](validCreatePurchaserReturnJson).asEither.value

        result.purchaserResourceRef mustBe "PRF-001"
        result.purchaserId mustBe "PID-001"
      }

      "must fail to deserialize when purchaserResourceRef is missing" in {
        val json = Json.obj("purchaserId" -> "PID-001")
        val result = Json.fromJson[CreatePurchaserReturn](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when purchaserId is missing" in {
        val json = Json.obj("purchaserResourceRef" -> "PRF-001")
        val result = Json.fromJson[CreatePurchaserReturn](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[CreatePurchaserReturn]]
      }

      "must serialize CreatePurchaserReturn" in {
        val json = Json.toJson(createPurchaserReturn)

        (json \ "purchaserResourceRef").as[String] mustBe "PRF-001"
        (json \ "purchaserId").as[String] mustBe "PID-001"
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[CreatePurchaserReturn]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(createPurchaserReturn)
        val result = Json.fromJson[CreatePurchaserReturn](json).asEither.value

        result mustEqual createPurchaserReturn
      }
    }
  }

  "UpdatePurchaserRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[UpdatePurchaserRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[UpdatePurchaserRequest](validUpdatePurchaserRequestJsonComplete).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.purchaserResourceRef mustBe "PRF-001"
        result.isCompany mustBe "N"
        result.isTrustee mustBe "N"
        result.isConnectedToVendor mustBe "N"
        result.isRepresentedByAgent mustBe "Y"
        result.surname mustBe Some("Jones Updated")
        result.nextPurchaserId mustBe Some("PID-002")
      }

      "must deserialize valid JSON with only required fields" in {
        val result = Json.fromJson[UpdatePurchaserRequest](validUpdatePurchaserRequestJsonMinimal).asEither.value

        result.stornId mustBe "STORN12345"
        result.purchaserResourceRef mustBe "PRF-001"
        result.nextPurchaserId must not be defined
      }

      "must fail to deserialize when purchaserResourceRef is missing" in {
        val json = validUpdatePurchaserRequestJsonComplete - "purchaserResourceRef"
        val result = Json.fromJson[UpdatePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[UpdatePurchaserRequest]]
      }

      "must serialize UpdatePurchaserRequest with all fields" in {
        val json = Json.toJson(completeUpdatePurchaserRequest)

        (json \ "stornId").as[String] mustBe "STORN12345"
        (json \ "purchaserResourceRef").as[String] mustBe "PRF-001"
        (json \ "nextPurchaserId").asOpt[String] mustBe Some("PID-002")
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[UpdatePurchaserRequest]]
      }

      "must round-trip serialize and deserialize with all fields" in {
        val json = Json.toJson(completeUpdatePurchaserRequest)
        val result = Json.fromJson[UpdatePurchaserRequest](json).asEither.value

        result mustEqual completeUpdatePurchaserRequest
      }
    }

    "case class" - {

      "must support copy with modifications" in {
        val modified = minimalUpdatePurchaserRequest.copy(nextPurchaserId = Some("PID-999"))

        modified.nextPurchaserId mustBe Some("PID-999")
      }
    }
  }

  "UpdatePurchaserReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[UpdatePurchaserReturn]]
      }

      "must deserialize valid JSON with updated true" in {
        val result = Json.fromJson[UpdatePurchaserReturn](validUpdatePurchaserReturnJsonTrue).asEither.value
        result.updated mustBe true
      }

      "must deserialize valid JSON with updated false" in {
        val result = Json.fromJson[UpdatePurchaserReturn](validUpdatePurchaserReturnJsonFalse).asEither.value
        result.updated mustBe false
      }

      "must fail to deserialize when updated is missing" in {
        val json = Json.obj()
        val result = Json.fromJson[UpdatePurchaserReturn](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[UpdatePurchaserReturn]]
      }

      "must serialize UpdatePurchaserReturn with updated true" in {
        val json = Json.toJson(updatePurchaserReturnTrue)
        (json \ "updated").as[Boolean] mustBe true
      }

      "must serialize UpdatePurchaserReturn with updated false" in {
        val json = Json.toJson(updatePurchaserReturnFalse)
        (json \ "updated").as[Boolean] mustBe false
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[UpdatePurchaserReturn]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(updatePurchaserReturnTrue)
        val result = Json.fromJson[UpdatePurchaserReturn](json).asEither.value

        result mustEqual updatePurchaserReturnTrue
      }
    }

    "case class" - {

      "must support equality" in {
        val return1 = updatePurchaserReturnTrue
        val return2 = updatePurchaserReturnTrue.copy()

        return1 mustEqual return2
      }

      "must not be equal when fields differ" in {
        updatePurchaserReturnTrue must not equal updatePurchaserReturnFalse
      }
    }
  }

  "DeletePurchaserRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[DeletePurchaserRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[DeletePurchaserRequest](validDeletePurchaserRequestJson).asEither.value

        result.storn mustBe "STORN12345"
        result.purchaserId mustBe "PUR001"
        result.returnResourceRef mustBe "RRF-2024-001"
      }

      "must fail to deserialize when storn is missing" in {
        val json = validDeletePurchaserRequestJson - "storn"
        val result = Json.fromJson[DeletePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when purchaserResourceRef is missing" in {
        val json = validDeletePurchaserRequestJson - "purchaserId"
        val result = Json.fromJson[DeletePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when returnResourceRef is missing" in {
        val json = validDeletePurchaserRequestJson - "returnResourceRef"
        val result = Json.fromJson[DeletePurchaserRequest](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[DeletePurchaserRequest]]
      }

      "must serialize DeletePurchaserRequest with all fields" in {
        val json = Json.toJson(deletePurchaserRequest)

        (json \ "storn").as[String] mustBe "STORN12345"
        (json \ "purchaserId").as[String] mustBe "PUR001"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[DeletePurchaserRequest]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(deletePurchaserRequest)
        val result = Json.fromJson[DeletePurchaserRequest](json).asEither.value

        result mustEqual deletePurchaserRequest
      }
    }

    "case class" - {

      "must support copy with modifications" in {
        val modified = deletePurchaserRequest.copy(storn = "MODIFIED")

        modified.storn mustBe "MODIFIED"
        modified.purchaserId mustBe deletePurchaserRequest.purchaserId
      }
    }
  }

  "DeletePurchaserReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[DeletePurchaserReturn]]
      }

      "must deserialize valid JSON with deleted true" in {
        val result = Json.fromJson[DeletePurchaserReturn](validDeletePurchaserReturnJsonTrue).asEither.value
        result.deleted mustBe true
      }

      "must deserialize valid JSON with deleted false" in {
        val result = Json.fromJson[DeletePurchaserReturn](validDeletePurchaserReturnJsonFalse).asEither.value
        result.deleted mustBe false
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[DeletePurchaserReturn]]
      }

      "must serialize DeletePurchaserReturn with deleted true" in {
        val json = Json.toJson(deletePurchaserReturnTrue)
        (json \ "deleted").as[Boolean] mustBe true
      }

      "must serialize DeletePurchaserReturn with deleted false" in {
        val json = Json.toJson(deletePurchaserReturnFalse)
        (json \ "deleted").as[Boolean] mustBe false
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[DeletePurchaserReturn]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(deletePurchaserReturnTrue)
        val result = Json.fromJson[DeletePurchaserReturn](json).asEither.value

        result mustEqual deletePurchaserReturnTrue
      }
    }

    "case class" - {

      "must create instance with deleted true" in {
        deletePurchaserReturnTrue.deleted mustBe true
      }

      "must create instance with deleted false" in {
        deletePurchaserReturnFalse.deleted mustBe false
      }

      "must support equality" in {
        val return1 = deletePurchaserReturnTrue
        val return2 = deletePurchaserReturnTrue.copy()

        return1 mustEqual return2
      }

      "must support copy with modifications" in {
        val modified = deletePurchaserReturnTrue.copy(deleted = false)

        modified.deleted mustBe false
      }

      "must not be equal when fields differ" in {
        val return1 = deletePurchaserReturnTrue
        val return2 = deletePurchaserReturnFalse

        return1 must not equal return2
      }
    }
  }

  "CreateCompanyDetailsRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[CreateCompanyDetailsRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[CreateCompanyDetailsRequest](validCreateCompanyDetailsRequestJsonComplete).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.purchaserResourceRef mustBe "PRF-001"
        result.utr mustBe Some("1234567890")
        result.vatReference mustBe Some("GB123456789")
        result.compTypeBank mustBe Some("Y")
      }

      "must deserialize valid JSON with only required fields" in {
        val result = Json.fromJson[CreateCompanyDetailsRequest](validCreateCompanyDetailsRequestJsonMinimal).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.purchaserResourceRef mustBe "PRF-001"
        result.utr must not be defined
        result.vatReference must not be defined
      }

      "must fail to deserialize when stornId is missing" in {
        val json = validCreateCompanyDetailsRequestJsonComplete - "stornId"
        val result = Json.fromJson[CreateCompanyDetailsRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when returnResourceRef is missing" in {
        val json = validCreateCompanyDetailsRequestJsonComplete - "returnResourceRef"
        val result = Json.fromJson[CreateCompanyDetailsRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when purchaserResourceRef is missing" in {
        val json = validCreateCompanyDetailsRequestJsonComplete - "purchaserResourceRef"
        val result = Json.fromJson[CreateCompanyDetailsRequest](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[CreateCompanyDetailsRequest]]
      }

      "must serialize CreateCompanyDetailsRequest with all fields" in {
        val json = Json.toJson(completeCreateCompanyDetailsRequest)

        (json \ "stornId").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"
        (json \ "purchaserResourceRef").as[String] mustBe "PRF-001"
        (json \ "utr").asOpt[String] mustBe Some("1234567890")
        (json \ "vatReference").asOpt[String] mustBe Some("GB123456789")
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[CreateCompanyDetailsRequest]]
      }

      "must round-trip serialize and deserialize with all fields" in {
        val json = Json.toJson(completeCreateCompanyDetailsRequest)
        val result = Json.fromJson[CreateCompanyDetailsRequest](json).asEither.value

        result mustEqual completeCreateCompanyDetailsRequest
      }

      "must round-trip serialize and deserialize with only required fields" in {
        val json = Json.toJson(minimalCreateCompanyDetailsRequest)
        val result = Json.fromJson[CreateCompanyDetailsRequest](json).asEither.value

        result mustEqual minimalCreateCompanyDetailsRequest
      }
    }

    "case class" - {

      "must create instance with all fields" in {
        completeCreateCompanyDetailsRequest.stornId mustBe "STORN12345"
        completeCreateCompanyDetailsRequest.utr mustBe Some("1234567890")
      }

      "must support equality" in {
        val request1 = minimalCreateCompanyDetailsRequest
        val request2 = minimalCreateCompanyDetailsRequest.copy()

        request1 mustEqual request2
      }
    }
  }

  "CreateCompanyDetailsReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[CreateCompanyDetailsReturn]]
      }

      "must deserialize valid JSON" in {
        val result = Json.fromJson[CreateCompanyDetailsReturn](validCreateCompanyDetailsReturnJson).asEither.value
        result.companyDetailsId mustBe "CID-001"
      }

      "must fail to deserialize when companyDetailsId is missing" in {
        val json = Json.obj()
        val result = Json.fromJson[CreateCompanyDetailsReturn](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[CreateCompanyDetailsReturn]]
      }

      "must serialize CreateCompanyDetailsReturn" in {
        val json = Json.toJson(createCompanyDetailsReturn)
        (json \ "companyDetailsId").as[String] mustBe "CID-001"
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[CreateCompanyDetailsReturn]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(createCompanyDetailsReturn)
        val result = Json.fromJson[CreateCompanyDetailsReturn](json).asEither.value

        result mustEqual createCompanyDetailsReturn
      }
    }
  }

  "UpdateCompanyDetailsRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[UpdateCompanyDetailsRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[UpdateCompanyDetailsRequest](validUpdateCompanyDetailsRequestJsonComplete).asEither.value

        result.stornId mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
        result.purchaserResourceRef mustBe "PRF-001"
        result.utr mustBe Some("9876543210")
      }

      "must deserialize valid JSON with only required fields" in {
        val result = Json.fromJson[UpdateCompanyDetailsRequest](validUpdateCompanyDetailsRequestJsonMinimal).asEither.value

        result.stornId mustBe "STORN12345"
        result.utr must not be defined
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[UpdateCompanyDetailsRequest]]
      }

      "must serialize UpdateCompanyDetailsRequest with all fields" in {
        val json = Json.toJson(completeUpdateCompanyDetailsRequest)

        (json \ "stornId").as[String] mustBe "STORN12345"
        (json \ "utr").asOpt[String] mustBe Some("9876543210")
      }

      "must serialize UpdateCompanyDetailsRequest with only required fields" in {
        val json = Json.toJson(minimalUpdateCompanyDetailsRequest)

        (json \ "stornId").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"
        (json \ "purchaserResourceRef").as[String] mustBe "PRF-001"
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[UpdateCompanyDetailsRequest]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(completeUpdateCompanyDetailsRequest)
        val result = Json.fromJson[UpdateCompanyDetailsRequest](json).asEither.value

        result mustEqual completeUpdateCompanyDetailsRequest
      }

      "must round-trip serialize and deserialize with only required fields" in {
        val json = Json.toJson(minimalUpdateCompanyDetailsRequest)
        val result = Json.fromJson[UpdateCompanyDetailsRequest](json).asEither.value

        result mustEqual minimalUpdateCompanyDetailsRequest
      }
    }

    "case class" - {

      "must create instance with all fields" in {
        completeUpdateCompanyDetailsRequest.stornId mustBe "STORN12345"
        completeUpdateCompanyDetailsRequest.utr mustBe Some("9876543210")
      }

      "must support equality" in {
        val request1 = minimalUpdateCompanyDetailsRequest
        val request2 = minimalUpdateCompanyDetailsRequest.copy()

        request1 mustEqual request2
      }
    }
  }

  "UpdateCompanyDetailsReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[UpdateCompanyDetailsReturn]]
      }

      "must deserialize valid JSON with updated true" in {
        val result = Json.fromJson[UpdateCompanyDetailsReturn](validUpdateCompanyDetailsReturnJsonTrue).asEither.value
        result.updated mustBe true
      }

      "must deserialize valid JSON with updated false" in {
        val result = Json.fromJson[UpdateCompanyDetailsReturn](validUpdateCompanyDetailsReturnJsonFalse).asEither.value
        result.updated mustBe false
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[UpdateCompanyDetailsReturn]]
      }

      "must serialize UpdateCompanyDetailsReturn" in {
        val json = Json.toJson(updateCompanyDetailsReturnTrue)
        (json \ "updated").as[Boolean] mustBe true
      }

      "must serialize UpdateCompanyDetailsReturn with updated false" in {
        val json = Json.toJson(updateCompanyDetailsReturnFalse)
        (json \ "updated").as[Boolean] mustBe false
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[UpdateCompanyDetailsReturn]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(updateCompanyDetailsReturnTrue)
        val result = Json.fromJson[UpdateCompanyDetailsReturn](json).asEither.value

        result mustEqual updateCompanyDetailsReturnTrue
      }

      "must round-trip serialize and deserialize with updated false" in {
        val json = Json.toJson(updateCompanyDetailsReturnFalse)
        val result = Json.fromJson[UpdateCompanyDetailsReturn](json).asEither.value

        result mustEqual updateCompanyDetailsReturnFalse
      }
    }

    "case class" - {

      "must create instance with updated true" in {
        updateCompanyDetailsReturnTrue.updated mustBe true
      }

      "must create instance with updated false" in {
        updateCompanyDetailsReturnFalse.updated mustBe false
      }

      "must support equality" in {
        val return1 = updateCompanyDetailsReturnTrue
        val return2 = updateCompanyDetailsReturnTrue.copy()

        return1 mustEqual return2
      }

      "must support copy with modifications" in {
        val modified = updateCompanyDetailsReturnTrue.copy(updated = false)

        modified.updated mustBe false
      }

      "must not be equal when fields differ" in {
        val return1 = updateCompanyDetailsReturnTrue
        val return2 = updateCompanyDetailsReturnFalse

        return1 must not equal return2
      }
    }
  }

  "DeleteCompanyDetailsRequest" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[DeleteCompanyDetailsRequest]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[DeleteCompanyDetailsRequest](validDeleteCompanyDetailsRequestJson).asEither.value

        result.storn mustBe "STORN12345"
        result.returnResourceRef mustBe "RRF-2024-001"
      }

      "must fail to deserialize when storn is missing" in {
        val json = validDeleteCompanyDetailsRequestJson - "storn"
        val result = Json.fromJson[DeleteCompanyDetailsRequest](json).asEither
        result.isLeft mustBe true
      }

      "must fail to deserialize when returnResourceRef is missing" in {
        val json = validDeleteCompanyDetailsRequestJson - "returnResourceRef"
        val result = Json.fromJson[DeleteCompanyDetailsRequest](json).asEither
        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[DeleteCompanyDetailsRequest]]
      }

      "must serialize DeleteCompanyDetailsRequest" in {
        val json = Json.toJson(deleteCompanyDetailsRequest)

        (json \ "storn").as[String] mustBe "STORN12345"
        (json \ "returnResourceRef").as[String] mustBe "RRF-2024-001"
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[DeleteCompanyDetailsRequest]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(deleteCompanyDetailsRequest)
        val result = Json.fromJson[DeleteCompanyDetailsRequest](json).asEither.value

        result mustEqual deleteCompanyDetailsRequest
      }
    }
  }

  "DeleteCompanyDetailsReturn" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[DeleteCompanyDetailsReturn]]
      }

      "must deserialize valid JSON with deleted true" in {
        val result = Json.fromJson[DeleteCompanyDetailsReturn](validDeleteCompanyDetailsReturnJsonTrue).asEither.value
        result.deleted mustBe true
      }

      "must deserialize valid JSON with deleted false" in {
        val result = Json.fromJson[DeleteCompanyDetailsReturn](validDeleteCompanyDetailsReturnJsonFalse).asEither.value
        result.deleted mustBe false
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[DeleteCompanyDetailsReturn]]
      }

      "must serialize DeleteCompanyDetailsReturn" in {
        val json = Json.toJson(deleteCompanyDetailsReturnTrue)
        (json \ "deleted").as[Boolean] mustBe true
      }

      "must serialize DeleteCompanyDetailsReturn with deleted false" in {
        val json = Json.toJson(deleteCompanyDetailsReturnFalse)
        (json \ "deleted").as[Boolean] mustBe false
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[DeleteCompanyDetailsReturn]]
      }

      "must round-trip serialize and deserialize" in {
        val json = Json.toJson(deleteCompanyDetailsReturnTrue)
        val result = Json.fromJson[DeleteCompanyDetailsReturn](json).asEither.value

        result mustEqual deleteCompanyDetailsReturnTrue
      }

      "must round-trip serialize and deserialize with deleted false" in {
        val json = Json.toJson(deleteCompanyDetailsReturnFalse)
        val result = Json.fromJson[DeleteCompanyDetailsReturn](json).asEither.value

        result mustEqual deleteCompanyDetailsReturnFalse
      }
    }

    "case class" - {

      "must create instance with deleted true" in {
        deleteCompanyDetailsReturnTrue.deleted mustBe true
      }

      "must create instance with deleted false" in {
        deleteCompanyDetailsReturnFalse.deleted mustBe false
      }

      "must support equality" in {
        val return1 = deleteCompanyDetailsReturnTrue
        val return2 = deleteCompanyDetailsReturnTrue.copy()

        return1 mustEqual return2
      }

      "must support copy with modifications" in {
        val modified = deleteCompanyDetailsReturnTrue.copy(deleted = false)

        modified.deleted mustBe false
      }

      "must not be equal when fields differ" in {
        val return1 = deleteCompanyDetailsReturnTrue
        val return2 = deleteCompanyDetailsReturnFalse

        return1 must not equal return2
      }
    }
  }
}