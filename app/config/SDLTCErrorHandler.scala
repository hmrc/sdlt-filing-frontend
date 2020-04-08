/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package config

import javax.inject.Inject
import play.twirl.api.Html
import play.api.Configuration
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import play.api.mvc.Request

class SDLTCErrorHandler @Inject()(val messagesApi: MessagesApi,
                                  implicit val configuration: FrontendAppConfig) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html =
      views.html.error_template(pageTitle, heading, message)

}
