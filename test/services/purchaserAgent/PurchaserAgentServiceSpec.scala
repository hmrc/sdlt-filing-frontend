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
import models.address.Address
import models.purchaserAgent.{PurchaserAgentsContactDetails, SelectPurchaserAgent}
import models.{FullReturn, NormalMode, ReturnAgent, UserAnswers, Agent}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.Helpers.*
import repositories.SessionRepository
import navigation.Navigator
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.{verify, when}
import pages.purchaserAgent.{AddContactDetailsForPurchaserAgentPage, PurchaserAgentAddressPage, PurchaserAgentNamePage, PurchaserAgentsContactDetailsPage, SelectPurchaserAgentPage}

import scala.concurrent.{ExecutionContext, Future}

class PurchaserAgentServiceSpec extends SpecBase {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockNavigator = mock[Navigator]

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val service = new PurchaserAgentService(
    sessionRepository = mockSessionRepository, navigator = mockNavigator
  )

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

    "populatePurchaserAgentInSession" - {

      "must update user answers if agentId found" in {

        val fullReturn = emptyFullReturn.copy(stornId = "STN001")
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))
        val testAgent: Agent = Agent(
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
          agentResourceReference = Some("REF001"))

        val expectedAgentAddress = Address(
          line1 = testAgent.address1.getOrElse(""),
          line2 = testAgent.address2,
          line3 = testAgent.address3,
          line4 = testAgent.address4,
          postcode = testAgent.postcode
        )

        val expectedContactDetails = PurchaserAgentsContactDetails(
          phoneNumber = testAgent.phone, emailAddress = testAgent.email
        )

        val expectedUserAnswers = for {
          withName <- userAnswers.set(PurchaserAgentNamePage, testAgent.name.getOrElse(""))
          withAddAddress <- withName.set(AddContactDetailsForPurchaserAgentPage, true)
          withAddress <- withAddAddress.set(PurchaserAgentAddressPage, expectedAgentAddress)
          finalAnswers <- withAddress.set(PurchaserAgentsContactDetailsPage, expectedContactDetails)
        } yield finalAnswers


        val result = service.populatePurchaserAgentInSession(testAgent, userAnswers)
        result mustBe expectedUserAnswers
      }

      "must throw IllegalStateException if agent not found" in {

        val fullReturn = emptyFullReturn.copy(stornId = "STN001")
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))
        val testAgent: Agent = Agent(
          storn = Some("STN001"),
          agentId = None,
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
          agentResourceReference = Some("REF001"))

        val result = service.populatePurchaserAgentInSession(testAgent, userAnswers)
        result.isFailure mustBe true
        result.failed.get mustBe a[IllegalStateException]
      }

    }

    "clearPurchaserAgentAnswers" - {

      "must clear user answers of purchaser agent details" in {

        val expectedAgentAddress = Address(
          line1 = "123 Street",
          line2 = Some("Town"),
          line3 = Some("City"),
          line4 = Some("County"),
          postcode = Some("AA1 1AA"),
        )

        val expectedContactDetails = PurchaserAgentsContactDetails(
          phoneNumber = Some("0123456789"),
          emailAddress = Some("test@example.com")
        )

        val testUserAnswers: UserAnswers = emptyUserAnswers
          .set(PurchaserAgentNamePage, "John Smith").success.value
          .set(PurchaserAgentAddressPage, expectedAgentAddress).success.value
          .set(PurchaserAgentsContactDetailsPage, expectedContactDetails).success.value

        val result = service.clearPurchaserAgentAnswers(testUserAnswers)
        val clearedAnswers = result.get
        clearedAnswers.get(PurchaserAgentNamePage) mustBe None
        clearedAnswers.get(PurchaserAgentAddressPage) mustBe None
        clearedAnswers.get(PurchaserAgentsContactDetailsPage) mustBe None
      }

    }

    "agentSummaryList" - {

      "must create summary list of agents" in {
        val result = service.agentSummaryList(testAgents)
        val expectedResult: Seq[(Option[String], Option[String], Option[String])] = Seq(
          (Some("Joe Smith"), Some("City"), Some("AGT001")),
          (Some("Sarah Jones"), None, Some("AGT002"))
        )
        result mustBe expectedResult
      }
    }

    "handleAgentSelection" - {

      "when addNewAgent is selected" - {

        "must clear purchaser agent answers and redirect to Agent Name page" in {
          val updatedAnswers = emptyUserAnswers
            .set(SelectPurchaserAgentPage, SelectPurchaserAgent.AddNewAgent.toString).success.value
            .set(PurchaserAgentNamePage, "John Smith").success.value

          when(mockSessionRepository.set(any()))
            .thenReturn(Future.successful(true))

          val result = service.handleAgentSelection(
            SelectPurchaserAgent.AddNewAgent.toString,
            agentList = Seq.empty,
            userAnswers = updatedAnswers,
            mode = NormalMode
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url

          verify(mockSessionRepository).set(argThat { answers =>
            answers.get(PurchaserAgentNamePage).isEmpty &&
              answers.get(PurchaserAgentAddressPage).isEmpty &&
              answers.get(PurchaserAgentsContactDetailsPage).isEmpty
          })
        }
      }

      "when selected agentId does not exist" - {

        "must redirect to journey recovery" in {
          val result = service.handleAgentSelection(
            value = "UNKNOWN_AGENT",
            agentList = Seq.empty,
            userAnswers = emptyUserAnswers,
            mode = NormalMode
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "when selected agent exists with valid Id" - {

        "must populate session and redirect using navigator" in {
          val updatedAnswers = emptyUserAnswers
            .set(SelectPurchaserAgentPage, "AGT001").success.value

          when(mockSessionRepository.set(any()))
            .thenReturn(Future.successful(true))

          when(mockNavigator.nextPage(
            any(),
            any(),
            any()
          )).thenReturn(controllers.purchaserAgent.routes.AddPurchaserAgentReferenceNumberController.onPageLoad(NormalMode))


          val result = service.handleAgentSelection(
            value = "AGT001",
            agentList = testAgents,
            userAnswers = updatedAnswers,
            mode = NormalMode
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.purchaserAgent.routes.AddPurchaserAgentReferenceNumberController.onPageLoad(NormalMode).url

          verify(mockSessionRepository).set(argThat { answers =>
            answers.get(PurchaserAgentNamePage).contains("Joe Smith") &&
              answers.get(PurchaserAgentAddressPage).exists(_.line1 == "123 Street") &&
              answers.get(PurchaserAgentsContactDetailsPage).exists(_.emailAddress.contains("test@example.com"))
          })
        }
      }
    }

    "populateAssignedPurchaserAgentInSession" - {

      "must populate purchaser agent pages when returnAgentID is present" in {

        val fullReturn = emptyFullReturn
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

        val returnAgent = ReturnAgent(
          returnAgentID = Some("AGENT123"),
          agentType = Some("PURCHASER"),
          name = Some("Assigned Purchaser Agent"),
          houseNumber = None,
          address1 = Some("1 Assigned Street"),
          address2 = Some("Assigned Town"),
          address3 = Some("Assigned City"),
          address4 = None,
          postcode = Some("ZZ1 1ZZ"),
          phone = Some("07123456789"),
          email = Some("assigned@example.com"),
          reference = Some("ABF1241"),
          isAuthorised = Some("NO")
        )

        val expectedAddress = Address(
          line1 = "1 Assigned Street",
          line2 = Some("Assigned Town"),
          line3 = Some("Assigned City"),
          line4 = None,
          postcode = Some("ZZ1 1ZZ")
        )

        val expectedContactDetails = PurchaserAgentsContactDetails(
          phoneNumber = Some("07123456789"),
          emailAddress = Some("assigned@example.com")
        )

        val result =
          service.populateAssignedPurchaserAgentInSession(returnAgent, userAnswers)

        result.isSuccess mustBe true

        val updatedAnswers = result.get

        updatedAnswers.get(PurchaserAgentNamePage) mustBe Some("Assigned Purchaser Agent")
        updatedAnswers.get(PurchaserAgentAddressPage) mustBe Some(expectedAddress)
        updatedAnswers.get(PurchaserAgentsContactDetailsPage) mustBe Some(expectedContactDetails)
      }

      "must fail with IllegalStateException when returnAgentID is missing" in {

        val userAnswers = emptyUserAnswers

        val returnAgent = ReturnAgent(
          returnAgentID = None,
          agentType = Some("PURCHASER"),
          name = Some("Broken Agent"),
          houseNumber = None,
          address1 = Some("Broken Street"),
          address2 = None,
          address3 = None,
          address4 = None,
          postcode = Some("XX1 1XX"),
          phone = None,
          email = None,
          reference = None
        )

        val result =
          service.populateAssignedPurchaserAgentInSession(returnAgent, userAnswers)

        result.isFailure mustBe true
        result.failed.get mustBe a[IllegalStateException]
      }
    }
  }
}