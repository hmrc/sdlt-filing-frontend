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

package controllers.taxCalculation

import controllers.actions.*
import models.taxCalculation.MissingDataError
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CalculationResultHelper
import controllers.routes.*

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FreeholdCalculatedSdltBreakdown @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       sdltCalculationService: SdltCalculationService,
                                       val controllerComponents: MessagesControllerComponents,
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService
        .calculateStampDutyLandTax(request.userAnswers)
        .map {
          case Right(result) => CalculationResultHelper.toViewModel(result)
          case Left(err)     =>
            logger.warn(s"Required field missing: ${err.message}")
            Redirect(ReturnTaskListController.onPageLoad())
        }
  }
}
