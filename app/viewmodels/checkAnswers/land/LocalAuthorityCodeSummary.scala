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

package viewmodels.checkAnswers.land

import models.{CheckMode, UserAnswers}
import pages.land.LocalAuthorityCodePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object LocalAuthorityCodeSummary {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryListRow =
    answers.flatMap(_.get(LocalAuthorityCodePage)).map {
      answer =>

        SummaryListRowViewModel(
          key = "land.localAuthorityCode.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.land.routes.LocalAuthorityCodeController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("land.localAuthorityCode.change.hidden"))
          )
        )
    }.getOrElse {
      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="${controllers.land.routes.LocalAuthorityCodeController.onPageLoad(CheckMode).url}" class="govuk-link">${messages("land.checkYourAnswers.localAuthorityCode.missing")}</a>""")
      )

      SummaryListRowViewModel(
        key = "land.localAuthorityCode.checkYourAnswersLabel",
        value = value
      )

    }
}
