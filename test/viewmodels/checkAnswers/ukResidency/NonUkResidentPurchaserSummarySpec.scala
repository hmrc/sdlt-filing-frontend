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
import pages.ukResidency.NonUkResidentPurchaserPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class NonUkResidentPurchaserSummarySpec extends SpecBase {

  "NonUkResidentPurchaserSummary" - {

    "when non UK resident purchaser answer is present" - {

      "must return a SummaryListRow with 'yes' text and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(NonUkResidentPurchaserPage, true).success.value

          val result = NonUkResidentPurchaserSummary.row(userAnswers)

          result.key.content.asHtml.toString mustEqual msgs("ukResidency.nonUkResidentPurchaser.checkYourAnswersLabel")

          result.value.content.asInstanceOf[Text].asHtml.toString mustEqual msgs("site.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual
            controllers.ukResidency.routes.NonUkResidentPurchaserController.onPageLoad(CheckMode).url

          result.actions.get.items.head.content.asHtml.toString must include(msgs("site.change"))

          result.actions.get.items.head.visuallyHiddenText.value mustEqual
            msgs("ukResidency.nonUkResidentPurchaser.change.hidden")
        }
      }

      "must return a SummaryListRow with 'no' text and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(NonUkResidentPurchaserPage, false).success.value

          val result = NonUkResidentPurchaserSummary.row(userAnswers)

          result.key.content.asHtml.toString mustEqual msgs("ukResidency.nonUkResidentPurchaser.checkYourAnswersLabel")

          result.value.content.asInstanceOf[Text].asHtml.toString mustEqual msgs("site.no")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual
            controllers.ukResidency.routes.NonUkResidentPurchaserController.onPageLoad(CheckMode).url

          result.actions.get.items.head.content.asHtml.toString must include(msgs("site.change"))

          result.actions.get.items.head.visuallyHiddenText.value mustEqual
            msgs("ukResidency.nonUkResidentPurchaser.change.hidden")
        }
      }
    }

    "when non UK resident purchaser answer is not present" - {

      "must return a SummaryListRow with a missing link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = NonUkResidentPurchaserSummary.row(emptyUserAnswers)

          result.key.content.asHtml.toString mustEqual msgs("ukResidency.nonUkResidentPurchaser.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString
          valueHtml must include(controllers.ukResidency.routes.NonUkResidentPurchaserController.onPageLoad(CheckMode).url)
          valueHtml must include(msgs("ukResidency.nonUkResidentPurchaser.missing"))
          valueHtml must include("govuk-link")

          result.actions mustBe None
        }
      }
    }
  }
}