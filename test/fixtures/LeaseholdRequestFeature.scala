/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package fixtures

import enums.sdltRebuild.RightToBuy
import enums.{HoldingTypes, PropertyTypes}
import models.{LeaseDetails, LeaseTerm, PropertyDetails, RelevantRentDetails, Request}
import models.sdltRebuild.TaxReliefDetails

import java.time.LocalDate

trait LeaseholdRequestFeature extends LeaseDetailsFixture {

  //July 20 requests

  def leaseholdResidentialJuly20OnwardsFTBRequest(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 18150,
    leaseDetails = Some(testLeaseDetailsJuly2020),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = Some(true)
  )

  def leaseholdResidentialAddPropJuly20OnwardsRequestIsIndividual(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetailsJuly2020),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = None
  )

  def leaseholdResidentialAddPropJuly20OnwardsRequestNotIndividual(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetailsJuly2020),
    propertyDetails = Some(
      PropertyDetails(
        individual = false,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = None
  )

  def leaseholdResidentialJuly20OnwardsCompanyRequest(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 18150,
    leaseDetails = Some(testLeaseDetailsJuly2020),
    propertyDetails = Some(
      PropertyDetails(
        individual = false,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = Some(false)
  )

  def leaseholdResidentialJuly20OnwardsIndividualRequest(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 18150,
    leaseDetails = Some(testLeaseDetailsJuly2020),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = Some(false)
  )

  //Nov 17 requests

  def leaseholdResidentialNov17OnwardsFTBRequestShared(premium: BigDecimal, testLeaseDetails: LeaseDetails): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = LocalDate.of(2018, 1, 1),
    nonUKResident = None,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetails),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = Some(true),
        currentValue = Some(true)
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = Some(true)
  )

  def leaseholdResidentialNov17OnwardsFTBRequest(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetails),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = Some(true)
  )

  def leaseholdResidentialSept2022OnwardsFTBRequest(premium: BigDecimal, effectiveDate: LocalDate, nonUk: Option[Boolean] = Some(false)): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = nonUk,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetailsSept2022),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = Some(true)
  )

  def leaseholdResidentialSept2022OnwardsSharedOwnershipFTBRequest(premium: BigDecimal, effectiveDate: LocalDate, nonUk: Option[Boolean] = Some(false)): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = nonUk,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetailsSept2022),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = Some(true),
        sharedOwnership = Some(true),
        currentValue = Some(true)
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = Some(true)
  )

  //March 16 requests

  def leaseholdNonResidentialMar16OnwardsRequest(premium: BigDecimal, year2Rent: BigDecimal = 200, exchangedPreMarch2016: Boolean = true): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.nonResidential,
    effectiveDate = LocalDate.of(2013, 2, 14),
    nonUKResident = None,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetailsAllRentsUnder2000.copy(year2Rent = Some(year2Rent))),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = Some(
      RelevantRentDetails(
        exchangedContractsBeforeMar16 = Some(exchangedPreMarch2016),
        contractChangedSinceMar16 = Some(false),
        relevantRent = Some(999)
      )
    ),
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = Some(false)
  )

  //Dec 14 requests

  def leaseholdResidentialDec14OnwardsRequest(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetails),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = Some(false)
  )

  //April 16 requests

  def leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetails),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = None
  )

  def leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetails),
    propertyDetails = Some(
      PropertyDetails(
        individual = false,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = None
  )

  //March 12 requests

  def leaseholdResidentialMar12toDec14Request(premium: BigDecimal): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = LocalDate.of(2013, 2, 14),
    nonUKResident = None,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetails),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = Some(false)
  )

  def leaseholdNonResidentialMar12toMar16Request(premium: BigDecimal, year2Rent: BigDecimal = 200): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.nonResidential,
    effectiveDate = LocalDate.of(2013, 2, 14),
    nonUKResident = None,
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetailsAllRentsUnder2000.copy(year2Rent = Some(year2Rent))),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = Some(
      RelevantRentDetails(
        exchangedContractsBeforeMar16 = None,
        contractChangedSinceMar16 = None,
        relevantRent = Some(999)
      )
    ),
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = None
  )

  def leaseholdMixedNonResBeforeMarch2016Request(premium: BigDecimal): Request = {
    Request(
      holdingType = HoldingTypes.leasehold,
      propertyType = PropertyTypes.mixed,
      effectiveDate = LocalDate.of(2016, 1, 1),
      nonUKResident = Some(false),
      premium = premium,
      highestRent = BigDecimal(0),
      leaseDetails = Some(LeaseDetails(
        startDate = LocalDate.of(2014, 3, 24),
        endDate = LocalDate.of(2015, 3, 24),
        leaseTerm = LeaseTerm(
          years = 1,
          days = 1,
          daysInPartialYear = 365
        ),
        year1Rent = BigDecimal(999),
        year2Rent = Some(BigDecimal(999)),
        year3Rent = None,
        year4Rent = None,
        year5Rent = None
      )),
      isLinked = Some(false),
      propertyDetails = None,

      relevantRentDetails = Some(
        RelevantRentDetails(
          exchangedContractsBeforeMar16 = None,
          contractChangedSinceMar16 = None,
          relevantRent = Some(BigDecimal(999))
        )
      ),
      firstTimeBuyer = None,
      interestTransferred = None,
      taxReliefDetails = None
    )
  }

  def leaseholdResidentialAddPropOct24BeforeApril25RequestIsIndividual(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 125000,
    leaseDetails = Some(testLeaseDetailsOct2024),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = None
  )

  def leaseholdResidentialAddPropOct24BeforeApril25NonUKResRequestIsIndividual(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 99000,
    leaseDetails = Some(testLeaseDetailsOct2024NonUKRes),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = None
  )

  def leaseholdResidentialAddPropApril25OnwardsRequestIsIndividual(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 125000,
    leaseDetails = Some(testLeaseDetailsApril2025),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = None
  )

  def leaseholdResidentialAddPropApril25OnwardsNonUKResRequestIsIndividual(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 99000,
    leaseDetails = Some(testLeaseDetailsApril25OnwardsNonUKRes),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = None,
    interestTransferred = None,
    taxReliefDetails = None,
    firstTimeBuyer = None
  )

  def leaseholdMixedNonResidentialRightToBuyBeforeMarch16Request(premium: BigDecimal): Request = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.nonResidential,
    effectiveDate = LocalDate.of(2016, 3, 16),
    nonUKResident = None,
    premium = premium,
    highestRent = 0,
    leaseDetails = Some(testLeaseDetailMixedNonResidentialRightToBuyBeforeMarch16),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    isLinked = Some(false),
    interestTransferred = None,
    taxReliefDetails = Some( TaxReliefDetails(
      taxReliefCode = RightToBuy,
      isPartialRelief = None
    )),
    firstTimeBuyer = None
  )

}
