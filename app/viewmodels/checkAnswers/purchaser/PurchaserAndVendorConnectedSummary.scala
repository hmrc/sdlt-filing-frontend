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
import pages.purchaser.{NameOfPurchaserPage, PurchaserAndVendorConnectedPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object PurchaserAndVendorConnectedSummary  {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryListRow =
    answers.flatMap(_.get(PurchaserAndVendorConnectedPage)).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"site.$answer"))
          )
        )

        SummaryListRowViewModel(
          key     = messages("purchaser.purchaserAndVendorConnected.checkYourAnswersLabel",answers.flatMap(_.get(NameOfPurchaserPage)).map(_.name).getOrElse("")),
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", controllers.purchaser.routes.PurchaserAndVendorConnectedController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("purchaser.purchaserAndVendorConnected.change.hidden"))
          )
        )
    }.getOrElse {

      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="${controllers.purchaser.routes.PurchaserAndVendorConnectedController.onPageLoad(CheckMode).url}" class="govuk-link">${messages("purchaser.checkYourAnswers.purchaserAndVendorConnected.missing")}</a>""")
      )

      SummaryListRowViewModel(
        key = messages("purchaser.purchaserAndVendorConnected.checkYourAnswersLabel",answers.flatMap(_.get(NameOfPurchaserPage)).map(_.name).getOrElse("")),
        value = value
      )
    }
}
