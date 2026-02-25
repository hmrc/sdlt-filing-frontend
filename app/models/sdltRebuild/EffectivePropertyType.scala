/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.sdltRebuild

import enums.PropertyTypes
import models.Request

sealed trait EffectivePropertyType
case object ResidentialAdditionalProperty extends EffectivePropertyType
case object Residential                   extends EffectivePropertyType
case object NonResidential                extends EffectivePropertyType
case object Mixed                         extends EffectivePropertyType

object EffectivePropertyType {

  private def isAdditionalProperty(request: Request): Boolean = {
    request.propertyDetails.exists(_.individual == true) &&
    request.propertyDetails.exists(_.twoOrMoreProperties.contains(true)) &&
    request.propertyDetails.exists(_.replaceMainResidence.contains(false))
  }

  def effectivePropertyType(request: Request): EffectivePropertyType =
    request.propertyType match {
      case PropertyTypes.residential if isAdditionalProperty(request) => ResidentialAdditionalProperty
      case PropertyTypes.residential                                  => Residential
      case PropertyTypes.nonResidential                               => NonResidential
      case mixed                                                      => Mixed
    }
}
