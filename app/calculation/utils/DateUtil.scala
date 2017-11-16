package calculation.utils

import java.time.LocalDate

trait DateUtil {
  implicit class DateHelper(dt: LocalDate) {
    def isBetween(startDate: LocalDate, endDate: LocalDate): Boolean = {
      (dt.isAfter(startDate) || dt.isEqual(startDate)) && (dt.isBefore(endDate) || dt.isEqual(endDate))
    }

    def onOrAfter(compDate: LocalDate): Boolean = {
      dt.isAfter(compDate) || dt.isEqual(compDate)
    }
  }
}
