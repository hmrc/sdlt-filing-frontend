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

import models.{TaxCalculation, UserAnswers}
import models.taxCalculation.TaxCalculationFlow
import models.taxCalculation.TaxCalculationFlow.{FreeholdSelfAssessed, FreeholdTaxCalculated, LeaseholdSelfAssessed, LeaseholdTaxCalculated}
import pages.taxCalculation.freeholdSelfAssessed.{FreeholdSelfAssessedAmountPage, FreeholdSelfAssessedPenaltiesAndInterestPage, FreeholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedPenaltiesAndInterestPage, FreeholdTaxCalculatedSelfAssessedAmountPage, FreeholdTaxCalculatedTotalAmountDuePage}
import pages.taxCalculation.leaseholdSelfAssessed.{LeaseholdSelfAssessedNpvTaxPage, LeaseholdSelfAssessedPenaltiesAndInterestPage, LeaseholdSelfAssessedPremiumPayableTaxPage, LeaseholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.leaseholdTaxCalculated.{LeaseholdTaxCalculatedPenaltiesAndInterestPage, LeaseholdTaxCalculatedSelfAssessedAmountPage, LeaseholdTaxCalculatedTotalAmountDuePage}

import scala.util.{Success, Try}

class PopulateTaxCalculationService {

  def populateTaxCalculationInSession(taxCalculation: TaxCalculation, flow: TaxCalculationFlow, userAnswers: UserAnswers): Try[UserAnswers] =
    for {
      withTotalAmountDue <- totalAmountDuePage(taxCalculation, flow, userAnswers)
      withPenalties      <- penaltiesPage(taxCalculation, flow, withTotalAmountDue)
      finalAnswers       <- taxDuePages(taxCalculation, flow, withPenalties)
    } yield finalAnswers

  private def totalAmountDuePage(taxCalculation: TaxCalculation, flow: TaxCalculationFlow, userAnswers: UserAnswers): Try[UserAnswers] = {
    val page = flow match {
      case FreeholdTaxCalculated  => FreeholdTaxCalculatedTotalAmountDuePage
      case FreeholdSelfAssessed   => FreeholdSelfAssessedTotalAmountDuePage
      case LeaseholdTaxCalculated => LeaseholdTaxCalculatedTotalAmountDuePage
      case LeaseholdSelfAssessed  => LeaseholdSelfAssessedTotalAmountDuePage
    }
    taxCalculation.amountPaid match {
      case Some(amount) => userAnswers.set(page, amount)
      case None         => Success(userAnswers)
    }
  }

  private def penaltiesPage(taxCalculation: TaxCalculation, flow: TaxCalculationFlow, userAnswers: UserAnswers): Try[UserAnswers] = {
    val page = flow match {
      case FreeholdTaxCalculated  => FreeholdTaxCalculatedPenaltiesAndInterestPage
      case FreeholdSelfAssessed   => FreeholdSelfAssessedPenaltiesAndInterestPage
      case LeaseholdTaxCalculated => LeaseholdTaxCalculatedPenaltiesAndInterestPage
      case LeaseholdSelfAssessed  => LeaseholdSelfAssessedPenaltiesAndInterestPage
    }
    taxCalculation.includesPenalty.map(_.equalsIgnoreCase("yes")) match {
      case Some(includesPenalty) => userAnswers.set(page, includesPenalty)
      case None                  => Success(userAnswers)
    }
  }

  private def taxDuePages(taxCalculation: TaxCalculation, flow: TaxCalculationFlow, userAnswers: UserAnswers): Try[UserAnswers] =
    flow match {
      case FreeholdTaxCalculated =>
        taxCalculation.taxDue match {
          case Some(amount) => userAnswers.set(FreeholdTaxCalculatedSelfAssessedAmountPage, amount)
          case None         => Success(userAnswers)
        }
      case FreeholdSelfAssessed =>
        taxCalculation.taxDue match {
          case Some(amount) => userAnswers.set(FreeholdSelfAssessedAmountPage, amount)
          case None         => Success(userAnswers)
        }
      case LeaseholdTaxCalculated =>
        taxCalculation.taxDue match {
          case Some(amount) => userAnswers.set(LeaseholdTaxCalculatedSelfAssessedAmountPage, amount)
          case None         => Success(userAnswers)
        }
      case LeaseholdSelfAssessed =>
        for {
          withPremium  <- taxCalculation.taxDuePremium match {
            case Some(premium) => userAnswers.set(LeaseholdSelfAssessedPremiumPayableTaxPage, premium)
            case None          => Success(userAnswers)
          }
          finalAnswers <- taxCalculation.taxDueNPV match {
            case Some(npv) => withPremium.set(LeaseholdSelfAssessedNpvTaxPage, npv)
            case None      => Success(withPremium)
          }
        } yield finalAnswers
    }
}
