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
import models.purchaser.PurchaserTypeOfCompanyAnswers
import pages.purchaser.PurchaserTypeOfCompanyPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class PurchaserTypeOfCompanySummarySpec extends SpecBase {

  "PurchaserTypeOfCompanySummary" - {

    "when purchaser name is present" - {

      "must return a summary list row with surname only" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserTypeOfCompanyPage, PurchaserTypeOfCompanyAnswers(bank = "YES",
              buildingAssociation = "NO",
              centralGovernment = "NO",
              individualOther = "NO",
              insuranceAssurance = "NO",
              localAuthority = "NO",
              partnership = "NO",
              propertyCompany = "NO",
              publicCorporation = "NO",
              otherCompany = "NO",
              otherFinancialInstitute = "NO",
              otherIncludingCharity = "NO",
              superannuationOrPensionFund = "NO",
              unincorporatedBuilder = "NO",
              unincorporatedSoleTrader = "NO")).success.value

          val result = PurchaserTypeOfCompanySummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result.key.content.asHtml.toString() mustEqual msgs("purchaser.purchaserTypeOfCompany.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "Bank"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserTypeOfCompanyController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.purchaserTypeOfCompany.change.hidden")
        }
      }


    }
  }
}