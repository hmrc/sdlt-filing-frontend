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
import models.taxCalculation.TaxTypes.*
import models.taxCalculation.{CalcTypes, SliceDetails, TaxCalculationResult, TaxTypes}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}
import utils.DateTimeFormats.asDate
import utils.YesNoHelper.toYesNo

case class CalculationResultViewModel(
                                       taxCalculationSummary: SummaryList,
                                       rateCardSummary:       SummaryList,
                                       premiumRateTable:      Option[Table],
                                       npvRateTable:          Option[Table],
                                       totalTax:              Table
                                     )

/**
 * Builds the SDLT breakdown view model. The SDLT-rates and total-tax tables are derived from the
 * calc result, the tax-calculation and rate-card summary lists pull their values from the session
 * FullReturn in UserAnswers.
 *
 * Every result from sdltc has exactly one premium CalculationDetails, the breakdown is either
 * band-based (`slices = Some(...)`, modern rates) or single-rate (`slices = None`, legacy
 * pre-Dec-2014 residential / pre-Mar-2016 non-residential slab rates). Leasehold results add a
 * second `rent` CalculationDetails for the NPV rate table.
 */

object CalculationResultHelper extends CurrencyFormatter {

  def toViewModel(result: TaxCalculationResult, answers: UserAnswers)
                 (implicit messages: Messages): CalculationResultViewModel =
    CalculationResultViewModel(
      taxCalculationSummary = getTaxCalculationSummary(result, answers),
      rateCardSummary       = getRateCardSummary(answers),
      premiumRateTable      = getPremiumRateTable(result),
      npvRateTable          = getNpvRateTable(result),
      totalTax              = getTotalTaxTable(result.totalTax)
    )

  private[utils] def getTaxCalculationSummary(result: TaxCalculationResult, answers: UserAnswers)
                                      (implicit messages: Messages): SummaryList =
    (for {
      transaction       <- answers.fullReturn.flatMap(_.transaction)
      effectiveDate      = transaction.effectiveDate                  .map(asDate).getOrElse("")
      totalConsideration = transaction.totalConsideration             .map(_.toCurrency).getOrElse("")
      claimingRelief     = transaction.claimingRelief                 .map(toYesNo).getOrElse("")
      premiumTax         = result.taxCalcs.find(_.taxType == premium) .map(_.taxDue.toCurrency).getOrElse("")
      npvTax             = result.taxCalcs.find(_.taxType == rent)    .map(_.taxDue.toCurrency)
    } yield SummaryList(Seq(
      Some(SummaryListRow(Key(Text(getMessage("taxCalculation.effectiveDate"))),            Value(Text(effectiveDate)))),
      Option.when(npvTax.isEmpty)(
        SummaryListRow(Key(Text(getMessage("taxCalculation.totalConsideration"))),          Value(Text(totalConsideration)))
      ),
      npvTax.map(_ => SummaryListRow(Key(Text(getMessage("taxCalculation.taxDuePremium"))), Value(Text(premiumTax)))),
      npvTax.map(npv => SummaryListRow(Key(Text(getMessage("taxCalculation.taxDueNpv"))),   Value(Text(npv)))),
      Some(SummaryListRow(Key(Text(getMessage("taxCalculation.reliefClaimed"))),            Value(Text(claimingRelief)))),
      Some(SummaryListRow(Key(Text(getMessage("taxCalculation.sdltDue"))),                  Value(Text(result.totalTax.toCurrency))))
    ).flatten))
      .getOrElse(SummaryList(rows = Nil))

  private[utils] def getRateCardSummary(answers: UserAnswers)
                                (implicit messages: Messages): SummaryList =
    (for {
      transaction     <- answers.fullReturn.flatMap(_.transaction)
      land            <- answers.fullReturn.flatMap(_.land).flatMap(_.headOption)
      transactionType  = transaction.transactionDescription .map(code => getMessage(s"transactionType.$code")).getOrElse("")
      claimingRelief   = transaction.claimingRelief         .map(toYesNo).getOrElse("")
      propertyType     = land.propertyType                  .map(code => getMessage(s"propertyType.$code")).getOrElse("")
      linked           = transaction.isLinked               .map(toYesNo).getOrElse("")
    } yield {
      SummaryList(Seq(
        SummaryListRow(Key(Text(getMessage("rateCard.transactionType"))), Value(Text(transactionType))),
        SummaryListRow(Key(Text(getMessage("rateCard.claimingRelief"))),  Value(Text(claimingRelief))),
        SummaryListRow(Key(Text(getMessage("rateCard.propertyType"))),    Value(Text(propertyType))),
        SummaryListRow(Key(Text(getMessage("rateCard.linked"))),          Value(Text(linked)))))
    })
      .getOrElse(SummaryList(rows = Nil))

  private[utils] def getPremiumRateTable(result: TaxCalculationResult)(implicit messages: Messages): Option[Table] =
    result.taxCalcs.find(_.taxType == TaxTypes.premium).map { calc =>
      val isLeasehold = result.taxCalcs.exists(_.taxType == TaxTypes.rent)

      Table(
      caption = Some(if (isLeasehold) getMessage("rates.captionPremium") else getMessage("rates.caption")),
      captionClasses = "govuk-table__caption--m",
      head = Some(Seq(
        HeadCell(content = Text(getMessage("rates.column.description"))),
        HeadCell(content = Text(getMessage("rates.column.rate")),    classes = numericHeader),
        HeadCell(content = Text(getMessage("rates.column.sdltDue")), classes = numericHeader)
      )),
      rows = (calc.calcType match {
        case CalcTypes.slice => calc.slices.toSeq.flatten.filterNot(zeroTaxRows).map { slice =>
          Seq(
            TableRow(content = Text(sliceDescription(slice)),  classes = bold   ),
            TableRow(content = Text(slice.rate.toPercentage),  classes = numeric),
            TableRow(content = Text(slice.taxDue.toCurrency),  classes = numeric)
          )
        }
        case CalcTypes.slab if calc.taxDue != 0 => Seq(Seq(
          TableRow(content = Text(getMessage("rates.premium")),              classes = bold   ),
          TableRow(content = Text(formatRate(calc.rate, calc.rateFraction)), classes = numeric),
          TableRow(content = Text(calc.taxDue.toCurrency),                   classes = numeric)
        ))
        case CalcTypes.slab => Nil
      }) ++ Option.when(isLeasehold)(Seq(
        TableRow(content = Text(getMessage("rates.totalOnPremium")), classes = bold   ),
        TableRow(content = Empty,                                    classes = ""     ),
        TableRow(content = Text(calc.taxDue.toCurrency),             classes = numeric)
      )).toSeq
    )
  }

  private[utils] def getNpvRateTable(result: TaxCalculationResult)(implicit messages: Messages): Option[Table] =
    result.taxCalcs.find(_.taxType == TaxTypes.rent).map { calc =>
      Table(
        caption = Some(getMessage("rates.captionNpv")),
        captionClasses = "govuk-table__caption--m",
        head = Some(Seq(
          HeadCell(content = Text(getMessage("rates.column.description"))),
          HeadCell(content = Text(getMessage("rates.column.rate")),    classes = numericHeader),
          HeadCell(content = Text(getMessage("rates.column.sdltDue")), classes = numericHeader)
        )),
        rows = (calc.calcType match {
          case CalcTypes.slice => calc.slices.toSeq.flatten.filterNot(zeroTaxRows).map { slice =>
            Seq(
              TableRow(content = Text(sliceDescription(slice)),  classes = bold   ),
              TableRow(content = Text(slice.rate.toPercentage),  classes = numeric),
              TableRow(content = Text(slice.taxDue.toCurrency),  classes = numeric)
            )
          }
          case CalcTypes.slab if calc.taxDue != 0 => Seq(Seq(
            TableRow(content = Text(getMessage("rates.npv")),                  classes = bold   ),
            TableRow(content = Text(formatRate(calc.rate, calc.rateFraction)), classes = numeric),
            TableRow(content = Text(calc.taxDue.toCurrency),                   classes = numeric)
          ))
          case CalcTypes.slab => Nil
        }) ++ Seq(Seq(
          TableRow(content = Text(getMessage("rates.totalOnNpv")), classes = bold   ),
          TableRow(content = Empty,                                classes = ""     ),
          TableRow(content = Text(calc.taxDue.toCurrency),         classes = numeric)
        ))
      )
    }

  private[utils] def getTotalTaxTable(totalTax: Int)(implicit messages: Messages): Table =
    Table(rows = Seq(Seq(
      TableRow(content = Text(getMessage("totalSdltDue")), classes = bold   ),
      TableRow(content = Empty,                            classes = ""     ),
      TableRow(content = Text(totalTax.toCurrency),        classes = numeric)
  )))

  private[utils] def sliceDescription(slice: SliceDetails)(implicit messages: Messages): String =
    (slice.from, slice.to) match {
      case (0,    Some(to))             => getMessage("rates.upTo",         to.toCurrency)
      case (from, Some(to)) if to != -1 => getMessage("rates.aboveAndUpTo", from.toCurrency, to.toCurrency)
      case (from,        _)             => getMessage("rates.aboveOpen",    from.toCurrency)
    }

  /** Renders a rate with optional fractional tenths — e.g. (Some(0), Some(5)) → "0.5%", (Some(3), None) → "3%". **/
  private[utils] def formatRate(rate: Option[Int], fraction: Option[Int]): String = {
    val r = rate.getOrElse(0)
    fraction.fold(s"$r%")(f => s"$r.$f%")
  }

  /** Readability helper to make removing zero tax rows more explanatory **/
  private val zeroTaxRows: SliceDetails => Boolean = _.taxDue == 0

  private val bold = "govuk-!-font-weight-bold"
  private val numeric = "govuk-table__cell--numeric"
  private val numericHeader = "govuk-table__header--numeric"

  private def getMessage(key: String, args: Any*)(implicit messages: Messages): String =
    messages(s"taxCalculation.calculation.$key", args*)
}
