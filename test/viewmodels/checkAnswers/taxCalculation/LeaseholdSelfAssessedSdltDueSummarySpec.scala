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

package viewmodels.checkAnswers.taxCalculation

import base.SpecBase
import models.CheckMode
import pages.taxCalculation.leaseholdSelfAssessed.{LeaseholdSelfAssessedNpvTaxPage, LeaseholdSelfAssessedPremiumPayableTaxPage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class LeaseholdSelfAssessedSdltDueSummarySpec extends SpecBase {

  "LeaseholdSelfAssessedSdltDueSummary" - {

    "when premium payable and NVP are present" - {

      "must return a summary list row with the £-prefixed amount" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(LeaseholdSelfAssessedPremiumPayableTaxPage, "1200").success.value
            .set(LeaseholdSelfAssessedNpvTaxPage, "350").success.value

          val row = LeaseholdSelfAssessedSdltDueSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("taxCalculation.selfAssessedSdltDue.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "£1,550"
        }
      }
    }

    "when premium payable and NVP are not present" - {

      "must return a Missing and redirect call to missing page when data is missing" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = LeaseholdSelfAssessedSdltDueSummary.row(emptyUserAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedPremiumPayableTaxController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }

      "must return a Missing and redirect call to missing page when premium payable and NVP are invalid" in {

        val userAnswers = emptyUserAnswers
          .set(LeaseholdSelfAssessedPremiumPayableTaxPage, "abc").success.value
          .set(LeaseholdSelfAssessedNpvTaxPage, "abc").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = LeaseholdSelfAssessedSdltDueSummary.row(userAnswers)

          result match{
            case Missing(call) =>
              call mustEqual controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedPremiumPayableTaxController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}
