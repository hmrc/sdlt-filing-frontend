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

import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import utils.TaxCalculationPenaltiesHelper.getPenalties

import java.time.LocalDate

class TaxCalculationPenaltiesHelperSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  private def timeMachineFor(today: LocalDate): TimeMachine = {
    val tm = mock[TimeMachine]
    when(tm.today).thenReturn(today)
    tm
  }

  ".getPenalties (14-day filing window for transactions on/after 2019-03-01)" - {

    val Today = LocalDate.of(2026, 5, 1)
    val tm    = timeMachineFor(Today)

    "is £0 when the transaction is today (0 days)" in {
      getPenalties(Today, tm) mustBe BigDecimal(0)
    }

    "is £0 at the boundary of the 14-day window" in {
      getPenalties(Today.minusDays(14), tm) mustBe BigDecimal(0)
    }

    "is £100 one day past the 14-day window" in {
      getPenalties(Today.minusDays(15), tm) mustBe BigDecimal(100)
    }

    "is £100 in the middle of the £100 band" in {
      getPenalties(Today.minusDays(60), tm) mustBe BigDecimal(100)
    }

    "is £100 one day before the £200 threshold" in {
      getPenalties(Today.minusDays(122), tm) mustBe BigDecimal(100)
    }

    "is £200 at the 123-day threshold" in {
      getPenalties(Today.minusDays(123), tm) mustBe BigDecimal(200)
    }

    "is £200 well past the 123-day threshold" in {
      getPenalties(Today.minusDays(365), tm) mustBe BigDecimal(200)
    }

    "is £0 for a future-dated transaction (daysSince < 0)" in {
      getPenalties(Today.plusDays(7), tm) mustBe BigDecimal(0)
    }
  }

  ".getPenalties (30-day filing window for transactions before 2019-03-01)" - {

    val PreCutoverToday = LocalDate.of(2018, 12, 1)
    val tm = timeMachineFor(PreCutoverToday)

    "is £0 at the boundary of the 30-day window" in {
      getPenalties(PreCutoverToday.minusDays(30), tm) mustBe BigDecimal(0)
    }

    "is £100 one day past the 30-day window" in {
      getPenalties(PreCutoverToday.minusDays(31), tm) mustBe BigDecimal(100)
    }

    "is £200 at the 123-day threshold" in {
      getPenalties(PreCutoverToday.minusDays(123), tm) mustBe BigDecimal(200)
    }

    "uses the 30-day window for a transaction the day before the cut-over" in {
      val cutoverEve = LocalDate.of(2019, 2, 28)
      getPenalties(cutoverEve, timeMachineFor(cutoverEve.plusDays(20))) mustBe BigDecimal(0)
    }

    "uses the 14-day window for a transaction on the cut-over date" in {
      val cutover = LocalDate.of(2019, 3, 1)
      getPenalties(cutover, timeMachineFor(cutover.plusDays(20))) mustBe BigDecimal(100)
    }
  }
}
