/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package utils

import java.time.LocalDate

trait DateUtil {
  implicit class DateHelper(dt: LocalDate) {
    def onOrAfter(compDate: LocalDate): Boolean = {
      dt.isAfter(compDate) || dt.isEqual(compDate)
    }

    def onOrBefore(compDate: LocalDate): Boolean = {
      dt.isBefore(compDate) || dt.isEqual(compDate)
    }
  }
}
