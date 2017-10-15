package calculation.models

import java.time.LocalDate

import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.validators.api.RequestValidators
import play.api.libs.json.Json
import play.api.libs.functional.syntax._
import play.api.libs.json.__
import RequestValidators.{multiFieldDateReads, yesNoToBooleanReads}

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

object PropertyDetails {
  implicit val reads = (
    (__ \ "individual").read[Boolean](yesNoToBooleanReads) and
    (__ \ "twoOrMoreProperties").readNullable[Boolean](yesNoToBooleanReads) and
    (__ \ "replaceMainResidence").readNullable[Boolean](yesNoToBooleanReads)
  )(PropertyDetails.apply _)
}

object Request {
  implicit val reads = (
    (__ \ "holdingType").read[HoldingTypes.Value] and
    (__ \ "propertyType").read[PropertyTypes.Value] and
     __.read[LocalDate](multiFieldDateReads("effectiveDate")) and
    (__ \ "premium").read[BigDecimal] and
    (__ \ "highestRent").read[BigDecimal] and
    (__ \ "propertyDetails").readNullable[PropertyDetails] and
    (__ \ "leaseDetails").readNullable[LeaseDetails]
  )(Request.apply _)
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
