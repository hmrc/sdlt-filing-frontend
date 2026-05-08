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

package services.transaction

import base.SpecBase
import models.{Transaction, UserAnswers}
import models.prelimQuestions.TransactionType
import models.transaction.TransactionFormsOfConsiderationAnswers
import org.scalatestplus.mockito.MockitoSugar
import pages.transaction.*

import java.time.LocalDate
import scala.util.{Failure, Success}

class PopulateTransactionServiceSpec extends SpecBase with MockitoSugar {

  val service = new PopulateTransactionService()

  val userAnswers: UserAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

  "PopulateTransactionService" - {

    "when transaction type is ConveyanceTransfer" - {

      "when all fields are populated" - {
        "must correctly populate the session" in {
          val transaction = Transaction(
            transactionDescription       = Some("F"),
            effectiveDate                = Some("2024-01-15"),
            contractDate                 = Some("2024-01-10"),
            totalConsideration           = Some(BigDecimal(250000)),
            considerationVAT             = Some(BigDecimal(50000)),
            considerationCash            = Some(BigDecimal(200000)),
            considerationDebt            = Some(BigDecimal(0)),
            considerationBuild           = Some(BigDecimal(0)),
            considerationEmploy          = Some(BigDecimal(0)),
            considerationOther           = Some(BigDecimal(0)),
            considerationSharesQTD       = Some(BigDecimal(0)),
            considerationSharesUNQTD     = Some(BigDecimal(0)),
            considerationLand            = Some(BigDecimal(0)),
            considerationServices        = Some(BigDecimal(0)),
            considerationContingent      = Some(BigDecimal(0)),
            isLinked                     = Some("YES"),
            totalConsiderationLinked     = Some(BigDecimal(500000)),
            claimingRelief               = Some("YES"),
            reliefAmount                 = Some(BigDecimal(10000)),
            reliefSchemeNumber           = Some("CS123456")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(TypeOfTransactionPage)                    mustBe Some(TransactionType.ConveyanceTransfer)
          updatedAnswers.get(TransactionEffectiveDatePage)             mustBe Some(LocalDate.of(2024, 1, 15))
          updatedAnswers.get(TransactionAddDateOfContractPage)         mustBe Some(true)
          updatedAnswers.get(TransactionDateOfContractPage)            mustBe Some(LocalDate.of(2024, 1, 10))
          updatedAnswers.get(TotalConsiderationOfTransactionPage)      mustBe Some("250000")
          updatedAnswers.get(TransactionVatIncludedPage)               mustBe Some(true)
          updatedAnswers.get(TransactionVatAmountPage)                 mustBe Some("50000")
          updatedAnswers.get(TransactionFormsOfConsiderationPage)      mustBe Some(TransactionFormsOfConsiderationAnswers(
            cash                      = "yes",
            debt                      = "no",
            buildingWorks             = "no",
            employment                = "no",
            other                     = "no",
            sharesInAQuotedCompany    = "no",
            sharesInAnUnquotedCompany = "no",
            otherLand                 = "no",
            services                  = "no",
            contingent                = "no"
          ))
          updatedAnswers.get(TransactionLinkedTransactionsPage)             mustBe Some(true)
          updatedAnswers.get(TotalConsiderationOfLinkedTransactionPage)     mustBe Some("500000")
          updatedAnswers.get(PurchaserEligibleToClaimReliefPage)            mustBe Some(true)
          updatedAnswers.get(TransactionPartialReliefPage)                  mustBe Some(true)
          updatedAnswers.get(ClaimingPartialReliefAmountPage)               mustBe Some("10000")
          updatedAnswers.get(AddRegisteredCharityNumberPage)                mustBe Some(true)
          updatedAnswers.get(CharityRegisteredNumberPage)                   mustBe Some("CS123456")
        }
      }

      "when there is no contract date" - {
        "must set add date of contract to false and not set the date page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("2024-01-15"),
            contractDate           = None,
            isLinked               = Some("NO"),
            claimingRelief         = Some("NO")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(TransactionAddDateOfContractPage) mustBe Some(false)
          updatedAnswers.get(TransactionDateOfContractPage)    mustBe None
        }
      }

      "when VAT is zero" - {
        "must set vat included to false and not set the vat amount page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("2024-01-15"),
            considerationVAT       = Some(BigDecimal(0)),
            isLinked               = Some("NO"),
            claimingRelief         = Some("NO")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(TransactionVatIncludedPage) mustBe Some(false)
          updatedAnswers.get(TransactionVatAmountPage)   mustBe None
        }
      }

      "when VAT is absent" - {
        "must set vat included to false and not set the vat amount page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("2024-01-15"),
            considerationVAT       = None,
            isLinked               = Some("NO"),
            claimingRelief         = Some("NO")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(TransactionVatIncludedPage) mustBe Some(false)
          updatedAnswers.get(TransactionVatAmountPage)   mustBe None
        }
      }

      "when transaction is not linked" - {
        "must set linked transactions to false and not set the linked total page" in {
          val transaction = Transaction(
            transactionDescription   = Some("F"),
            effectiveDate            = Some("2024-01-15"),
            isLinked                 = Some("NO"),
            totalConsiderationLinked = Some(BigDecimal(500000)),
            claimingRelief           = Some("NO")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(TransactionLinkedTransactionsPage)         mustBe Some(false)
          updatedAnswers.get(TotalConsiderationOfLinkedTransactionPage) mustBe None
        }
      }
    }

    "when transaction type is GrantOfLease" - {

      "must skip all consideration pages" in {
        val transaction = Transaction(
          transactionDescription   = Some("L"),
          effectiveDate            = Some("2024-06-01"),
          totalConsideration       = Some(BigDecimal(100000)),
          considerationVAT         = Some(BigDecimal(20000)),
          considerationCash        = Some(BigDecimal(100000)),
          isLinked                 = Some("NO"),
          claimingRelief           = Some("NO")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(TypeOfTransactionPage)               mustBe Some(TransactionType.GrantOfLease)
        updatedAnswers.get(TotalConsiderationOfTransactionPage) mustBe None
        updatedAnswers.get(TransactionVatIncludedPage)          mustBe None
        updatedAnswers.get(TransactionVatAmountPage)            mustBe None
        updatedAnswers.get(TransactionFormsOfConsiderationPage) mustBe None
      }
    }

    "when transaction type is ConveyanceTransferLease" - {

      "must correctly set the transaction type" in {
        val transaction = Transaction(
          transactionDescription = Some("A"),
          effectiveDate          = Some("2024-01-15"),
          isLinked               = Some("NO"),
          claimingRelief         = Some("NO")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        result.get.get(TypeOfTransactionPage) mustBe Some(TransactionType.ConveyanceTransferLease)
      }
    }

    "when transaction type is OtherTransaction" - {

      "must correctly set the transaction type" in {
        val transaction = Transaction(
          transactionDescription = Some("O"),
          effectiveDate          = Some("2024-01-15"),
          isLinked               = Some("NO"),
          claimingRelief         = Some("NO")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        result.get.get(TypeOfTransactionPage) mustBe Some(TransactionType.OtherTransaction)
      }
    }

    "when relief is claimed" - {

      "when there is a partial relief amount" - {
        "must set partial relief to true and populate the amount page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("2024-01-15"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefAmount           = Some(BigDecimal(5000)),
            reliefSchemeNumber     = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(PurchaserEligibleToClaimReliefPage) mustBe Some(true)
          updatedAnswers.get(TransactionPartialReliefPage)        mustBe Some(true)
          updatedAnswers.get(ClaimingPartialReliefAmountPage)     mustBe Some("5000")
        }
      }

      "when there is no partial relief amount" - {
        "must set partial relief to false and not set the amount page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("2024-01-15"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefAmount           = None,
            reliefSchemeNumber     = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(PurchaserEligibleToClaimReliefPage) mustBe Some(true)
          updatedAnswers.get(TransactionPartialReliefPage)        mustBe Some(false)
          updatedAnswers.get(ClaimingPartialReliefAmountPage)     mustBe None
        }
      }

      "when there is a charity scheme number" - {
        "must set add charity number to true and populate the number page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("2024-01-15"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefAmount           = None,
            reliefSchemeNumber     = Some("CS654321")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(AddRegisteredCharityNumberPage) mustBe Some(true)
          updatedAnswers.get(CharityRegisteredNumberPage)    mustBe Some("CS654321")
        }
      }

      "when there is no charity scheme number" - {
        "must set add charity number to false and not set the number page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("2024-01-15"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefAmount           = None,
            reliefSchemeNumber     = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(AddRegisteredCharityNumberPage) mustBe Some(false)
          updatedAnswers.get(CharityRegisteredNumberPage)    mustBe None
        }
      }
    }

    "when relief is not claimed" - {
      "must set eligible to false and partial relief to false" in {
        val transaction = Transaction(
          transactionDescription = Some("F"),
          effectiveDate          = Some("2024-01-15"),
          isLinked               = Some("NO"),
          claimingRelief         = Some("NO")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(PurchaserEligibleToClaimReliefPage) mustBe Some(false)
        updatedAnswers.get(TransactionPartialReliefPage)        mustBe Some(false)
        updatedAnswers.get(ClaimingPartialReliefAmountPage)     mustBe None
        updatedAnswers.get(AddRegisteredCharityNumberPage)      mustBe None
        updatedAnswers.get(CharityRegisteredNumberPage)         mustBe None
      }
    }

    "must fail when effectiveDate is missing" in {
      val transaction = Transaction(
        transactionDescription = Some("F"),
        effectiveDate          = None
      )

      val result = service.populateTransactionInSession(transaction, userAnswers)

      result mustBe a[Failure[_]]
      result.failed.get mustBe an[IllegalStateException]
    }

    "must fail when transactionDescription is missing" in {
      val transaction = Transaction(
        transactionDescription = None,
        effectiveDate          = Some("2024-01-15")
      )

      val result = service.populateTransactionInSession(transaction, userAnswers)

      result mustBe a[Failure[_]]
      result.failed.get mustBe an[IllegalStateException]
    }

    "must fail when transactionDescription is an unrecognised code" in {
      val transaction = Transaction(
        transactionDescription = Some("INVALID"),
        effectiveDate          = Some("2024-01-15")
      )

      val result = service.populateTransactionInSession(transaction, userAnswers)

      result mustBe a[Failure[_]]
      result.failed.get mustBe an[IllegalStateException]
    }
  }
}
