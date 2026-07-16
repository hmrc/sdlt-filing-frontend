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

package services.crossflow.errors

import models.{Land, UserAnswers}
import services.crossflow.*
import services.crossflow.CrossFlowBody.WithBullets
import services.crossflow.errors.CrossFlowProjections.*
import services.crossflow.errors.Targets.*

private object Targets:
  val reliefReasonTarget:      CrossFlowTarget = CrossFlowTarget(Pages.ReliefReason,      Fields.ReliefReason)
  val effectiveDateTarget:     CrossFlowTarget = CrossFlowTarget(Pages.EffectiveDate,     Fields.EffectiveDate)
  val propertyTypeTarget:      CrossFlowTarget = CrossFlowTarget(Pages.LandPropertyType,  Fields.PropertyType)
  val contractDateTarget:      CrossFlowTarget = CrossFlowTarget(Pages.ContractDate,      Fields.ContractDate)
  val landAuthorityCodeTarget: CrossFlowTarget = CrossFlowTarget(Pages.LandAuthorityCode, Fields.LandAuthorityCode)
  val landPostcodeTarget:      CrossFlowTarget = CrossFlowTarget(Pages.LandPostcode,      Fields.LandPostcode)
  val leaseTypeTarget:         CrossFlowTarget = CrossFlowTarget(Pages.LeaseType,         Fields.LeaseType)
  val useOfPropertyTarget:     CrossFlowTarget = CrossFlowTarget(Pages.UseOfProperty, Fields.UseOfProperty)


/** Property type must be 01 (Residential). */
object FirstTimeBuyerRelief extends GuardRule:
  val id      = "F23-32"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction, ReturnSection.Land)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "32")
  protected def isValid(ua: UserAnswers):   Boolean = propertyTypeAcceptable(ua, Set(Residential))
  protected def messageKey                          = "crossflow.relief.firstTimeBuyer.notResidential"
  protected override def inlineErrorKey             = "crossflow.relief.firstTimeBuyer.notResidential.inline"


object FirstTimeBuyerReliefMultipleLands extends GuardRule:
  val id      = "F23-32-multipleLands"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction, ReturnSection.Land)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "32")
  protected def isValid(ua: UserAnswers):   Boolean = landCount(ua) <= 1
  protected def messageKey                          = "crossflow.relief.firstTimeBuyer.multipleLands"
  protected override def inlineErrorKey             = "crossflow.relief.firstTimeBuyer.multipleLands.inline"


/** Property type must be 01 / 02 / 04. The date side of code 33 is owned by F25. */
object MultipleDwellingsRelief extends GuardRule:
  val id      = "F23-33"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction, ReturnSection.Land)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget)

  private val allowedPropertyTypes = Set(Residential, Mixed, ResidentialAdditional)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "33")
  protected def isValid(ua: UserAnswers):   Boolean = propertyTypeAcceptable(ua, allowedPropertyTypes)
  protected def messageKey                          = "crossflow.relief.multipleDwellings.notAllowedPropertyType"
  protected override def inlineErrorKey             = "crossflow.relief.multipleDwellings.notAllowedPropertyType.inline"


/** Effective date on/after 06/03/2013. */
object PreCompletionRelief extends GuardRule:
  val id      = "F23-34"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "34")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(!_.isBefore(Dates.reliefFloor2013))
  protected def messageKey                          = "crossflow.relief.preCompletion.dateTooEarly"
  protected override def inlineErrorKey             = "crossflow.relief.preCompletion.dateTooEarly.inline"


/** Effective date on/after 06/03/2013. */
object FifteenPercentRateRelief extends GuardRule:
  val id      = "F23-35"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget, effectiveDateTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "35")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(!_.isBefore(Dates.reliefFloor2013))
  protected def messageKey                          = "crossflow.relief.fifteenPercentRate.dateTooEarly"
  protected override def inlineErrorKey             = "crossflow.relief.fifteenPercentRate.dateTooEarly.inline"


/** Effective date within 19/10/2021..30/09/2026 (inclusive). */
object FreeportRelief extends GuardRule:
  val id      = "F23-36"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "36")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(within(_, Dates.freeportStart, Dates.freeportEnd))
  protected def messageKey                          = "crossflow.relief.freeport.outsideWindow"
  protected override def inlineErrorKey             = "crossflow.relief.freeport.outsideWindow.inline"


/** Effective date within 29/09/2023..30/09/2034 (inclusive). */
object InvestmentZoneRelief extends GuardRule:
  val id      = "F23-37"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "37")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(within(_, Dates.investmentZoneStart, Dates.investmentZoneEnd))
  protected def messageKey                          = "crossflow.relief.investmentZone.outsideWindow"
  protected override def inlineErrorKey             = "crossflow.relief.investmentZone.outsideWindow.inline"


/** Effective date on/after the Reserved Investors Fund date 19/03/2025. */
object SeedingRelief extends GuardRule:
  val id      = "F23-38"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "38")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(!_.isBefore(Dates.reservedInvestorsFund))
  protected def messageKey                          = "crossflow.relief.seeding.dateTooEarly"
  protected override def inlineErrorKey             = "crossflow.relief.seeding.dateTooEarly.inline"

object F24AdditionalResidentialEffDate extends LandGuardRule:
  val id      = "Cf-3"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(propertyTypeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.propertyType.contains(ResidentialAdditional)

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    effectiveDate(ua).forall(!_.isBefore(Dates.f24EffectiveFloor))

  protected def messageKey              = "crossflow.land.Cf-3.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-3.inline"
  protected override def headingKey     = "crossflow.land.Cf-3.heading"

/** F25 effective-date half: effective date must be before 01/06/2024. Null is OK. */
object F25EffectiveDate extends GuardRule:
  val id      = "F25-effective"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "33")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(_.isBefore(Dates.mdrEffectiveDateCutOff))
  protected def messageKey                          = "crossflow.relief.multipleDwellings.effectiveDateTooLate"
  protected override def inlineErrorKey             = "crossflow.relief.multipleDwellings.effectiveDateTooLate.inline"


/** F25 contract-date half: contract date must be present AND before 07/03/2024. */
object F25ContractDate extends GuardRule:
  val id      = "F25-contract"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "33")
  protected def isValid(ua: UserAnswers):   Boolean = contractDate(ua).exists(_.isBefore(Dates.mdrLatestContractDate))
  protected def messageKey                          = "crossflow.relief.multipleDwellings.contractDateTooLate"
  protected override def inlineErrorKey             = "crossflow.relief.multipleDwellings.contractDateTooLate.inline"


// ============================================================================
// F28 — First Time Buyer's Relief premium caps
// ============================================================================

/** F28 First Time Buyer's Relief — £500k cap.
 * Applies when the effective date is in the original FTB window (22/11/2017 to 22/09/2022)
 * OR on/after the post-2025 cap reset (01/04/2025). Total premium payable must be £500k or less.
 */
object F28FtbCap500k extends GuardRule:
  val id      = "F28-cap500k"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget)

  private val Cap: BigDecimal = 500000

  protected def appliesTo(ua: UserAnswers): Boolean =
    isClaimingRelief(ua) && isReason(ua, "32") && in500kWindow(ua)

  protected def isValid(ua: UserAnswers): Boolean =
    totalPremium(ua).forall(_ <= Cap)

  protected def messageKey              = "crossflow.relief.firstTimeBuyer.over500k"
  protected override def inlineErrorKey = "crossflow.relief.firstTimeBuyer.over500k.inline"

  private def in500kWindow(ua: UserAnswers): Boolean =
    effectiveDate(ua).exists { d =>
      val inOriginalWindow = !d.isBefore(Dates.ftbStart) && d.isBefore(Dates.ftbCap625FromSept2022)
      val inPost2025Window = !d.isBefore(Dates.ftbCap500FromApril2025)
      inOriginalWindow || inPost2025Window
    }

  protected override def body: CrossFlowBody = CrossFlowBody.WithBullets(
    leadKey = "crossflow.relief.firstTimeBuyer.over500k.intro",
    bulletKeys = Seq(
      "crossflow.relief.firstTimeBuyer.over500k.option1",
      "crossflow.relief.firstTimeBuyer.over500k.option2"
    )
  )


/** F28 First Time Buyer's Relief — £625k cap.
 * Applies when the effective date is in the middle window (23/09/2022 to 31/03/2025).
 * Total premium payable must be £625k or less.
 */
object F28FtbCap625k extends GuardRule:
  val id      = "F28-cap625k"
  val affects: ReturnSection        = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(reliefReasonTarget)

  private val Cap: BigDecimal = 625000

  protected def appliesTo(ua: UserAnswers): Boolean =
    isClaimingRelief(ua) && isReason(ua, "32") && in625kWindow(ua)

  protected def isValid(ua: UserAnswers): Boolean =
    totalPremium(ua).forall(_ <= Cap)

  protected def messageKey              = "crossflow.relief.firstTimeBuyer.over625k"
  protected override def inlineErrorKey = "crossflow.relief.firstTimeBuyer.over625k.inline"

  private def in625kWindow(ua: UserAnswers): Boolean =
    effectiveDate(ua).exists { d =>
      !d.isBefore(Dates.ftbCap625FromSept2022) && d.isBefore(Dates.ftbCap500FromApril2025)
    }

  protected override def body: CrossFlowBody = CrossFlowBody.WithBullets(
    leadKey = "crossflow.relief.firstTimeBuyer.over625k.intro",
    bulletKeys = Seq(
      "crossflow.relief.firstTimeBuyer.over625k.option1",
      "crossflow.relief.firstTimeBuyer.over625k.option2"
    )
  )


/** Cf-8 — F17 regular Welsh codes (6805–6955 excluding specials) must not be used
 * for transactions on or after the Wales Act effective date (01/04/2018).
 * Spec: "If effective transaction date is blank or after or on the Wales act effective date, it is invalid"
 */
object Cf8_RegularWelshCodes extends LandGuardRule:
  val id      = "Cf-8"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(landAuthorityCodeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.localAuthorityNumber.exists(welshRegularCodes.contains)

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    effectiveDate(ua).forall(_.isBefore(Dates.welshActEffective))

  protected override def headingKey = "crossflow.land.heading"
  protected def messageKey              = "crossflow.land.Cf-8.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-8.inline"


/** Cf-9a — F17 6996/6997 must not be used when effective date is blank or before
 * the Wales Act effective date (01/04/2018).
 * Spec: "If effective transaction date is blank or before Wales effective date, then it is invalid"
 */
object Cf9a_Welsh6996_6997EffDate extends LandGuardRule:
  val id      = "Cf-9a"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(landAuthorityCodeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.localAuthorityNumber.exists(welshSpecial6996_6997.contains)

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    effectiveDate(ua).forall(!_.isBefore(Dates.welshActEffective))

  protected override def headingKey = "crossflow.land.heading"
  protected def messageKey              = "crossflow.land.Cf-9.welsh6996_6997.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-9.welsh6996_6997.inline"


/** Cf-9b — F17 6998 must not be used when effective date is blank or before
 * the Wales Act effective date (01/04/2018).
 * Spec: "6998: If effective transaction date is blank or before the Wales effective date, invalid"
 */
object Cf9b_Welsh6998EffDate extends LandGuardRule:
  val id      = "Cf-9b"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(landAuthorityCodeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.localAuthorityNumber.contains(welshSpecial6998)

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    effectiveDate(ua).forall(!_.isBefore(Dates.welshActEffective))

  protected override def headingKey = "crossflow.land.heading"
  protected def messageKey              = "crossflow.land.Cf-9.welsh6998.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-9.welsh6998.inline"


/** Cf-9c — F17 6999 must not be used when effective date is blank or before
 * the Wales Act effective date (01/04/2018).
 * Spec: "6999: If effective transaction date is blank or before the Wales effective date, invalid"
 */
object Cf9c_Welsh6999EffDate extends LandGuardRule:
  val id      = "Cf-9c"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(landAuthorityCodeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.localAuthorityNumber.contains(welshSpecial6999)

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    effectiveDate(ua).forall(!_.isBefore(Dates.welshActEffective))

  protected override def headingKey = "crossflow.land.heading"
  protected def messageKey              = "crossflow.land.Cf-9.welsh6999.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-9.welsh6999.inline"


/** Cf-10 — F17 6998 must not be used when contract date is blank or on/after
 * the Wales Act effective date (01/04/2018).
 * Spec: "6998: If date of contract is blank, after or on the Wales effective date, invalid"
 */
object Cf10_Welsh6998ContractDate extends LandGuardRule:
  val id      = "Cf-10"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(landAuthorityCodeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.localAuthorityNumber.contains(welshSpecial6998)

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    contractDate(ua).forall(_.isBefore(Dates.welshActEffective))

  protected override def headingKey = "crossflow.land.heading"
  protected def messageKey              = "crossflow.land.Cf-10.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-10.inline"


/** Cf-11 — F17 6999 must not be used when contract date is blank or after
 * the Wales Act passing date (17/12/2014).
 * Spec: "6999: If date of contract is blank or after the Wales act date (17 12 2014), invalid"
 */
object Cf11_Welsh6999ContractDate extends LandGuardRule:
  val id      = "Cf-11"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(landAuthorityCodeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.localAuthorityNumber.contains(welshSpecial6999)

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    contractDate(ua).forall(!_.isAfter(Dates.welshActDate))

  protected override def headingKey = "crossflow.land.heading"
  protected def messageKey              = "crossflow.land.Cf-11.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-11.inline"


/** Cf-12 — F18 dummy codes 8998/8999 must not be used when effective date is
 * before CR223 (01/04/2015).
 * Spec: "if the current time or the effective date time of the transaction is before the CR223 effective date time (01 04 2015), then the code is premature"
 */
object Cf12_Dummy8998_8999EffDate extends LandGuardRule:
  val id      = "Cf-12"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(landAuthorityCodeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.localAuthorityNumber.exists(Set("8998", "8999").contains)

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    effectiveDate(ua).forall(!_.isBefore(Dates.cr223Effective))

  protected override def headingKey = "crossflow.land.heading"
  protected def messageKey              = "crossflow.land.Cf-12.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-12.inline"


/** Cf-13 — F18 dummy code 8999 must not be used when contract date is on/after
 * the Scotland Act date (01/05/2012).
 * Spec: "if the date of contract is on or after the scotland act date (01 05 2012) then the code is invalid if it is 8999"
 */
object Cf13_Dummy8999ContractDate extends LandGuardRule:
  val id      = "Cf-13"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(landAuthorityCodeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.localAuthorityNumber.contains("8999")

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    contractDate(ua).forall(_.isBefore(Dates.scotlandActDate))

  protected override def headingKey = "crossflow.land.heading"
  protected def messageKey              = "crossflow.land.Cf-13.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-13.inline"


/** Cf-14 — F18 dummy code 8998 must not be used when contract date is on/after
 * the CR223 date (01/04/2015).
 * Spec: "if the date of contract is on or after the CR223 date (01 04 2015) then the code is invalid if it is 8998"
 */
object Cf14_Dummy8998ContractDate extends LandGuardRule:
  val id      = "Cf-14"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(landAuthorityCodeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.localAuthorityNumber.contains("8998")

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    contractDate(ua).forall(_.isBefore(Dates.cr223Effective))

  protected override def headingKey = "crossflow.land.heading"
  protected def messageKey              = "crossflow.land.Cf-14.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-14.inline"


/** Cf-15 — F18 Scottish-pattern codes (^9[0-9]{3}$) must not be used when
 * effective date is on/after the CR223 date (01/04/2015).
 * Spec: "are on or after the CR223 effective date time. If it matches, then it is invalid"
 */
object Cf15_ScottishCodes extends LandGuardRule:
  val id      = "Cf-15"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(landAuthorityCodeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.localAuthorityNumber.exists(scottishCodePattern.matches)

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    effectiveDate(ua).forall(_.isBefore(Dates.cr223Effective))

  protected override def headingKey = "crossflow.land.heading"
  protected def messageKey              = "crossflow.land.Cf-15.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-15.inline"


/** Cf-16 — F18 Scottish postcodes must not be used when effective date is on/after
 * the CR223 date (01/04/2015).
 * Spec: "if the current date time and effective date time are on or after the CR223 effective date time then we check the postcode"
 */
object Cf16_ScottishPostcode extends LandGuardRule:
  val id      = "Cf-16"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(landPostcodeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    land.postcode.exists(isScottishPostcode)

  protected def isValid(land: Land, ua: UserAnswers): Boolean =
    effectiveDate(ua).forall(_.isBefore(Dates.cr223Effective))

  protected override def headingKey     = "crossflow.land.Cf-16.heading"
  protected def messageKey              = "crossflow.land.Cf-16.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-16.inline"


  /** Cf-17 — Mural Business Function. Tr-11 (use of land or property) only appears when
   * property type is '02 - Mixed' or '03 - Non-residential'. If the user committed the
   * transaction before choosing 02/03 on Lr-1, Tr-11 has never fired and no use-of-property
   * flags are set. Per BA spec, this is not technically a validation error — but the
   * section must be flagged as needing completion via a dedicated cross-flow screen.
   *
   * AggregateOnly because there is no form-level inline message; the failure exists only
   * to drive section status and the dedicated cross-flow page.
   */
object Cf17_UseOfPropertyMissing extends GuardRule:
    val id = "Cf-17"
    val affects: ReturnSection = ReturnSection.Transaction
    val inputs: Set[ReturnSection] = Set(ReturnSection.Transaction, ReturnSection.Land)
    val targets: Seq[CrossFlowTarget] = Seq(useOfPropertyTarget)

    private val triggeringPropertyTypes: Set[String] = Set(Mixed, NonResidential)

    protected def appliesTo(ua: UserAnswers): Boolean =
      anyLandPropertyType(ua, triggeringPropertyTypes)

    protected def isValid(ua: UserAnswers): Boolean =
      useOfPropertyAnswered(ua)

    protected def messageKey = "crossflow.transaction.Cf-17.body"

    protected override def inlineErrorKey = "crossflow.transaction.Cf-17.body"

    protected override def headingKey = "crossflow.transaction.Cf-17.heading"

    override val aggregateOnly: Boolean = true

    protected override def body: CrossFlowBody = CrossFlowBody.WithBullets(
      leadKey = "crossflow.transaction.Cf-17.body",
      bulletKeys = Seq(
        "crossflow.transaction.Cf-17.bullet1",
        "crossflow.transaction.Cf-17.bullet2"
      )
    )

  /** Cf-5a — When the main land is '01 - Residential' or '04 - Additional residential',
   * the lease type must be 'R - Residential'. Fires when a residential-type main land
   * exists but the lease type is something other than R.
   *
   * Cf-6 enforces all lands sharing a single property type, so checking the main land
   * is representative of the whole return.
   */
object Cf5a_LeaseRResidential extends GuardRule:
    val id = "Cf-5a"
    val affects: ReturnSection = ReturnSection.Lease
    val inputs: Set[ReturnSection] = Set(ReturnSection.Lease, ReturnSection.Land)
    val targets: Seq[CrossFlowTarget] = Seq(leaseTypeTarget)

    private val triggeringPropertyTypes: Set[String] = Set(Residential, ResidentialAdditional)

    protected def appliesTo(ua: UserAnswers): Boolean =
      mainLandPropertyType(ua).exists(triggeringPropertyTypes.contains)

    protected def isValid(ua: UserAnswers): Boolean =
      isLeaseType(ua, LeaseResidential)

    protected def messageKey = "crossflow.lease.Cf-5a.body"

    protected override def inlineErrorKey = "crossflow.lease.Cf-5a.inline"

    protected override def headingKey = "crossflow.lease.heading"

    protected override def body: CrossFlowBody = WithBullets(
      messageKey,
      Seq("crossflow.lease.Cf-5a.bullet1", "crossflow.lease.Cf-5a.bullet2")
    )


  /** Cf-5b — When the main land is '02 - Mixed', the lease type must be 'M - Mixed use'. */
object Cf5b_LeaseMMixed extends GuardRule:
    val id = "Cf-5b"
    val affects: ReturnSection = ReturnSection.Lease
    val inputs: Set[ReturnSection] = Set(ReturnSection.Lease, ReturnSection.Land)
    val targets: Seq[CrossFlowTarget] = Seq(leaseTypeTarget)

    protected def appliesTo(ua: UserAnswers): Boolean =
      mainLandPropertyType(ua).contains(Mixed)

    protected def isValid(ua: UserAnswers): Boolean =
      isLeaseType(ua, LeaseMixed)

    protected def messageKey = "crossflow.lease.Cf-5b.body"

    protected override def inlineErrorKey = "crossflow.lease.Cf-5b.inline"

    protected override def headingKey = "crossflow.lease.heading"


  /** Cf-5c — When the main land is '03 - Non-residential', the lease type must be 'N - Non-residential'. */
object Cf5c_LeaseNNonResidential extends GuardRule:
    val id = "Cf-5c"
    val affects: ReturnSection = ReturnSection.Lease
    val inputs: Set[ReturnSection] = Set(ReturnSection.Lease, ReturnSection.Land)
    val targets: Seq[CrossFlowTarget] = Seq(leaseTypeTarget)

    protected def appliesTo(ua: UserAnswers): Boolean =
      mainLandPropertyType(ua).contains(NonResidential)

    protected def isValid(ua: UserAnswers): Boolean =
      isLeaseType(ua, LeaseNonResidential)

    protected def messageKey = "crossflow.lease.Cf-5c.body"

    protected override def inlineErrorKey = "crossflow.lease.Cf-5c.inline"

    protected override def headingKey = "crossflow.lease.heading"

object Cf6_MultiLandPropertyTypeMismatch extends LandGuardRule:
  val id      = "Cf-6"
  val affects: ReturnSection        = ReturnSection.Land
  val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Lease, ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget] = Seq(propertyTypeTarget)

  protected def appliesTo(land: Land, ua: UserAnswers): Boolean =
    hasLeaseInvolvement(ua) && landCount(ua) > 1

  protected def isValid(land: Land, ua: UserAnswers): Boolean = {
    val pts = allLandPropertyTypes(ua)
    pts.size <= 1
  }
  
  protected def messageKey              = "crossflow.land.Cf-6.body"
  protected override def inlineErrorKey = "crossflow.land.Cf-6.inline"
  protected override def headingKey     = "crossflow.land.Cf-6.heading"
  override val aggregateOnly: Boolean = true

  protected override def body: CrossFlowBody = WithBullets(
    messageKey,
    Seq("crossflow.land.Cf-6.bullet1",
      "crossflow.land.Cf-6.bullet2")
  )

object F23Rules:
  val all: Set[CrossFlowRule] = Set(
    FirstTimeBuyerRelief,
    FirstTimeBuyerReliefMultipleLands,
    MultipleDwellingsRelief,
    PreCompletionRelief,
    FifteenPercentRateRelief,
    FreeportRelief,
    InvestmentZoneRelief,
    SeedingRelief
  )

object F24Rules:
  val all: Set[LandRule] = Set(
    F24AdditionalResidentialEffDate
  )

object F25Rules:
  val all: Set[CrossFlowRule] = Set(
    F25EffectiveDate,
    F25ContractDate
  )

object F28Rules:
  val all: Set[CrossFlowRule] = Set(
    F28FtbCap500k,
    F28FtbCap625k
  )

object F17Rules:
  val all: Set[LandRule] = Set(
    Cf8_RegularWelshCodes,
    Cf9a_Welsh6996_6997EffDate,
    Cf9b_Welsh6998EffDate,
    Cf9c_Welsh6999EffDate,
    Cf10_Welsh6998ContractDate,
    Cf11_Welsh6999ContractDate
  )

object F18Rules:
  val all: Set[LandRule] = Set(
    Cf12_Dummy8998_8999EffDate,
    Cf13_Dummy8999ContractDate,
    Cf14_Dummy8998ContractDate,
    Cf15_ScottishCodes,
    Cf16_ScottishPostcode
  )


object F30Rules:
  val all: Set[CrossFlowRule] = Set(
    Cf5a_LeaseRResidential,
    Cf5b_LeaseMMixed,
    Cf5c_LeaseNNonResidential,
    Cf17_UseOfPropertyMissing
  )

object F30RulesLand:
  val all: Set[LandRule] = Set(
    Cf6_MultiLandPropertyTypeMismatch
  )