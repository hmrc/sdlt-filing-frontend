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
import pages.transaction.{AddRegisteredCharityNumberPage, ReasonForReliefPage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class AddRegisteredCharityNumberSummarySpec extends SpecBase {

  "AddRegisteredCharityNumberSummary" - {

    "when the add registered charity number is present" - {

      "must return a summary list row with 'yes' value and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(AddRegisteredCharityNumberPage, true).success.value

          val row = AddRegisteredCharityNumberSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("transaction.addRegisteredCharityNumber.checkYourAnswersLabel")

          val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

          contentString mustEqual msgs("site.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.AddRegisteredCharityNumberController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.addRegisteredCharityNumber.change.hidden")
        }
      }

      "must return a summary list row with 'no' value and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(AddRegisteredCharityNumberPage, false).success.value

          val row = AddRegisteredCharityNumberSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("transaction.addRegisteredCharityNumber.checkYourAnswersLabel")

          val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

          contentString mustEqual msgs("site.no")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.AddRegisteredCharityNumberController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.addRegisteredCharityNumber.change.hidden")
        }
      }
    }

    "when the add registered charity number is not present" - {

      "must return a Missing and redirect call to missing page when reason for relief is CharitiesRelief but answer is missing" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(ReasonForReliefPage, ReasonForRelief.CharitiesRelief).success.value

          val result = AddRegisteredCharityNumberSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result match {
            case Missing(call) =>
              call mustEqual controllers.transaction.routes.AddRegisteredCharityNumberController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }

      "must return None when reason for relief is not CharitiesRelief" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          AddRegisteredCharityNumberSummary.row(emptyUserAnswers) mustBe None
        }
      }
    }
  }
}
