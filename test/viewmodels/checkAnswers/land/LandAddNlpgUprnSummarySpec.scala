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

package viewmodels.checkAnswers.land

import base.SpecBase
import models.CheckMode
import pages.land.LandNlpgUprnPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

class LandNlpgUprnSummarySpec extends SpecBase {

  "LandNlpgUprnSummary" - {

    "when NLPG UPRN is present" - {

      "must return a SummaryListRow with the UPRN value and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(LandNlpgUprnPage, "1234567890").success.value

          val result = LandNlpgUprnSummary.row(userAnswers).get

          result.key.content.asHtml.toString() mustEqual msgs("land.nlpgUprn.checkYourAnswersLabel")

          val contentString = result.value.content.asHtml.toString()
          contentString mustEqual "1234567890"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.land.routes.LandNlpgUprnController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("land.nlpgUprn.change.hidden")
        }
      }
    }

    "when NLPG UPRN is not present" - {

      "must return a SummaryListRow with a missing link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = LandNlpgUprnSummary.row(emptyUserAnswers).get

          result.key.content.asHtml.toString() mustEqual msgs("land.nlpgUprn.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.land.routes.LandNlpgUprnController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("land.nlpgUprn.missing"))

          result.actions mustBe None
        }
      }
    }
  }
}