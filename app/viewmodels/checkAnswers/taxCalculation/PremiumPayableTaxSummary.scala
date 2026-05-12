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
import pages.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedPremiumPayableTaxPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object PremiumPayableTaxSummary {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryRowResult = {

    val route = controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedPremiumPayableTaxController.onPageLoad(CheckMode)
    val label = messages("taxCalculation.leaseholdSelfAssessed.premiumPayable.checkYourAnswers")

    answers.flatMap(_.get(LeaseholdSelfAssessedPremiumPayableTaxPage)).map {
      answer =>
        val value = ValueViewModel(HtmlContent(s"£$answer"))

        Row(
          SummaryListRowViewModel(
            key = label,
            value = value,
            actions = Seq(
              ActionItemViewModel("site.change", route.url)
                .withVisuallyHiddenText(messages("taxCalculation.leaseholdSelfAssessed.premiumPayable.change.hidden"))
            )
          )
        )
    }.getOrElse {
      Missing(route)
    }
  }
}

