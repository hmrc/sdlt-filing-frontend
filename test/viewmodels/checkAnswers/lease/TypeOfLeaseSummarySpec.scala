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
import models.lease.TypeOfLease
import pages.lease.TypeOfLeasePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}


class TypeOfLeaseSummarySpec extends SpecBase {

  "TypeOfLeaseSummarySpec" - {

    "when type of lease answer is present  " - {

      "must return a SummaryListRow lease type value and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(TypeOfLeasePage, TypeOfLease.R).success.value

          val row = TypeOfLeaseSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("lease.typeOfLease.checkYourAnswersLabel")
          
          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs(s"lease.typeOfLease.${TypeOfLease.R}")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.lease.routes.TypeOfLeaseController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("lease.typeOfLease.change.hidden")
        }
      }

      "must display the correct message for different lease types" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val leaseTypes = Seq(
            TypeOfLease.R,
            TypeOfLease.N,
            TypeOfLease.M
          )

          leaseTypes.foreach { leaseType =>
            val userAnswers = emptyUserAnswers
              .set(TypeOfLeasePage, leaseType).success.value

            val row = TypeOfLeaseSummary.row(userAnswers)

            val result = row match {
              case Row(r) => r
              case _ => fail("Expected Row but got Missing")
            }

            val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
            htmlContent mustEqual msgs(s"lease.typeOfLease.$leaseType")
          }
        }
      }
    }

    "when type of lease is not present" - {

      "must return a Missing and redirect call to missing page when data is missing" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = TypeOfLeaseSummary.row(emptyUserAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.lease.routes.TypeOfLeaseController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}

