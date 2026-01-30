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

import controllers.actions.*
import forms.purchaserAgent.PurchaserAgentOverviewFormProvider
import models.*
import pages.purchaserAgent.PurchaserAgentOverviewPage
import play.api.i18n.Lang.logger

import scala.concurrent.ExecutionContext
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.FullReturnService
import services.purchaserAgent.PurchaserAgentService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PurchaserAgentHelper
import views.html.purchaserAgent.PurchaserAgentOverview

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class PurchaserAgentOverviewController @Inject()(
                                                  val controllerComponents: MessagesControllerComponents,
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  fullReturnService: FullReturnService,
                                                  sessionRepository: SessionRepository,
                                                  purchaserAgentService: PurchaserAgentService,
                                                  view: PurchaserAgentOverview,
                                                  formProvider: PurchaserAgentOverviewFormProvider
                                                )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val effectiveReturnId = request.userAnswers.returnId

      effectiveReturnId.fold(
        Future.successful(Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad()))
      ) { id =>
        fullReturnService.getFullReturn(GetReturnByRefRequest(returnResourceRef = id, storn = request.userAnswers.storn))
          .flatMap { fullReturn =>
            val userAnswers = UserAnswers(id = request.userId, returnId = Some(id), fullReturn = Some(fullReturn), storn = request.userAnswers.storn)
            sessionRepository.set(userAnswers).map { _ =>

              val returnAgentList = fullReturn.returnAgent.getOrElse(Seq.empty)

              val maybeAgent: Option[ReturnAgent] =
                fullReturn.returnAgent
                  .flatMap(PurchaserAgentHelper.getPurchaserAgent)

              val maybeSummary: Option[SummaryList] =
                maybeAgent.flatMap(agent =>
                  PurchaserAgentHelper.buildSummary(Some(agent))
                )

              returnAgentList match {
                case Nil => Ok(view(None, form, mode))

                case agents => Ok(view(maybeSummary, form, mode))
              }
            }
          } recover {
          case ex =>
            logger.error("[PurchaserOverviewController][onPageLoad] Unexpected failure", ex)
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val maybeAgent: Option[ReturnAgent] =
        request.userAnswers.fullReturn.flatMap(_.returnAgent)
          .flatMap(PurchaserAgentHelper.getPurchaserAgent)

      form.bindFromRequest().fold(
        formWithErrors => {
          maybeAgent match {
            case None => Future.successful(BadRequest(view(None, formWithErrors, mode)))
            case Some(_) => Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
          }
        },
        value =>
          if (value) {
            Future.successful(Redirect(controllers.purchaserAgent.routes.PurchaserAgentBeforeYouStartController.onPageLoad(NormalMode)))
          } else {
            Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
          }
      )
    }

  def changePurchaserAgent(returnAgentId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val maybeAgent: Option[ReturnAgent] = request.userAnswers.fullReturn
        .flatMap(_.returnAgent)
        .flatMap(PurchaserAgentHelper.getPurchaserAgent)

      maybeAgent match {
        case Some(purchaserAgent) =>
          for {
            updatedAnswers <- Future.fromTry(
              purchaserAgentService.populateAssignedPurchaserAgentInSession(purchaserAgent, request.userAnswers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad())

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def removePurchaserAgent(returnAgentId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val maybeAgent: Option[ReturnAgent] = request.userAnswers.fullReturn
        .flatMap(_.returnAgent)
        .flatMap(PurchaserAgentHelper.getPurchaserAgent)

      maybeAgent match {
        case Some(purchaserAgent) =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserAgentOverviewPage, returnAgentId))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.purchaserAgent.routes.RemovePurchaserAgentController.onPageLoad())

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}
