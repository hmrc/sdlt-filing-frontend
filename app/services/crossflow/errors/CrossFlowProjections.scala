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
import models.Land

object CrossFlowProjections:

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
    
  private def committedTotalPremium(ua: UserAnswers): Option[BigDecimal] =
    ua.fullReturn.flatMap(_.lease).flatMap(_.totalPremiumPayable).flatMap(s => Try(BigDecimal(s)).toOption)
  

  def totalPremium(ua: UserAnswers): Option[BigDecimal] =
    committedTotalPremium(ua)

  def currentSessionLand(ua: UserAnswers): Option[Land] =
    (ua.data \ "landCurrent").asOpt[LandSessionQuestions].map(landFromSession)

  private def landFromSession(lsq: LandSessionQuestions): Land =
    Land(
      landID = lsq.landId,
      propertyType = lsq.propertyType,
      localAuthorityNumber = Some(lsq.localAuthorityCode),
      postcode = Some(lsq.landAddress.postcode)
    )
  
  val scottishCodePattern: scala.util.matching.Regex = "^9[0-9]{3}$".r
  val dummyCodePattern: scala.util.matching.Regex = "^899[89]$".r

  val scottishPostcodePatterns: Seq[scala.util.matching.Regex] = Seq(
    "^ML([1-9]|1[0-2])$",
    "^PA(1|[3-9]|1[0-4]|1[6-9]|[2-4][0-9]|6[0-8]|7[0-8])$",
    "^PH([1-9]|1[0-9]|2[0-6]|3[0-9]|4[0-4])$",
    "^TD([1-4]|[6-7]|10|11|13|14)$",
    "^ZE([1-3])$",
    "^AB([1-3]|23|3[0-8]|4[1-5]|5[1-6])$",
    "^DD([1-9]|1[0-1])$",
    "^DG([1-9]|10|11|13)$",
    "^EH([1-2]|[4-9]|10|1[2-9]|2[0-9]|3[0-9]|4[0-9]|5[2-5])$",
    "^FK([1-9]|1[0-9]|2[0-1])$",
    "^G([1-2]|1[1-2]|14|15|20|21|32|33|41|43|45|46|51|53|6[0-9]|7[1-8]|8[1-4])$",
    "^HS([1-9])$",
    "^IV([1-9]|1[0-9]|2[0-8]|3[0-2]|36|4[0-9]|5[1-6])$",
    "^KA([1-9]|1[0-9]|2[0-9]|30)$",
    "^KW([1-3]|[5-9]|1[0-7])$",
    "^KY([1-9]|1[0-6])$"
  ).map(_.r)

  def isScottishPostcode(postcode: String): Boolean = {
    val outcode = postcode.trim.toUpperCase.split("\\s+").headOption.getOrElse("")
    scottishPostcodePatterns.exists(_.findFirstMatchIn(outcode).isDefined)
  }

  val welshRegularCodes: Set[String] = Set(
    "6805", "6810", "6815", "6820", "6825",
    "6828", "6829", "6830", "6835", "6840",
    "6845", "6850", "6853", "6854", "6855",
    "6905", "6910", "6915", "6920", "6925",
    "6930", "6935", "6940", "6945", "6950",
    "6955"
  )

  val welshSpecial6996_6997: Set[String] = Set("6996", "6997")
  val welshSpecial6998: String = "6998"
  val welshSpecial6999: String = "6999"

  val welshAllCodes: Set[String] =
    welshRegularCodes ++ welshSpecial6996_6997 ++ Set(welshSpecial6998, welshSpecial6999)

  object Dates:
    val reliefFloor2013: LocalDate = LocalDate.of(2013, 3, 6)
    val freeportStart: LocalDate = LocalDate.of(2021, 10, 19)
    val freeportEnd: LocalDate = LocalDate.of(2026, 9, 30)
    val investmentZoneStart: LocalDate = LocalDate.of(2023, 9, 29)
    val investmentZoneEnd: LocalDate = LocalDate.of(2034, 9, 30)
    val reservedInvestorsFund: LocalDate = LocalDate.of(2025, 3, 19)
    val mdrEffectiveDateCutOff: LocalDate = LocalDate.of(2024, 6, 1)
    val mdrLatestContractDate: LocalDate = LocalDate.of(2024, 3, 7)
    val ftbStart: LocalDate = LocalDate.of(2017, 11, 22)
    val ftbCap625FromSept2022: LocalDate = LocalDate.of(2022, 9, 23)
    val ftbCap500FromApril2025: LocalDate = LocalDate.of(2025, 4, 1)
    val welshActEffective: LocalDate = LocalDate.of(2018, 4, 1)
    val welshActDate: LocalDate = LocalDate.of(2014, 12, 17)
    val scotlandActDate: LocalDate = LocalDate.of(2012, 5, 1)
    val cr223Effective: LocalDate = LocalDate.of(2015, 4, 1)