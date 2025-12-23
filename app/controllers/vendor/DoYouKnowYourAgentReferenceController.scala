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

package controllers.vendor

import controllers.actions.*
import forms.vendor.DoYouKnowYourAgentReferenceFormProvider
import models.Mode
import models.vendor.DoYouKnowYourAgentReference
import navigation.Navigator
import pages.vendor.{AgentNamePage, DoYouKnowYourAgentReferencePage, VendorRepresentedByAgentPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.vendor.AgentChecksService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.vendor.DoYouKnowYourAgentReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DoYouKnowYourAgentReferenceController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: DoYouKnowYourAgentReferenceFormProvider,
                                       agentChecksService: AgentChecksService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: DoYouKnowYourAgentReferenceView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()
  
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
         
      val maybeAgentName: Option[String] = request.userAnswers.get(AgentNamePage)
      val isRepresentedByAgent = request.userAnswers.get(VendorRepresentedByAgentPage).getOrElse(false)

      (maybeAgentName, isRepresentedByAgent) match {
        case (_, false) =>
          //TODO: update to check your answers once created DTR-2057
          Redirect(controllers.routes.IndexController.onPageLoad())
          
        case (None, _) =>
          Redirect(controllers.vendor.routes.AgentNameController.onPageLoad(mode))
          
        case (Some(agentName), true) =>
          val preparedForm = request.userAnswers.get(DoYouKnowYourAgentReferencePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          val continueRoute = Ok(view(preparedForm, mode, agentName))
          agentChecksService.checkMainVendorAgentRepresentedByAgent(request.userAnswers, continueRoute)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(AgentNamePage) match {
        case None =>
          Future.successful(Redirect(controllers.vendor.routes.AgentNameController.onPageLoad(mode)))
          
        case Some(agentName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, agentName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(DoYouKnowYourAgentReferencePage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield {
                if (value.toString.equals("yes")) {
                  Redirect(navigator.nextPage(DoYouKnowYourAgentReferencePage, mode, updatedAnswers))
                } else {
                  Redirect(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad())
                }
              }
          )
      }
  }
}

