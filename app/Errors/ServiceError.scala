/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package Errors

trait ServiceError

case class MongoError(message: String) extends ServiceError

case class ResultServiceError(message: String) extends ServiceError

