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
import models.prelimQuestions.CompanyOrIndividualRequest
import pages.preliminary.PurchaserIsIndividualPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.preliminary.PurchaserIsIndividualSummary

class PurchaserIsIndividualSummarySpec extends SpecBase {

  "PurchaserIsIndividualSummary" - {

    "when purchaser type is Individual" - {

      "must return a summary list row with Individual text and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option1).success.value

          val result = PurchaserIsIndividualSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaserIsIndividual.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs("purchaserIsIndividual.company.value")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.preliminary.routes.PurchaserIsIndividualController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserIsIndividual.change.hidden")
        }
      }
    }

    "when purchaser type is Company" - {

      "must return a summary list row with Company text and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option2).success.value

          val result = PurchaserIsIndividualSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaserIsIndividual.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs("purchaserIsIndividual.individual.value")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.preliminary.routes.PurchaserIsIndividualController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserIsIndividual.change.hidden")
        }
      }
    }

    "when purchaser type data is not present" - {

      "must return a summary list row with a link to enter purchaser type" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PurchaserIsIndividualSummary.row(Some(emptyUserAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaserIsIndividual.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.preliminary.routes.PurchaserIsIndividualController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("purchaserIsIndividual.link.message"))

          result.actions mustBe None
        }
      }

      "must return a summary list row with a link when UserAnswers is None" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PurchaserIsIndividualSummary.row(None)

          result.key.content.asHtml.toString() mustEqual msgs("purchaserIsIndividual.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.preliminary.routes.PurchaserIsIndividualController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("purchaserIsIndividual.link.message"))

          result.actions mustBe None
        }
      }
    }

    "must use CheckMode for the change link" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(PurchaserIsIndividualPage, CompanyOrIndividualRequest.Option1).success.value

        val result = PurchaserIsIndividualSummary.row(Some(userAnswers))

        result.actions.get.items.head.href mustEqual controllers.preliminary.routes.PurchaserIsIndividualController.onPageLoad(CheckMode).url
      }
    }
  }
}