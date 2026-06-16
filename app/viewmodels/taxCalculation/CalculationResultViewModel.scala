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

package viewmodels.taxCalculation

import config.CurrencyFormatter
import models.{Lease, Transaction, UserAnswers}
import models.taxCalculation.*
import models.taxCalculation.TaxTypes.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.*
import utils.DateTimeFormats.*
import utils.YesNoHelper.toYesNo

case class CalculationResultViewModel(
                                       taxCalculationSummary: SummaryList,
                                       rateCardSummary:       SummaryList,
                                       premiumRateTable:      Table,
                                       npvRateTable:          Option[Table],
                                       totalTax:              Table
                                     )

object CalculationResultViewModel extends CurrencyFormatter {

  def toViewModel(result: TaxCalculationResult, answers: UserAnswers)
                 (implicit messages: Messages): Either[BuildRequestError, CalculationResultViewModel] =
    for {
      fullReturn <- answers.fullReturn.toRight(MissingFullReturnError)
      transaction <- fullReturn.transaction.toRight(MissingAboutTheTransactionError)
      mainLandId <- fullReturn.returnInfo.flatMap(_.mainLandID).toRight(MissingMainLandIdError)
      land <- fullReturn.land.flatMap(_.find(_.landID.contains(mainLandId))).toRight(MissingAboutTheLandError)
      effectiveDate <- transaction.effectiveDate.toRight(MissingTransactionAnswerError("effectiveDate"))
      formattedDate <- parseDate(effectiveDate).map(_.toLongDate).left.map(_ => InvalidDateError(effectiveDate))
      transactionDescription <- transaction.transactionDescription.toRight(MissingTransactionAnswerError("transactionDescription"))
      holdingType <- HoldingTypes.fromCode(transactionDescription).toRight(UnknownHoldingTypeError(transactionDescription))
      totalConsideration <- considerationFor(holdingType, transaction, fullReturn.lease)
      claimingRelief <- transaction.claimingRelief.toRight(MissingTransactionAnswerError("claimingRelief"))
      reliefReason <- Right(transaction.reliefReason)
      formattedRelief <- toYesNo(claimingRelief).left.map(_ => InvalidYesNoAnswerError(claimingRelief))
      propertyType <- land.propertyType.toRight(MissingLandAnswerError("propertyType"))
      isLinked <- transaction.isLinked.toRight(MissingTransactionAnswerError("isLinked"))
      formattedLinked <- toYesNo(isLinked).left.map(_ => InvalidYesNoAnswerError(isLinked))
      premiumCalc <- result.taxCalcs.find(_.taxType == premium).toRight(MissingPremiumCalcError)
      rentCalc = result.taxCalcs.find(_.taxType == rent)
    } yield {
      CalculationResultViewModel(
        taxCalculationSummary = getTaxCalculationSummary(
          effectiveDate = formattedDate,
          totalConsideration = totalConsideration.toCurrency,
          claimingRelief = formattedRelief,
          reliefReason = reliefReason,
          premiumTax = premiumCalc.taxDue.toCurrency,
          npvTax = rentCalc.map(_.taxDue.toCurrency),
          totalSdltDue = result.totalTax.toCurrency
        ),
        rateCardSummary = getRateCardSummary(
          transactionDescription = transactionDescription,
          claimingRelief = formattedRelief,
          reliefReason = reliefReason,
          propertyType = propertyType,
          isLinked = formattedLinked
        ),
        premiumRateTable = getPremiumRateTable(premiumCalc, rentCalc.isDefined),
        npvRateTable = getNpvRateTable(rentCalc),
        totalTax = getTotalTaxTable(result.totalTax.toCurrency)
      )
    }

  private def considerationFor(holdingType: HoldingTypes.Value, transaction: Transaction, lease: Option[Lease]): Either[BuildRequestError, String] =
    holdingType match {
      case HoldingTypes.leasehold => lease.flatMap(_.totalPremiumPayable).toRight(MissingLeaseAnswerError("totalPremiumPayable"))
      case _                      => transaction.totalConsideration.toRight(MissingTransactionAnswerError("totalConsideration"))
    }

  private[taxCalculation] def getTaxCalculationSummary(
                                                        effectiveDate: String,
                                                        totalConsideration: String,
                                                        claimingRelief: String,
                                                        reliefReason: Option[String],
                                                        premiumTax: String,
                                                        npvTax: Option[String],
                                                        totalSdltDue: String
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

    val bottomRows = reliefReason match {
      case Some(rReason) => Seq(
        SummaryListRow(Key(Text(getMessage("taxCalculation.reliefClaimed"))), Value(Text(claimingRelief))),
        SummaryListRow(Key(Text(getMessage("taxCalculation.reliefReason"))), Value(Text(rReason))),
        SummaryListRow(Key(Text(getMessage("taxCalculation.sdltDue"))), Value(Text(totalSdltDue)))
      )
      case None => Seq(
        SummaryListRow(Key(Text(getMessage("taxCalculation.reliefClaimed"))), Value(Text(claimingRelief))),
        SummaryListRow(Key(Text(getMessage("taxCalculation.sdltDue"))), Value(Text(totalSdltDue)))
      )
    }

    SummaryList(topRow ++ middleRows ++ bottomRows)
  }

  private[taxCalculation] def getRateCardSummary(
                                                  transactionDescription: String,
                                                  claimingRelief: String,
                                                  reliefReason: Option[String],
                                                  propertyType: String,
                                                  isLinked: String
                                                )(implicit messages: Messages): SummaryList = {

    val filteredRows = reliefReason match {
      case Some(rReason) => Seq(
        SummaryListRow(Key(Text(getMessage("rateCard.transactionType"))), Value(Text(getMessage(s"transactionType.$transactionDescription")))),
        SummaryListRow(Key(Text(getMessage("rateCard.claimingRelief"))), Value(Text(claimingRelief))),
        SummaryListRow(Key(Text(getMessage("rateCard.reliefReason"))), Value(Text(rReason))),
        SummaryListRow(Key(Text(getMessage("rateCard.propertyType"))), Value(Text(getMessage(s"propertyType.$propertyType")))),
        SummaryListRow(Key(Text(getMessage("rateCard.linked"))), Value(Text(isLinked)))
      )
      case None => Seq(
        SummaryListRow(Key(Text(getMessage("rateCard.transactionType"))), Value(Text(getMessage(s"transactionType.$transactionDescription")))),
        SummaryListRow(Key(Text(getMessage("rateCard.claimingRelief"))), Value(Text(claimingRelief))),
        SummaryListRow(Key(Text(getMessage("rateCard.propertyType"))), Value(Text(getMessage(s"propertyType.$propertyType")))),
        SummaryListRow(Key(Text(getMessage("rateCard.linked"))), Value(Text(isLinked)))
      )
    }
    SummaryList(filteredRows)
  }



  private[taxCalculation] def getPremiumRateTable(calc: CalculationDetails, isLeasehold: Boolean)
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

  private[taxCalculation] def getNpvRateTable(rentCalc: Option[CalculationDetails])
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

  private[taxCalculation] def getTotalTaxTable(totalSdltDue: String)(implicit messages: Messages): Table =
    Table(rows = Seq(totalRow("totalSdltDue", totalSdltDue)))

  private[taxCalculation] def sliceRow(slice: SliceDetails)(implicit messages: Messages): Seq[TableRow] =
    Seq(
      TableRow(content = Text(sliceDescription(slice)), classes = bold),
      TableRow(content = Text(slice.rate.toPercentage), classes = numeric),
      TableRow(content = Text(slice.taxDue.toCurrency), classes = numeric)
    )

  private[taxCalculation] def slabRow(labelKey: String, calc: CalculationDetails)(implicit messages: Messages): Seq[TableRow] =
    Seq(
      TableRow(content = Text(getMessage(labelKey)),                     classes = bold),
      TableRow(content = Text(formatRate(calc.rate, calc.rateFraction)), classes = numeric),
      TableRow(content = Text(calc.taxDue.toCurrency),                   classes = numeric)
    )

  private[taxCalculation] def totalRow(labelKey: String, amount: String)(implicit messages: Messages): Seq[TableRow] =
    Seq(
      TableRow(content = Text(getMessage(labelKey)), classes = bold),
      TableRow(content = Empty,                      classes = ""),
      TableRow(content = Text(amount),               classes = numeric)
    )

  private[taxCalculation] def rateTableHeader(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(content = Text(getMessage("rates.column.description")), classes = ""           ),
      HeadCell(content = Text(getMessage("rates.column.rate")),        classes = numericHeader),
      HeadCell(content = Text(getMessage("rates.column.sdltDue")),     classes = numericHeader)
    )

  private[taxCalculation] def sliceDescription(slice: SliceDetails)(implicit messages: Messages): String =
    (slice.from, slice.to) match {
      case (0,    Some(to))             => getMessage("rates.upTo",         to.toCurrency                 )
      case (from, Some(to)) if to != -1 => getMessage("rates.aboveAndUpTo", from.toCurrency, to.toCurrency)
      case (from,        _)             => getMessage("rates.aboveOpen",    from.toCurrency               )
    }

  private[taxCalculation] def formatRate(rate: Option[Int], fraction: Option[Int]): String = {
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
