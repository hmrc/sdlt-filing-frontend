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
import controllers.actions.{CheckSubmissionStatusAction, DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import models.vendor.VendorSessionQuestions
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsObject, JsSuccess}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.checkAnswers.CheckAnswersService
import services.vendor.VendorCreateOrUpdateService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.vendor.{VendorAddressSummary, VendorOrCompanyNameSummary, WhoIsTheVendorSummary}
import views.html.vendor.VendorCheckYourAnswersView

import javax.inject.Singleton
import scala.concurrent.*

@Singleton
class VendorCheckYourAnswersController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  statusCheck: CheckSubmissionStatusAction,
                                                  sessionRepository: SessionRepository,
                                                  vendorCreateOrUpdateService: VendorCreateOrUpdateService,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: VendorCheckYourAnswersView,
                                                  checkAnswersService: CheckAnswersService
                                                )(implicit ex: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>

      for {
        result <- sessionRepository.get(request.userAnswers.id)
      } yield {

        val isReturnIdEmpty = result.exists(_.returnId.isEmpty)
        val isDataEmpty = result.exists(_.data.value.isEmpty)
        val isVendorDataEmpty = result.exists(userAnswers => (userAnswers.data \ "vendorCurrent").asOpt[JsObject].forall(_.values.isEmpty))

        if (isReturnIdEmpty) {
          Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
        } else {
          (isDataEmpty, isVendorDataEmpty, result) match {
            case (true, _, _) => Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad())
            case (_, true, _) => Redirect(controllers.vendor.routes.VendorBeforeYouStartController.onPageLoad())
            case (_, _, Some(userAnswers)) =>
              val rowResults = Seq(
                WhoIsTheVendorSummary.row(Some(userAnswers)),
                VendorOrCompanyNameSummary.row(Some(userAnswers)),
                VendorAddressSummary.row(Some(userAnswers))
              )

              checkAnswersService.redirectOrRender(rowResults) match {
                case Left(call) => Redirect(call)
                case Right(summaryList) => Ok(view(summaryList))
              }
            case (false, false, None) => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
          }
        }
      }

  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>
      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(userAnswers) if userAnswers.returnId.isDefined =>
          userAnswers.data.validate[VendorSessionQuestions] match {
            case JsSuccess(sessionData, _) =>
              (sessionData.vendorCurrent.vendorID.isDefined, vendorCreateOrUpdateService.isVendorPurchaserCountBelowMaximum(userAnswers)) match {
                case (true, _) =>
                  vendorCreateOrUpdateService.updateVendor(userAnswers)
                case (false, true) =>
                  vendorCreateOrUpdateService.createVendor(userAnswers)
                case (false, false) =>
                  Future.successful(Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad()))
              }
            case JsError(_) =>
              Future.successful(Redirect(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()))
          }

        case _ =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
