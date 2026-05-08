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
import models.{CheckMode}
import pages.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedPenaltiesAndInterestPage
import pages.taxCalculation.freeholdTaxCalculated.FreeholdTaxCalculatedPenaltiesAndInterestPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.Row

class AmountWithPenaltiesSummarySpec extends SpecBase {

  "AmountWithPenaltiesSummary" - {

    "FreeholdTaxCalculatedPenalties :: must return a SummaryListRow with change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(FreeholdTaxCalculatedPenaltiesAndInterestPage, true).success.value

        val row = AmountWithPenaltiesSummary.row(FreeholdTaxCalculatedPenaltiesAndInterestPage)(userAnswers)

        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        result.key.content.asHtml.toString() mustEqual msgs("taxCalculation.penaltiesAndInterest.checkYourAnswersLabel")

        val contentString = result.value.content.asHtml.toString()

        contentString mustEqual "true"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.taxCalculation
          .freeholdTaxCalculated.routes
          .FreeholdSdltCalculatedPenaltiesAndInterestController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
      }
    }

    "FreeholdSelfAssessedPenaltiesAndInterestPage :: must return a SummaryListRow with change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(FreeholdSelfAssessedPenaltiesAndInterestPage, true).success.value

        val row = AmountWithPenaltiesSummary.row(FreeholdSelfAssessedPenaltiesAndInterestPage)(userAnswers)

        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        result.key.content.asHtml.toString() mustEqual msgs("taxCalculation.penaltiesAndInterest.checkYourAnswersLabel")

        val contentString = result.value.content.asHtml.toString()

        contentString mustEqual "true"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.taxCalculation
          .freeholdSelfAssessed.routes
          .FreeholdSelfAssessedPenaltiesAndInterestController.onPageLoad(CheckMode).url

        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
      }
    }

    "LeaseholdTaxCalculatedPenaltiesAndInterestPage :: must return a SummaryListRow with change link" in {
      // TODO: as a part of https://jira.tools.tax.service.gov.uk/browse/DTR-5066
    }

    "LeaseholdSelfAssessedPenaltiesAndInterestPage :: must return a SummaryListRow with change link" in {
      // TODO: as a part of https://jira.tools.tax.service.gov.uk/browse/DTR-5067
    }

  }

}
