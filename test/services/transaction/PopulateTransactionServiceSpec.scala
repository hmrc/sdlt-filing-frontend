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
import models.address.Address
import models.prelimQuestions.TransactionType
import models.transaction.{ReasonForRelief, TransactionFormsOfConsiderationAnswers, TransactionRulingFollowed, TransactionSaleOfBusinessAssetsAnswers, TransactionUseOfLandOrPropertyAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.transaction.*

import java.time.LocalDate
import scala.util.Success

class PopulateTransactionServiceSpec extends SpecBase with MockitoSugar {

  val service = new PopulateTransactionService()

  val userAnswers: UserAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

  "PopulateTransactionService" - {

    "when transaction type is ConveyanceTransfer" - {

      "when all fields are populated" - {
        "must correctly populate the session" in {
          val transaction = Transaction(
            transactionDescription   = Some("F"),
            effectiveDate            = Some("15/01/2024"),
            contractDate             = Some("10/01/2024"),
            totalConsideration       = Some("250000"),
            considerationVAT         = Some("50000"),
            considerationCash        = Some("yes"),
            considerationDebt        = Some("yes"),
            considerationBuild       = Some("no"),
            considerationEmploy      = Some("no"),
            considerationOther       = Some("no"),
            considerationSharesQTD   = Some("no"),
            considerationSharesUNQTD = Some("no"),
            considerationLand        = Some("no"),
            considerationServices    = Some("no"),
            considerationContingent  = Some("no"),
            isLinked                 = Some("YES"),
            totalConsiderationLinked = Some("500000"),
            claimingRelief           = Some("YES"),
            reliefAmount             = Some("10000"),
            reliefReason             = Some("20"),
            reliefSchemeNumber       = Some("CS123456")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(TypeOfTransactionPage)               mustBe Some(TransactionType.ConveyanceTransfer)
          updatedAnswers.get(TransactionEffectiveDatePage)        mustBe Some(LocalDate.of(2024, 1, 15))
          updatedAnswers.get(TransactionAddDateOfContractPage)    mustBe Some(true)
          updatedAnswers.get(TransactionDateOfContractPage)       mustBe Some(LocalDate.of(2024, 1, 10))
          updatedAnswers.get(TotalConsiderationOfTransactionPage) mustBe Some("250000")
          updatedAnswers.get(TransactionVatIncludedPage)          mustBe Some(true)
          updatedAnswers.get(TransactionVatAmountPage)            mustBe Some("50000")
          updatedAnswers.get(TransactionFormsOfConsiderationPage) mustBe Some(TransactionFormsOfConsiderationAnswers(
            cash                      = "yes",
            debt                      = "yes",
            buildingWorks             = "no",
            employment                = "no",
            other                     = "no",
            sharesInAQuotedCompany    = "no",
            sharesInAnUnquotedCompany = "no",
            otherLand                 = "no",
            services                  = "no",
            contingent                = "no"
          ))
          updatedAnswers.get(TransactionLinkedTransactionsPage)         mustBe Some(true)
          updatedAnswers.get(TotalConsiderationOfLinkedTransactionPage) mustBe Some("500000")
          updatedAnswers.get(PurchaserEligibleToClaimReliefPage)        mustBe Some(true)
          updatedAnswers.get(ReasonForReliefPage)                       mustBe Some(ReasonForRelief.CharitiesRelief)
          updatedAnswers.get(AddRegisteredCharityNumberPage)            mustBe Some(true)
          updatedAnswers.get(CharityRegisteredNumberPage)               mustBe Some("CS123456")
          updatedAnswers.get(TransactionPartialReliefPage)              mustBe Some(true)
          updatedAnswers.get(ClaimingPartialReliefAmountPage)           mustBe Some("10000")
        }
      }

      "when there is no contract date" - {
        "must set add date of contract to false and not set the date page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
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

      "when VAT is a positive number" - {
        "must set vat included to true and populate the vat amount page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            considerationVAT       = Some("20000"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("NO")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(TransactionVatIncludedPage) mustBe Some(true)
          updatedAnswers.get(TransactionVatAmountPage)   mustBe Some("20000")
        }
      }

      "when VAT is zero" - {
        "must set vat included to false and not set the vat amount page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            considerationVAT       = Some("0"),
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
            effectiveDate          = Some("15/01/2024"),
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

      "when transaction is linked" - {
        "must set linked transactions to true and populate the linked total page" in {
          val transaction = Transaction(
            transactionDescription   = Some("F"),
            effectiveDate            = Some("15/01/2024"),
            isLinked                 = Some("YES"),
            totalConsiderationLinked = Some("500000"),
            claimingRelief           = Some("NO")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(TransactionLinkedTransactionsPage)         mustBe Some(true)
          updatedAnswers.get(TotalConsiderationOfLinkedTransactionPage) mustBe Some("500000")
        }
      }

      "when transaction is not linked" - {
        "must set linked transactions to false and not set the linked total page" in {
          val transaction = Transaction(
            transactionDescription   = Some("F"),
            effectiveDate            = Some("15/01/2024"),
            isLinked                 = Some("NO"),
            totalConsiderationLinked = Some("500000"),
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
          effectiveDate            = Some("01/06/2024"),
          totalConsideration       = Some("100000"),
          considerationVAT         = Some("20000"),
          considerationCash        = Some("yes"),
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
          effectiveDate          = Some("15/01/2024"),
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
          effectiveDate          = Some("15/01/2024"),
          isLinked               = Some("NO"),
          claimingRelief         = Some("NO")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        result.get.get(TypeOfTransactionPage) mustBe Some(TransactionType.OtherTransaction)
      }
    }

    "forms of consideration" - {

      "must correctly map yes/no values when at least one is yes" in {
        val transaction = Transaction(
          transactionDescription   = Some("F"),
          effectiveDate            = Some("15/01/2024"),
          isLinked                 = Some("NO"),
          claimingRelief           = Some("NO"),
          considerationCash        = Some("yes"),
          considerationDebt        = Some("yes"),
          considerationBuild       = Some("no"),
          considerationEmploy      = Some("yes"),
          considerationOther       = Some("no"),
          considerationSharesQTD   = Some("yes"),
          considerationSharesUNQTD = Some("no"),
          considerationLand        = Some("yes"),
          considerationServices    = Some("no"),
          considerationContingent  = Some("yes")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        result.get.get(TransactionFormsOfConsiderationPage) mustBe Some(TransactionFormsOfConsiderationAnswers(
          cash                      = "yes",
          debt                      = "yes",
          buildingWorks             = "no",
          employment                = "yes",
          other                     = "no",
          sharesInAQuotedCompany    = "yes",
          sharesInAnUnquotedCompany = "no",
          otherLand                 = "yes",
          services                  = "no",
          contingent                = "yes"
        ))
      }

      "must not set forms of consideration page when all values are no" in {
        val transaction = Transaction(
          transactionDescription   = Some("F"),
          effectiveDate            = Some("15/01/2024"),
          isLinked                 = Some("NO"),
          claimingRelief           = Some("NO"),
          considerationCash        = Some("no"),
          considerationDebt        = Some("no"),
          considerationBuild       = Some("no"),
          considerationEmploy      = Some("no"),
          considerationOther       = Some("no"),
          considerationSharesQTD   = Some("no"),
          considerationSharesUNQTD = Some("no"),
          considerationLand        = Some("no"),
          considerationServices    = Some("no"),
          considerationContingent  = Some("no")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        result.get.get(TransactionFormsOfConsiderationPage) mustBe None
      }

      "must not set forms of consideration page when all values are absent" in {
        val transaction = Transaction(
          transactionDescription = Some("F"),
          effectiveDate          = Some("15/01/2024"),
          isLinked               = Some("NO"),
          claimingRelief         = Some("NO")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        result.get.get(TransactionFormsOfConsiderationPage) mustBe None
      }
    }

    "when relief is claimed" - {

      "when there is a partial relief amount" - {
        "must set partial relief to true and populate the amount page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefReason           = Some("20"),
            reliefAmount           = Some("5000"),
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
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefReason           = Some("20"),
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
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefReason           = Some("20"),
            reliefAmount           = None,
            reliefSchemeNumber     = Some("CS654321")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(ReasonForReliefPage)            mustBe Some(ReasonForRelief.CharitiesRelief)
          updatedAnswers.get(AddRegisteredCharityNumberPage) mustBe Some(true)
          updatedAnswers.get(CharityRegisteredNumberPage)    mustBe Some("CS654321")
        }
      }

      "when there is a partExchange scheme number" - {
        "must set CIS registered to true and populate the CIS number page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefReason           = Some("08"),
            reliefAmount           = None,
            reliefSchemeNumber     = Some("CIS123456")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(ReasonForReliefPage)              mustBe Some(ReasonForRelief.PartExchange)
          updatedAnswers.get(IsPurchaserRegisteredWithCISPage) mustBe Some(true)
          updatedAnswers.get(TransactionCisNumberPage)         mustBe Some("CIS123456")
        }
      }

      "when there is no scheme number for charitiesRelief" - {
        "must set add charity number to false and not set the number page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefReason           = Some("20"),
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

      "when there is no scheme number for partExchange" - {
        "must set CIS registered to false and not set the CIS number page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefReason           = Some("08"),
            reliefAmount           = None,
            reliefSchemeNumber     = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(IsPurchaserRegisteredWithCISPage) mustBe Some(false)
          updatedAnswers.get(TransactionCisNumberPage)         mustBe None
        }
      }

      "when relief reason is invalid" - {
        "must set eligible to true but not set reason for relief page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefReason           = Some("invalidReason"),
            reliefAmount           = None,
            reliefSchemeNumber     = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(PurchaserEligibleToClaimReliefPage) mustBe Some(true)
          updatedAnswers.get(ReasonForReliefPage)                mustBe None
        }
      }

      "when relief reason is absent" - {
        "must set eligible to true but not set reason for relief page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("YES"),
            reliefReason           = None,
            reliefAmount           = None,
            reliefSchemeNumber     = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(PurchaserEligibleToClaimReliefPage) mustBe Some(true)
          updatedAnswers.get(ReasonForReliefPage)                mustBe None
        }
      }
    }

    "when relief is not claimed" - {
      "must set eligible to false and partial relief to false" in {
        val transaction = Transaction(
          transactionDescription = Some("F"),
          effectiveDate          = Some("15/01/2024"),
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

    "consideration deferring pages" - {

      "when isDependantOnFutureEvent and agreedToDeferPayment are YES" - {
        "must set both to true" in {
          val transaction = Transaction(
            transactionDescription   = Some("F"),
            effectiveDate            = Some("15/01/2024"),
            isLinked                 = Some("NO"),
            claimingRelief           = Some("NO"),
            isDependantOnFutureEvent = Some("YES"),
            agreedToDeferPayment     = Some("YES")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(ConsiderationsAffectedUncertainPage) mustBe Some(true)
          updatedAnswers.get(TransactionDeferringPaymentPage)     mustBe Some(true)
        }
      }

      "when isDependantOnFutureEvent is YES and agreedToDeferPayment is NO" - {
        "must set consideration to true and deferring to false" in {
          val transaction = Transaction(
            transactionDescription   = Some("F"),
            effectiveDate            = Some("15/01/2024"),
            isLinked                 = Some("NO"),
            claimingRelief           = Some("NO"),
            isDependantOnFutureEvent = Some("YES"),
            agreedToDeferPayment     = Some("NO")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(ConsiderationsAffectedUncertainPage) mustBe Some(true)
          updatedAnswers.get(TransactionDeferringPaymentPage)     mustBe Some(false)
        }
      }

      "when isDependantOnFutureEvent and agreedToDeferPayment are NO" - {
        "must set both to false" in {
          val transaction = Transaction(
            transactionDescription   = Some("F"),
            effectiveDate            = Some("15/01/2024"),
            isLinked                 = Some("NO"),
            claimingRelief           = Some("NO"),
            isDependantOnFutureEvent = Some("NO"),
            agreedToDeferPayment     = Some("NO")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(ConsiderationsAffectedUncertainPage) mustBe Some(false)
          updatedAnswers.get(TransactionDeferringPaymentPage)     mustBe Some(false)
        }
      }

      "when isDependantOnFutureEvent is absent" - {
        "must set consideration to false" in {
          val transaction = Transaction(
            transactionDescription   = Some("F"),
            effectiveDate            = Some("15/01/2024"),
            isLinked                 = Some("NO"),
            claimingRelief           = Some("NO"),
            isDependantOnFutureEvent = None,
            agreedToDeferPayment     = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(ConsiderationsAffectedUncertainPage) mustBe Some(false)
          updatedAnswers.get(TransactionDeferringPaymentPage)     mustBe Some(false)
        }
      }
    }

    "property use pages" - {

      "must correctly map yes/no values when at least one is yes" in {
        val transaction = Transaction(
          transactionDescription = Some("F"),
          effectiveDate          = Some("15/01/2024"),
          isLinked               = Some("NO"),
          claimingRelief         = Some("NO"),
          usedAsOffice           = Some("yes"),
          usedAsHotel            = Some("no"),
          usedAsShop             = Some("yes"),
          usedAsWarehouse        = Some("no"),
          usedAsFactory          = Some("yes"),
          usedAsIndustrial       = Some("no"),
          usedAsOther            = Some("yes")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        result.get.get(TransactionUseOfLandOrPropertyPage) mustBe Some(TransactionUseOfLandOrPropertyAnswers(
          office              = "yes",
          hotel               = "no",
          shop                = "yes",
          warehouse           = "no",
          factory             = "yes",
          otherIndustrialUnit = "no",
          other               = "yes"
        ))
      }

      "must not set property use page when all values are no" in {
        val transaction = Transaction(
          transactionDescription = Some("F"),
          effectiveDate          = Some("15/01/2024"),
          isLinked               = Some("NO"),
          claimingRelief         = Some("NO"),
          usedAsOffice           = Some("no"),
          usedAsHotel            = Some("no"),
          usedAsShop             = Some("no"),
          usedAsWarehouse        = Some("no"),
          usedAsFactory          = Some("no"),
          usedAsIndustrial       = Some("no"),
          usedAsOther            = Some("no")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        result.get.get(TransactionUseOfLandOrPropertyPage) mustBe None
      }

      "must not set property use page when all values are absent" in {
        val transaction = Transaction(
          transactionDescription = Some("F"),
          effectiveDate          = Some("15/01/2024"),
          isLinked               = Some("NO"),
          claimingRelief         = Some("NO")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        result.get.get(TransactionUseOfLandOrPropertyPage) mustBe None
      }
    }

    "sale of business pages" - {

      "when total consideration for business is present" - {
        "must set sale of business to true and populate assets and total pages" in {
          val transaction = Transaction(
            transactionDescription     = Some("F"),
            effectiveDate              = Some("15/01/2024"),
            isLinked                   = Some("NO"),
            claimingRelief             = Some("NO"),
            totalConsiderationBusiness = Some("100000"),
            includesStock              = Some("yes"),
            includesGoodwill           = Some("no"),
            includesChattel            = Some("yes"),
            includesOther              = Some("no")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(SaleOfBusinessPage) mustBe Some(true)
          updatedAnswers.get(TransactionSaleOfBusinessAssetsPage) mustBe Some(TransactionSaleOfBusinessAssetsAnswers(
            stock                = "yes",
            goodwill             = "no",
            chattelsAndMoveables = "yes",
            others               = "no"
          ))
          updatedAnswers.get(TotalAssetsConsiderationPage) mustBe Some("100000")
        }
      }

      "when total consideration for business is absent" - {
        "must set sale of business to false" in {
          val transaction = Transaction(
            transactionDescription     = Some("F"),
            effectiveDate              = Some("15/01/2024"),
            isLinked                   = Some("NO"),
            claimingRelief             = Some("NO"),
            totalConsiderationBusiness = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(SaleOfBusinessPage)                  mustBe Some(false)
          updatedAnswers.get(TransactionSaleOfBusinessAssetsPage) mustBe None
          updatedAnswers.get(TotalAssetsConsiderationPage)        mustBe None
        }
      }
    }

    "cap1 or nsbc pages" - {

      "when postTransRulingFollowed is YES" - {
        "must set cap1OrNsbc to true and ruling followed to Yes" in {
          val transaction = Transaction(
            transactionDescription  = Some("F"),
            effectiveDate           = Some("15/01/2024"),
            isLinked                = Some("NO"),
            claimingRelief          = Some("NO"),
            postTransRulingFollowed = Some("yes")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(Cap1OrNsbcPage)                mustBe Some(true)
          updatedAnswers.get(TransactionRulingFollowedPage) mustBe Some(TransactionRulingFollowed.Yes)
        }
      }

      "when postTransRulingFollowed is NO" - {
        "must set cap1OrNsbc to true and ruling followed to No" in {
          val transaction = Transaction(
            transactionDescription  = Some("F"),
            effectiveDate           = Some("15/01/2024"),
            isLinked                = Some("NO"),
            claimingRelief          = Some("NO"),
            postTransRulingFollowed = Some("no")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(Cap1OrNsbcPage)                mustBe Some(true)
          updatedAnswers.get(TransactionRulingFollowedPage) mustBe Some(TransactionRulingFollowed.No)
        }
      }

      "when postTransRulingFollowed is rulingNotReceived" - {
        "must set cap1OrNsbc to true and ruling followed to RulingNotReceived" in {
          val transaction = Transaction(
            transactionDescription  = Some("F"),
            effectiveDate           = Some("15/01/2024"),
            isLinked                = Some("NO"),
            claimingRelief          = Some("NO"),
            postTransRulingFollowed = Some("rulingNotReceived")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(Cap1OrNsbcPage)                mustBe Some(true)
          updatedAnswers.get(TransactionRulingFollowedPage) mustBe Some(TransactionRulingFollowed.RulingNotReceived)
        }
      }

      "when postTransRulingFollowed is an unrecognised value" - {
        "must set cap1OrNsbc to true but not set ruling followed page" in {
          val transaction = Transaction(
            transactionDescription  = Some("F"),
            effectiveDate           = Some("15/01/2024"),
            isLinked                = Some("NO"),
            claimingRelief          = Some("NO"),
            postTransRulingFollowed = Some("unknown")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(Cap1OrNsbcPage)                mustBe Some(true)
          updatedAnswers.get(TransactionRulingFollowedPage) mustBe None
        }
      }

      "when postTransRulingFollowed is absent" - {
        "must set cap1OrNsbc to false" in {
          val transaction = Transaction(
            transactionDescription  = Some("F"),
            effectiveDate           = Some("15/01/2024"),
            isLinked                = Some("NO"),
            claimingRelief          = Some("NO"),
            postTransRulingFollowed = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(Cap1OrNsbcPage)                mustBe Some(false)
          updatedAnswers.get(TransactionRulingFollowedPage) mustBe None
        }
      }
    }

    "restriction pages" - {

      "when restriction details are present" - {
        "must set restrictions to true and populate description page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("NO"),
            restrictionDetails     = Some("some restriction details")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(TransactionRestrictionsCovenantsAndConditionsPage) mustBe Some(true)
          updatedAnswers.get(DescriptionOfRestrictionsPage)                     mustBe Some("some restriction details")
        }
      }

      "when restriction details are absent" - {
        "must set restrictions to false" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("NO"),
            restrictionDetails     = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(TransactionRestrictionsCovenantsAndConditionsPage) mustBe Some(false)
          updatedAnswers.get(DescriptionOfRestrictionsPage)                     mustBe None
        }
      }
    }

    "exchange land pages" - {

      "when exchanged land address is present" - {
        "must set land exchanged to true and populate address page" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("NO"),
            exchangedLandAddress1  = Some("1 Exchange Street"),
            exchangedLandAddress2  = Some("Exchange Town"),
            exchangedLandAddress3  = None,
            exchangedLandAddress4  = None,
            exchangedLandPostcode  = Some("EX1 1EX")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(IsLandOrPropertyExchangedPage) mustBe Some(true)
          updatedAnswers.get(TransactionAddressPage) mustBe Some(Address(
            line1    = "1 Exchange Street",
            line2    = Some("Exchange Town"),
            line3    = None,
            line4    = None,
            line5    = None,
            postcode = Some("EX1 1EX")
          ))
        }
      }

      "when exchanged land address is absent" - {
        "must set land exchanged to false" in {
          val transaction = Transaction(
            transactionDescription = Some("F"),
            effectiveDate          = Some("15/01/2024"),
            isLinked               = Some("NO"),
            claimingRelief         = Some("NO"),
            exchangedLandAddress1  = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          val updatedAnswers = result.get

          updatedAnswers.get(IsLandOrPropertyExchangedPage) mustBe Some(false)
          updatedAnswers.get(TransactionAddressPage)        mustBe None
        }
      }
    }

    "option page" - {

      "when isPursuantToPreviousOption is YES" - {
        "must set exercising an option to true" in {
          val transaction = Transaction(
            transactionDescription     = Some("F"),
            effectiveDate              = Some("15/01/2024"),
            isLinked                   = Some("NO"),
            claimingRelief             = Some("NO"),
            isPursuantToPreviousOption = Some("YES")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          result.get.get(TransactionExercisingAnOptionPage) mustBe Some(true)
        }
      }

      "when isPursuantToPreviousOption is NO" - {
        "must set exercising an option to false" in {
          val transaction = Transaction(
            transactionDescription     = Some("F"),
            effectiveDate              = Some("15/01/2024"),
            isLinked                   = Some("NO"),
            claimingRelief             = Some("NO"),
            isPursuantToPreviousOption = Some("NO")
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          result.get.get(TransactionExercisingAnOptionPage) mustBe Some(false)
        }
      }

      "when isPursuantToPreviousOption is absent" - {
        "must set exercising an option to false" in {
          val transaction = Transaction(
            transactionDescription     = Some("F"),
            effectiveDate              = Some("15/01/2024"),
            isLinked                   = Some("NO"),
            claimingRelief             = Some("NO"),
            isPursuantToPreviousOption = None
          )

          val result = service.populateTransactionInSession(transaction, userAnswers)

          result mustBe a[Success[_]]

          result.get.get(TransactionExercisingAnOptionPage) mustBe Some(false)
        }
      }
    }

    "graceful handling of incomplete or unparseable data" - {

      "must succeed when transactionDescription is missing, leaving the page unset" in {
        val transaction = Transaction(
          transactionDescription = None,
          effectiveDate          = Some("15/01/2024"),
          isLinked               = Some("NO"),
          claimingRelief         = Some("NO")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]
        result.get.get(TypeOfTransactionPage) mustBe None
      }

      "must succeed when transactionDescription is an unrecognised code, leaving the page unset" in {
        val transaction = Transaction(
          transactionDescription = Some("INVALID"),
          effectiveDate          = Some("15/01/2024"),
          isLinked               = Some("NO"),
          claimingRelief         = Some("NO")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]
        result.get.get(TypeOfTransactionPage) mustBe None
      }

      "must succeed when effectiveDate is missing, leaving the page unset" in {
        val transaction = Transaction(
          transactionDescription = Some("F"),
          effectiveDate          = None,
          isLinked               = Some("NO"),
          claimingRelief         = Some("NO")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]
        result.get.get(TransactionEffectiveDatePage) mustBe None
      }

      "must populate everything else even when transactionDescription is missing" in {
        val transaction = Transaction(
          transactionDescription = None,
          effectiveDate          = Some("15/01/2024"),
          isLinked               = Some("YES"),
          totalConsiderationLinked = Some("500000"),
          claimingRelief         = Some("NO")
        )

        val result = service.populateTransactionInSession(transaction, userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(TypeOfTransactionPage)                     mustBe None
        updatedAnswers.get(TransactionEffectiveDatePage)              mustBe Some(LocalDate.of(2024, 1, 15))
        updatedAnswers.get(TransactionLinkedTransactionsPage)         mustBe Some(true)
        updatedAnswers.get(TotalConsiderationOfLinkedTransactionPage) mustBe Some("500000")
        updatedAnswers.get(PurchaserEligibleToClaimReliefPage)        mustBe Some(false)
      }
    }
  }
}