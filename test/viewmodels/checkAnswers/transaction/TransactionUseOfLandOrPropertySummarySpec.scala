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
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class TransactionUseOfLandOrPropertySummarySpec extends SpecBase {

  "TransactionUseOfLandOrPropertySummary" - {

    "when use of land or property is present" - {

      "must return a Row with a single use of land or property" in {
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

          result mustBe a[Row]
          val row = result.asInstanceOf[Row].row

          row.key.content.asHtml.toString() mustEqual msgs("transaction.transactionUseOfLandOrProperty.checkYourAnswersLabel")

          val htmlContent = row.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "Office"

          row.actions.get.items.size mustEqual 1
          row.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionUseOfLandOrPropertyController.onPageLoad(CheckMode).url
          row.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          row.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.transactionUseOfLandOrProperty.change.hidden")
        }
      }

      "must return a Row with multiple uses of land or property" in {
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

          result mustBe a[Row]
          val row = result.asInstanceOf[Row].row

          row.key.content.asHtml.toString() mustEqual msgs("transaction.transactionUseOfLandOrProperty.checkYourAnswersLabel")

          val htmlContent = row.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "Office,<br>Hotel,<br>Shop"

          row.actions.get.items.size mustEqual 1
          row.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionUseOfLandOrPropertyController.onPageLoad(CheckMode).url
          row.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          row.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.transactionUseOfLandOrProperty.change.hidden")
        }
      }
    }

    "when use of land or property is not present" - {

      "must return a Missing and redirect call to missing page" in {
        val userAnswers = emptyUserAnswers
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = TransactionUseOfLandOrPropertySummary.row(userAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.transaction.routes.TransactionUseOfLandOrPropertyController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}
