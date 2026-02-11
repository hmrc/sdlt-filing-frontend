/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild.actions

import com.google.inject.Inject
import models.scalabuild.requests.IdentifierRequest
import play.api.i18n.Lang.logger
import play.api.mvc.Results.Redirect
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]

class SessionIdentifierAction @Inject() (
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    hc.sessionId match {
      case Some(session) =>
        block(IdentifierRequest(request, session.value))
      case None          =>
        logger.error("[IdentifierAction][SessionIdentifierAction] No sessionId found")
        Future.successful(Redirect(controllers.scalabuild.routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}
