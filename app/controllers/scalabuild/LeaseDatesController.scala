/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import forms.scalabuild.LeaseDatesFormProvider
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

import javax.inject.Inject
import scala.concurrent.Future
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.LeaseDatesView

import java.time.LocalDate

class LeaseDatesController @Inject()(
                                      val controllerComponents: MessagesControllerComponents,
                                      view: LeaseDatesView,
                                      formProvider: LeaseDatesFormProvider,
                                    ) extends FrontendBaseController {


  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    val form:Form[_] = formProvider(LocalDate.now())//TODO: Fetch the effective date from the user answers
    Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    val form:Form[_] = formProvider(LocalDate.now())//TODO: Fetch the effective date from the user answers
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        _ =>
          Future.successful(Redirect(controllers.scalabuild.routes.LeaseDatesController.onPageLoad().url))
      )
  }
}