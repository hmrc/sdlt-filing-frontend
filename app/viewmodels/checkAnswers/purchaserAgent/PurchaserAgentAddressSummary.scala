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

package viewmodels.checkAnswers.purchaserAgent

import models.UserAnswers
import models.address.Address.toHtml
import models.address.Address
import pages.purchaserAgent.PurchaserAgentAddressPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object PurchaserAgentAddressSummary {
  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    val changeRoute = controllers.purchaserAgent.routes.PurchaserAgentAddressController.redirectToAddressLookupPurchaserAgent(Some("change")).url
    val label = messages("purchaserAgent.address.checkYourAnswersLabel")
    answers.get(PurchaserAgentAddressPage).map { answer =>

      SummaryListRowViewModel(
        key = label,
        value = ValueViewModel(HtmlContent(toHtml(answer))),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            changeRoute
          ).withVisuallyHiddenText(messages("purchaserAgent.address.change.hidden"))
        )
      )
    }.getOrElse {
      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="$changeRoute" class="govuk-link">${messages("returnAgent.checkYourAnswers.address.missing")}</a>""")
      )
      SummaryListRowViewModel(
        key = label,
        value = value
      )
    }
  }
}
