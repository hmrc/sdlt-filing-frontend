/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package errors

trait ServiceError

case class ResultServiceError(message: String) extends ServiceError

