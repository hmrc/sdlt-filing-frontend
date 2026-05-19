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

package controllers.lease

import controllers.actions.*
import forms.lease.LeaseIsVatPayableFormProvider
import models.{Mode, NormalMode}
import models.prelimQuestions.TransactionType
import models.prelimQuestions.TransactionType.{ConveyanceTransferLease, GrantOfLease}
import navigation.Navigator
import pages.lease.LeaseIsVatPayablePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.lease.LeaseIsVatPayableView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LeaseIsVatPayableController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: LeaseIsVatPayableFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: LeaseIsVatPayableView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(LeaseIsVatPayablePage) match {
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
            updatedAnswers <- Future.fromTry(request.userAnswers.set(LeaseIsVatPayablePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {

            val transactionType: Option[TransactionType] = TransactionType.parse(
              request.userAnswers.fullReturn.flatMap(_.transaction).flatMap(_.transactionDescription)
            )

            (value, transactionType) match {
              case (true, _) => Redirect(navigator.nextPage(LeaseIsVatPayablePage, mode, updatedAnswers))
              case (false, Some(GrantOfLease)) => Redirect(routes.LeaseIsVatPayableController.onPageLoad(NormalMode)) // TODO DTR-3539: Redirect to What is the total premium payable including VAT? - ls-10
              case (false, Some(ConveyanceTransferLease)) => Redirect(routes.LeaseIsVatPayableController.onPageLoad(NormalMode)) // TODO DTR-3545: Redirect to CYA
              case (false, _) => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          }
      )
  }
}