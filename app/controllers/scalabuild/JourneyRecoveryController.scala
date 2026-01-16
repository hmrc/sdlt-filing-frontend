/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package controllers.scalabuild

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import controllers.scalabuild.actions.IdentifierAction
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import play.api.i18n.I18nSupport
import views.html.scalabuild.JourneyRecoveryStartAgainView

class JourneyRecoveryController @Inject()(
                                           val controllerComponents: MessagesControllerComponents,
                                           identify: IdentifierAction,
                                           startAgainView: JourneyRecoveryStartAgainView
                                         ) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = identify {
    implicit request =>
      Ok(startAgainView())
  }
}