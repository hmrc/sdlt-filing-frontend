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

package viewmodels.checkAnswers.vendor


import models.{CheckMode, NormalMode, UserAnswers}
import pages.vendor.WhoIsTheVendorPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object VendorTypeSummary {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryListRow =
    answers.flatMap(_.get(WhoIsTheVendorPage)).map { answer =>

      val answerText = answer.toString match {
        case "Individual" => messages("vendor.checkYourAnswers.Individual")
        case _  => messages("vendor.checkYourAnswers.Company")
      }

      val value = ValueViewModel(
        HtmlContent(answerText)
      )

      SummaryListRowViewModel(
        key = "vendor.checkYourAnswers.vendorType.label",
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", controllers.vendor.routes.WhoIsTheVendorController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("vendor.checkYourAnswers.hidden"))
        )
      )
    }.getOrElse {

      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="${controllers.vendor.routes.WhoIsTheVendorController.onPageLoad(CheckMode).url}" class="govuk-link">${messages("vendor.checkYourAnswers.vendorTypeMissing")}</a>""")
      )

      SummaryListRowViewModel(
        key = "vendor.checkYourAnswers.vendorType.label",
        value = value
      )
    }
}
