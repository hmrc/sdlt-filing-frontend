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
import forms.transaction.ReasonForReliefFormProvider
import models.transaction.ReasonForRelief
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.transaction.{AddRegisteredCharityNumberPage, CharityRegisteredNumberPage, IsPurchaserRegisteredWithCISPage, ReasonForReliefPage, TransactionCisNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.crossflow.Pages
import services.crossflow.fields.{CrossFlowFormSupport, CrossFlowValidationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transaction.ReasonForReliefView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReasonForReliefController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           sessionRepository: SessionRepository,
                                           navigator: Navigator,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           statusCheck: CheckSubmissionStatusAction,
                                           formProvider: ReasonForReliefFormProvider,
                                           crossFlow: CrossFlowValidationService,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: ReasonForReliefView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ReasonForReliefPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      
      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>

      CrossFlowFormSupport.bindFromRequestWithCrossFlow(form, Pages.ReliefReason, crossFlow) { value =>
        request.userAnswers.set(ReasonForReliefPage, value).get
      } match {

        case Left(formWithErrors) =>
          Future.successful(BadRequest(view(formWithErrors, mode)))

        case Right((value, updatedAnswers)) =>
          for {
            finalAnswers <- Future.fromTry {
              value match {
                case ReasonForRelief.PartExchange =>
                  updatedAnswers
                    .remove(CharityRegisteredNumberPage)
                    .flatMap(_.remove(AddRegisteredCharityNumberPage))
                case ReasonForRelief.CharitiesRelief =>
                  updatedAnswers
                    .remove(IsPurchaserRegisteredWithCISPage)
                    .flatMap(_.remove(TransactionCisNumberPage))
                case _ =>
                  updatedAnswers
                    .remove(IsPurchaserRegisteredWithCISPage)
                    .flatMap(_.remove(TransactionCisNumberPage))
                    .flatMap(_.remove(CharityRegisteredNumberPage))
                    .flatMap(_.remove(AddRegisteredCharityNumberPage))
              }
            }
            _ <- sessionRepository.set(finalAnswers)
          } yield {
            if (value.toString == "08" && mode == NormalMode) {
              Redirect(controllers.transaction.routes.IsPurchaserRegisteredWithCISController.onPageLoad(mode))
            } else if (value.toString == "20" && mode == NormalMode) {
              Redirect(controllers.transaction.routes.AddRegisteredCharityNumberController.onPageLoad(mode))
            } else {
              Redirect(navigator.nextPage(ReasonForReliefPage, mode, finalAnswers))
            }
          }
      }
  }
}