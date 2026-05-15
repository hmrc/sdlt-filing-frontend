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
import pages.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedAmountPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class FreeholdSelfAssessedAmountSummarySpec extends SpecBase {

  "FreeholdSelfAssessedAmountSummary" - {

    "when an answer has been entered" - {

      "must return a Row with the £-prefixed amount and a change link" in {
        val app = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(app) {
          implicit val msgs: Messages = messages(app)
          val changeRoute = controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedSdltSelfAssessmentController.onPageLoad(CheckMode)
          val answers     = emptyUserAnswers.set(FreeholdSelfAssessedAmountPage, "12345").success.value

          val result = FreeholdSelfAssessedAmountSummary.row(Some(answers)) match {
            case Row(r) => r
            case _      => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("taxCalculation.sdltSelfAssessment.checkYourAnswersLabel")
          result.value.content.asInstanceOf[HtmlContent].asHtml.toString() mustEqual "£12345"
          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual changeRoute.url
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("taxCalculation.sdltSelfAssessment.change.hidden")
        }
      }
    }

    "when no answer has been entered" - {

      "must return Missing pointing at the question page in CheckMode" in {
        val app = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(app) {
          implicit val msgs: Messages = messages(app)
          val changeRoute = controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedSdltSelfAssessmentController.onPageLoad(CheckMode)

          FreeholdSelfAssessedAmountSummary.row(Some(emptyUserAnswers)) mustBe Missing(changeRoute)
        }
      }

      "must return Missing when UserAnswers is None" in {
        val app = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(app) {
          implicit val msgs: Messages = messages(app)
          val changeRoute = controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedSdltSelfAssessmentController.onPageLoad(CheckMode)

          FreeholdSelfAssessedAmountSummary.row(None) mustBe Missing(changeRoute)
        }
      }
    }
  }
}
