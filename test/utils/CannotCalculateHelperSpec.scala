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
import models.{FullReturn, Land, Lease, Residency, ReturnInfo, Transaction, UserAnswers}
import org.scalatest.matchers.must.Matchers
import utils.CannotCalculateHelper.getCannotCalculateReason

class CannotCalculateHelperSpec extends SpecBase with Matchers {

  private val leaseholdAnswers: UserAnswers =
    emptyUserAnswers
      .copy(fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
        transaction = Some(Transaction(
          effectiveDate = Some("2026-01-01"),
          totalConsideration = Some("a"),
          claimingRelief = Some("no"),
          transactionDescription = Some("L"),
          isLinked = Some("no")
        )),
        residency = Some(Residency(isNonUkResidents = Some("no"))),
        land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("LG")))),
        lease = Some(Lease(isAnnualRentOver1000 = Some("no")))
      )))

  ".getCannotCalculateReason" - {

    "must return an empty list when no reasons match" in {
      getCannotCalculateReason(leaseholdAnswers) mustEqual List()
    }

    "must return a list with a single value when reason1 matches" in {
      val isLinked = leaseholdAnswers.copy(fullReturn = leaseholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(isLinked = Some("yes"))))
      ))

      getCannotCalculateReason(isLinked) mustEqual List("reason1")
    }

    "must return a list with a single value when reason2 matches" in {
      val partialRelief = leaseholdAnswers.copy(fullReturn = leaseholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(reliefAmount = Some("5000"))))
      ))

      getCannotCalculateReason(partialRelief) mustEqual List("reason2")
    }

      "must return a list with a single value when reason3 matches" in {
        val interestTransferred = leaseholdAnswers.copy(fullReturn = leaseholdAnswers.fullReturn.map(fr =>
          fr.copy(land = fr.land.map(_.map(_.copy(interestCreatedTransferred = Some("OT")))))
        ))

        getCannotCalculateReason(interestTransferred) mustEqual List("reason3")
      }

    "must return a list with a single value when reason4 matches" in {
      val multipleDwellingsRelief = leaseholdAnswers.copy(fullReturn = leaseholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(reliefReason = Some("33"))))
      ))

      getCannotCalculateReason(multipleDwellingsRelief) mustEqual List("reason4")
    }

    "must return a list with a single value when reason5 matches" in {
      val effectiveDate = leaseholdAnswers.copy(fullReturn = leaseholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(effectiveDate = Some("01/01/2012"))))
      ))

      getCannotCalculateReason(effectiveDate) mustEqual List("reason5")
    }

    "must return a list with a single value when reason6 matches" in {
      val collectiveEnfranchisement = leaseholdAnswers.copy(fullReturn = leaseholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(reliefReason = Some("25"))))
      ))

      getCannotCalculateReason(collectiveEnfranchisement) mustEqual List("reason6")
    }

    "must return a list with a single value when reason7 matches" in {
      val predatesCalc1 = leaseholdAnswers.copy(fullReturn = leaseholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(reliefReason = Some("32"), effectiveDate = Some("24/03/2012"))))
      ))

      getCannotCalculateReason(predatesCalc1) mustEqual List("reason7")
    }

    "must return a list with a single value when reason8 matches" in {
      val predatesCalc2 = leaseholdAnswers.copy(fullReturn = leaseholdAnswers.fullReturn.map(fr =>
        fr.copy(
          transaction = fr.transaction.map(_.copy(effectiveDate = Some("24/03/2012"))),
          lease = fr.lease.map(_.copy(isAnnualRentOver1000 = Some("yes"))),
          land = fr.land.map(_.map(_.copy(propertyType = Some("02")))))
      ))

      getCannotCalculateReason(predatesCalc2) mustEqual List("reason8")
    }

    "must return a list with multiple values when more than one reason matches" in {
      val isLinkedAndPartialRelief = leaseholdAnswers.copy(fullReturn = leaseholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(isLinked = Some("yes"), reliefAmount = Some("5000"))))
      ))

      getCannotCalculateReason(isLinkedAndPartialRelief) mustEqual List("reason1", "reason2")
    }
  }

}
