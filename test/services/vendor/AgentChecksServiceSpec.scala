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

import base.SpecBase
import constants.FullReturnConstants.*
import models.{NormalMode, CheckMode, ReturnAgent, UserAnswers}
import play.api.mvc.Results.Ok
import play.api.mvc.Result
import play.api.test.Helpers.*
import services.vendorAgent.AgentChecksService

import scala.concurrent.Future

class AgentChecksServiceSpec extends SpecBase {

  val service = new AgentChecksService()

  val continueRoute: Result = Ok("Continue route")

  "AgentChecksService" - {

    "vendorAgentExistsCheck" - {

      "must redirect to Vendor Overview when vendor agent exists" in {
        val fullReturn = completeFullReturn.copy(
          returnAgent = Some(Seq(ReturnAgent(agentType = Some("VENDOR"))))
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute, NormalMode)
          redirectLocation(Future.successful(result)) mustBe Some(controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url)
        }
      }

      "must continue when in check mode" in {
        val fullReturn = completeFullReturn.copy(
          returnAgent = Some(Seq(ReturnAgent(agentType = Some("VENDOR"))))
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute, CheckMode)
          result mustBe continueRoute
        }
      }

      "must continue when no vendor agent exists" in {
        val fullReturn = completeFullReturn.copy(
          returnAgent = Some(Seq(ReturnAgent(agentType = Some("PURCHASER"))))
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute, NormalMode)
          result mustBe continueRoute
        }
      }

      "must redirect to Return Task List when no full return exists" in {
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = None)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute, NormalMode)
          redirectLocation(Future.successful(result)) mustBe Some(controllers.routes.ReturnTaskListController.onPageLoad().url)
        }
      }
    }
  }
}