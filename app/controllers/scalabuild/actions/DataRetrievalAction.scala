/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild.actions

import models.scalabuild.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.mvc.ActionTransformer
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class DataRetrievalActionImpl @Inject() (
  sessionRepository: SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends DataRetrievalAction {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {
    sessionRepository.get(request.userId).map {
      OptionalDataRequest(request.request, request.userId, _)

    }
  }
}

trait DataRetrievalAction extends ActionTransformer[IdentifierRequest, OptionalDataRequest]
