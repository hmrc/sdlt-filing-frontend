/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import models.SliceDetails
import models.scalabuild.DetailTableRow
import viewmodels.scalabuild.FormatUtils.bigDecimalFormat

object DetailSummary {
  def tableRows(detailSlices: Seq[SliceDetails]): Seq[DetailTableRow] =
    detailSlices.map { slice =>
      val rowHeading = (slice.from, slice.to) match {
        case (0, Some(firstThreshold)) =>  s"Up to ${bigDecimalFormat(firstThreshold)}"
        case (lowerThreshold, Some(upperThreshold)) =>  s"Above ${bigDecimalFormat(lowerThreshold)} and up to ${bigDecimalFormat(upperThreshold)}"
        case (upperThreshold, None)  => s"Above ${bigDecimalFormat(upperThreshold)}+"
      }
        DetailTableRow(
          rowHeading = rowHeading,
          rate = slice.rate,
          taxDue = slice.taxDue
        )
    }
}