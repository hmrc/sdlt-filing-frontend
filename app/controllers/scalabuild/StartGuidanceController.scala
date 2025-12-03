/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.StartGuidanceView

import javax.inject.{Inject, Singleton}

@Singleton
class StartGuidanceController @Inject()(
                                      val controllerComponents: MessagesControllerComponents,
                                      view: StartGuidanceView
                                       ) extends FrontendBaseController {

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
     Ok(view())
  }

}