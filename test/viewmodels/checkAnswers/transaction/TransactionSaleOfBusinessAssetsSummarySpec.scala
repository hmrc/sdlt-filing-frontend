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
import models.transaction.TransactionSaleOfBusinessAssetsAnswers
import models.CheckMode
import pages.transaction.TransactionSaleOfBusinessAssetsPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class TransactionSaleOfBusinessAssetsSummarySpec extends SpecBase {
  
  "TransactionSaleOfBusinessAssetsSummary" - {
    "must return a summary list row with a single asset included in the sale of Business" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(TransactionSaleOfBusinessAssetsPage, TransactionSaleOfBusinessAssetsAnswers(
            stock = "yes",
            goodwill = "no",
            chattelsAndMoveables = "no",
            others = "no",
          )).success.value

        val row = TransactionSaleOfBusinessAssetsSummary.row(userAnswers)

        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        result.key.content.asHtml.toString() mustEqual msgs("transaction.transactionSaleOfBusinessAssets.checkYourAnswersLabel")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "Stock"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionSaleOfBusinessAssetsController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.transactionSaleOfBusinessAssets.change.hidden")
      }
    }

    "must return a summary list row with a multiple assets included in the sale of Business" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(TransactionSaleOfBusinessAssetsPage, TransactionSaleOfBusinessAssetsAnswers(
            stock = "yes",
            goodwill = "no",
            chattelsAndMoveables = "yes",
            others = "no",
          )).success.value

        val row = TransactionSaleOfBusinessAssetsSummary.row(userAnswers)

        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        result.key.content.asHtml.toString() mustEqual msgs("transaction.transactionSaleOfBusinessAssets.checkYourAnswersLabel")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "Stock,<br>Chattels and moveables"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionSaleOfBusinessAssetsController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.transactionSaleOfBusinessAssets.change.hidden")
      }
    }
    
    "must return a Missing and redirect call to missing page when data is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val result = TransactionSaleOfBusinessAssetsSummary.row(emptyUserAnswers)

        result match {
          case Missing(call) =>
            call mustEqual controllers.transaction.routes.TransactionSaleOfBusinessAssetsController.onPageLoad(CheckMode)

          case Row(_) =>
            fail("Expected Missing but got Row")
        }
      }
    }
  }
}
