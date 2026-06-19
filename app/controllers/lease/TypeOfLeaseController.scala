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
import forms.lease.TypeOfLeaseFormProvider
import models.Mode
import navigation.Navigator
import pages.lease.TypeOfLeasePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.crossflow.Pages
import services.crossflow.fields.{CrossFlowFormSupport, CrossFlowValidationService}
import services.lease.LeaseService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.lease.TypeOfLeaseView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TypeOfLeaseController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: TypeOfLeaseFormProvider,
                                       crossFlow: CrossFlowValidationService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: TypeOfLeaseView,
                                       leaseService: LeaseService
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      leaseService.leaseFlowValidationCheck(request.userAnswers) match {
        case Some(redirect) => Redirect(redirect)
        case None =>
          val preparedForm = request.userAnswers.get(TypeOfLeasePage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      CrossFlowFormSupport.bindFromRequestWithCrossFlow(form, Pages.LeaseType, crossFlow) { value =>
        request.userAnswers.set(TypeOfLeasePage, value).get
      } match {

        case Left(formWithErrors) =>
          Future.successful(BadRequest(view(formWithErrors, mode)))

        case Right((_, updatedAnswers)) =>
          for {
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(TypeOfLeasePage, mode, updatedAnswers))
      }
  }
}