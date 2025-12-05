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
import models.purchaser.DoesPurchaserHaveNI
import pages.purchaser.{DoesPurchaserHaveNIPage, PurchaserNationalInsurancePage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class PurchaserNationalInsuranceSummarySpec extends SpecBase {

  "PurchaserNationalInsuranceSummary" - {

    "when valid national insurance present" - {

      "must return summary list row with national insurance only" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserNationalInsurancePage, "AA123456A").success.value

          val result = PurchaserNationalInsuranceSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result.key.content.asHtml.toString() mustEqual msgs("purchaserNationalInsurance.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[Text].asHtml.toString()
          htmlContent mustEqual "AA123456A"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserNationalInsuranceController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserNationalInsurance.change.hidden")
        }
      }

      "must use check mode to change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserNationalInsurancePage, "AA123465A").success.value

          val result = PurchaserNationalInsuranceSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserNationalInsuranceController.onPageLoad(CheckMode).url
        }
      }
    }
  }
}
