/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package exceptions

class RequiredValueNotDefinedException(message: String) extends Throwable(message)

class InvalidDateException(message: String) extends Throwable(message)