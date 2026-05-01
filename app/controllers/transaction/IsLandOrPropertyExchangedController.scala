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

package controllers.transaction

import controllers.actions.*
import forms.transaction.IsLandOrPropertyExchangedFormProvider
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.transaction.IsLandOrPropertyExchangedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transaction.IsLandOrPropertyExchangedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsLandOrPropertyExchangedController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: IsLandOrPropertyExchangedFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: IsLandOrPropertyExchangedView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(IsLandOrPropertyExchangedPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(IsLandOrPropertyExchangedPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            if (value) {
              Redirect(navigator.nextPage(IsLandOrPropertyExchangedPage, mode, updatedAnswers))
            } else { //TODO DTR-3492: SPRINT-15 - Is this transaction pursuant to a previous option agreement? - tr-16
              Redirect(controllers.transaction.routes.IsLandOrPropertyExchangedController.onPageLoad(NormalMode))
            }
          }
      )
  }
}
