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

package viewmodels.checkAnswers.vendorAgent

import base.SpecBase
import models.CheckMode
import pages.vendorAgent.{VendorAgentsAddReferencePage, VendorAgentsReferencePage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}


class VendorAgentsReferenceSummarySpec extends SpecBase {

  "VendorAgentReference Summary" - {

    "when agent reference is present" - {

      "must return a summary list row with agent reference label" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(VendorAgentsReferencePage, "12345678").success.value

          val result = VendorAgentsReferenceSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row")) match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("vendorAgent.agentsReference.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "12345678"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendorAgent.routes.VendorAgentsReferenceController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendorAgent.agentsReference.change.hidden")
        }
      }
    }

    "must use CheckMode for the change link" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(VendorAgentsReferencePage, "12345678").success.value

        val result = VendorAgentsReferenceSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row")) match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        result.actions.get.items.head.href mustEqual controllers.vendorAgent.routes.VendorAgentsReferenceController.onPageLoad(CheckMode).url
      }
    }

    "must return Some(Missing) with the reference controller route when add reference number is true but reference is not filled in" in {
      val userAnswers = emptyUserAnswers.set(VendorAgentsAddReferencePage, true).success.value
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        VendorAgentsReferenceSummary.row(userAnswers) match {
          case Some(Missing(call)) =>
            call.url mustEqual controllers.vendorAgent.routes.VendorAgentsReferenceController.onPageLoad(CheckMode).url
          case _ => fail("Expected Some(Missing) but got something else")
        }
      }
    }

    "must return None when add reference number is false" in {
      val userAnswers = emptyUserAnswers.set(VendorAgentsAddReferencePage, false).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        VendorAgentsReferenceSummary.row(emptyUserAnswers) mustBe None
      }
    }
  }
}