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

package viewmodels.checkAnswers.lease

import models.{CheckMode, UserAnswers}
import pages.lease.LeaseEnterTotalPremiumPayablePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object LeaseEnterTotalPremiumPayableSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryRowResult = {
    val changeRoute = controllers.lease.routes.LeaseEnterTotalPremiumPayableController.onPageLoad(CheckMode)
    answers.get(LeaseEnterTotalPremiumPayablePage).map {
      answer =>

        Row(
          SummaryListRowViewModel(
            key     = "lease.enterTotalPremiumPayable.checkYourAnswersLabel",
            value   = ValueViewModel(HtmlFormat.escape(s"£$answer").toString),
            actions = Seq(
              ActionItemViewModel("site.change", changeRoute.url)
                .withVisuallyHiddenText(messages("lease.enterTotalPremiumPayable.change.hidden"))
            )
          )
        )
    }.getOrElse(Missing(changeRoute))
  }
}
