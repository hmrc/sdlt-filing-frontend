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
import forms.vendor.VendorAgentsContactDetailsFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.vendor.{AgentNamePage, VendorAgentsContactDetailsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.vendor.AgentChecksService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.vendor.VendorAgentsContactDetailsView

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

  val form = formProvider()

  //TODO remove once DoYouKnowYourAgentContactDetailsPage exists
  protected def tempKnowsAgentDetails(userAnswers: UserAnswers): Boolean = true

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val agentName: Option[String] = request.userAnswers.get(AgentNamePage)

      // TODO: replace knowsAgentDetails with commented check when DoYouKnowYourAgentContactDetailsPage exists
      val knowsAgentDetails = tempKnowsAgentDetails(request.userAnswers)
//      val knowsAgentDetails: Boolean = request.userAnswers
//        .get(DoYouKnowYourAgentContactDetailsPage)
//        .flatMap(_.asOpt[Map[String, Any]]
//          .flatMap { vc =>
//            vc.get("doYouKnowYourAgentContactDetails") match {
//              case Some(true) => true
//              case Some(false) => false
//              case Some(s: String) if s.equalsIgnoreCase("yes") => true
//              case Some(s: String) if s.equalsIgnoreCase("no") => false
//              case _ => false
//            }
//          }
//        ).getOrElse(false)

      if(!knowsAgentDetails) {
        //TODO update to check your answers once created
        Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      } else {
        val preparedForm = request.userAnswers.get(VendorAgentsContactDetailsPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        val continueRoute = Ok(view(preparedForm, mode, agentName))
        agentChecksService.checkMainVendorAgentRepresentedByAgent(request.userAnswers, continueRoute)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val agentName: Option[String] = request.userAnswers
        .get(AgentNamePage)

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
