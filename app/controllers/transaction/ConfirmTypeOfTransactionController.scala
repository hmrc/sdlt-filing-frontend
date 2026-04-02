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
import forms.transaction.ConfirmTypeOfTransactionFormProvider
import models.NormalMode
import models.prelimQuestions.TransactionType
import navigation.Navigator
import pages.transaction.ConfirmTypeOfTransactionPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transaction.ConfirmTypeOfTransactionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmTypeOfTransactionController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: ConfirmTypeOfTransactionFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ConfirmTypeOfTransactionView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val transactionTypeString: Option[String] = request.userAnswers.fullReturn.flatMap(_.transaction).flatMap(_.transactionDescription)

      val transactionType: Option[TransactionType] = TransactionType.parse(transactionTypeString)

      transactionType match {
        case Some(transaction) =>
          Ok(view(form, transaction.toString))

        case None =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val transactionTypeString: Option[String] = request.userAnswers.fullReturn.flatMap(_.transaction).flatMap(_.transactionDescription)

      val transactionType: Option[TransactionType] = TransactionType.parse(transactionTypeString)

      transactionType match {
        case Some(transaction) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, transaction.toString))),

            value =>
              if(value) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmTypeOfTransactionPage, transaction))
                  _ <- sessionRepository.set(updatedAnswers)
                } yield
                  // TODO DTR-2909: Redirect to Effective Date of the Transaction
                  Redirect(navigator.nextPage(ConfirmTypeOfTransactionPage, NormalMode, updatedAnswers))
              } else {
                // TODO DTR-2905: Redirect to What type of transaction is this?
                Future.successful(Redirect(controllers.transaction.routes.ConfirmTypeOfTransactionController.onPageLoad()))
              }

          )
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
