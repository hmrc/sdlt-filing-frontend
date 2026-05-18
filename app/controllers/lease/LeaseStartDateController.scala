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
import forms.lease.LeaseStartDateFormProvider
import models.Mode
import navigation.Navigator
import pages.lease.LeaseStartDatePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.lease.LeaseDatesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.lease.LeaseStartDateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LeaseStartDateController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: LeaseStartDateFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        leaseDatesService: LeaseDatesService,
                                        view: LeaseStartDateView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val form = formProvider()

      val preparedForm = request.userAnswers.get(LeaseStartDatePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val form = formProvider()

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(LeaseStartDatePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {

            val result = leaseDatesService.leaseDatesValidation(updatedAnswers)

            Redirect(navigator.nextPage(LeaseStartDatePage, mode, updatedAnswers))

            result match {

              case LeaseDatesService.LeaseDateValid  => Redirect(navigator.nextPage(LeaseStartDatePage, mode, updatedAnswers))
              case LeaseDatesService.LeaseStartBeforeRentEndDate => BadRequest(view(form.fill(value).withError("value", "lease.leaseStartDate.error.leaseBeforeRentEndDate"), mode))
              case LeaseDatesService.LeaseStartBeforeLeaseEndDate => BadRequest(view(form.fill(value).withError("value", "lease.leaseStartDate.error.leaseStartBeforeLeaseEndDate"), mode))
            }
          }
      )
  }
}
