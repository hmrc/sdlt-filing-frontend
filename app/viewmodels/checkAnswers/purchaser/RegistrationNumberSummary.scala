/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.purchaser

import models.{CheckMode, UserAnswers}
import pages.purchaser.RegistrationNumberPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object RegistrationNumberSummary  {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryRowResult = {
    val changeRoute = controllers.purchaser.routes.RegistrationNumberController.onPageLoad(CheckMode)
    answers.flatMap(_.get(RegistrationNumberPage)).map {
      answer =>

        Row(
          SummaryListRowViewModel(
            key     = "purchaser.registrationNumber.checkYourAnswersLabel",
            value   = ValueViewModel(HtmlFormat.escape(answer).toString),
            actions = Seq(
              ActionItemViewModel("site.change", changeRoute.url)
                .withVisuallyHiddenText(messages("purchaser.registrationNumber.change.hidden"))
            )
          )
        )
    }.getOrElse {
      Missing(changeRoute)
    }
  }
}
