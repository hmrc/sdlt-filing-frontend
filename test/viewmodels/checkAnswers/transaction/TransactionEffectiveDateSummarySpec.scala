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

package viewmodels.checkAnswers.transaction

import base.SpecBase
import models.CheckMode
import pages.transaction.TransactionEffectiveDatePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import java.time.LocalDate

class TransactionEffectiveDateSummarySpec extends SpecBase{

  "TransactionEffectiveDateSummary" - {
    "must return a summary list row with the transaction effective date" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(TransactionEffectiveDatePage, LocalDate.of(2016, 10, 26)).success.value

        val result = TransactionEffectiveDateSummary.row(Some(userAnswers))

        result.key.content.asHtml.toString() mustEqual msgs("transaction.transactionEffectiveDate.checkYourAnswersLabel")

        val htmlContent = result.value.content.asInstanceOf[Text].asHtml.toString()
        htmlContent mustEqual msgs("26 October 2016")

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.transactionEffectiveDate.change.hidden")
      }
    }

    "must return a summary list row with a link to enter transaction effective date when userAnswers is empty" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers

        val result = TransactionEffectiveDateSummary.row(Some(userAnswers))

        result.key.content.asHtml.toString() mustEqual msgs("transaction.transactionEffectiveDate.checkYourAnswersLabel")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()

        htmlContent must include("govuk-link")
        htmlContent must include(controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(CheckMode).url)
        htmlContent must include(msgs("transaction.transactionEffectiveDate.missing"))
        result.actions mustBe None
      }
    }

  }

}
