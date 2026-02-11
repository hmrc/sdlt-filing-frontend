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

package services.vendorAgent

import models.{AgentType, Mode, NormalMode, UserAnswers}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

class AgentChecksService {

  def vendorAgentExistsCheck(userAnswers: UserAnswers, continueRoute: Result, mode: Mode): Result = {

    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        if (fullReturn.returnAgent.exists(_.exists(_.agentType.contains(AgentType.Vendor.toString))) && mode == NormalMode) {
          Redirect(controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad())
        } else {
          continueRoute
        }
      case _ => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
    }
  }
}
