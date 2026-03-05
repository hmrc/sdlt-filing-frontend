/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package fixtures.scalabuild
import enums.{HoldingTypes, PropertyTypes}
import models.PropertyDetails
import models.scalabuild.RequestFromMongo

import java.time.LocalDate

trait FreeholdRequestFromMongo {
  val freeResRequestFromMongo: RequestFromMongo = RequestFromMongo(
    holdingType = HoldingTypes.freehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = LocalDate.of(2015, 1, 1),
    nonUKResident = Some(true),
    premium = 500000,
    mongoLeaseDetails = None,
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
      )
    ),
    firstTimeBuyer = None,
    relevantRentDetails = None,
    mainResidence = None,
    ownedOtherProperties = Some(true)
  )

  val slabRequest: RequestFromMongo = RequestFromMongo(
    holdingType = HoldingTypes.freehold,
    propertyType = PropertyTypes.nonResidential,
    effectiveDate = LocalDate.of(2021, 7, 1),
    nonUKResident = None,
    premium = 300000,
    mongoLeaseDetails = None,
    propertyDetails = None,
    firstTimeBuyer = None,
    relevantRentDetails = None,
    mainResidence = None,
    ownedOtherProperties = None
  )

}

