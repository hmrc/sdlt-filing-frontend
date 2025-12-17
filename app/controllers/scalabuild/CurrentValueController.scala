/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import config.FrontendAppConfig
import forms.scalabuild.CurrentValueFormProvider
import models.scalabuild.CurrentValue
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.CurrentValueView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.Future

class CurrentValueController @Inject()(
                                        clock: Clock,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CurrentValueView,
                                        formProvider: CurrentValueFormProvider,
                                        appConfig: FrontendAppConfig
                                      ) extends FrontendBaseController {

  val form:Form[CurrentValue] = formProvider()

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    val effectiveDate = LocalDate.now(clock)//TODO: Fetch the selected effective date from the user answers
    val ftbLimitValue = ftbLimit(effectiveDate)
    Ok(view(form, ftbLimitValue))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => {
          val effectiveDate = LocalDate.now//TODO: Fetch the selected effective date from the user answers
          val ftbLimitValue = ftbLimit(effectiveDate)
          Future.successful(BadRequest(view(formWithErrors, ftbLimitValue)))},
        _ => Future.successful(Redirect(controllers.scalabuild.routes.CurrentValueController.onPageLoad().url))
      )
  }

  def ftbLimit(effectiveDate: LocalDate): Int = {
    if(!effectiveDate.isBefore(appConfig.ftbStartDate) && effectiveDate.isBefore(appConfig.ftbEndDate))
      appConfig.highValue
    else
      appConfig.lowValue
  }
}