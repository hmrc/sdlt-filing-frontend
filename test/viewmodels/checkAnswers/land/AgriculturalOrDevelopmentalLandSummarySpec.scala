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
import models.land.LandTypeOfProperty
import pages.land.{AgriculturalOrDevelopmentalLandPage, LandTypeOfPropertyPage}
import models.land.LandTypeOfProperty.*
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class AgriculturalOrDevelopmentalLandSummarySpec extends SpecBase {

  "AgriculturalOrDevelopmentalLandSummary" - {

    "when agricultural or land is present" - {

      "must return a SummaryListRow with 'yes' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(AgriculturalOrDevelopmentalLandPage, true).success.value

          val result = AgriculturalOrDevelopmentalLandSummary.row(userAnswers).getOrElse(fail("Failed to create SummaryListRow"))

          result.key.content.asHtml.toString() mustEqual msgs("land.agriculturalOrDevelopmental.checkYourAnswersLabel")

          val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

          contentString mustEqual msgs("site.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.land.routes.AgriculturalOrDevelopmentalLandController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("land.agriculturalOrDevelopmental.change.hidden")
        }
      }

      "must return a SummaryListRow with 'no' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(AgriculturalOrDevelopmentalLandPage, false).success.value

          val result = AgriculturalOrDevelopmentalLandSummary.row(userAnswers).getOrElse(fail("Failed to create SummaryListRow"))

          result.key.content.asHtml.toString() mustEqual msgs("land.agriculturalOrDevelopmental.checkYourAnswersLabel")

          val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

          contentString mustEqual msgs("site.no")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.land.routes.AgriculturalOrDevelopmentalLandController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("land.agriculturalOrDevelopmental.change.hidden")
        }
      }
    }

    "when agricultural or developmental is not present" - {

      "must return a SummaryListRow with a link to select if the transaction is agricultural or developmental" - {

        "when the property type is non-residential or mixed" in {
          val userAnswers = emptyUserAnswers
            .set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            implicit val msgs: Messages = messages(application)

            val result = AgriculturalOrDevelopmentalLandSummary.row(userAnswers).getOrElse(fail("Failed to create SummaryListRow"))

            result.key.content.asHtml.toString() mustEqual msgs("land.agriculturalOrDevelopmental.checkYourAnswersLabel")

            val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
            htmlContent must include("govuk-link")
            htmlContent must include(controllers.land.routes.AgriculturalOrDevelopmentalLandController.onPageLoad(CheckMode).url)
            htmlContent must include(msgs("land.agriculturalOrDevelopmental.missing"))

            result.actions mustBe None
          }
        }
      }

      "must return None" - {

        "when property type is not non-residential or mixed" in {
          val userAnswers = emptyUserAnswers
            .set(LandTypeOfPropertyPage, LandTypeOfProperty.Residential).success.value
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            implicit val msgs: Messages = messages(application)

            AgriculturalOrDevelopmentalLandSummary.row(userAnswers) mustBe None
          }
        }

        "when the property type is not set" in {
          val userAnswers = emptyUserAnswers
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            implicit val msgs: Messages = messages(application)

            AgriculturalOrDevelopmentalLandSummary.row(userAnswers) mustBe None
          }
        }
      }
    }
  }
}