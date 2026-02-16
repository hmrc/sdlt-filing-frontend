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

package controllers.purchaser

import connectors.StampDutyLandTaxConnector
import controllers.actions.*
import models.UserAnswers
import models.purchaser._
import pages.purchaser._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc.*
import repositories.SessionRepository
import services.purchaser._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.*
import viewmodels.checkAnswers.purchaser.*
import viewmodels.govuk.summarylist.*
import views.html.purchaser.PurchaserCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserCheckYourAnswersController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     sessionRepository: SessionRepository,
                                                     backendConnector: StampDutyLandTaxConnector,
                                                     purchaserRequestService: PurchaserRequestService,
                                                     purchaserService: PurchaserService,
                                                     purchaserCreateOrUpdateService: PurchaserCreateOrUpdateService,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: PurchaserCheckYourAnswersView
                                                   )(implicit ex: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.get(request.userAnswers.id).map {

        case Some(userAnswers) if userAnswers.returnId.isEmpty =>
          Redirect(controllers.routes.ReturnTaskListController.onPageLoad())

        case Some(userAnswers) if userAnswers.data.value.isEmpty =>
          Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad())

        case Some(userAnswers) =>
          val connectedRows = Seq(
            IsPurchaserActingAsTrusteeSummary.row(Some(userAnswers)),
            PurchaserAndVendorConnectedSummary.row(Some(userAnswers))
          )

          val mainPurchaserID = userAnswers.fullReturn.flatMap(_.returnInfo.flatMap(_.mainPurchaserID))
          val confirmName = userAnswers.get(ConfirmNameOfThePurchaserPage)
          val purchaserAndCompanyId = userAnswers.get(PurchaserAndCompanyIdPage).map(_.purchaserID)

          def fullRows = purchaserService.initialSummaryRows(userAnswers) ++
            purchaserService.individualConditionalSummaryRows(userAnswers) ++
            purchaserService.companyConditionalSummaryRows(userAnswers) ++
            connectedRows

          def initialRows = purchaserService.initialSummaryRows(userAnswers) ++ connectedRows

          val rows = (confirmName, mainPurchaserID) match {
            case (Some(ConfirmNameOfThePurchaser.Yes), _) => fullRows
            case (Some(ConfirmNameOfThePurchaser.No), _) => initialRows
            case (None, id) if id == purchaserAndCompanyId => fullRows
            case _ => initialRows
          }

          Ok(view(SummaryListViewModel(rows = rows)))

        case None =>
          Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(userAnswers) if userAnswers.returnId.isDefined =>
          userAnswers.data.validate[PurchaserSessionQuestions] match {
            case JsSuccess(sessionData, _) if purchaserService.purchaserSessionOptionalQuestionsValidation(sessionData, userAnswers) =>
              purchaserCreateOrUpdateService.result(userAnswers, sessionData, backendConnector, purchaserRequestService)
            case _ => Future.successful(Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()))
          }
        case _ => Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      }
  }
}
