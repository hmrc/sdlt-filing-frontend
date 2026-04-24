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

package viewmodels.checkAnswers.purchaserAgent

import base.SpecBase
import models.CheckMode
import pages.purchaserAgent.{PurchaserAgentAuthorisedPage, PurchaserAgentNamePage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{ Row, Missing }

class PurchaserAgentAuthorisedSummarySpec extends SpecBase {

  "PurchaserAgentAuthorisedSummary" - {

    "when purchaser authorised is present" - {

      "must return a summary list row with 'yes' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserAgentAuthorisedPage, true).success.value
            .set(PurchaserAgentNamePage, "Agent name").success.value

          val row = PurchaserAgentAuthorisedSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.checkYourAnswersLabel", "Agent name")

          val htmlContent = result.value.content.asHtml.toString()
          htmlContent mustEqual "Yes"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.change.hidden", "Agent name")
        }
      }

      "must return a summary list row with 'yes' text and change link when agent name is not set" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserAgentAuthorisedPage, true).success.value

          val row = PurchaserAgentAuthorisedSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.checkYourAnswersLabel", "the agent")

          val htmlContent = result.value.content.asHtml.toString()
          htmlContent mustEqual "Yes"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.change.hidden", "the agent")
        }
      }

      "must return a summary list row with 'no' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserAgentAuthorisedPage, false).success.value
            .set(PurchaserAgentNamePage, "Agent name").success.value

          val row = PurchaserAgentAuthorisedSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.checkYourAnswersLabel", "Agent name")

          val htmlContent = result.value.content.asHtml.toString()
          htmlContent mustEqual "No"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.change.hidden", "Agent name")
        }
      }
    }

    "when purchaser authorised is not present" - {

      "must return a Missing and redirect call to missing page when data is not present" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PurchaserAgentAuthorisedSummary.row(emptyUserAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}
