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

import models.{CheckMode, UserAnswers}
import pages.vendor.{VendorOrCompanyNamePage, WhoIsTheVendorPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Row, Missing}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object VendorOrCompanyNameSummary {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryRowResult = {
    val changeRoute = controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode)

    val vendorOrBusiness: String = answers.flatMap(_.get(WhoIsTheVendorPage)) match {
      case Some(value) => if (value.toString == "Individual") "Individual" else "Company"
      case _ => ""
    }

    answers.flatMap(_.get(VendorOrCompanyNamePage)).map {
      answer =>
        val name = if vendorOrBusiness == "Individual" then answer.fullName else answer.name
        Row(
          SummaryListRowViewModel(
            key = s"vendor.checkYourAnswers.vendorName.label",
            value = ValueViewModel(HtmlContent(HtmlFormat.escape(name).toString)),
            actions = Seq(
              ActionItemViewModel("site.change", changeRoute.url)
                .withVisuallyHiddenText(messages("vendor.checkYourAnswers.vendorName.hidden"))
            )
          )
        )
    }.getOrElse {
      Missing(changeRoute)
    }
  }
}
