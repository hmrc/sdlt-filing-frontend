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

package viewmodels.checkAnswers.land

import models.UserAnswers
import models.address.Address
import models.address.Address.toHtml
import pages.land.LandAddressPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object LandAddressSummary {
  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    val changeRoute = controllers.land.routes.LandAddressController.redirectToAddressLookupLand(Some("change")).url
    val label = messages("land.address.checkYourAnswersLabel")
    answers.get(LandAddressPage).map { answer =>

      SummaryListRowViewModel(
        key = label,
        value = ValueViewModel(HtmlContent(toHtml(answer))),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            changeRoute
          ).withVisuallyHiddenText(messages("land.address.change.hidden"))
        )
      )
    }.getOrElse {
      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="$changeRoute" class="govuk-link">${messages("land.checkYourAnswers.address.missing")}</a>""")
      )
      SummaryListRowViewModel(
        key = label,
        value = value
      )
    }
  }
}
