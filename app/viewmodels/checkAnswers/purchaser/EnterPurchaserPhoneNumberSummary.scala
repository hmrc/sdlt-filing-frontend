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
import pages.purchaser.{EnterPurchaserPhoneNumberPage, NameOfPurchaserPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object EnterPurchaserPhoneNumberSummary  {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryListRow =
    answers.flatMap(_.get(EnterPurchaserPhoneNumberPage)).map {
      answer =>

        SummaryListRowViewModel(
          key     = messages("purchaser.enterPhoneNumber.checkYourAnswersLabel", answers.flatMap(_.get(NameOfPurchaserPage)).map(_.fullName).getOrElse("")),
          value   = ValueViewModel(HtmlContent(HtmlFormat.escape(answer).toString)),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.purchaser.routes.EnterPurchaserPhoneNumberController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("purchaser.enterPhoneNumber.change.hidden"))
          )
        )
    }.getOrElse {

      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="${controllers.purchaser.routes.EnterPurchaserPhoneNumberController.onPageLoad(CheckMode).url}" class="govuk-link">${messages("purchaser.checkYourAnswers.enterPurchaserPhoneNumber.missing")}</a>""")
      )

      SummaryListRowViewModel(
        key = "purchaser.enterPhoneNumber.checkYourAnswersLabel",
        value = value
      )
    }
}
