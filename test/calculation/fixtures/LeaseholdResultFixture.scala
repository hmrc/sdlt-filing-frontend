package calculation.fixtures

import calculation.enums.{CalcTypes, TaxTypes}
import calculation.models.{CalculationDetails, Result, SliceDetails}

trait LeaseholdResultFixture {


  def leaseholdResidentialAddPropApr16OnwardsResult(
                                              leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                              premTaxDue:  Int, premSliceDetails:  Seq[SliceDetails],
                                              npv:         Int, resHintAmount: Option[String]) =

      Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = Some("Results based on SDLT rules from 1 April 2016" ),
      resultHint = resHintAmount,
      npv = Some(npv),
        taxCalcs = Seq(
          CalculationDetails(
          taxType = TaxTypes.rent,
          calcType = CalcTypes.slice,
          detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 1 April 2016"),
          bandHeading = Some("Rent bands (£)"),
          detailFooter = Some("SDLT due on the rent"),
          taxDue = leaseTaxDue,
          slices = Some(leaseSliceDetails)
          ),
          CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = Some("This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 1 April 2016"),
          bandHeading = Some("Premium bands (£)"),
          detailFooter = Some("SDLT due on the premium"),
          taxDue = premTaxDue,
          slices = Some(premSliceDetails)
          )
        )
      )


  def leaseholdResidentialDec14OnwardsResult(
                                              leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                              premTaxDue:  Int, premSliceDetails:  Seq[SliceDetails],
                                              npv:         Int, asPreviousResult: Boolean = false) =
    Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = if(asPreviousResult) Some("Results based on SDLT rules before 1 April 2016") else None,
      resultHint = if(asPreviousResult) Some("You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015.") else None,
      npv = Some(npv),
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.rent,
          calcType = CalcTypes.slice,
          detailHeading = if(asPreviousResult){
            Some("This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules before 1 April 2016")
          }else{
            Some("This is a breakdown of how the amount of SDLT on the rent was calculated")
          },
          bandHeading = Some("Rent bands (£)"),
          detailFooter = Some("SDLT due on the rent"),
          taxDue = leaseTaxDue,
          slices = Some(leaseSliceDetails)
        ),
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = if(asPreviousResult){
            Some("This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules before 1 April 2016")
          }else{
            Some("This is a breakdown of how the amount of SDLT on the premium was calculated")
          },
          bandHeading = Some("Premium bands (£)"),
          detailFooter = Some("SDLT due on the premium"),
          taxDue = premTaxDue,
          slices = Some(premSliceDetails)
        )
      )
    )

  def leaseholdResidentialMar12toDec14Result(
                                              leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                              premTaxDue:  Int, premRate: Int,
                                              npv: Int) =
    Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = None,
      resultHint = None,
      npv = Some(npv),
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.rent,
          calcType = CalcTypes.slice,
          detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
          bandHeading = Some("Rent bands (£)"),
          detailFooter = Some("SDLT due on the rent"),
          taxDue = leaseTaxDue,
          slices = Some(leaseSliceDetails)
        ),
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slab,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          taxDue = premTaxDue,
          slices = None,
          rate = Some(premRate)
        )
      )
    )

}
