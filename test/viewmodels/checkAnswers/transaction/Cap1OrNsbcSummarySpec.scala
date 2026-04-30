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
import pages.transaction.Cap1OrNsbcPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}


class Cap1OrNsbcSummarySpec extends SpecBase {

  "Cap1OrNsbcSummarySpec" - {

    "when CAP1 or NSBC data is present" - {

      "must return a SummaryListRow with 'yes' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(Cap1OrNsbcPage, true).success.value

          val row = Cap1OrNsbcSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("transaction.cap1OrNsbc.checkYourAnswersLabel")

          val contentString = result.value.content.asHtml.toString()

          contentString mustEqual msgs("site.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.Cap1OrNsbcController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.cap1OrNsbc.change.hidden")
        }
      }

      "must return a SummaryListRow with 'no' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(Cap1OrNsbcPage, false).success.value

          val row = Cap1OrNsbcSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("transaction.cap1OrNsbc.checkYourAnswersLabel")

          val contentString = result.value.content.asHtml.toString()

          contentString mustEqual msgs("site.no")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.Cap1OrNsbcController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.cap1OrNsbc.change.hidden")
        }
      }
    }

    "when CAP1 or NSBC data is not present" - {

      "must return a Missing and redirect call to missing page when data is not present" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = Cap1OrNsbcSummary.row(emptyUserAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.transaction.routes.Cap1OrNsbcController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}

