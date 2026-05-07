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
import pages.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedPremiumPayableTaxPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary
import viewmodels.checkAnswers.summary.SummaryRowResult

class PremiumPayableTaxSummarySpec extends SpecBase {

  "LeaseholdSelfAssessedPremiumPayableTaxSummary" - {
    "when there is premium payable tax" - {
      "must return a summary list row with values" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)
          val value = "1000.00"

          val userAnswers = emptyUserAnswers
            .set(LeaseholdSelfAssessedPremiumPayableTaxPage, value).toOption

          val res = PremiumPayableTaxSummary.row(userAnswers)

          val result = res match {
            case r: SummaryRowResult.Row => r
            case _ => fail("Failed to retrieve SummaryRowResult.Row")
          }

          result.row.key.content.asHtml.toString() mustEqual msgs("taxCalculation.leaseholdSelfAssessed.premiumPayable.checkYourAnswers")

          val valueHtml = result.row.value.content.asHtml.toString()

          valueHtml mustEqual "£1000.00"
          result.row.actions.get.items.size mustEqual 1
          result.row.actions.get.items.head.href mustEqual
            controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedPremiumPayableTaxController.onPageLoad(CheckMode).url
          result.row.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.row.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("taxCalculation.leaseholdSelfAssessed.premiumPayable.change.hidden")
        }
      }
    }

    "when there is no premium payable tax" - {
      "must return missing with route" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val res = PremiumPayableTaxSummary.row(Some(emptyUserAnswers))

          val result = res match {
            case m: SummaryRowResult.Missing => m
            case _ => fail("Failed to retrieve SummaryRowResult.Missing")
          }

          result mustBe SummaryRowResult.Missing(controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedPremiumPayableTaxController.onPageLoad(CheckMode))
        }
      }
    }
  }
}
