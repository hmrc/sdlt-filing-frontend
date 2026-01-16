/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package controllers.scalabuild.actions

import com.google.inject.Inject
import models.scalabuild.requests.IdentifierRequest
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class FakeIdentifierAction @Inject() (bodyParsers: PlayBodyParsers) extends IdentifierAction {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] =
    block(IdentifierRequest(request, "id"))

  override def parser: BodyParser[AnyContent] =
    bodyParsers.default

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}