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
import models.land.LandSelectMeasurementUnit
import pages.land.{AreaOfLandPage, LandSelectMeasurementUnitPage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

class AreaOfLandSummarySpec extends SpecBase {

  "AreaOfLandSummary" - {

    "when the area of land and unit type are present" - {

      "must return a summary list row with value and change link" - {

        "when unit type is square metres" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            implicit val msgs: Messages = messages(application)

            val area = "100.000"

            val userAnswers = emptyUserAnswers
              .set(AreaOfLandPage, area).success.value
              .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Sqms).success.value

            val result = AreaOfLandSummary.row(userAnswers).getOrElse(fail("Failed to create summaryListRow"))

            result.key.content.asHtml.toString() mustEqual
              msgs("land.areaOfLand.checkYourAnswersLabel")

            val valueHtml = result.value.content.asHtml.toString()
            valueHtml mustEqual s"$area ${msgs("land.areaOfLand.SQMETRE.suffix")}"

            result.actions.get.items.size mustEqual 1
            result.actions.get.items.head.href mustEqual
              controllers.land.routes.AreaOfLandController.onPageLoad(CheckMode).url
            result.actions.get.items.head.content.asHtml.toString() must include(
              msgs("site.change")
            )
            result.actions.get.items.head.visuallyHiddenText.value mustEqual
              msgs("land.areaOfLand.change.hidden")
          }
        }

        "when unit type is hectares" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            implicit val msgs: Messages = messages(application)

            val area = "100.000"

            val userAnswers = emptyUserAnswers
              .set(AreaOfLandPage, area).success.value
              .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Hectares).success.value

            val result = AreaOfLandSummary.row(userAnswers).getOrElse(fail("Failed to create summaryListRow"))

            result.key.content.asHtml.toString() mustEqual
              msgs("land.areaOfLand.checkYourAnswersLabel")

            val valueHtml = result.value.content.asHtml.toString()
            valueHtml mustEqual s"$area ${msgs("land.areaOfLand.HECTARES.suffix")}"

            result.actions.get.items.size mustEqual 1
            result.actions.get.items.head.href mustEqual
              controllers.land.routes.AreaOfLandController.onPageLoad(CheckMode).url
            result.actions.get.items.head.content.asHtml.toString() must include(
              msgs("site.change")
            )
            result.actions.get.items.head.visuallyHiddenText.value mustEqual
              msgs("land.areaOfLand.change.hidden")
          }
        }
      }
    }

    "when only the unit type is present" - {

      "must return a summary list row with a link to add the area of land" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(LandSelectMeasurementUnitPage, LandSelectMeasurementUnit.Sqms).success.value

          val result = AreaOfLandSummary.row(userAnswers).getOrElse(fail("Failed to create summaryListRow"))

          result.key.content.asHtml.toString() mustEqual
            msgs("land.areaOfLand.checkYourAnswersLabel")

          val htmlContent =
            result.value.content.asInstanceOf[HtmlContent].asHtml.toString()

          htmlContent must include("govuk-link")
          htmlContent must include(
            controllers.land.routes.AreaOfLandController.onPageLoad(CheckMode).url
          )
          htmlContent must include(
            msgs("land.areaOfLand.missing")
          )

          result.actions mustBe None
        }
      }
    }

    "when the unit type is not present" - {

      "must return None" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers

          val result = AreaOfLandSummary.row(userAnswers)

          result mustBe None
        }
      }
    }
  }
}
