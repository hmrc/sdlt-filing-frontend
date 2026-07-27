/*
 * Copyright 2025 HM Revenue & Customs
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

import connectors.StampDutyLandTaxConnector
import controllers.actions.*
import forms.purchaserAgent.PurchaserAgentBeforeYouStartFormProvider
import models.{AgentType, Mode}
import navigation.Navigator
import pages.purchaserAgent.PurchaserAgentBeforeYouStartPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PurchaserCreateOrUpdateService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaserAgent.PurchaserAgentBeforeYouStartView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PurchaserAgentBeforeYouStartController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         statusCheck: CheckSubmissionStatusAction,
                                         navigator: Navigator,
                                         formProvider: PurchaserAgentBeforeYouStartFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         purchaserCreateOrUpdateService: PurchaserCreateOrUpdateService,
                                         backendConnector: StampDutyLandTaxConnector,
                                         view: PurchaserAgentBeforeYouStartView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PurchaserAgentBeforeYouStartPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      
      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>

      val hasAgentTypePurchaser = request.userAnswers.fullReturn
        .flatMap(_.returnAgent)
        .exists(_.exists(_.agentType.contains(AgentType.Purchaser.toString)))

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserAgentBeforeYouStartPage, value))
            _              <- sessionRepository.set(updatedAnswers)
            _              <- purchaserCreateOrUpdateService.updateIsRepresentedByAgent(backendConnector, value, updatedAnswers)
          } yield {
            if(value) {
              if(hasAgentTypePurchaser) {
                Redirect(controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad())
              } else {
                Redirect(navigator.nextPage(PurchaserAgentBeforeYouStartPage, mode, updatedAnswers))
              }
            } else {
              Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
            }
          }
      )
  }
}
