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

package controllers.vendorAgent

import connectors.StampDutyLandTaxConnector
import controllers.actions.*
import models.AgentType.Purchaser
import models.vendorAgent.VendorAgentSessionQuestions
import models.{CreateReturnAgentRequest, ReturnVersionUpdateRequest, UpdateReturnAgentRequest, UserAnswers}
import pages.purchaserAgent.PurchaserAgentOverviewPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.vendorAgent.{AddVendorAgentContactDetailsSummary, AgentNameSummary, VendorAgentAddressSummary, VendorAgentsAddReferenceSummary, VendorAgentsContactDetailsSummary, VendorAgentsReferenceSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.vendorAgent.VendorAgentCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VendorAgentCheckYourAnswersController @Inject()(
                                                       override val messagesApi: MessagesApi,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       sessionRepository: SessionRepository,
                                                       backendConnector: StampDutyLandTaxConnector,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: VendorAgentCheckYourAnswersView
                                                     )(implicit ex: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val result = Some(request.userAnswers)

      val isDataEmpty = result.exists(_.data.value.isEmpty)

      //todo reinstate once overview is done
      //      for {
      //        result <- sessionRepository.get(request.userAnswers.id)
      //      } yield {
      //
      //        val isDataEmpty = result.exists(_.data.value.isEmpty)

      if (isDataEmpty) {
        Future.successful(Redirect(controllers.vendorAgent.routes.VendorAgentBeforeYouStartController.onPageLoad()))
      } else {
        val summaryList = SummaryListViewModel(
          //todo return to this once all vendorAgent pages are updated
          rows = Seq(
            Some(AgentNameSummary.row(request.userAnswers)),
            Some(VendorAgentAddressSummary.row(request.userAnswers)),
            Some(AddVendorAgentContactDetailsSummary.row(request.userAnswers)),
            VendorAgentsContactDetailsSummary.row(request.userAnswers),
            Some(VendorAgentsAddReferenceSummary.row(request.userAnswers)),
            VendorAgentsReferenceSummary.row(request.userAnswers),
            //              Some(PurchaserAgentAuthorisedSummary.row(request.userAnswers)) //to remove??
          ).flatten
        )

        Future.successful(Ok(view(summaryList)))
      }
  }
  //}

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>

    sessionRepository.get(request.userAnswers.id).flatMap {
      case Some(userAnswers) =>
        //look at vendorAgentSessionQuestions maybe
        (userAnswers.data \ "vendorAgentCurrent").validate[VendorAgentSessionQuestions] match {
          case JsSuccess(sessionData, _) =>
            //TODO DTR-2060 - update to VendorAgentOverviewPage
            request.userAnswers.get(PurchaserAgentOverviewPage).map { returnAgentId =>
              updateReturnAgent(userAnswers)
            }.getOrElse(createReturnAgent(userAnswers))

          case JsError(_) =>
            Future.successful(
              Redirect(controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad())
            )
        }

      case None =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def updateReturnAgent(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      updateRequest <- ReturnVersionUpdateRequest.from(userAnswers)
      version <- backendConnector.updateReturnVersion(updateRequest)
      updateReturnAgentRequest <- UpdateReturnAgentRequest.from(userAnswers, Purchaser) if version.newVersion.isDefined
      updateReturnAgentReturn <- backendConnector.updateReturnAgent(updateReturnAgentRequest) if version.newVersion.isDefined
    } yield {
      if (updateReturnAgentReturn.updated) {
        //TODO DTR-2060 - update to VendorAgentOverviewController
        Redirect(controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad())
      } else {
        Redirect(controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad())
      }
    }
  }

  private def createReturnAgent(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      createReturnAgentRequest <- CreateReturnAgentRequest.from(userAnswers, Purchaser)
      createReturnAgentReturn <- backendConnector.createReturnAgent(createReturnAgentRequest)
    } yield {
      if (createReturnAgentReturn.returnAgentId.nonEmpty) {
        //TODO DTR-2060 - update to VendorAgentOverviewController
        Redirect(controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad())
      } else {
        Redirect(controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad())
      }
    }
  }
}
