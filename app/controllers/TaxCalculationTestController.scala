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

package controllers

import forms.taxCalculation.TaxCalculationTestFormProvider
import models.taxCalculation.*
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.taxCalculation.TaxCalcResultViewModel
import views.html.{TaxCalcResultView, TaxCalcTestView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxCalculationTestController @Inject()(
  val controllerComponents: MessagesControllerComponents,
  formProvider: TaxCalculationTestFormProvider,
  calculationService: SdltCalculationService,
  testView: TaxCalcTestView,
  resultView: TaxCalcResultView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(testView(formProvider()))
  }

  def onSubmit: Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    formProvider().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(testView(formWithErrors))),
      data => {
        val sdltRequest  = SdltCalculationRequestMapper.from(data)
        val requestJson  = Json.prettyPrint(Json.toJson(sdltRequest))
        calculationService.calculateStampDutyLandTax(sdltRequest).map { response =>
          val responseJson = Json.prettyPrint(Json.toJson(response))
          val viewModel    = TaxCalcResultViewModel.from(response, data, requestJson, responseJson)
          Ok(resultView(viewModel))
        }.recover {
          case ex: Exception =>
            InternalServerError(testView(formProvider().fill(data).withGlobalError(s"Calculation failed: ${ex.getMessage}")))
        }
      }
    )
  }
}
