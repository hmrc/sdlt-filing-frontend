/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild
import forms.scalabuild.IsPurchaserIndividualFormProvider
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.IsPurchaserIndividualView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class IsPurchaserIndividualController @Inject()(
                                         val controllerComponents: MessagesControllerComponents,
                                         view: IsPurchaserIndividualView,
                                         formProvider: IsPurchaserIndividualFormProvider,
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
          Future.successful(Redirect(controllers.scalabuild.routes.IsPurchaserIndividualController.onPageLoad().url))
      )
  }
}