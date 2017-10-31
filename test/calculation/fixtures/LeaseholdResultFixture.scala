package calculation.fixtures

import calculation.data.ResultText._
import calculation.enums.{CalcTypes, TaxTypes}
import calculation.models.{CalculationDetails, Result, SliceDetails}

trait LeaseholdResultFixture {

  def leaseholdResidentialDec14OnwardsResult(
      leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
      premTaxDue:  Int, premSliceDetails:  Seq[SliceDetails],
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
          detailHeading = Some(DETAIL_HEADING_SDLT_ON_RENT),
          bandHeading = Some(DETAIL_COL_HEADER_RENT),
          detailFooter = Some(DETAIL_FOOTER_RENT),
          taxDue = leaseTaxDue,
          slices = Some(leaseSliceDetails)
        ),
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = Some(DETAIL_HEADING_SDLT_ON_PREM),
          bandHeading = Some(DETAIL_COL_HEADER_PREM),
          detailFooter = Some(DETAIL_FOOTER_PREM),
          taxDue = premTaxDue,
          slices = Some(premSliceDetails)
        )
      )
    )

}
