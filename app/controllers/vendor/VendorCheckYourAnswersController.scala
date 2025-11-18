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
import models.prelimQuestions.{PrelimReturn, PrelimSessionQuestions}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.preliminary.{PrelimAddressSummary, PurchaserIsIndividualSummary, PurchaserSurnameOrCompanyNameSummary, TransactionTypeSummary}
import viewmodels.checkAnswers.vendor.{IndividualOrCompanyNameSummary, RepresentedByAnAgentSummary, VendorAddressSummary, VendorTypeSummary}
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
                                            val controllerComponents: MessagesControllerComponents,
                                            view: VendorCheckYourAnswersView
                                          )(implicit ex: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      for {
        result <- sessionRepository.get(request.userAnswers.id)
      } yield {

        val isDataEmpty = result.exists(_.data.value.isEmpty)

        if (isDataEmpty) {
          Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad())
        } else {
          val summaryList = SummaryListViewModel(
            rows = Seq(
              VendorTypeSummary.row(result),
              IndividualOrCompanyNameSummary.row(result),
              VendorAddressSummary.row(result),
              RepresentedByAnAgentSummary.row(result)
            )
          )

          Ok(view(summaryList))
        }
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(userAnswers) =>
          userAnswers.data.validate[PrelimSessionQuestions] match {
            case JsSuccess(sessionData, _) =>

              for {
                prelimReturn <- PrelimReturn.from(Some(userAnswers))
                returnId <- backendConnector.createReturn(prelimReturn)
                _ <- sessionRepository.set(userAnswers.copy(returnId = Some(returnId.returnResourceRef)))
              } yield {
                if (returnId.returnResourceRef.nonEmpty) {
                  Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
                } else {
                  Redirect(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad())
                }
              }

            case JsError(_) =>
              Future.successful(Redirect(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad()))
          }

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
