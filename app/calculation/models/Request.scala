package calculation.models

import java.time.LocalDate

import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.validators.api.RequestValidators
import play.api.libs.json.Json
import play.api.libs.functional.syntax._
import play.api.libs.json._

import RequestValidators.multiFieldDateReads

object LeaseTerm {
  implicit val reads = Json.reads[LeaseTerm]
}

object LeaseDetails {
  implicit val reads = (
     __.read[LocalDate](multiFieldDateReads("startDate")) and
     __.read[LocalDate](multiFieldDateReads("endDate")) and
    (__ \ "leaseTerm").read[LeaseTerm] and
    (__ \ "year1Rent").read[BigDecimal] and
    (__ \ "year2Rent").read[BigDecimal] and
    (__ \ "year3Rent").read[BigDecimal] and
    (__ \ "year4Rent").read[BigDecimal] and
    (__ \ "year5Rent").read[BigDecimal]
  )(LeaseDetails.apply _)
}

case class Request(
                  holdingType: HoldingTypes.Value,
                  propertyType: PropertyTypes.Value,
                  effectiveDate: LocalDate,
                  premium: BigDecimal,
                  highestRent: BigDecimal,
                  propertyDetails: Option[PropertyDetails],
                  leaseDetails: Option[LeaseDetails]
                  )

case class PropertyDetails(
                          individual: Boolean,
                          twoOrMoreProperties: Option[Boolean],
                          replaceMainResidence: Option[Boolean]
                          )

case class LeaseDetails(
                         startDate: LocalDate,
                         endDate: LocalDate,
                         leaseTerm: LeaseTerm,
                         year1Rent: BigDecimal,
                         year2Rent: BigDecimal,
                         year3Rent: BigDecimal,
                         year4Rent: BigDecimal,
                         year5Rent: BigDecimal
                       )

  case class LeaseTerm(
                      years: Int,
                      days: Int,
                      daysInPartialYear: Int
                      )
