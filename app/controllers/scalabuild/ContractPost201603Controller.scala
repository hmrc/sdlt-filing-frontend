/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import forms.scalabuild.ContractPost201603FormProvider
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.ContractPost201603View

import javax.inject.Inject
import scala.concurrent.Future

class ContractPost201603Controller @Inject()(
                                              val controllerComponents: MessagesControllerComponents,
                                              view: ContractPost201603View,
                                              formProvider: ContractPost201603FormProvider,
                                            ) extends FrontendBaseController {
  val form = formProvider()
  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        _ =>
          Future.successful(Redirect(controllers.scalabuild.routes.ContractPost201603Controller.onPageLoad().url))
      )
  }
}