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

package services.purchaserAgent

import base.SpecBase
import models.{FullReturn, ReturnAgent}
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.Helpers._

import scala.concurrent.Future

class PurchaserAgentServiceSpec extends SpecBase {

  private val service = new PurchaserAgentService()

  private def emptyFullReturn: FullReturn = FullReturn(
    returnResourceRef = "REF123",
    stornId = "TESTSTORN",
    vendor = None,
    purchaser = None,
    transaction = None,
    returnAgent = None
  )

  val continueRoute: Result = Ok("Continue route")

  "PurchaserAgentService" - {

    "purchaserAgentExistsCheck" - {

      "when fullReturn exists" - {

        "when returnAgent exists with PURCHASER agentType" - {

          "must redirect to ReturnTaskList" in {
            val purchaserAgent = ReturnAgent(
              returnAgentID = Some("AGENT001"),
              agentType = Some("PURCHASER"),
              name = Some("Purchaser Agent Ltd")
            )

            val fullReturn = emptyFullReturn.copy(returnAgent = Some(Seq(purchaserAgent)))
            val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

            val result = service.purchaserAgentExistsCheck(userAnswers, continueRoute)

            redirectLocation(Future.successful(result)) mustBe Some(
              controllers.routes.ReturnTaskListController.onPageLoad().url
            )
          }

          "must redirect to ReturnTaskList when PURCHASER is uppercase" in {
            val purchaserAgent = ReturnAgent(
              returnAgentID = Some("AGENT002"),
              agentType = Some("PURCHASER"),
              name = Some("Another Purchaser Agent")
            )

            val fullReturn = emptyFullReturn.copy(returnAgent = Some(Seq(purchaserAgent)))
            val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

            val result = service.purchaserAgentExistsCheck(userAnswers, continueRoute)

            redirectLocation(Future.successful(result)) mustBe Some(
              controllers.routes.ReturnTaskListController.onPageLoad().url
            )
          }

          "must redirect to ReturnTaskList when multiple agents exist with PURCHASER type" in {
            val vendorAgent = ReturnAgent(
              returnAgentID = Some("AGENT003"),
              agentType = Some("VENDOR"),
              name = Some("Vendor Agent Ltd")
            )

            val purchaserAgent = ReturnAgent(
              returnAgentID = Some("AGENT004"),
              agentType = Some("PURCHASER"),
              name = Some("Purchaser Agent Ltd")
            )

            val fullReturn = emptyFullReturn.copy(returnAgent = Some(Seq(vendorAgent, purchaserAgent)))
            val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

            val result = service.purchaserAgentExistsCheck(userAnswers, continueRoute)

            redirectLocation(Future.successful(result)) mustBe Some(
              controllers.routes.ReturnTaskListController.onPageLoad().url
            )
          }
        }

        "when returnAgent exists but without PURCHASER agentType" - {

          "must continue to next route when agent has VENDOR type" in {
            val vendorAgent = ReturnAgent(
              returnAgentID = Some("AGENT005"),
              agentType = Some("VENDOR"),
              name = Some("Vendor Agent Ltd")
            )

            val fullReturn = emptyFullReturn.copy(returnAgent = Some(Seq(vendorAgent)))
            val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

            val result = service.purchaserAgentExistsCheck(userAnswers, continueRoute)

            result mustBe continueRoute
          }

          "must continue to next route when agent has different type" in {
            val otherAgent = ReturnAgent(
              returnAgentID = Some("AGENT006"),
              agentType = Some("OTHER"),
              name = Some("Other Agent Ltd")
            )

            val fullReturn = emptyFullReturn.copy(returnAgent = Some(Seq(otherAgent)))
            val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

            val result = service.purchaserAgentExistsCheck(userAnswers, continueRoute)

            result mustBe continueRoute
          }

          "must continue to next route when agent has None agentType" in {
            val agentWithoutType = ReturnAgent(
              returnAgentID = Some("AGENT007"),
              agentType = None,
              name = Some("Agent Without Type")
            )

            val fullReturn = emptyFullReturn.copy(returnAgent = Some(Seq(agentWithoutType)))
            val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

            val result = service.purchaserAgentExistsCheck(userAnswers, continueRoute)

            result mustBe continueRoute
          }

          "must continue to next route when multiple agents exist without PURCHASER type" in {
            val vendorAgent = ReturnAgent(
              returnAgentID = Some("AGENT008"),
              agentType = Some("VENDOR"),
              name = Some("Vendor Agent Ltd")
            )

            val otherAgent = ReturnAgent(
              returnAgentID = Some("AGENT009"),
              agentType = Some("OTHER"),
              name = Some("Other Agent Ltd")
            )

            val fullReturn = emptyFullReturn.copy(returnAgent = Some(Seq(vendorAgent, otherAgent)))
            val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

            val result = service.purchaserAgentExistsCheck(userAnswers, continueRoute)

            result mustBe continueRoute
          }
        }

        "when returnAgent list is empty" - {

          "must continue to next route" in {
            val fullReturn = emptyFullReturn.copy(returnAgent = Some(Seq.empty))
            val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

            val result = service.purchaserAgentExistsCheck(userAnswers, continueRoute)

            result mustBe continueRoute
          }
        }

        "when returnAgent is None" - {

          "must continue to next route" in {
            val fullReturn = emptyFullReturn.copy(returnAgent = None)
            val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

            val result = service.purchaserAgentExistsCheck(userAnswers, continueRoute)

            result mustBe continueRoute
          }
        }
      }

      "when fullReturn is None" - {

        "must redirect to ReturnTaskList" in {
          val userAnswers = emptyUserAnswers

          val result = service.purchaserAgentExistsCheck(userAnswers, continueRoute)

          redirectLocation(Future.successful(result)) mustBe Some(
            controllers.routes.ReturnTaskListController.onPageLoad().url
          )
        }
      }
    }
  }
}