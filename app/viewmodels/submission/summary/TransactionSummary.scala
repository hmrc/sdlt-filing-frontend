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

package viewmodels.submission.summary

import models.address.Address
import models.address.Address.toHtml
import models.prelimQuestions.TransactionType
import models.transaction.{TransactionFormsOfConsiderationAnswers, TransactionRulingFollowed, TransactionSaleOfBusinessAssetsAnswers, TransactionUseOfLandOrPropertyAnswers}
import models.{FullReturn, Transaction}
import play.api.i18n.{Lang, Messages}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist.*
import viewmodels.submission.summary.SummaryUtil.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

object TransactionSummary {

  def getSummaryCard(fullReturn: FullReturn)(implicit messages: Messages): Option[SummaryList] = {
    implicit val lang: Lang = messages.lang
    fullReturn.transaction.flatMap { transaction =>
      TransactionType.parse(transaction.transactionDescription).map { transactionType =>
        SummaryListViewModel(
          Seq(
            getOptSummaryRow(
              messages("transaction.transactionEffectiveDate.checkYourAnswersLabel"),
              transaction.effectiveDate.flatMap(effectiveDate =>
                Try(
                  LocalDate.parse(effectiveDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")).format(dateTimeFormat())
                ).toOption
              )
            ),
            getOptSummaryRow(
              messages("transaction.transactionDateOfContract.checkYourAnswersLabel"),
              transaction.contractDate.flatMap(effectiveDate =>
                Try(
                  LocalDate.parse(effectiveDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")).format(dateTimeFormat())
                ).toOption
              )
            ),
            getOptSummaryRow(
              messages("transaction.totalConsiderationOfTransaction.checkYourAnswersLabel"),
              getOptMoneyValue(transaction.totalConsideration)
            ),
            getOptSummaryRow(
              messages("transaction.vatAmount.checkYourAnswersLabel"),
              getOptMoneyValue(transaction.considerationVAT)
            ),
            getOptSummaryRowHtml(
              messages("transaction.transactionFormsOfConsideration.checkYourAnswersLabel"),
              getFormsOfConsiderationHtml(transaction)
            ),
            getOptSummaryRow(
              messages("transaction.linkedTransactions.checkYourAnswersLabel"),
              getOptYesNo(transaction.isLinked)
            ),
            getOptSummaryRow(
              messages("transaction.totalConsiderationOfLinkedTransaction.checkYourAnswersLabel"),
              getOptMoneyValue(transaction.totalConsiderationLinked)
            ),
            getOptSummaryRow(
              messages("transaction.purchaserEligibleToClaimRelief.checkYourAnswersLabel"),
              getOptYesNo(transaction.claimingRelief)
            ),
            getOptSummaryRow(
              messages("transaction.ReasonForRelief.checkYourAnswersLabel"),
              transaction.reliefReason.map(reason => messages(s"transaction.ReasonForRelief.$reason"))
            ),
            getOptSummaryRow(
              messages("transaction.charityRegisteredNumber.checkYourAnswersLabel"),
              transaction.reliefSchemeNumber.flatMap(reliefSchemeNumber => Option.when(transaction.reliefReason.contains("20"))(reliefSchemeNumber))
            ),
            getOptSummaryRow(
              messages("transaction.cisNumber.checkYourAnswersLabel"),
              transaction.reliefSchemeNumber.flatMap(reliefSchemeNumber => Option.when(transaction.reliefReason.contains("08"))(reliefSchemeNumber))
            ),
            getOptSummaryRow(
              messages("transaction.claimingPartialReliefAmount.checkYourAnswersLabel"),
              getOptMoneyValue(transaction.reliefAmount)
            ),
            getOptSummaryRow(
              messages("transaction.considerationsAffectedUncertain.checkYourAnswersLabel"),
              getOptYesNo(transaction.isDependantOnFutureEvent)
            ),
            getOptSummaryRow(
              messages("transaction.deferringPayment.checkYourAnswersLabel"),
              getOptYesNo(transaction.agreedToDeferPayment)
            ),
            getOptSummaryRowHtml(
              messages("transaction.transactionUseOfLandOrProperty.checkYourAnswersLabel"),
              getUseOfLandOrPropertyHtml(transaction)
            ),
            getOptSummaryRowHtml(
              messages("transaction.transactionSaleOfBusinessAssets.checkYourAnswersLabel"),
              getIncludedInSaleOfBusinessHtml(transaction)
            ),
            getOptSummaryRow(
              messages("transaction.totalAssetsConsideration.checkYourAnswersLabel"),
              getOptMoneyValue(transaction.totalConsiderationBusiness)
            ),
            getOptSummaryRow(
              messages("transaction.cap1OrNsbc.checkYourAnswersLabel"),
              getOptYesNo(transaction.postTransRulingApplied)
            ),
            getOptSummaryRow(
              messages("transaction.rulingFollowed.checkYourAnswersLabel"),
              TransactionRulingFollowed.parse(transaction.postTransRulingFollowed).map(rulingFollowed =>
                messages(s"transaction.rulingFollowed.${rulingFollowed.toString}")
              )
            ),
            getOptSummaryRow(
              messages("transaction.descriptionOfRestrictions.checkYourAnswersLabel"),
              transaction.restrictionDetails
            ),
            getOptSummaryRowHtml(
              messages("transaction.address.checkYourAnswersLabel"),
              transaction.exchangedLandAddress1.map(address1 =>
                HtmlContent(toHtml(
                  Address(
                    line1 = address1,
                    line2 = transaction.exchangedLandAddress2,
                    line3 = transaction.exchangedLandAddress3,
                    line4 = transaction.exchangedLandAddress4,
                    postcode = transaction.exchangedLandPostcode
                  )
                ))
              )
            ),
            getOptSummaryRow(
              messages("transaction.transactionExercisingAnOption.checkYourAnswersLabel"),
              getOptYesNo(transaction.isPursuantToPreviousOption)
            )
          ).flatMap(_.toSeq)
        ).withCard(
          messages(
            "submission.completedSdltReturn.transaction.header",
            messages(s"prelim.transactionType.$transactionType")
          )
        )
      }
    }
  }

  private def getFormsOfConsiderationHtml(transaction: Transaction)(implicit messages: Messages): Option[HtmlContent] = {
    TransactionFormsOfConsiderationAnswers.fromTransaction(transaction).map { answers =>
      val selected = TransactionFormsOfConsiderationAnswers.toSet(answers).toSeq.map(_.toString)
      HtmlContent(
        selected.map { answer =>
          HtmlFormat.escape(messages(s"transaction.transactionFormsOfConsideration.$answer")).toString
        }.mkString(",<br>")
      )
    }
  }

  private def getUseOfLandOrPropertyHtml(transaction: Transaction)(implicit messages: Messages): Option[HtmlContent] = {
    TransactionUseOfLandOrPropertyAnswers.fromTransaction(transaction).map { answers =>
      val selected = TransactionUseOfLandOrPropertyAnswers.toSet(answers).toSeq.sortBy(_.order)
      HtmlContent(
        selected.map { answer =>
          HtmlFormat.escape(messages(s"transaction.transactionUseOfLandOrProperty.$answer")).toString
        }.mkString(",<br>")
      )
    }
  }

  private def getIncludedInSaleOfBusinessHtml(transaction: Transaction)(implicit messages: Messages): Option[HtmlContent] = {
    TransactionSaleOfBusinessAssetsAnswers.fromTransaction(transaction).map { answers =>
      val selected = TransactionSaleOfBusinessAssetsAnswers.toSet(answers).toSeq.sortBy(_.order)
      HtmlContent(
        selected.map { answer =>
          HtmlFormat.escape(messages(s"transaction.transactionSaleOfBusinessAssets.$answer")).toString
        }.mkString(",<br>")
      )
    }
  }
}
