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

package viewmodels.checkAnswers.preliminary

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.preliminary.{PurchaserIsIndividualPage, PurchaserOrCompanyNamePage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object PurchaserOrCompanyNameSummary  {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryListRow = {
    
    val typeOfPurchaser = answers.flatMap(_.get(PurchaserIsIndividualPage)) match {
      case Some(value) => if(value.toString == "Individual") "purchaser" else "company"
      case _ => "default"
    }

    answers.flatMap(_.get(PurchaserOrCompanyNamePage)).map { answer =>

      val displayName =
        Seq(answer.forename1, answer.forename2, Some(answer.name))
          .flatten
          .mkString(" ")

      SummaryListRowViewModel(
        key = s"purchaserOrCompanyName.checkYourAnswersLabel.$typeOfPurchaser",
        value = ValueViewModel(HtmlFormat.escape(displayName).toString),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.preliminary.routes.PurchaserOrCompanyNameController.onPageLoad(CheckMode).url
          ).withVisuallyHiddenText(messages("purchaserOrCompanyName.change.hidden"))
        )
      )
    }.getOrElse {

      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="${
            controllers.preliminary.routes.PurchaserOrCompanyNameController.onPageLoad(CheckMode).url
          }" class="govuk-link">${
            messages("purchaserOrCompanyName.link.message")
          }</a>"""
        )
      )

      SummaryListRowViewModel(
        key = s"purchaserOrCompanyName.checkYourAnswersLabel.$typeOfPurchaser",
        value = value
      )
    }
  }
}