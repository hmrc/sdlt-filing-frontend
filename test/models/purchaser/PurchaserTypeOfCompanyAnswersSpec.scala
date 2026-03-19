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

import models.purchaser.PurchaserTypeOfCompany.*
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.*

class PurchaserTypeOfCompanyAnswersSpec extends AnyFreeSpec with Matchers with EitherValues {

  private val allTypesOfCompanySet: Set[PurchaserTypeOfCompany] = Set(
    Bank,
    BuildingSociety,
    CentralGovernment,
    IndividualOther,
    InsuranceAssurance,
    LocalAuthority,
    Partnership,
    PropertyCompany,
    PublicCorporation,
    OtherCompany,
    OtherFinancialInstitute,
    OtherIncludingCharity,
    SuperannuationOrPensionFund,
    UnincorporatedBuilder,
    UnincorporatedSoleTrader
  )

  private val allTypesOfCompanyAnswers = PurchaserTypeOfCompanyAnswers(
    bank = "yes",
    buildingSociety = "yes",
    centralGovernment = "yes",
    individualOther = "yes",
    insuranceAssurance = "yes",
    localAuthority = "yes",
    partnership = "yes",
    propertyCompany = "yes",
    publicCorporation = "yes",
    otherCompany = "yes",
    otherFinancialInstitute = "yes",
    otherIncludingCharity = "yes",
    superannuationOrPensionFund = "yes",
    unincorporatedBuilder = "yes",
    unincorporatedSoleTrader = "yes"
  )

  private val someTypesOfCompanySet: Set[PurchaserTypeOfCompany] = Set(
    Bank,
    LocalAuthority,
    OtherCompany
  )

  private val someTypesOfCompanyAnswers = PurchaserTypeOfCompanyAnswers(
    bank = "yes",
    buildingSociety = "no",
    centralGovernment = "no",
    individualOther = "no",
    insuranceAssurance = "no",
    localAuthority = "yes",
    partnership = "no",
    propertyCompany = "no",
    publicCorporation = "no",
    otherCompany = "yes",
    otherFinancialInstitute = "no",
    otherIncludingCharity = "no",
    superannuationOrPensionFund = "no",
    unincorporatedBuilder = "no",
    unincorporatedSoleTrader = "no"
  )

  private val noTypesOfCompanySet: Set[PurchaserTypeOfCompany] = Set.empty

  private val noTypesOfCompanyAnswers = PurchaserTypeOfCompanyAnswers(
    bank = "no",
    buildingSociety = "no",
    centralGovernment = "no",
    individualOther = "no",
    insuranceAssurance = "no",
    localAuthority = "no",
    partnership = "no",
    propertyCompany = "no",
    publicCorporation = "no",
    otherCompany = "no",
    otherFinancialInstitute = "no",
    otherIncludingCharity = "no",
    superannuationOrPensionFund = "no",
    unincorporatedBuilder = "no",
    unincorporatedSoleTrader = "no"
  )

  private val someTypesOfCompanyAnswersJson = Json.obj(
    "bank" -> "yes",
    "buildingSociety" -> "no",
    "centralGovernment" -> "no",
    "individualOther" -> "no",
    "insuranceAssurance" -> "no",
    "localAuthority" -> "yes",
    "partnership" -> "no",
    "propertyCompany" -> "no",
    "publicCorporation" -> "no",
    "otherCompany" -> "yes",
    "otherFinancialInstitute" -> "no",
    "otherIncludingCharity" -> "no",
    "superannuationOrPensionFund" -> "no",
    "unincorporatedBuilder" -> "no",
    "unincorporatedSoleTrader" -> "no"
  )

  "PurchaserTypeOfCompanyAnswers" - {

    ".fromSet" - {
      "must convert a set of all types of company to an answers object with all 'yes'" in {
        val answers = PurchaserTypeOfCompanyAnswers.fromSet(allTypesOfCompanySet)
        answers mustEqual allTypesOfCompanyAnswers
      }

      "must convert a set of some types of company to an answers object with corresponding 'yes' and 'no'" in {
        val answers = PurchaserTypeOfCompanyAnswers.fromSet(someTypesOfCompanySet)
        answers mustEqual someTypesOfCompanyAnswers
      }

      "must convert an empty set to an answers object with all 'no'" in {
        val answers = PurchaserTypeOfCompanyAnswers.fromSet(noTypesOfCompanySet)
        answers mustEqual noTypesOfCompanyAnswers
      }
    }

    ".toSet" - {
      "must convert an answers object with all 'yes' to a set of all types of company" in {
        val typesOfCompanySet = PurchaserTypeOfCompanyAnswers.toSet(allTypesOfCompanyAnswers)
        typesOfCompanySet mustEqual allTypesOfCompanySet
      }

      "must convert an answers object with some 'yes' and some 'no' to a set of corresponding types of company" in {
        val typesOfCompanySet = PurchaserTypeOfCompanyAnswers.toSet(someTypesOfCompanyAnswers)
        typesOfCompanySet mustEqual someTypesOfCompanySet
      }

      "must convert an answers object with all 'no' to an empty set" in {
        val typesOfCompanySet = PurchaserTypeOfCompanyAnswers.toSet(noTypesOfCompanyAnswers)
        typesOfCompanySet mustEqual noTypesOfCompanySet
      }

      "must omit invalid values and treat them as 'no'" in {
        val invalidAnswers = someTypesOfCompanyAnswers.copy(bank = "YES")
        val typesOfCompanySet = PurchaserTypeOfCompanyAnswers.toSet(invalidAnswers)
        typesOfCompanySet mustEqual (someTypesOfCompanySet - Bank)
      }
    }

    ".writes" - {
      "must be found implicitly" in {
        implicitly[Writes[PurchaserTypeOfCompanyAnswers]]
      }

      "must serialize PurchaserTypeOfCompanyAnswers" in {
        val json: JsValue = Json.toJson(someTypesOfCompanyAnswers)
        (json \ "bank").as[String] mustBe "yes"
        (json \ "buildingSociety").as[String] mustBe "no"
        (json \ "centralGovernment").as[String] mustBe "no"
        (json \ "individualOther").as[String] mustBe "no"
        (json \ "insuranceAssurance").as[String] mustBe "no"
        (json \ "localAuthority").as[String] mustBe "yes"
        (json \ "partnership").as[String] mustBe "no"
        (json \ "propertyCompany").as[String] mustBe "no"
        (json \ "publicCorporation").as[String] mustBe "no"
        (json \ "otherCompany").as[String] mustBe "yes"
        (json \ "otherFinancialInstitute").as[String] mustBe "no"
        (json \ "otherIncludingCharity").as[String] mustBe "no"
        (json \ "superannuationOrPensionFund").as[String] mustBe "no"
        (json \ "unincorporatedBuilder").as[String] mustBe "no"
        (json \ "unincorporatedSoleTrader").as[String] mustBe "no"
      }
    }

    ".reads" - {
      "must be found implicitly" in {
        implicitly[Reads[PurchaserTypeOfCompanyAnswers]]
      }

      "must deserialize valid JSON to PurchaserTypeOfCompanyAnswers" in {
        val result = Json.fromJson[PurchaserTypeOfCompanyAnswers](someTypesOfCompanyAnswersJson).asEither.value
        result mustEqual someTypesOfCompanyAnswers
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[PurchaserTypeOfCompanyAnswers]]
      }
    }
  }
}
