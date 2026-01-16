/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package controllers.scalabuild.actions

import models.scalabuild.UserAnswers
import models.scalabuild.requests.{IdentifierRequest, OptionalDataRequest}

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalAction (dataToReturn: Option[UserAnswers]) extends DataRetrievalAction {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] =
    Future(OptionalDataRequest(request.request, request.userId, dataToReturn))

  override protected implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}