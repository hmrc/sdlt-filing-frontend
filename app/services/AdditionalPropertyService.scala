/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import enums.sdltRebuild.{PreCompletionTransaction, TaxReliefCode}
import exceptions.RequiredValueNotDefinedException
import models.PropertyDetails

import javax.inject.Singleton

@Singleton
class AdditionalPropertyService {

  def additionalPropertyRatesApply(premium: BigDecimal, oPropertyDetails: Option[PropertyDetails],
                                   leaseDetails: Option[Int], taxReliefCode: Option[TaxReliefCode] = None): Boolean = {
    oPropertyDetails.map { propertyDetails =>
          additionalProperty(individual = propertyDetails.individual,propertyDetails.twoOrMoreProperties, propertyDetails.replaceMainResidence, premium, leaseDetails, taxReliefCode)
    }.getOrElse{
      throw new RequiredValueNotDefinedException(
        "[AdditionalPropertyService] [additionalPropertyRatesApply]" +
          " - property details not defined in additional property calculation"
      )}
  }

  private def additionalProperty(individual: Boolean, twoOrMoreProperties: Option[Boolean], replaceMainResidence: Option[Boolean],
                                 premium: BigDecimal, leaseYears: Option[Int], taxReliefCode: Option[TaxReliefCode]): Boolean = {
    (individual, twoOrMoreProperties, replaceMainResidence, leaseYears, taxReliefCode) match {
      case (true, Some(true), Some(true), _, Some(PreCompletionTransaction)) => true
      case (false, None, None, Some(years), _) => years > 7 && premium >= 40000
      case (false, None, None, None, _) => premium >= 40000
      case (true, Some(false), _, _, _) => false
      case (true, Some(true), Some(true), _, _) => false
      case (true, Some(true), Some(false), Some(years), _) => years > 7 && premium >= 40000
      case (true, Some(true), Some(false), None, _) => premium >= 40000
      case (oIndividual, oTwoOrMore, oReplace, oLeaseDetails, _) =>
        throw new RequiredValueNotDefinedException(
          "[AdditionalPropertyService] [additionalProperty] - " +
            s"individual: $oIndividual, twoOrMoreProperties: $oTwoOrMore," +
            s" replaceMainResidence: $oReplace, leaseDetails: $oLeaseDetails"
        )
    }
  }
}
