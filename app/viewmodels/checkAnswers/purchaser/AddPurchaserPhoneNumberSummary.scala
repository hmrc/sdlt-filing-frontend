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
import pages.purchaser.{AddPurchaserPhoneNumberPage, NameOfPurchaserPage}
import play.api.i18n.Messages
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AddPurchaserPhoneNumberSummary {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryRowResult = {
      val changeRoute = controllers.purchaser.routes.AddPurchaserPhoneNumberController.onPageLoad(CheckMode)
      answers.flatMap(_.get(AddPurchaserPhoneNumberPage)).map { answer =>
        val value = if (answer) "site.yes" else "site.no"

        Row(
          SummaryListRowViewModel(
            key = messages("purchaser.addPurchaserPhoneNumber.checkYourAnswersLabel",answers.flatMap(_.get(NameOfPurchaserPage)).map(_.fullName).getOrElse("")),
            value = ValueViewModel(value),
            actions = Seq(
              ActionItemViewModel("site.change", changeRoute.url)
                .withVisuallyHiddenText(messages("purchaser.addPurchaserPhoneNumber.change.hidden",answers.flatMap(_.get(NameOfPurchaserPage)).map(_.fullName).getOrElse("")))
            )
          )
        )
    }.getOrElse {
        Missing(changeRoute)
    }
  }
}
