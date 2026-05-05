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

package viewmodels.checkAnswers.transaction

import base.SpecBase
import models.CheckMode
import models.transaction.ReasonForRelief
import pages.transaction.ReasonForReliefPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

class ReasonForReliefSummarySpec extends SpecBase {

  "ReasonForReliefSummary" - {

    "when reason for relief is present" - {

      "must return a summary list row with the reason for relief value and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(ReasonForReliefPage, ReasonForRelief.GroupRelief).success.value

          val result = ReasonForReliefSummary.row(userAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("transaction.ReasonForRelief.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs(s"transaction.ReasonForRelief.${ReasonForRelief.GroupRelief}")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.ReasonForReliefController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.ReasonForRelief.change.hidden")
        }
      }

      "must display the correct message for different relief types" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val reliefTypes = Seq(
            ReasonForRelief.PartExchange,
            ReasonForRelief.GroupRelief,
            ReasonForRelief.CharitiesRelief,
            ReasonForRelief.RightToBuy,
            ReasonForRelief.OtherRelief
          )

          reliefTypes.foreach { reliefType =>
            val userAnswers = emptyUserAnswers
              .set(ReasonForReliefPage, reliefType).success.value

            val result = ReasonForReliefSummary.row(userAnswers)

            val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
            htmlContent mustEqual msgs(s"transaction.ReasonForRelief.${reliefType.toString}")
          }
        }
      }
    }

    "when reason for relief is not present" - {

      "must return a summary list row with a link to enter the reason for relief" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = ReasonForReliefSummary.row(emptyUserAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("transaction.ReasonForRelief.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.transaction.routes.ReasonForReliefController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("transaction.ReasonForRelief.missing"))

          result.actions mustBe None
        }
      }
    }

    "must use CheckMode for the change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(ReasonForReliefPage, ReasonForRelief.GroupRelief).success.value

        val result = ReasonForReliefSummary.row(userAnswers)

        result.actions.get.items.head.href mustEqual controllers.transaction.routes.ReasonForReliefController.onPageLoad(CheckMode).url
      }
    }
  }
}
