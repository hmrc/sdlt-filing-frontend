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
import models.taxCalculation.FreeHoldSelfAssessedTotalAmountDue
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.all.SummaryListViewModel
import viewmodels.taxCalculation.{PenaltiesSummary, SdltDueSummary, TotalAmountDueSummary}

object CalculatedTotalDueHelper {
  
  def calculateFreeHoldSelfAssessedTotalAmountSummaryRow(userAnswers: UserAnswers, sdltTaxDue:Int):FreeHoldSelfAssessedTotalAmountDue =
    FreeHoldSelfAssessedTotalAmountDue(
      sdltDue = convertSdltDueToBigDecimal(sdltTaxDue),
      penalties = calculatePenalties(),
      total = calculateTotalAmountDue(convertSdltDueToBigDecimal(sdltTaxDue), calculatePenalties())
    )
  
  def getSummaryListRows(userAnswers: UserAnswers, sdltTaxDue:Int)(implicit messages:Messages):SummaryList = SummaryListViewModel(
    Seq(
      SdltDueSummary.row(convertSdltDueToBigDecimal(sdltTaxDue)),
      PenaltiesSummary.row(calculatePenalties()),
      TotalAmountDueSummary.row(calculateTotalAmountDue(convertSdltDueToBigDecimal(sdltTaxDue), calculatePenalties()))
    )
  )
  
  private def convertSdltDueToBigDecimal(sdltDue:Int):BigDecimal = BigDecimal(sdltDue)

  private def calculatePenalties(): BigDecimal = {
    123456
  }
  private def calculateTotalAmountDue(sdltTaxDue:BigDecimal, penalties:BigDecimal):BigDecimal = sdltTaxDue + penalties
  

}
