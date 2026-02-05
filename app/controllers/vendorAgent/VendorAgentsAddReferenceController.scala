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

package controllers.vendorAgent

import controllers.actions.*
import forms.vendorAgent.VendorAgentsAddReferenceFormProvider
import models.vendorAgent.VendorAgentsAddReference
import models.{Mode, NormalMode}
import pages.vendorAgent.VendorAgentsReferencePage

import scala.util.Success
import navigation.Navigator
import pages.vendorAgent.{AgentNamePage, VendorAgentsAddReferencePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.vendorAgent.AgentChecksService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.vendorAgent.VendorAgentsAddReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VendorAgentsAddReferenceController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    sessionRepository: SessionRepository,
                                                    navigator: Navigator,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: VendorAgentsAddReferenceFormProvider,
                                                    agentChecksService: AgentChecksService,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: VendorAgentsAddReferenceView
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(AgentNamePage) match {
        case None =>
          Redirect(controllers.vendorAgent.routes.AgentNameController.onPageLoad(NormalMode))

        case Some(agentName) =>
          val preparedForm = request.userAnswers.get(VendorAgentsAddReferencePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          val continueRoute = Ok(view(preparedForm, mode, agentName))
          agentChecksService.vendorAgentExistsCheck(request.userAnswers, continueRoute)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(AgentNamePage) match {
        case None =>
          Future.successful(Redirect(controllers.vendorAgent.routes.AgentNameController.onPageLoad(NormalMode)))

        case Some(agentName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, agentName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(VendorAgentsAddReferencePage, value))
                finalAnswers <- Future.fromTry {
                  if value == VendorAgentsAddReference.No then updatedAnswers.remove(VendorAgentsReferencePage)
                  else Success(updatedAnswers)
                }
                _ <- sessionRepository.set(finalAnswers)
              } yield {
                if (value == VendorAgentsAddReference.No) {
                  Redirect(controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad())
                } else {
                  Redirect(navigator.nextPage(VendorAgentsAddReferencePage, mode, updatedAnswers))
                }
              }
          )
      }
  }
}
