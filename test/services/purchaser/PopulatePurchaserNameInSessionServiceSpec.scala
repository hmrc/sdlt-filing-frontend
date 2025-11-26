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

package services.purchaser

import base.SpecBase
import models.purchaser.{ConfirmNameOfThePurchaser, NameOfPurchaser}
import models.{FullReturn, Purchaser, UserAnswers}
import pages.purchaser.{ConfirmNameOfThePurchaserPage, NameOfPurchaserPage}

import scala.util.{Failure, Success}

class PopulatePurchaserNameInSessionServiceSpec extends SpecBase {

  val service = new PurchaserService()

  private def emptyFullReturn: FullReturn = FullReturn(
    returnResourceRef = "REF123",
    stornId = "TESTSTORN",
    vendor = None,
    purchaser = None,
    transaction = None
  )

  "PopulatePurchaserNameInSessionService" - {

    "populatePurchaserNameInSession" - {

      "when purchaserCheck is 'Yes'" - {

        "must successfully populate session with individual purchaser with full name" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH001"),
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            surname = Some("Smith"),
            companyName = None,
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("Yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            name = "Smith"
          ))

          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }

        "must successfully populate session with individual purchaser without middle name" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH002"),
            forename1 = Some("Jane"),
            forename2 = None,
            surname = Some("Doe"),
            companyName = None,
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("Yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = Some("Jane"),
            forename2 = None,
            name = "Doe"
          ))

          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }

        "must successfully populate session with company purchaser" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH003"),
            forename1 = None,
            forename2 = None,
            surname = None,
            companyName = Some("ACME Corporation"),
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("Yes", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
            forename1 = None,
            forename2 = None,
            name = "ACME Corporation"
          ))

          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        }
      }

      "when purchaserCheck is 'No'" - {

        "must only set confirmation to No without setting name" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH005"),
            forename1 = Some("John"),
            forename2 = Some("Michael"),
            surname = Some("Smith"),
            companyName = None,
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("No", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.No)
        }

        "must only set confirmation to No when no purchaser exists" in {
          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

          val result = service.populatePurchaserNameInSession("No", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.No)
        }
      }

      "when purchaserCheck is any other value" - {

        "must set confirmation to No" in {
          val purchaser = Purchaser(
            purchaserID = Some("PURCH006"),
            forename1 = Some("John"),
            surname = Some("Smith"),
            companyName = None,
            address1 = None,
            address2 = None,
            address3 = None,
            address4 = None,
            postcode = None
          )

          val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
          val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
            .copy(fullReturn = Some(fullReturn))

          val result = service.populatePurchaserNameInSession("Invalid", userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(NameOfPurchaserPage) mustBe None
          updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.No)
        }
      }

      "must preserve existing user answers when populating purchaser data" in {
        val existingAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value

        val purchaser = Purchaser(
          purchaserID = Some("PURCH007"),
          forename1 = Some("Jane"),
          surname = Some("Doe"),
          companyName = None,
          address1 = None,
          address2 = None,
          address3 = None,
          address4 = None,
          postcode = None
        )

        val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
        val userAnswersWithPurchaser = existingAnswers.copy(fullReturn = Some(fullReturn))

        val result = service.populatePurchaserNameInSession("Yes", userAnswersWithPurchaser)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        // Should override the existing value
        updatedAnswers.get(ConfirmNameOfThePurchaserPage) mustBe Some(ConfirmNameOfThePurchaser.Yes)
        updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
          forename1 = Some("Jane"),
          forename2 = None,
          name = "Doe"
        ))
      }

      "must handle purchaser with only surname (no forenames)" in {
        val purchaser = Purchaser(
          purchaserID = Some("PURCH008"),
          forename1 = None,
          forename2 = None,
          surname = Some("Madonna"),
          companyName = None,
          address1 = None,
          address2 = None,
          address3 = None,
          address4 = None,
          postcode = None
        )

        val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturn))

        val result = service.populatePurchaserNameInSession("Yes", userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
          forename1 = None,
          forename2 = None,
          name = "Madonna"
        ))
      }

      "must prioritize company name over surname when both are present" in {
        val purchaser = Purchaser(
          purchaserID = Some("PURCH009"),
          forename1 = Some("John"),
          forename2 = None,
          surname = Some("Smith"),
          companyName = Some("Smith & Co Ltd"),
          address1 = None,
          address2 = None,
          address3 = None,
          address4 = None,
          postcode = None
        )

        val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(purchaser)))
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturn))

        val result = service.populatePurchaserNameInSession("Yes", userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
          forename1 = None,
          forename2 = None,
          name = "Smith & Co Ltd"
        ))
      }

      "must use first purchaser when multiple purchasers exist" in {
        val firstPurchaser = Purchaser(
          purchaserID = Some("PURCH010"),
          forename1 = Some("First"),
          surname = Some("Purchaser"),
          companyName = None,
          address1 = None,
          address2 = None,
          address3 = None,
          address4 = None,
          postcode = None
        )

        val secondPurchaser = Purchaser(
          purchaserID = Some("PURCH011"),
          forename1 = Some("Second"),
          surname = Some("Buyer"),
          companyName = None,
          address1 = None,
          address2 = None,
          address3 = None,
          address4 = None,
          postcode = None
        )

        val fullReturn = emptyFullReturn.copy(purchaser = Some(Seq(firstPurchaser, secondPurchaser)))
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .copy(fullReturn = Some(fullReturn))

        val result = service.populatePurchaserNameInSession("Yes", userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(NameOfPurchaserPage) mustBe Some(NameOfPurchaser(
          forename1 = Some("First"),
          forename2 = None,
          name = "Purchaser"
        ))
      }
    }
  }
}