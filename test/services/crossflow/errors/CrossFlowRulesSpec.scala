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
import pages.transaction.{ReasonForReliefPage, TotalConsiderationOfTransactionPage, TransactionEffectiveDatePage}
import services.crossflow.*
import services.crossflow.errors.Cf6_MultiLandPropertyTypeMismatch.Cf18_AnnualRentOver1000Missing
import services.crossflow.errors.CrossFlowProjections.Dates

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

  /** Transaction description for a grant of lease — the same "L" code LeaseTaskList keys off. */
  private val GrantOfLeaseCode = "L"

  private def answersWith(
                           claimingRelief:   Option[String]          = Some("yes"),
                           reliefReason:     Option[ReasonForRelief] = None,
                           effectiveDate:    Option[LocalDate]       = None,
                           contractDate:     Option[String]          = None,
                           propertyType:     Option[String]          = None,
                           totalPremium:     Option[String]          = None,
                           totalConsideration: Option[String]        = None,
                           mainLandId:       Option[String]          = None,
                           additionalLands:  Seq[Land]               = Nil,
                           leaseType:        Option[String]          = None,
                           transactionDescription: Option[String]    = None,
                           annualRentOver1000: Option[String]        = None,
                           usedAsFactory:    Option[String]          = None,
                           usedAsHotel:      Option[String]          = None,
                           usedAsIndustrial: Option[String]          = None,
                           usedAsOffice:     Option[String]          = None,
                           usedAsOther:      Option[String]          = None,
                           usedAsShop:       Option[String]          = None,
                           usedAsWarehouse:  Option[String]          = None
                         ): UserAnswers = {
    val committedTransaction = Transaction(
      claimingRelief   = claimingRelief,
      reliefReason     = reliefReason.flatMap {
        case ReasonForRelief.FirstTimeBuyer       => Some("32")
        case ReasonForRelief.MultipleDwellings    => Some("33")
        case ReasonForRelief.PreCompletion        => Some("34")
        case ReasonForRelief.ReliefFromRate       => Some("35")
        case ReasonForRelief.ReliefForFreeport    => Some("36")
        case ReasonForRelief.ReliefInvestmentZone => Some("37")
        case ReasonForRelief.SeedingRelief        => Some("38")
        case _                                    => None
      },
      effectiveDate    = effectiveDate.map(_.toString),
      contractDate     = contractDate,
      totalConsideration = totalConsideration,
      transactionDescription = transactionDescription,
      usedAsFactory    = usedAsFactory,
      usedAsHotel      = usedAsHotel,
      usedAsIndustrial = usedAsIndustrial,
      usedAsOffice     = usedAsOffice,
      usedAsOther      = usedAsOther,
      usedAsShop       = usedAsShop,
      usedAsWarehouse  = usedAsWarehouse
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

    val committedLease = (totalPremium, leaseType, annualRentOver1000) match {
      case (None, None, None) => None
      case _ => Some(Lease(
        totalPremiumPayable  = totalPremium,
        leaseType            = leaseType,
        isAnnualRentOver1000 = annualRentOver1000
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
    val withTotalConsideration = totalConsideration.fold(withDate)(c => withDate.set(TotalConsiderationOfTransactionPage, c).success.value)
    withTotalConsideration
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
      val ua = answersWith(claimingRelief = Some("no"), reliefReason = Some(ReasonForRelief.FirstTimeBuyer), propertyType = Some("02"))

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
      val ua = answersWith(claimingRelief = Some("no"), reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalPremium = Some("600000.00"))

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
      val ua = answersWith(claimingRelief = Some("no"), reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalPremium = Some("700000.00"))

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

  "F28FtbCap500kTotalConsideration" - {

    val ftbStart = LocalDate.of(2017, 11, 22)
    val ftb625WindowStart = LocalDate.of(2022, 9, 23)
    val ftb500PostResetStart = LocalDate.of(2025, 4, 1)

    "must not apply when not claiming relief" in {
      val ua = answersWith(claimingRelief = Some("no"), reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalConsideration = Some("600000.00"))

      F28FtbCap500kTotalConsideration.validate(ua) mustBe None
    }

    "must not apply when the relief reason is not 32" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefForFreeport), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalConsideration = Some("600000.00"))

      F28FtbCap500kTotalConsideration.validate(ua) mustBe None
    }

    "must not apply when the effective date is in the middle window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalConsideration = Some("600000.00"))

      F28FtbCap500kTotalConsideration.validate(ua) mustBe None
    }

    "must not apply when the effective date is before FTB relief started" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftbStart.minusDays(1)), totalConsideration = Some("600000.00"))

      F28FtbCap500kTotalConsideration.validate(ua) mustBe None
    }

    "must pass when total consideration is exactly £500,000 in the original window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalConsideration = Some("500000.00"))

      F28FtbCap500kTotalConsideration.validate(ua) mustBe None
    }

    "must pass when total consideration is under £500,000 in the original window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalConsideration = Some("450000.00"))

      F28FtbCap500kTotalConsideration.validate(ua) mustBe None
    }

    "must fire when total consideration is over £500,000 in the original window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalConsideration = Some("600000.00"))

      F28FtbCap500kTotalConsideration.validate(ua).map(_.ruleId) mustBe Some("F28-cap500k-totalConsideration")
    }

    "must fire when total consideration is over £500,000 in the post-2025 window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2025, 6, 1)), totalConsideration = Some("600000.00"))

      F28FtbCap500kTotalConsideration.validate(ua).map(_.ruleId) mustBe Some("F28-cap500k-totalConsideration")
    }

    "must pass when totalConsideration is missing (incomplete, not in error)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)))

      F28FtbCap500kTotalConsideration.validate(ua) mustBe None
    }

    "must fire on the lower boundary of the original window (22/11/2017)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftbStart), totalConsideration = Some("600000.00"))

      F28FtbCap500kTotalConsideration.validate(ua).map(_.ruleId) mustBe Some("F28-cap500k-totalConsideration")
    }

    "must fire on the upper boundary of the original window (22/09/2022)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftb625WindowStart.minusDays(1)), totalConsideration = Some("600000.00"))

      F28FtbCap500kTotalConsideration.validate(ua).map(_.ruleId) mustBe Some("F28-cap500k-totalConsideration")
    }

    "must fire on the lower boundary of the post-2025 window (01/04/2025)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftb500PostResetStart), totalConsideration = Some("600000.00"))

      F28FtbCap500kTotalConsideration.validate(ua).map(_.ruleId) mustBe Some("F28-cap500k-totalConsideration")
    }
  }

  "F28FtbCap625kTotalConsideration" - {

    val ftb625WindowStart = LocalDate.of(2022, 9, 23)
    val ftb500PostResetStart = LocalDate.of(2025, 4, 1)

    "must not apply when not claiming relief" in {
      val ua = answersWith(claimingRelief = Some("no"), reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalConsideration = Some("700000.00"))

      F28FtbCap625kTotalConsideration.validate(ua) mustBe None
    }

    "must not apply when the relief reason is not 32" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.ReliefForFreeport), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalConsideration = Some("700000.00"))

      F28FtbCap625kTotalConsideration.validate(ua) mustBe None
    }

    "must not apply when the effective date is in the original window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2020, 6, 1)), totalConsideration = Some("700000.00"))

      F28FtbCap625kTotalConsideration.validate(ua) mustBe None
    }

    "must not apply when the effective date is in the post-2025 window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2025, 6, 1)), totalConsideration = Some("700000.00"))

      F28FtbCap625kTotalConsideration.validate(ua) mustBe None
    }

    "must pass when total consideration is exactly £625,000 in the middle window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalConsideration = Some("625000.00"))

      F28FtbCap625kTotalConsideration.validate(ua) mustBe None
    }

    "must pass when total consideration is under £625,000 in the middle window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalConsideration = Some("600000.00"))

      F28FtbCap625kTotalConsideration.validate(ua) mustBe None
    }

    "must fire when total consideration is over £625,000 in the middle window" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)), totalConsideration = Some("700000.00"))

      F28FtbCap625kTotalConsideration.validate(ua).map(_.ruleId) mustBe Some("F28-cap625k-totalConsideration")
    }

    "must pass when total consideration is missing (incomplete, not in error)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(LocalDate.of(2023, 9, 24)))

      F28FtbCap625kTotalConsideration.validate(ua) mustBe None
    }

    "must fire on the lower boundary of the middle window (23/09/2022)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftb625WindowStart), totalConsideration = Some("700000.00"))

      F28FtbCap625kTotalConsideration.validate(ua).map(_.ruleId) mustBe Some("F28-cap625k-totalConsideration")
    }

    "must fire on the upper boundary of the middle window (31/03/2025)" in {
      val ua = answersWith(reliefReason = Some(ReasonForRelief.FirstTimeBuyer), effectiveDate = Some(ftb500PostResetStart.minusDays(1)), totalConsideration = Some("700000.00"))

      F28FtbCap625kTotalConsideration.validate(ua).map(_.ruleId) mustBe Some("F28-cap625k-totalConsideration")
    }
  }

  "F24AdditionalResidentialEffDate" - {

    "must fire when the land is '04 - Additional residential' and the effective date is before the F24 floor" in {
      val land = Land(landID = Some("LND001"), propertyType = Some("04"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.f24EffectiveFloor.minusDays(1)))

      F24AdditionalResidentialEffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-3")
    }

    "must pass when the effective date is on the F24 floor" in {
      val land = Land(landID = Some("LND001"), propertyType = Some("04"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.f24EffectiveFloor))

      F24AdditionalResidentialEffDate.validate(land, ua) mustBe None
    }

    "must pass when the effective date is not yet answered" in {
      val land = Land(landID = Some("LND001"), propertyType = Some("04"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = None)

      F24AdditionalResidentialEffDate.validate(land, ua) mustBe None
    }

    "must not apply when the land is not '04'" in {
      val land = Land(landID = Some("LND001"), propertyType = Some("01"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.f24EffectiveFloor.minusDays(1)))

      F24AdditionalResidentialEffDate.validate(land, ua) mustBe None
    }
  }

  "Cf8_RegularWelshCodes" - {

    "must fire when a regular Welsh code is used on or after the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6805"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective))

      Cf8_RegularWelshCodes.validate(land, ua).map(_.ruleId) mustBe Some("Cf-8")
    }

    "must fire when a regular Welsh code is used and the effective date is blank" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6805"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = None)

      Cf8_RegularWelshCodes.validate(land, ua) mustBe None
    }

    "must pass when the effective date is before the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6805"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective.minusDays(1)))

      Cf8_RegularWelshCodes.validate(land, ua) mustBe None
    }

    "must not apply to a non-Welsh authority code" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("0114"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective))

      Cf8_RegularWelshCodes.validate(land, ua) mustBe None
    }
  }

  "Cf9a_Welsh6996_6997EffDate" - {

    "must fire when 6996 is used before the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6996"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective.minusDays(1)))

      Cf9a_Welsh6996_6997EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-9a")
    }

    "must fire when 6997 is used before the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6997"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective.minusDays(1)))

      Cf9a_Welsh6996_6997EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-9a")
    }

    "must pass when the effective date is on the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6996"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective))

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }

    "must not apply to any other authority code" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6998"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective.minusDays(1)))

      Cf9a_Welsh6996_6997EffDate.validate(land, ua) mustBe None
    }
  }

  "Cf9b_Welsh6998EffDate" - {

    "must fire when 6998 is used before the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6998"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective.minusDays(1)))

      Cf9b_Welsh6998EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-9b")
    }

    "must pass when the effective date is on the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6998"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective))

      Cf9b_Welsh6998EffDate.validate(land, ua) mustBe None
    }

    "must not apply to any other authority code" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6999"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective.minusDays(1)))

      Cf9b_Welsh6998EffDate.validate(land, ua) mustBe None
    }
  }

  "Cf9c_Welsh6999EffDate" - {

    "must fire when 6999 is used before the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6999"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective.minusDays(1)))

      Cf9c_Welsh6999EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-9c")
    }

    "must pass when the effective date is on the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6999"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective))

      Cf9c_Welsh6999EffDate.validate(land, ua) mustBe None
    }
  }

  "Cf10_Welsh6998ContractDate" - {

    "must fire when 6998 is used on/after the Wales Act date and the contract date is blank" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6998"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective), contractDate = None)

      Cf10_Welsh6998ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-10")
    }

    "must fire when the contract date is on the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6998"))
      val ua   = answersWith(
        claimingRelief = Some("no"),
        effectiveDate  = Some(Dates.welshActEffective),
        contractDate   = Some(Dates.welshActEffective.toString))

      Cf10_Welsh6998ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-10")
    }

    "must pass when the contract date is before the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6998"))
      val ua   = answersWith(
        claimingRelief = Some("no"),
        effectiveDate  = Some(Dates.welshActEffective),
        contractDate   = Some(Dates.welshActEffective.minusDays(1).toString))

      Cf10_Welsh6998ContractDate.validate(land, ua) mustBe None
    }

    "must not apply when the effective date is before the Wales Act effective date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6998"))
      val ua   = answersWith(
        claimingRelief = Some("no"),
        effectiveDate  = Some(Dates.welshActEffective.minusDays(1)),
        contractDate   = None)

      Cf10_Welsh6998ContractDate.validate(land, ua) mustBe None
    }
  }

  "Cf11_Welsh6999ContractDate" - {

    "must fire when 6999 is used on/after the Wales Act effective date and the contract date is blank" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6999"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.welshActEffective), contractDate = None)

      Cf11_Welsh6999ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-11")
    }

    "must fire when the contract date is after the Wales Act passing date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6999"))
      val ua   = answersWith(
        claimingRelief = Some("no"),
        effectiveDate  = Some(Dates.welshActEffective),
        contractDate   = Some(Dates.welshActDate.plusDays(1).toString))

      Cf11_Welsh6999ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-11")
    }

    "must pass when the contract date is on the Wales Act passing date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("6999"))
      val ua   = answersWith(
        claimingRelief = Some("no"),
        effectiveDate  = Some(Dates.welshActEffective),
        contractDate   = Some(Dates.welshActDate.toString))

      Cf11_Welsh6999ContractDate.validate(land, ua) mustBe None
    }
  }

  "Cf12_Dummy8998_8999EffDate" - {

    "must fire when 8998 is used before the CR223 date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8998"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.cr223Effective.minusDays(1)))

      Cf12_Dummy8998_8999EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-12")
    }

    "must fire when 8999 is used before the CR223 date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8999"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.cr223Effective.minusDays(1)))

      Cf12_Dummy8998_8999EffDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-12")
    }

    "must pass when the effective date is on the CR223 date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8998"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.cr223Effective))

      Cf12_Dummy8998_8999EffDate.validate(land, ua) mustBe None
    }

    "must not apply to a non-dummy authority code" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("0114"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.cr223Effective.minusDays(1)))

      Cf12_Dummy8998_8999EffDate.validate(land, ua) mustBe None
    }
  }

  "Cf13_Dummy8999ContractDate" - {

    "must fire when 8999 is used and the contract date is blank" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8999"))
      val ua   = answersWith(claimingRelief = Some("no"), contractDate = None)

      Cf13_Dummy8999ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-13")
    }

    "must fire when the contract date is on the Scotland Act date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8999"))
      val ua   = answersWith(claimingRelief = Some("no"), contractDate = Some(Dates.scotlandActDate.toString))

      Cf13_Dummy8999ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-13")
    }

    "must pass when the contract date is before the Scotland Act date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8999"))
      val ua   = answersWith(claimingRelief = Some("no"), contractDate = Some(Dates.scotlandActDate.minusDays(1).toString))

      Cf13_Dummy8999ContractDate.validate(land, ua) mustBe None
    }

    "must not apply to 8998" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8998"))
      val ua   = answersWith(claimingRelief = Some("no"), contractDate = None)

      Cf13_Dummy8999ContractDate.validate(land, ua) mustBe None
    }
  }

  "Cf14_Dummy8998ContractDate" - {

    "must fire when 8998 is used and the contract date is blank" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8998"))
      val ua   = answersWith(claimingRelief = Some("no"), contractDate = None)

      Cf14_Dummy8998ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-14")
    }

    "must fire when the contract date is on the CR223 date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8998"))
      val ua   = answersWith(claimingRelief = Some("no"), contractDate = Some(Dates.cr223Effective.toString))

      Cf14_Dummy8998ContractDate.validate(land, ua).map(_.ruleId) mustBe Some("Cf-14")
    }

    "must pass when the contract date is before the CR223 date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8998"))
      val ua   = answersWith(claimingRelief = Some("no"), contractDate = Some(Dates.cr223Effective.minusDays(1).toString))

      Cf14_Dummy8998ContractDate.validate(land, ua) mustBe None
    }

    "must not apply to 8999" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8999"))
      val ua   = answersWith(claimingRelief = Some("no"), contractDate = None)

      Cf14_Dummy8998ContractDate.validate(land, ua) mustBe None
    }
  }

  "Cf15_ScottishCodes" - {

    "must fire when a Scottish-pattern code is used on the CR223 date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("9001"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.cr223Effective))

      Cf15_ScottishCodes.validate(land, ua).map(_.ruleId) mustBe Some("Cf-15")
    }

    "must fire when the effective date is blank" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("9001"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = None)

      Cf15_ScottishCodes.validate(land, ua) mustBe None
    }

    "must pass when the effective date is before the CR223 date" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("9001"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.cr223Effective.minusDays(1)))

      Cf15_ScottishCodes.validate(land, ua) mustBe None
    }

    "must not apply to a code outside the Scottish pattern" in {
      val land = Land(landID = Some("LND001"), localAuthorityNumber = Some("8999"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.cr223Effective))

      Cf15_ScottishCodes.validate(land, ua) mustBe None
    }
  }

  "Cf16_ScottishPostcode" - {

    "must fire when a Scottish postcode is used on the CR223 date" in {
      val land = Land(landID = Some("LND001"), postcode = Some("EH1 1AA"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.cr223Effective))

      Cf16_ScottishPostcode.validate(land, ua).map(_.ruleId) mustBe Some("Cf-16")
    }

    "must pass when the effective date is before the CR223 date" in {
      val land = Land(landID = Some("LND001"), postcode = Some("EH1 1AA"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.cr223Effective.minusDays(1)))

      Cf16_ScottishPostcode.validate(land, ua) mustBe None
    }

    "must not apply to a non-Scottish postcode" in {
      val land = Land(landID = Some("LND001"), postcode = Some("SW1A 1AA"))
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.cr223Effective))

      Cf16_ScottishPostcode.validate(land, ua) mustBe None
    }

    "must not apply when there is no postcode" in {
      val land = Land(landID = Some("LND001"), postcode = None)
      val ua   = answersWith(claimingRelief = Some("no"), effectiveDate = Some(Dates.cr223Effective))

      Cf16_ScottishPostcode.validate(land, ua) mustBe None
    }
  }

  "Cf5a_LeaseRResidential" - {

    "must fire when lease type is R but main land is '02 - Mixed'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("02"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5a_LeaseRResidential.validate(ua).map(_.ruleId) mustBe Some("Cf-5a")
    }

    "must fire when lease type is R but main land is '03 - Non-residential'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("03"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5a_LeaseRResidential.validate(ua).map(_.ruleId) mustBe Some("Cf-5a")
    }

    "must pass when lease type is R and main land is '01'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }

    "must pass when lease type is R and main land is '04'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("04"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }

    "must not apply when lease type is M" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("02"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("M")
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }

    "must not apply when lease type is N" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("03"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("N")
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }

    "must not apply when no lease type has been chosen" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("02"),
        mainLandId     = Some("LND001"),
        leaseType      = None
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }

    "must pass when lease type is R and main land has no property type" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5a_LeaseRResidential.validate(ua) mustBe None
    }
  }

  "Cf5b_LeaseMMixed" - {

    "must fire when lease type is M but main land is '01 - Residential'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("M")
      )

      Cf5b_LeaseMMixed.validate(ua).map(_.ruleId) mustBe Some("Cf-5b")
    }

    "must pass when lease type is M and main land is '02'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("02"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("M")
      )

      Cf5b_LeaseMMixed.validate(ua) mustBe None
    }

    "must not apply when lease type is R" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5b_LeaseMMixed.validate(ua) mustBe None
    }

    "must not apply when lease type is N" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("03"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("N")
      )

      Cf5b_LeaseMMixed.validate(ua) mustBe None
    }

    "must pass when lease type is M and main land has no property type" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("M")
      )

      Cf5b_LeaseMMixed.validate(ua) mustBe None
    }
  }

  "Cf5c_LeaseNNonResidential" - {

    "must fire when lease type is N but main land is '01 - Residential'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("N")
      )

      Cf5c_LeaseNNonResidential.validate(ua).map(_.ruleId) mustBe Some("Cf-5c")
    }

    "must fire when lease type is N but main land is '02 - Mixed'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("02"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("N")
      )

      Cf5c_LeaseNNonResidential.validate(ua).map(_.ruleId) mustBe Some("Cf-5c")
    }

    "must pass when lease type is N and main land is '03'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("03"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("N")
      )

      Cf5c_LeaseNNonResidential.validate(ua) mustBe None
    }

    "must not apply when lease type is R" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf5c_LeaseNNonResidential.validate(ua) mustBe None
    }

    "must not apply when lease type is M" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
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
        claimingRelief  = Some("no"),
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
        claimingRelief  = Some("no"),
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
        claimingRelief  = Some("no"),
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
        claimingRelief = Some("no"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001"),
        leaseType      = Some("R")
      )

      Cf6_MultiLandPropertyTypeMismatch.validate(land1, ua) mustBe None
    }
  }

  "Cf17_UseOfPropertyMissing" - {

    "must be flagged as aggregateOnly so it does not fire during inline form binding" in {
      Cf17_UseOfPropertyMissing.aggregateOnly mustBe true
    }

    "must fire when property type is '02 - Mixed' and no use-of-property flags are set" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("02"),
        mainLandId     = Some("LND001")
      )

      Cf17_UseOfPropertyMissing.validate(ua).map(_.ruleId) mustBe Some("Cf-17")
    }

    "must fire when property type is '03 - Non-residential' and no use-of-property flags are set" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("03"),
        mainLandId     = Some("LND001")
      )

      Cf17_UseOfPropertyMissing.validate(ua).map(_.ruleId) mustBe Some("Cf-17")
    }

    "must pass when property type is '02' and at least one use-of-property flag is 'yes'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("02"),
        mainLandId     = Some("LND001"),
        usedAsOffice   = Some("yes")
      )

      Cf17_UseOfPropertyMissing.validate(ua) mustBe None
    }

    "must pass when property type is '03' and multiple use-of-property flags are 'yes'" in {
      val ua = answersWith(
        claimingRelief  = Some("no"),
        propertyType    = Some("03"),
        mainLandId      = Some("LND001"),
        usedAsFactory   = Some("yes"),
        usedAsWarehouse = Some("yes"),
        usedAsOther     = Some("yes")
      )

      Cf17_UseOfPropertyMissing.validate(ua) mustBe None
    }

    "must fire when property type is '02' and all use-of-property flags are 'no'" in {
      val ua = answersWith(
        claimingRelief   = Some("no"),
        propertyType     = Some("02"),
        mainLandId       = Some("LND001"),
        usedAsFactory    = Some("no"),
        usedAsHotel      = Some("no"),
        usedAsIndustrial = Some("no"),
        usedAsOffice     = Some("no"),
        usedAsOther      = Some("no"),
        usedAsShop       = Some("no"),
        usedAsWarehouse  = Some("no")
      )

      Cf17_UseOfPropertyMissing.validate(ua).map(_.ruleId) mustBe Some("Cf-17")
    }

    "must not apply when property type is '01 - Residential'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("01"),
        mainLandId     = Some("LND001")
      )

      Cf17_UseOfPropertyMissing.validate(ua) mustBe None
    }

    "must not apply when property type is '04 - Additional residential'" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = Some("04"),
        mainLandId     = Some("LND001")
      )

      Cf17_UseOfPropertyMissing.validate(ua) mustBe None
    }

    "must not apply when no land is configured" in {
      val ua = answersWith(
        claimingRelief = Some("no"),
        propertyType   = None
      )

      Cf17_UseOfPropertyMissing.validate(ua) mustBe None
    }

    "must fire when at least one of multiple lands has a triggering property type (02) and use-of-property is unanswered" in {
      val additionalLand = Land(landID = Some("LND002"), propertyType = Some("01"))
      val ua = answersWith(
        claimingRelief  = Some("no"),
        propertyType    = Some("02"),
        mainLandId      = Some("LND001"),
        additionalLands = Seq(additionalLand)
      )

      Cf17_UseOfPropertyMissing.validate(ua).map(_.ruleId) mustBe Some("Cf-17")
    }
  }

  "Cf18_AnnualRentOver1000Missing" - {
    
    def cf18Answers(
                             leaseType:          Option[String] = Some("R"),
                             transaction:        Option[String] = Some(GrantOfLeaseCode),
                             effectiveDate:      Option[LocalDate] = Some(Dates.annualRentOver1000Cutoff.minusDays(1)),
                             annualRentOver1000: Option[String] = None
                           ): UserAnswers =
      answersWith(
        claimingRelief         = Some("no"),
        leaseType              = leaseType,
        transactionDescription = transaction,
        effectiveDate          = effectiveDate,
        annualRentOver1000     = annualRentOver1000
      )

    "must be flagged as aggregateOnly so it does not fire during inline form binding" in {
      Cf18_AnnualRentOver1000Missing.aggregateOnly mustBe true
    }

    "must fire when a grant of lease before the cutoff has not answered the £1,000 question" in {
      Cf18_AnnualRentOver1000Missing.validate(cf18Answers()).map(_.ruleId) mustBe Some("Cf-18")
    }

    "must pass when the £1,000 question is answered yes" in {
      Cf18_AnnualRentOver1000Missing.validate(cf18Answers(annualRentOver1000 = Some("yes"))) mustBe None
    }

    "must pass when the £1,000 question is answered no" in {
      Cf18_AnnualRentOver1000Missing.validate(cf18Answers(annualRentOver1000 = Some("no"))) mustBe None
    }

    "must not apply when the transaction is not a grant of lease" in {
      Cf18_AnnualRentOver1000Missing.validate(cf18Answers(transaction = Some("F"))) mustBe None
    }

    "must not apply when the transaction description is absent" in {
      Cf18_AnnualRentOver1000Missing.validate(cf18Answers(transaction = None)) mustBe None
    }

    "must not apply when the effective date is on the cutoff" in {
      Cf18_AnnualRentOver1000Missing.validate(
        cf18Answers(effectiveDate = Some(Dates.annualRentOver1000Cutoff))) mustBe None
    }

    "must not apply when the effective date is after the cutoff" in {
      Cf18_AnnualRentOver1000Missing.validate(
        cf18Answers(effectiveDate = Some(Dates.annualRentOver1000Cutoff.plusDays(1)))) mustBe None
    }

    "must not apply when the effective date is not yet answered" in {
      Cf18_AnnualRentOver1000Missing.validate(cf18Answers(effectiveDate = None)) mustBe None
    }

    "must not apply when there is no lease involvement" in {
      Cf18_AnnualRentOver1000Missing.validate(cf18Answers(leaseType = None)) mustBe None
    }

    "must fire on the day before the cutoff" in {
      Cf18_AnnualRentOver1000Missing.validate(
          cf18Answers(effectiveDate = Some(Dates.annualRentOver1000Cutoff.minusDays(1))))
        .map(_.ruleId) mustBe Some("Cf-18")
    }
  }

  "F23Rules.all" - {

    "must contain all seven F23 rules" in {
      F23Rules.all.map(_.id) must contain allOf (
        "F23-32", "F23-33", "F23-34", "F23-35", "F23-36", "F23-37", "F23-38"
      )
    }

    "must produce no failures for a baseline (no relief claimed)" in {
      val ua = answersWith(claimingRelief = Some("no"))

      F23Rules.all.flatMap(_.validate(ua)) mustBe empty
    }
  }

  "F24Rules.all" - {

    "must contain Cf-3" in {
      F24Rules.all.map(_.id) must contain ("Cf-3")
    }
  }

  "F25Rules.all" - {

    "must contain both halves of F25" in {
      F25Rules.all.map(_.id) must contain allOf ("F25-effective", "F25-contract")
    }
  }

  "F28Rules.all" - {

    "must contain all four F28 rules" in {
      F28Rules.all.map(_.id) must contain allOf (
        "F28-cap500k", "F28-cap625k", "F28-cap500k-totalConsideration", "F28-cap625k-totalConsideration"
      )
    }

    "must produce no failures for a baseline (no relief claimed)" in {
      val ua = answersWith(claimingRelief = Some("no"))

      F28Rules.all.flatMap(_.validate(ua)) mustBe empty
    }
  }

  "F17Rules.all" - {

    "must contain the Welsh authority code rules" in {
      F17Rules.all.map(_.id) must contain allOf ("Cf-8", "Cf-9a", "Cf-9b", "Cf-9c", "Cf-10", "Cf-11")
    }
  }

  "F18Rules.all" - {

    "must contain the dummy and Scottish code rules" in {
      F18Rules.all.map(_.id) must contain allOf ("Cf-12", "Cf-13", "Cf-14", "Cf-15", "Cf-16")
    }
  }

  "F30Rules.all" - {

    "must contain Cf-5a, Cf-5b, Cf-5c, Cf-17 and Cf-18" in {
      F30Rules.all.map(_.id) must contain allOf ("Cf-5a", "Cf-5b", "Cf-5c", "Cf-17", "Cf-18")
    }

    "must produce no failures when no lease and no land are configured" in {
      val ua = answersWith(claimingRelief = Some("no"))

      F30Rules.all.flatMap(_.validate(ua)) mustBe empty
    }
  }

  "F30RulesLand.all" - {

    "must contain Cf-6" in {
      F30RulesLand.all.map(_.id) must contain ("Cf-6")
    }
  }
}