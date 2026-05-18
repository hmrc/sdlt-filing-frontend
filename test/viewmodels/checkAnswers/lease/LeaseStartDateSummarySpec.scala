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

package viewmodels.checkAnswers.lease

import base.SpecBase
import models.CheckMode
import pages.lease.LeaseStartDatePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

import java.time.LocalDate

class LeaseStartDateSummarySpec extends SpecBase {

  "LeaseStartDateSummarySpec" - {

    "when start date of lease answer is present  " - {

      "must return a SummaryListRow lease start date value and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(LeaseStartDatePage, LocalDate.of(2016, 10, 26)).success.value

          val row = LeaseStartDateSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("lease.leaseStartDate.checkYourAnswersLabel")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.lease.routes.LeaseStartDateController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("lease.leaseStartDate.change.hidden")
        }
      }
    }

    "when start date of lease is not present" - {

      "must return a Missing and redirect call to missing page when data is missing" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = LeaseStartDateSummary.row(emptyUserAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.lease.routes.LeaseStartDateController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}