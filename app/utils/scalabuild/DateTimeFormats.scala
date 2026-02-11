/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package utils.scalabuild

import java.time.format.DateTimeFormatter

object DateTimeFormats {
  def localDateTimeFormatter():DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
}
