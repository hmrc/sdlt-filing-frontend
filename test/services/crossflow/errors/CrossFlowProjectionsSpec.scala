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
import models.{Land, Lease, Transaction, UserAnswers}
import org.scalatest.matchers.must.Matchers
import pages.transaction._
import services.crossflow.errors.CrossFlowProjections.*

import java.time.LocalDate

class CrossFlowProjectionsSpec extends SpecBase with Matchers {

  private val date2024 = LocalDate.of(2024, 1, 15)

  private def withCommittedTransaction(
                                        claimingRelief: Option[String] = None,
                                        reliefReason:   Option[String] = None,
                                        effectiveDate:  Option[String] = None,
                                        contractDate:   Option[String] = None
                                      ): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
      transaction = Some(Transaction(
        claimingRelief = claimingRelief,
        reliefReason   = reliefReason,
        effectiveDate  = effectiveDate,
        contractDate   = contractDate
      ))
    )))

  private def withCommittedPropertyType(propertyType: String): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
      land = Some(Seq(Land(propertyType = Some(propertyType))))
    )))

  "isClaimingRelief" - {

    "must return true when the session answer is true" in {
      val ua = emptyUserAnswers.set(PurchaserEligibleToClaimReliefPage, true).success.value

      isClaimingRelief(ua) mustBe true
    }

    "must return false when the session answer is false" in {
      val ua = emptyUserAnswers.set(PurchaserEligibleToClaimReliefPage, false).success.value

      isClaimingRelief(ua) mustBe false
    }

    "must prefer the session answer over the committed snapshot" in {
      val ua = withCommittedTransaction(claimingRelief = Some("YES"))
        .set(PurchaserEligibleToClaimReliefPage, false).success.value

      isClaimingRelief(ua) mustBe false
    }

    "must fall back to the committed snapshot when no session answer exists" in {
      val ua = withCommittedTransaction(claimingRelief = Some("YES"))

      isClaimingRelief(ua) mustBe true
    }

    "must accept yes / Yes / YES case-insensitively in the committed snapshot" in {
      val ua = withCommittedTransaction(claimingRelief = Some("yes"))

      isClaimingRelief(ua) mustBe true
    }

    "must return false when neither session nor committed answer exists" in {
      isClaimingRelief(emptyUserAnswers) mustBe false
    }
  }

  "reliefReason" - {

    "must return the F23 code matching the session-selected enum" in {
      val ua = emptyUserAnswers.set(ReasonForReliefPage, ReasonForRelief.ReliefForFreeport).success.value

      reliefReason(ua) mustBe Some("36")
    }

    "must return None when the session-selected enum has no F23 code" in {
      val ua = emptyUserAnswers.set(ReasonForReliefPage, ReasonForRelief.PartExchange).success.value

      reliefReason(ua) mustBe None
    }

    "must NOT fall back to committed when the session is set but unmapped (regression: flatMap trap)" in {
      val ua = withCommittedTransaction(reliefReason = Some("36"))
        .set(ReasonForReliefPage, ReasonForRelief.PartExchange).success.value

      reliefReason(ua) mustBe None
    }

    "must fall back to the committed reliefReason when no session answer exists" in {
      val ua = withCommittedTransaction(reliefReason = Some("36"))

      reliefReason(ua) mustBe Some("36")
    }

    "must trim whitespace on the committed reliefReason" in {
      val ua = withCommittedTransaction(reliefReason = Some("  36  "))

      reliefReason(ua) mustBe Some("36")
    }

    "must return None when neither session nor committed answer exists" in {
      reliefReason(emptyUserAnswers) mustBe None
    }
  }

  "isReason" - {

    "must return true when the code matches" in {
      val ua = emptyUserAnswers.set(ReasonForReliefPage, ReasonForRelief.ReliefForFreeport).success.value

      isReason(ua, "36") mustBe true
    }

    "must return false when the code does not match" in {
      val ua = emptyUserAnswers.set(ReasonForReliefPage, ReasonForRelief.ReliefForFreeport).success.value

      isReason(ua, "37") mustBe false
    }

    "must return false when there is no relief reason set" in {
      isReason(emptyUserAnswers, "36") mustBe false
    }
  }

  "effectiveDate" - {

    "must return the session-set LocalDate directly" in {
      val ua = emptyUserAnswers.set(TransactionEffectiveDatePage, date2024).success.value

      effectiveDate(ua) mustBe Some(date2024)
    }

    "must prefer the session date over the committed snapshot" in {
      val ua = withCommittedTransaction(effectiveDate = Some("2020-01-01"))
        .set(TransactionEffectiveDatePage, date2024).success.value

      effectiveDate(ua) mustBe Some(date2024)
    }

    "must fall back to the committed snapshot when no session date exists" in {
      val ua = withCommittedTransaction(effectiveDate = Some("2024-01-15"))

      effectiveDate(ua) mustBe Some(date2024)
    }

    "must parse the committed date in yyyy-MM-dd format" in {
      val ua = withCommittedTransaction(effectiveDate = Some("2024-01-15"))

      effectiveDate(ua) mustBe Some(date2024)
    }

    "must parse the committed date in dd/MM/yyyy format" in {
      val ua = withCommittedTransaction(effectiveDate = Some("15/01/2024"))

      effectiveDate(ua) mustBe Some(date2024)
    }

    "must return None when neither session nor committed date exists" in {
      effectiveDate(emptyUserAnswers) mustBe None
    }

    "must return None when the committed date is unparseable" in {
      val ua = withCommittedTransaction(effectiveDate = Some("not-a-date"))

      effectiveDate(ua) mustBe None
    }
  }

  "contractDate" - {

    "must return the session-set LocalDate directly" in {
      val ua = emptyUserAnswers.set(TransactionDateOfContractPage, date2024).success.value

      contractDate(ua) mustBe Some(date2024)
    }

    "must prefer the session date over the committed snapshot" in {
      val ua = withCommittedTransaction(contractDate = Some("2020-01-01"))
        .set(TransactionDateOfContractPage, date2024).success.value

      contractDate(ua) mustBe Some(date2024)
    }

    "must fall back to the committed snapshot when no session date exists" in {
      val ua = withCommittedTransaction(contractDate = Some("2024-01-15"))

      contractDate(ua) mustBe Some(date2024)
    }

    "must return None when neither session nor committed date exists" in {
      contractDate(emptyUserAnswers) mustBe None
    }
  }

  "propertyTypes" - {

    "must return the committed property types when no session land exists" in {
      val ua = withCommittedPropertyType("01")

      propertyTypes(ua) mustBe Set("01")
    }

    "must return an empty set when neither session nor committed land exists" in {
      propertyTypes(emptyUserAnswers) mustBe empty
    }

    "must return multiple property types from multiple committed lands" in {
      val ua = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
        land = Some(Seq(
          Land(propertyType = Some("01")),
          Land(propertyType = Some("02"))
        ))
      )))

      propertyTypes(ua) mustBe Set("01", "02")
    }
  }

  "propertyTypeAcceptable" - {

    "must return true when the property type is in the allowed set" in {
      val ua = withCommittedPropertyType("01")

      propertyTypeAcceptable(ua, Set(Residential)) mustBe true
    }

    "must return false when the property type is not in the allowed set" in {
      val ua = withCommittedPropertyType("03")

      propertyTypeAcceptable(ua, Set(Residential)) mustBe false
    }

    "must return true when there are no property types (incomplete, not in error)" in {
      propertyTypeAcceptable(emptyUserAnswers, Set(Residential)) mustBe true
    }

    "must return true when ANY property type is in the allowed set" in {
      val ua = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
        land = Some(Seq(
          Land(propertyType = Some("01")),
          Land(propertyType = Some("03"))
        ))
      )))

      propertyTypeAcceptable(ua, Set(Residential)) mustBe true
    }
  }

  "effectiveDateAcceptable" - {

    "must return true when the date satisfies the predicate" in {
      val ua = emptyUserAnswers.set(TransactionEffectiveDatePage, date2024).success.value

      effectiveDateAcceptable(ua)(_.getYear == 2024) mustBe true
    }

    "must return false when the date does not satisfy the predicate" in {
      val ua = emptyUserAnswers.set(TransactionEffectiveDatePage, date2024).success.value

      effectiveDateAcceptable(ua)(_.getYear == 2030) mustBe false
    }

    "must return true when the date is missing (incomplete, not in error)" in {
      effectiveDateAcceptable(emptyUserAnswers)(_ => false) mustBe true
    }
  }

  "parseDate" - {

    "must parse yyyy-MM-dd" in {
      parseDate("2024-01-15") mustBe Some(date2024)
    }

    "must parse dd/MM/yyyy" in {
      parseDate("15/01/2024") mustBe Some(date2024)
    }

    "must trim surrounding whitespace" in {
      parseDate("  2024-01-15  ") mustBe Some(date2024)
    }

    "must return None for unparseable input" in {
      parseDate("not-a-date") mustBe None
    }

    "must return None for an empty string" in {
      parseDate("") mustBe None
    }
  }

  "within" - {

    val start = LocalDate.of(2021, 10, 19)
    val end   = LocalDate.of(2026,  9, 30)

    "must return true when the date is on the start boundary" in {
      within(start, start, end) mustBe true
    }

    "must return true when the date is on the end boundary" in {
      within(end, start, end) mustBe true
    }

    "must return true when the date is strictly inside the window" in {
      within(LocalDate.of(2023, 6, 1), start, end) mustBe true
    }

    "must return false when the date is before the start" in {
      within(start.minusDays(1), start, end) mustBe false
    }

    "must return false when the date is after the end" in {
      within(end.plusDays(1), start, end) mustBe false
    }
  }

  "Dates" - {

    "must have the FTB relief start at 22/11/2017" in {
      Dates.ftbStart mustBe LocalDate.of(2017, 11, 22)
    }

    "must have the FTB £625k cap window opening at 23/09/2022" in {
      Dates.ftbCap625FromSept2022 mustBe LocalDate.of(2022, 9, 23)
    }

    "must have the FTB £500k cap reset at 01/04/2025" in {
      Dates.ftbCap500FromApril2025 mustBe LocalDate.of(2025, 4, 1)
    }

    "must have the F23-34/35 floor at 06/03/2013" in {
      Dates.reliefFloor2013 mustBe LocalDate.of(2013, 3, 6)
    }

    "must have the Freeport window at 19/10/2021 to 30/09/2026" in {
      Dates.freeportStart mustBe LocalDate.of(2021, 10, 19)
      Dates.freeportEnd   mustBe LocalDate.of(2026,  9, 30)
    }

    "must have the Investment Zone window at 29/09/2023 to 30/09/2034" in {
      Dates.investmentZoneStart mustBe LocalDate.of(2023, 9, 29)
      Dates.investmentZoneEnd   mustBe LocalDate.of(2034, 9, 30)
    }

    "must have the Seeding (Reserved Investors Fund) date at 19/03/2025" in {
      Dates.reservedInvestorsFund mustBe LocalDate.of(2025, 3, 19)
    }

    "must have the F25 effective-date cutoff at 01/06/2024" in {
      Dates.mdrEffectiveDateCutOff mustBe LocalDate.of(2024, 6, 1)
    }

    "must have the F25 contract-date cutoff at 07/03/2024" in {
      Dates.mdrLatestContractDate mustBe LocalDate.of(2024, 3, 7)
    }

    "must have the Welsh Act effective date at 01/04/2018" in {
      Dates.welshActEffective mustBe LocalDate.of(2018, 4, 1)
    }

    "must have the Welsh Act date at 17/12/2014" in {
      Dates.welshActDate mustBe LocalDate.of(2014, 12, 17)
    }

    "must have the Scotland Act date at 01/05/2012" in {
      Dates.scotlandActDate mustBe LocalDate.of(2012, 5, 1)
    }

    "must have the CR223 effective date at 01/04/2015" in {
      Dates.cr223Effective mustBe LocalDate.of(2015, 4, 1)
    }
  }

  "totalPremium" - {

    "must return the lease's totalPremiumPayable when present" in {
      val ua = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
        lease = Some(Lease(totalPremiumPayable = Some("500000.00")))
      )))

      totalPremium(ua) mustBe Some(BigDecimal(500000))
    }

    "must parse decimal values" in {
      val ua = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
        lease = Some(Lease(totalPremiumPayable = Some("625000.50")))
      )))

      totalPremium(ua) mustBe Some(BigDecimal("625000.50"))
    }

    "must return None when there is no lease" in {
      val ua = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(lease = None)))

      totalPremium(ua) mustBe None
    }

    "must return None when the lease has no totalPremiumPayable" in {
      val ua = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
        lease = Some(Lease(totalPremiumPayable = None))
      )))

      totalPremium(ua) mustBe None
    }

    "must return None when the value is unparseable" in {
      val ua = emptyUserAnswers.copy(fullReturn = Some(emptyFullReturn.copy(
        lease = Some(Lease(totalPremiumPayable = Some("not-a-number")))
      )))

      totalPremium(ua) mustBe None
    }

    "must return None when there is no fullReturn at all" in {
      totalPremium(emptyUserAnswers) mustBe None
    }
  }
}