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
import forms.transaction.TransactionAddDateOfContractFormProvider
import models.Mode
import models.prelimQuestions.TransactionType.GrantOfLease
import navigation.Navigator
import pages.transaction.{TransactionAddDateOfContractPage, TypeOfTransactionPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transaction.TransactionAddDateOfContractView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransactionAddDateOfContractController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: TransactionAddDateOfContractFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: TransactionAddDateOfContractView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(TransactionAddDateOfContractPage) match {
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
            updatedAnswers <- Future.fromTry(request.userAnswers.set(TransactionAddDateOfContractPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            (value, request.userAnswers.get(TypeOfTransactionPage)) match {
              case (true, Some(_)) =>
                Redirect(navigator.nextPage(TransactionAddDateOfContractPage, mode, updatedAnswers))
              case (false, Some(GrantOfLease)) =>
                Redirect(controllers.routes.ReturnTaskListController.onPageLoad()) // TODO - DTR-2953 - change to is transaction linked to another tr-7
              case (false, Some(_)) =>
                Redirect(controllers.routes.ReturnTaskListController.onPageLoad()) // TODO - DTR-2923 - change to total consideration of transaction tr-4
              case _ =>
                Redirect(controllers.transaction.routes.TypeOfTransactionController.onPageLoad(mode))
            }
          }
      )
  }
}
