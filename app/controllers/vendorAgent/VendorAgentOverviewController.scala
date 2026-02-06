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

package controllers.vendorAgent

import controllers.actions.*
import forms.vendorAgent.VendorAgentOverviewFormProvider
import models.*
import pages.vendorAgent.VendorAgentOverviewPage
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.FullReturnService
import services.vendorAgent.VendorAgentService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.VendorAgentHelper
import views.html.vendorAgent.VendorAgentOverviewView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VendorAgentOverviewController @Inject()(
                                                  val controllerComponents: MessagesControllerComponents,
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  fullReturnService: FullReturnService,
                                                  sessionRepository: SessionRepository,
                                                  vendorAgentService: VendorAgentService,
                                                  view: VendorAgentOverviewView,
                                                  formProvider: VendorAgentOverviewFormProvider
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

              val maybeAgent: Option[ReturnAgent] = fullReturn.returnAgent.flatMap(VendorAgentHelper.getVendorAgent)
              val maybeSummary: Option[SummaryList] = maybeAgent.flatMap(agent => VendorAgentHelper.buildSummary(Some(agent)))

              Ok(view(maybeSummary, form, mode))
            }
          } recover {
          case ex =>
            logger.error("[VendorAgentOverviewController][onPageLoad] Unexpected failure", ex)
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val maybeAgent: Option[ReturnAgent] =
        request.userAnswers.fullReturn.flatMap(_.returnAgent)
          .flatMap(VendorAgentHelper.getVendorAgent)

      form.bindFromRequest().fold(
        formWithErrors => {
          maybeAgent match {
            case None => Future.successful(BadRequest(view(None, formWithErrors, mode)))
            case Some(_) => Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
          }
        },
        value =>
          if (value) {
            Future.successful(Redirect(controllers.vendorAgent.routes.VendorAgentBeforeYouStartController.onPageLoad()))
          } else {
            Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
          }
      )
    }

  def changeVendorAgent(returnAgentId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val maybeAgent: Option[ReturnAgent] = request.userAnswers.fullReturn
        .flatMap(_.returnAgent)
        .flatMap(VendorAgentHelper.getVendorAgent)

      maybeAgent match {
        case Some(vendorAgent) =>
          for {
            updatedAnswers <- Future.fromTry(
              vendorAgentService.populateAssignedVendorAgentInSession(vendorAgent, request.userAnswers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad())

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def removeVendorAgent(returnAgentId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val maybeAgent: Option[ReturnAgent] = request.userAnswers.fullReturn
        .flatMap(_.returnAgent)
        .flatMap(VendorAgentHelper.getVendorAgent)

      maybeAgent match {
        case Some(vendorAgent) =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(VendorAgentOverviewPage, returnAgentId))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.routes.ReturnTaskListController.onPageLoad()) // TODO change to Remove

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}
