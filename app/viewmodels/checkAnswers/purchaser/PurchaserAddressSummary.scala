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

package viewmodels.checkAnswers.purchaser

import models.UserAnswers
import models.address.Country
import pages.purchaser.PurchaserAddressPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object PurchaserAddressSummary {
  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryRowResult = {
    val changeRoute = controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser(Some("change"))
    answers.flatMap(_.get(PurchaserAddressPage)).map { answer =>

      val listOfAddressDetails = List(
        answer.line1,
        answer.line2,
        answer.line3,
        answer.line4,
        answer.line5,
        answer.postcode,
        answer.country
      )

      val list = listOfAddressDetails.collect {
        case Some(Country(Some(code), Some(name))) => name
        case Some(detail) => detail
        case detail => detail
      }.filter(x => x != None)

      val purchaserAddressString = list.mkString(", ")

      val value = ValueViewModel(
        HtmlContent(HtmlFormat.escape(purchaserAddressString))
      )

      Row(
        SummaryListRowViewModel(
          key = "purchaser.checkYourAnswers.purchaserAddress.label",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute.url)
              .withVisuallyHiddenText(messages("purchaser.checkYourAnswers.purchaserAddress.hidden"))
          )
        )
      )
    }.getOrElse {
      Missing(changeRoute)
    }
  }
}
