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

package models.taxCalculation

import models.UserAnswers
import models.taxCalculation.TaxCalculationFlow.*
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.freeholdSelfAssessed.*
import pages.taxCalculation.freeholdTaxCalculated.*
import pages.taxCalculation.leaseholdSelfAssessed.*
import pages.taxCalculation.leaseholdTaxCalculated.*
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.Future

case class UpdateTaxCalculationRequest(
                                        stornId: String,
                                        returnResourceRef: String,
                                        amountPaid: Option[String] = None,
                                        includesPenalty: Option[String] = None,
                                        taxDue: Option[String] = None,
                                        calcPenaltyDue: Option[String] = None,
                                        calcTaxDue: Option[String] = None,
                                        calcTaxRate1: Option[String] = None,
                                        calcTaxRate2: Option[String] = None,
                                        calcTotalTaxPenaltyDue: Option[String] = None,
                                        calcTotalNpvTax: Option[String] = None,
                                        calcTotalPremiumTax: Option[String] = None,
                                        taxDuePremium: Option[String] = None,
                                        taxDueNpv: Option[String] = None,
                                        honestyDeclaration: Option[String] = None
                                      )

object UpdateTaxCalculationRequest {
  implicit val format: OFormat[UpdateTaxCalculationRequest] = Json.format[UpdateTaxCalculationRequest]

  def from(userAnswers: UserAnswers, result: Option[TaxCalculationResult], penalty: BigDecimal): Future[UpdateTaxCalculationRequest] =
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        val leasehold = isLeasehold(userAnswers)
        Future.successful(UpdateTaxCalculationRequest(
          stornId                = userAnswers.storn,
          returnResourceRef      = fullReturn.returnResourceRef,
          amountPaid             = amountPaid(userAnswers),
          includesPenalty        = includesPenalty(userAnswers),
          taxDue                 = selfAssessedAmount(userAnswers).orElse(result.map(_.totalTax.toString)),
          calcPenaltyDue         = Some(penalty.toString),
          calcTaxDue             = result.map(_.totalTax.toString),
          calcTaxRate1           = calcRate(result, TaxTypes.premium),
          calcTaxRate2           = if (leasehold) calcRate(result, TaxTypes.rent) else None,
          calcTotalTaxPenaltyDue = Some(result.map(r => BigDecimal(r.totalTax) + penalty).getOrElse(penalty).toString),
          calcTotalNpvTax        = if (leasehold) calcTaxFor(result, TaxTypes.rent) else None,
          calcTotalPremiumTax    = if (leasehold) calcTaxFor(result, TaxTypes.premium) else None,
          taxDuePremium          = premiumTax(userAnswers).orElse(if (leasehold) calcTaxFor(result, TaxTypes.premium) else None),
          taxDueNpv              = npvTax(userAnswers).orElse(if (leasehold) calcTaxFor(result, TaxTypes.rent) else None),
          honestyDeclaration     = Some("yes")
        ))
      case None => Future.failed(new NoSuchElementException("Full return not found"))
    }

  private def isLeasehold(userAnswers: UserAnswers): Boolean =
    userAnswers.get(TaxCalculationFlowPage).exists(flow => flow == LeaseholdTaxCalculated || flow == LeaseholdSelfAssessed)

  private def calcRate(result: Option[TaxCalculationResult], taxType: TaxTypes.Value): Option[String] =
    result.flatMap(_.taxCalcs.find(_.taxType == taxType)).flatMap(calc => calc.rate.map(formatRate(_, calc.rateFraction)))

  private def formatRate(rate: Int, fraction: Option[Int]): String =
    fraction.filter(_ != 0).fold(s"$rate%")(decimal => s"$rate.$decimal%")

  private def calcTaxFor(result: Option[TaxCalculationResult], taxType: TaxTypes.Value): Option[String] =
    result.flatMap(_.taxCalcs.find(_.taxType == taxType).map(_.taxDue.toString))

  private def amountPaid(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(TaxCalculationFlowPage).flatMap {
      case FreeholdTaxCalculated  => userAnswers.get(FreeholdTaxCalculatedTotalAmountDuePage)
      case FreeholdSelfAssessed   => userAnswers.get(FreeholdSelfAssessedTotalAmountDuePage)
      case LeaseholdTaxCalculated => userAnswers.get(LeaseholdTaxCalculatedTotalAmountDuePage)
      case LeaseholdSelfAssessed  => userAnswers.get(LeaseholdSelfAssessedTotalAmountDuePage)
    }

  private def includesPenalty(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(TaxCalculationFlowPage).flatMap {
      case FreeholdTaxCalculated  => userAnswers.get(FreeholdTaxCalculatedPenaltiesAndInterestPage)
      case FreeholdSelfAssessed   => userAnswers.get(FreeholdSelfAssessedPenaltiesAndInterestPage)
      case LeaseholdTaxCalculated => userAnswers.get(LeaseholdTaxCalculatedPenaltiesAndInterestPage)
      case LeaseholdSelfAssessed  => userAnswers.get(LeaseholdSelfAssessedPenaltiesAndInterestPage)
    }.map(includes => if (includes) "yes" else "no")

  private def selfAssessedAmount(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(TaxCalculationFlowPage).flatMap {
      case FreeholdTaxCalculated  => userAnswers.get(FreeholdTaxCalculatedSelfAssessedAmountPage)
      case FreeholdSelfAssessed   => userAnswers.get(FreeholdSelfAssessedAmountPage)
      case LeaseholdTaxCalculated => userAnswers.get(LeaseholdTaxCalculatedSelfAssessedAmountPage)
      case LeaseholdSelfAssessed  => None
    }

  private def premiumTax(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(TaxCalculationFlowPage).flatMap {
      case LeaseholdSelfAssessed => userAnswers.get(LeaseholdSelfAssessedPremiumPayableTaxPage)
      case _                     => None
    }

  private def npvTax(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(TaxCalculationFlowPage).flatMap {
      case LeaseholdSelfAssessed => userAnswers.get(LeaseholdSelfAssessedNpvTaxPage)
      case _                     => None
    }
}

case class UpdateTaxCalculationReturn(
                                       updated: Boolean
                                     )

object UpdateTaxCalculationReturn {
  implicit val format: OFormat[UpdateTaxCalculationReturn] = Json.format[UpdateTaxCalculationReturn]
}
