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

package viewmodels.checkAnswers.purchaser

import base.SpecBase
import models.CheckMode
import pages.purchaser.PurchaserDateOfBirthPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

import java.time.LocalDate

class PurchaserDateOfBirthSummarySpec extends SpecBase {

  "PurchaserDateOfBirthSummary" - {

    "must return a summary list row with the date of birth" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(PurchaserDateOfBirthPage, LocalDate.of(2000, 10, 26)).success.value

        val row = PurchaserDateOfBirthSummary.row(Some(userAnswers))
        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        result.key.content.asHtml.toString() mustEqual msgs("purchaser.dateOfBirth.checkYourAnswersLabel")

        val htmlContent = result.value.content.asInstanceOf[Text].asHtml.toString()
        htmlContent mustEqual msgs("26 October 2000")

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserDateOfBirthController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.dateOfBirth.change.hidden")
      }
    }

    "must return a Missing and redirect call to missing page when data is not present" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers

        val result = PurchaserDateOfBirthSummary.row(Some(userAnswers))

        result match {
          case Missing(call) =>
            call mustEqual controllers.purchaser.routes.PurchaserDateOfBirthController.onPageLoad(CheckMode)

          case Row(_) =>
            fail("Expected Missing but got Row")
        }
      }
    }

  }
}