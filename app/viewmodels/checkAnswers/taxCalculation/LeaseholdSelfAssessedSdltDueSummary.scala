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

package viewmodels.checkAnswers.taxCalculation

import config.CurrencyFormatter
import models.{CheckMode, UserAnswers}
import pages.taxCalculation.leaseholdSelfAssessed.{LeaseholdSelfAssessedNpvTaxPage, LeaseholdSelfAssessedPremiumPayableTaxPage}
import play.api.i18n.Messages

import scala.util.Try
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object LeaseholdSelfAssessedSdltDueSummary extends CurrencyFormatter {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryRowResult = {
    val changeRoute = controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedPremiumPayableTaxController.onPageLoad(CheckMode)
    val label = messages("taxCalculation.selfAssessedSdltDue.checkYourAnswersLabel")

    (answers.get(LeaseholdSelfAssessedPremiumPayableTaxPage), answers.get(LeaseholdSelfAssessedNpvTaxPage)) match {
      case (Some(premiumPayable), Some(nvp)) =>
        Try(BigDecimal(premiumPayable) + BigDecimal(nvp)).toOption.map { sdltDue =>
          Row(
            SummaryListRowViewModel(
              key = label,
              value = ValueViewModel(HtmlContent(s"${sdltDue.toCurrency}"))
            )
          )
        }.getOrElse(Missing(changeRoute))
      case _ =>
        Missing(changeRoute)
    }
  }
}
