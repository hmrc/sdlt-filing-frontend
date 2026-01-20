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
import models.purchaser.{ConfirmNameOfThePurchaser, PurchaserSessionQuestions}
import pages.purchaser.{ConfirmNameOfThePurchaserPage, PurchaserAndCompanyIdPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess}
import repositories.SessionRepository
import viewmodels.checkAnswers.*
import views.html.purchaser.PurchaserCheckYourAnswersView
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.govuk.summarylist.*
import play.api.mvc.*
import services.purchaser.{PurchaserCreateOrUpdateService, PurchaserRequestService, PurchaserService}
import viewmodels.checkAnswers.purchaser.*

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

      for {
        result <- sessionRepository.get(request.userAnswers.id)
      } yield {

        val isReturnIdEmpty = result.exists(_.returnId.isEmpty)
        val isDataEmpty = result.exists(_.data.value.isEmpty)

        if (isReturnIdEmpty) {
          Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
        } else {
          (isDataEmpty, result) match {
            case (true, _) => Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad())
            case (_, Some(userAnswers)) =>

              val baseRowsConnected = Seq(
                IsPurchaserActingAsTrusteeSummary.row(Some(userAnswers)),
                PurchaserAndVendorConnectedSummary.row(Some(userAnswers))
              )

              val baseRows = {
                val mainPurchaserID = userAnswers.fullReturn.flatMap(_.returnInfo.flatMap(_.mainPurchaserID))
                val confirmNameOfThePurchaser = userAnswers.get(ConfirmNameOfThePurchaserPage)
                val purchaserAndCompanyIdPage = userAnswers.get(PurchaserAndCompanyIdPage).map(_.purchaserID)

                (confirmNameOfThePurchaser, mainPurchaserID) match {
                  case (Some(ConfirmNameOfThePurchaser.Yes), _) => purchaserService.initialSummaryRows(userAnswers) ++ purchaserService.individualConditionalSummaryRows(userAnswers) ++ purchaserService.companyConditionalSummaryRows(userAnswers) ++ baseRowsConnected
                  case (Some(ConfirmNameOfThePurchaser.No), _) => purchaserService.initialSummaryRows(userAnswers)
                  case (None, mainPurchaserID) if (mainPurchaserID == purchaserAndCompanyIdPage) => purchaserService.initialSummaryRows(userAnswers) ++ purchaserService.individualConditionalSummaryRows(userAnswers) ++ purchaserService.companyConditionalSummaryRows(userAnswers) ++ baseRowsConnected
                  case (_, _) => purchaserService.initialSummaryRows(userAnswers)
                }
              }
              val summaryList = SummaryListViewModel(rows = baseRows)
              Ok(view(summaryList))
            case (false, None)
            => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())

          }
        }
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(userAnswers) if userAnswers.returnId.isDefined =>
          userAnswers.data.validate[PurchaserSessionQuestions] match {
            case JsSuccess(sessionData, _) =>
              if (purchaserService.purchaserSessionOptionalQuestionsValidation(sessionData, userAnswers)) {
                purchaserCreateOrUpdateService.result(userAnswers,
                  sessionData,
                  backendConnector,
                  purchaserRequestService)
              } else {
                Future.successful(Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()))
              }
            case JsError(error) =>
              Future.successful(Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad()))
          }

        case _ =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }

}
