/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild.requests

import play.api.mvc.{Request, WrappedRequest}

case class IdentifierRequest[A](request: Request[A], userId: String)
    extends WrappedRequest[A](request)
