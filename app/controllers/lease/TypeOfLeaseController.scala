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
import services.lease.LeaseService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.lease.TypeOfLeaseView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeOfLeaseController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: TypeOfLeaseFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: TypeOfLeaseView,
                                       leaseService: LeaseService
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(TypeOfLeasePage) match {
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
            updatedAnswers <- Future.fromTry(request.userAnswers.set(TypeOfLeasePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            val result = leaseService.leasePropertyLandPropertyValidation(updatedAnswers, value)

            result match {
              case LeaseService.Valid => Redirect(navigator.nextPage(TypeOfLeasePage, mode, updatedAnswers))
              case LeaseService.InvalidResidentialRule => BadRequest(view(form.withError("value", "lease.typeOfLease.error.invalidResidentialRule"), mode))
              case LeaseService.InvalidMixedRule => BadRequest(view(form.withError("value", "lease.typeOfLease.error.invalidMixedRule"), mode))
              case LeaseService.InvalidNonResidentialRule => BadRequest(view(form.withError("value", "lease.typeOfLease.error.invalidNonResidentialRule"), mode))
            }
          }
      )
  }
}
