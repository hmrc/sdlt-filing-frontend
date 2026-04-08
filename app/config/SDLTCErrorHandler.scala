/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package config

import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.scalabuild.JourneyRecoveryStartAgainView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SDLTCErrorHandler @Inject()(val messagesApi: MessagesApi,
                                  val templateError: views.html.scalabuild.error_template,
                                  startAgainView: JourneyRecoveryStartAgainView,
implicit val configuration: FrontendAppConfig, val ec: ExecutionContext) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: RequestHeader): Future[Html] =
    Future.successful( templateError(pageTitle, heading, message))

  override def internalServerErrorTemplate(implicit request: RequestHeader): Future[Html] = {
    Future.successful(templateError(
      Messages("calc.error.InternalServerError500.title"),
      Messages("calc.error.InternalServerError500.heading"),
      Messages("calc.error.InternalServerError500.message")
    ))
  }

  override def notFoundTemplate(implicit request: RequestHeader): Future[Html] = {
    Future.successful(startAgainView())
  }
}