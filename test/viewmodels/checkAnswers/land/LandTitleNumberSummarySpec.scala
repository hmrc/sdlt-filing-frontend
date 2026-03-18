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
import pages.land.LandTitleNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

class LandTitleNumberSummarySpec extends SpecBase {

  "LandTitleNumberSummary" - {

    "when the land title number is present" - {

      "must return a summary list row with value and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val titleNumber = "AB123456"

          val userAnswers = emptyUserAnswers
            .set(LandTitleNumberPage, titleNumber).success.value

          val result = LandTitleNumberSummary.row(userAnswers)

          result.key.content.asHtml.toString() mustEqual
            msgs("land.titleNumber.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml mustEqual titleNumber

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual
            controllers.land.routes.LandTitleNumberController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(
            msgs("site.change")
          )
          result.actions.get.items.head.visuallyHiddenText.value mustEqual
            msgs("land.titleNumber.change.hidden")
        }
      }

      "must properly escape special characters in the title number" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val unsafeTitleNumber = "<script>alert('xss')</script>"

          val userAnswers = emptyUserAnswers
            .set(LandTitleNumberPage, unsafeTitleNumber).success.value

          val result = LandTitleNumberSummary.row(userAnswers)

          val valueHtml = result.value.content.asHtml.toString()

          valueHtml must not include "<script>"
          valueHtml must include("&amp;lt;script&amp;gt;")

        }
      }

      "must use CheckMode for the change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(LandTitleNumberPage, "AB123456").success.value

          val result = LandTitleNumberSummary.row(userAnswers)

          result.actions.get.items.head.href mustEqual
            controllers.land.routes.LandTitleNumberController.onPageLoad(CheckMode).url
        }
      }
    }

    "when the land title number is not present" - {

      "must return a summary list row with a link to add the title number" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = LandTitleNumberSummary.row(emptyUserAnswers)

          result.key.content.asHtml.toString() mustEqual
            msgs("land.titleNumber.checkYourAnswersLabel")

          val htmlContent =
            result.value.content.asInstanceOf[HtmlContent].asHtml.toString()

          htmlContent must include("govuk-link")
          htmlContent must include(
            controllers.land.routes.LandTitleNumberController.onPageLoad(CheckMode).url
          )
          htmlContent must include(
            msgs("land.titleNumber.missing")
          )

          result.actions mustBe None
        }
      }
    }
  }
}
