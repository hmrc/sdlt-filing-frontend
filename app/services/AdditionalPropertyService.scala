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
          additionalProperty(individual = propertyDetails.individual,propertyDetails.twoOrMoreProperties, propertyDetails.replaceMainResidence, premium, leaseDetails)
    }.getOrElse{
      throw new RequiredValueNotDefinedException(
        "[AdditionalPropertyService] [additionalPropertyRatesApply]" +
          " - property details not defined in additional property calculation"
      )}
  }

  private def additionalProperty(individual: Boolean, twoOrMoreProperties: Option[Boolean], replaceMainResidence: Option[Boolean],
                                 premium: BigDecimal, leaseYears: Option[Int]): Boolean = {
    (individual, twoOrMoreProperties, replaceMainResidence, leaseYears) match {
      case (false, None, None, Some(years)) => years > 7 && premium >= 40000
      case (false, None, None, None) => premium >= 40000
      case (true, Some(false), _, _) => false
      case (true, Some(true), Some(true), _) => false
      case (true, Some(true), Some(false), Some(years)) => years > 7 && premium >= 40000
      case (true, Some(true), Some(false), None) => premium >= 40000
      case (oIndividual, oTwoOrMore, oReplace, oLeaseDetails) =>
        throw new RequiredValueNotDefinedException(
          "[AdditionalPropertyService] [additionalProperty] - " +
            s"individual: $oIndividual, twoOrMoreProperties: $oTwoOrMore," +
            s" replaceMainResidence: $oReplace, leaseDetails: $oLeaseDetails"
        )
    }
  }
}
