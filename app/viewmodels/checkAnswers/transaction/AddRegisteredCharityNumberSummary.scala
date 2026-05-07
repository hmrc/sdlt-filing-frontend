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

package viewmodels.checkAnswers.transaction

import models.transaction.ReasonForRelief
import models.{CheckMode, UserAnswers}
import pages.transaction.{AddRegisteredCharityNumberPage, ReasonForReliefPage}
import play.api.i18n.Messages
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AddRegisteredCharityNumberSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryRowResult] = {
    val changeRoute = controllers.transaction.routes.AddRegisteredCharityNumberController.onPageLoad(CheckMode)
    val label = messages("transaction.addRegisteredCharityNumber.checkYourAnswersLabel")

    (answers.get(AddRegisteredCharityNumberPage), answers.get(ReasonForReliefPage)) match {
      case (Some(answer), _) =>
        val value = if (answer) "site.yes" else "site.no"

        Some(Row(
          SummaryListRowViewModel(
            key   = label,
            value = ValueViewModel(value),
            actions = Seq(
              ActionItemViewModel("site.change", changeRoute.url)
                .withVisuallyHiddenText(messages("transaction.addRegisteredCharityNumber.change.hidden"))
            )
          )
        ))
      case (None, Some(ReasonForRelief.CharitiesRelief)) =>
        Some(Missing(changeRoute))
      case _ =>
        None
    }
  }
}
