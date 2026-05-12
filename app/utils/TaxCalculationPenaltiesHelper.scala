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

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object TaxCalculationPenaltiesHelper {

  private val FOURTEEN_DAY_RULE_FROM     = LocalDate.of(2019, 3, 1)
  private val NEW_FILING_WINDOW_DAYS     = 14L
  private val OLD_FILING_WINDOW_DAYS     = 30L
  private val MAX_PENALTY_THRESHOLD_DAYS = 123L

  private val NoPenalty:        BigDecimal = BigDecimal(0)
  private val OneHundredPounds: BigDecimal = BigDecimal(100)
  private val TwoHundredPounds: BigDecimal = BigDecimal(200)

  def getPenalties(effectiveDate: LocalDate, timeMachine: TimeMachine): BigDecimal = {
    val daysSince    = ChronoUnit.DAYS.between(effectiveDate, timeMachine.today)
    val filingWindow = if (effectiveDate.isBefore(FOURTEEN_DAY_RULE_FROM)) OLD_FILING_WINDOW_DAYS else NEW_FILING_WINDOW_DAYS

    if      (daysSince >= MAX_PENALTY_THRESHOLD_DAYS) TwoHundredPounds
    else if (daysSince >  filingWindow)               OneHundredPounds
    else                                              NoPenalty
  }
}
