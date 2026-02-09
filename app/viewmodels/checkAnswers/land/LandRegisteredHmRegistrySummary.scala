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
import pages.land.LandRegisteredHmRegistryPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object LandRegisteredHmRegistrySummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow =
    val changeRoute = controllers.land.routes.LandRegisteredHmRegistryController.onPageLoad(CheckMode).url
    val label = messages("land.landRegisteredHmRegistry.checkYourAnswersLabel")
    
    answers.get(LandRegisteredHmRegistryPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = label,
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.land.routes.LandRegisteredHmRegistryController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("land.landRegisteredHmRegistry.change.hidden"))
          )
        )
    }.getOrElse {
      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="$changeRoute" class="govuk-link">${messages("land.landRegisteredHmRegistry.missing")}</a>""")
      )
      SummaryListRowViewModel(
        key = label,
        value = value
      )
    }
}
