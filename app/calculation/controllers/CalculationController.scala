package calculation.controllers

import javax.inject.{Inject, Singleton}

import calculation.models.Request
import calculation.services._
import calculation.validators.internal.ModelValidation
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController


@Singleton
class CalculationController @Inject()(
                                       val calculationService :  CalculationService
                                     ) extends CalculationCtr

object CalculationController extends CalculationController(
  new CalculationService(
    new LeaseholdCalculationService(new BaseCalculationService),
    new FreeholdCalculationService(new BaseCalculationService)
  )
)

trait CalculationCtr extends FrontendController{

  val calculationService :  CalculationSrv

  def calculateSDLTC: Action[AnyContent] = Action{ implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate[Request] match {
          case success: JsSuccess[Request] =>
            if(validateModel(success.value)) {
              val result = Json.toJson(calculationService.CalculateTax(success.value))
                Ok(result)
            }else{
              Logger.warn("[CalculationController] - Json model contains errors.")
              BadRequest("Validation error: "+ModelValidation.listValidationErrors(success.value))
            }
          case error: JsError =>
            Logger.warn("[CalculationController] - Json data found but data does not match model.")
            BadRequest("Json format does not match model: "+error)
      }
      case None =>
        Logger.warn("[CalculationController] - No json data received.")
        BadRequest("No json data received.")
    }
  }

  private def validateModel(request: Request) : Boolean ={
    val listOfErrors = ModelValidation.listValidationErrors(request)
    if(listOfErrors.isEmpty) true else false
  }
}