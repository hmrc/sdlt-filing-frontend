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

import models.FullReturn
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

case class Summaries (purchaserSummaryCards: Option[Seq[SummaryList]],
                      purchaserAgentSummaryCard: Option[SummaryList],
                      vendorSummaryCards: Option[Seq[SummaryList]],
                      vendorAgentSummaryCard: Option[SummaryList],
                      landSummaryCards: Option[Seq[SummaryList]],
                      ukResidencySummaryCard: Option[SummaryList],
                      transactionSummaryCard: Option[SummaryList],
                      leaseSummaryCard: Option[SummaryList],
                      taxCalcSummaryCard: Option[SummaryList])

object Summaries {
  def from(fullReturn: FullReturn)(implicit messages: Messages) =
    Summaries(
      PurchaserSummary.getSummaryCards(fullReturn),
      PurchaserAgentSummary.getSummaryCard(fullReturn),
      VendorSummary.getSummaryCards(fullReturn),
      VendorAgentSummary.getSummaryCard(fullReturn),
      LandSummary.getSummaryCards(fullReturn),
      UkResidencySummary.getSummaryCard(fullReturn),
      TransactionSummary.getSummaryCard(fullReturn),
      LeaseSummary.getSummaryCard(fullReturn),
      TaxCalcSummary.getSummaryCard(fullReturn)
    )
}
