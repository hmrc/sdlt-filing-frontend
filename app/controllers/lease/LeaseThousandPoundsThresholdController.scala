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
import forms.lease.LeaseThousandPoundsThresholdFormProvider
import models.prelimQuestions.TransactionType
import models.Mode
import navigation.Navigator
import pages.lease.LeaseThousandPoundsThresholdPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.lease.LeaseThousandPoundsThresholdView
import services.lease.LeaseService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LeaseThousandPoundsThresholdController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: LeaseThousandPoundsThresholdFormProvider,
                                         leaseService: LeaseService,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: LeaseThousandPoundsThresholdView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
    if (leaseService.transactionType(request.userAnswers).contains(TransactionType.GrantOfLease)) {
          leaseService.leaseFlowValidationCheck(request.userAnswers) match {
            case Some(redirect) => Redirect(redirect)
            case None =>
              val preparedForm = request.userAnswers.get(LeaseThousandPoundsThresholdPage) match {
                case None => form
                case Some(value) => form.fill(value)
              }
              Ok(view(preparedForm, mode))
          }
        } else {
          Redirect(controllers.lease.routes.LeaseIsVatPayableController.onPageLoad(mode))
        }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(LeaseThousandPoundsThresholdPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(LeaseThousandPoundsThresholdPage, mode, updatedAnswers))
      )
  }
}
