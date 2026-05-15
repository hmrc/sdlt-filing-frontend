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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import base.SpecBase
import org.scalatest.EitherValues._
import models.{FullReturn, Land, ReturnInfo, Transaction}


class EffectiveDateHelperSpec extends AnyFreeSpec with SpecBase with Matchers {

  private def userAnswersWithValidEffectiveDate(effectiveDate:String) = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
    stornId = "STORN",
    returnResourceRef = "REF",
    returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
    transaction = Some(Transaction(
      effectiveDate = Some(effectiveDate),
      totalConsideration = Some(BigDecimal(300000)),
      claimingRelief = Some("no"),
      transactionDescription = Some("F"),
      isLinked = Some("no")
    ))
  )))

  ".getEffectiveDate" - {

    "must format  and return effective date in English from transaction" in {

      val effectiveDate:String = "2020-06-07"

      val result = EffectiveDateHelper.getEffectiveDate(userAnswersWithValidEffectiveDate(effectiveDate))

      result.value mustBe "7 June 2020"

    }

    "must return MissingFullReturnError when fullReturn data is empty" in {

    }

    "must return MissingAboutTheTransaction error when the transaction data is empty" in {

    }

    "must return MissingTransactionAnswerError when effective data is invalid" in {

    }

    "must return InvalidDate error when the effective date is invalid " in {

    }


  }

}
