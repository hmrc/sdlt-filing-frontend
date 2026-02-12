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

package controllers.vendor

import com.google.inject.Inject
import connectors.StampDutyLandTaxConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import models.vendor.VendorSessionQuestions
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.vendor.{VendorCreateOrUpdateService, VendorRequestService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.vendor.{IndividualOrCompanyNameSummary, VendorAddressSummary, VendorTypeSummary}
import viewmodels.govuk.summarylist.*
import views.html.vendor.VendorCheckYourAnswersView

import scala.concurrent.*

class VendorCheckYourAnswersController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  sessionRepository: SessionRepository,
                                                  backendConnector: StampDutyLandTaxConnector,
                                                  vendorRequestService: VendorRequestService,
                                                  vendorCreateOrUpdateService: VendorCreateOrUpdateService,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: VendorCheckYourAnswersView
                                                )(implicit ex: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      for {
        result <- sessionRepository.get(request.userAnswers.id)
      } yield {

        val isReturnIdEmpty = result.exists(_.returnId.isEmpty)
        val isDataEmpty = result.exists(_.data.value.isEmpty)
        
        if(isReturnIdEmpty){
          Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
        } else {
          (isDataEmpty, result) match {
            case (true, _) => Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad())
            case (_, Some(userAnswers)) =>
              val baseRows = Seq(
                VendorTypeSummary.row(Some(userAnswers)),
                IndividualOrCompanyNameSummary.row(Some(userAnswers)),
                VendorAddressSummary.row(Some(userAnswers))
              )

              val summaryList = SummaryListViewModel(rows = baseRows)
              Ok(view(summaryList))
            case (false, None) => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
          }
        }
      }

  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(userAnswers) if userAnswers.returnId.isDefined =>
          userAnswers.data.validate[VendorSessionQuestions] match {
            case JsSuccess(sessionData, _) =>
              vendorCreateOrUpdateService.result(userAnswers,
                sessionData,
                backendConnector,
                vendorRequestService)
            case JsError(_) =>
              Future.successful(Redirect(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()))
          }

        case _ =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
