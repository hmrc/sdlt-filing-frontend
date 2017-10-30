package calculation.services

import javax.inject.{Inject, Singleton}

import calculation.data.{SlabRatesTables, SliceRatesTables}
import calculation.exceptions.RequiredValueNotDefinedException
import calculation.models.{PropertyDetails, Request, Result}
import calculation.factories.ResultFactory

@Singleton
class FreeholdCalculationService @Inject()(
  val baseCalculationService: BaseCalculationService
) extends FreeholdCalculationSrv

trait FreeholdCalculationSrv {

  val baseCalculationService: BaseCalculationSrv

  def freeholdResidentialAddPropApr16Onwards(request: Request): Seq[Result] = {
    val prevResult = freeholdResidentialDec14Onwards(request, asPreviousResult = true)

    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      SliceRatesTables.freeholdResidentialAddPropApr16OnwardsRates.slices
    )

    val refundEntitlement = if(
      currentPremiumResult.taxDue.toInt > prevResult.totalTax &&
        individualWithAdditionalProperty(request.propertyDetails)
    ) Some(currentPremiumResult.taxDue.toInt - prevResult.totalTax) else None

    Seq(
      ResultFactory.freeholdResidentialAddPropApr16OnwardsResult(currentPremiumResult, refundEntitlement),
      prevResult
    )
  }

  def freeholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      SliceRatesTables.freeholdResidentialDec14OnwardsRates.slices
    )

    ResultFactory.freeholdResidentialDec14OnwardsResult(premiumResult, asPreviousResult)
  }

  def freeholdResidentialMar12toDec14(request: Request): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      SlabRatesTables.freeholdResidentialMar12toDec14Rates.slabs
    )

    ResultFactory.freeholdResidentialMar12toDec14Result(premiumResult)
  }

  def freeholdNonResidentialMar16Onwards(request: Request): Seq[Result] = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      SliceRatesTables.freeholdNonResidentialMar16OnwardsRates.slices
    )

    Seq(
      ResultFactory.freeholdNonResidentialMar16OnwardsResult(premiumResult),
      freeholdNonResidentialMar12toMar16(request, asPrevResult = true)
    )
  }

  def freeholdNonResidentialMar12toMar16(request: Request, asPrevResult: Boolean = false): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      SlabRatesTables.freeholdNonResidentialMar12toMar16Rates.slabs
    )

    ResultFactory.freeholdNonResidentialMar12toMar16Result(premiumResult, asPrevResult)
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
