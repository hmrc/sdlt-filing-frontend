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

package controllers.purchaserAgent

import controllers.actions.*
import forms.purchaserAgent.PurchaserAgentBeforeYouStartFormProvider
import models.Mode
import pages.purchaserAgent.PurchaserAgentBeforeYouStartPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaserAgent.PurchaserAgentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaserAgent.PurchaserAgentBeforeYouStartView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserAgentBeforeYouStartController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: PurchaserAgentBeforeYouStartFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         purchaserAgentService: PurchaserAgentService,
                                         view: PurchaserAgentBeforeYouStartView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PurchaserAgentBeforeYouStartPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      
      purchaserAgentService.purchaserAgentExistsCheck(
        userAnswers = request.userAnswers,
        continueRoute = Ok(view(preparedForm, mode))
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserAgentBeforeYouStartPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            if(value) {
              //TODO change this to the next purchaser agent page
              Redirect(controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad())
            } else {
              Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
            }
          }
      )
  }
}
