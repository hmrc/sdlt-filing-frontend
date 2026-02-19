/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package generators

import data.Dates.DECEMBER2014_RESIDENTIAL_DATE
import enums.sdltRebuild._
import enums.{HoldingTypes, PropertyTypes}
import models.sdltRebuild.TaxReliefDetails
import models.{LeaseDetails, LeaseTerm, PropertyDetails, Request}
import org.scalacheck.Gen

import java.time.LocalDate
import scala.util.Try

trait RequestGenerators {

  private val zeroRateTaxReliefGen: Gen[TaxReliefCode with StandardZeroRate] = Gen.oneOf(Set(
    PartExchange,
    ReLocationEmployment,
    CompulsoryPurchaseFacilitatingDevelopment,
    ComplianceWithPlanningObligations,
    GroupRelief,
    ReConstructionRelief,
    DemutualisationOfInsuranceCompany,
    DemutualisationOfBuildingSociety,
    IncorporationOfLimitedLiabilityPartnership,
    TransfersInvolvingPublicBodies,
    TransferInConsequenceOfReorganisationOfParliamentaryConstituencies,
    CharitiesTaxReliefs,
    AcquisitionByBodiesEstablishedForNationalPurposes,
    RegisteredSocialLandlords,
    AlternativePropertyFinance,
    CroftingCommunityRightToBuy,
    DiplomaticPrivileges,
    OtherTaxReliefs,
    CombinationOfReliefs,
    AlternativeFinanceInvestmentBondsRelief,
    SeedingRelief
  ))

  private val zeroRateLeaseHoldsTaxReliefGen: Gen[TaxReliefCode with StandardZeroRate] = Gen.oneOf(Set(
    PartExchange,
    ReLocationEmployment,
    CompulsoryPurchaseFacilitatingDevelopment,
    ComplianceWithPlanningObligations,
    GroupRelief,
    ReConstructionRelief,
    DemutualisationOfInsuranceCompany,
    DemutualisationOfBuildingSociety,
    IncorporationOfLimitedLiabilityPartnership,
    TransfersInvolvingPublicBodies,
    TransferInConsequenceOfReorganisationOfParliamentaryConstituencies,
    CharitiesTaxReliefs,
    AcquisitionByBodiesEstablishedForNationalPurposes,
    RegisteredSocialLandlords,
    AlternativePropertyFinance,
    CroftingCommunityRightToBuy,
    DiplomaticPrivileges,
    OtherTaxReliefs,
    CombinationOfReliefs,
    AlternativeFinanceInvestmentBondsRelief,
    SeedingRelief
  ))

  private val zeroRateTaxReliefForSelfAssessedGen: Gen[TaxReliefCode with StandardZeroRate] = Gen.oneOf(Set(
    PartExchange,
    ReLocationEmployment,
    CompulsoryPurchaseFacilitatingDevelopment,
    ComplianceWithPlanningObligations,
    GroupRelief,
    ReConstructionRelief,
    DemutualisationOfInsuranceCompany,
    DemutualisationOfBuildingSociety,
    IncorporationOfLimitedLiabilityPartnership,
    TransfersInvolvingPublicBodies,
    TransferInConsequenceOfReorganisationOfParliamentaryConstituencies,
    CharitiesTaxReliefs,
    AcquisitionByBodiesEstablishedForNationalPurposes,
    RegisteredSocialLandlords,
    AlternativePropertyFinance,
    CroftingCommunityRightToBuy,
    DiplomaticPrivileges,
    OtherTaxReliefs,
    CombinationOfReliefs,
    AlternativeFinanceInvestmentBondsRelief
  ))


  private val amountGen: Gen[Int] = Gen.oneOf(1 to 1000000)
  private val dateGenerator : Gen[Option[LocalDate]] = for {
    year <- Gen.oneOf(1990 to 2025)
    month <- Gen.oneOf(1 to 12)
    day <- Gen.oneOf(1 to 31)
  } yield Try {
    LocalDate.of(year, month, day)
  }.toOption

  private val beforeDateGenerator: LocalDate => Gen[Option[LocalDate]] = (beforeDate: LocalDate) => for {
    year <- Gen.oneOf(1990 to beforeDate.getYear)
    month <- Gen.oneOf(1 to 12)
    day <- Gen.oneOf(1 to 31)
  } yield Try {
    if (LocalDate.of(year, month, day).toEpochDay < beforeDate.toEpochDay)
      Some(LocalDate.of(year, month, day))
    else
      None
  }.toOption.flatten


  private val onOrAfterDateGenerator: LocalDate => Gen[Option[LocalDate]] = (onOrAfterDate: LocalDate) => for {
    year <- Gen.oneOf(onOrAfterDate.getYear to LocalDate.now.getYear)
    month <- Gen.oneOf(1 to 12)
    day <- Gen.oneOf(1 to 31)
  } yield Try {
    if (onOrAfterDate.toEpochDay < LocalDate.of(year, month, day).toEpochDay)
      Some(LocalDate.of(year, month, day))
    else
      None
  }.toOption.flatten

  private val onOrAfterAndBeforeDateGenerator: LocalDate => LocalDate => Gen[Option[LocalDate]] = (onOrAfterDate: LocalDate) => (beforeDate: LocalDate) => for {
    year <- Gen.oneOf(onOrAfterDate.getYear to beforeDate.getYear)
    month <- Gen.oneOf(1 to 12)
    day <- Gen.oneOf(1 to 31)
  } yield Try {
    if (LocalDate.of(year, month, day).toEpochDay >= onOrAfterDate.toEpochDay && LocalDate.of(year, month, day).toEpochDay < beforeDate.toEpochDay)
      Some(LocalDate.of(year, month, day))
    else
      None
  }.toOption.flatten

  private def generateLeaseDetails(date: LocalDate): LeaseDetails = {
    LeaseDetails(
      startDate = date,
      endDate = date.plusYears(1),
      leaseTerm = LeaseTerm(
        years = 1,
        days = 1,
        daysInPartialYear = 365
      ),
      year1Rent = 999,
      year2Rent = None,
      year3Rent = None,
      year4Rent = None,
      year5Rent = None
    )
  }


  private def taxReliefRequestGenerator(holdType: HoldingTypes.Value, propertyTypes: Seq[PropertyTypes.Value]): Gen[Request] =
    for {
      taxRelief <- if (holdType == HoldingTypes.freehold) zeroRateTaxReliefGen else zeroRateLeaseHoldsTaxReliefGen
      anyPropertyType <- if (propertyTypes.isEmpty) Gen.oneOf(PropertyTypes.residential, PropertyTypes.nonResidential, PropertyTypes.mixed)
      else Gen.oneOf(propertyTypes)
      nonZeroAmount <- amountGen
      anyDay <- dateGenerator
    } yield
      Request(
        holdingType = holdType,
        propertyType = anyPropertyType,
        effectiveDate = anyDay.getOrElse(LocalDate.of(2026, 1, 1)),
        nonUKResident = None,
        premium = nonZeroAmount,
        highestRent = BigDecimal(0),
        propertyDetails = Some(
          PropertyDetails(
            individual = true,
            twoOrMoreProperties = Some(false),
            replaceMainResidence = Some(true),
            sharedOwnership = None,
            currentValue = None
          )
        ),
        leaseDetails = None,
        relevantRentDetails = None,
        firstTimeBuyer = Some(true),
        isLinked = Some(false),
        interestTransferred = None,
        taxReliefDetails = Some(TaxReliefDetails(taxReliefCode = taxRelief, isPartialRelief = Some(false))),
      )

  val freeHoldRequestWithStandardPropertyTypesGenerator: Gen[Request] = taxReliefRequestGenerator(HoldingTypes.freehold, Seq(PropertyTypes.residential, PropertyTypes.nonResidential))
  val leasedHoldNonResidentialMixedRequestGenerator: Gen[Request] = taxReliefRequestGenerator(HoldingTypes.leasehold, Seq(PropertyTypes.nonResidential, PropertyTypes.mixed))

  val freeHoldSelfAssessedBeforeDateRequestGenerator: Gen[Request] = {
    for {
      propertyType <- Gen.oneOf(PropertyTypes.residential, PropertyTypes.nonResidential, PropertyTypes.mixed)
      taxRelief <- zeroRateTaxReliefForSelfAssessedGen
      nonZeroAmount <- amountGen
      effectiveDate <- beforeDateGenerator(DECEMBER2014_RESIDENTIAL_DATE)
    } yield
      Request(
        holdingType = HoldingTypes.freehold,
        propertyType = propertyType,
        effectiveDate = effectiveDate.getOrElse(DECEMBER2014_RESIDENTIAL_DATE.plusDays(-1)), // Before 04/12/2014 :: or any other date generate before
        nonUKResident = None,
        premium = nonZeroAmount,
        highestRent = BigDecimal(0),
        propertyDetails = Some(
          PropertyDetails(
            individual = true,
            twoOrMoreProperties = Some(false),
            replaceMainResidence = Some(true),
            sharedOwnership = None,
            currentValue = None
          )
        ),
        leaseDetails = None,
        relevantRentDetails = None,
        firstTimeBuyer = Some(true),
        isLinked = Some(true),
        interestTransferred = None,
        taxReliefDetails = Some(TaxReliefDetails(taxReliefCode = taxRelief, isPartialRelief = Some(false))),
      )
  }

  val freeHoldRightToBuy: Gen[Request] =
    for {
      propertyType <- Gen.oneOf(PropertyTypes.mixed, PropertyTypes.nonResidential)
      nonZeroAmount <- amountGen
      anyDay <- beforeDateGenerator(LocalDate.of(2016, 3, 17))
    } yield
      Request(
        holdingType = HoldingTypes.freehold,
        propertyType = propertyType,
        effectiveDate = anyDay.getOrElse(LocalDate.of(2016, 3, 16)),
        nonUKResident = None,
        premium = nonZeroAmount,
        highestRent = BigDecimal(0),
        propertyDetails = Some(
          PropertyDetails(
            individual = true,
            twoOrMoreProperties = Some(false),
            replaceMainResidence = Some(true),
            sharedOwnership = None,
            currentValue = None
          )
        ),
        leaseDetails = None,
        relevantRentDetails = None,
        firstTimeBuyer = Some(true),
        isLinked = Some(false),
        interestTransferred = None,
        taxReliefDetails = Some(
          TaxReliefDetails(taxReliefCode = RightToBuy,
          isPartialRelief = Some(false))),
      )

  val freeHoldResidentialRightToBuyFromMarch2012ToApril2014: Gen[Request] =
    for {
      nonZeroAmount <- amountGen
      anyDay <- onOrAfterAndBeforeDateGenerator(LocalDate.of(2012, 3, 22))(LocalDate.of(2014, 4, 12))
    } yield
      Request(
        holdingType = HoldingTypes.freehold,
        propertyType = PropertyTypes.residential,
        effectiveDate = anyDay.getOrElse(LocalDate.of(2012, 3, 22)),
        nonUKResident = None,
        premium = nonZeroAmount,
        highestRent = BigDecimal(0),
        propertyDetails = None,
        leaseDetails = None,
        relevantRentDetails = None,
        firstTimeBuyer = Some(true),
        isLinked = Some(true),
        interestTransferred = None,
        taxReliefDetails = Some(
          TaxReliefDetails(taxReliefCode = RightToBuy,
            isPartialRelief = None )),
      )

  def leaseholdNoTaxReliefGenerator(date: LocalDate, onOrAfter: Boolean): Gen[Request] = {
    for {
      propertyType <- Gen.oneOf(PropertyTypes.residential, PropertyTypes.nonResidential, PropertyTypes.mixed)
      nonZeroAmount <- amountGen
      maybeDate <- if(onOrAfter) onOrAfterDateGenerator(date) else beforeDateGenerator(date)

      effectiveDate = maybeDate.getOrElse(if(onOrAfter) date else date.minusDays(1))
    } yield
      Request(
        holdingType = HoldingTypes.leasehold,
        propertyType = propertyType,
        effectiveDate = effectiveDate,
        nonUKResident = None,
        premium = nonZeroAmount,
        highestRent = BigDecimal(0),
        propertyDetails = Some(
          PropertyDetails(
            individual = true,
            twoOrMoreProperties = None,
            replaceMainResidence = None,
            sharedOwnership = None,
            currentValue = None
          )
        ),
        leaseDetails = Some(generateLeaseDetails(effectiveDate)),
        relevantRentDetails = None,
        firstTimeBuyer = None,
        isLinked = Some(true),
        taxReliefDetails = None,
        interestTransferred = None
      )
  }

}