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
import models.{FullReturn, ReturnAgent, ReturnInfo, UserAnswers, Vendor}
import play.api.mvc.Results.Ok
import play.api.mvc.{Result, Results}
import play.api.test.Helpers.*
import services.vendorAgent.AgentChecksService

import scala.concurrent.Future

class AgentChecksServiceSpec extends SpecBase {

  val service = new AgentChecksService()

  val continueRoute: Result = Ok("Continue route")

  "AgentChecksService" - {

    "checkMainVendorAgentRepresentedByAgent" - {

      "must continue when no return agent or vendors" in {
        val fullReturn = completeFullReturn.copy(returnAgent = None, vendor = None)

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.checkMainVendorAgentRepresentedByAgent(userAnswers, continueRoute)
          result mustBe continueRoute
        }
      }

      "must continue when return agent exists and no vendors" in {
        val fullReturn = completeFullReturn.copy(vendor = None)

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        running(application) {
          val result = service.checkMainVendorAgentRepresentedByAgent(userAnswers, continueRoute)
          result mustBe continueRoute
        }
      }

      "must redirect to VendorCYA when agent is type VENDOR and main vendor is represented by agent" in {
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
          returnAgent = Some(Seq(ReturnAgent(agentType = Some("VENDOR")))),
          vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("true"))))
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.checkMainVendorAgentRepresentedByAgent(userAnswers, continueRoute)
          redirectLocation(Future.successful(result)) mustBe Some(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url)
        }
      }

      "must redirect to VendorCYA when agent is not type VENDOR and main vendor is not represented by agent" in { // Change to redirect to CYA
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
          returnAgent = Some(Seq(ReturnAgent(agentType = Some("SOLICITOR")))),
          vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("false"))))
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.checkMainVendorAgentRepresentedByAgent(userAnswers, continueRoute)
          redirectLocation(Future.successful(result)) mustBe Some(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url)
        }
      }

      "must redirect to VendorCYA there is no return agent and main vendor is not represented by agent" in {
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
          returnAgent = None,
          vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("false"))))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.checkMainVendorAgentRepresentedByAgent(userAnswers, continueRoute)
          redirectLocation(Future.successful(result)) mustBe Some(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url)
        }
      }

      "must redirect to error page when agent is type VENDOR and main vendor is not represented by agent" in { // Change to actual error page (TBC)
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
          returnAgent = Some(Seq(ReturnAgent(agentType = Some("VENDOR")))),
          vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("false"))))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.checkMainVendorAgentRepresentedByAgent(userAnswers, continueRoute)
          redirectLocation(Future.successful(result)) mustBe Some(controllers.routes.GenericErrorController.onPageLoad().url)
        }
      }

      "must redirect to error page when there is no return agent and main vendor is represented by agent" in { // Change to actual error page (TBC)
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
          returnAgent = None,
          vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("true"))))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.checkMainVendorAgentRepresentedByAgent(userAnswers, continueRoute)
          redirectLocation(Future.successful(result)) mustBe Some(controllers.routes.GenericErrorController.onPageLoad().url)
        }
      }

      "must redirect to error page when agent is not type VENDOR and main vendor is represented by agent" in { // Change to actual error page (TBC)
        val fullReturn = completeFullReturn.copy(
          returnInfo = Some(ReturnInfo(mainVendorID = Some("123"))),
          returnAgent = Some(Seq(ReturnAgent(agentType = Some("SOLICITOR")))),
          vendor = Some(Seq(Vendor(vendorID = Some("123"), isRepresentedByAgent = Some("true"))))
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.checkMainVendorAgentRepresentedByAgent(userAnswers, continueRoute)
          redirectLocation(Future.successful(result)) mustBe Some(controllers.routes.GenericErrorController.onPageLoad().url)
        }
      }

      "must redirect to error page when agent is type VENDOR and there are no vendors" in { // Change to actual error page (TBC)
        val fullReturn = completeFullReturn.copy(
          returnAgent = Some(Seq(ReturnAgent(agentType = Some("VENDOR")))),
          vendor = None
        )
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = Some(fullReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.checkMainVendorAgentRepresentedByAgent(userAnswers, continueRoute)
          redirectLocation(Future.successful(result)) mustBe Some(controllers.routes.GenericErrorController.onPageLoad().url)
        }
      }

      "must redirect to journey recovery page when full return doesn't exist" in {
        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN", fullReturn = None)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val result = service.checkMainVendorAgentRepresentedByAgent(userAnswers, continueRoute)
          redirectLocation(Future.successful(result)) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }
  }
}