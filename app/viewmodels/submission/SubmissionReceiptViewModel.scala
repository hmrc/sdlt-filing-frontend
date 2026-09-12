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

package viewmodels.submission

import models.{FullReturn, Land, Purchaser}
import models.prelimQuestions.TransactionType
import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{Table, TableRow}
import utils.DateTimeFormats.{dateTimeFormat, parseDate}

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField
import java.time.{LocalDateTime, OffsetDateTime}
import scala.util.Try

case class SubmissionReceiptViewModel(
                                       purchaserName: String,
                                       submissionTime: Option[String],
                                       submissionDate: Option[String],
                                       submissionReceiptNumber: Option[String],
                                       table: Table
                                     )

object SubmissionReceiptViewModel {

  private val timeFormatter = DateTimeFormatter.ofPattern("h:mma")
  private val bold = "govuk-!-font-weight-bold"

  private val backendTimestampFormat: DateTimeFormatter =
    new DateTimeFormatterBuilder()
      .appendPattern("yyyy-MM-dd HH:mm:ss")
      .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
      .toFormatter()

  private[submission] def parseSubmissionRequestDate(raw: String): Option[LocalDateTime] = {
    val trimmed = raw.trim
    Try(LocalDateTime.parse(trimmed, backendTimestampFormat))
      .orElse(Try(LocalDateTime.parse(trimmed)))
      .orElse(Try(OffsetDateTime.parse(trimmed).toLocalDateTime))
      .toOption
  }

  def apply(fullReturn: FullReturn)(implicit messages: Messages): Option[SubmissionReceiptViewModel] =
    for {
      submission <- fullReturn.submission
      utrn       <- submission.UTRN
    } yield {
      implicit val lang: Lang = messages.lang

      val parsedDate = submission.submissionRequestDate.flatMap(parseSubmissionRequestDate)

      SubmissionReceiptViewModel(
        purchaserName           = purchaserName(fullReturn),
        submissionTime          = parsedDate.map(_.format(timeFormatter).toLowerCase),
        submissionDate          = parsedDate.map(_.toLocalDate.format(dateTimeFormat())),
        submissionReceiptNumber = submission.submissionReceipt,
        table                   = buildTable(fullReturn, utrn)
      )
    }

  private def purchaserName(fullReturn: FullReturn): String =
    fullReturn.purchaser.flatMap(_.headOption).map(displayName).getOrElse("")

  private def displayName(purchaser: Purchaser): String =
    if (purchaser.isCompany.contains("yes")) {
      purchaser.companyName.getOrElse("")
    } else {
      Seq(purchaser.forename1, purchaser.forename2, purchaser.surname).flatten.mkString(" ")
    }

  private def vendorName(fullReturn: FullReturn): String =
    fullReturn.vendor.flatMap(_.headOption).flatMap(_.name).getOrElse("")

  private def agentReference(fullReturn: FullReturn): Option[String] =
    fullReturn.returnAgent
      .flatMap(_.find(_.agentType.contains("PURCHASER")))
      .flatMap(_.reference)

  private def transactionTypeDisplay(fullReturn: FullReturn)(implicit messages: Messages): String =
    TransactionType.parse(fullReturn.transaction.flatMap(_.transactionDescription))
      .map(transactionType => messages(s"prelim.transactionType.${transactionType.toString}"))
      .getOrElse("")

  private def formattedAddress(land: Land): String =
    Seq(land.houseNumber, land.address1, land.address2, land.address3, land.address4, land.postcode)
      .flatten
      .mkString(", ")

  private def formattedEffectiveDate(fullReturn: FullReturn)(implicit lang: Lang): String =
    fullReturn.transaction
      .flatMap(_.effectiveDate)
      .flatMap(parseDate(_).toOption)
      .map(_.format(dateTimeFormat()))
      .getOrElse("")

  private def buildTable(fullReturn: FullReturn, utrn: String)(implicit messages: Messages): Table = {
    implicit val lang: Lang = messages.lang
    val land = fullReturn.land.flatMap(_.headOption)

    val requiredRows: Seq[Seq[TableRow]] = Seq(
      row("submission.submissionReceipt.table.utrn", utrn),
      row("submission.submissionReceipt.table.address", land.map(formattedAddress).getOrElse("")),
      row("submission.submissionReceipt.table.purchaser", purchaserName(fullReturn)),
      row("submission.submissionReceipt.table.vendor", vendorName(fullReturn)),
      row("submission.submissionReceipt.table.transactionType", transactionTypeDisplay(fullReturn)),
      row("submission.submissionReceipt.table.effectiveDate", formattedEffectiveDate(fullReturn))
    )

    val optionalRows: Seq[Seq[TableRow]] = Seq(
      agentReference(fullReturn).map(row("submission.submissionReceipt.table.agentRef", _)),
      land.flatMap(_.titleNumber).map(row("submission.submissionReceipt.table.titleNumber", _)),
      land.flatMap(_.NLPGUPRN).map(row("submission.submissionReceipt.table.uprn", _))
    ).flatten

    Table(rows = requiredRows ++ optionalRows)
  }

  private def row(labelKey: String, value: String)(implicit messages: Messages): Seq[TableRow] =
    Seq(
      TableRow(content = Text(messages(labelKey)), classes = bold),
      TableRow(content = Text(value))
    )
}