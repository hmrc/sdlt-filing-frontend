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

import models.taxCalculation.{FreeHoldSelfAssessedTotalAmountDue, TotalAmountDue}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.all.SummaryListViewModel
import viewmodels.taxCalculation.{PenaltiesSummary, SdltDueSummary, TotalAmountDueSummary}

import java.time.LocalDate
import scala.math

object CalculatedTotalDueHelper extends PenaltyCalculator {

  def createFreeHoldSelfAssessedTotalAmountDue(sdltTaxDue: Int, effectiveDate: LocalDate): FreeHoldSelfAssessedTotalAmountDue = {
    FreeHoldSelfAssessedTotalAmountDue(
      convertSdltDueToBigDecimal(sdltTaxDue),
      getPenalty(effectiveDate),
      calculateTotalAmountDue(convertSdltDueToBigDecimal(sdltTaxDue), getPenalty(effectiveDate))
    )
  }
  
  def createTotalAmountDue(value:String):TotalAmountDue = {
    TotalAmountDue(value)
  }

  def getSummaryListRows(sdltTaxDue: Int, effectiveDate: LocalDate)(implicit messages: Messages): SummaryList = SummaryListViewModel(
    Seq(
      SdltDueSummary.row(convertSdltDueToBigDecimal(sdltTaxDue)),
      PenaltiesSummary.row(getPenalty(effectiveDate)),
      TotalAmountDueSummary.row(calculateTotalAmountDue(convertSdltDueToBigDecimal(sdltTaxDue), getPenalty(effectiveDate))))
  )

  private def getPenalty(effectiveDate: LocalDate): BigDecimal = calculatePenalties(effectiveDate)

  private def convertSdltDueToBigDecimal(sdltDue: Int): BigDecimal = BigDecimal(sdltDue)

  private def calculateTotalAmountDue(sdltTaxDue: BigDecimal, penalties: BigDecimal): BigDecimal = sdltTaxDue + penalties


}
