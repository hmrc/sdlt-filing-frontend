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
import models.land.LandSessionQuestions
import models.transaction.ReasonForRelief
import play.api.libs.json.*
import pages.transaction.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

object ReliefProjections:

  val Residential           = "01"
  val Mixed                 = "02"
  val ResidentialAdditional = "04"

  private def committedTransaction(ua: UserAnswers)     = ua.fullReturn.flatMap(_.transaction)
  private def committedClaiming(ua: UserAnswers)        = committedTransaction(ua).flatMap(_.claimingRelief)
  private def committedReason(ua: UserAnswers)          = committedTransaction(ua).flatMap(_.reliefReason)
  private def committedEffectiveDate(ua: UserAnswers)   = committedTransaction(ua).flatMap(_.effectiveDate)
  private def committedPropertyTypes(ua: UserAnswers): Set[String] =
    ua.fullReturn.flatMap(_.land).toSeq.flatten.flatMap(_.propertyType).toSet
  
  /** Map ReasonForRelief enum -> bare numeric code stored on FullReturn.transaction. */
  private def reasonToCode(r: ReasonForRelief): Option[String] = r match
    case ReasonForRelief.FirstTimeBuyer       => Some("32")
    case ReasonForRelief.MultipleDwellings    => Some("33")
    case ReasonForRelief.PreCompletion        => Some("34")
    case ReasonForRelief.ReliefFromRate       => Some("35")
    case ReasonForRelief.ReliefForFreeport    => Some("36")
    case ReasonForRelief.ReliefInvestmentZone => Some("37")
    case ReasonForRelief.SeedingRelief        => Some("38")
    case _                                    => None
  
  private def sessionPropertyTypes(ua: UserAnswers): Set[String] =
    (ua.data \ "landCurrent").asOpt[LandSessionQuestions].flatMap(_.propertyType).toSet
  
  def effectiveDate(ua: UserAnswers): Option[LocalDate] =
    ua.get(TransactionEffectiveDatePage) match
      case Some(d) => Some(d)
      case None => committedEffectiveDate(ua).flatMap(parseDate)

  def isClaimingRelief(ua: UserAnswers): Boolean =
    ua.get(PurchaserEligibleToClaimReliefPage) match
      case Some(b) => b
      case None => committedClaiming(ua).exists(_.equalsIgnoreCase("yes"))
      
  def reliefReason(ua: UserAnswers): Option[String] =
    ua.get(ReasonForReliefPage) match
      case Some(r) => reasonToCode(r)
      case None => committedReason(ua).map(_.trim)

  def isReason(ua: UserAnswers, code: String): Boolean =
    reliefReason(ua).contains(code)
  
  def propertyTypes(ua: UserAnswers): Set[String] =
    val session = sessionPropertyTypes(ua)
    if session.nonEmpty then session else committedPropertyTypes(ua)
  
  def propertyTypeAcceptable(ua: UserAnswers, allowed: Set[String]): Boolean =
    val pts = propertyTypes(ua)
    pts.isEmpty || pts.exists(allowed.contains)
  
  def effectiveDateAcceptable(ua: UserAnswers)(pred: LocalDate => Boolean): Boolean =
    effectiveDate(ua).forall(pred)
  
  private def committedContractDate(ua: UserAnswers) = committedTransaction(ua).flatMap(_.contractDate)
  
  private def sessionContractDate(ua: UserAnswers): Option[LocalDate] =
    ua.get(TransactionDateOfContractPage)

  def contractDate(ua: UserAnswers): Option[LocalDate] =
    sessionContractDate(ua).orElse(committedContractDate(ua).flatMap(parseDate))
  

  private val dateFormats: Seq[DateTimeFormatter] =
    Seq("dd/MM/yyyy", "yyyy-MM-dd").map(DateTimeFormatter.ofPattern)

  def parseDate(raw: String): Option[LocalDate] =
    dateFormats.iterator.flatMap(f => Try(LocalDate.parse(raw.trim, f)).toOption).nextOption()

  def within(date: LocalDate, start: LocalDate, end: LocalDate): Boolean =
    !date.isBefore(start) && !date.isAfter(end)
  

  object Dates:
    val reliefFloor2013: LocalDate = LocalDate.of(2013, 3, 6)
    val freeportStart: LocalDate = LocalDate.of(2021, 10, 19)
    val freeportEnd: LocalDate = LocalDate.of(2026, 9, 30)
    val investmentZoneStart: LocalDate = LocalDate.of(2023, 9, 29)
    val investmentZoneEnd: LocalDate = LocalDate.of(2034, 9, 30)
    val reservedInvestorsFund: LocalDate = LocalDate.of(2025, 3, 19)
    val mdrEffectiveDateCutOff: LocalDate = LocalDate.of(2024, 6, 1)
    val mdrLatestContractDate: LocalDate = LocalDate.of(2024, 3, 7)