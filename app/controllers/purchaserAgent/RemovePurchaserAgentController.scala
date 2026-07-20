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

package controllers.purchaserAgent

import controllers.actions.*
import forms.purchaserAgent.RemovePurchaserAgentFormProvider
import models.{AgentType, DeleteReturnAgentRequest, ReturnAgent, ReturnVersionUpdateRequest}
import pages.purchaserAgent.{PurchaserAgentOverviewPage, RemovePurchaserAgentPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaserAgent.RemovePurchaserAgentView
import connectors.StampDutyLandTaxConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RemovePurchaserAgentController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                statusCheck: CheckSubmissionStatusAction,
                                                formProvider: RemovePurchaserAgentFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: RemovePurchaserAgentView,
                                                backendConnector: StampDutyLandTaxConnector

                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck) {
    implicit request =>

      request.userAnswers.get(PurchaserAgentOverviewPage).map { returnAgentId =>

        val maybeReturnAgentToRemove = request.userAnswers.fullReturn.flatMap(_.returnAgent.flatMap(_.find(_.returnAgentID.contains(returnAgentId))))
        val maybeReturnAgentToRemoveName = maybeReturnAgentToRemove.flatMap(_.name)

        maybeReturnAgentToRemoveName match {
          case None =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

          case Some(name) =>
            val form = formProvider(Some(name))
            val preparedForm = request.userAnswers.get(RemovePurchaserAgentPage) match {
              case None => form
              case Some(value) => form.fill(value)
            }
            Ok(view(preparedForm, name))
        }
      }.getOrElse(
        Redirect(controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad())
      )
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>

      request.userAnswers.get(PurchaserAgentOverviewPage).map { returnAgentId =>

        val maybePurchaserAgentToDelete: Option[ReturnAgent] = for {
          fullReturn <- request.userAnswers.fullReturn
          allAgents <- fullReturn.returnAgent
          purchaserAgents = allAgents.filter(_.agentType.contains(AgentType.Purchaser.toString))
          if purchaserAgents.size == 1
          returnAgentToDelete <- purchaserAgents.find((_.returnAgentID.contains(returnAgentId)))
        } yield returnAgentToDelete

        maybePurchaserAgentToDelete match {
          case None =>
            Future.successful(Redirect(controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad()))

          case Some(agent) =>
            val agentName = agent.name
            val form = formProvider(agentName)
            form.bindFromRequest().fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, agentName.getOrElse("")))),

              value =>
                if (value) {
                  (for {
                    updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(request.userAnswers)
                    returnVersion <- backendConnector.updateReturnVersion(updateReturnVersionRequest)
                    deletePurchaserAgentRequest <- DeleteReturnAgentRequest.from(request.userAnswers, agentType = AgentType.Purchaser)
                    if returnVersion.newVersion.isDefined
                    deletePurchaserAgentReturn <- backendConnector.deleteReturnAgent(deletePurchaserAgentRequest) if returnVersion.newVersion.isDefined
                  } yield {
                    Redirect(controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad())
                      .flashing("purchaserAgentDeleted" -> maybePurchaserAgentToDelete.flatMap(_.name).getOrElse(""))
                  }).recover {
                    case _ =>
                      Redirect(controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad())
                  }
                }
                else {
                  Future.successful(Redirect(controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad()))
                }
            )
        }
      }.getOrElse(
        Future.successful(Redirect(controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad()))
      )
  }
}
