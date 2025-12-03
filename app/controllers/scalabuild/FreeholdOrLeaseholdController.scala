/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import forms.scalabuild.FreeholdOrLeaseholdFormProvider
import models.scalabuild.Tenancy
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.FreeholdOrLeaseholdView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class FreeholdOrLeaseholdController @Inject()(
                                         val controllerComponents: MessagesControllerComponents,
                                         view: FreeholdOrLeaseholdView,
                                         formProvider: FreeholdOrLeaseholdFormProvider,
                                       ) extends FrontendBaseController {

  val form:Form[Tenancy] = formProvider()
  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        value =>
           Future.successful(Redirect(controllers.scalabuild.routes.FreeholdOrLeaseholdController.onPageLoad().url))
      )
    }
}