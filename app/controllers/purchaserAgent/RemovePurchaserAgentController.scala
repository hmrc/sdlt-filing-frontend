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
import pages.purchaserAgent.RemovePurchaserAgentPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaserAgent.RemovePurchaserAgentView
import connectors.StampDutyLandTaxConnector

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemovePurchaserAgentController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: RemovePurchaserAgentFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: RemovePurchaserAgentView,
                                       backendConnector: StampDutyLandTaxConnector

                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      //TODO update to take ID from user answers DTR-1835
      val returnAgentToRemoveId = "RA001"

      val maybeReturnAgentToRemove = request.userAnswers.fullReturn.flatMap(_.returnAgent.flatMap(_.find(_.returnAgentID.contains(returnAgentToRemoveId))))
      val maybeReturnAgentToRemoveName = maybeReturnAgentToRemove.flatMap(_.name)

      maybeReturnAgentToRemoveName match {
        case None =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

        case Some(name) =>
          val preparedForm = request.userAnswers.get(RemovePurchaserAgentPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, name))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      //TODO update to take ID from user answers DTR-1835
      val returnAgentToRemoveId = "RA001"

      val maybePurchaserAgentToDelete: Option[ReturnAgent] = for {
        fullReturn <- request.userAnswers.fullReturn
        allAgents <- fullReturn.returnAgent
        purchaserAgents = allAgents.filter(_.agentType.contains(AgentType.Purchaser.toString))
        if purchaserAgents.size == 1
        returnAgentToDelete <- purchaserAgents.find((_.returnAgentID.contains(returnAgentToRemoveId)))
      } yield returnAgentToDelete

      maybePurchaserAgentToDelete match {
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Some(agent) =>
          val agentName = agent.name.get
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, agentName))),

            value =>
              if(value) {
                (for {
                  updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(request.userAnswers)
                  returnVersion <- backendConnector.updateReturnVersion(updateReturnVersionRequest)
                  deletePurchaserAgentRequest <- DeleteReturnAgentRequest.from(request.userAnswers, agentType = AgentType.Purchaser)
                  if returnVersion.newVersion.isDefined
                  deletePurchaserAgentReturn <- backendConnector.deleteReturnAgent(deletePurchaserAgentRequest) if returnVersion.newVersion.isDefined
                } yield {
                  //TODO update to purchaserAgent overview page DTR-1835 and update flashing prompt
                  Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad()).flashing("purchaserDeleted" -> maybePurchaserAgentToDelete.flatMap(_.name).getOrElse(""))
                }).recover {
                  case _ =>
                    //TODO update to purchaser overview page DTR-1835
                    Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad())
                }
              }
              else {
                //TODO update to purchaser overview page DTR-1835
                Future.successful(Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad()))
              }
          )
      }
  }
}
