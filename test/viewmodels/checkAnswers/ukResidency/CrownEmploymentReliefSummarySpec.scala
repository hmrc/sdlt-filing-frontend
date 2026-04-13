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
import pages.ukResidency.CrownEmploymentReliefPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class CrownEmploymentReliefSummarySpec extends SpecBase {

  "CrownEmploymentReliefSummary" - {

    "when CrownEmploymentRelief has been answered" - {

      "must return a SummaryListRow with 'yes' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(CrownEmploymentReliefPage, true).success.value

          val result = CrownEmploymentReliefSummary.row(userAnswers).value

          result.key.content.asHtml.toString() mustEqual msgs("ukResidency.crownEmploymentRelief.checkYourAnswersLabel")

          val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

          contentString mustEqual msgs("site.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.ukResidency.routes.CrownEmploymentReliefController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("ukResidency.crownEmploymentRelief.change.hidden")
        }
      }

      "must return a SummaryListRow with 'no' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(CrownEmploymentReliefPage, false).success.value

          val result = CrownEmploymentReliefSummary.row(userAnswers).value

          result.key.content.asHtml.toString() mustEqual msgs("ukResidency.crownEmploymentRelief.checkYourAnswersLabel")

          val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

          contentString mustEqual msgs("site.no")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.ukResidency.routes.CrownEmploymentReliefController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("ukResidency.crownEmploymentRelief.change.hidden")
        }
      }
    }

    "when CrownEmploymentRelief has not been answered" - {

      "must return None" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          CrownEmploymentReliefSummary.row(emptyUserAnswers) mustBe None
        }
      }
    }
  }
}