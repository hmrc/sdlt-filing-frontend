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

import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate

class PenaltyCalculatorSpec extends SpecBase with MockitoSugar {
  
  val fakePenaltyCalculator: PenaltyCalculator = new PenaltyCalculator {
    override def today: LocalDate = LocalDate.of(2026,5,7)

  }
  "penaltyCalculationService" - {

    "calculatePenalties" - {
      "must return 200£ penalty when effective date is onOrBefore  maximumPenaltyDate" in {
        val effectiveDate: LocalDate = LocalDate.of(2025, 12, 1)
        fakePenaltyCalculator.calculatePenalties(effectiveDate) mustBe 200
      }

      "must return 100£ penalty when effectiveDate is before minimumPenaltyDate and after maximumPenaltyDate" in {
        val effectiveDate: LocalDate = LocalDate.of(2026, 3, 1)
        fakePenaltyCalculator.calculatePenalties(effectiveDate) mustBe 100

      }
      "must return 0£ penalty when effective date is too recent" in {
        val effectiveDate: LocalDate = LocalDate.of(2026,4,25)
        fakePenaltyCalculator.calculatePenalties(effectiveDate) mustBe 0

      }
    }

  }

}
