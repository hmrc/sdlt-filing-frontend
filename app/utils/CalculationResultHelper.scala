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
import models.UserAnswers
import models.taxCalculation.{CalculationDetails, SliceDetails, TaxCalculationResult}
import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}

import java.time.LocalDate
import scala.util.Try

case class CalculationResultViewModel(
                                       taxCalculationSummary: SummaryList,
                                       rateCardSummary:       SummaryList,
                                       ratesTable:            Table,
                                       totalTax:              Table
                                     )

/**
 * Builds the freehold SDLT breakdown view model. The SDLT-rates and total-tax tables are derived from
 * the calc result; the tax-calculation and rate-card summary lists pull their values from the cached
 * FullReturn in UserAnswers.
 *
 * Every freehold result from sdltc has exactly one premium CalculationDetails — the breakdown is
 * either band-based (`slices = Some(...)`, modern rates) or single-rate (`slices = None`, legacy
 * pre-Dec-2014 residential / pre-Mar-2016 non-residential slab rates). Both shapes are handled here.
 */

object CalculationResultHelper extends CurrencyFormatter {

  def toViewModel(result: TaxCalculationResult, answers: UserAnswers)(implicit messages: Messages): CalculationResultViewModel =
    CalculationResultViewModel(
      taxCalculationSummary = getTaxCalculationSummary(result, answers),
      rateCardSummary       = getRateCardSummary(answers),
      ratesTable            = buildRatesTable(result.taxCalcs.head),
      totalTax              = buildTotalTaxTable(result.totalTax)
    )

  private def getTaxCalculationSummary(result: TaxCalculationResult, answers: UserAnswers)
                                      (implicit messages: Messages): SummaryList =
    (for {
      fullReturn        <- answers.fullReturn
      transaction       <- fullReturn.transaction
      effectiveDate      = transaction.effectiveDate.map(asDate).getOrElse("")
      totalConsideration = transaction.totalConsideration.map(currencyFormat).getOrElse("")
      claimingRelief     = transaction.claimingRelief.map(asYesNo).getOrElse("")
    } yield {
      SummaryList(rows = Seq(
        SummaryListRow(
          Key(Text(getMessage("taxCalculation.effectiveDate"))),
          Value(Text(effectiveDate))),
        SummaryListRow(
          Key(Text(getMessage("taxCalculation.totalConsideration"))),
          Value(Text(totalConsideration))),
        SummaryListRow(
          Key(Text(getMessage("taxCalculation.reliefClaimed"))),
          Value(Text(claimingRelief))),
        SummaryListRow(
          Key(Text(getMessage("taxCalculation.sdltDue"))),
          Value(Text(currencyFormat(result.totalTax))))
      ))
    }).getOrElse(SummaryList(rows = Nil))

  private def getRateCardSummary(answers: UserAnswers)
                                (implicit messages: Messages): SummaryList =
    (for {
      fullReturn      <- answers.fullReturn
      transaction     <- fullReturn.transaction
      lands           <- fullReturn.land
      land            <- lands.headOption
      transactionType  = transaction.transactionDescription.map(code => getMessage(s"transactionType.$code")).getOrElse("")
      claimingRelief   = transaction.claimingRelief.map(asYesNo).getOrElse("")
      propertyType     = land.propertyType.map(code => getMessage(s"propertyType.$code")).getOrElse("")
      linked           = transaction.isLinked.map(asYesNo).getOrElse("")
    } yield {
      SummaryList(rows = Seq(
        SummaryListRow(
          Key(Text(getMessage("rateCard.transactionType"))),
          Value(Text(transactionType))),
        SummaryListRow(
          Key(Text(getMessage("rateCard.claimingRelief"))),
          Value(Text(claimingRelief))),
        SummaryListRow(
          Key(Text(getMessage("rateCard.propertyType"))),
          Value(Text(propertyType))),
        SummaryListRow(
          Key(Text(getMessage("rateCard.linked"))),
          Value(Text(linked)))
      ))
    }).getOrElse(SummaryList(rows = Nil))

  private def buildRatesTable(calc: CalculationDetails)(implicit messages: Messages): Table = Table(
    caption        = Some(getMessage("rates.caption")),
    captionClasses = "govuk-table__caption--m",
    head           = Some(Seq(
      HeadCell(content = Text(getMessage("rates.column.description"))),
      HeadCell(content = Text(getMessage("rates.column.rate")),    classes = "govuk-table__header--numeric"),
      HeadCell(content = Text(getMessage("rates.column.sdltDue")), classes = "govuk-table__header--numeric")
    )),
    rows           = dataRowsFor(calc)
  )

  private def dataRowsFor(calc: CalculationDetails)(implicit messages: Messages): Seq[Seq[TableRow]] =
    calc.slices match {
      case Some(slices) => slices.filter(_.taxDue != 0).map { slice =>
        Seq(
          TableRow(content = Text(sliceDescription(slice)),      classes = "govuk-!-font-weight-bold"),
          TableRow(content = Text(s"${slice.rate}%"),            classes = "govuk-table__cell--numeric"),
          TableRow(content = Text(currencyFormat(slice.taxDue)), classes = "govuk-table__cell--numeric")
        )
      }
      case None if calc.taxDue != 0 => Seq(Seq(
        TableRow(content = Text(getMessage("rates.premium")),   classes = "govuk-!-font-weight-bold"),
        TableRow(content = Text(s"${calc.rate.getOrElse(0)}%"), classes = "govuk-table__cell--numeric"),
        TableRow(content = Text(currencyFormat(calc.taxDue)),   classes = "govuk-table__cell--numeric")
      ))
      case None => Nil
    }

  private def buildTotalTaxTable(totalTax: Int)(implicit messages: Messages): Table = Table(rows = Seq(Seq(
    TableRow(content = Text(getMessage("totalSdltDue")), classes = "govuk-!-font-weight-bold"),
    TableRow(content = Empty),
    TableRow(content = Text(currencyFormat(totalTax)),   classes = "govuk-table__cell--numeric govuk-!-font-weight-bold")
  )))

  private def sliceDescription(slice: SliceDetails)(implicit messages: Messages): String =
    (slice.from, slice.to) match {
      case (0,    Some(to))             => getMessage("rates.upTo",         currencyFormat(to))
      case (from, Some(to)) if to != -1 => getMessage("rates.aboveAndUpTo", currencyFormat(from), currencyFormat(to))
      case (from,        _)             => getMessage("rates.aboveOpen",    currencyFormat(from))
    }

  private def asYesNo(s: String)(implicit messages: Messages): String = s.toLowerCase match {
    case "yes" => messages("site.yes")
    case "no"  => messages("site.no")
    case _     => s
  }

  private def asDate(s: String)(implicit messages: Messages): String = {
    implicit val lang: Lang = messages.lang
    Try(LocalDate.parse(s)).toOption.map(_.format(DateTimeFormats.dateTimeFormat())).getOrElse(s)
  }
  
  private def getMessage(key: String, args: Any*)(implicit messages: Messages): String =
    messages(s"taxCalculation.calculation.$key", args*)
}
