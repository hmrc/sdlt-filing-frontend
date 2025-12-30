/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild.actions

import models.scalabuild.UserAnswers
import models.scalabuild.requests.{DataRequest, OptionalDataRequest}
import play.api.libs.json.JsObject
import play.api.mvc.{ActionRefiner, Result}

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionImpl @Inject() (implicit val executionContext: ExecutionContext) extends DataRequiredAction {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    request.userAnswers match {case None       =>
        val time = Instant.now()
        val ua = Some(UserAnswers(request.userId,JsObject.empty,time))
        Future.successful(Right(DataRequest(request.request, request.userId, ua.value)))
      case Some(data) =>
        Future.successful(Right(DataRequest(request.request, request.userId, data)))
    }
  }
}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
