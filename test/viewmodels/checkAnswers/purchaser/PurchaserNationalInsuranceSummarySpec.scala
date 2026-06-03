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

package viewmodels.checkAnswers.purchaser

import base.SpecBase
import models.CheckMode
import pages.purchaser.PurchaserNationalInsurancePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class PurchaserNationalInsuranceSummarySpec extends SpecBase {

  "PurchaserNationalInsuranceSummary" - {

    "when valid national insurance present" - {

      "must return summary list row with national insurance only" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserNationalInsurancePage, "AA123456A").success.value

          val row = PurchaserNationalInsuranceSummary.row(Some(userAnswers))
          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("purchaser.nationalInsurance.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "AA123456A"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserNationalInsuranceController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.nationalInsurance.change.hidden")
        }
      }
    }

    "must return a Missing and redirect call to missing page when data is not present" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers

        val result = PurchaserNationalInsuranceSummary.row(Some(userAnswers))

        result match {
          case Missing(call) =>
            call mustEqual controllers.purchaser.routes.PurchaserNationalInsuranceController.onPageLoad(CheckMode)

          case Row(_) =>
            fail("Expected Missing but got Row")
        }
      }
    }
  }
}
