package calculation.services

import javax.inject.Singleton

import calculation.exceptions.RequiredValueNotDefinedException
import calculation.models.PropertyDetails

@Singleton
class RefundEntitlementService extends RefundEntitlementSrv

trait RefundEntitlementSrv {

  def calculateRefundEntitlement(premiumResultTaxDue: BigDecimal, prevResultTax: Int, reqPropertyDetails: Option[PropertyDetails]): Option[Int] ={
    if(premiumResultTaxDue.toInt > prevResultTax &&
        individualWithAdditionalProperty(reqPropertyDetails)
    ) Some(premiumResultTaxDue.toInt - prevResultTax) else None
  }

  private [services] def individualWithAdditionalProperty(oPropertyDetails: Option[PropertyDetails]): Boolean = {
    oPropertyDetails.map {propertyDetails =>
      if(propertyDetails.individual) {
        additionalProperty(propertyDetails.twoOrMoreProperties, propertyDetails.replaceMainResidence)
      } else false
    }.getOrElse{
      throw new RequiredValueNotDefinedException(
        "[FreeholdCalculationService] [individualWithAdditionalProperty]" +
          " - property details not defined in freehold residential additional property calculation"
      )}
  }

  private def additionalProperty(twoOrMoreProperties: Option[Boolean], replaceMainResidence: Option[Boolean]): Boolean = {
    (twoOrMoreProperties, replaceMainResidence) match {
      case (Some(twoOrMore), Some(replace)) => twoOrMore && !replace
      case (oTwoOrMore, oReplace) =>
        throw new RequiredValueNotDefinedException(
          "[FreeholdCalculationService] [additionalProperty]" +
            s" - twoOrMoreProperties: $oTwoOrMore, replaceMainResidence: $oReplace"
        )
    }
  }

}
