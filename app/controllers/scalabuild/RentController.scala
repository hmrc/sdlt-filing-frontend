/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import forms.scalabuild.RentFormProvider
import models.scalabuild.LeaseContextBuilder
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.RentView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.Future

class RentController @Inject()(
                                val controllerComponents: MessagesControllerComponents,
                                leaseContextBuilder: LeaseContextBuilder,
                                view: RentView,
                                formProvider: RentFormProvider,
                              ) extends FrontendBaseController {


  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    val leaseCtx = leaseContext()
    val form = formProvider(leaseCtx.periodCount)
    Ok(view(form, leaseCtx.periodCount))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    val leaseCtx = leaseContext()
    val form = formProvider(leaseCtx.periodCount)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, leaseCtx.periodCount))),
        _ => Future.successful(Redirect(controllers.scalabuild.routes.RentController.onPageLoad().url))
      )
  }

  private def leaseContext() = leaseContextBuilder.build(
    effectiveDate = LocalDate.of(2025,2,28),//TODO: Fetch the selected effective date from the user answers
    leaseStart = LocalDate.of(2025,2,28),//TODO: Fetch the selected lease start date from the user answers
    leaseEnd = LocalDate.of(2029,2,27)//TODO: Fetch the selected lease end date from the user answers
  )
}