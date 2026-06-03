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

import models.UserAnswers
import services.crossflow.*
import services.crossflow.errors.ReliefProjections.*
import services.crossflow.errors.Targets.*

private object Targets:
  val reliefReasonTarget:  CrossFlowTarget = CrossFlowTarget(Pages.ReliefReason,     Fields.ReliefReason)
  val effectiveDateTarget: CrossFlowTarget = CrossFlowTarget(Pages.EffectiveDate,    Fields.EffectiveDate)
  val propertyTypeTarget:  CrossFlowTarget = CrossFlowTarget(Pages.LandPropertyType, Fields.PropertyType)
  val contractDateTarget:  CrossFlowTarget = CrossFlowTarget(Pages.ContractDate,     Fields.ContractDate)

/** Property type must be 01 (Residential). */
object FirstTimeBuyerRelief extends GuardRule:
  val id      = "F23-32"
  val affects: ReturnSection         = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]    = Set(ReturnSection.Transaction, ReturnSection.Land)
  val targets: Seq[CrossFlowTarget]  = Seq(reliefReasonTarget, propertyTypeTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "32")
  protected def isValid(ua: UserAnswers):   Boolean = propertyTypeAcceptable(ua, Set(Residential))
  protected def messageKey                          = "crossflow.relief.firstTimeBuyer.notResidential"

/** Property type must be 01 / 02 / 04. The date side of code 33 is owned by F25. */
object MultipleDwellingsRelief extends GuardRule:
  val id      = "F23-33"
  val affects: ReturnSection         = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]    = Set(ReturnSection.Transaction, ReturnSection.Land)
  val targets: Seq[CrossFlowTarget]  = Seq(reliefReasonTarget, propertyTypeTarget)

  private val allowedPropertyTypes = Set(Residential, Mixed, ResidentialAdditional)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "33")
  protected def isValid(ua: UserAnswers):   Boolean = propertyTypeAcceptable(ua, allowedPropertyTypes)
  protected def messageKey                          = "crossflow.relief.multipleDwellings.notAllowedPropertyType"

/** Effective date on/after 06/03/2013. */
object PreCompletionRelief extends GuardRule:
  val id      = "F23-34"
  val affects: ReturnSection         = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]    = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget]  = Seq(reliefReasonTarget, effectiveDateTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "34")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(!_.isBefore(Dates.reliefFloor2013))
  protected def messageKey                          = "crossflow.relief.preCompletion.dateTooEarly"

/** Effective date on/after 06/03/2013. */
object FifteenPercentRateRelief extends GuardRule:
  val id      = "F23-35"
  val affects: ReturnSection         = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]    = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget]  = Seq(reliefReasonTarget, effectiveDateTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "35")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(!_.isBefore(Dates.reliefFloor2013))
  protected def messageKey                          = "crossflow.relief.fifteenPercentRate.dateTooEarly"

/** Effective date within 19/10/2021..30/09/2026 (inclusive). */
object FreeportRelief extends GuardRule:
  val id      = "F23-36"
  val affects: ReturnSection         = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]    = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget]  = Seq(reliefReasonTarget, effectiveDateTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "36")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(within(_, Dates.freeportStart, Dates.freeportEnd))
  protected def messageKey                          = "crossflow.relief.freeport.outsideWindow"

/** Effective date within 29/09/2023..30/09/2034 (inclusive). */
object InvestmentZoneRelief extends GuardRule:
  val id      = "F23-37"
  val affects: ReturnSection         = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]    = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget]  = Seq(reliefReasonTarget, effectiveDateTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "37")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(within(_, Dates.investmentZoneStart, Dates.investmentZoneEnd))
  protected def messageKey                          = "crossflow.relief.investmentZone.outsideWindow"

/** Effective date on/after the Reserved Investors Fund date 19/03/2025. */
object SeedingRelief extends GuardRule:
  val id      = "F23-38"
  val affects: ReturnSection         = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]    = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget]  = Seq(reliefReasonTarget, effectiveDateTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "38")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(!_.isBefore(Dates.reservedInvestorsFund))
  protected def messageKey                          = "crossflow.relief.seeding.dateTooEarly"

/** F25 effective-date half: effective date must be before 01/06/2024. Null is OK. */
object F25EffectiveDate extends GuardRule:
  val id      = "F25-effective"
  val affects: ReturnSection         = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]    = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget]  = Seq(reliefReasonTarget, effectiveDateTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "33")
  protected def isValid(ua: UserAnswers):   Boolean = effectiveDateAcceptable(ua)(_.isBefore(Dates.mdrEffectiveDateCutOff))
  protected def messageKey                          = "crossflow.relief.multipleDwellings.effectiveDateTooLate"

/** F25 contract-date half: contract date must be present AND before 07/03/2024. */
object F25ContractDate extends GuardRule:
  val id      = "F25-contract"
  val affects: ReturnSection         = ReturnSection.Transaction
  val inputs:  Set[ReturnSection]    = Set(ReturnSection.Transaction)
  val targets: Seq[CrossFlowTarget]  = Seq(reliefReasonTarget, contractDateTarget)

  protected def appliesTo(ua: UserAnswers): Boolean = isClaimingRelief(ua) && isReason(ua, "33")
  protected def isValid(ua: UserAnswers):   Boolean = contractDate(ua).exists(_.isBefore(Dates.mdrLatestContractDate))
  protected def messageKey                          = "crossflow.relief.multipleDwellings.contractDateTooLate"


object F23ReliefReasonRules:
  val all: Set[CrossFlowRule] = Set(
    FirstTimeBuyerRelief,
    MultipleDwellingsRelief,
    PreCompletionRelief,
    FifteenPercentRateRelief,
    FreeportRelief,
    InvestmentZoneRelief,
    SeedingRelief,
    F25EffectiveDate,
    F25ContractDate
  )