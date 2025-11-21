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

package services.vendor

import models.UserAnswers
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

class AgentChecksService {
  
  def checkMainVendorAgentRepresentedByAgent(userAnswers: UserAnswers, continueRoute: Result): Result = {
    userAnswers.fullReturn.map { fullReturn =>
      val returnAgentExists = fullReturn.returnAgent.isDefined
      val isAgentTypeVendor = fullReturn.returnAgent.exists(_.exists(_.agentType.contains("VENDOR")))
      val mainVendorId: Option[String] = fullReturn.returnInfo.flatMap(_.mainVendorID)
      val mainVendor = fullReturn.vendor.flatMap(_.find(_.vendorID == mainVendorId))
      val mainVendorExists = mainVendor.isDefined
      val mainVendorIsRepresentedByAgent = mainVendor.flatMap(_.isRepresentedByAgent).exists(_.equals("true"))

      // TODO Change return task list routes to vendor CYA
      (returnAgentExists, isAgentTypeVendor, mainVendorExists, mainVendorIsRepresentedByAgent) match {
        case (false, _, false, _) => continueRoute
        case (true, false, false, _) => continueRoute
        case (true, true, true, true) => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
        case (true, false, true, false) => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
        case (false, _, true, false) => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
        case (true, true, true, false) => Redirect(controllers.routes.GenericErrorController.onPageLoad())
        case (false, _, true, true) => Redirect(controllers.routes.GenericErrorController.onPageLoad())
        case (true, false, true, true) => Redirect(controllers.routes.GenericErrorController.onPageLoad())
        case (true, true, false, _) => Redirect(controllers.routes.GenericErrorController.onPageLoad())
      }
    }.getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }
}
