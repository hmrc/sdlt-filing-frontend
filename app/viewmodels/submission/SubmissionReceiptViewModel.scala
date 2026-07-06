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
import utils.DateTimeFormats.dateTimeFormat

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

case class SubmissionReceiptViewModel(
                                        purchaserName: String,
                                        submissionTime: String,
                                        submissionDate: String,
                                        utrn: String,
                                        table: Table
                                      )

object SubmissionReceiptViewModel {

  private val timeFormatter = DateTimeFormatter.ofPattern("h:mma")
  private val bold = "govuk-!-font-weight-bold"

  def apply(fullReturn: FullReturn)(implicit messages: Messages): Option[SubmissionReceiptViewModel] =
    for {
      submission  <- fullReturn.submission
      utrn        <- submission.UTRN
      requestDate <- submission.submissionRequestDate
    } yield {
      implicit val lang: Lang = messages.lang
      val parsedDate = ZonedDateTime.parse(requestDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

      SubmissionReceiptViewModel(
        purchaserName  = purchaserName(fullReturn),
        submissionTime = parsedDate.format(timeFormatter).toLowerCase,
        submissionDate = parsedDate.toLocalDate.format(dateTimeFormat()),
        utrn           = utrn,
        table          = buildTable(fullReturn, utrn)
      )
    }

  private def purchaserName(fullReturn: FullReturn): String =
    fullReturn.purchaser.flatMap(_.headOption).map(displayName).getOrElse("")

  private def displayName(purchaser: Purchaser): String =
    if (purchaser.isCompany.contains("YES")) {
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

  private def buildTable(fullReturn: FullReturn, utrn: String)(implicit messages: Messages): Table = {
    val land = fullReturn.land.flatMap(_.headOption)

    val requiredRows: Seq[Seq[TableRow]] = Seq(
      row("submission.submissionReceipt.table.utrn", utrn),
      row("submission.submissionReceipt.table.address", land.map(formattedAddress).getOrElse("")),
      row("submission.submissionReceipt.table.purchaser", purchaserName(fullReturn)),
      row("submission.submissionReceipt.table.vendor", vendorName(fullReturn)),
      row("submission.submissionReceipt.table.transactionType", transactionTypeDisplay(fullReturn)),
      row("submission.submissionReceipt.table.effectiveDate", fullReturn.transaction.flatMap(_.effectiveDate).getOrElse(""))
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
