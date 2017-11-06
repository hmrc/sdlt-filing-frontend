package calculation.services

import javax.inject.{Inject, Singleton}

import calculation.data.{SlabRatesTables, SliceRatesTables}
import calculation.data.SignificantAmounts._
import calculation.exceptions.RequiredValueNotDefinedException
import calculation.factories.LeaseholdResultFactory
import calculation.models.calculationtables.SlabResult
import calculation.models.{LeaseDetails, Request, Result}
import calculation.validators.internal.ModelValidation

@Singleton
class LeaseholdCalculationService @Inject()(
  val baseCalculationService: BaseCalculationService
) extends LeaseholdCalculationSrv

trait LeaseholdCalculationSrv {

  val baseCalculationService: BaseCalculationSrv

  def leaseholdResidentialAddPropApr16Onwards(request: Request): Seq[Result] = ???

  def leaseholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false): Result = {
    val npv = getNPV("leaseholdResidentialDec14Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdResidentialDec14OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, SliceRatesTables.leaseholdResidentialDec14OnwardsPremiumRates.slices)

    LeaseholdResultFactory.leaseholdResidentialDec14OnwardsResult(leaseResult, premiumResult, npv)
  }

  def leaseholdResidentialMar12toDec14(request: Request): Result = {
    val npv = getNPV("leaseholdResidentialMar12toDec14", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdResidentialMar12toDec14LeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlab(request.premium, SlabRatesTables.leaseholdResidentialMar12toDec14PremiumRates.slabs)

    LeaseholdResultFactory.leaseholdResidentialMar12toDec14Result(leaseResult, premiumResult, npv)
  }

  def leaseholdNonResidentialMar16Onwards(request: Request): Seq[Result] = ???

  def leaseholdNonResidentialMar12toMar16(request: Request, asPrevResult: Boolean = false): Result = {
    val npv = getNPV("leaseholdNonResidentialMar12toMar16", request.leaseDetails)

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdNonResidentialMar12toMar16LeaseRates.slices)
    val premiumResult = if(eligibleForZeroRate(request)) {
      SlabResult(rate = 0, taxDue = 0)
    } else {
      baseCalculationService.calculateTaxDueSlab(request.premium, SlabRatesTables.leaseholdNonResidentialMar12toMar16PremiumRates.slabs)
    }

    LeaseholdResultFactory.leaseholdNonResidentialMar12toMar16Result(leaseResult, premiumResult, npv)
  }

  private[services] def getNPV(func: String, oLeaseDetails: Option[LeaseDetails]): BigDecimal = {
    baseCalculationService.calculateNPV(
      oLeaseDetails.getOrElse{throw new RequiredValueNotDefinedException(
        s"[LeaseholdCalculationService] [$func] Lease details not defined when required"
      )}
    )
  }

  private[services] def eligibleForZeroRate(request: Request): Boolean = {
    if(request.premium >= RELEVANT_RENT_PREMIUM_THRESHOLD)
      false
    else
      request.leaseDetails.map(ModelValidation.allRentsBelow2000) match {
        case Some(false) => false
        case Some(true)  => extractRelevantRent(request) < RELEVANT_RENT_ZERO_RATE_LIMIT
        case None =>
          throw new RequiredValueNotDefinedException(
          "[LeaseholdCalculationService] [eligibleForZeroRate] - lease details not defined when premium less than £150,000"
        )
      }
  }

  private def extractRelevantRent(request: Request): BigDecimal = {
    request.relevantRentDetails.flatMap(_.relevantRent).getOrElse{
      throw new RequiredValueNotDefinedException("[LeaseholdCalculationService] [extractRelevantRent] - relevant rent not defined")
    }
  }

  private[services] def nonResPrevCalcRequired(request: Request): Boolean = {
    if(request.premium >= RELEVANT_RENT_PREMIUM_THRESHOLD)
      true
    else
      request.leaseDetails.map(ModelValidation.allRentsBelow2000) match {
        case Some(false) => true
        case Some(true)  => request.relevantRentDetails.map{ details =>
          (details.exchangedContractsBeforeMar16, details.contractChangedSinceMar16) match {
            case (Some(true), Some(false)) => true
            case _ => false
          }
        }.getOrElse(
          throw new RequiredValueNotDefinedException("[LeaseholdCalculationService] [nonResPrevCalcRequired] - relevant rent not defined")
        )
        case None =>
          throw new RequiredValueNotDefinedException(
            "[LeaseholdCalculationService] [nonResPrevCalcRequired] - lease details not defined when premium less than £150,000"
          )
      }
  }

}
