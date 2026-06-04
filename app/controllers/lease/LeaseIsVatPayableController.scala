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
import models.{CheckMode, Mode, NormalMode}
import models.prelimQuestions.TransactionType
import models.prelimQuestions.TransactionType.{ConveyanceTransferLease, GrantOfLease}
import navigation.Navigator
import pages.lease.{EnterAnnualRentVatPage, LeaseIsVatPayablePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.lease.LeaseService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.lease.LeaseIsVatPayableView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class LeaseIsVatPayableController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: LeaseIsVatPayableFormProvider,
                                         leaseService: LeaseService,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: LeaseIsVatPayableView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      leaseService.leaseFlowValidationCheck(request.userAnswers) match {
        case Some(redirect) => Redirect(redirect)
        case None =>
          val preparedForm = request.userAnswers.get(LeaseIsVatPayablePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(LeaseIsVatPayablePage, value))
            finalAnswers   <- Future.fromTry {
              if !value then updatedAnswers.remove(EnterAnnualRentVatPage)
              else Success(updatedAnswers)
            }
            _ <- sessionRepository.set(finalAnswers)
          } yield {
            if(mode == CheckMode) {
              Redirect(controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad())
            } else {
              val transactionType: Option[TransactionType] = leaseService.transactionType(finalAnswers)

              (value, transactionType) match {
                case (true, _) => Redirect(navigator.nextPage(LeaseIsVatPayablePage, mode, finalAnswers))
                case (false, Some(GrantOfLease)) => Redirect(routes.LeaseEnterTotalPremiumPayableController.onPageLoad(NormalMode))
                case (false, Some(ConveyanceTransferLease)) => Redirect(routes.LeaseCheckYourAnswersController.onPageLoad())
                case (false, _) => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
            } 
          }
      )
  }
}