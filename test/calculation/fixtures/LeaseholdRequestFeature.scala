/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.fixtures

import java.time.LocalDate

import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.models.{LeaseDetails, PropertyDetails, RelevantRentDetails, Request}

trait LeaseholdRequestFeature extends LeaseDetailsFixture {

  def leaseholdResidentialJuly20OnwardsFTBRequest(premium: BigDecimal, effectiveDate: LocalDate) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
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
    firstTimeBuyer = Some(true)
  )

  def leaseholdResidentialNov17OnwardsFTBRequestShared(premium: BigDecimal, testLeaseDetails: LeaseDetails) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = LocalDate.of(2018, 1, 1),
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
    firstTimeBuyer = Some(true)
  )

  def leaseholdResidentialNov17OnwardsFTBRequest(premium: BigDecimal, effectiveDate: LocalDate) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
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
    firstTimeBuyer = Some(true)
  )


  def leaseholdResidentialAddPropJuly20OnwardsRequestIsIndividual(premium: BigDecimal, effectiveDate: LocalDate) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
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
    firstTimeBuyer = None
  )

  def leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(premium: BigDecimal, effectiveDate: LocalDate) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
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
    firstTimeBuyer = None
  )

  def leaseholdResidentialAddPropJuly20OnwardsRequestNotIndividual(premium: BigDecimal, effectiveDate: LocalDate) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
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
    firstTimeBuyer = None
  )

  def leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(premium: BigDecimal, effectiveDate: LocalDate) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
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
    firstTimeBuyer = None
  )

  def leaseholdResidentialJuly20OnwardsCompanyRequest(premium: BigDecimal, effectiveDate: LocalDate) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
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
    firstTimeBuyer = Some(false)
  )

  def leaseholdResidentialJuly20OnwardsIndividualRequest(premium: BigDecimal, effectiveDate: LocalDate) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
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
    firstTimeBuyer = Some(false)
  )

  def leaseholdResidentialDec14OnwardsRequest(premium: BigDecimal, effectiveDate: LocalDate) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
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
    firstTimeBuyer = Some(false)
  )

  def leaseholdResidentialMar12toDec14Request(premium: BigDecimal) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = LocalDate.of(2013, 2, 14),
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
    firstTimeBuyer = Some(false)
  )

  def leaseholdNonResidentialMar16OnwardsRequest(
                                                  premium: BigDecimal,
                                                  year2Rent: BigDecimal = 200,
                                                  exchangedPreMarch2016: Boolean = true
                                                ) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.nonResidential,
    effectiveDate = LocalDate.of(2013, 2, 14),
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
    firstTimeBuyer = Some(false)
  )

  def leaseholdNonResidentialMar12toMar16Request(premium: BigDecimal, year2Rent: BigDecimal = 200) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.nonResidential,
    effectiveDate = LocalDate.of(2013, 2, 14),
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
    firstTimeBuyer = None
  )

}
