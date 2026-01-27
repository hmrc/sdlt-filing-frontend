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

import models.purchaser.PurchaserConfirmIdentity
import models.{CheckMode, UserAnswers}
import pages.purchaser.{PurchaserConfirmIdentityPage, PurchaserUTRPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object PurchaserConfirmIdentitySummary {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryListRow = {

    answers.flatMap(_.get(PurchaserConfirmIdentityPage)) match {
      case Some(answer) =>
        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"purchaser.confirmIdentity.$answer"))
          )
        )

        SummaryListRowViewModel(
          key = "purchaser.confirmIdentity.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("purchaser.confirmIdentity.change.hidden"))
          )
        )

      case None =>
        val doesUtrExist = answers.flatMap(_.get(PurchaserUTRPage)).isDefined

        if(doesUtrExist) {
          val value = ValueViewModel(
            HtmlContent(
              HtmlFormat.escape(messages(s"purchaser.corporationTaxUTR.checkYourAnswersLabel"))
            )
          )

          SummaryListRowViewModel(
            key = "purchaser.confirmIdentity.checkYourAnswersLabel",
            value = value,
            actions = Seq(
              ActionItemViewModel("site.change", controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(CheckMode).url)
                .withVisuallyHiddenText(messages("purchaser.confirmIdentity.change.hidden"))
            )
          )
        } else {
          val value = ValueViewModel(
            HtmlContent(
              s"""<a href="${controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(CheckMode).url}" class="govuk-link">${messages("purchaser.checkYourAnswers.confirmIdentity.missing")}</a>""")
          )

          SummaryListRowViewModel(
            key = "purchaser.confirmIdentity.checkYourAnswersLabel",
            value = value
          )
        }
      }
    }
  }