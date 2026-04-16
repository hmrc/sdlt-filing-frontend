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
import pages.transaction.AddRegisteredCharityNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

class AddRegisteredCharityNumberSummarySpec extends SpecBase {


  "AddRegisteredCharityNumberSummary" - {

    "when the add registered charity number of transaction is present" - {

      "must return a summary list row with 'yes' value and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)
          
          val userAnswers = emptyUserAnswers.set(AddRegisteredCharityNumberPage, true).success.value

          val result = AddRegisteredCharityNumberSummary.row(userAnswers)

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

          val result = AddRegisteredCharityNumberSummary.row(userAnswers)

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

    "when the add registered charity number of transaction is no present" - {

      "must return a summary list row with a missing link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = AddRegisteredCharityNumberSummary.row(emptyUserAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("transaction.addRegisteredCharityNumber.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.transaction.routes.AddRegisteredCharityNumberController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("transaction.addRegisteredCharityNumber.missing"))

          result.actions mustBe None
        }
      }
    }
  }
}