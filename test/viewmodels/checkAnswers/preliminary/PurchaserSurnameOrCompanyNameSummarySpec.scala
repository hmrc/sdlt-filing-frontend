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

package viewmodels.checkAnswers.preliminary

import base.SpecBase
import controllers.routes
import models.CheckMode
import models.prelimQuestions.BusinessOrIndividualRequest
import pages.preliminary.{PurchaserIsIndividualPage, PurchaserSurnameOrCompanyNamePage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import viewmodels.checkAnswers.preliminary.PurchaserSurnameOrCompanyNameSummary

class PurchaserSurnameOrCompanyNameSummarySpec extends SpecBase {

  "PurchaserSurnameOrCompanyNameSummary" - {

    "when purchaser name is present" - {

      "must return a summary list row with purchaser label when purchaser is Individual" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserIsIndividualPage, BusinessOrIndividualRequest.Option1).success.value
            .set(PurchaserSurnameOrCompanyNamePage, "Smith").success.value

          val result = PurchaserSurnameOrCompanyNameSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaserSurnameOrCompanyName.checkYourAnswersLabel.purchaser")

          val textContent = result.value.content.asInstanceOf[Text].asHtml.toString()
          textContent mustEqual "Smith"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.preliminary.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserSurnameOrCompanyName.change.hidden")
        }
      }

      "must return a summary list row with business label when purchaser is Business" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserIsIndividualPage, BusinessOrIndividualRequest.Option2).success.value
            .set(PurchaserSurnameOrCompanyNamePage, "ACME Corp").success.value

          val result = PurchaserSurnameOrCompanyNameSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaserSurnameOrCompanyName.checkYourAnswersLabel.business")

          val textContent = result.value.content.asInstanceOf[Text].asHtml.toString()
          textContent mustEqual "ACME Corp"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.preliminary.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserSurnameOrCompanyName.change.hidden")
        }
      }

      "must return a summary list row with default label when purchaser type is not set" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserSurnameOrCompanyNamePage, "Test Name").success.value

          val result = PurchaserSurnameOrCompanyNameSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaserSurnameOrCompanyName.checkYourAnswersLabel.default")

          val textContent = result.value.content.asInstanceOf[Text].asHtml.toString()
          textContent mustEqual "Test Name"
        }
      }

      "must properly escape special characters in name" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserIsIndividualPage, BusinessOrIndividualRequest.Option1).success.value
            .set(PurchaserSurnameOrCompanyNamePage, "O'Brien & Sons <Ltd>").success.value

          val result = PurchaserSurnameOrCompanyNameSummary.row(Some(userAnswers))

          val textContent = result.value.content.asInstanceOf[Text].asHtml.toString()
          textContent must include("O&amp;#x27;Brien")
          textContent must include("&amp;amp;")
          textContent must include("&amp;lt;")
          textContent must include("&amp;gt;")
        }
      }
    }

    "must use CheckMode for the change link" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(PurchaserIsIndividualPage, BusinessOrIndividualRequest.Option1).success.value
          .set(PurchaserSurnameOrCompanyNamePage, "Smith").success.value

        val result = PurchaserSurnameOrCompanyNameSummary.row(Some(userAnswers))

        result.actions.get.items.head.href mustEqual controllers.preliminary.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(CheckMode).url
      }
    }
  }
}