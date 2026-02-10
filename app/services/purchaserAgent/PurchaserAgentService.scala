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

import models.{Mode, NormalMode, ReturnAgent, UserAnswers}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import models.Agent
import models.address.*
import models.purchaserAgent.{PurchaserAgentAuthorised, PurchaserAgentsContactDetails, SelectPurchaserAgent}
import navigation.Navigator
import pages.purchaserAgent.*
import repositories.SessionRepository

import scala.util.Try
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserAgentService @Inject(
                                     sessionRepository: SessionRepository,
                                     navigator: Navigator)(implicit ec: ExecutionContext) {

  def purchaserAgentExistsCheck(userAnswers: UserAnswers, continueRoute: Result): Result = {

    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        if (fullReturn.returnAgent.exists(_.exists(_.agentType.contains("PURCHASER")))) {
          Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
        } else {
          continueRoute
        }
      case _ => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
    }
  }

  def populatePurchaserAgentInSession(agent: Agent, userAnswers: UserAnswers): Try[UserAnswers] = {

    (agent.agentId, agent.address1, agent.name) match {
      case (Some(agentId), Some(address1), Some(name)) =>

        val purchaserAgentAddress = Address(
          line1 = address1,
          line2 = agent.address2,
          line3 = agent.address3,
          line4 = agent.address4,
          postcode = agent.postcode
        )

        val purchaserAgentsContactDetails = PurchaserAgentsContactDetails(
          phoneNumber = agent.phone, emailAddress = agent.email
        )

        val addPaContactDetails = agent.phone.isDefined || agent.email.isDefined

        for {
          withName <- userAnswers.set(PurchaserAgentNamePage, name)
          withAddAddress <- withName.set(AddContactDetailsForPurchaserAgentPage, addPaContactDetails)
          withAddress <- withAddAddress.set(PurchaserAgentAddressPage, purchaserAgentAddress)
          finalAnswers <- withAddress.set(PurchaserAgentsContactDetailsPage, purchaserAgentsContactDetails)
        } yield finalAnswers

      case _ =>
        Try(throw new IllegalStateException(s"Purchaser ${agent.agentId} is missing"))
    }
  }

  def clearPurchaserAgentAnswers(userAnswers: UserAnswers): Try[UserAnswers] = {
    for {
      clearedName <- userAnswers.remove(PurchaserAgentNamePage)
      clearedAddress <- clearedName.remove(PurchaserAgentAddressPage)
      clearedAnswers <- clearedAddress.remove(PurchaserAgentsContactDetailsPage)
    } yield clearedAnswers
  }

  def agentSummaryList(agents: Seq[Agent]): Seq[(Option[String], Option[String], Option[String])] = {
    agents.map(agent => (agent.name, agent.address3, agent.agentId))
  }

  def handleAgentSelection(
                            value: String,
                            agentList: Seq[Agent],
                            userAnswers: UserAnswers,
                            mode: Mode): Future[Result] = {
    value match {
      case value if value == SelectPurchaserAgent.AddNewAgent.toString =>
        for {
          clearedAnswers <- Future.fromTry(clearPurchaserAgentAnswers(userAnswers))
          updatedAnswers <- Future.fromTry(clearedAnswers.set(SelectPurchaserAgentPage, value))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode))

      case agentId =>
        agentList.find(_.agentId.contains(agentId)) match {
          case None => Future.successful(
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          )

          case Some(agent) =>
            for {
              answers <- Future.fromTry(userAnswers.set(SelectPurchaserAgentPage, agent.agentId.get))
              updatedAnswers <- Future.fromTry(populatePurchaserAgentInSession(agent, answers))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(SelectPurchaserAgentPage, mode, updatedAnswers))
        }
    }
  }

  def populateAssignedPurchaserAgentInSession(returnAgent: ReturnAgent, userAnswers: UserAnswers): Try[UserAnswers] = {

    returnAgent.returnAgentID match {
      case Some(agentId) =>

        val purchaserAgentAddress = Address(
          line1 = returnAgent.address1.get,
          line2 = returnAgent.address2,
          line3 = returnAgent.address3,
          line4 = returnAgent.address4,
          postcode = returnAgent.postcode
        )

        val purchaserAgentsContactDetails = PurchaserAgentsContactDetails(
          phoneNumber = returnAgent.phone, emailAddress = returnAgent.email
        )

        val authorised: PurchaserAgentAuthorised = returnAgent.isAuthorised match {
          case Some("yes") | Some("YES") => PurchaserAgentAuthorised.Yes
          case _ => PurchaserAgentAuthorised.No
        }

        val baseAnswers = for {
          withId <- userAnswers.set(PurchaserAgentOverviewPage, agentId)
          withName <- withId.set(PurchaserAgentNamePage, returnAgent.name.get)
          withAddress <- withName.set(PurchaserAgentAddressPage, purchaserAgentAddress)
          addContact <- withAddress.set(AddContactDetailsForPurchaserAgentPage, returnAgent.phone.isDefined || returnAgent.email.isDefined)
          withContact <- addContact.set(PurchaserAgentsContactDetailsPage, purchaserAgentsContactDetails)
        } yield withContact

        val answersWithReference = baseAnswers.flatMap { answers =>
          returnAgent.reference match {
            case Some(ref) =>
              for {
                addReference <- answers.set(AddPurchaserAgentReferenceNumberPage, true)
                withReference <- addReference.set(PurchaserAgentReferencePage, ref)
              } yield withReference
            case None =>
              answers.set(AddPurchaserAgentReferenceNumberPage, false)
          }
        }

        answersWithReference.flatMap(_.set(PurchaserAgentAuthorisedPage, authorised))

      case _ =>
        Try(throw new IllegalStateException(s"ReturnAgent ${returnAgent.returnAgentID} is missing returnAgentID"))
    }
  }

}