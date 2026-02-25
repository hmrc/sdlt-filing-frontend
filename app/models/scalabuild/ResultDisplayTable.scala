/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.scalabuild
import enums.TaxTypes
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

case class ResultDisplayTable(
    resultHeading: Option[String] = None,
    resultHint: Option[String] = None,
    totalTax: Int,
    netPresentValue: Option[Int],
    summaryTable: SummaryList,
    taxesDue: Seq[(TaxTypes.Value, Int)],
    viewDetailsLink: Option[String] = None
)

sealed trait DisplayType
  case object DisplaySlab extends DisplayType
  case object DisplayFreehold extends DisplayType
  case object DisplayLeasehold extends DisplayType

case class PrintDisplayTable(
    summaryList: SummaryList,
    resultsTables: Seq[ResultDisplayTable]
)
