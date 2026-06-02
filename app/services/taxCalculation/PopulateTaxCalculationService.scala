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
import pages.QuestionPage
import pages.taxCalculation.freeholdSelfAssessed.{FreeholdSelfAssessedAmountPage, FreeholdSelfAssessedPenaltiesAndInterestPage, FreeholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedPenaltiesAndInterestPage, FreeholdTaxCalculatedSelfAssessedAmountPage, FreeholdTaxCalculatedTotalAmountDuePage}
import pages.taxCalculation.leaseholdSelfAssessed.{LeaseholdSelfAssessedNpvTaxPage, LeaseholdSelfAssessedPenaltiesAndInterestPage, LeaseholdSelfAssessedPremiumPayableTaxPage, LeaseholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.leaseholdTaxCalculated.{LeaseholdTaxCalculatedPenaltiesAndInterestPage, LeaseholdTaxCalculatedSelfAssessedAmountPage, LeaseholdTaxCalculatedTotalAmountDuePage}

import scala.util.{Success, Try}

class PopulateTaxCalculationService {

  def populateTaxCalculationInSession(taxCalculation: TaxCalculation, flow: TaxCalculationFlow, userAnswers: UserAnswers): Try[UserAnswers] =
    flow match {
      case FreeholdTaxCalculated =>
        for {
          a <- setString(userAnswers, FreeholdTaxCalculatedTotalAmountDuePage, taxCalculation.amountPaid)
          b <- setBoolean(a, FreeholdTaxCalculatedPenaltiesAndInterestPage, includesPenalty(taxCalculation))
          c <- setString(b, FreeholdTaxCalculatedSelfAssessedAmountPage, taxCalculation.taxDue)
        } yield c

      case FreeholdSelfAssessed =>
        for {
          a <- setString(userAnswers, FreeholdSelfAssessedTotalAmountDuePage, taxCalculation.amountPaid)
          b <- setBoolean(a, FreeholdSelfAssessedPenaltiesAndInterestPage, includesPenalty(taxCalculation))
          c <- setString(b, FreeholdSelfAssessedAmountPage, taxCalculation.taxDue)
        } yield c

      case LeaseholdTaxCalculated =>
        for {
          a <- setString(userAnswers, LeaseholdTaxCalculatedTotalAmountDuePage, taxCalculation.amountPaid)
          b <- setBoolean(a, LeaseholdTaxCalculatedPenaltiesAndInterestPage, includesPenalty(taxCalculation))
          c <- setString(b, LeaseholdTaxCalculatedSelfAssessedAmountPage, taxCalculation.taxDue)
        } yield c

      case LeaseholdSelfAssessed =>
        for {
          a <- setString(userAnswers, LeaseholdSelfAssessedTotalAmountDuePage, taxCalculation.amountPaid)
          b <- setBoolean(a, LeaseholdSelfAssessedPenaltiesAndInterestPage, includesPenalty(taxCalculation))
          c <- setString(b, LeaseholdSelfAssessedPremiumPayableTaxPage, taxCalculation.taxDuePremium)
          d <- setString(c, LeaseholdSelfAssessedNpvTaxPage, taxCalculation.taxDueNPV)
        } yield d
    }

  private def includesPenalty(taxCalculation: TaxCalculation): Option[Boolean] =
    taxCalculation.includesPenalty.map(_.equalsIgnoreCase("yes"))

  private def setString(userAnswers: UserAnswers, page: QuestionPage[String], value: Option[String]): Try[UserAnswers] =
    value.fold[Try[UserAnswers]](Success(userAnswers))(userAnswers.set(page, _))

  private def setBoolean(userAnswers: UserAnswers, page: QuestionPage[Boolean], value: Option[Boolean]): Try[UserAnswers] =
    value.fold[Try[UserAnswers]](Success(userAnswers))(userAnswers.set(page, _))
}
