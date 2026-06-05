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
import pages.land.{AreaOfLandPage, LandSelectMeasurementUnitPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AreaOfLandSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryRowResult =
    val changeRoute = controllers.land.routes.LandSelectMeasurementUnitController.onPageLoad(CheckMode)
    val label = messages("land.areaOfLand.checkYourAnswersLabel")
    
    (answers.get(AreaOfLandPage), answers.get(LandSelectMeasurementUnitPage)) match {
      case (Some(areaOfLand), Some(unitType)) =>
        Row(SummaryListRowViewModel(
          key = label,
          value = ValueViewModel(
            HtmlContent(HtmlFormat.escape(s"$areaOfLand ${messages(s"land.areaOfLand.$unitType.suffix")}").toString)
          ),
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute.url)
              .withVisuallyHiddenText(messages("land.areaOfLand.change.hidden"))
          )
        ))
      case _ =>
        Missing(changeRoute)
    }
}
