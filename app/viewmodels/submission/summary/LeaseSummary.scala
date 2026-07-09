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

package viewmodels.submission.summary

import models.lease.TypeOfLease
import models.FullReturn
import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist.*
import viewmodels.submission.summary.SummaryUtil.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

object LeaseSummary {

  def getSummaryCard(fullReturn: FullReturn)(implicit messages: Messages): Option[SummaryList] = {
    implicit val lang: Lang = messages.lang
    fullReturn.lease.flatMap { lease =>
      TypeOfLease.parse(lease.leaseType).map { leaseType =>
        SummaryListViewModel(
          Seq(
            getOptSummaryRow(
              messages("lease.leaseStartDate.checkYourAnswersLabel"),
              lease.contractStartDate.flatMap(startDate =>
                Try(
                  LocalDate.parse(startDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")).format(dateTimeFormat())
                ).toOption
              )
            ),
            getOptSummaryRow(
              messages("lease.leaseEndDate.checkYourAnswersLabel"),
              lease.contractEndDate.flatMap(endDate =>
                Try(
                  LocalDate.parse(endDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")).format(dateTimeFormat())
                ).toOption
              )
            ),
            getOptSummaryRow(
              messages("lease.enterRentFreePeriod.checkYourAnswersLabel"),
              lease.rentFreePeriod
            ),
            getOptSummaryRow(
              messages("lease.annualStartingRent.checkYourAnswersLabel"),
              getOptMoneyValue(lease.startingRent)
            ),
            getOptSummaryRow(
              messages("lease.leaseStartingRentEndDate.checkYourAnswersLabel"),
              lease.startingRentEndDate.flatMap(endDate =>
                Try(
                  LocalDate.parse(endDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")).format(dateTimeFormat())
                ).toOption
              )
            ),
            getOptSummaryRow(
              messages("lease.laterRent.checkYourAnswersLabel"),
              getOptYesNo(lease.laterRentKnown)
            ),
            getOptSummaryRow(
              messages("lease.leaseThousandPoundsThreshold.checkYourAnswersLabel"),
              getOptYesNo(lease.isAnnualRentOver1000)
            ),
            getOptSummaryRow(
              messages("lease.enterAnnualRentVat.checkYourAnswersLabel"),
              getOptMoneyValue(lease.VATAmount)
            ),
            getOptSummaryRow(
              messages("lease.enterTotalPremiumPayable.checkYourAnswersLabel"),
              getOptMoneyValue(lease.totalPremiumPayable)
            ),
            getOptSummaryRow(
              messages("lease.netPresentValue.checkYourAnswersLabel"),
              getOptMoneyValue(lease.netPresentValue)
            )
          ).flatMap(_.toSeq)
        ).withCard(
          messages(
            "submission.completedSdltReturn.lease.header",
            messages(s"lease.typeOfLease.${leaseType.toString}")
          )
        )
      }
    }
  }
}
