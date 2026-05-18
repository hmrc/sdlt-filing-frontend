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

import base.SpecBase
import models.{FullReturn, Land, Residency, ReturnInfo, Transaction, UserAnswers}
import org.scalatest.matchers.must.Matchers
import utils.EffectiveDateHelper.isBeforeMinimumEffectiveDate

class EffectiveDateHelperSpec extends SpecBase with Matchers {

  private val freeholdAnswers: UserAnswers =
    emptyUserAnswers
      .copy(fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
        transaction = Some(Transaction(
          effectiveDate = Some("2026-01-01"),
          totalConsideration = Some(BigDecimal(300000)),
          claimingRelief = Some("no"),
          transactionDescription = Some("F"),
          isLinked = Some("yes")
        )),
        residency = Some(Residency(isNonUkResidents = Some("no"))),
        land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("LG")))),
      )))

  ".isBeforeMinimumEffectiveDate" - {

    "must return true when the date is before the minimum effective date" in {
      val beforeMinumumDate = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(effectiveDate = Some("2008-01-01"))))
      ))

      isBeforeMinimumEffectiveDate(beforeMinumumDate) mustEqual true
    }

    "must return true when the date is a day before the minimum effective date boundary" in {
      val beforeMinumumDate = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(effectiveDate = Some("2012-03-21"))))
      ))

      isBeforeMinimumEffectiveDate(beforeMinumumDate) mustEqual true
    }

    "must return false when the date is on the minimum effective date boundary" in {
      val onMinumumDate = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(effectiveDate = Some("2012-03-22"))))
      ))

      isBeforeMinimumEffectiveDate(onMinumumDate) mustEqual false
    }

    "must return false when the date is after the minimum effective date" in {
      val afterMinumumDate = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(effectiveDate = Some("2019-03-22"))))
      ))

      isBeforeMinimumEffectiveDate(afterMinumumDate) mustEqual false
    }
  }

}
