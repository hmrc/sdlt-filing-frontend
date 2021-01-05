/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package config

import javax.inject.Inject
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

class SDLTCErrorHandler @Inject()(val messagesApi: MessagesApi,
                                  val templateError: views.html.error_template,
                                  implicit val configuration: FrontendAppConfig) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html =
    templateError(pageTitle, heading, message)

  override def internalServerErrorTemplate(implicit request: Request[_]): Html = {
    templateError(
      Messages("calc.error.InternalServerError500.title"),
      Messages("calc.error.InternalServerError500.heading"),
      Messages("calc.error.InternalServerError500.message")
    )
  }
}
