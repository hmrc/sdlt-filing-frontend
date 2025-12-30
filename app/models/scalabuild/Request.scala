/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.scalabuild

import enums.{HoldingTypes, PropertyTypes}

import java.time.LocalDate

object PageConstants {

  val propertyDetails: String = "propertyDetails"
  val leaseDetails: String = "leaseDetails"
  val relevantRentDetails: String = "relevantRentDetails"
  val leaseTerm: String = "leaseTerm"
}

case class Request(
                  holdingType: HoldingTypes.Value,
                  propertyType: PropertyTypes.Value,
                  effectiveDate: LocalDate,
                  nonUKResident: Option[Boolean],
                  premium: BigDecimal,
                  highestRent: BigDecimal,
                  propertyDetails: Option[PropertyDetails],
                  leaseDetails: Option[LeaseDetails],
                  relevantRentDetails: Option[RelevantRentDetails],
                  firstTimeBuyer: Option[Boolean]
                  )

case class PropertyDetails(
                          individual: Boolean,
                          twoOrMoreProperties: Option[Boolean],
                          replaceMainResidence: Option[Boolean],
                          sharedOwnership: Option[Boolean],
                          currentValue: Option[Boolean]
                          )

case class LeaseDetails(
                         startDate: LocalDate,
                         endDate: LocalDate,
                         leaseTerm: LeaseTerm,
                         year1Rent: BigDecimal,
                         year2Rent: Option[BigDecimal],
                         year3Rent: Option[BigDecimal],
                         year4Rent: Option[BigDecimal],
                         year5Rent: Option[BigDecimal]
                       )

case class LeaseTerm(
                    years: Int,
                    days: Int,
                    daysInPartialYear: Int
                    )

case class RelevantRentDetails(
                              exchangedContractsBeforeMar16: Option[Boolean],
                              contractChangedSinceMar16: Option[Boolean],
                              relevantRent: Option[BigDecimal]
                              )
