/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import javax.inject.Singleton

import exceptions.RequiredValueNotDefinedException
import models.PropertyDetails

@Singleton
class AdditionalPropertyService {

  def additionalPropertyRatesApply(oPropertyDetails: Option[PropertyDetails]): Boolean = {
    oPropertyDetails.map { propertyDetails =>
      if(propertyDetails.individual) {
        additionalProperty(propertyDetails.twoOrMoreProperties, propertyDetails.replaceMainResidence)
      } else true
    }.getOrElse{
      throw new RequiredValueNotDefinedException(
        "[AdditionalPropertyService] [additionalPropertyRatesApply]" +
          " - property details not defined in additional property calculation"
      )}
  }

  private def additionalProperty(twoOrMoreProperties: Option[Boolean], replaceMainResidence: Option[Boolean]): Boolean = {
    (twoOrMoreProperties, replaceMainResidence) match {
      case (Some(false), _) => false
      case (Some(true), Some(replace)) => !replace
      case (oTwoOrMore, oReplace) =>
        throw new RequiredValueNotDefinedException(
          "[AdditionalPropertyService] [additionalProperty]" +
            s" - twoOrMoreProperties: $oTwoOrMore, replaceMainResidence: $oReplace"
        )
    }
  }
}