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

package services.taxCalculation

import models.UserAnswers
import models.prelimQuestions.TransactionType
import models.taxCalculation.UpdateTaxCalculationRequest
import models.transaction.ReasonForRelief
import pages.land.{LandInterestTransferredOrCreatedPage, LandTypeOfPropertyPage}
import pages.lease.{LeaseEndDatePage, LeaseNetPresentValuePage, LeaseStartDatePage, LeaseThousandPoundsThresholdPage}
import pages.transaction.{ClaimingPartialReliefAmountPage, PurchaserEligibleToClaimReliefPage, ReasonForReliefPage, TotalConsiderationOfTransactionPage, TransactionEffectiveDatePage, TransactionLinkedTransactionsPage, TypeOfTransactionPage}
import pages.ukResidency.NonUkResidentPurchaserPage

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class UpdateTaxCalcService {

  def updateTaxCalcRequest(ua: UserAnswers): Future[UpdateTaxCalculationRequest] = {
    ua.fullReturn match {
      case Some(fullReturn) =>
        Future.successful(
          UpdateTaxCalculationRequest(
            stornId = ua.storn,
            returnResourceRef = fullReturn.returnResourceRef,
            amountPaid = fullReturn.taxCalculation.flatMap(_.amountPaid),
            includesPenalty = fullReturn.taxCalculation.flatMap(_.includesPenalty),
            taxDue = None,
            calcPenaltyDue = fullReturn.taxCalculation.flatMap(_.calcPenaltyDue),
            calcTaxDue = fullReturn.taxCalculation.flatMap(_.calcTaxDue),
            calcTaxRate1 = fullReturn.taxCalculation.flatMap(_.calcTaxRate1),
            calcTaxRate2 = fullReturn.taxCalculation.flatMap(_.calcTaxRate2),
            calcTotalTaxPenaltyDue = fullReturn.taxCalculation.flatMap(_.calcTotalTaxPenaltyDue),
            calcTotalNpvTax = fullReturn.taxCalculation.flatMap(_.calcTotalNPVTax),
            calcTotalPremiumTax = fullReturn.taxCalculation.flatMap(_.calcTotalPremiumTax),
            taxDuePremium = fullReturn.taxCalculation.flatMap(_.taxDuePremium),
            taxDueNpv = fullReturn.taxCalculation.flatMap(_.taxDueNPV),
            honestyDeclaration = fullReturn.taxCalculation.flatMap(_.honestyDeclaration)
          )
        )
      case None =>
        Future.failed(new Exception("No full return found"))
    }
  }

  private def taxCalcExists(ua: UserAnswers): Boolean = {
    ua.fullReturn.flatMap(_.taxCalculation).isDefined
  }
  
  private def transactionDescriptionCheckFails(ua: UserAnswers): Boolean = {
    val transactionDescription = ua.fullReturn.flatMap(_.transaction).flatMap(_.transactionDescription)
    val transactionDescriptionPage = ua.get(TypeOfTransactionPage)
    
    (transactionDescription, transactionDescriptionPage) match {
      case (Some(descriptionCode), Some(descriptionPage)) => if (!TransactionType.parse(Some(descriptionCode)).contains(descriptionPage)) true else false
      case _ => false
    }
  }
  
  private def transactionEffectiveDateCheckFails(ua: UserAnswers): Boolean = {
    val effectiveDate = ua.fullReturn.flatMap(_.transaction).flatMap(_.effectiveDate)
    val effectiveDatePage = ua.get(TransactionEffectiveDatePage)
    
    (effectiveDate, effectiveDatePage) match {
      case (Some(effectiveDateValue), Some(effectiveDatePageValue)) =>
        if (effectiveDatePageValue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) != effectiveDateValue) true else false
      case _ => false
    }
  }

  private def transactionTotalConsiderationFails(ua: UserAnswers): Boolean = {
    val transactionTotalConsideration = ua.fullReturn.flatMap(_.transaction).flatMap(_.totalConsideration)
    val transactionTotalConsiderationPage = ua.get(TotalConsiderationOfTransactionPage)
    (transactionTotalConsideration, transactionTotalConsiderationPage) match {
      case (Some(tc), Some(tcp)) =>
        try {
          if (BigDecimal(tc.replace(",", "")) != BigDecimal(tcp.replace(",", ""))) true else false
        } catch {
          case _: NumberFormatException => false
        }
      case _ => false
    }
  }

    private def transactionIsLinkedFails(ua: UserAnswers): Boolean = {
    val transactionIsLinked = ua.fullReturn.flatMap(_.transaction).flatMap(_.isLinked)
    val transactionIsLinkedPage = ua.get(TransactionLinkedTransactionsPage)

    (transactionIsLinked, transactionIsLinkedPage) match {
      case (Some(isLinked), Some(isLinkedPage)) =>
        val isLinkedString = if (isLinkedPage) "yes" else "no"
        if (!isLinked.equalsIgnoreCase(isLinkedString)) true else false
      case _ => false
    }
  }

  private def transactionClaimingReliefFails(ua: UserAnswers): Boolean = {
    val transactionClaimingRelief = ua.fullReturn.flatMap(_.transaction).flatMap(_.claimingRelief)
    val transactionClaimingReliefPage = ua.get(PurchaserEligibleToClaimReliefPage)

    (transactionClaimingRelief, transactionClaimingReliefPage) match {
      case (Some(claimingRelief), Some(claimingReliefPage)) =>
        val claimingReliefString = if (claimingReliefPage) "yes" else "no"
        if (!claimingRelief.equalsIgnoreCase(claimingReliefString)) true else false
      case _ => false
    }
  }

  private def transactionReliefReasonFails(ua: UserAnswers): Boolean = {
    val transactionReliefReason = ua.fullReturn.flatMap(_.transaction).flatMap(_.reliefReason)
    val transactionReliefReasonPage = ua.get(ReasonForReliefPage)

    (transactionReliefReason, transactionReliefReasonPage) match {
      case (Some(reliefReason), Some(reliefReasonPage)) => if (!ReasonForRelief.parse(Some(reliefReason)).contains(reliefReasonPage)) true else false
      case _ => false
    }
  }

  private def transactionReliefAmountFails(ua: UserAnswers): Boolean = {
    val transactionReliefAmount = ua.fullReturn.flatMap(_.transaction).flatMap(_.reliefAmount)
    val transactionReliefAmountPage = ua.get(ClaimingPartialReliefAmountPage)
    (transactionReliefAmount, transactionReliefAmountPage) match {
      case (Some(reliefAmount), Some(reliefAmountPage)) =>
        try {
          if (BigDecimal(reliefAmount.replace(",", "")) != BigDecimal(reliefAmountPage.replace(",", ""))) true else false
        } catch {
          case _: NumberFormatException => false
        }
      case _ => false
    }
  }
  
  private def annualRentCheckFails(ua: UserAnswers): Boolean = {
    val annualRent = ua.fullReturn.flatMap(_.lease).flatMap(_.isAnnualRentOver1000)
    val annualRentPage = ua.get(LeaseThousandPoundsThresholdPage)

    (annualRent, annualRentPage) match {
      case (Some(annualRent), Some(annualRentPage)) =>
        val annualRentString = if (annualRentPage) "yes" else "no"
        if (!annualRent.equalsIgnoreCase(annualRentString)) true else false
      case _ => false
    }
  }

  private def netPresetCheckFails(ua: UserAnswers): Boolean = {
    val netPreset = ua.fullReturn.flatMap(_.lease).flatMap(_.netPresentValue)
    val netPresetPage = ua.get(LeaseNetPresentValuePage)
    (netPreset, netPresetPage) match {
      case (Some(np), Some(npp)) =>
        try {
          if (BigDecimal(np.replace(",", "")) != BigDecimal(npp.replace(",", ""))) true else false
        } catch {
          case _: NumberFormatException => false
        }
      case _ => false
    }
  }
  private def contractStartDateCheckFails(ua: UserAnswers): Boolean = {
    val contractStartDate = ua.fullReturn.flatMap(_.lease).flatMap(_.contractStartDate)
    val contractStartDatePage = ua.get(LeaseStartDatePage)

    (contractStartDate, contractStartDatePage) match {
      case (Some(startDate), Some(startDatePage)) =>
        if (startDatePage.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) != startDate) true else false
      case _ => false
    }
  }

  private def contractEndDateCheckFails(ua: UserAnswers): Boolean = {
    val contractEndDate = ua.fullReturn.flatMap(_.lease).flatMap(_.contractEndDate)
    val contractEndDatePage = ua.get(LeaseEndDatePage)

    (contractEndDate, contractEndDatePage) match {
      case (Some(endDate), Some(endDatePage)) =>
        if (endDatePage.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) != endDate) true else false
      case _ => false
    }
  }

  private def isNonUkResidentCheckFails(ua: UserAnswers): Boolean = {
    val isNonUkResident = ua.fullReturn.flatMap(_.residency).flatMap(_.isNonUkResidents)
    val isNonUkResidentPage = ua.get(NonUkResidentPurchaserPage)
    
    (isNonUkResident, isNonUkResidentPage)  match {
      case (Some(resident), Some(residentPage)) =>
        val residentString = if (residentPage) "yes" else "no"
        if (!resident.equalsIgnoreCase(residentString)) true else false
      case _ => false
    }
  }
  
  private def landInterestTransferredCheckFails(ua: UserAnswers): Boolean = {
    val mainLandId = ua.fullReturn.flatMap(_.returnInfo).flatMap(_.mainLandID)
    val interestCreatedTransferred = ua.fullReturn.flatMap(_.land).flatMap(_.find(l => l.landID == mainLandId)).flatMap(_.interestCreatedTransferred)
    val pageInterestCreated = ua.get(LandInterestTransferredOrCreatedPage)

    (interestCreatedTransferred, pageInterestCreated) match {
      case (Some(interest), Some(interestPage)) => if (interest != interestPage.toString) true else false
      case _ => false
    }
  }
  
  private def landPropertyTypeCheckFails(ua: UserAnswers): Boolean = {
    val mainLandId = ua.fullReturn.flatMap(_.returnInfo).flatMap(_.mainLandID)
    val typeOfProperty = ua.fullReturn.flatMap(_.land).flatMap(_.find(l => l.landID == mainLandId)).flatMap(_.propertyType)
    val pagePropertyType = ua.get(LandTypeOfPropertyPage)

    (typeOfProperty, pagePropertyType) match {
      case (Some(fullReturnType), Some(pageType)) => if (fullReturnType != pageType.toString) true else false
      case _ => false
    }
  }
  
  def transactionDataMatches(ua: UserAnswers): Boolean = {
    (transactionDescriptionCheckFails(ua) || transactionEffectiveDateCheckFails(ua) || transactionTotalConsiderationFails(ua)
      || transactionIsLinkedFails(ua) || transactionClaimingReliefFails(ua) || transactionReliefReasonFails(ua)
      || transactionReliefAmountFails(ua)) && taxCalcExists(ua)
  }

  def leaseDataMatches(ua: UserAnswers): Boolean = {
    (contractStartDateCheckFails(ua) || contractEndDateCheckFails(ua) || annualRentCheckFails(ua) || netPresetCheckFails(ua)) && taxCalcExists(ua)
  }
  
  def residencyDataMatches(ua: UserAnswers): Boolean ={
    isNonUkResidentCheckFails(ua) && taxCalcExists(ua)
  }
  
  def landDataMatches(ua: UserAnswers): Boolean = {
    (landInterestTransferredCheckFails(ua) || landPropertyTypeCheckFails(ua)) && taxCalcExists(ua)
  }
}
