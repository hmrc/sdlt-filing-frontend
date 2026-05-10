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

package utils.taxCalculation

import models.UserAnswers
import models.taxCalculation.*
import pages.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedTotalAmountDueSummaryPage
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.all.SummaryListViewModel
import viewmodels.taxCalculation.{PenaltiesSummary, SdltDueSummary, TotalAmountDueSummary}

import scala.util.Try
import java.time.LocalDate
import scala.math

object CalculatedTotalDueHelper extends PenaltyCalculator with Logging {

  def createFreeHoldSelfAssessedTotalAmountDue(sdltTaxDue: Int, effectiveDate: LocalDate): FreeHoldSelfAssessedTotalAmountDue = {
    FreeHoldSelfAssessedTotalAmountDue(
      convertSdltDueToBigDecimal(sdltTaxDue),
      getPenalty(effectiveDate),
      calculateTotalAmountDue(convertSdltDueToBigDecimal(sdltTaxDue), getPenalty(effectiveDate))
    )
  }
  
  def totalAmountDue(value:String):TotalAmountDue = {
    TotalAmountDue(value)
  }

  def getEffectiveDate(userAnswers: UserAnswers):Either[EffectiveDateOfTransactionError, LocalDate] = {
    userAnswers.fullReturn
      .flatMap(_.transaction)
      .flatMap(_.effectiveDate) match {
      case Some(effectiveDate) =>
        parseIntoLocalDate(effectiveDate) match {
          case Some(date) =>
            logger.error("[CalculatedTotalDueHelper][getEffectiveDate] effective date of transaction retrieved successfully")
            Right(date)
          case None =>
            logger.error("[CalculatedTotalDueHelper][getEffectiveDate] Cannot parse effective date of transaction into valid format")
            Left(InvalidEffectiveDateOfTransactionError)
        }
      case None =>
        logger.error("[CalculatedTotalDueHelper][getEffectiveDate] Cannot retrieve effective date of transaction")
        Left(MissingEffectiveDateOfTransactionError)

    }
  }

  def getSummaryListRows(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryList] =
    userAnswers.get(FreeholdSelfAssessedTotalAmountDueSummaryPage).map { freeholdSelfAssessedTotalAmountDuePage =>
      SummaryListViewModel(
        Seq(
          SdltDueSummary.row(freeholdSelfAssessedTotalAmountDuePage.sdltDue),
          PenaltiesSummary.row(freeholdSelfAssessedTotalAmountDuePage.penalties),
          TotalAmountDueSummary.row(freeholdSelfAssessedTotalAmountDuePage.total)
        )
      )
    }

  private def parseIntoLocalDate(effectiveDate: String): Option[LocalDate] = {
    Try {
      LocalDate.parse(effectiveDate)
    }.toOption
  }

  private def convertSdltDueToBigDecimal(sdltDue: Int): BigDecimal = BigDecimal(sdltDue)

  private def getPenalty(effectiveDate: LocalDate): BigDecimal = calculatePenalties(effectiveDate)

  private def calculateTotalAmountDue(sdltTaxDue: BigDecimal, penalties: BigDecimal): BigDecimal = sdltTaxDue + penalties


}
