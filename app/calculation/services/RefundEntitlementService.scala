package calculation.services

import javax.inject.Singleton

import calculation.exceptions.RequiredValueNotDefinedException
import calculation.models.PropertyDetails

@Singleton
class RefundEntitlementService extends RefundEntitlementSrv

trait RefundEntitlementSrv {

  def calculateRefundEntitlement(premiumResultTaxDue: BigDecimal, prevResultTax: Int, reqPropertyDetails: Option[PropertyDetails]): Option[Int] ={
    if(eligibleForRefund(premiumResultTaxDue.toInt, prevResultTax, reqPropertyDetails))
      Some(premiumResultTaxDue.toInt - prevResultTax)
    else
      None
  }

  private def eligibleForRefund(currentTaxDue: Int, prevTaxDue: Int, oPropertyDetails: Option[PropertyDetails]): Boolean = {
    currentTaxDue > prevTaxDue && oPropertyDetails.map(_.individual).getOrElse(
      throw new RequiredValueNotDefinedException(
        "[RefundEntitlementService] [eligibleForRefund] - property details not defined when expected"
      )
    )
  }

}
