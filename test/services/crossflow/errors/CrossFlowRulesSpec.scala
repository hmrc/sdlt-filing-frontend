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
import models.{Land, Lease, ReturnInfo, Transaction, UserAnswers}
import org.scalatest.matchers.must.Matchers
import pages.transaction.{ReasonForReliefPage, TransactionEffectiveDatePage}
import services.crossflow.*

import java.time.LocalDate

class CrossFlowRulesSpec extends SpecBase with Matchers {

  private val freeportStart      = LocalDate.of(2021, 10, 19)
  private val freeportEnd        = LocalDate.of(2026,  9, 30)
  private val investmentStart    = LocalDate.of(2023,  9, 29)
  private val investmentEnd      = LocalDate.of(2034,  9, 30)
  private val reliefFloor2013    = LocalDate.of(2013,  3,  6)
  private val seedingFloor       = LocalDate.of(2025,  3, 19)
  private val mdrEffectiveCutOff = LocalDate.of(2024,  6,  1)
  private val mdrContractCutOff  = LocalDate.of(2024,  3,  7)

  private def answersWith(
                           claimingRelief:  Option[String]          = Some("YES"),
                           reliefReason:    Option[ReasonForRelief] = None,
                           effectiveDate:   Option[LocalDate]       = None,
                           contractDate:    Option[String]          = None,
                           propertyType:    Option[String]          = None,
                           totalPremium:    Option[String]          = None,
                           mainLandId:      Option[String]          = None,
                           additionalLands: Seq[Land]               = Nil,
                           leaseType:       Option[String]          = None
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

    val firstLand = propertyType.map(t => Land(
      landID       = mainLandId.orElse(Some("LND001")),
      propertyType = Some(t)
    ))

    val allLands: Option[Seq[Land]] = (firstLand, additionalLands) match {
      case (None, Nil)        => None
      case (None, ls)         => Some(ls)
      case (Some(l), Nil)     => Some(Seq(l))
      case (Some(l), ls)      => Some(Seq(l) ++ ls)
    }

    val committedLease = (totalPremium, leaseType) match {
      case (None, None) => None
      case _ => Some(Lease(
        totalPremiumPayable = totalPremium,
        leaseType           = leaseType
      ))
    }

    val baseReturnInfo = emptyFullReturn.returnInfo.getOrElse(ReturnInfo())
    val returnInfoWithMain = mainLandId.fold(baseReturnInfo)(id => baseReturnInfo.copy(mainLandID = Some(id)))

    val base = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
      returnInfo  = Some(returnInfoWithMain),
      transaction = Some(committedTransaction),
      land        = allLands,
      lease       = committedLease
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

  "F28FtbCap500k" - {

    val ftbStart = LocalDate.of(2017, 11, 22)
    val ftb625WindowStart = LocalDate.of(2022, 9, 23)
    val ftb500PostResetStart = LocalDate.of(2025, 4, 1)

    "must not apply when not claiming relief" in {
      val ua = answersWith(claimingRelief = Some("NO"), reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalPremium = Some("600000.00"))

      F28FtbCap500k.validate(ua) mustBe None
    }

    "must not apply when the relief reason is not 32" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefForFreeport), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalPremium = Some("600000.00"))

      F28FtbCap500k.validate(ua) mustBe None
    }

    "must not apply when the effective date is in the middle window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalPremium = Some("600000.00"))

      F28FtbCap500k.validate(ua) mustBe None
    }

    "must not apply when the effective date is before FTB relief started" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftbStart.minusDays(1)), totalPremium = Some("600000.00"))

      F28FtbCap500k.validate(ua) mustBe None
    }

    "must pass when premium is exactly £500,000 in the original window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalPremium = Some("500000.00"))

      F28FtbCap500k.validate(ua) mustBe None
    }

    "must pass when premium is under £500,000 in the original window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalPremium = Some("450000.00"))

      F28FtbCap500k.validate(ua) mustBe None
    }

    "must fire when premium is over £500,000 in the original window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalPremium = Some("600000.00"))

      F28FtbCap500k.validate(ua).map(_.ruleId) mustBe Some("F28-cap500k")
    }

    "must fire when premium is over £500,000 in the post-2025 window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2025, 6, 1)), totalPremium = Some("600000.00"))

      F28FtbCap500k.validate(ua).map(_.ruleId) mustBe Some("F28-cap500k")
    }

    "must pass when premium is missing (incomplete, not in error)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)))

      F28FtbCap500k.validate(ua) mustBe None
    }

    "must fire on the lower boundary of the original window (22/11/2017)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftbStart), totalPremium = Some("600000.00"))

      F28FtbCap500k.validate(ua).map(_.ruleId) mustBe Some("F28-cap500k")
    }

    "must fire on the upper boundary of the original window (22/09/2022)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftb625WindowStart.minusDays(1)), totalPremium = Some("600000.00"))

      F28FtbCap500k.validate(ua).map(_.ruleId) mustBe Some("F28-cap500k")
    }

    "must fire on the lower boundary of the post-2025 window (01/04/2025)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftb500PostResetStart), totalPremium = Some("600000.00"))

      F28FtbCap500k.validate(ua).map(_.ruleId) mustBe Some("F28-cap500k")
    }
  }

  "F28FtbCap625k" - {

    val ftb625WindowStart = LocalDate.of(2022, 9, 23)
    val ftb500PostResetStart = LocalDate.of(2025, 4, 1)

    "must not apply when not claiming relief" in {
      val ua = answersWith(claimingRelief = Some("NO"), reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalPremium = Some("700000.00"))

      F28FtbCap625k.validate(ua) mustBe None
    }

    "must not apply when the relief reason is not 32" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefForFreeport), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalPremium = Some("700000.00"))

      F28FtbCap625k.validate(ua) mustBe None
    }

    "must not apply when the effective date is in the original window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalPremium = Some("700000.00"))

      F28FtbCap625k.validate(ua) mustBe None
    }

    "must not apply when the effective date is in the post-2025 window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2025, 6, 1)), totalPremium = Some("700000.00"))

      F28FtbCap625k.validate(ua) mustBe None
    }

    "must pass when premium is exactly £625,000 in the middle window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalPremium = Some("625000.00"))

      F28FtbCap625k.validate(ua) mustBe None
    }

    "must pass when premium is under £625,000 in the middle window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalPremium = Some("600000.00"))

      F28FtbCap625k.validate(ua) mustBe None
    }

    "must fire when premium is over £625,000 in the middle window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalPremium = Some("700000.00"))

      F28FtbCap625k.validate(ua).map(_.ruleId) mustBe Some("F28-cap625k")
    }

    "must pass when premium is missing (incomplete, not in error)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)))

      F28FtbCap625k.validate(ua) mustBe None
    }

    "must fire on the lower boundary of the middle window (23/09/2022)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftb625WindowStart), totalPremium = Some("700000.00"))

      F28FtbCap625k.validate(ua).map(_.ruleId) mustBe Some("F28-cap625k")
    }

    "must fire on the upper boundary of the middle window (31/03/2025)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftb500PostResetStart.minusDays(1)), totalPremium = Some("700000.00"))

      F28FtbCap625k.validate(ua).map(_.ruleId) mustBe Some("F28-cap625k")
    }
  }

  "Cf5a_LeaseRResidential" - {

    "must fire when main land is '01 - Residential' but lease type is not R" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("N")
      )

      Cf5a_LeaseRResidential.validate(ua).map(_.ruleId) mustBe Some("Cf-5a")
    }

    "must fire when main land is '04 - Additional residential' but lease type is not R" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("04"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("M")
      )

      Cf5a_LeaseRResidential.validate(ua).map(_.ruleId) mustBe Some("Cf-5a")
    }

    "must pass when main land is '01' and lease type is R" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }

    "must pass when main land is '04' and lease type is R" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("04"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }

    "must not apply when main land is '02 - Mixed'" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("02"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("M")
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }

    "must not apply when main land is '03 - Non-residential'" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("03"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("N")
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }

    "must not apply when no main land is configured" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("01"),
        mainLandId     = None,
        leaseType      = Some("N")
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }

    "must not apply when main land has no property type" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("N")
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }
  }

  "Cf5b_LeaseMMixed" - {

    "must fire when main land is '02 - Mixed' but lease type is not M" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("02"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5b_LeaseMMixed.validate(ua).map(_.ruleId) mustBe Some("Cf-5b")
    }

    "must pass when main land is '02' and lease type is M" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("02"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("M")
      )

      Cf5b_LeaseMMixed.validate(ua) mustBe None
    }

    "must not apply when main land is '01 - Residential'" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5b_LeaseMMixed.validate(ua) mustBe None
    }

    "must not apply when main land is '03 - Non-residential'" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("03"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("N")
      )

      Cf5b_LeaseMMixed.validate(ua) mustBe None
    }

    "must not apply when main land is '04 - Additional residential'" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("04"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5b_LeaseMMixed.validate(ua) mustBe None
    }
  }

  "Cf5c_LeaseNNonResidential" - {

    "must fire when main land is '03 - Non-residential' but lease type is not N" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("03"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5c_LeaseNNonResidential.validate(ua).map(_.ruleId) mustBe Some("Cf-5c")
    }

    "must fire when main land is '03' and lease type is M" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("03"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("M")
      )

      Cf5c_LeaseNNonResidential.validate(ua).map(_.ruleId) mustBe Some("Cf-5c")
    }

    "must pass when main land is '03' and lease type is N" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("03"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("N")
      )

      Cf5c_LeaseNNonResidential.validate(ua) mustBe None
    }

    "must not apply when main land is '01 - Residential'" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5c_LeaseNNonResidential.validate(ua) mustBe None
    }

    "must not apply when main land is '02 - Mixed'" in {
      val ua = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("02"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("M")
      )

      Cf5c_LeaseNNonResidential.validate(ua) mustBe None
    }
  }

  "Cf6_MultiLandPropertyTypeMismatch" - {

    "must be flagged as aggregateOnly so it does not fire during inline form binding" in {
      Cf6_MultiLandPropertyTypeMismatch.aggregateOnly mustBe true
    }

    "must fire on each committed land when there is lease involvement and lands have differing property types" in {
      val land1 = Land(landID = Some("LND001"), propertyType = Some("01"))
      val land2 = Land(landID = Some("LND002"), propertyType = Some("03"))
      val ua    = answersWith(
        claimingRelief  = Some("NO"),
        propertyType    = Some("01"),
        mainLandId      = Some("LND001"),
        additionalLands = Seq(land2),
        leaseType       = Some("R")
      )

      Cf6_MultiLandPropertyTypeMismatch.validate(land1, ua).map(_.ruleId) mustBe Some("Cf-6")
      Cf6_MultiLandPropertyTypeMismatch.validate(land2, ua).map(_.ruleId) mustBe Some("Cf-6")
    }

    "must pass when all committed lands share the same property type" in {
      val land1 = Land(landID = Some("LND001"), propertyType = Some("01"))
      val land2 = Land(landID = Some("LND002"), propertyType = Some("01"))
      val ua    = answersWith(
        claimingRelief  = Some("NO"),
        propertyType    = Some("01"),
        mainLandId      = Some("LND001"),
        additionalLands = Seq(land2),
        leaseType       = Some("R")
      )

      Cf6_MultiLandPropertyTypeMismatch.validate(land1, ua) mustBe None
      Cf6_MultiLandPropertyTypeMismatch.validate(land2, ua) mustBe None
    }

    "must not apply when there is no lease involvement" in {
      val land1 = Land(landID = Some("LND001"), propertyType = Some("01"))
      val land2 = Land(landID = Some("LND002"), propertyType = Some("03"))
      val ua    = answersWith(
        claimingRelief  = Some("NO"),
        propertyType    = Some("01"),
        mainLandId      = Some("LND001"),
        additionalLands = Seq(land2),
        leaseType       = None
      )

      Cf6_MultiLandPropertyTypeMismatch.validate(land1, ua) mustBe None
      Cf6_MultiLandPropertyTypeMismatch.validate(land2, ua) mustBe None
    }

    "must not apply when there is only one land" in {
      val land1 = Land(landID = Some("LND001"), propertyType = Some("01"))
      val ua    = answersWith(
        claimingRelief = Some("NO"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf6_MultiLandPropertyTypeMismatch.validate(land1, ua) mustBe None
    }
  }

  "F23Rules.all" - {

    "must contain all nine rules" in {
      F23Rules.all.map(_.id) must contain allOf(
        "F23-32", "F23-33", "F23-34", "F23-35", "F23-36", "F23-37", "F23-38"
      )
    }

    "must produce no failures for a baseline (no relief claimed)" in {
      val ua = answersWith(claimingRelief = Some("NO"))

      F23Rules.all.flatMap(_.validate(ua)) mustBe empty
    }
  }

  "F28Rules.all" - {

    "must contain both F28 rules" in {
      F28Rules.all.map(_.id) must contain allOf("F28-cap500k", "F28-cap625k")
    }

    "must produce no failures for a baseline (no relief claimed)" in {
      val ua = answersWith(claimingRelief = Some("NO"))

      F28Rules.all.flatMap(_.validate(ua)) mustBe empty
    }
  }

  "F30Rules.all" - {

    "must contain Cf-5a, Cf-5b, and Cf-5c" in {
      F30Rules.all.map(_.id) must contain allOf("Cf-5a", "Cf-5b", "Cf-5c")
    }

    "must produce no failures when no lease and no land are configured" in {
      val ua = answersWith(claimingRelief = Some("NO"))

      F30Rules.all.flatMap(_.validate(ua)) mustBe empty
    }
  }

  "F30RulesLand.all" - {

    "must contain Cf-6" in {
      F30RulesLand.all.map(_.id) must contain("Cf-6")
    }
  }
}