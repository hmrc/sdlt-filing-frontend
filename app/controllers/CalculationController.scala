/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package controllers

import models.Request
import services._
import utils.LoggerUtil._
import validators.internal.ModelValidation
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController


@Singleton
class CalculationController @Inject()(val calculationService: CalculationService,
                                      mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  val calculateSDLTC: Action[AnyContent] = Action{ implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate[Request] match {
          case success: JsSuccess[Request] =>
            if(validateModel(success.value)) {
              val result = Json.toJson(calculationService.calculateTax(success.value))
                Ok(result)
            }else{
              logError(s"[CalculationController] - Json request body fails model validation with errors: ${ModelValidation.listValidationErrors(success.value)} for request: $success from json: $json.")
              BadRequest(Json.toJson(s"Validation error: ${ModelValidation.listValidationErrors(success.value)}"))
            }
          case error: JsError =>
            logError(s"[CalculationController] - Incorrect Json request body format supplied for request json: $json. Failed validation with errors: $error.")
            BadRequest(Json.toJson("Incorrect Json request body format supplied: "+error))
      }
      case None =>
        logWarn("[CalculationController] - No json data received.")
        BadRequest(Json.toJson("No json data received."))
    }
  }

  private def validateModel(request: Request) : Boolean ={
    ModelValidation.listValidationErrors(request).isEmpty
  }
}