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
import forms.transaction.IsPurchaserRegisteredWithCISFormProvider
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.transaction.{IsPurchaserRegisteredWithCISPage, TransactionCisNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transaction.IsPurchaserRegisteredWithCISView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class IsPurchaserRegisteredWithCISController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: IsPurchaserRegisteredWithCISFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: IsPurchaserRegisteredWithCISView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(IsPurchaserRegisteredWithCISPage) match {
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
            updatedAnswers <- Future.fromTry(request.userAnswers.set(IsPurchaserRegisteredWithCISPage, value))
            finalAnswers   <- Future.fromTry {
              if value then Success(updatedAnswers)
              else updatedAnswers.remove(TransactionCisNumberPage)
            }
            _              <- sessionRepository.set(finalAnswers)
          } yield {
            if (!value && mode == NormalMode) {
              Redirect(controllers.transaction.routes.TransactionPartialReliefController.onPageLoad(mode))
            } else {
              Redirect(navigator.nextPage(IsPurchaserRegisteredWithCISPage, mode, finalAnswers))
            }
          }
      )
  }
}
