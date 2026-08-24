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

import models.UserAnswers

import java.time.LocalDate

object SharedOwnershipLeaseHelper {

  private val firstTimeBuyerReliefStartDate: LocalDate = LocalDate.of(2017, 11, 22)

  def shouldDisplayNotification(answers: UserAnswers): Boolean = {
    val transaction = answers.fullReturn.flatMap(_.transaction)

    val effectiveDateOnOrAfterCutoff = transaction
      .flatMap(_.effectiveDate)
      .flatMap(PropertyTypeHelper.parseEffectiveDate)
      .exists(!_.isBefore(firstTimeBuyerReliefStartDate))

    val isGrantOfLease         = transaction.flatMap(_.transactionDescription).contains("L")
    val isClaimingRelief       = transaction.flatMap(_.claimingRelief).contains("yes")
    val isFirstTimeBuyerRelief = transaction.flatMap(_.reliefReason).contains("32")

    effectiveDateOnOrAfterCutoff && isGrantOfLease && isClaimingRelief && isFirstTimeBuyerRelief
  }
}
