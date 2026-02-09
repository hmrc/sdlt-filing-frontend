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

package controllers.vendorAgent

import connectors.StampDutyLandTaxConnector
import controllers.actions.*
import forms.vendorAgent.RemoveVendorAgentFormProvider
import models.{AgentType, DeleteReturnAgentRequest, ReturnAgent, ReturnVersionUpdateRequest}
import pages.vendorAgent.{RemoveVendorAgentPage, VendorAgentOverviewPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.vendorAgent.RemoveVendorAgentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveVendorAgentController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: RemoveVendorAgentFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: RemoveVendorAgentView,
                                                backendConnector: StampDutyLandTaxConnector

                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(VendorAgentOverviewPage).map { returnAgentId =>

        val maybeReturnAgentToRemove = request.userAnswers.fullReturn.flatMap(_.returnAgent.flatMap(_.find(_.returnAgentID.contains(returnAgentId))))
        val maybeReturnAgentToRemoveName = maybeReturnAgentToRemove.flatMap(_.name)

        maybeReturnAgentToRemoveName match {
          case None =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

          case Some(name) =>
            val preparedForm = request.userAnswers.get(RemoveVendorAgentPage) match {
              case None => form
              case Some(value) => form.fill(value)
            }
            Ok(view(preparedForm, name))
        }
      }.getOrElse(
        Redirect(controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad())
      )
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(VendorAgentOverviewPage).map { returnAgentId =>

        val maybeVendorAgentToDelete: Option[ReturnAgent] = for {
          fullReturn <- request.userAnswers.fullReturn
          allAgents <- fullReturn.returnAgent
          vendorAgents = allAgents.filter(_.agentType.contains(AgentType.Vendor.toString))
          if vendorAgents.size == 1
          returnAgentToDelete <- vendorAgents.find((_.returnAgentID.contains(returnAgentId)))
        } yield returnAgentToDelete

        maybeVendorAgentToDelete match {
          case None =>
            Future.successful(Redirect(controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad()))

          case Some(agent) =>
            val agentName = agent.name.getOrElse("")
            form.bindFromRequest().fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, agentName))),

              value =>
                if (value) {
                  (for {
                    updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(request.userAnswers)
                    returnVersion <- backendConnector.updateReturnVersion(updateReturnVersionRequest)
                    deleteVendorAgentRequest <- DeleteReturnAgentRequest.from(request.userAnswers, agentType = AgentType.Vendor)
                    if returnVersion.newVersion.isDefined
                    deleteVendorAgentReturn <- backendConnector.deleteReturnAgent(deleteVendorAgentRequest) if returnVersion.newVersion.isDefined
                  } yield {
                    Redirect(controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad())
                      .flashing("vendorAgentDeleted" -> maybeVendorAgentToDelete.flatMap(_.name).getOrElse(""))
                  }).recover {
                    case _ =>
                      Redirect(controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad())
                  }
                }
                else {
                  Future.successful(Redirect(controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad()))
                }
            )
        }
      }.getOrElse(
        Future.successful(Redirect(controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad()))
      )
  }
}
