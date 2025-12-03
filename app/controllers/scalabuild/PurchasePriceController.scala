/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import forms.scalabuild.PurchasePriceFormProvider
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.PurchasePriceView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class PurchasePriceController @Inject()(
                                         val controllerComponents: MessagesControllerComponents,
                                         view: PurchasePriceView,
                                         formProvider: PurchasePriceFormProvider,
                                       ) extends FrontendBaseController {
  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    val form:Form[_] = formProvider()
    Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    val form:Form[_] = formProvider()
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        _ =>
          Future.successful(Redirect(controllers.scalabuild.routes.PurchasePriceController.onPageLoad().url))
      )
  }
}
