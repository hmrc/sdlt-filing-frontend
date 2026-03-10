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

package viewmodels.checkAnswers.ukResidency

import base.SpecBase
import models.CheckMode
import pages.ukResidency.CloseCompanyPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class CloseCompanySummarySpec extends SpecBase {

  "CloseCompanySummary" - {

    "when close company answer is present" - {

      "must return a SummaryListRow with 'yes' text and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers =
            emptyUserAnswers.set(CloseCompanyPage, true).success.value

          val result = CloseCompanySummary.row(userAnswers).value

          result.key.content.asHtml.toString mustEqual msgs("ukResidency.closeCompany.checkYourAnswersLabel")

          val contentString =
            result.value.content.asInstanceOf[Text].asHtml.toString

          contentString mustEqual msgs("site.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual
            controllers.ukResidency.routes.CloseCompanyController.onPageLoad(CheckMode).url

          result.actions.get.items.head.content.asHtml.toString must include(msgs("site.change"))

          result.actions.get.items.head.visuallyHiddenText.value mustEqual
            msgs("ukResidency.closeCompany.change.hidden")
        }
      }

      "must return a SummaryListRow with 'no' text and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers =
            emptyUserAnswers.set(CloseCompanyPage, false).success.value

          val result = CloseCompanySummary.row(userAnswers).value

          result.key.content.asHtml.toString mustEqual msgs("ukResidency.closeCompany.checkYourAnswersLabel")

          val contentString =
            result.value.content.asInstanceOf[Text].asHtml.toString

          contentString mustEqual msgs("site.no")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual
            controllers.ukResidency.routes.CloseCompanyController.onPageLoad(CheckMode).url

          result.actions.get.items.head.content.asHtml.toString must include(msgs("site.change"))

          result.actions.get.items.head.visuallyHiddenText.value mustEqual
            msgs("ukResidency.closeCompany.change.hidden")
        }
      }
    }

    "when close company answer is not present" - {

      "must return None" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = CloseCompanySummary.row(emptyUserAnswers)

          result mustBe None
        }
      }
    }
  }
}
