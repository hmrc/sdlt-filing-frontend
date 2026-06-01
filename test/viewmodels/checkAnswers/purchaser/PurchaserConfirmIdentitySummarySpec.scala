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

import base.SpecBase
import models.CheckMode
import models.purchaser.{NameOfPurchaser, PurchaserConfirmIdentity}
import pages.purchaser.{NameOfPurchaserPage, PurchaserConfirmIdentityPage, PurchaserUTRPage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class PurchaserConfirmIdentitySummarySpec extends SpecBase{

"PurchaserConfirmIdentitySummary" - {
  
  "when purchaser name is present" - {
    
    "must return a summary list row with surname only" in {
      
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(PurchaserConfirmIdentityPage, PurchaserConfirmIdentity.VatRegistrationNumber).success.value
          .set(NameOfPurchaserPage, NameOfPurchaser(forename1 = Some("Test"), forename2 = Some("Test2"), name = "Test")).success.value

        val row = PurchaserConfirmIdentitySummary.row(Some(userAnswers))
        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        result.key.content.asHtml.toString() mustEqual msgs("purchaser.confirmIdentity.checkYourAnswersLabel", userAnswers.get(NameOfPurchaserPage).map(_.name).getOrElse(""))

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "VAT registration number"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.confirmIdentity.change.hidden")
      }
    }

    "must return a summary list row with UTR when the values comes from the overview page" in {
      
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(NameOfPurchaserPage, NameOfPurchaser(forename1 = Some("Test"), forename2 = Some("Test2"), name = "Test")).success.value
          .set(PurchaserUTRPage, "11111111").success.value

        val row = PurchaserConfirmIdentitySummary.row(Some(userAnswers))
        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        result.key.content.asHtml.toString() mustEqual msgs("purchaser.confirmIdentity.checkYourAnswersLabel", userAnswers.get(NameOfPurchaserPage).map(_.name).getOrElse(""))

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "UTR"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.confirmIdentity.change.hidden")
      }
    }

    "must return a Missing and redirect call to missing page when data is not present" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers

        val result = PurchaserConfirmIdentitySummary.row(Some(userAnswers))

        result match {
          case Missing(call) =>
            call mustEqual controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(CheckMode)

          case Row(_) =>
            fail("Expected Missing but got Row")
        }
      }
    }
  }
}

}
