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
import controllers.routes.{NoReturnReferenceController, ReturnTaskListController}
import controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedBYSController
import controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdTaxCalculatedBYSController
import controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedBYSController
import controllers.taxCalculation.leaseholdTaxCalculated.routes.LeaseholdTaxCalculatedBYSController
import models.taxCalculation.MissingFullReturnError
import models.taxCalculation.TaxCalculationFlow.*
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TaxCalculationHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxCalculationBeforeYouStartController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       sdltCalculationService: SdltCalculationService,
                                       sessionRepository: SessionRepository,
                                       val controllerComponents: MessagesControllerComponents,
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        calc <- sdltCalculationService.calculateStampDutyLandTax(request.userAnswers)
        flow  = calc.toOption.flatMap(TaxCalculationHelper.flowFor(request.userAnswers, _))
        dest  = (flow, calc) match {
          case (Some(FreeholdTaxCalculated),     _) => FreeholdTaxCalculatedBYSController.onPageLoad()
          case (Some(FreeholdSelfAssessed),      _) => FreeholdSelfAssessedBYSController.onPageLoad()
          case (Some(LeaseholdTaxCalculated),    _) => LeaseholdTaxCalculatedBYSController.onPageLoad()
          case (Some(LeaseholdSelfAssessed),     _) => LeaseholdSelfAssessedBYSController.onPageLoad()
          case (None, Left(MissingFullReturnError)) => NoReturnReferenceController.onPageLoad()
          case (_,                               _) => ReturnTaskListController.onPageLoad()
        }
        updated <- Future.fromTry {
          flow match {
            case Some(f) => request.userAnswers.set(TaxCalculationFlowPage, f)
            case None    => request.userAnswers.remove(TaxCalculationFlowPage)
          }
        }
        _ <- sessionRepository.set(updated)
      } yield Redirect(dest)
  }
}
