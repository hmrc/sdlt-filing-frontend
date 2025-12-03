/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild
import forms.scalabuild.{IsAdditionalPropertyFormProvider, ReplaceMainResidenceFormProvider}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import views.html.scalabuild.AdditionalPropAndReplaceView

@Singleton
class AdditionalPropAndReplaceController @Inject()(
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: AdditionalPropAndReplaceView,
                                                    addPropFormProvider: IsAdditionalPropertyFormProvider,
                                                    replaceMainFormProvider: ReplaceMainResidenceFormProvider,
                                              ) extends FrontendBaseController {
  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    val addPropForm:Form[_] = addPropFormProvider()
    val replaceMainForm:Form[_] = replaceMainFormProvider()
    Ok(view(addPropForm, replaceMainForm))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    val addPropForm:Form[_] = addPropFormProvider()
    val replaceMainForm:Form[_] = replaceMainFormProvider()
    addPropForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, formWithErrors))),
        _ =>
          Future.successful(Redirect(controllers.scalabuild.routes.ReplaceMainResidenceController.onPageLoad().url))
      )
  }
}
