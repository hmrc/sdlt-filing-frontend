/*
 * Copyright 2023 HM Revenue & Customs
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

    def betweenDates(dateAfter:LocalDate, dateBefore:LocalDate):Boolean ={
      dt.isAfter(dateAfter) && dt.isBefore(dateBefore)
    }
  }
}
