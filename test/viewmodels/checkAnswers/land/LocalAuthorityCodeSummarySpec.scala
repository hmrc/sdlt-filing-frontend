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
import pages.land.LocalAuthorityCodePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class LocalAuthorityCodeSummarySpec extends SpecBase {

  "LocalAuthorityCodeSummary" - {

    "when Local Authority Code is valid " - {

      "must return a summary list row with when Local Authority Code is valid" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(LocalAuthorityCodePage, "1234").success.value

          val result = LocalAuthorityCodeSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("land.localAuthorityCode.checkYourAnswersLabel")

          result.actions.get.items.head.href mustEqual controllers.land.routes.LocalAuthorityCodeController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("land.localAuthorityCode.change.hidden")
        }
      }

      "must return a summary list row with a link to enter loca authority code when userAnswers is empty" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers

          val result = LocalAuthorityCodeSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("land.localAuthorityCode.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()

          htmlContent must include("govuk-link")
          htmlContent must include(controllers.land.routes.LocalAuthorityCodeController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("land.checkYourAnswers.localAuthorityCode.missing"))
          result.actions mustBe None
        }
      }
    }

  }
}
