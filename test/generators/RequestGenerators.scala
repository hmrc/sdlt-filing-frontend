/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package generators

import enums.sdltRebuild._
import enums.{HoldingTypes, PropertyTypes}
import models.sdltRebuild.TaxReliefDetails
import models.{PropertyDetails, Request}
import org.scalacheck.Gen

import java.time.LocalDate
import scala.util.Try

trait RequestGenerators {

  // 20 Items / ouf of 25 in TaxReliefCode
  // TODO: looks like something is missing already in the list below
  private val zeroRateTaxReliefGen: Gen[TaxReliefCode with ZeroRate] = Gen.oneOf(
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
    AcquisitionByBodiesEstablishedForNationalPurposes, RegisteredSocialLandlords,
    AlternativePropertyFinance,
    CroftingCommunityRightToBuy,
    DiplomaticPrivileges,
    OtherTaxReliefs,
    CombinationOfReliefs,
    AlternativeFinanceInvestmentBondsRelief,
    SeedingRelief)

  // 21 Items:: as per Jira Story specified / ouf of 25 in TaxReliefCode
  private val zeroRateLeaseHoldsTaxReliefGen: Gen[TaxReliefCode] = Gen.oneOf(Set(
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

  private val amountGen: Gen[Int] = Gen.oneOf(1 to 1000000)
  private val dateGenerator: Gen[Option[LocalDate]] = for {
    year <- Gen.oneOf(1990 to 2025)
    month <- Gen.oneOf(1 to 12)
    day <- Gen.oneOf(1 to 31)
  } yield Try {
    LocalDate.of(year, month, day)
  }.toOption

  private def taxReliefRequestGenerator(holdType: HoldingTypes.Value, propertyTypes: Seq[PropertyTypes.Value] = Seq.empty): Gen[Request] =
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
        isLinked = false,
        taxReliefDetails = Some(TaxReliefDetails(taxReliefCode = taxRelief, isPartialRelief = Some(false))),
      )

  val freeHoldRequestWithStandardPropertyTypesGenerator: Gen[Request] = taxReliefRequestGenerator(HoldingTypes.freehold, Seq(PropertyTypes.residential, PropertyTypes.nonResidential))
  val leasedHoldNonResidentialMixedRequestGenerator: Gen[Request] = taxReliefRequestGenerator(HoldingTypes.leasehold, Seq(PropertyTypes.nonResidential, PropertyTypes.mixed))

}