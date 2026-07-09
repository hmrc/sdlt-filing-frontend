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

import models.{FullReturn}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist.*
import viewmodels.submission.summary.SummaryUtil.*

object UkResidencySummary {

  def getSummaryCard(fullReturn: FullReturn)(implicit messages: Messages): Option[SummaryList] = {
    fullReturn.residency.flatMap { ukResidency =>
      ukResidency.isNonUkResidents.map { isNonUkResident =>
        SummaryListViewModel(
          Seq(
            getOptSummaryRow(
              messages("ukResidency.closeCompany.checkYourAnswersLabel"),
              getOptYesNo(ukResidency.isCloseCompany)
            ),
            getOptSummaryRow(
              messages("ukResidency.crownEmploymentRelief.checkYourAnswersLabel"),
              getOptYesNo(ukResidency.isCrownRelief)
            )
          ).flatMap(_.toSeq)
        ).withCard(
          messages(
            "submission.completedSdltReturn.ukResidency.header",
            if isNonUkResident.equalsIgnoreCase("YES") then messages("ukResidency.nonUkResident")
            else messages("ukResidency.ukResident")
          )
        )
      }
    }
  }
}
