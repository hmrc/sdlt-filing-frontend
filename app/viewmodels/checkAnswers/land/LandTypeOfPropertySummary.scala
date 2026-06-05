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
import pages.land.LandTypeOfPropertyPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object LandTypeOfPropertySummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryRowResult =
    val changeRoute = controllers.land.routes.LandTypeOfPropertyController.onPageLoad(CheckMode)
    val label = messages("land.landTypeOfProperty.checkYourAnswersLabel")
    
    answers.get(LandTypeOfPropertyPage).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"land.landTypeOfProperty.$answer"))
          )
        )

        Row(
          SummaryListRowViewModel(
            key     = label,
            value   = value,
            actions = Seq(
              ActionItemViewModel("site.change", changeRoute.url)
                .withVisuallyHiddenText(messages("land.landTypeOfProperty.change.hidden"))
            )
          )
        )
    }.getOrElse {
      Missing(changeRoute)
    }
}
