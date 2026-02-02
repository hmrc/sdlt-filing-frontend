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
import forms.vendorAgent.VendorAgentsContactDetailsFormProvider
import models.{Mode, NormalMode}
import models.vendorAgent.VendorAgentsContactDetails
import navigation.Navigator
import pages.vendorAgent.{AgentNamePage, VendorAgentsContactDetailsPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.vendorAgent.AgentChecksService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.vendorAgent.VendorAgentsContactDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VendorAgentsContactDetailsController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      sessionRepository: SessionRepository,
                                      navigator: Navigator,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: VendorAgentsContactDetailsFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      agentChecksService: AgentChecksService,
                                      view: VendorAgentsContactDetailsView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[VendorAgentsContactDetails] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val maybeAgentName: Option[String] = request.userAnswers.get(AgentNamePage)

      maybeAgentName match {

        case None =>
          Redirect(controllers.vendorAgent.routes.AgentNameController.onPageLoad(NormalMode))

        case Some(agentName) =>
          val preparedForm = request.userAnswers.get(VendorAgentsContactDetailsPage) match {
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
          Future.successful(Redirect(controllers.vendorAgent.routes.AgentNameController.onPageLoad(mode)))

        case Some(agentName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, agentName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(VendorAgentsContactDetailsPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(VendorAgentsContactDetailsPage, mode, updatedAnswers))
          )
      }
  }
}
