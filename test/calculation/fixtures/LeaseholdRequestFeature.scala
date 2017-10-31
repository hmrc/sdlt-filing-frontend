package calculation.fixtures

import java.time.LocalDate

import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.models.{PropertyDetails, Request}

trait LeaseholdRequestFeature extends LeaseDetailsFixture {

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
    )
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
    )
  )

}
