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
import models.taxCalculation.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}
import utils.DateTimeFormats.asDate
import utils.YesNoHelper.toYesNo

case class CalculationResultViewModel(
                                       taxCalculationSummary: SummaryList,
                                       rateCardSummary:       SummaryList,
                                       premiumRateTable:      Table,
                                       npvRateTable:          Option[Table],
                                       totalTax:              Table
                                     )

/**
 * Builds the SDLT breakdown view model. The SDLT-rates and total-tax tables are derived from the
 * calc result, the tax-calculation and rate-card summary lists pull their values from the session
 * FullReturn in UserAnswers.
 *
 * Every result from sdltc has exactly one premium CalculationDetails, the breakdown is either
 * band-based (slices = Some(...)) or single-rate (slices = None)
 * Leasehold results add a second rent CalculationDetails for the NPV rate table.
 */

object CalculationResultHelper extends CurrencyFormatter {

  def toViewModel(result: TaxCalculationResult, answers: UserAnswers)
                 (implicit messages: Messages): Either[MissingDataError, CalculationResultViewModel] =
    for {
      fullReturn             <- answers.fullReturn.toRight(MissingFullReturnError)
      transaction            <- fullReturn.transaction.toRight(MissingAboutTheTransactionError)
      land                   <- fullReturn.land.flatMap(_.headOption).toRight(MissingAboutTheLandError)
      effectiveDate          <- transaction.effectiveDate.toRight(MissingTransactionAnswerError("effectiveDate"))
      totalConsideration     <- transaction.totalConsideration.toRight(MissingTransactionAnswerError("totalConsideration"))
      claimingRelief         <- transaction.claimingRelief.toRight(MissingTransactionAnswerError("claimingRelief"))
      transactionDescription <- transaction.transactionDescription.toRight(MissingTransactionAnswerError("transactionDescription"))
      propertyType           <- land.propertyType.toRight(MissingLandAnswerError("propertyType"))
      isLinked               <- transaction.isLinked.toRight(MissingTransactionAnswerError("isLinked"))
      premiumCalc            <- result.taxCalcs.find(_.taxType == premium).toRight(MissingPremiumCalcError)
      rentCalc                = result.taxCalcs.find(_.taxType == rent)
    } yield {
      CalculationResultViewModel(
        taxCalculationSummary = getTaxCalculationSummary(
          effectiveDate      = asDate(effectiveDate),
          totalConsideration = totalConsideration.toCurrency,
          claimingRelief     = toYesNo(claimingRelief),
          premiumTax         = premiumCalc.taxDue.toCurrency,
          npvTax             = rentCalc.map(_.taxDue.toCurrency),
          totalSdltDue       = result.totalTax.toCurrency
        ),
        rateCardSummary       = getRateCardSummary(
          transactionDescription = transactionDescription,
          claimingRelief         = toYesNo(claimingRelief),
          propertyType           = propertyType,
          isLinked               = toYesNo(isLinked)
        ),
        premiumRateTable      = getPremiumRateTable(premiumCalc, rentCalc.isDefined),
        npvRateTable          = getNpvRateTable(rentCalc),
        totalTax              = getTotalTaxTable(result.totalTax.toCurrency)
      )
    }

  private[utils] def getTaxCalculationSummary(
                                               effectiveDate:      String,
                                               totalConsideration: String,
                                               claimingRelief:     String,
                                               premiumTax:         String,
                                               npvTax:             Option[String],
                                               totalSdltDue:       String
                                             )(implicit messages: Messages): SummaryList = {
    
    val topRow = Seq(
      SummaryListRow(Key(Text(getMessage("taxCalculation.effectiveDate"))), Value(Text(effectiveDate)))
    )
    
    val middleRows = npvTax match {
      case Some(npv) => Seq(
        SummaryListRow(Key(Text(getMessage("taxCalculation.taxDuePremium"))), Value(Text(premiumTax))),
        SummaryListRow(Key(Text(getMessage("taxCalculation.taxDueNpv"))), Value(Text(npv)))
      )
      case None => Seq(
        SummaryListRow(Key(Text(getMessage("taxCalculation.totalConsideration"))), Value(Text(totalConsideration)))
      )
    }
    
    val bottomRows = Seq(
      SummaryListRow(Key(Text(getMessage("taxCalculation.reliefClaimed"))), Value(Text(claimingRelief))),
      SummaryListRow(Key(Text(getMessage("taxCalculation.sdltDue"))), Value(Text(totalSdltDue)))
    )

    SummaryList(topRow ++ middleRows ++ bottomRows)
  }

  private[utils] def getRateCardSummary(
                                         transactionDescription: String,
                                         claimingRelief:         String,
                                         propertyType:           String,
                                         isLinked:               String
                                       )(implicit messages: Messages): SummaryList =
    SummaryList(Seq(
      SummaryListRow(Key(Text(getMessage("rateCard.transactionType"))), Value(Text(getMessage(s"transactionType.$transactionDescription")))),
      SummaryListRow(Key(Text(getMessage("rateCard.claimingRelief"))), Value(Text(claimingRelief))),
      SummaryListRow(Key(Text(getMessage("rateCard.propertyType"))), Value(Text(getMessage(s"propertyType.$propertyType")))),
      SummaryListRow(Key(Text(getMessage("rateCard.linked"))), Value(Text(isLinked)))
    ))

  private[utils] def getPremiumRateTable(calc: CalculationDetails, isLeasehold: Boolean)
                                        (implicit messages: Messages): Table =
    Table(
      caption = Some(if (isLeasehold) getMessage("rates.captionPremium") else getMessage("rates.caption")),
      captionClasses = mediumCaption,
      head = Some(rateTableHeader),
      rows = (calc.calcType match {
        case CalcTypes.slice => calc.slices.toSeq.flatten.map(sliceRow)
        case CalcTypes.slab  => Seq(slabRow("rates.premium", calc))
      }) ++ Option.when(isLeasehold)(
        totalRow("rates.totalOnPremium", calc.taxDue.toCurrency)
      ).toSeq
    )

  private[utils] def getNpvRateTable(rentCalc: Option[CalculationDetails])
                                    (implicit messages: Messages): Option[Table] =
    rentCalc.map { calc =>
      Table(
        caption = Some(getMessage("rates.captionNpv")),
        captionClasses = mediumCaption,
        head = Some(rateTableHeader),
        rows = (calc.calcType match {
          case CalcTypes.slice => calc.slices.toSeq.flatten.map(sliceRow)
          case CalcTypes.slab  => Seq(slabRow("rates.npv", calc))
        }) ++ Seq(
          totalRow("rates.totalOnNpv", calc.taxDue.toCurrency)
        )
      )
    }

  private[utils] def getTotalTaxTable(totalSdltDue: String)(implicit messages: Messages): Table =
    Table(rows = Seq(totalRow("totalSdltDue", totalSdltDue)))

  private[utils] def sliceRow(slice: SliceDetails)(implicit messages: Messages): Seq[TableRow] =
    Seq(
      TableRow(content = Text(sliceDescription(slice)), classes = bold),
      TableRow(content = Text(slice.rate.toPercentage), classes = numeric),
      TableRow(content = Text(slice.taxDue.toCurrency), classes = numeric)
    )

  private[utils] def slabRow(labelKey: String, calc: CalculationDetails)(implicit messages: Messages): Seq[TableRow] =
    Seq(
      TableRow(content = Text(getMessage(labelKey)),                     classes = bold),
      TableRow(content = Text(formatRate(calc.rate, calc.rateFraction)), classes = numeric),
      TableRow(content = Text(calc.taxDue.toCurrency),                   classes = numeric)
    )

  private[utils] def totalRow(labelKey: String, amount: String)(implicit messages: Messages): Seq[TableRow] =
    Seq(
      TableRow(content = Text(getMessage(labelKey)), classes = bold),
      TableRow(content = Empty,                      classes = ""),
      TableRow(content = Text(amount),               classes = numeric)
    )

  private[utils] def rateTableHeader(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(content = Text(getMessage("rates.column.description")), classes = ""           ),
      HeadCell(content = Text(getMessage("rates.column.rate")),        classes = numericHeader),
      HeadCell(content = Text(getMessage("rates.column.sdltDue")),     classes = numericHeader)
    )

  private[utils] def sliceDescription(slice: SliceDetails)(implicit messages: Messages): String =
    (slice.from, slice.to) match {
      case (0,    Some(to))             => getMessage("rates.upTo",         to.toCurrency                 )
      case (from, Some(to)) if to != -1 => getMessage("rates.aboveAndUpTo", from.toCurrency, to.toCurrency)
      case (from,        _)             => getMessage("rates.aboveOpen",    from.toCurrency               )
    }

  private[utils] def formatRate(rate: Option[Int], fraction: Option[Int]): String = {
    val r = rate.getOrElse(0)
    fraction.filter(_ != 0).fold(s"$r%")(f => s"$r.$f%")
  }

  private val bold          = "govuk-!-font-weight-bold"
  private val numeric       = "govuk-table__cell--numeric"
  private val numericHeader = "govuk-table__header--numeric"
  private val mediumCaption = "govuk-table__caption--m"

  private def getMessage(key: String, args: String*)(implicit messages: Messages): String =
    messages(s"taxCalculation.calculation.$key", args*)
}
