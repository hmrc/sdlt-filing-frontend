/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package calculation.utils

import java.time.LocalDate

trait DateUtil {
  implicit class DateHelper(dt: LocalDate) {
    def onOrAfter(compDate: LocalDate): Boolean = {
      dt.isAfter(compDate) || dt.isEqual(compDate)
    }
  }
}
