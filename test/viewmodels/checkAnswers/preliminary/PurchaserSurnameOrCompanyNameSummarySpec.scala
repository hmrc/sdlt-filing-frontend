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
import models.CheckMode
import models.prelimQuestions.CompanyOrIndividualRequest
import pages.preliminary.{PurchaserIsIndividualPage, PurchaserSurnameOrCompanyNamePage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class PurchaserSurnameOrCompanyNameSummarySpec extends SpecBase {

  "PurchaserSurnameOrCompanyNameSummary" - {

    "when purchaser name is present" - {

      "must return a summary list row with purchaser label when purchaser is Individual" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option2).success.value
            .set(PurchaserSurnameOrCompanyNamePage, "Smith").success.value

          val result = PurchaserSurnameOrCompanyNameSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("prelim.purchaser.name.checkYourAnswersLabel.purchaser")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "Smith"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.preliminary.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("prelim.purchaser.name.change.hidden")
        }
      }

      "must return a summary list row with company label when purchaser is Company" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option1).success.value
            .set(PurchaserSurnameOrCompanyNamePage, "ACME Corp").success.value

          val result = PurchaserSurnameOrCompanyNameSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("prelim.purchaser.name.checkYourAnswersLabel.company")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "ACME Corp"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.preliminary.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("prelim.purchaser.name.change.hidden")
        }
      }

      "must return a summary list row with default label when purchaser type is not set" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserSurnameOrCompanyNamePage, "Test Name").success.value

          val result = PurchaserSurnameOrCompanyNameSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("prelim.purchaser.name.checkYourAnswersLabel.default")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "Test Name"
        }
      }

      "must properly escape special characters in name" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option1).success.value
            .set(PurchaserSurnameOrCompanyNamePage, "O'Brien & Sons <Ltd>").success.value

          val result = PurchaserSurnameOrCompanyNameSummary.row(Some(userAnswers))

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("O&#x27;Brien")
          htmlContent must include("&amp;")
          htmlContent must include("&lt;")
          htmlContent must include("&gt;")
        }
      }
    }

    "when purchaser name is missing" - {

      "must return a summary list row with a link to add the name and no actions" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option2).success.value


          val result = PurchaserSurnameOrCompanyNameSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual
            msgs("prelim.purchaser.name.checkYourAnswersLabel.purchaser")

          val html = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()

          html must include(
            controllers.preliminary.routes.PurchaserSurnameOrCompanyNameController
              .onPageLoad(CheckMode).url
          )

          html must include(msgs("prelim.purchaser.name.link.message"))

          result.actions mustBe None
        }
      }
    }

    "must use CheckMode for the change link" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option1).success.value
          .set(PurchaserSurnameOrCompanyNamePage, "Smith").success.value

        val result = PurchaserSurnameOrCompanyNameSummary.row(Some(userAnswers))

        result.actions.get.items.head.href mustEqual controllers.preliminary.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(CheckMode).url
      }
    }
  }
}