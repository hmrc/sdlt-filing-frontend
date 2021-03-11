/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import exceptions.RequiredValueNotDefinedException
import models.PropertyDetails

import javax.inject.Singleton

@Singleton
class AdditionalPropertyService {

  def additionalPropertyRatesApply(premium: BigDecimal, oPropertyDetails: Option[PropertyDetails],
                                   leaseDetails: Option[Int]): Boolean = {
    oPropertyDetails.map { propertyDetails =>
      if(propertyDetails.individual)
          additionalProperty(propertyDetails.twoOrMoreProperties, propertyDetails.replaceMainResidence, premium, leaseDetails)
      else true
    }.getOrElse{
      throw new RequiredValueNotDefinedException(
        "[AdditionalPropertyService] [additionalPropertyRatesApply]" +
          " - property details not defined in additional property calculation"
      )}
  }

  private def additionalProperty(twoOrMoreProperties: Option[Boolean], replaceMainResidence: Option[Boolean],
                                 premium: BigDecimal, leaseYears: Option[Int]): Boolean = {
    (twoOrMoreProperties, replaceMainResidence, leaseYears) match {
      case (Some(false), _, _) => false
      case (Some(true), Some(true), _) => false
      case (Some(true), Some(false), Some(years)) => years > 7 && premium >= 40000
      case (Some(true), Some(false), None) => premium >= 40000
      case (oTwoOrMore, oReplace, oLeaseDetails) =>
        throw new RequiredValueNotDefinedException(
          "[AdditionalPropertyService] [additionalProperty]" +
            s" - twoOrMoreProperties: $oTwoOrMore, replaceMainResidence: $oReplace, leaseDetails: $oLeaseDetails"
        )
    }
  }
}
