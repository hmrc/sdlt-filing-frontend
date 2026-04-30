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
import constants.FullReturnConstants
import models.{CheckMode, FullReturn}
import pages.ukResidency.CloseCompanyPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class CloseCompanySummarySpec extends SpecBase {

  private val fullReturnWithCompany: FullReturn =
    FullReturnConstants.completeFullReturn.copy(
      purchaser = Some(Seq(FullReturnConstants.completePurchaser3))
    )

  private val fullReturnWithIndividual: FullReturn =
    FullReturnConstants.completeFullReturn.copy(
      purchaser = Some(Seq(FullReturnConstants.completePurchaser1))
    )

  "CloseCompanySummary" - {

    "when at least one purchaser is a company" - {

      "and the close company answer is true" - {

        "must return Some SummaryListRow with 'yes' text and change link" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            implicit val msgs: Messages = messages(application)

            val userAnswers = emptyUserAnswers
              .copy(fullReturn = Some(fullReturnWithCompany))
              .set(CloseCompanyPage, true).success.value

            val result = CloseCompanySummary.row(userAnswers).value

            result.key.content.asHtml.toString mustEqual msgs("ukResidency.closeCompany.checkYourAnswersLabel")

            result.value.content.asInstanceOf[Text].asHtml.toString mustEqual msgs("site.yes")

            result.actions.get.items.size mustEqual 1
            result.actions.get.items.head.href mustEqual
              controllers.ukResidency.routes.CloseCompanyController.onPageLoad(CheckMode).url

            result.actions.get.items.head.content.asHtml.toString must include(msgs("site.change"))

            result.actions.get.items.head.visuallyHiddenText.value mustEqual
              msgs("ukResidency.closeCompany.change.hidden")
          }
        }
      }

      "and the close company answer is false" - {

        "must return Some SummaryListRow with 'no' text and change link" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            implicit val msgs: Messages = messages(application)

            val userAnswers = emptyUserAnswers
              .copy(fullReturn = Some(fullReturnWithCompany))
              .set(CloseCompanyPage, false).success.value

            val result = CloseCompanySummary.row(userAnswers).value

            result.key.content.asHtml.toString mustEqual msgs("ukResidency.closeCompany.checkYourAnswersLabel")

            result.value.content.asInstanceOf[Text].asHtml.toString mustEqual msgs("site.no")

            result.actions.get.items.size mustEqual 1
            result.actions.get.items.head.href mustEqual
              controllers.ukResidency.routes.CloseCompanyController.onPageLoad(CheckMode).url

            result.actions.get.items.head.content.asHtml.toString must include(msgs("site.change"))

            result.actions.get.items.head.visuallyHiddenText.value mustEqual
              msgs("ukResidency.closeCompany.change.hidden")
          }
        }
      }

      "and the close company answer is not present" - {

        "must return Some SummaryListRow with a missing link" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            implicit val msgs: Messages = messages(application)

            val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithCompany))

            val result = CloseCompanySummary.row(userAnswers).value

            result.key.content.asHtml.toString mustEqual msgs("ukResidency.closeCompany.checkYourAnswersLabel")

            val valueHtml = result.value.content.asHtml.toString
            valueHtml must include(controllers.ukResidency.routes.CloseCompanyController.onPageLoad(CheckMode).url)
            valueHtml must include(msgs("ukResidency.closeCompany.missing"))
            valueHtml must include("govuk-link")

            result.actions mustBe None
          }
        }
      }
    }

    "when no purchaser is a company" - {

      "must return None" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturnWithIndividual))
            .set(CloseCompanyPage, true).success.value

          CloseCompanySummary.row(userAnswers) mustBe None
        }
      }
    }

    "when fullReturn is missing" - {

      "must return None" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(CloseCompanyPage, true).success.value

          CloseCompanySummary.row(userAnswers) mustBe None
        }
      }
    }
  }
}