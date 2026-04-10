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
import forms.transaction.ChangeTypeOfTransactionFormProvider
import models.{Mode, NormalMode}
import models.prelimQuestions.TransactionType
import navigation.Navigator
import pages.transaction.{ChangeTypeOfTransactionPage, TypeOfTransactionPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transaction.ChangeTypeOfTransactionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeTypeOfTransactionController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: ChangeTypeOfTransactionFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ChangeTypeOfTransactionView,
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val transactionTypeString: Option[String] = request.userAnswers.fullReturn.flatMap(_.transaction).flatMap(_.transactionDescription)
      val transactionType: Option[TransactionType] = TransactionType.parse(transactionTypeString)
      val typeOfTransactionPage = request.userAnswers.get(TypeOfTransactionPage)
      
      val preparedForm = request.userAnswers.get(ChangeTypeOfTransactionPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      (transactionType, typeOfTransactionPage) match {
        case (Some(prelimTransaction), Some(transaction))
          if (prelimTransaction.equals(TransactionType.GrantOfLease) || prelimTransaction.equals(TransactionType.ConveyanceTransferLease))
            && (transaction.equals(TransactionType.ConveyanceTransfer) || transaction.equals(TransactionType.OtherTransaction)) =>
          Ok(view(preparedForm, mode, "transaction.changeTypeOfTransaction.hint.option1"))
        case (Some(prelimTransaction), Some(transaction))
          if (prelimTransaction.equals(TransactionType.ConveyanceTransfer) || prelimTransaction.equals(TransactionType.OtherTransaction))
            && (transaction.equals(TransactionType.GrantOfLease) || transaction.equals(TransactionType.ConveyanceTransferLease)) =>
          Ok(view(preparedForm, mode, "transaction.changeTypeOfTransaction.hint.option2"))
        case (Some(prelimTransaction), Some(transaction))
          if prelimTransaction.equals(TransactionType.GrantOfLease) && transaction.equals(TransactionType.ConveyanceTransferLease) =>
          Ok(view(preparedForm, mode, "transaction.changeTypeOfTransaction.hint.option3"))
        case (Some(prelimTransaction), Some(transaction))
          if prelimTransaction.equals(TransactionType.ConveyanceTransferLease) && transaction.equals(TransactionType.GrantOfLease) =>
          Ok(view(preparedForm, mode, "transaction.changeTypeOfTransaction.hint.option4"))
        case _ => Redirect(controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(NormalMode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val transactionTypeString: Option[String] = request.userAnswers.fullReturn.flatMap(_.transaction).flatMap(_.transactionDescription)
      val transactionType: Option[TransactionType] = TransactionType.parse(transactionTypeString)
      val typeOfTransactionPage = request.userAnswers.get(TypeOfTransactionPage)

      val option: String = (transactionType, typeOfTransactionPage) match {
        case (Some(prelimTransaction), Some(transaction))
          if (prelimTransaction.equals(TransactionType.GrantOfLease) || prelimTransaction.equals(TransactionType.ConveyanceTransferLease))
            && (transaction.equals(TransactionType.ConveyanceTransfer) || transaction.equals(TransactionType.OtherTransaction)) =>
          "transaction.changeTypeOfTransaction.hint.option1"
        case (Some(prelimTransaction), Some(transaction))
          if (prelimTransaction.equals(TransactionType.ConveyanceTransfer) || prelimTransaction.equals(TransactionType.OtherTransaction))
            && (transaction.equals(TransactionType.GrantOfLease) || transaction.equals(TransactionType.ConveyanceTransferLease)) =>
          "transaction.changeTypeOfTransaction.hint.option2"
        case (Some(prelimTransaction), Some(transaction))
          if prelimTransaction.equals(TransactionType.GrantOfLease) && transaction.equals(TransactionType.ConveyanceTransferLease) =>
          "transaction.changeTypeOfTransaction.hint.option3"
        case (Some(prelimTransaction), Some(transaction))
          if prelimTransaction.equals(TransactionType.ConveyanceTransferLease) && transaction.equals(TransactionType.GrantOfLease) =>
          "transaction.changeTypeOfTransaction.hint.option4"
        case _ => ""
      }

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, option))),

        value =>
          if (value) {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ChangeTypeOfTransactionPage, value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ChangeTypeOfTransactionPage, mode, updatedAnswers))
          } else {
            for {
              typeOfTransactionUpdate <- Future.fromTry(request.userAnswers.set(TypeOfTransactionPage, transactionType.get))
              updatedAnswers <- Future.fromTry(typeOfTransactionUpdate.set(ChangeTypeOfTransactionPage, value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ChangeTypeOfTransactionPage, mode, updatedAnswers))
          }
      )
  }
}
