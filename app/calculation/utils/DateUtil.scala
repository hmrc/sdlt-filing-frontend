package calculation.utils

import java.time.LocalDate

trait DateUtil {
  implicit class DateHelper(dt: LocalDate) {
    def isBetween(compDate: LocalDate, limitDate: LocalDate): Boolean = {
      (dt.isAfter(compDate) || dt.isEqual(compDate)) && (dt.isBefore(limitDate) || dt.isEqual(limitDate))
    }

    def onOrAfter(compDate: LocalDate): Boolean = {
      dt.isAfter(compDate) || dt.isEqual(compDate)
    }
  }
}
