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
import utils.CannotCalculateHelper.getCannotCalculateReason

class CannotCalculateHelperSpec extends SpecBase with Matchers {

  private val freeholdAnswers: UserAnswers =
    emptyUserAnswers
      .copy(fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
        transaction = Some(Transaction(
          effectiveDate = Some("2026-01-01"),
          totalConsideration = Some("a"),
          claimingRelief = Some("no"),
          transactionDescription = Some("F"),
          isLinked = Some("no")
        )),
        residency = Some(Residency(isNonUkResidents = Some("no"))),
        land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("LG")))),
      )))

  ".getCannotCalculateReason" - {

    "must return reason 1 when the return isLinked" in {
      val isLinked = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(isLinked = Some("yes"))))
      ))

      getCannotCalculateReason(isLinked) mustEqual Some("taxCalculation.cannotCalculateSdltDue.reason1")
    }

    "must return reason 2 when the return includes partial relief" in {
      val partialRelief = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(reliefAmount = Some("5000"))))
      ))

      getCannotCalculateReason(partialRelief) mustEqual Some("taxCalculation.cannotCalculateSdltDue.reason2")
    }

    "must return reason 3 when the return is OT" in {
      val isOT = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(land = fr.land.map(_.map(_.copy(interestCreatedTransferred = Some("OT")))))
      ))

      getCannotCalculateReason(isOT) mustEqual Some("taxCalculation.cannotCalculateSdltDue.reason3")
    }

    "must return reason 4 when the return is has Multiple Dwellings Relief" in {
      val multipleDwellingsRelief = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(reliefReason = Some("33"))))
      ))

      getCannotCalculateReason(multipleDwellingsRelief) mustEqual Some("taxCalculation.cannotCalculateSdltDue.reason4")
    }

    "must return reason 5 when the effectiveDate is before the minimum" in {
      val beforeMinumumDate = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(effectiveDate = Some("2008-01-01"))))
      ))

      getCannotCalculateReason(beforeMinumumDate) mustEqual Some("taxCalculation.cannotCalculateSdltDue.reason5")
    }

    "must return None when no reasons apply" in {
      getCannotCalculateReason(freeholdAnswers) mustEqual None
    }
  }

}
