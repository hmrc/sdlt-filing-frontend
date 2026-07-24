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
import forms.lease.LeaseStartingRentEndDateFormProvider
import models.Mode
import navigation.Navigator
import pages.lease.LeaseStartingRentEndDatePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.lease.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.lease.LeaseStartingRentEndDateView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LeaseStartingRentEndDateController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        statusCheck: CheckSubmissionStatusAction,
                                        formProvider: LeaseStartingRentEndDateFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        leaseDatesService: LeaseDatesService,
                                        leaseService: LeaseService,
                                        view: LeaseStartingRentEndDateView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck) {
    implicit request =>

      leaseService.leaseFlowValidationCheck(request.userAnswers) match {
        case Some(redirect) => Redirect(redirect)
        case None =>
          val form = formProvider()
          val preparedForm = request.userAnswers.get(LeaseStartingRentEndDatePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>

      val form = formProvider()

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          Future.fromTry(request.userAnswers.set(LeaseStartingRentEndDatePage, value)).flatMap { updatedAnswers =>
            leaseDatesService.leaseDatesValidation(updatedAnswers) match {
              case LeaseDatesService.LeaseDateValid =>
                sessionRepository.set(updatedAnswers).map(_ =>
                  Redirect(navigator.nextPage(LeaseStartingRentEndDatePage, mode, updatedAnswers)))
              case LeaseDatesService.LeaseStartBeforeRentEndDate =>
                Future.successful(BadRequest(view(form.fill(value).withError("value", "lease.leaseStartingRentEndDate.error.afterLeaseStartDate"), mode)))
              case LeaseDatesService.RentEndDateAfterLeaseEndDate =>
                Future.successful(BadRequest(view(form.fill(value).withError("value", "lease.leaseStartingRentEndDate.error.beforeLeaseEndDate"), mode)))
              case LeaseDatesService.LeaseStartBeforeLeaseEndDate =>
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }
          }
      )
  }
}
