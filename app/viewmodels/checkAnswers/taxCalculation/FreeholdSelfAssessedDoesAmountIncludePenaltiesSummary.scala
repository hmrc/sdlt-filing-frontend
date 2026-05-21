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
import pages.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedPenaltiesAndInterestPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*


object FreeholdSelfAssessedDoesAmountIncludePenaltiesSummary {

  def row(answers: Option[UserAnswers])
         (implicit messages: Messages): SummaryRowResult = {

    val label = messages("taxCalculation.penaltiesAndInterest.checkYourAnswersLabel")
    val changeRoute =
      controllers.taxCalculation
              .freeholdSelfAssessed.routes
              .FreeholdSelfAssessedPenaltiesAndInterestController.onPageLoad(CheckMode)

    answers.flatMap(_.get(FreeholdSelfAssessedPenaltiesAndInterestPage)).map { yesNoAnswer =>
      val yesNoValue = ValueViewModel(HtmlContent({
        if (yesNoAnswer) {
          messages("site.yes")
        } else {
          messages("site.no")
        }
      }))

      Row(
        SummaryListRowViewModel(
          key = label,
          value = yesNoValue,
          actions = Seq(ActionItemViewModel("site.change", changeRoute.url))
        )
      )
    }.getOrElse(Missing(changeRoute))

  }
}