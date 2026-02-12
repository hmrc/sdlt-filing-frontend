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

package models.purchaser

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.{JsNull, JsObject, Json, Reads}

import java.time.LocalDate

class PurchaserSessionQuestionsSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  object TestData {
    def purchaserSessionQuestionsJsonComplete: JsObject = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "purchaserAndCompanyId" -> Json.obj(
          "purchaserID" -> "PUR001",
          "companyDetailsID" -> "COMPDET001",
        ),
        "ConfirmNameOfThePurchaser" -> "yes",
        "whoIsMakingThePurchase" -> "Company",
        "nameOfPurchaser" -> Json.obj(
          "forename1" -> "Name1",
          "forename2" -> "Name2",
          "name" -> "Samsung",
        ),
        "purchaserAddress" -> Json.obj(
          "houseNumber" -> "1",
          "line1" -> "Street 1",
          "line2" -> "Street 2",
          "line3" -> "Street 3",
          "line4" -> "Street 4",
          "line5" -> "Street 5",
          "postcode" -> "CR7 8LU",
          "country" -> Json.obj(
            "code" -> "GB",
            "name" -> "UK"
          ),
          "addressValidated" -> true
        ),
        "addPurchaserPhoneNumber" -> true,
        "enterPurchaserPhoneNumber" -> "+447874363636",
        "doesPurchaserHaveNI" -> "yes",
        "nationalInsuranceNumber" -> "AA123456A",
        "purchaserFormOfIdIndividual" -> Json.obj(
          "idNumberOrReference" -> "ref",
          "countryIssued" -> "country"
        ),
        "purchaserDateOfBirth" -> "2000-02-02",
        "purchaserConfirmIdentity" -> "vatRegistrationNumber",
        "registrationNumber" -> "VAT1234",
        "purchaserUTRPage" -> "UTR1234",
        "purchaserFormOfIdCompany" -> Json.obj(
          "referenceId" -> "ID12345",
          "countryIssued" -> "country"
        ),
        "purchaserTypeOfCompany" -> Json.obj(
          "bank" -> "YES",
          "buildingAssociation" -> "NO",
          "centralGovernment" -> "NO",
          "individualOther" -> "NO",
          "insuranceAssurance" -> "NO",
          "localAuthority" -> "NO",
          "partnership" -> "NO",
          "propertyCompany" -> "NO",
          "publicCorporation" -> "NO",
          "otherCompany" -> "NO",
          "otherFinancialInstitute" -> "NO",
          "otherIncludingCharity" -> "NO",
          "superannuationOrPensionFund" -> "NO",
          "unincorporatedBuilder" -> "NO",
          "unincorporatedSoleTrader" -> "NO"
        ),
        "isPurchaserActingAsTrustee" -> "yes",
        "purchaserAndVendorConnected" -> "yes",
      ))

    def purchaserSessionQuestionsJsonWithNoOptional: JsObject = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "purchaserAndCompanyId" -> JsNull,
        "ConfirmNameOfThePurchaser" -> JsNull,
        "whoIsMakingThePurchase" -> "Company",
        "nameOfPurchaser" -> Json.obj(
          "forename1" -> JsNull,
          "forename2" -> JsNull,
          "name" -> "Samsung",
        ),
        "purchaserAddress" -> Json.obj(
          "houseNumber" -> JsNull,
          "line1" -> "Street 1",
          "line2" -> JsNull,
          "line3" -> JsNull,
          "line4" -> JsNull,
          "line5" -> JsNull,
          "postcode" -> JsNull,
          "country" -> JsNull,
          "addressValidated" -> JsNull
        ),
        "addPurchaserPhoneNumber" -> false,
        "enterPurchaserPhoneNumber" -> JsNull,
        "doesPurchaserHaveNI" -> JsNull,
        "nationalInsuranceNumber" -> JsNull,
        "purchaserFormOfIdIndividual" -> JsNull,
        "purchaserDateOfBirth" -> JsNull,
        "purchaserConfirmIdentity" -> JsNull,
        "registrationNumber" -> JsNull,
        "purchaserUTRPage" -> JsNull,
        "purchaserFormOfIdCompany" -> JsNull,
        "purchaserTypeOfCompany" -> JsNull,
        "isPurchaserActingAsTrustee" -> "yes",
        "purchaserAndVendorConnected" -> "yes",
      ))
  }

  "PurchaserSessionQuestions" - {
    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[PurchaserSessionQuestions]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[PurchaserSessionQuestions](TestData.purchaserSessionQuestionsJsonComplete).asEither.value

        val expectedResult: PurchaserSessionQuestions =
          PurchaserSessionQuestions(
            PurchaserCurrent(
              purchaserAndCompanyId = Some(PurchaserAndCompanyId(purchaserID = "PUR001", companyDetailsID = Some("COMPDET001"))),
              ConfirmNameOfThePurchaser = Some(ConfirmNameOfThePurchaser.Yes),
              whoIsMakingThePurchase = "Company",
              nameOfPurchaser = NameOfPurchaser(forename1 = Some("Name1"), forename2 = Some("Name2"), name = "Samsung"),
              purchaserAddress = PurchaserSessionAddress(
                houseNumber = Some("1"),
                line1 = Some("Street 1"),
                line2 = Some("Street 2"),
                line3 = Some("Street 3"),
                line4 = Some("Street 4"),
                line5 = Some("Street 5"),
                postcode = Some("CR7 8LU"),
                country = Some(PurchaserSessionCountry(
                  code = Some("GB"),
                  name = Some("UK")
                )),
                addressValidated = Some(true)),
              addPurchaserPhoneNumber = Some(true),
              enterPurchaserPhoneNumber = Some("+447874363636"),
              doesPurchaserHaveNI = Some(DoesPurchaserHaveNI.Yes),
              nationalInsuranceNumber = Some("AA123456A"),
              purchaserFormOfIdIndividual = Some(PurchaserFormOfIdIndividual(idNumberOrReference = "ref", countryIssued = "country")),
              purchaserDateOfBirth = Some(LocalDate.of(2000,2,2)),
              purchaserConfirmIdentity = Some(PurchaserConfirmIdentity.VatRegistrationNumber),
              registrationNumber = Some("VAT1234"),
              purchaserUTRPage = Some("UTR1234"),
              purchaserFormOfIdCompany  = Some(CompanyFormOfId(referenceId = "ID12345", countryIssued = "country")),
              purchaserTypeOfCompany  = Some(
                PurchaserTypeOfCompanyAnswers(
                  bank = "YES",
                  buildingAssociation = "NO",
                  centralGovernment = "NO",
                  individualOther = "NO",
                  insuranceAssurance = "NO",
                  localAuthority = "NO",
                  partnership = "NO",
                  propertyCompany = "NO",
                  publicCorporation = "NO",
                  otherCompany = "NO",
                  otherFinancialInstitute = "NO",
                  otherIncludingCharity = "NO",
                  superannuationOrPensionFund = "NO",
                  unincorporatedBuilder = "NO",
                  unincorporatedSoleTrader = "NO")
              ),
              isPurchaserActingAsTrustee = Some("yes"),
              purchaserAndVendorConnected = Some("yes"),
            ))

        result shouldBe expectedResult
      }

      "must deserialize valid JSON with minimal fields" in {
        val result = Json.fromJson[PurchaserSessionQuestions](TestData.purchaserSessionQuestionsJsonWithNoOptional).asEither.value

        val expectedResult: PurchaserSessionQuestions =
          PurchaserSessionQuestions(
            PurchaserCurrent(
              purchaserAndCompanyId = None,
              ConfirmNameOfThePurchaser = None,
              whoIsMakingThePurchase = "Company",
              nameOfPurchaser = NameOfPurchaser(forename1 = None, None, name = "Samsung"),
              purchaserAddress = PurchaserSessionAddress(
                houseNumber = None,
                line1 = Some("Street 1"),
                line2 = None,
                line3 = None,
                line4 = None,
                line5 = None,
                postcode = None,
                country = None,
                addressValidated = None),
              addPurchaserPhoneNumber = Some(false),
              enterPurchaserPhoneNumber = None,
              doesPurchaserHaveNI = None,
              nationalInsuranceNumber = None,
              purchaserFormOfIdIndividual = None,
              purchaserDateOfBirth = None,
              purchaserConfirmIdentity = None,
              registrationNumber = None,
              purchaserUTRPage = None,
              purchaserFormOfIdCompany = None,
              purchaserTypeOfCompany = None,
              isPurchaserActingAsTrustee = Some("yes"),
              purchaserAndVendorConnected = Some("yes"),
            ))

        result shouldBe expectedResult
      }
    }
  }
}