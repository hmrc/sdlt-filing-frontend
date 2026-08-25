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
import models.{FullReturn, ReturnInfo, Transaction, UserAnswers}
import utils.SharedOwnershipLeaseHelper.shouldDisplayNotification

class SharedOwnershipLeaseHelperSpec extends SpecBase {

  private def answersWith(
                            effectiveDate: Option[String] = Some("2018-01-01"),
                            transactionDescription: Option[String] = Some("L"),
                            claimingRelief: Option[String] = Some("yes"),
                            reliefReason: Option[String] = Some("32")
                          ): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(FullReturn(
      stornId           = "STORN",
      returnResourceRef = "REF",
      returnInfo        = Some(ReturnInfo(mainLandID = Some("L1"))),
      transaction       = Some(Transaction(
        effectiveDate           = effectiveDate,
        transactionDescription  = transactionDescription,
        claimingRelief          = claimingRelief,
        reliefReason            = reliefReason
      ))
    )))

  ".shouldDisplayNotification" - {

    "must return true when all four conditions are met" in {
      shouldDisplayNotification(answersWith()) mustBe true
    }

    "must return true when claiming relief is 'YES' in a different case" in {
      shouldDisplayNotification(answersWith(claimingRelief = Some("YES"))) mustBe true
    }

    "must return true when the effective date is exactly on the cutoff date (22/11/2017)" in {
      shouldDisplayNotification(answersWith(effectiveDate = Some("2017-11-22"))) mustBe true
    }

    "must accept the effective date in dd/MM/yyyy format" in {
      shouldDisplayNotification(answersWith(effectiveDate = Some("22/11/2017"))) mustBe true
    }

    "must return false when the effective date is one day before the cutoff date" in {
      shouldDisplayNotification(answersWith(effectiveDate = Some("2017-11-21"))) mustBe false
    }

    "must return false when the effective date is absent" in {
      shouldDisplayNotification(answersWith(effectiveDate = None)) mustBe false
    }

    "must return false when the effective date is unparseable" in {
      shouldDisplayNotification(answersWith(effectiveDate = Some("not-a-date"))) mustBe false
    }

    "must return false when the transaction description is not 'L'" in {
      shouldDisplayNotification(answersWith(transactionDescription = Some("F"))) mustBe false
    }

    "must return false when the transaction description is absent" in {
      shouldDisplayNotification(answersWith(transactionDescription = None)) mustBe false
    }

    "must return false when claiming relief is 'no'" in {
      shouldDisplayNotification(answersWith(claimingRelief = Some("no"))) mustBe false
    }

    "must return false when claiming relief is absent" in {
      shouldDisplayNotification(answersWith(claimingRelief = None)) mustBe false
    }

    "must return false when the relief reason is not '32'" in {
      shouldDisplayNotification(answersWith(reliefReason = Some("08"))) mustBe false
    }

    "must return false when the relief reason is absent" in {
      shouldDisplayNotification(answersWith(reliefReason = None)) mustBe false
    }

    "must return false when fullReturn is absent" in {
      shouldDisplayNotification(emptyUserAnswers) mustBe false
    }

    "must return false when transaction is absent" in {
      val answers = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
        stornId           = "STORN",
        returnResourceRef = "REF",
        returnInfo        = Some(ReturnInfo(mainLandID = Some("L1")))
      )))
      shouldDisplayNotification(answers) mustBe false
    }
  }
}
