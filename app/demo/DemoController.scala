package demo

import javax.inject.Singleton

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController

@Singleton
class DemoController extends DemoCtr

trait DemoCtr extends FrontendController {

  val respond: Action[AnyContent] = Action { implicit request =>
    Ok("response confirmed")
  }

}
