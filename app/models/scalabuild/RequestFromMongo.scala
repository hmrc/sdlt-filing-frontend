/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.scalabuild

import enums.{HoldingTypes, PropertyTypes}
import models._
import play.api.libs.json.{Format, Json, OFormat}

import java.time.LocalDate

object PageConstants {
  val propertyDetails: String = "propertyDetails"
  val leaseDetails: String = "mongoLeaseDetails"
  val relevantRentDetails: String = "relevantRentDetails"
  val leaseTerm: String = "leaseTerm"
}

case class MongoLeaseDetails(leaseDates: LeaseDates, rentDetails: RentPeriods, leaseTerm: LeaseTerm)
object MongoLeaseDetails {
  implicit val formats: OFormat[MongoLeaseDetails] = Json.format[MongoLeaseDetails]
}

case class RequestFromMongo(
    holdingType: HoldingTypes.Value,
    propertyType: PropertyTypes.Value,
    effectiveDate: LocalDate,
    nonUKResident: Option[Boolean],
    premium: BigDecimal,
    mongoLeaseDetails: Option[MongoLeaseDetails],
    propertyDetails: Option[PropertyDetails],
    relevantRentDetails: Option[RelevantRentDetails],
    firstTimeBuyer: Option[Boolean]
) {
  def toRequest: Request = {
    val request = Request(
      holdingType = holdingType,
      propertyType = propertyType,
      effectiveDate = effectiveDate,
      nonUKResident = nonUKResident,
      premium = premium,
      highestRent = getHighestRent,
      propertyDetails = propertyDetails,
      leaseDetails = constructLeaseDetails,
      relevantRentDetails = relevantRentDetails,
      firstTimeBuyer = Some(true),
      isLinked = None,
      interestTransferred = None,
      taxReliefDetails = None
    )
    request
  }

  private def getHighestRent: BigDecimal = {
    mongoLeaseDetails.fold(BigDecimal(0)) { leaseDetails => // todo: only returns 0 for highest rent
      leaseDetails.rentDetails.rents.max
    }
  }
  private def constructLeaseDetails: Option[LeaseDetails] = {
    mongoLeaseDetails.map(lease =>
      LeaseDetails(
        startDate = lease.leaseDates.startDate,
        endDate = lease.leaseDates.endDate,
        leaseTerm = lease.leaseTerm,
        year1Rent = lease.rentDetails.year1Rent,
        year2Rent = lease.rentDetails.year2Rent,
        year3Rent = lease.rentDetails.year3Rent,
        year4Rent = lease.rentDetails.year4Rent,
        year5Rent = lease.rentDetails.year5Rent
      )
    )
  }
}

case object RequestFromMongo {
  implicit val mongoFormats: Format[RequestFromMongo] = {
    implicit val propertyDetailsMongoFormats: Format[PropertyDetails] = PropertyDetails.mongoFormats
    implicit val relevantRentDetailsMongoFormats: Format[RelevantRentDetails] = RelevantRentDetails.mongoFormats
    Json.format[RequestFromMongo]
  }
}

