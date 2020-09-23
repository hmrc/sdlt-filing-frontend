/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package config

import javax.inject.Inject
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

class SDLTCErrorHandler @Inject()(val messagesApi: MessagesApi,
                                  implicit val configuration: FrontendAppConfig) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html =
    views.html.error_template(pageTitle, heading, message)

  override def internalServerErrorTemplate(implicit request: Request[_]): Html = {
    views.html.error_template(
      Messages("calc.error.InternalServerError500.title"),
      Messages("calc.error.InternalServerError500.heading"),
      Messages("calc.error.InternalServerError500.message")
    )
  }
}
