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
import models.transaction.TransactionUseOfLandOrPropertyAnswers
import pages.transaction.TransactionUseOfLandOrPropertyPage
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import play.api.i18n.Messages

class TransactionUseOfLandOrPropertySummarySpec extends SpecBase {

  "TransactionUseOfLandOrPropertySummary" - {
    "must return a summary list row with a single use of Land or Property" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(TransactionUseOfLandOrPropertyPage, TransactionUseOfLandOrPropertyAnswers(
            office = "yes",
            hotel = "no",
            shop = "no",
            warehouse = "no",
            factory = "no",
            otherIndustrialUnit = "no",
            other = "no"
          )).success.value

        val result = TransactionUseOfLandOrPropertySummary.row(userAnswers)

        result.key.content.asHtml.toString() mustEqual msgs("transaction.transactionUseOfLandOrProperty.checkYourAnswersLabel")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "Office"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionUseOfLandOrPropertyController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.transactionUseOfLandOrProperty.change.hidden")
      }
    }

    "must return a summary list row with a multiple uses of Land or Property" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(TransactionUseOfLandOrPropertyPage, TransactionUseOfLandOrPropertyAnswers(
            office = "yes",
            hotel = "yes",
            shop = "yes",
            warehouse = "no",
            factory = "no",
            otherIndustrialUnit = "no",
            other = "no"
          )).success.value

        val result = TransactionUseOfLandOrPropertySummary.row(userAnswers)

        result.key.content.asHtml.toString() mustEqual msgs("transaction.transactionUseOfLandOrProperty.checkYourAnswersLabel")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "Office,<br>Hotel,<br>Shop"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionUseOfLandOrPropertyController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.transactionUseOfLandOrProperty.change.hidden")
      }
    }

    "must return a summary list row with a link to enter use of Land or Property when userAnswers is empty" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers

        val result = TransactionUseOfLandOrPropertySummary.row(userAnswers)

        result.key.content.asHtml.toString() mustEqual msgs("transaction.transactionUseOfLandOrProperty.checkYourAnswersLabel")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()

        htmlContent must include("govuk-link")
        htmlContent must include(controllers.transaction.routes.TransactionUseOfLandOrPropertyController.onPageLoad(CheckMode).url)
        htmlContent must include(msgs("transaction.transactionUseOfLandOrProperty.missing"))
        result.actions mustBe None
      }
    }
  }
}
