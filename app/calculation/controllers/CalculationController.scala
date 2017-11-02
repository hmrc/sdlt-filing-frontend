package calculation.controllers

import javax.inject.{Inject, Singleton}

import calculation.exceptions.ConversionFailureException
import calculation.models.{CalculationResponse, Request, Result}
import calculation.services._
import calculation.validators.internal.ModelValidation
import play.api.Logger
import play.api.libs.json
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
              Logger.warn("**placeholder text: Level 3 -> Json converted to model but model validation made")
              BadRequest("Validation error: "+ModelValidation.listValidationErrors(success.value))
            }
          case error: JsError =>
            Logger.warn("**placeholder text: Level 2 -> Json data found but does not match model")
            BadRequest("Json format does not match model: "+error)
      }
      case None =>
        Logger.warn("**placeholder text: Level 1 -> No Json data")
        BadRequest("No json data received.")
    }
  }

  private def validateModel(request: Request) : Boolean ={
    val listOfErrors = ModelValidation.listValidationErrors(request)
    if(listOfErrors.isEmpty) true else false
  }
}