/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import forms.scalabuild.ResidentialOrNonResidentialFormProvider
import models.scalabuild.PropertyType

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import play.api.data.Form
import views.html.scalabuild.ResidentialOrNonResidentialView

import scala.concurrent.Future

@Singleton
class ResidentialOrNonResidentialController @Inject()(
                                               val controllerComponents: MessagesControllerComponents,
                                               view: ResidentialOrNonResidentialView,
                                               formProvider: ResidentialOrNonResidentialFormProvider,
                                             ) extends FrontendBaseController {

  val form:Form[PropertyType] = formProvider()
  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        _ => Future.successful(Redirect(controllers.scalabuild.routes.ResidentialOrNonResidentialController.onPageLoad().url))
      )

  }
}
