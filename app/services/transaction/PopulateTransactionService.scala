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

import models.address.Address
import models.{Transaction, UserAnswers}
import models.prelimQuestions.TransactionType
import models.transaction.{ReasonForRelief, TransactionFormsOfConsiderationAnswers, TransactionRulingFollowed, TransactionSaleOfBusinessAssetsAnswers, TransactionUseOfLandOrPropertyAnswers}
import pages.transaction.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Success, Try}

class PopulateTransactionService {

  def populateTransactionInSession(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] =
    for {
      withTypeOfTransaction               <- typeOfTransactionPage(transaction, userAnswers)
      withEffectiveDate                   <- effectiveDatePage(transaction, withTypeOfTransaction)
      withContractDate                    <- contractDatePages(transaction, withEffectiveDate)
      withConsideration                   <- considerationPages(transaction, withContractDate)
      withLinkedTransactions              <- linkedTransactionPages(transaction, withConsideration)
      withReliefPages                     <- reliefPages(transaction, withLinkedTransactions)
      withConsiderationDeferringPages     <- considerationDeferringPages(transaction, withReliefPages)
      withSaleBusinessPage                <- saleBusinessPage(transaction, withConsiderationDeferringPages)
      withCap1OrNsbcPages                 <- cap1OrNsbcPages(transaction, withSaleBusinessPage)
      withRestrictionPages                <- restrictionPages(transaction, withCap1OrNsbcPages)
      withExchangeLandPages               <- exchangeLandPages(transaction, withRestrictionPages)
      finalAnswers                        <- optionPage(transaction, withExchangeLandPages)
    } yield finalAnswers

  private def typeOfTransactionPage(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] =
    TransactionType.parse(transaction.transactionDescription) match {
      case Some(transactionType) => userAnswers.set(TypeOfTransactionPage, transactionType)
      case None                  => Success(userAnswers)
    }

  private def effectiveDatePage(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] =
    transaction.effectiveDate match {
      case Some(dateStr) => Try(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))).flatMap(userAnswers.set(TransactionEffectiveDatePage, _))
      case None          => Success(userAnswers)
    }

  private def contractDatePages(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] =
    transaction.contractDate match {
      case Some(dateStr) =>
        for {
          withAddDateOfContract <- userAnswers.set(TransactionAddDateOfContractPage, true)
          date                  <- Try(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")))
          finalAnswers          <- withAddDateOfContract.set(TransactionDateOfContractPage, date)
        } yield finalAnswers
      case None =>
        userAnswers.set(TransactionAddDateOfContractPage, false)
    }

  private def considerationPages(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] =
    if (userAnswers.get(TypeOfTransactionPage).contains(TransactionType.GrantOfLease)) {
      Success(userAnswers)
    } else {
      for {
        withTotalConsideration <- totalConsiderationPage(transaction, userAnswers)
        withVat                <- vatPages(transaction, withTotalConsideration)
        finalAnswers           <- formsOfConsiderationPage(transaction, withVat)
      } yield finalAnswers
    }

  private def totalConsiderationPage(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] =
    transaction.totalConsideration match {
      case Some(amount) => userAnswers.set(TotalConsiderationOfTransactionPage, amount)
      case None         => Success(userAnswers)
    }

  private def vatPages(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] =
    transaction.considerationVAT match {
      case Some(vat) if vat.nonEmpty && vat.toDouble > 0 =>
        for {
          withVatIncluded <- userAnswers.set(TransactionVatIncludedPage, true)
          finalAnswers <- withVatIncluded.set(TransactionVatAmountPage, vat)
        } yield finalAnswers
      case _ =>
        userAnswers.set(TransactionVatIncludedPage, false)
    }

  private def formsOfConsiderationPage(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] = {
    TransactionFormsOfConsiderationAnswers.fromTransaction(transaction) match {
      case Some(answers) =>
        userAnswers.set(TransactionFormsOfConsiderationPage, answers)
      case _ =>
        Success(userAnswers)
    }
  }

  private def linkedTransactionPages(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] = {
    val isLinked = transaction.isLinked.exists(_.equalsIgnoreCase("YES"))
    if (isLinked) {
      for {
        withLinked   <- userAnswers.set(TransactionLinkedTransactionsPage, true)
        finalAnswers <- transaction.totalConsiderationLinked match {
          case Some(amount) => withLinked.set(TotalConsiderationOfLinkedTransactionPage, amount)
          case None         => Success(withLinked)
        }
      } yield finalAnswers
    } else {
      userAnswers.set(TransactionLinkedTransactionsPage, false)
    }
  }

  private def reliefPages(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] = {
    val isClaimingRelief = transaction.claimingRelief.exists(_.equalsIgnoreCase("YES"))
    val reliefReason = transaction.reliefReason

     (isClaimingRelief, reliefReason) match {
       case (true, Some(reliefReason)) if ReasonForRelief.isValid(reliefReason) =>
       for {
        withEligible      <- userAnswers.set(PurchaserEligibleToClaimReliefPage, true)
        withReliefReason  <- withEligible.set(ReasonForReliefPage, ReasonForRelief.fromString(reliefReason))
        withReliefSchemePages <- reliefSchemePages(transaction, withReliefReason)
        finalAnswers      <- partialReliefPages(transaction, withReliefSchemePages)
      } yield finalAnswers
       case (true, _) =>
         for {
           finalAnswers <- userAnswers.set(PurchaserEligibleToClaimReliefPage, true)
         } yield finalAnswers
       case _ =>
         for {
        withEligible <- userAnswers.set(PurchaserEligibleToClaimReliefPage, false)
        finalAnswers <- withEligible.set(TransactionPartialReliefPage, false)
      } yield finalAnswers
    }
  }

  private def partialReliefPages(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] =
    transaction.reliefAmount match {
      case Some(amount) =>
        for {
          withPartialRelief <- userAnswers.set(TransactionPartialReliefPage, true)
          finalAnswers      <- withPartialRelief.set(ClaimingPartialReliefAmountPage, amount)
        } yield finalAnswers
      case None =>
        userAnswers.set(TransactionPartialReliefPage, false)
    }

  private def reliefSchemePages(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] = {
    (transaction.reliefSchemeNumber, transaction.reliefReason) match {
      case (Some(schemeNumber), Some(reliefReason)) if reliefReason.contains("20") =>
        for {
          withAddCharity <- userAnswers.set(AddRegisteredCharityNumberPage, true)
          finalAnswers <- withAddCharity.set(CharityRegisteredNumberPage, schemeNumber)
        } yield finalAnswers
      case (None, Some(reliefReason)) if reliefReason.contains("20") =>
        userAnswers.set(AddRegisteredCharityNumberPage, false)
      case (Some(schemeNumber), Some(reliefReason)) if reliefReason.contains("08") =>
        for {
          withCIS <- userAnswers.set(IsPurchaserRegisteredWithCISPage, true)
          finalAnswers <- withCIS.set(TransactionCisNumberPage, schemeNumber)
        } yield finalAnswers
      case (None, Some(reliefReason)) if reliefReason.contains("08") =>
        userAnswers.set(IsPurchaserRegisteredWithCISPage, false)
      case _ =>
        Try(userAnswers)
    }
  }

  private def considerationDeferringPages(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] = {
    val considerationCheck: Boolean = transaction.isDependantOnFutureEvent.exists(_.equalsIgnoreCase("YES"))
    val deferringCheck: Boolean = transaction.agreedToDeferPayment.exists(_.equalsIgnoreCase("YES"))

    for {
      withConsideration <- userAnswers.set(ConsiderationsAffectedUncertainPage, considerationCheck)
      withDeferring <- withConsideration.set(TransactionDeferringPaymentPage, deferringCheck)
      finalAnswers <- propertyUsePage(transaction, withDeferring)
    } yield finalAnswers
  }

  private def propertyUsePage(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] = {
  TransactionUseOfLandOrPropertyAnswers.fromTransaction(transaction) match {
    case Some(answers) =>
      userAnswers.set(TransactionUseOfLandOrPropertyPage, answers)
    case _ =>
      Success(userAnswers)
  }
}

  private def includedSaleBusinessPage(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] = {
    TransactionSaleOfBusinessAssetsAnswers.fromTransaction(transaction) match {
      case Some(answers) =>
        userAnswers.set(TransactionSaleOfBusinessAssetsPage, answers)
      case _ =>
        Success(userAnswers)
    }
  }


  private def saleBusinessPage(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] = {
    transaction.totalConsiderationBusiness match {
      case Some(totalConsideration) =>
        for {
          withSaleOfBusiness <- userAnswers.set(SaleOfBusinessPage, true)
          withIncludedSaleBusinessPage <- includedSaleBusinessPage(transaction, withSaleOfBusiness)
          finalAnswers <- withIncludedSaleBusinessPage.set(TotalAssetsConsiderationPage, totalConsideration)
        } yield finalAnswers
      case None =>
        userAnswers.set(SaleOfBusinessPage, false)
    }
  }

  private def cap1OrNsbcPages(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] = {
    val hasCap1OrNsbc = transaction.postTransRulingFollowed.exists(_.nonEmpty)

    if (hasCap1OrNsbc) {
      for {
        withCap1OrNsbc <- userAnswers.set(Cap1OrNsbcPage, true)
        finalAnswers <- rulingFollowedPage(transaction, withCap1OrNsbc)
      } yield finalAnswers
    } else {
      userAnswers.set(Cap1OrNsbcPage, false)
    }
  }

  private def rulingFollowedPage(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] = {
    TransactionRulingFollowed.parse(transaction.postTransRulingFollowed) match {
      case Some(transactionRulingFollowed) =>
        userAnswers.set(TransactionRulingFollowedPage, transactionRulingFollowed)
      case _ =>
        Success(userAnswers)
    }
  }

  private def restrictionPages(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] =
    transaction.restrictionDetails match {
      case Some(details) =>
        for {
          withTransactionRestrictions <- userAnswers.set(TransactionRestrictionsCovenantsAndConditionsPage, true)
          finalAnswers <- withTransactionRestrictions.set(DescriptionOfRestrictionsPage, details)
        } yield finalAnswers
      case None =>
        userAnswers.set(TransactionRestrictionsCovenantsAndConditionsPage, false)
    }

  private def exchangeLandPages(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] =
    transaction.exchangedLandAddress1 match {
      case Some(landAddress1) =>
        for {
          withIsLandOrPropertyExchanged <- userAnswers.set(IsLandOrPropertyExchangedPage, true)
          finalAnswers <- withIsLandOrPropertyExchanged.set(TransactionAddressPage,
            Address(line1 = landAddress1, line2 = transaction.exchangedLandAddress2, line3 = transaction.exchangedLandAddress3,
              line4 = transaction.exchangedLandAddress4, line5 = transaction.exchangedLandPostcode, postcode = transaction.exchangedLandPostcode))
        } yield finalAnswers
      case _ =>
        userAnswers.set(IsLandOrPropertyExchangedPage, false)
    }

  private def optionPage(transaction: Transaction, userAnswers: UserAnswers): Try[UserAnswers] = {
    val isPursuant = transaction.isPursuantToPreviousOption.exists(_.equalsIgnoreCase("YES"))
    
    if (isPursuant) {
      userAnswers.set(TransactionExercisingAnOptionPage, true)
    } else {
      userAnswers.set(TransactionExercisingAnOptionPage, false)
    }
  }

}
