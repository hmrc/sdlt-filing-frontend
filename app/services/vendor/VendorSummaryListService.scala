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

///*
// * Copyright 2025 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package services.vendor
//
//import models.UserAnswers
//import play.api.mvc.Result
//import play.api.mvc.Results.Redirect
//import pages.vendor.VendorRepresentedByAgentPage
//import viewmodels.checkAnswers.vendor.{AgentAddressSummary, AgentNameSummary, IndividualOrCompanyNameSummary, RepresentedByAnAgentSummary, VendorAddressSummary, VendorTypeSummary}
//import viewmodels.govuk.summarylist.*
//import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
//
//import scala.concurrent.ExecutionContext
//
//class VendorSummaryListService {
//
//  def checkSummaryListRows(userAnswers: UserAnswers, continueRoute: Result)(implicit ex: ExecutionContext) = {
//    val showAgentCYA: Option[Boolean] = userAnswers
//      .get(VendorRepresentedByAgentPage)
//      .map(_.self)
//
//        val showAgentCYACheck = showAgentCYA match {
//          case Some(true) => true
//          case _ => false
//        }
//        val baseRows = Seq(
//          VendorTypeSummary.row(Some(userAnswers)),
//          IndividualOrCompanyNameSummary.row(Some(userAnswers)),
//          VendorAddressSummary.row(Some(userAnswers)),
//          RepresentedByAnAgentSummary.row(Some(userAnswers))
//        )
//        val agentRows = if (showAgentCYACheck) {
//          Seq(
//            AgentNameSummary.row(Some(userAnswers)),
//            AgentAddressSummary.row(Some(userAnswers))
//          )
//        } else {
//          Seq.empty
//        }
//
//        val summaryList = SummaryListViewModel(rows = baseRows ++ agentRows)
//        
//
//  }
//}
