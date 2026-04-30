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
import pages.transaction.Cap1OrNsbcPage
import play.api.i18n.Messages
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object Cap1OrNsbcSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryRowResult = {

    val changeRoute = controllers.transaction.routes.Cap1OrNsbcController.onPageLoad(CheckMode)
    val label = messages("transaction.cap1OrNsbc.checkYourAnswersLabel")
      
    answers.get(Cap1OrNsbcPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        Row(
          SummaryListRowViewModel(
            key     = label,
            value   = ValueViewModel(value),
            actions = Seq(
              ActionItemViewModel("site.change", changeRoute.url)
                .withVisuallyHiddenText(messages("transaction.cap1OrNsbc.change.hidden"))
            )
          )
        )
    }.getOrElse {
      Missing(changeRoute)
    }
  }
}
