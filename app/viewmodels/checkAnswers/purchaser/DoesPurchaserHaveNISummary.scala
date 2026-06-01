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
import pages.purchaser.{DoesPurchaserHaveNIPage, NameOfPurchaserPage}
import play.api.i18n.Messages
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object DoesPurchaserHaveNISummary  {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryRowResult = {
    val changeRoute = controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(CheckMode)
    answers.flatMap(_.get(DoesPurchaserHaveNIPage)).map {
      answer =>

        val value = ValueViewModel(
          if (answer) "site.yes" else "site.no"
        )

        Row(
          SummaryListRowViewModel(
            key     = messages("purchaser.doesPurchaserHaveNI.checkYourAnswersLabel", answers.flatMap(_.get(NameOfPurchaserPage)).map(_.fullName).getOrElse("")),
            value   = value,
            actions = Seq(
              ActionItemViewModel("site.change", changeRoute.url)
                .withVisuallyHiddenText(messages("purchaser.doesPurchaserHaveNI.change.hidden"))
            )
          )
        )
    }.getOrElse {
      Missing(changeRoute)
    }
  }
}
