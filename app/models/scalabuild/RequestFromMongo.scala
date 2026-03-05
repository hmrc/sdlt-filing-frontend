/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.scalabuild

import data.Dates
import data.Dates.{APR2021_RESIDENTIAL_DATE, APRIL2016_RESIDENTIAL_DATE, JUNE2021_RESIDENTIAL_DATE}
import enums.{HoldingTypes, PropertyTypes}
import models._
import play.api.libs.json.{Format, Json, OFormat}
import utils.CalculationUtils.DateHelper

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
    firstTimeBuyer: Option[Boolean],
    mainResidence: Option[Boolean],
    ownedOtherProperties: Option[Boolean]
) {
  def toRequest: Request = {
    val oNonUKResident = nonUKResident match {
      case Some(value) if effectiveDate.onOrAfter(APR2021_RESIDENTIAL_DATE) => Some(value)
      case _        => None
    }
    val oPropertyDetails: Option[PropertyDetails] = propertyType match {
      case enums.PropertyTypes.residential if effectiveDate.onOrAfter(APRIL2016_RESIDENTIAL_DATE)   => {
        propertyDetails match {
          case Some(value) => Some(constructPropertyDetails(value, mainResidence))
          case  None => None
        }
      }
      case _ => None
    }
    val oLeaseDetails = holdingType match {
      case enums.HoldingTypes.leasehold => Some(constructLeaseDetails).flatten
      case enums.HoldingTypes.freehold  => None
      case _ => None
    }
    val effDateWithinFTBRange:Boolean = (effectiveDate.onOrAfter(Dates.NOV2017_RESIDENTIAL_DATE)  && effectiveDate.isBefore(Dates.JULY2020_RESIDENTIAL_DATE)) | effectiveDate.isAfter(JUNE2021_RESIDENTIAL_DATE)

    val oFirstTimeBuyer = propertyType match {
      case enums.PropertyTypes.residential if effDateWithinFTBRange => {
        propertyDetails match {
          case Some(propDetails) => Some(validFirstTimeBuyer(propDetails, mainResidence, ownedOtherProperties))
          case None        => None
        }
      }
      case _ => None
    }
    val request = Request(
      holdingType = holdingType,
      propertyType = propertyType,
      effectiveDate = effectiveDate,
      nonUKResident = oNonUKResident,
      premium = premium,
      highestRent = getHighestRent,
      propertyDetails = oPropertyDetails,
      leaseDetails = oLeaseDetails,
      relevantRentDetails = relevantRentDetails,
      firstTimeBuyer = oFirstTimeBuyer,
      isLinked = None,
      interestTransferred = None,
      taxReliefDetails = None
    )
    request
  }

  private def constructPropertyDetails(propertyDetails: PropertyDetails, oMainResidence: Option[Boolean]) : PropertyDetails = {
    val twoOrMore = if (propertyDetails.individual) propertyDetails.twoOrMoreProperties else None
    val replaceMainResidence = if (propertyDetails.twoOrMoreProperties.contains(true)) propertyDetails.replaceMainResidence else None
    val sharedOwnership = if (oMainResidence.contains(true)) propertyDetails.sharedOwnership else None
    val currentValue = if (propertyDetails.sharedOwnership.contains(true)) propertyDetails.currentValue else None
      PropertyDetails(
        individual = propertyDetails.individual,
        twoOrMoreProperties = twoOrMore,
        replaceMainResidence = replaceMainResidence,
        sharedOwnership = sharedOwnership,
        currentValue = currentValue
      )

  }

  private def validFirstTimeBuyer(propertyDetails: PropertyDetails, mainResidence: Option[Boolean], ownsOtherProperties: Option[Boolean]): Boolean ={

    (ownsOtherProperties, mainResidence, propertyDetails.sharedOwnership, propertyDetails.currentValue) match {
      case (Some(false), Some(true), _, Some(false)) => false
      case (Some(false), Some(true), _, _) => true
      case _ => false
    }
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

