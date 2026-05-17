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

package utils

import base.SpecBase
import models.taxCalculation.{InvalidDateError, MissingFullReturnError, MissingTransactionAnswerError, MissingAboutTheTransactionError}
import models.{FullReturn, Land, ReturnInfo, Transaction}
import org.scalatest.EitherValues.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers


class EffectiveDateHelperSpec extends AnyFreeSpec with SpecBase with Matchers {

  private def transaction(effectiveDate: Option[String]) = Some(Transaction(
    effectiveDate = effectiveDate,
    totalConsideration = Some(BigDecimal(300000)),
    claimingRelief = Some("no"),
    transactionDescription = Some("F"),
    isLinked = Some("no")
  ))

  private def userAnswers(transaction: Option[Transaction] = None) =
    emptyUserAnswers.copy(fullReturn = Some(FullReturn(
      stornId = "STORN",
      returnResourceRef = "REF",
      returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
      transaction = transaction
    )))

  ".getEffectiveDate" - {

    "must format  and return effective date in English from transaction when valid effective date of transaction is present" in {

      val validEffectiveDate: String = "2020-06-07"
      val validTransaction = transaction(Some(validEffectiveDate))

      val result = EffectiveDateHelper.getEffectiveDate(userAnswers(validTransaction))

      result.value mustEqual "7 June 2020"

    }

    "must return MissingFullReturnError when fullReturn data is empty" in {

      val result = EffectiveDateHelper.getEffectiveDate(emptyUserAnswers)

      result.left.value mustBe MissingFullReturnError

    }

    "must return MissingAboutTheTransaction error when the transaction data is empty" in {

      val result = EffectiveDateHelper.getEffectiveDate(userAnswers())

      result.left.value mustBe MissingAboutTheTransactionError

    }

    "must return MissingTransactionAnswerError when effective date of transaction is empty" in {

      val transactionWithoutEffectiveDate = transaction(None)

      val result = EffectiveDateHelper.getEffectiveDate(userAnswers(transactionWithoutEffectiveDate))

      result.left.value mustBe MissingTransactionAnswerError("effectiveDate")

    }

    "must return InvalidDate error when the effective date of transactions is invalid " in {

      val invalidDate: String = "12-31-20"
      val transactionWithInvalidEffectiveDate = transaction(Some(invalidDate))

      val result = EffectiveDateHelper.getEffectiveDate(userAnswers(transactionWithInvalidEffectiveDate))

      result.left.value mustBe InvalidDateError(invalidDate)

    }


  }

}
