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

import models.{CheckMode, UserAnswers}
import pages.taxCalculation.freeholdTaxCalculated.FreeholdTaxCalculatedTotalAmountDuePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

object FreeholdTaxCalculatedTotalAmountDueSummary {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryRowResult = {
    val changeRoute = controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdTaxCalculatedTotalAmountDueController.onPageLoad(CheckMode)
    val label = messages("taxCalculation.totalAmountDue.checkYourAnswersLabel")

    answers.flatMap(_.get(FreeholdTaxCalculatedTotalAmountDuePage)).map { answer =>

      val value = ValueViewModel(
        HtmlContent(s"£$answer")
      )

      Row(
        SummaryListRowViewModel(
          key = label,
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute.url)
              .withVisuallyHiddenText(messages("taxCalculation.totalAmountDue.change.hidden"))
          )
        )
      )
    }.getOrElse {
      Missing(changeRoute)
    }
  }
}
