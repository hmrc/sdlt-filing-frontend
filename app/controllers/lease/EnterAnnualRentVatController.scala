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
import forms.lease.EnterAnnualRentVatFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.lease.{AnnualStartingRentPage, EnterAnnualRentVatPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.lease.EnterAnnualRentVatView
import services.lease.LeaseService
import models.prelimQuestions.TransactionType
import models.prelimQuestions.TransactionType.{ConveyanceTransferLease, GrantOfLease}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class EnterAnnualRentVatController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        statusCheck: CheckSubmissionStatusAction,
                                        formProvider: EnterAnnualRentVatFormProvider,
                                        leaseService: LeaseService,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: EnterAnnualRentVatView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck) {
    implicit request =>
      val form = buildForm(request.userAnswers)

      leaseService.leaseFlowValidationCheck(request.userAnswers) match {
        case Some(redirect) => Redirect(redirect)
        case None =>
          val preparedForm = request.userAnswers.get(EnterAnnualRentVatPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>
      val form = buildForm(request.userAnswers)

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EnterAnnualRentVatPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            val transactionType: Option[TransactionType] = leaseService.transactionType(updatedAnswers)
            transactionType match {
              case Some(GrantOfLease) => Redirect(navigator.nextPage(EnterAnnualRentVatPage, mode, updatedAnswers))
              case Some(ConveyanceTransferLease) =>
                Redirect(controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad())
              case _ =>
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          }
      )
  }

  private def buildForm(userAnswers: UserAnswers): Form[String] = {
    val annualStartingRent: Option[BigDecimal] = Try(userAnswers.get(AnnualStartingRentPage).map(BigDecimal(_))).toOption.flatten

    def validateAnnualRentVat(annualRentVat: String): Boolean =
      annualStartingRent.forall(_ > BigDecimal(annualRentVat))

    formProvider(validateAnnualRentVat)
  }
}
