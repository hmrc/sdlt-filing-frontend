package calculation.controllers

import javax.inject.{Inject, Singleton}

import calculation.models.Request
import calculation.services._
import calculation.validators.internal.{ModelValidation, ValidationFailure}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController


@Singleton
class CalculationController @Inject()(
                                       val calculationService :  CalculationService
                                     ) extends CalculationCtr

trait CalculationCtr extends FrontendController{

  val calculationService :  CalculationSrv

  val calculateSDLTC: Action[AnyContent] = Action{ implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate[Request] match {
          case success: JsSuccess[Request] =>
            if(validateModel(success.value)._1) {
              val result = Json.toJson(calculationService.CalculateTax(success.value))
                Ok(result)
            }else{
              val listOfErrors = validateModel(success.value)._2
              Logger.error(s"[CalculationController] - Json request body fails model validation with errors: $listOfErrors for request: $success from json: $json.")
              BadRequest(Json.toJson(s"Validation error: $listOfErrors"))
            }
          case error: JsError =>
            Logger.error(s"[CalculationController] - Incorrect Json request body format supplied for request json: $json. Failed validation with errors: $error.")
            BadRequest(Json.toJson("Incorrect Json request body format supplied: "+error))
      }
      case None =>
        Logger.warn("[CalculationController] - No json data received.")
        BadRequest(Json.toJson("No json data received."))
    }
  }

  private def validateModel(request: Request) : (Boolean, Seq[ValidationFailure]) ={
    val listOfErrors = ModelValidation.listValidationErrors(request)
    if(listOfErrors.isEmpty) (true, listOfErrors) else (false, listOfErrors)
  }
}