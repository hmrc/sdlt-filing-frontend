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

package services.crossflow.errors

import base.SpecBase
import constants.FullReturnConstants.emptyFullReturn
import models.transaction.ReasonForRelief
import models.{Land, Transaction, UserAnswers}
import org.scalatest.matchers.must.Matchers
import pages.transaction.{ReasonForReliefPage, TransactionEffectiveDatePage}
import services.crossflow.*

import java.time.LocalDate

class F23ReliefReasonRulesSpec extends SpecBase with Matchers {

  private val freeportStart      = LocalDate.of(2021, 10, 19)
  private val freeportEnd        = LocalDate.of(2026,  9, 30)
  private val investmentStart    = LocalDate.of(2023,  9, 29)
  private val investmentEnd      = LocalDate.of(2034,  9, 30)
  private val reliefFloor2013    = LocalDate.of(2013,  3,  6)
  private val seedingFloor       = LocalDate.of(2025,  3, 19)
  private val mdrEffectiveCutOff = LocalDate.of(2024,  6,  1)
  private val mdrContractCutOff  = LocalDate.of(2024,  3,  7)

  private def answersWith(
                           claimingRelief: Option[String]     = Some("YES"),
                           reliefReason:   Option[ReasonForRelief] = None,
                           effectiveDate:  Option[LocalDate]  = None,
                           contractDate:   Option[String]     = None,
                           propertyType:   Option[String]     = None
                         ): UserAnswers = {
    val committedTransaction = Transaction(
      claimingRelief = claimingRelief,
      reliefReason   = reliefReason.flatMap {
        case ReasonForRelief.FirstTimeBuyer       => Some("32")
        case ReasonForRelief.MultipleDwellings    => Some("33")
        case ReasonForRelief.PreCompletion        => Some("34")
        case ReasonForRelief.ReliefFromRate       => Some("35")
        case ReasonForRelief.ReliefForFreeport    => Some("36")
        case ReasonForRelief.ReliefInvestmentZone => Some("37")
        case ReasonForRelief.SeedingRelief        => Some("38")
        case _                                    => None
      },
      effectiveDate  = effectiveDate.map(_.toString),
      contractDate   = contractDate
    )

    val committedLand = propertyType.map(t => Land(propertyType = Some(t)))

    val base = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
      transaction = Some(committedTransaction),
      land        = committedLand.map(l => Seq(l))
    )))

    val withReason = reliefReason.fold(base)(r => base.set(ReasonForReliefPage, r).success.value)
    val withDate   = effectiveDate.fold(withReason)(d => withReason.set(TransactionEffectiveDatePage, d).success.value)
    withDate
  }

  "FirstTimeBuyerRelief" - {

    "must fire when relief is claimed for code 32 and property type is not Residential" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), propertyType = Some("02"))

      FirstTimeBuyerRelief.validate(ua).map(_.ruleId) mustBe Some("F23-32")
    }

    "must pass when property type is Residential" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), propertyType = Some("01"))

      FirstTimeBuyerRelief.validate(ua) mustBe None
    }

    "must not apply when relief is not being claimed" in {
      val ua = answersWith(claimingRelief = Some("NO"), reliefReason = Some(ReasonForRelief.FirstTimeBuyer), propertyType = Some("02"))

      FirstTimeBuyerRelief.validate(ua) mustBe None
    }

    "must not apply when a different relief reason is selected" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("02"))

      FirstTimeBuyerRelief.validate(ua) mustBe None
    }

    "must pass when property type is not yet answered (incomplete, not in error)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), propertyType = None)

      FirstTimeBuyerRelief.validate(ua) mustBe None
    }
  }

  "MultipleDwellingsRelief" - {

    "must fire when property type is non-residential (03)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("03"))

      MultipleDwellingsRelief.validate(ua).map(_.ruleId) mustBe Some("F23-33")
    }

    "must pass when property type is Residential (01)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("01"))

      MultipleDwellingsRelief.validate(ua) mustBe None
    }

    "must pass when property type is Mixed (02)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("02"))

      MultipleDwellingsRelief.validate(ua) mustBe None
    }

    "must pass when property type is Residential Additional (04)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("04"))

      MultipleDwellingsRelief.validate(ua) mustBe None
    }

    "must not apply when a different relief reason is selected" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), propertyType = Some("03"))

      MultipleDwellingsRelief.validate(ua) mustBe None
    }
  }

  "PreCompletionRelief" - {

    "must fire when effective date is before 06/03/2013" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.PreCompletion), effectiveDate = Some(reliefFloor2013.minusDays(1)))

      PreCompletionRelief.validate(ua).map(_.ruleId) mustBe Some("F23-34")
    }

    "must pass when effective date is on the floor date" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.PreCompletion), effectiveDate = Some(reliefFloor2013))

      PreCompletionRelief.validate(ua) mustBe None
    }

    "must pass when effective date is after the floor date" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.PreCompletion), effectiveDate = Some(LocalDate.of(2024, 1, 1)))

      PreCompletionRelief.validate(ua) mustBe None
    }

    "must pass when effective date is not yet answered" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.PreCompletion), effectiveDate = None)

      PreCompletionRelief.validate(ua) mustBe None
    }
  }

  "FifteenPercentRateRelief" - {

    "must fire when effective date is before 06/03/2013" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefFromRate), effectiveDate = Some(reliefFloor2013.minusDays(1)))

      FifteenPercentRateRelief.validate(ua).map(_.ruleId) mustBe Some("F23-35")
    }

    "must pass when effective date is on or after the floor date" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefFromRate), effectiveDate = Some(reliefFloor2013))

      FifteenPercentRateRelief.validate(ua) mustBe None
    }
  }

  "FreeportRelief" - {

    "must fire when effective date is before the window opens" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefForFreeport), effectiveDate = Some(freeportStart.minusDays(1)))

      FreeportRelief.validate(ua).map(_.ruleId) mustBe Some("F23-36")
    }

    "must fire when effective date is after the window closes" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefForFreeport), effectiveDate = Some(freeportEnd.plusDays(1)))

      FreeportRelief.validate(ua).map(_.ruleId) mustBe Some("F23-36")
    }

    "must pass when effective date is on the window's start" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefForFreeport), effectiveDate = Some(freeportStart))

      FreeportRelief.validate(ua) mustBe None
    }

    "must pass when effective date is on the window's end" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefForFreeport), effectiveDate = Some(freeportEnd))

      FreeportRelief.validate(ua) mustBe None
    }

    "must pass when effective date is inside the window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefForFreeport), effectiveDate = Some(LocalDate.of(2023, 6, 1)))

      FreeportRelief.validate(ua) mustBe None
    }

    "must not apply when a different relief reason is selected" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(freeportEnd.plusDays(1)))

      FreeportRelief.validate(ua) mustBe None
    }
  }

  "InvestmentZoneRelief" - {

    "must fire when effective date is before the window opens" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefInvestmentZone), effectiveDate = Some(investmentStart.minusDays(1)))

      InvestmentZoneRelief.validate(ua).map(_.ruleId) mustBe Some("F23-37")
    }

    "must fire when effective date is after the window closes" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefInvestmentZone), effectiveDate = Some(investmentEnd.plusDays(1)))

      InvestmentZoneRelief.validate(ua).map(_.ruleId) mustBe Some("F23-37")
    }

    "must pass when effective date is inside the window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefInvestmentZone), effectiveDate = Some(LocalDate.of(2024, 1, 1)))

      InvestmentZoneRelief.validate(ua) mustBe None
    }
  }

  "SeedingRelief" - {

    "must fire when effective date is before 19/03/2025" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.SeedingRelief), effectiveDate = Some(seedingFloor.minusDays(1)))

      SeedingRelief.validate(ua).map(_.ruleId) mustBe Some("F23-38")
    }

    "must pass when effective date is on or after 19/03/2025" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.SeedingRelief), effectiveDate = Some(seedingFloor))

      SeedingRelief.validate(ua) mustBe None
    }
  }

  "F25EffectiveDate" - {

    "must fire when effective date is on the cutoff (01/06/2024)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("01"), effectiveDate = Some(mdrEffectiveCutOff))

      F25EffectiveDate.validate(ua).map(_.ruleId) mustBe Some("F25-effective")
    }

    "must fire when effective date is after the cutoff" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("01"), effectiveDate = Some(mdrEffectiveCutOff.plusDays(1)))

      F25EffectiveDate.validate(ua).map(_.ruleId) mustBe Some("F25-effective")
    }

    "must pass when effective date is before the cutoff" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("01"), effectiveDate = Some(mdrEffectiveCutOff.minusDays(1)))

      F25EffectiveDate.validate(ua) mustBe None
    }

    "must pass when effective date is not yet answered (null is OK)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("01"), effectiveDate = None)

      F25EffectiveDate.validate(ua) mustBe None
    }

    "must not apply when a different relief reason is selected" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(mdrEffectiveCutOff))

      F25EffectiveDate.validate(ua) mustBe None
    }
  }

  "F25ContractDate" - {

    "must fire when contract date is null" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("01"), contractDate = None)

      F25ContractDate.validate(ua).map(_.ruleId) mustBe Some("F25-contract")
    }

    "must fire when contract date is on the cutoff (07/03/2024)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("01"), contractDate = Some(mdrContractCutOff.toString))

      F25ContractDate.validate(ua).map(_.ruleId) mustBe Some("F25-contract")
    }

    "must fire when contract date is after the cutoff" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("01"), contractDate = Some(mdrContractCutOff.plusDays(1).toString))

      F25ContractDate.validate(ua).map(_.ruleId) mustBe Some("F25-contract")
    }

    "must pass when contract date is before the cutoff" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.MultipleDwellings), propertyType = Some("01"), contractDate = Some(mdrContractCutOff.minusDays(1).toString))

      F25ContractDate.validate(ua) mustBe None
    }

    "must not apply when a different relief reason is selected" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), contractDate = None)

      F25ContractDate.validate(ua) mustBe None
    }
  }

  "F23ReliefReasonRules.all" - {

    "must contain all nine rules" in {
      F23ReliefReasonRules.all.map(_.id) must contain allOf (
        "F23-32", "F23-33", "F23-34", "F23-35", "F23-36", "F23-37", "F23-38",
        "F25-effective", "F25-contract"
      )
    }

    "must produce no failures for a baseline (no relief claimed)" in {
      val ua = answersWith(claimingRelief = Some("NO"))

      F23ReliefReasonRules.all.flatMap(_.validate(ua)) mustBe empty
    }
  }
}