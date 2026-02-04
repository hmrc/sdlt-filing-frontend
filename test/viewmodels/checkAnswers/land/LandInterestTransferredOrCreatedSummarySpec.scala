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
import models.land.LandInterestTransferredOrCreated
import pages.land.LandInterestTransferredOrCreatedPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

class LandInterestTransferredOrCreatedSummarySpec extends SpecBase {

  "LandInterestTransferredOrCreatedSummary" - {

    "when interest transferred or created is present" - {

      "must return a summary list row with value and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(LandInterestTransferredOrCreatedPage, LandInterestTransferredOrCreated.FG).success.value

          val result = LandInterestTransferredOrCreatedSummary.row(userAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("land.landInterestTransferredOrCreated.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs(s"land.landInterestTransferredOrCreated.${LandInterestTransferredOrCreated.FG}")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.land.routes.LandInterestTransferredOrCreatedController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("land.landInterestTransferredOrCreated.change.hidden")
        }
      }

      "must display the correct message for different values" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val interestTransferredOrCreatedValues = Seq(
            LandInterestTransferredOrCreated.FG,
            LandInterestTransferredOrCreated.FP,
            LandInterestTransferredOrCreated.FT,
            LandInterestTransferredOrCreated.LG
          )

          interestTransferredOrCreatedValues.foreach { value =>
            val userAnswers = emptyUserAnswers
              .set(LandInterestTransferredOrCreatedPage, value).success.value

            val result = LandInterestTransferredOrCreatedSummary.row(userAnswers)

            val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
            htmlContent mustEqual msgs(s"land.landInterestTransferredOrCreated.$value")
          }
        }
      }

      "must properly escape special characters in message" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(LandInterestTransferredOrCreatedPage, LandInterestTransferredOrCreated.FG).success.value

          val result = LandInterestTransferredOrCreatedSummary.row(userAnswers)

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent.nonEmpty mustBe true
          result.value.content mustBe a[HtmlContent]
        }
      }
    }

    "when interest transferred or created is not present" - {

      "must return a summary list row with a link to select interest transferred or created" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = LandInterestTransferredOrCreatedSummary.row(emptyUserAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("land.landInterestTransferredOrCreated.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.land.routes.LandInterestTransferredOrCreatedController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("land.landInterestTransferredOrCreated.missing"))

          result.actions mustBe None
        }
      }
    }

    "must use CheckMode for the change link" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(LandInterestTransferredOrCreatedPage, LandInterestTransferredOrCreated.FG).success.value

        val result = LandInterestTransferredOrCreatedSummary.row(userAnswers)

        result.actions.get.items.head.href mustEqual controllers.land.routes.LandInterestTransferredOrCreatedController.onPageLoad(CheckMode).url
      }
    }
  }

}
