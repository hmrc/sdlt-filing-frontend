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

import models.land.LandTypeOfProperty
import models.{CheckMode, UserAnswers}
import pages.land.{AgriculturalOrDevelopmentalLandPage, LandTypeOfPropertyPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AgriculturalOrDevelopmentalLandSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    val changeRoute = controllers.land.routes.AgriculturalOrDevelopmentalLandController.onPageLoad(CheckMode).url
    val label = messages("land.agriculturalOrDevelopmental.checkYourAnswersLabel")
    (answers.get(AgriculturalOrDevelopmentalLandPage), answers.get(LandTypeOfPropertyPage)) match {
      case (Some(agriculturalOrDevelopmental), _) =>

        val value = if (agriculturalOrDevelopmental) "site.yes" else "site.no"

        Some(SummaryListRowViewModel(
          key = label,
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute)
              .withVisuallyHiddenText(messages("land.agriculturalOrDevelopmental.change.hidden"))
          )
        ))
      case (None, Some(LandTypeOfProperty.Mixed | LandTypeOfProperty.NonResidential)) =>
        val value = ValueViewModel(
          HtmlContent(
            s"""<a href="$changeRoute" class="govuk-link">${messages("land.agriculturalOrDevelopmental.missing")}</a>""")
        )
        Some(SummaryListRowViewModel(
          key = label,
          value = value
        ))
      case _ => None
    }
}
