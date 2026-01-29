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

package controllers.purchaserAgent

import controllers.actions.*
import forms.purchaserAgent.AddPurchaserAgentReferenceNumberFormProvider
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.purchaserAgent.{AddPurchaserAgentReferenceNumberPage, PurchaserAgentNamePage, PurchaserAgentReferencePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaserAgent.AddPurchaserAgentReferenceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class AddPurchaserAgentReferenceNumberController @Inject()(
                                                            override val messagesApi: MessagesApi,
                                                            sessionRepository: SessionRepository,
                                                            navigator: Navigator,
                                                            identify: IdentifierAction,
                                                            getData: DataRetrievalAction,
                                                            requireData: DataRequiredAction,
                                                            formProvider: AddPurchaserAgentReferenceNumberFormProvider,
                                                            val controllerComponents: MessagesControllerComponents,
                                                            view: AddPurchaserAgentReferenceNumberView
                                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(PurchaserAgentNamePage) match {
        case None =>
          Redirect(controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode))

        case Some(agentName) =>
          val preparedForm = request.userAnswers.get(AddPurchaserAgentReferenceNumberPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, agentName, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(PurchaserAgentNamePage) match {
        case None =>
          Future.successful(
            Redirect(controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode))
          )

        case Some(agentName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, agentName, mode))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddPurchaserAgentReferenceNumberPage, value))
                finalAnswers <- Future.fromTry {
                  if !value then updatedAnswers.remove(PurchaserAgentReferencePage)
                  else Success(updatedAnswers)
                }
                _ <- sessionRepository.set(finalAnswers)
              } yield {
                if (!value && mode == NormalMode) {
                  Redirect(controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(mode))
                } else {
                  Redirect(navigator.nextPage(AddPurchaserAgentReferenceNumberPage, mode, finalAnswers))
                }
              }
          )
      }
  }
}
