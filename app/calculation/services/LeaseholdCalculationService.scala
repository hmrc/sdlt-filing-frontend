package calculation.services

import javax.inject.{Inject, Singleton}

import calculation.models.{Request, Result}

@Singleton
class LeaseholdCalculationService @Inject()(
  val baseCalculationService: BaseCalculationService
) extends LeaseholdCalculationSrv

trait LeaseholdCalculationSrv {

  val baseCalculationService: BaseCalculationSrv

  def leaseholdResidentialAddPropApr16Onwards(request: Request): Seq[Result] = ???

  def leaseholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false): Result = ???

  def leaseholdResidentialMar12toDec14(request: Request): Result = ???

  def leaseholdNonResidentialMar16Onwards(request: Request): Seq[Result] = ???

  def leaseholdNonResidentialMar12toMar16(request: Request, asPrevResult: Boolean = false): Result = ???
}
