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

import controllers.actions.*
import forms.taxCalculation.TotalAmountToPayFormProvider
import models.taxCalculation.TaxCalculationFlow.FreeholdSelfAssessed
import models.taxCalculation.{MissingDataError, MissingFullReturnError}
import pages.taxCalculation.freeholdSelfAssessed.TotalAmountDuePage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.taxCalculation.CalculatedTotalDueHelper
import views.html.taxCalculation.freeholdSelfAssessed.TotalAmountDueView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FreeholdSdltNotCalculatedTotalDueController @Inject()(
                                                                      override val messagesApi: MessagesApi,
                                                                      identify: IdentifierAction,
                                                                      getData: DataRetrievalAction,
                                                                      requireData: DataRequiredAction,
                                                                      formProvider: TotalAmountToPayFormProvider,
                                                                      sdltCalculationService: SdltCalculationService,
                                                                      val controllerComponents: MessagesControllerComponents,
                                                                      view: TotalAmountDueView
                                                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {


  lazy val form: Form[String] = formProvider()
  val postAction: Call = controllers.routes.ReturnTaskListController.onPageLoad()

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sdltCalculationService.validateAsyncFlow(FreeholdSelfAssessed) {
        sdltCalculationService.calculateStampDutyLandTax(request.userAnswers) map {
          case Right(result) =>
            val preparedForm = request.userAnswers.get(TotalAmountDuePage) match {
              case None => form
              case Some(value) => form.fill(value.amount)
            }
            Ok(view(preparedForm, CalculatedTotalDueHelper.getSummaryListRows(request.userAnswers, result.totalTax), postAction))

          case Left(error:MissingDataError) => error match {
            case MissingFullReturnError =>
              logger.error(s"[FreeholdSelfAssessedSdltCalculatedTotalDueController][onPageLoad] sdltc reported missing full return: ${error.message}")
              Redirect(controllers.routes.NoReturnReferenceController.onPageLoad())
            case _ =>
              logger.error(s"[FreeholdSelfAssessedSdltCalculatedTotalDueController][onPageLoad] sdltc reported missing data: ${error.message}")
              Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
          }
        }
    }
  }


  def onSubmit: Action[AnyContent] = {
    ???
  }
}



