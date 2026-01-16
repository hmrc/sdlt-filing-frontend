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

import connectors.StampDutyLandTaxConnector
import controllers.actions.*
import forms.purchaserAgent.SelectPurchaserAgentFormProvider
import models.{Mode, NormalMode}
import pages.purchaserAgent.{PurchaserAgentBeforeYouStartPage, SelectPurchaserAgentPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaserAgent.SelectPurchaserAgentView
import play.api.Logging
import play.api.data.Form
import services.purchaserAgent.PurchaserAgentService
import services.purchaser.PurchaserService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectPurchaserAgentController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       connector: StampDutyLandTaxConnector,
                                       formProvider: SelectPurchaserAgentFormProvider,
                                       purchaserService: PurchaserService,
                                       purchaserAgentService: PurchaserAgentService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: SelectPurchaserAgentView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val maybeMainPurchaserName = purchaserService.mainPurchaserName(request.userAnswers).map(_.fullName)
      val beforeYouStartYes = request.userAnswers.get(PurchaserAgentBeforeYouStartPage).contains(true)
      val maybeStorn: Option[String] = request.userAnswers.fullReturn.map(_.stornId)
      
      (maybeMainPurchaserName, beforeYouStartYes, maybeStorn) match {
        case (None, true, _) =>
          Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)))

        case (Some(purchaser), true, Some(storn)) =>
          connector.getSdltOrganisation(storn).flatMap { organisation =>
            organisation.agents match {

              case Nil => Future.successful(Redirect(controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode)))

              case agents =>
                val form: Form[String] = formProvider(agents)

                val preparedForm = request.userAnswers.get(SelectPurchaserAgentPage) match {
                  case None => form
                  case Some(value) => form.fill(value)
                }
                Future.successful(Ok(view(preparedForm, mode, purchaser, Some(purchaserAgentService.agentSummaryList(agents)))))
            }
          }.recover {
            case ex =>
              logger.error("[PurchaserAgentController][onPageLoad] Error fetching SDLT organisation", ex)
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }

        case _ =>
          Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val maybeMainPurchaserName = purchaserService.mainPurchaserName(request.userAnswers).map(_.fullName)
      val maybeStorn: Option[String] = request.userAnswers.fullReturn.map(_.stornId)
      
      (maybeMainPurchaserName, maybeStorn) match {
        case (None, _) =>
          Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode)))

        case (Some(purchaser), Some(storn)) =>
          connector.getSdltOrganisation(storn).flatMap { organisation =>
            val agentList = organisation.agents
            val form: Form[String] = formProvider(agentList)
            
            form.bindFromRequest().fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, purchaser, Some(purchaserAgentService.agentSummaryList(agentList))))),

              value =>
                purchaserAgentService.handleAgentSelection(
                  value = value,
                  agentList = agentList,
                  userAnswers = request.userAnswers,
                  mode = mode
                )
            )
          }.recover {
            case ex =>
              logger.error("[PurchaserAgentController][onSubmit] Error fetching SDLT organisation", ex)
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
        case _ => Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      }
  }
}
