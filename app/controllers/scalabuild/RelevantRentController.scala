/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import forms.scalabuild.RelevantRentFormProvider
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.RelevantRentView

import javax.inject.Inject
import scala.concurrent.Future

class RelevantRentController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        view: RelevantRentView,
                                        formProvider: RelevantRentFormProvider,
                                      ) extends FrontendBaseController {

  val form = formProvider()
  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        _ => Future.successful(Redirect(controllers.scalabuild.routes.RelevantRentController.onPageLoad().url))
      )
  }
}