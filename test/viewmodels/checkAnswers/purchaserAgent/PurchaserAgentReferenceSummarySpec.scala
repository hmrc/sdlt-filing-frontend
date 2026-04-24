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

package viewmodels.checkAnswers.purchaserAgent

import base.SpecBase
import models.CheckMode
import pages.purchaserAgent.{AddPurchaserAgentReferenceNumberPage, PurchaserAgentReferencePage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult. { Row, Missing }

class PurchaserAgentReferenceSummarySpec extends SpecBase {

  "PurchaserAgentReferenceSummary" - {

    "when reference number is present" - {

      "must return a SummaryListRow with change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(PurchaserAgentReferencePage, "123456").success.value

          val row = PurchaserAgentReferenceSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.reference.checkYourAnswersLabel")

          val contentString = result.value.content.asHtml.toString()

          contentString mustEqual "123456"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaserAgent.routes.PurchaserAgentReferenceController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserAgent.reference.change.hidden")
        }
      }
    }

    "when reference number is not present" - {

      "must return a Missing and redirect call to missing page when data is not present add reference number is true but reference number is missing" in {
        val userAnswers = emptyUserAnswers.set(AddPurchaserAgentReferenceNumberPage, true).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PurchaserAgentReferenceSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result match {
            case Missing(call) =>
              call mustEqual controllers.purchaserAgent.routes.PurchaserAgentReferenceController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }

      "must return None when add reference number is false" in {
        val userAnswers = emptyUserAnswers.set(AddPurchaserAgentReferenceNumberPage, false).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          PurchaserAgentReferenceSummary.row(emptyUserAnswers) mustBe None
        }
      }
    }
  }
}