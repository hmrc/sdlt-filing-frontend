/*
 * Copyright 2025 HM Revenue & Customs
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
import pages.land.LandTypeOfPropertyPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class LandTypeOfPropertySummarySpec extends SpecBase {

  "LandTypeOfPropertySummary" - {

    "when property type is Residential" - {

      "must return a summary list row with Residential label" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(LandTypeOfPropertyPage, LandTypeOfProperty.Residential).success.value

          val result = LandTypeOfPropertySummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("land.landTypeOfProperty.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs("land.landTypeOfProperty.01")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.land.routes.LandTypeOfPropertyController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("land.landTypeOfProperty.change.hidden")
        }
      }
    }

    "when property type is Mixed" - {

      "must return a summary list row with Mixed label" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value

          val result = LandTypeOfPropertySummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("land.landTypeOfProperty.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs("land.landTypeOfProperty.02")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.land.routes.LandTypeOfPropertyController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("land.landTypeOfProperty.change.hidden")
        }
      }
    }

    "when property type is Non-Residential" - {

      "must return a summary list row with Non-Residential label" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value

          val result = LandTypeOfPropertySummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("land.landTypeOfProperty.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs("land.landTypeOfProperty.03")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.land.routes.LandTypeOfPropertyController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("land.landTypeOfProperty.change.hidden")
        }
      }
    }

    "when property type is Additional" - {

      "must return a summary list row with Additional label" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(LandTypeOfPropertyPage, LandTypeOfProperty.Additional).success.value

          val result = LandTypeOfPropertySummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("land.landTypeOfProperty.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs("land.landTypeOfProperty.04")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.land.routes.LandTypeOfPropertyController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("land.landTypeOfProperty.change.hidden")
        }
      }
    }


    "when property type is not present" - {

      "must return None" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = LandTypeOfPropertySummary.row(Some(emptyUserAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("land.landTypeOfProperty.checkYourAnswersLabel")
        }
      }
    }

    "must use CheckMode for the change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Residential).success.value

        val result = LandTypeOfPropertySummary.row(Some(userAnswers))

        result.actions.get.items.head.href mustEqual controllers.land.routes.LandTypeOfPropertyController.onPageLoad(CheckMode).url
      }
    }

    "must properly escape messages in value" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Residential).success.value

        val result = LandTypeOfPropertySummary.row(Some(userAnswers))

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent must not include "<script>"
        htmlContent must not include "&"
      }
    }

    "must handle all enum values correctly" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val residentialAnswers = emptyUserAnswers
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Residential).success.value

        val mixedAnswers = emptyUserAnswers
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value

        val nonResidentialAnswers = emptyUserAnswers
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value

        val additionalAnswers = emptyUserAnswers
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Additional).success.value

        val residentialResult = LandTypeOfPropertySummary.row(Some(residentialAnswers))
        val mixedResult = LandTypeOfPropertySummary.row(Some(mixedAnswers))
        val nonResidentialResult = LandTypeOfPropertySummary.row(Some(nonResidentialAnswers))
        val additionalResult = LandTypeOfPropertySummary.row(Some(additionalAnswers))

        val residentialContent= residentialResult.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        val mixedContent= mixedResult.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        val nonResidentialContent= nonResidentialResult.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        val additionalContent= additionalResult.value.content.asInstanceOf[HtmlContent].asHtml.toString()

        residentialContent mustEqual msgs("land.landTypeOfProperty.01")
        mixedContent mustEqual msgs("land.landTypeOfProperty.02")
        nonResidentialContent mustEqual msgs("land.landTypeOfProperty.03")
        additionalContent mustEqual msgs("land.landTypeOfProperty.04")
      }
    }
  }
}