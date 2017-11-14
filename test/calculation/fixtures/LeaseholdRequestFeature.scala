package calculation.fixtures

import java.time.LocalDate

import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.models.{PropertyDetails, RelevantRentDetails, Request}

trait LeaseholdRequestFeature extends LeaseDetailsFixture {

  def leaseholdResidentialNov17OnwardsFTBRequest(premium: BigDecimal) = Request(
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
        replaceMainResidence = None
      )
    ),
    relevantRentDetails = None,
    firstTimeBuyer = Some(true)
  )

  def leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(premium: BigDecimal) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = LocalDate.of(2017, 12, 30),
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetails),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false)
      )
    ),
    relevantRentDetails = None,
    firstTimeBuyer = None
  )

  def leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(premium: BigDecimal) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = LocalDate.of(2017, 12, 30),
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetails),
    propertyDetails = Some(
      PropertyDetails(
        individual = false,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = Some(false)
      )
    ),
    relevantRentDetails = None,
    firstTimeBuyer = None
  )


  def leaseholdResidentialDec14OnwardsRequest(premium: BigDecimal) = Request(
    holdingType = HoldingTypes.leasehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = LocalDate.of(2017, 2, 14),
    premium = premium,
    highestRent = 1000,
    leaseDetails = Some(testLeaseDetails),
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None
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
        replaceMainResidence = None
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
        replaceMainResidence = None
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
        replaceMainResidence = None
      )
    ),
    relevantRentDetails = Some(
      RelevantRentDetails(
        exchangedContractsBeforeMar16 = None,
        contractChangedSinceMar16 = None,
        relevantRent = Some(999)
      )
    ),
    firstTimeBuyer = Some(false)
  )

}
