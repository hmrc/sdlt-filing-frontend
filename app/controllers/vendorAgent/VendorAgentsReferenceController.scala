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
import forms.vendorAgent.VendorAgentsReferenceFormProvider
import models.Mode
import models.vendorAgent.VendorAgentsAddReference
import navigation.Navigator
import pages.vendorAgent.{AgentNamePage, VendorAgentsAddReferencePage, VendorAgentsReferencePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.vendorAgent.AgentChecksService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.vendorAgent.VendorAgentsReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VendorAgentsReferenceController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 sessionRepository: SessionRepository,
                                                 navigator: Navigator,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: VendorAgentsReferenceFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 agentChecksService: AgentChecksService,
                                                 view: VendorAgentsReferenceView
                                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val userAnswers = request.userAnswers
      userAnswers.get(AgentNamePage) match {
        case None =>
          Redirect(controllers.vendorAgent.routes.AgentNameController.onPageLoad(mode))

        case Some(agentName) =>
          userAnswers.get(VendorAgentsAddReferencePage) match {
            case Some(value) if value == VendorAgentsAddReference.Yes =>
              val form = formProvider(agentName)

              val preparedForm = request.userAnswers.get(VendorAgentsReferencePage) match {
                case None => form
                case Some(value) => form.fill(value)
              }
              val continueRoute = Ok(view(preparedForm, agentName, mode))
              agentChecksService.vendorAgentExistsCheck(request.userAnswers, continueRoute, mode)
            case _ =>
              Redirect(controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad())
          }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val userAnswers = request.userAnswers

      userAnswers.get(AgentNamePage) match {
        case None =>
          Future.successful(Redirect(controllers.vendorAgent.routes.AgentNameController.onPageLoad(mode)))

        case Some(agentName) =>
          val form = formProvider(agentName)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, agentName, mode))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(VendorAgentsReferencePage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(VendorAgentsReferencePage, mode, updatedAnswers))
          )
      }
  }
}