/*
 * Copyright 2025 HM Revenue & Customs
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

package utils

import config.CurrencyFormatter
import models.taxCalculation.{CalculationDetails, SliceDetails, TaxCalculationResult}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}

case class CalculationResultViewModel(ratesTable: Table, totalTax: Table)

/**
 * Builds a freehold SDLT breakdown view model: a single rate table for the premium plus a standalone
 * total. Every freehold result from sdltc has exactly one premium CalculationDetails — the breakdown
 * is either band-based (`slices = Some(...)`, modern rates) or single-rate (`slices = None`, legacy
 * pre-Dec-2014 residential / pre-Mar-2016 non-residential slab rates). Both shapes are handled here.
 */
object CalculationResultHelper extends CurrencyFormatter {

  private val UNBOUNDED = -1

  private val tableHead = Some(Seq(
    HeadCell(content = Text("Description")),
    HeadCell(content = Text("Rate"),     classes = "govuk-table__header--numeric"),
    HeadCell(content = Text("SDLT due"), classes = "govuk-table__header--numeric")
  ))

  def toViewModel(result: TaxCalculationResult): CalculationResultViewModel =
    CalculationResultViewModel(
      ratesTable = buildRatesTable(result.taxCalcs.head),
      totalTax   = buildTotalTaxTable(result.totalTax)
    )

  private def buildRatesTable(calc: CalculationDetails): Table = Table(
    caption        = Some("SDLT rates"),
    captionClasses = "govuk-table__caption--m",
    head           = tableHead,
    rows           = dataRowsFor(calc)
  )

  private def dataRowsFor(calc: CalculationDetails): Seq[Seq[TableRow]] =
    calc.slices match {
      case Some(slices) => slices.map { slice =>
        Seq(
          TableRow(content = Text(sliceDescription(slice)),      classes = "govuk-!-font-weight-bold"),
          TableRow(content = Text(s"${slice.rate}%"),            classes = "govuk-table__cell--numeric"),
          TableRow(content = Text(currencyFormat(slice.taxDue)), classes = "govuk-table__cell--numeric")
        )
      }
      case None => Seq(Seq(
        TableRow(content = Text("Premium"),                     classes = "govuk-!-font-weight-bold"),
        TableRow(content = Text(s"${calc.rate.getOrElse(0)}%"), classes = "govuk-table__cell--numeric"),
        TableRow(content = Text(currencyFormat(calc.taxDue)),   classes = "govuk-table__cell--numeric")
      ))
    }

  private def buildTotalTaxTable(totalTax: Int): Table = Table(rows = Seq(Seq(
    TableRow(content = Text("Total SDLT due"),         classes = "govuk-!-font-weight-bold"),
    TableRow(content = Empty),
    TableRow(content = Text(currencyFormat(totalTax)), classes = "govuk-table__cell--numeric govuk-!-font-weight-bold")
  )))

  private def sliceDescription(slice: SliceDetails): String = (slice.from, slice.to.filterNot(_ == UNBOUNDED)) match {
    case (0, to)          => s"Up to ${currencyFormat(to.getOrElse(0))}"
    case (from, Some(to)) => s"Above ${currencyFormat(from)} and up to ${currencyFormat(to)}"
    case (from, None)     => s"Above ${currencyFormat(from)}+"
  }
}
