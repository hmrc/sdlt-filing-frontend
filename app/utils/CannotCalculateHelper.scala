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
import utils.EffectiveDateHelper.isBeforeMinimumEffectiveDate

object CannotCalculateHelper {
  
  def cannotCalculateReason(answers: UserAnswers): Option[String] = {
    val isLinked = answers.fullReturn.flatMap(_.transaction.flatMap(_.isLinked))
    val partialRelief = answers.fullReturn.flatMap(_.transaction.flatMap(_.reliefAmount))
    val interestTransferred =
      answers.fullReturn.flatMap(_.land.flatMap(_.find(_.interestCreatedTransferred.isDefined).flatMap(_.interestCreatedTransferred)))
    val reliefReason = answers.fullReturn.flatMap(_.transaction.flatMap(_.reliefReason))

    (isLinked, partialRelief, interestTransferred, reliefReason) match {
      case (Some("yes"), _, _, _) =>
        Some("taxCalculation.cannotCalculateSdltDue.reason1")
      case (_, Some(value), _, _) =>
        Some("taxCalculation.cannotCalculateSdltDue.reason2")
      case (_, _, Some("OT"), _) =>
        Some("taxCalculation.cannotCalculateSdltDue.reason3")
      case (_, _, _, Some("33")) =>
        Some("taxCalculation.cannotCalculateSdltDue.reason4")
      case (_, _, _, _) if isBeforeMinimumEffectiveDate(answers) =>
        Some("taxCalculation.cannotCalculateSdltDue.reason5")
      case (_, _, _, _) =>
        None
    }
  }

}
