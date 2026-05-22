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

object SelfAssessedHelper {

  private val minimumEffectiveDate: LocalDate = LocalDate.of(2012, 3, 22)
  private val residentialPropertyTypes: Set[String] = Set("01", "04")

  def isResidentialBeforeMarch2012Date(answers: UserAnswers): Boolean =
    isBeforeMinimumEffectiveDate(answers) && mainLandIsResidentialProperty(answers)

  private def isBeforeMinimumEffectiveDate(answers: UserAnswers): Boolean =
    answers.fullReturn
      .flatMap(_.transaction.flatMap(_.effectiveDate))
      .flatMap(PropertyTypeHelper.parseEffectiveDate)
      .exists(_.isBefore(minimumEffectiveDate))

  private def mainLandIsResidentialProperty(answers: UserAnswers): Boolean =
    answers.fullReturn.exists { fullReturn =>
      val mainLandId = fullReturn.returnInfo.flatMap(_.mainLandID)

      fullReturn.land
        .flatMap(_.find(land => land.landID == mainLandId))
        .flatMap(_.propertyType)
        .exists(residentialPropertyTypes.contains)
    }

}
