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

package services.taxCalculation

import base.SpecBase
import models._
import models.taxCalculation._

class TaxCalcRequestValidatorSpec extends SpecBase {

  def userAnswersWith(fr: FullReturn): UserAnswers =
    UserAnswers(id = "id", storn = "STORN", fullReturn = Some(fr))

  def freeholdReturn(
    propertyType: String = "01",
    effectiveDate: String = "2025-06-15",
    consideration: BigDecimal = 250000,
    isLinked: Option[String] = Some("no"),
    claimingRelief: Option[String] = Some("no"),
    reliefReason: Option[String] = None,
    reliefAmount: Option[BigDecimal] = None,
    isNonUkResident: Option[String] = Some("no")
  ): FullReturn = FullReturn(
    stornId = "STORN", returnResourceRef = "REF",
    land = Some(Seq(Land(propertyType = Some(propertyType), interestCreatedTransferred = Some("FPF")))),
    transaction = Some(Transaction(
      transactionDescription = Some("F"), effectiveDate = Some(effectiveDate),
      totalConsideration = Some(consideration), isLinked = isLinked,
      claimingRelief = claimingRelief, reliefReason = reliefReason, reliefAmount = reliefAmount
    )),
    residency = Some(Residency(isNonUkResidents = isNonUkResident))
  )

  def leaseholdReturn(
    startDate: String = "2025-06-15",
    endDate: String = "2030-06-14",
    effectiveDate: String = "2025-06-15",
    consideration: BigDecimal = 250000,
    npv: String = "100000",
    annualRentOver1000: Option[String] = Some("yes"),
    startingRent: Option[String] = None
  ): FullReturn = FullReturn(
    stornId = "STORN", returnResourceRef = "REF",
    land = Some(Seq(Land(propertyType = Some("01"), interestCreatedTransferred = Some("LG")))),
    transaction = Some(Transaction(
      transactionDescription = Some("L"), effectiveDate = Some(effectiveDate),
      totalConsideration = Some(consideration), isLinked = Some("no"),
      claimingRelief = Some("no")
    )),
    residency = Some(Residency(isNonUkResidents = Some("no"))),
    lease = Some(Lease(
      contractStartDate = Some(startDate), contractEndDate = Some(endDate),
      isAnnualRentOver1000 = annualRentOver1000, netPresentValue = Some(npv),
      startingRent = startingRent
    ))
  )

  "buildRequest" - {

    "missing fields" - {

      "must fail when fullReturn is missing" in {
        val ua = UserAnswers(id = "id", storn = "STORN", fullReturn = None)
        TaxCalcRequestValidator.buildRequest(ua) mustBe Left(MissingFullReturnError)
      }

      "must fail when land is missing" in {
        val fr = freeholdReturn().copy(land = None)
        TaxCalcRequestValidator.buildRequest(userAnswersWith(fr)) mustBe Left(MissingAboutTheLandError)
      }

      "must fail when transaction is missing" in {
        val fr = freeholdReturn().copy(transaction = None)
        TaxCalcRequestValidator.buildRequest(userAnswersWith(fr)) mustBe Left(MissingAboutTheTransactionError)
      }

      "must fail when interestCreatedTransferred is missing" in {
        val fr = freeholdReturn().copy(land = Some(Seq(Land(propertyType = Some("01")))))
        TaxCalcRequestValidator.buildRequest(userAnswersWith(fr)) mustBe Left(MissingLandAnswerError("interestCreatedTransferred"))
      }

      "must fail when propertyType is missing" in {
        val fr = freeholdReturn().copy(land = Some(Seq(Land(interestCreatedTransferred = Some("FPF")))))
        TaxCalcRequestValidator.buildRequest(userAnswersWith(fr)) mustBe Left(MissingLandAnswerError("propertyType"))
      }

      "must fail when totalConsideration is missing" in {
        val fr = freeholdReturn().copy(transaction = Some(Transaction(
          transactionDescription = Some("F"), effectiveDate = Some("2025-06-15")
        )))
        TaxCalcRequestValidator.buildRequest(userAnswersWith(fr)) mustBe Left(MissingTransactionAnswerError("totalConsideration"))
      }
    }

    "invalid values" - {

      "must fail when effectiveDate is unparseable" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn(effectiveDate = "not-a-date"))) mustBe
          Left(InvalidDateError("not-a-date"))
      }

      "must fail when transactionDescription is unrecognised" in {
        val fr = freeholdReturn().copy(transaction = Some(Transaction(
          transactionDescription = Some("X"), effectiveDate = Some("2025-06-15"),
          totalConsideration = Some(250000)
        )))
        TaxCalcRequestValidator.buildRequest(userAnswersWith(fr)) mustBe Left(UnknownHoldingTypeError("X"))
      }

      "must fail when propertyType is unrecognised" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn(propertyType = "99"))) mustBe
          Left(UnknownPropertyTypeError("99"))
      }
    }

    "holding type" - {

      "must map F to freehold" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn())).toOption.get.holdingType mustBe HoldingTypes.freehold
      }

      "must map L to leasehold" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(leaseholdReturn())).toOption.get.holdingType mustBe HoldingTypes.leasehold
      }

      "must map A to freehold" in {
        val fr = freeholdReturn().copy(transaction = Some(Transaction(
          transactionDescription = Some("A"), effectiveDate = Some("2025-06-15"),
          totalConsideration = Some(250000), isLinked = Some("no"), claimingRelief = Some("no")
        )))
        TaxCalcRequestValidator.buildRequest(userAnswersWith(fr)).toOption.get.holdingType mustBe HoldingTypes.freehold
      }
    }

    "property type" - {

      Seq(("01", PropertyTypes.residential), ("04", PropertyTypes.residential),
        ("02", PropertyTypes.mixed), ("03", PropertyTypes.nonResidential)).foreach { case (code, expected) =>

        s"must map property code $code to $expected" in {
          TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn(propertyType = code))).toOption.get.propertyType mustBe expected
        }
      }
    }

    "property details" - {

      "must set additional property rates for property type 04" in {
        val pd = TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn(propertyType = "04"))).toOption.get.propertyDetails.get
        pd.individual mustBe "Yes"
        pd.twoOrMoreProperties mustBe Some("Yes")
        pd.replaceMainResidence mustBe Some("No")
      }

      "must set non-additional property details for other property types" in {
        val pd = TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn(propertyType = "01"))).toOption.get.propertyDetails.get
        pd.individual mustBe "No"
        pd.twoOrMoreProperties mustBe None
      }
    }

    "non-UK resident" - {

      "must send None for residential before April 2021" in {
        val request = TaxCalcRequestValidator.buildRequest(userAnswersWith(
          freeholdReturn(effectiveDate = "2021-03-31", isNonUkResident = Some("yes"))
        )).toOption.get
        request.nonUKResident mustBe None
      }

      "must send capitalised value for residential after April 2021" in {
        val request = TaxCalcRequestValidator.buildRequest(userAnswersWith(
          freeholdReturn(effectiveDate = "2025-06-15", isNonUkResident = Some("yes"))
        )).toOption.get
        request.nonUKResident mustBe Some("Yes")
      }
    }

    "first time buyer" - {

      "must be Yes when relief reason is 32" in {
        val request = TaxCalcRequestValidator.buildRequest(userAnswersWith(
          freeholdReturn(claimingRelief = Some("yes"), reliefReason = Some("32"))
        )).toOption.get
        request.firstTimeBuyer mustBe Some("Yes")
      }

      "must be No when relief reason is not 32" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn())).toOption.get.firstTimeBuyer mustBe Some("No")
      }
    }

    "tax relief" - {

      "must build TaxReliefDetails when claiming relief" in {
        val relief = TaxCalcRequestValidator.buildRequest(userAnswersWith(
          freeholdReturn(claimingRelief = Some("yes"), reliefReason = Some("20"))
        )).toOption.get.taxReliefDetails.get
        relief.taxReliefCode mustBe 20
        relief.isPartialRelief mustBe Some(false)
      }

      "must set isPartialRelief to true when reliefAmount is present" in {
        val relief = TaxCalcRequestValidator.buildRequest(userAnswersWith(
          freeholdReturn(claimingRelief = Some("yes"), reliefReason = Some("36"), reliefAmount = Some(5000))
        )).toOption.get.taxReliefDetails.get
        relief.taxReliefCode mustBe 36
        relief.isPartialRelief mustBe Some(true)
      }

      "must be None when not claiming relief" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn())).toOption.get.taxReliefDetails mustBe None
      }

      "must fail when claimingRelief is missing entirely" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn(claimingRelief = None))) mustBe
          Left(MissingTransactionAnswerError("claimingRelief"))
      }

      "must fail when claiming relief but reliefReason is missing" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn(claimingRelief = Some("yes")))) mustBe
          Left(MissingTransactionAnswerError("reliefReason"))
      }

      "must fail when claiming relief but reliefReason is not an integer" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(
          freeholdReturn(claimingRelief = Some("yes"), reliefReason = Some("INVALID"))
        )) mustBe Left(InvalidReliefReasonError("INVALID"))
      }
    }

    "lease details" - {

      "must not include lease details for freehold" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn())).toOption.get.leaseDetails mustBe None
      }

      "must include lease details for grant of lease" in {
        val ld = TaxCalcRequestValidator.buildRequest(userAnswersWith(leaseholdReturn())).toOption.get.leaseDetails.get
        ld.startDateDay mustBe 15
        ld.startDateMonth mustBe 6
        ld.startDateYear mustBe 2025
        ld.endDateDay mustBe 14
        ld.endDateMonth mustBe 6
        ld.endDateYear mustBe 2030
      }

      "must calculate lease term correctly for a 5 year lease" in {
        val term = TaxCalcRequestValidator.buildRequest(userAnswersWith(
          leaseholdReturn(startDate = "2025-06-15", endDate = "2030-06-14")
        )).toOption.get.leaseDetails.get.leaseTerm
        term.years mustBe 5
        term.days mustBe 0
      }

      "must calculate lease term correctly for a partial year lease" in {
        val ld = TaxCalcRequestValidator.buildRequest(userAnswersWith(
          leaseholdReturn(startDate = "2025-06-15", endDate = "2025-12-14")
        )).toOption.get.leaseDetails.get
        ld.leaseTerm.years mustBe 0
        ld.leaseTerm.days mustBe 183
        ld.leaseTerm.daysInPartialYear mustBe 183
      }

      "must send correct number of rent entries for a 3 year lease" in {
        val ld = TaxCalcRequestValidator.buildRequest(userAnswersWith(
          leaseholdReturn(startDate = "2025-06-15", endDate = "2028-06-14")
        )).toOption.get.leaseDetails.get
        ld.year1Rent mustBe 0
        ld.year2Rent mustBe Some(0)
        ld.year3Rent mustBe Some(0)
        ld.year4Rent mustBe None
        ld.year5Rent mustBe None
      }

      "must include declaredNpv from lease" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(leaseholdReturn(npv = "75000"))).toOption.get.declaredNpv mustBe Some(BigDecimal(75000))
      }

      "must fail when lease start date is missing" in {
        val fr = leaseholdReturn().copy(lease = Some(Lease(contractEndDate = Some("2030-06-14"))))
        TaxCalcRequestValidator.buildRequest(userAnswersWith(fr)) mustBe Left(MissingLeaseAnswerError("contractStartDate"))
      }

      "must fail when lease end date is missing" in {
        val fr = leaseholdReturn().copy(lease = Some(Lease(contractStartDate = Some("2025-06-15"))))
        TaxCalcRequestValidator.buildRequest(userAnswersWith(fr)) mustBe Left(MissingLeaseAnswerError("contractEndDate"))
      }
    }

    "highest rent" - {

      "must be parsed from startingRent for leasehold" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(leaseholdReturn(startingRent = Some("1500")))).toOption.get.highestRent mustBe BigDecimal(1500)
      }
    }

    "relevant rent details" - {

      "must set relevantRent to 1000 when annual rent is over 1000" in {
        val rrd = TaxCalcRequestValidator.buildRequest(userAnswersWith(
          leaseholdReturn(annualRentOver1000 = Some("yes"))
        )).toOption.get.relevantRentDetails.get
        rrd.relevantRent mustBe Some(BigDecimal(1000))
      }

      "must set relevantRent to 0 when annual rent is not over 1000" in {
        val rrd = TaxCalcRequestValidator.buildRequest(userAnswersWith(
          leaseholdReturn(annualRentOver1000 = Some("no"))
        )).toOption.get.relevantRentDetails.get
        rrd.relevantRent mustBe Some(BigDecimal(0))
      }

      "must not include relevantRentDetails for freehold" in {
        TaxCalcRequestValidator.buildRequest(userAnswersWith(freeholdReturn())).toOption.get.relevantRentDetails mustBe None
      }
    }
  }
}
