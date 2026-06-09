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

package viewmodels.checkAnswers.vendorAgent

import base.SpecBase
import models.CheckMode
import pages.vendorAgent.VendorAgentsAddReferencePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class VendorAgentsAddReferenceSummarySpec extends SpecBase {

  "VendorAgentsAddReferenceSummary" - {

    "when purchaser name is present" - {

      "must return a summary list row with surname only" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(VendorAgentsAddReferencePage, true).success.value

          val result = VendorAgentsAddReferenceSummary.row(userAnswers) match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("vendorAgent.VendorAgentsAddReference.checkYourAnswersLabel")

          result.value.content.asHtml.toString() mustEqual msgs("site.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendorAgent.routes.VendorAgentsAddReferenceController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendorAgent.VendorAgentsAddReference.change.hidden")
        }
      }

      "must return Missing with the add reference controller route when add reference is not answered" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          VendorAgentsAddReferenceSummary.row(emptyUserAnswers) match {
            case Missing(call) =>
              call.url mustEqual controllers.vendorAgent.routes.VendorAgentsAddReferenceController.onPageLoad(CheckMode).url
            case _ => fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}