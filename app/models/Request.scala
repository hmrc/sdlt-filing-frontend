/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models

import java.time.LocalDate
import enums.{HoldingTypes, PropertyTypes}
import validators.api.RequestValidators
import play.api.libs.json.{Format, Json, Reads, __}
import play.api.libs.functional.syntax._
import RequestValidators.{multiFieldDateReads, yesNoToBooleanReads}
import models.sdltRebuild.TaxReliefDetails

object RelevantRentDetails {
  implicit val reads: Reads[RelevantRentDetails] = (
    (__ \ "contractPre201603").readNullable[Boolean](yesNoToBooleanReads) and
      (__ \ "contractVariedPost201603").readNullable[Boolean](yesNoToBooleanReads) and
      (__ \ "relevantRent").readNullable[BigDecimal]
    )(RelevantRentDetails.apply _)
  val mongoFormats: Format[RelevantRentDetails] = Json.format
}

object LeaseTerm {
  implicit val format: Format[LeaseTerm] = Json.format
}

object LeaseDetails {
  implicit val reads: Reads[LeaseDetails] = (
     __.read[LocalDate](multiFieldDateReads("startDate")) and
     __.read[LocalDate](multiFieldDateReads("endDate")) and
    (__ \ "leaseTerm").read[LeaseTerm] and
    (__ \ "year1Rent").read[BigDecimal] and
    (__ \ "year2Rent").readNullable[BigDecimal] and
    (__ \ "year3Rent").readNullable[BigDecimal] and
    (__ \ "year4Rent").readNullable[BigDecimal] and
    (__ \ "year5Rent").readNullable[BigDecimal]
  )(LeaseDetails.apply _)
  val mongoFormats: Format[LeaseDetails] = Json.format
}

object PropertyDetails {
  implicit val reads: Reads[PropertyDetails] = (
    (__ \ "individual").read[Boolean](yesNoToBooleanReads) and
    (__ \ "twoOrMoreProperties").readNullable[Boolean](yesNoToBooleanReads) and
    (__ \ "replaceMainResidence").readNullable[Boolean](yesNoToBooleanReads) and
    (__ \ "sharedOwnership").readNullable[Boolean](yesNoToBooleanReads) and
    (__ \ "currentValue").readNullable[Boolean](yesNoToBooleanReads)
  )(PropertyDetails.apply _)
  val mongoFormats: Format[PropertyDetails] = Json.format
}

object Request {
  implicit val reads: Reads[Request] = (
    (__ \ "holdingType").read[HoldingTypes.Value] and
    (__ \ "propertyType").read[PropertyTypes.Value] and
     __.read[LocalDate](multiFieldDateReads("effectiveDate")) and
    (__ \ "nonUKResident").readNullable[Boolean](yesNoToBooleanReads) and
    (__ \ "premium").read[BigDecimal] and
    (__ \ "highestRent").read[BigDecimal] and
    (__ \ "propertyDetails").readNullable[PropertyDetails] and
    (__ \ "leaseDetails").readNullable[LeaseDetails] and
    (__ \ "relevantRentDetails").readNullable[RelevantRentDetails] and
    (__ \ "firstTimeBuyer").readNullable[Boolean](yesNoToBooleanReads) and
    (__ \ "isLinked").readNullable[Boolean] and
    (__ \ "taxReliefDetails").readNullable[TaxReliefDetails]
  )(Request.apply _)
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
                  firstTimeBuyer: Option[Boolean],
                  isLinked: Option[Boolean],
                  taxReliefDetails: Option[TaxReliefDetails]
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
