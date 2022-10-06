/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package fixtures

import enums.{CalcTypes, TaxTypes}
import models.{CalculationDetails, Result, SliceDetails}

trait LeaseholdResultFixture {

  //July 20 results

  def leaseholdResidentialAddPropJuly20OnwardsResult(
                                                      leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                                      premTaxDue:  Int, premSliceDetails:  Seq[SliceDetails],
                                                      npv:         Int, resHintAmount: Option[String] = None): Result =

    Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
      resultHint = resHintAmount.map(value => s"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. " +
        s"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £$value."),
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
          calcType = CalcTypes.slice,
          detailHeading = Some("This is a breakdown of how the amount of SDLT on the premium was calculated"),
          bandHeading = Some("Premium bands (£)"),
          detailFooter = Some("SDLT due on the premium"),
          taxDue = premTaxDue,
          slices = Some(premSliceDetails)
        )
      )
    )

  def leaseholdResidentialJuly20OnwardsResult(
                                               leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                               premTaxDue:  Int, premSliceDetails:  Seq[SliceDetails],
                                               npv:         Int, asPreviousResult: Boolean = false): Result = {
    val resultheading = if(asPreviousResult) {
      Some("Result if you become eligible for a repayment of the higher rate on additional dwellings")
    } else {
      Some("Results of calculation based on SDLT rules for the effective date entered")
    }

    val resultHint = if(asPreviousResult) {
      Some("If you dispose of your previous main residence within 3 years you may be eligible for a refund. " +
        "You must apply for any repayment within 12 months of disposing of your old main residence.")
    } else {
      None
    }

    Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = resultheading,
      resultHint = resultHint,
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
          calcType = CalcTypes.slice,
          detailHeading = Some("This is a breakdown of how the amount of SDLT on the premium was calculated"),
          bandHeading = Some("Premium bands (£)"),
          detailFooter = Some("SDLT due on the premium"),
          taxDue = premTaxDue,
          slices = Some(premSliceDetails)
        )
      )
    )
  }


  //Nov 17 results

  def leaseholdResidentialNov17OnwardsFTBResult(
                                              leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                              premTaxDue:  Int, premSliceDetails:  Seq[SliceDetails],
                                              npv:         Int): Result = {
    val resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered")

      Result(
        totalTax = leaseTaxDue + premTaxDue,
        resultHeading = resultHeading,
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
            calcType = CalcTypes.slice,
            detailHeading = Some("This is a breakdown of how the amount of SDLT on the premium was calculated"),
            bandHeading = Some("Premium bands (£)"),
            detailFooter = Some("SDLT due on the premium"),
            taxDue = premTaxDue,
            slices = Some(premSliceDetails)
          )
        )
      )
    }

  //  Sept 22 result
  def leaseholdResidentialSept2022OnwardsNonUKFTBResult(
                                                 leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                                 premTaxDue:  Int, premSliceDetails:  Seq[SliceDetails],
                                                 leaseTaxDueRefunded: Int, leaseSliceDetailsRefunded: Seq[SliceDetails],
                                                 premTaxDueRefunded:  Int, premSliceDetailsRefunded:  Seq[SliceDetails],
                                                 npv:         Int): Seq[Result] = {
    val resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered")
    val refundHeading = Some("Result if you become eligible for a repayment of the non-resident rate of SDLT")
    val resultHint = Some("The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.")
    val refundHint = Some("If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.")

    Seq(
    Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = resultHeading,
      resultHint = resultHint,
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
          calcType = CalcTypes.slice,
          detailHeading = Some("This is a breakdown of how the amount of SDLT on the premium was calculated"),
          bandHeading = Some("Premium bands (£)"),
          detailFooter = Some("SDLT due on the premium"),
          taxDue = premTaxDue,
          slices = Some(premSliceDetails)
        )
      )
    ),
      Result(
        totalTax = leaseTaxDueRefunded + premTaxDueRefunded,
        resultHeading = refundHeading,
        resultHint = refundHint,
        npv = Some(npv),
        taxCalcs = Seq(
          CalculationDetails(
            taxType = TaxTypes.rent,
            calcType = CalcTypes.slice,
            detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
            bandHeading = Some("Rent bands (£)"),
            detailFooter = Some("SDLT due on the rent"),
            taxDue = leaseTaxDueRefunded,
            slices = Some(leaseSliceDetailsRefunded)
          ),
          CalculationDetails(
            taxType = TaxTypes.premium,
            calcType = CalcTypes.slice,
            detailHeading = Some("This is a breakdown of how the amount of SDLT on the premium was calculated"),
            bandHeading = Some("Premium bands (£)"),
            detailFooter = Some("SDLT due on the premium"),
            taxDue = premTaxDueRefunded,
            slices = Some(premSliceDetailsRefunded)
          )
        )
      )
    )
  }

  //April 16 results

  def leaseholdResidentialAddPropApr16OnwardsResult(
                                              leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                              premTaxDue:  Int, premSliceDetails:  Seq[SliceDetails],
                                              npv:         Int, resHintAmount: Option[String],
                                              afterMarch2021: Boolean = false): Result = {

    val (resultHeading, resultHint, leaseDetailHeading, premDetailHeading) = if(afterMarch2021) {
      (Some("Results of calculation based on SDLT rules for the effective date entered"),
        resHintAmount.map(value => s"The results are based on the answers you have provided and show that the higher rate on additional dwellings applies. " +
          s"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £$value."),
        Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
        Some("This is a breakdown of how the amount of SDLT on the premium was calculated")
         )
    } else {
      (Some("Results based on SDLT rules from 1 April 2016"),
        resHintAmount.map(value => s"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £$value."),
        Some("This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 1 April 2016"),
        Some("This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 1 April 2016") )
    }


      Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = resultHeading,
      resultHint = resultHint,
      npv = Some(npv),
        taxCalcs = Seq(
          CalculationDetails(
          taxType = TaxTypes.rent,
          calcType = CalcTypes.slice,
          detailHeading = leaseDetailHeading,
          bandHeading = Some("Rent bands (£)"),
          detailFooter = Some("SDLT due on the rent"),
          taxDue = leaseTaxDue,
          slices = Some(leaseSliceDetails)
          ),
          CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = premDetailHeading,
          bandHeading = Some("Premium bands (£)"),
          detailFooter = Some("SDLT due on the premium"),
          taxDue = premTaxDue,
          slices = Some(premSliceDetails)
          )
        )
      )
  }

  //Dec 14 results

  def leaseholdResidentialDec14OnwardsResult(
                                              leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                              premTaxDue:  Int, premSliceDetails:  Seq[SliceDetails],
                                              npv:         Int, asPreviousResult: Boolean = false, afterMarch2021: Boolean = false): Result = {
    val (resultHeading, resultHint, leaseDetailHeading, premDetailHeading) =
      (asPreviousResult, afterMarch2021) match {
        case (true, false) => (Some("Results based on SDLT rules before 1 April 2016"),
          Some("You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015."),
          Some("This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules before 1 April 2016"),
          Some("This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules before 1 April 2016"))
        case (true, true) => (Some("Result if you become eligible for a repayment of the higher rate on additional dwellings"),
          Some("If you dispose of your previous main residence within 3 years you may be eligible for a refund. " +
          "You must apply for any repayment within 12 months of disposing of your old main residence"),
          Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
          Some("This is a breakdown of how the amount of SDLT on the premium was calculated"))
        case (false, true) => (Some("Results of calculation based on SDLT rules for the effective date entered"),
          None,
          Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
          Some("This is a breakdown of how the amount of SDLT on the premium was calculated"))
        case _ => (Some("Results of calculation based on SDLT rules for the effective date entered"),
          None,
          Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
          Some("This is a breakdown of how the amount of SDLT on the premium was calculated"))
      }

    Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = resultHeading,
      resultHint = resultHint,
      npv = Some(npv),
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.rent,
          calcType = CalcTypes.slice,
          detailHeading = leaseDetailHeading,
          bandHeading = Some("Rent bands (£)"),
          detailFooter = Some("SDLT due on the rent"),
          taxDue = leaseTaxDue,
          slices = Some(leaseSliceDetails)
        ),
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = premDetailHeading,
          bandHeading = Some("Premium bands (£)"),
          detailFooter = Some("SDLT due on the premium"),
          taxDue = premTaxDue,
          slices = Some(premSliceDetails)
        )
      )
    )
  }

  //March 16 results

  def leaseholdNonResidentialMar16OnwardsResult(
                                                 leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                                 premTaxDue:  Int, premSliceDetails:  Seq[SliceDetails],
                                                 npv: Int): Result =
    Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = Some("Results based on SDLT rules from 17 March 2016"),
      resultHint = None,
      npv = Some(npv),
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.rent,
          calcType = CalcTypes.slice,
          detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated " +
            "based on the rules from 17 March 2016"),
          bandHeading = Some("Rent bands (£)"),
          detailFooter = Some("SDLT due on the rent"),
          taxDue = leaseTaxDue,
          slices = Some(leaseSliceDetails)
        ),
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = Some("This is a breakdown of how the amount of SDLT on the premium was calculated " +
            "based on the rules from 17 March 2016"),
          bandHeading = Some("Premium bands (£)"),
          detailFooter = Some("SDLT due on the premium"),
          taxDue = premTaxDue,
          slices = Some(premSliceDetails)
        )
      )
    )

  //March 12 results

  def leaseholdResidentialMar12toDec14Result(
                                              leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                              premTaxDue:  Int, premRate: Int,
                                              npv: Int): Result =
    Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
      resultHint = None,
      npv = Some(npv),
      taxCalcs = Seq(
        baseLeaseSliceDetails(leaseTaxDue, leaseSliceDetails),
        basePremSlabDetails(premTaxDue, premRate)
      )
    )

  def leaseholdNonResidentialMar12toMar16Result(
                                                 leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                                 premTaxDue:  Int, premRate: Int,
                                                 npv: Int): Result =
    Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
      resultHint = None,
      npv = Some(npv),
      taxCalcs = Seq(
        baseLeaseSliceDetails(leaseTaxDue, leaseSliceDetails),
        basePremSlabDetails(premTaxDue, premRate)
      )
    )

  def leaseholdNonResidentialMar12toMar16PrevResult(
                                                 leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails],
                                                 premTaxDue:  Int, premRate: Int,
                                                 npv: Int): Result =
    Result(
      totalTax = leaseTaxDue + premTaxDue,
      resultHeading = Some("Results based on SDLT rules before 17 March 2016"),
      resultHint = Some("You may be entitled to pay SDLT using the old rules if you exchanged contracts before 17 March 2016."),
      npv = Some(npv),
      taxCalcs = Seq(
        baseLeaseSliceDetails(leaseTaxDue, leaseSliceDetails).copy(
          detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated " +
            "based on the rules before 17 March 2016")
        ),
        basePremSlabDetails(premTaxDue, premRate)
      )
    )

  private def baseLeaseSliceDetails(leaseTaxDue: Int, leaseSliceDetails: Seq[SliceDetails]) = {
    CalculationDetails(
      taxType = TaxTypes.rent,
      calcType = CalcTypes.slice,
      detailHeading = Some("This is a breakdown of how the amount of SDLT on the rent was calculated"),
      bandHeading = Some("Rent bands (£)"),
      detailFooter = Some("SDLT due on the rent"),
      taxDue = leaseTaxDue,
      slices = Some(leaseSliceDetails)
    )
  }

  private def basePremSlabDetails(premTaxDue: Int, premRate: Int) = {
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
  }

}
