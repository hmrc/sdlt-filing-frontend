/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import forms.scalabuild.MarketValueFormProvider
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import models.scalabuild.MarketValue
import views.html.scalabuild.MarketValueView

import javax.inject.Inject
import scala.concurrent.Future

class MarketValueController  @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        view: MarketValueView,
                                        formProvider: MarketValueFormProvider,
                                      ) extends FrontendBaseController {

  val form:Form[MarketValue] = formProvider(true)//TODO: fetch the right ftbLimit previously selected from user answers

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
       Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        _ => Future.successful(Redirect(controllers.scalabuild.routes.MarketValueController.onPageLoad().url))
      )
  }
}