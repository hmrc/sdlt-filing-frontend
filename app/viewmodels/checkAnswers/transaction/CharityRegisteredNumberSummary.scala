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

import models.{CheckMode, UserAnswers}
import pages.transaction.{AddRegisteredCharityNumberPage, CharityRegisteredNumberPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object CharityRegisteredNumberSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryRowResult] = {
    val changeRoute = controllers.transaction.routes.CharityRegisteredNumberController.onPageLoad(CheckMode)
    val label = messages("transaction.charityRegisteredNumber.checkYourAnswersLabel")

    (answers.get(CharityRegisteredNumberPage), answers.get(AddRegisteredCharityNumberPage)) match {
      case (Some(number), _) =>
        Some(Row(
          SummaryListRowViewModel(
            key     = label,
            value   = ValueViewModel(HtmlFormat.escape(number).toString),
            actions = Seq(
              ActionItemViewModel("site.change", changeRoute.url)
                .withVisuallyHiddenText(messages("transaction.charityRegisteredNumber.change.hidden"))
            )
          )
        ))
      case (None, Some(true)) =>
        Some(Missing(changeRoute))
      case _ =>
        None
    }
  }
}
