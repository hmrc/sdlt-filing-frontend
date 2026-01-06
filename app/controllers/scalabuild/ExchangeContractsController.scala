/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import forms.scalabuild.ExchangeContractsFormProvider
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.ExchangeContractsView

import javax.inject.Inject
import scala.concurrent.Future

class ExchangeContractsController @Inject()(
                                             val controllerComponents: MessagesControllerComponents,
                                             view: ExchangeContractsView,
                                             formProvider: ExchangeContractsFormProvider,
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
          Future.successful(Redirect(controllers.scalabuild.routes.ExchangeContractsController.onPageLoad().url))
      )
  }
}