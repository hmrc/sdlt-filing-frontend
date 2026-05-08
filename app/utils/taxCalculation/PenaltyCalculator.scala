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

package utils.taxCalculation

import utils.taxCalculation.PenaltiesValue.{maximumPenalty, minimumPenalty, noPenalty}

import java.time.LocalDate

trait PenaltyCalculator extends DateUtil {

  private val maximumPenaltyDays: Long = 123
  private val effectiveDateAfter1March2019MinimumDays: Long = 14
  private val effectiveDateBefore1March2019MinimumDays: Long = 30
  private val March2019Date: LocalDate = LocalDate.of(2019, 3, 1)

  def today: LocalDate = LocalDate.now()

  private def maximumPenaltyDate: LocalDate = today.minusDays(maximumPenaltyDays)

  def calculatePenalties(effectiveDate: LocalDate): BigDecimal = {
    val minimumPenaltyDate = calculateMinimumPenaltyDate(effectiveDate)
    if(effectiveDate.isBefore(minimumPenaltyDate) && effectiveDate.isAfter(maximumPenaltyDate)) minimumPenalty
    else if(effectiveDate.onOrBefore(maximumPenaltyDate)) maximumPenalty
    else noPenalty
  }
  
  private def calculateMinimumPenaltyDate(effectiveDate:LocalDate):LocalDate = {
    if(effectiveDate.onOrAfter(March2019Date)){
      today.minusDays(effectiveDateAfter1March2019MinimumDays)
    }
    else {
      today.minusDays(effectiveDateBefore1March2019MinimumDays)
    }
  }

}
