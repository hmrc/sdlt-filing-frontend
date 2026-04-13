/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package viewmodels.taxCalculation

import config.CurrencyFormatter
import models.taxCalculation.{Result, SliceDetails, TaxTypes}
import uk.gov.hmrc.govukfrontend.views.Aliases.Empty
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

case class CalculationResultViewModel(tables: Seq[Table], totalTax: Table)

object CalculationResultHelper extends CurrencyFormatter {

  private val UNBOUNDED = -1
  private val bold    = "govuk-!-font-weight-bold"
  private val numeric = "govuk-table__cell--numeric"

  private def getCaption(taxType: TaxTypes.Value): String = taxType match {
    case TaxTypes.premium => "Total premium payable SDLT rates"
    case TaxTypes.rent    => "NPV SDLT rates"
    case _                => "SDLT rates"
  }

  private def getFooter(taxType: TaxTypes.Value): String = taxType match {
    case TaxTypes.premium => "Total SDLT due on the premium"
    case TaxTypes.rent    => "Total SDLT due on the NPV"
    case _                => "Total SDLT due"
  }

  def toViewModel(result: Result): CalculationResultViewModel = {
    val tables = result.taxCalcs.sortBy(_.taxType == TaxTypes.rent).map { calc =>
      val dataRows = calc.slices match {
        case Some(slices) => slices.map { slice =>
          Seq(
            TableRow(content = Text(sliceDescription(slice)), classes = bold),
            TableRow(content = Text(s"${slice.rate}%"), classes = numeric),
            TableRow(content = Text(currencyFormat(slice.taxDue)), classes = numeric)
          )
        }
        case None => Seq(Seq(
          TableRow(content = Text(getCaption(calc.taxType)), classes = bold),
          TableRow(content = Text(s"${calc.rate.getOrElse(0)}%"), classes = numeric),
          TableRow(content = Text(currencyFormat(calc.taxDue)), classes = numeric)
        ))
      }
      val footerRow = Seq(
        TableRow(content = Text(getFooter(calc.taxType)), classes = bold),
        TableRow(content = Empty),
        TableRow(content = Text(currencyFormat(calc.taxDue)), classes = s"$numeric $bold")
      )
      Table(
        caption = Some(getCaption(calc.taxType)),
        captionClasses = "govuk-table__caption--m",
        head = Some(Seq(
          HeadCell(content = Text("Description")),
          HeadCell(content = Text("Rate"),     classes = "govuk-table__header--numeric"),
          HeadCell(content = Text("SDLT due"), classes = "govuk-table__header--numeric")
        )),
        rows = dataRows :+ footerRow
      )
    }

    val totalTax = Table(rows = Seq(Seq(
      TableRow(content = Text("Total SDLT due"), classes = bold),
      TableRow(content = Empty),
      TableRow(content = Text(currencyFormat(result.totalTax)), classes = s"$numeric $bold")
    )))

    CalculationResultViewModel(tables, totalTax)
  }

  private def sliceDescription(slice: SliceDetails): String = (slice.from, slice.to.filterNot(_ == UNBOUNDED)) match {
    case (0, to)          => s"Up to ${currencyFormat(to.getOrElse(0))}"
    case (from, Some(to)) => s"Above ${currencyFormat(from)} and up to ${currencyFormat(to)}"
    case (from, None)     => s"Above ${currencyFormat(from)}+"
  }
}
