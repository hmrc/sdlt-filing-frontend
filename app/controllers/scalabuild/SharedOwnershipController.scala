/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import forms.scalabuild.SharedOwnershipFormProvider
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import views.html.scalabuild.SharedOwnershipView

import scala.concurrent.Future

@Singleton
class SharedOwnershipController @Inject()(
                                           val controllerComponents: MessagesControllerComponents,
                                           view: SharedOwnershipView,
                                           formProvider: SharedOwnershipFormProvider,
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
        _ => Future.successful(Redirect(controllers.scalabuild.routes.SharedOwnershipController.onPageLoad().url))
      )

  }
}