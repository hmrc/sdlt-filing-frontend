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

package services.vendorAgent

import base.SpecBase
import models.{Agent, FullReturn, NormalMode, ReturnAgent}
import play.api.mvc.Result
import play.api.test.Helpers.*
import play.api.mvc.Results.Ok
import play.api.test.Helpers.redirectLocation


import scala.concurrent.{ExecutionContext, Future}

class AgentChecksServiceSpec extends SpecBase {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val service = new AgentChecksService()

  private def emptyFullReturn: FullReturn = FullReturn(
    returnResourceRef = "REF123",
    stornId = "TESTSTORN",
    vendor = None,
    purchaser = None,
    transaction = None,
    returnAgent = None
  )

  val testAgents: Seq[Agent] = Seq(Agent(
    storn = Some("STN001"),
    agentId = Some("AGT001"),
    name = Some("Joe Smith"),
    houseNumber = None,
    address1 = Some("123 Street"),
    address2 = Some("Town"),
    address3 = Some("City"),
    address4 = Some("County"),
    postcode = Some("AA1 1AA"),
    phone = Some("0123456789"),
    email = Some("test@example.com"),
    dxAddress = Some("yes"),
    agentResourceReference = Some("REF001")
  ),
    Agent(
      storn = Some("STN001"),
      agentId = Some("AGT002"),
      name = Some("Sarah Jones"),
      houseNumber = None,
      address1 = Some("456 Street"),
      address2 = Some("Town"),
      address3 = None,
      address4 = Some("County"),
      postcode = Some("AA2 2AA"),
      phone = Some("0987654321"),
      email = Some("sarah@example.com"),
      dxAddress = Some("yes"),
      agentResourceReference = Some("REF001")
    )
  )

  val continueRoute: Result = Ok("Continue route")


  "AgentChecksService" - {

    "when fullReturn exists" - {

      "when returnAgent exists with Vendor agentType" - {

        "must redirect to ReturnTaskList" in {
          val vendorAgent = ReturnAgent(
            returnAgentID = Some("AGENT001"),
            agentType = Some("VENDOR"),
            name = Some("Vendor Agent Ltd")
          )

          val fullReturn = emptyFullReturn.copy(returnAgent = Some(Seq(vendorAgent)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute, NormalMode)

          redirectLocation(Future.successful(result)) mustBe Some(
            controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
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

          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute,NormalMode)

          redirectLocation(Future.successful(result)) mustBe Some(
            controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
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

          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute,NormalMode)

          redirectLocation(Future.successful(result)) mustBe Some(
            controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url)
        }

        "must continue to next route when agent has different type" in {
          val otherAgent = ReturnAgent(
            returnAgentID = Some("AGENT006"),
            agentType = Some("OTHER"),
            name = Some("Other Agent Ltd")
          )

          val fullReturn = emptyFullReturn.copy(returnAgent = Some(Seq(otherAgent)))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute,NormalMode)

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

          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute,NormalMode)

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

          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute,NormalMode)

          redirectLocation(Future.successful(result)) mustBe Some(
            controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url)

        }
      }

      "when returnAgent list is empty" - {

        "must continue to next route" in {
          val fullReturn = emptyFullReturn.copy(returnAgent = Some(Seq.empty))
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute, NormalMode)

          result mustBe continueRoute
        }
      }

      "when returnAgent is None" - {

        "must continue to next route" in {
          val fullReturn = emptyFullReturn.copy(returnAgent = None)
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val result = service.vendorAgentExistsCheck(userAnswers, continueRoute, NormalMode)

          result mustBe continueRoute
        }
      }
    }

    "when fullReturn is None" - {

      "must redirect to ReturnTaskList" in {
        val userAnswers = emptyUserAnswers

        val result = service.vendorAgentExistsCheck(userAnswers, continueRoute, NormalMode)

        redirectLocation(Future.successful(result)) mustBe Some(
          controllers.routes.ReturnTaskListController.onPageLoad().url
        )
      }
    }
  }

}
