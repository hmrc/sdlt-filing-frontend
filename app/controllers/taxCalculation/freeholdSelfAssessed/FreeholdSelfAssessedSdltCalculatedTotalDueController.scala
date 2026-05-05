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

package controllers.taxCalculation.freeholdSelfAssessed

import controllers.ReturnTaskListController
import controllers.actions.*
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.taxCalculation.CalculatedTotalDueHelper
import views.html.taxCalculation.freeholdSelfAssessed.TotalAmountDueView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FreeholdSelfAssessedSdltCalculatedTotalDueController @Inject()(
                                                                      override val messagesApi: MessagesApi,
                                                                      identify: IdentifierAction,
                                                                      getData: DataRetrievalAction,
                                                                      requireData: DataRequiredAction,
                                                                      sdltCalculationService: SdltCalculationService,
                                                                      val controllerComponents: MessagesControllerComponents,
                                                                      view: TotalAmountDueView
                                                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService
        .calculateStampDutyLandTax(request.userAnswers)
        .map {
          case Right(result) => Ok(view(CalculatedTotalDueHelper.getSummaryListRows(request.userAnswers, result.totalTax)))
          case Left(error) =>
            logger.warn(s"[FreeholdSelfAssessedSdltCalculatedTotalDueController][onPageLoad] sdltc reported missing data: ${error.message}")
            Redirect(controllers.routes.ReturnTaskListController.onPageLoad(request.userId))
        }
  }

  def onSubmit: Action[AnyContent] = {
    ???
  }
}
