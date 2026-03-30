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

import controllers.actions.*
import forms.vendorAgent.VendorAgentBeforeYouStartFormProvider
import models.{AgentType, Mode}
import navigation.Navigator
import pages.vendorAgent.VendorAgentBeforeYouStartPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.vendorAgent.VendorAgentBeforeYouStartView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VendorAgentBeforeYouStartController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     sessionRepository: SessionRepository,
                                                     navigator: Navigator,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: VendorAgentBeforeYouStartFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: VendorAgentBeforeYouStartView
                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val userAnswers = request.userAnswers

      val preparedForm = userAnswers.get(VendorAgentBeforeYouStartPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      
      userAnswers.fullReturn match {
        case Some(fullReturn) =>
          if (fullReturn.returnAgent.exists(_.exists(_.agentType.contains(AgentType.Vendor.toString)))) {
            Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
          } else {
            Ok(view(preparedForm, mode))
          }
        case _ => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      }
  }
  
  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(VendorAgentBeforeYouStartPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield {
            if (value) {
              Redirect(navigator.nextPage(VendorAgentBeforeYouStartPage, mode, updatedAnswers))
            } else {
              Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
            }
          }
      )
  }
}