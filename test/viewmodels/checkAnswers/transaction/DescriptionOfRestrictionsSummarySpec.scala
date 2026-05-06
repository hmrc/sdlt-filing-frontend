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

package viewmodels.checkAnswers.transaction

import base.SpecBase
import models.CheckMode
import pages.transaction.DescriptionOfRestrictionsPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class DescriptionOfRestrictionsSummarySpec extends SpecBase {

  "DescriptionOfRestrictionsSummary" - {

    "when the description of restrictions is present" - {

      "must return a Row with value and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val value = "ABCDEFGHIJKLMNOPQRuvwxyz0123456789 -`"

          val userAnswers = emptyUserAnswers
            .set(DescriptionOfRestrictionsPage, value).success.value

          val result = DescriptionOfRestrictionsSummary.row(userAnswers)

          result mustBe a[Row]

          val row = result.asInstanceOf[Row].row

          row.key.content.asHtml.toString() mustEqual msgs("transaction.descriptionOfRestrictions.checkYourAnswersLabel")

          val valueHtml = row.value.content.asHtml.toString()
          valueHtml must include("ABCDEFGHIJKLMNOPQRuvwxyz0123456789 -`")

          row.actions.get.items.size mustEqual 1
          row.actions.get.items.head.href mustEqual controllers.transaction.routes.DescriptionOfRestrictionsController.onPageLoad(CheckMode).url
          row.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          row.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.descriptionOfRestrictions.change.hidden")
        }
      }
    }

    "when the description of restrictions is not present" - {

      "must return a Missing and redirect call to missing page when data is not present" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = DescriptionOfRestrictionsSummary.row(emptyUserAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.transaction.routes.DescriptionOfRestrictionsController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}