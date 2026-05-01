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

package viewmodels.checkAnswers.ukResidency

import models.{CheckMode, UserAnswers}
import pages.ukResidency.{CrownEmploymentReliefPage, NonUkResidentPurchaserPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object CrownEmploymentReliefSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {

    val isNonUkResident = answers.get(NonUkResidentPurchaserPage).getOrElse(false)

    if (!isNonUkResident) None
    else Some(
      answers.get(CrownEmploymentReliefPage).map {
        answer =>

          val value = if (answer) "site.yes" else "site.no"

          SummaryListRowViewModel(
            key     = messages("ukResidency.crownEmploymentRelief.checkYourAnswersLabel"),
            value   = ValueViewModel(value),
            actions = Seq(
              ActionItemViewModel("site.change", controllers.ukResidency.routes.CrownEmploymentReliefController.onPageLoad(CheckMode).url)
                .withVisuallyHiddenText(messages("ukResidency.crownEmploymentRelief.change.hidden"))
            )
          )
      }.getOrElse {

        val value = ValueViewModel(
          HtmlContent(
            s"""<a href="${controllers.ukResidency.routes.CrownEmploymentReliefController.onPageLoad(CheckMode).url}" class="govuk-link">${messages("ukResidency.crownEmploymentRelief.missing")}</a>""")
        )

        SummaryListRowViewModel(
          key   = messages("ukResidency.crownEmploymentRelief.checkYourAnswersLabel"),
          value = value
        )
      }
    )
  }
}