/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import java.time.LocalDate
import data.ResultText.{RESULT_HEADING_GENERIC, RESULT_HEADING_TAX_RELIEF, RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT}
import enums.sdltRebuild.AcquisitionRelief
import enums.{CalcTypes, HoldingTypes, PropertyTypes, TaxTypes}
import exceptions.RequiredValueNotDefinedException
import models.sdltRebuild.TaxReliefDetails
import models.{CalculationDetails, Result, _}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FreeholdCalculationServiceSpec extends PlaySpec with ScalaCheckPropertyChecks {

  val testFreeholdCalcService = new FreeholdCalculationService(new BaseCalculationService, new RefundEntitlementService)
  val july2021EffectiveDate: LocalDate = LocalDate.of(2021, 7, 1)
  val june2021EffectiveDate: LocalDate = LocalDate.of(2021, 6, 30)
  val april2021EffectiveDate: LocalDate = LocalDate.of(2021, 4, 1)
  val october2021EffectiveDate: LocalDate = LocalDate.of(2021, 10, 1)
  val march2021EffectiveDate: LocalDate = LocalDate.of(2021, 3, 31)
  val july2020EffectiveDate: LocalDate = LocalDate.of(2020, 7, 8)
  val dec2014EffectiveDate: LocalDate = LocalDate.of(2014, 12, 30)
  val march2012EffectiveDate: LocalDate = LocalDate.of(2012, 4, 1)
  val april2016EffectiveDate: LocalDate = LocalDate.of(2016, 4, 1)
  val jan2018EffectiveDate: LocalDate = LocalDate.of(2018, 1, 1)
  val sep2022EffectiveDate: LocalDate = LocalDate.of(2022, 9, 23)
  val oct2024EffectiveDate: LocalDate = LocalDate.of(2024, 10, 31)

  def july2020AddPropSlices(band1Tax: Int, band2Tax: Int, band3Tax: Int, band4Tax: Int): Seq[SliceDetails] = {
    Seq(
      SliceDetails(from = 0, to = Some(500000), rate = 3, taxDue = band1Tax),
      SliceDetails(from = 500000, to = Some(925000), rate = 8, taxDue = band2Tax),
      SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = band3Tax),
      SliceDetails(from = 1500000, to = None, rate = 15, taxDue = band4Tax)
    )
  }

  def july2020Slices(band1Tax: Int, band2Tax: Int, band3Tax: Int, band4Tax: Int): Seq[SliceDetails] = {
    Seq(
      SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = band1Tax),
      SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = band2Tax),
      SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = band3Tax),
      SliceDetails(from = 1500000, to = None, rate = 12, taxDue = band4Tax)
    )
  }

  def baseRequestIndividual(premium: BigDecimal, effectiveDate: LocalDate, nonUKResident: Option[Boolean] = None,
                            taxReliefDetails: Option[TaxReliefDetails] = None, linked : Boolean = false): Request = Request(
    holdingType = HoldingTypes.freehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = nonUKResident,
    premium = premium,
    highestRent = 0,
    leaseDetails = None,
    propertyDetails = None,
    relevantRentDetails = None,
    taxReliefDetails = taxReliefDetails,
    firstTimeBuyer = None,
    isLinked = linked
  )

  def baseRequestAddProps(premium: BigDecimal, effectiveDate: LocalDate): Request = Request(
    holdingType = HoldingTypes.freehold,
    propertyType = PropertyTypes.nonResidential,
    effectiveDate = effectiveDate,
    nonUKResident = None,
    premium = premium,
    highestRent = 0,
    leaseDetails = None,
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(true),
        replaceMainResidence = Some(false),
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    taxReliefDetails = None,
    firstTimeBuyer = None
  )

  def baseRequestCompany(premium: BigDecimal, effectiveDate: LocalDate, nonUKResident: Option[Boolean] = None,
                         taxReliefDetails: Option[TaxReliefDetails] = None, linked : Boolean = false): Request = Request(
    holdingType = HoldingTypes.freehold,
    propertyType = PropertyTypes.nonResidential,
    effectiveDate = effectiveDate,
    nonUKResident = nonUKResident,
    premium = premium,
    highestRent = 0,
    leaseDetails = None,
    propertyDetails = Some(
      PropertyDetails(
        individual = false,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    taxReliefDetails = taxReliefDetails,
    firstTimeBuyer = None,
    isLinked = linked
  )

  def baseRequestFTB(premium: BigDecimal, effectiveDate: LocalDate, nonUKResident: Option[Boolean] = None): Request = Request(
    holdingType = HoldingTypes.freehold,
    propertyType = PropertyTypes.residential,
    effectiveDate = effectiveDate,
    nonUKResident = nonUKResident,
    premium = premium,
    highestRent = 0,
    leaseDetails = None,
    propertyDetails = Some(
      PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = None,
        sharedOwnership = None,
        currentValue = None
      )
    ),
    relevantRentDetails = None,
    taxReliefDetails = None,
    firstTimeBuyer = Some(true)
  )

  def baseResult(taxDue: Int,
                 calcDeets: CalculationDetails,
                 resultHeading: Option[String] = Some(RESULT_HEADING_GENERIC),
                 resultHint: Option[String] = None,
                 npv: Option[Int] = None): Result = Result(
    totalTax = taxDue,
    resultHeading = resultHeading,
    resultHint = resultHint,
    npv = npv,
    taxCalcs = Seq(calcDeets)
  )

  def basePrevResult(taxDue: Int,
                     calcDeets: CalculationDetails,
                     resultHeading: Option[String] = Some(RESULT_HEADING_GENERIC),
                     resultHint: Option[String] = None,
                     npv: Option[Int] = None): Result = Result(
    totalTax = taxDue,
    resultHeading = resultHeading,
    resultHint = resultHint,
    npv = npv,
    taxCalcs = Seq(calcDeets)
  )

  def baseCalculationDetails(taxDue: Int, slices: Seq[SliceDetails],
                             detailHeading: Option[String] = None,
                             bandHeading: Option[String] = None,
                             detailFooter: Option[String] = None): CalculationDetails = CalculationDetails(
    taxType = TaxTypes.premium,
    calcType = CalcTypes.slice,
    detailHeading = detailHeading,
    bandHeading = bandHeading,
    detailFooter = detailFooter,
    taxDue = taxDue,
    slices = Some(slices)
  )

  def prevSliceCalculationDetails(taxDue: Int, slices: Seq[SliceDetails],
                                  prevDetailHeading: Option[String] = None,
                                  prevBandHeading: Option[String] = None,
                                  prevDetailFooter: Option[String] = None,
                                  rate: Option[Int] = None): CalculationDetails = CalculationDetails(
    taxType = TaxTypes.premium,
    calcType = CalcTypes.slice,
    detailHeading = prevDetailHeading,
    bandHeading = prevBandHeading,
    detailFooter = prevDetailFooter,
    taxDue = taxDue,
    rate = rate,
    slices = Some(slices)
  )

  def baseSlabCalculationDetails(taxDue: Int, rate: Int): CalculationDetails = CalculationDetails(
    taxType = TaxTypes.premium,
    calcType = CalcTypes.slab,
    detailHeading = None,
    bandHeading = None,
    detailFooter = None,
    taxDue = taxDue,
    rate = Some(rate),
    slices = None
  )

  def hint(message: String, amount: String) = s"$message $amount."

  "calculating freeholdResidentialApril21Onwards as an Individual" when {

    "all purchasers are UK resident" must {
      val resultHeading: Option[String] = Some("Results of calculation based on SDLT rules for the effective date entered")
      val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
      val bandHeading: Option[String] = Some("Purchase price bands (£)")
      val detailFooter: Option[String] = Some("Total SDLT due")

      "return 0 for purchase price of 125000" in {

        val resSlices = Seq(
          SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
          SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 0),
          SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
          SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
        )

        val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(125000, october2021EffectiveDate, Some(false)))
        res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter), resultHeading)
      }

      "return 0 for purchase price of 499999" in {
        val resSlices = Seq(
          SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
          SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 0),
          SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
          SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
        )

        val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(250020, october2021EffectiveDate, Some(false)))
        res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter), resultHeading)
      }

      "return 21250 for purchase price of 925000" in {
        val resSlices = Seq(
          SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
          SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 21250),
          SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
          SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
        )

        val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(925000, october2021EffectiveDate, Some(false)))
        res shouldBe baseResult(21250, baseCalculationDetails(21250, resSlices, detailHeading, bandHeading, detailFooter), resultHeading)
      }

      "return 78750 for purchase price of 1500000" in {
        val resSlices = Seq(
          SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
          SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 21250),
          SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 57500),
          SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
        )

        val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(1500000, october2021EffectiveDate, Some(false)))
        res shouldBe baseResult(78750, baseCalculationDetails(78750, resSlices, detailHeading, bandHeading, detailFooter), resultHeading)
      }

      "return 2500 for purchase price of 250000 with effective date after 30 June 2021" in {
        val resultHeading: Option[String] = Some("Results of calculation based on SDLT rules for the effective date entered")
        val resSlices = Seq(
          SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
          SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
          SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 0),
          SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
          SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
        )

        val res = testFreeholdCalcService.freeholdResidentialDec14Onwards(baseRequestIndividual(250000, july2021EffectiveDate, Some(false)))
        res shouldBe baseResult(2500, baseCalculationDetails(2500, resSlices, detailHeading, bandHeading, detailFooter), resultHeading)
      }
    }

  }

  "calculating freeholdResidentialApril21Onwards as a Company" when {

    "the company is uk resident" must {
      val resultHeading: Option[String] = Some("Results of calculation based on SDLT rules for the effective date entered")
      val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
      val bandHeading: Option[String] = Some("Purchase price bands (£)")
      val detailFooter: Option[String] = Some("Total SDLT due")

      "return 3750 for purchase price of 125000" in {
        val resSlices = july2020AddPropSlices(band1Tax = 3750, band2Tax = 0, band3Tax = 0, band4Tax = 0)
        val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(125000, october2021EffectiveDate, Some(false)))

        res shouldBe Seq(baseResult(3750,
          baseCalculationDetails(3750, resSlices, detailHeading, bandHeading, detailFooter),
          resultHeading = resultHeading))
      }

      "return 14970 for purchase price of 499000" in {
        val resSlices = july2020AddPropSlices(band1Tax = 14970, band2Tax = 0, band3Tax = 0, band4Tax = 0)
        val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(499000, october2021EffectiveDate, Some(false)))

        res shouldBe Seq(baseResult(14970,
          baseCalculationDetails(14970, resSlices, detailHeading, bandHeading, detailFooter),
          resultHeading = resultHeading))
      }

      "return 15000 for purchase price of 500000" in {
        val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 0, band3Tax = 0, band4Tax = 0)
        val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(500000, october2021EffectiveDate, Some(false)))

        res shouldBe Seq(baseResult(15000,
          baseCalculationDetails(15000, resSlices, detailHeading, bandHeading, detailFooter),
          resultHeading = resultHeading))
      }

      "return 15000 for purchase price of 500000 with final effective date" in {
        val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 0, band3Tax = 0, band4Tax = 0)
        val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(500000, june2021EffectiveDate, Some(false)))

        res shouldBe Seq(baseResult(15000,
          baseCalculationDetails(15000, resSlices, detailHeading, bandHeading, detailFooter),
          resultHeading = resultHeading))
      }

      "return 15002 for purchase price of 500025" in {
        val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 2, band3Tax = 0, band4Tax = 0)
        val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(500025, october2021EffectiveDate, Some(false)))

        res shouldBe Seq(baseResult(15002,
          baseCalculationDetails(15002, resSlices, detailHeading, bandHeading, detailFooter),
          resultHeading = resultHeading))
      }

      "return 15080 for purchase price of 501000" in {
        val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 80, band3Tax = 0, band4Tax = 0)
        val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(501000, october2021EffectiveDate, Some(false)))

        res shouldBe Seq(baseResult(15080,
          baseCalculationDetails(15080, resSlices, detailHeading, bandHeading, detailFooter),
          resultHeading = resultHeading))
      }

      "return 49000 for purchase price of 925000" in {
        val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 0, band4Tax = 0)
        val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(925000, october2021EffectiveDate, Some(false)))

        res shouldBe Seq(baseResult(49000,
          baseCalculationDetails(49000, resSlices, detailHeading, bandHeading, detailFooter),
          resultHeading = resultHeading))
      }

      "return 49003 for purchase price of 925025" in {
        val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 3, band4Tax = 0)
        val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(925025, october2021EffectiveDate, Some(false)))

        res shouldBe Seq(baseResult(49003,
          baseCalculationDetails(49003, resSlices, detailHeading, bandHeading, detailFooter),
          resultHeading = resultHeading))
      }

      "return 123750 for purchase price of 1500000" in {
        val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 74750, band4Tax = 0)
        val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(1500000, october2021EffectiveDate, Some(false)))

        res shouldBe Seq(baseResult(123750,
          baseCalculationDetails(123750, resSlices, detailHeading, bandHeading, detailFooter),
          resultHeading = resultHeading))
      }

      "return 198750 for purchase price of 2000000" in {
        val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 74750, band4Tax = 75000)
        val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(2000000, october2021EffectiveDate, Some(false)))

        res shouldBe Seq(baseResult(198750,
          baseCalculationDetails(198750, resSlices, detailHeading, bandHeading, detailFooter),
          resultHeading = resultHeading))
      }
    }
  }

  "calculating freeholdResidentialAddPropJuly20Onwards" must {

    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")

    val addPropResultHeading: Option[String] = Some("Results of calculation based on SDLT rules for the effective date entered")
    val currentResultHeading: Option[String] = Some("Result if you become eligible for a repayment of the higher rate on additional dwellings")
    val currentResultHint: Option[String] = Some("If you dispose of your previous main residence within 3 years you may be eligible for a refund." +
      " You must apply for any repayment within 12 months of disposing of your old main residence.")

    val hintMessage: String = "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies." +
      s" If you dispose of your previous main residence within 3 years you may be eligible for a refund of"

    "return add props: 1200, current: 0 for purchase price of 40000" in {
      val addPropsSlices = july2020AddPropSlices(band1Tax = 1200, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val currentSlices = july2020Slices(band1Tax = 0, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestAddProps(40000, july2020EffectiveDate))

      res shouldBe Seq(
        baseResult(1200,
          baseCalculationDetails(1200, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£1,200"))),

        basePrevResult(0,
          prevSliceCalculationDetails(0, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint)
      )
    }

    "return add props: 14970, current: 0 for purchase price of 499000" in {
      val addPropsSlices = july2020AddPropSlices(band1Tax = 14970, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val currentSlices = july2020Slices(band1Tax = 0, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestAddProps(499000, july2020EffectiveDate))

      res shouldBe Seq(
        baseResult(14970,
          baseCalculationDetails(14970, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£14,970"))),

        basePrevResult(0,
          prevSliceCalculationDetails(0, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint
        )
      )
    }

    "return add props: 15000, current: 0 for purchase price of 500000" in {
      val addPropsSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val currentSlices = july2020Slices(band1Tax = 0, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestAddProps(500000, july2020EffectiveDate))

      res shouldBe Seq(
        baseResult(15000,
          baseCalculationDetails(15000, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£15,000"))),

        basePrevResult(0,
          prevSliceCalculationDetails(0, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint
        )
      )
    }

    "return add props: 15000, current: 0 for purchase price of 500000 with final effective date" in {
      val addPropsSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val currentSlices = july2020Slices(band1Tax = 0, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestAddProps(500000, march2021EffectiveDate))

      res shouldBe Seq(
        baseResult(15000,
          baseCalculationDetails(15000, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£15,000"))),

        basePrevResult(0,
          prevSliceCalculationDetails(0, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint
        )
      )
    }

    "return add props: 15080, current: 50 for purchase price of 501000" in {
      val addPropsSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 80, band3Tax = 0, band4Tax = 0)
      val currentSlices = july2020Slices(band1Tax = 0, band2Tax = 50, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestAddProps(501000, july2020EffectiveDate))

      res shouldBe Seq(
        baseResult(15080,
          baseCalculationDetails(15080, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£15,030"))),

        basePrevResult(50,
          prevSliceCalculationDetails(50, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint
        )
      )
    }

    "return add props: 15002, current: 1 for purchase price of 500025" in {
      val addPropsSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 2, band3Tax = 0, band4Tax = 0)
      val currentSlices = july2020Slices(band1Tax = 0, band2Tax = 1, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestAddProps(500025, july2020EffectiveDate))

      res shouldBe Seq(
        baseResult(15002,
          baseCalculationDetails(15002, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£15,001"))),

        basePrevResult(1,
          prevSliceCalculationDetails(1, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint)
      )
    }

    "return add props: 49000, current: 21250 for purchase price of 925000" in {
      val addPropsSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 0, band4Tax = 0)
      val currentSlices = july2020Slices(band1Tax = 0, band2Tax = 21250, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestAddProps(925000, july2020EffectiveDate))

      res shouldBe Seq(
        baseResult(49000,
          baseCalculationDetails(49000, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£27,750"))),

        basePrevResult(21250,
          prevSliceCalculationDetails(21250, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint)
      )
    }

    "return add props: 49003, current: 21252 for purchase price of 925025" in {
      val addPropsSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 3, band4Tax = 0)
      val currentSlices = july2020Slices(band1Tax = 0, band2Tax = 21250, band3Tax = 2, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestAddProps(925025, july2020EffectiveDate))

      res shouldBe Seq(
        baseResult(49003,
          baseCalculationDetails(49003, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£27,751"))),

        basePrevResult(21252,
          prevSliceCalculationDetails(21252, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint)
      )
    }

    "return add props: 123750, current: 78750 for purchase price of 1500000" in {
      val addPropsSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 74750, band4Tax = 0)
      val currentSlices = july2020Slices(band1Tax = 0, band2Tax = 21250, band3Tax = 57500, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestAddProps(1500000, july2020EffectiveDate))

      res shouldBe Seq(
        baseResult(123750,
          baseCalculationDetails(123750, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£45,000"))),

        basePrevResult(78750,
          prevSliceCalculationDetails(78750, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint)
      )
    }

    "return add props: 198750, current: 14970 for purchase price of 2000000" in {
      val addPropsSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 74750, band4Tax = 75000)
      val currentSlices = july2020Slices(band1Tax = 0, band2Tax = 21250, band3Tax = 57500, band4Tax = 60000)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestAddProps(2000000, july2020EffectiveDate))

      res shouldBe Seq(
        baseResult(198750,
          baseCalculationDetails(198750, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£60,000"))),

        basePrevResult(138750,
          prevSliceCalculationDetails(138750, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint)
      )
    }
  }

  "calculating freeholdResidentialAddPropOct24BeforeApril25" must {

    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")

    val addPropResultHeading: Option[String] = Some("Results of calculation based on SDLT rules for the effective date entered")
    val currentResultHeading: Option[String] = Some("Result if you become eligible for a repayment of the higher rate on additional dwellings")
    val currentResultHint: Option[String] = Some("If you dispose of your previous main residence within 3 years you may be eligible for a refund." +
      " You must apply for any repayment within 12 months of disposing of your old main residence.")

    val hintMessage: String = "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies." +
      s" If you dispose of your previous main residence within 3 years you may be eligible for a refund of"

    val addPropsSlices = Seq(
      SliceDetails(from = 0, to = Some(250000), rate = 5, taxDue = 12500),
      SliceDetails(from = 250000, to = Some(925000), rate = 10, taxDue = 67500),
      SliceDetails(from = 925000, to = Some(1500000), rate = 15, taxDue = 86250),
      SliceDetails(from = 1500000, to = None, rate = 17, taxDue = 17000)
    )
    val currentSlices = Seq(
      SliceDetails(from = 0, to = Some(250000), rate = 0, taxDue = 0),
      SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
      SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 57500),
      SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 12000)
    )
    "return add props: 183250, current: 103250 for purchase price of 1600000" in {
      val res = testFreeholdCalcService.freeholdResidentialAddPropOct24BeforeApril25(baseRequestAddProps(1600000, oct2024EffectiveDate))

      res shouldBe Seq(
        baseResult(183250,
          baseCalculationDetails(183250, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£80,000"))),

        basePrevResult(103250,
          prevSliceCalculationDetails(103250, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint)
      )
    }
  }

  "calculating freeholdResidentialAddPropNonUKResOct24BeforeApril25" must {

    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")

    val addPropResultHeading: Option[String] = Some("Results of calculation based on SDLT rules for the effective date entered")
    val currentResultHeading: Option[String] = Some("Result if you become eligible for a repayment of the higher rate on additional dwellings")
    val currentResultHint: Option[String] = Some("If you dispose of your previous main residence within 3 years you may be eligible for a refund." +
      " You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.")

    val hintMessage: String = "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies." +
      s" If you dispose of your previous main residence within 3 years you may be eligible for a refund of"
    val nonRessidentRefund: String = ".<br /><br />You may also be eligible for a refund of the non-resident rate"

    val addPropsSlices = Seq(
      SliceDetails(from = 0, to = Some(250000), rate = 7, taxDue = 17500),
      SliceDetails(from = 250000, to = Some(925000), rate = 12, taxDue = 81000),
      SliceDetails(from = 925000, to = Some(1500000), rate = 17, taxDue = 12750),
      SliceDetails(from = 1500000, to = None, rate = 19, taxDue = 0)
    )
    val currentSlices = Seq(
      SliceDetails(from = 0, to = Some(250000), rate = 2, taxDue = 5000),
      SliceDetails(from = 250000, to = Some(925000), rate = 7, taxDue = 47250),
      SliceDetails(from = 925000, to = Some(1500000), rate = 12, taxDue = 9000),
      SliceDetails(from = 1500000, to = None, rate = 14, taxDue = 0)
    )
    "return add props: 111250, current: 61250 for purchase price of 1000000" in {
      val res = testFreeholdCalcService.freeholdResidentialAddPropNonUKResOct24BeforeApril25(baseRequestAddProps(1000000, oct2024EffectiveDate))

      res shouldBe Seq(
        baseResult(111250,
          baseCalculationDetails(111250, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£50,000"+nonRessidentRefund))),

        basePrevResult(61250,
          prevSliceCalculationDetails(61250, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint)
      )
    }
  }

  "calculating freeholdResidentialAddPropApril25Onwards" must {

    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")

    val addPropResultHeading: Option[String] = Some("Results of calculation based on SDLT rules for the effective date entered")
    val currentResultHeading: Option[String] = Some("Result if you become eligible for a repayment of the higher rate on additional dwellings")
    val currentResultHint: Option[String] = Some("If you dispose of your previous main residence within 3 years you may be eligible for a refund." +
      " You must apply for any repayment within 12 months of disposing of your old main residence.")

    val hintMessage: String = "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies." +
      s" If you dispose of your previous main residence within 3 years you may be eligible for a refund of"

    val addPropsSlices = Seq(
      SliceDetails(from = 0,        to = Some(125000),   rate = 5, taxDue = 6250),
      SliceDetails(from = 125000,   to = Some(250000),   rate = 7, taxDue = 8750),
      SliceDetails(from = 250000,   to = Some(925000),   rate = 10, taxDue = 67500),
      SliceDetails(from = 925000,   to = Some(1500000),  rate = 15, taxDue = 86250),
      SliceDetails(from = 1500000,  to = None,           rate = 17, taxDue = 17000)
    )

    val currentSlices = Seq(
      SliceDetails(from = 0,       to = Some(125000),  rate = 0, taxDue = 0),
      SliceDetails(from = 125000,  to = Some(250000),  rate = 2, taxDue = 2500),
      SliceDetails(from = 250000,  to = Some(925000),  rate = 5, taxDue = 33750),
      SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 57500),
      SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 12000)
    )
    "return add props: 185750, current: 105750 for purchase price of 1600000" in {
      val res = testFreeholdCalcService.freeholdResidentialAddPropApril25Onwards(baseRequestAddProps(1600000, oct2024EffectiveDate))

      res shouldBe Seq(
        baseResult(185750,
          baseCalculationDetails(185750, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£80,000"))),

        basePrevResult(105750,
          prevSliceCalculationDetails(105750, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint)
      )
    }
  }

  "calculating freeholdResidentialAddPropNonUKResApril25Onwards" must {

    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")

    val addPropResultHeading: Option[String] = Some("Results of calculation based on SDLT rules for the effective date entered")
    val currentResultHeading: Option[String] = Some("Result if you become eligible for a repayment of the higher rate on additional dwellings")
    val currentResultHint: Option[String] = Some("If you dispose of your previous main residence within 3 years you may be eligible for a refund." +
      " You must apply for any repayment within 12 months of disposing of your old main residence.<br /><br />You may also be eligible for a refund of the non-resident rate.")

    val hintMessage: String = "The results are based on the answers you have provided and show that the higher rate on additional dwellings applies." +
      s" If you dispose of your previous main residence within 3 years you may be eligible for a refund of"
    val nonRessidentRefund: String = ".<br /><br />You may also be eligible for a refund of the non-resident rate"

    val addPropsSlices = Seq(
      SliceDetails(from = 0,        to = Some(125000),   rate = 7, taxDue = 8750),
      SliceDetails(from = 125000,   to = Some(250000),   rate = 9, taxDue = 11250),
      SliceDetails(from = 250000,   to = Some(925000),   rate = 12, taxDue = 81000),
      SliceDetails(from = 925000,   to = Some(1500000),  rate = 17, taxDue = 12750),
      SliceDetails(from = 1500000,  to = None,           rate = 19, taxDue = 0)
    )
    val currentSlices = Seq(
      SliceDetails(from = 0,       to = Some(125000),  rate = 2, taxDue = 2500),
      SliceDetails(from = 125000,  to = Some(250000),  rate = 4, taxDue = 5000),
      SliceDetails(from = 250000,  to = Some(925000),  rate = 7, taxDue = 47250),
      SliceDetails(from = 925000,  to = Some(1500000), rate = 12, taxDue = 9000),
      SliceDetails(from = 1500000, to = None,          rate = 14, taxDue = 0)
    )
    "return add props: 113750, current: 63750 for purchase price of 1000000" in {
      val res = testFreeholdCalcService.freeholdResidentialAddPropNonUKResApril25Onwards(baseRequestAddProps(1000000, oct2024EffectiveDate))

      res shouldBe Seq(
        baseResult(113750,
          baseCalculationDetails(113750, addPropsSlices, detailHeading, bandHeading, detailFooter),
          addPropResultHeading,
          Some(hint(hintMessage, "£50,000"+nonRessidentRefund))),

        basePrevResult(63750,
          prevSliceCalculationDetails(63750, currentSlices, detailHeading, bandHeading, detailFooter),
          currentResultHeading,
          currentResultHint)
      )
    }
  }

  "calculating freeholdResidentialJuly20Onwards as an Individual" must {

    val resultHeading: Option[String] = Some("Results of calculation based on SDLT rules for the effective date entered")
    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")

    "return 0 for purchase price of 125000" in {
      val resSlices = july2020Slices(band1Tax = 0, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(125000, july2020EffectiveDate))

      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }

    "return 0 for purchase price of 499999" in {
      val resSlices = july2020Slices(band1Tax = 0, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(499999, july2020EffectiveDate))

      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }

    "return 0 for purchase price of 499000" in {
      val resSlices = july2020Slices(band1Tax = 0, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(499000, july2020EffectiveDate))

      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }

    "return 0 for purchase price of 500000" in {
      val resSlices = july2020Slices(band1Tax = 0, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(500000, july2020EffectiveDate))

      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }

    "return 0 for purchase price of 500000 with final effective date" in {
      val resSlices = july2020Slices(band1Tax = 0, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(500000, june2021EffectiveDate))

      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }

    "return 50 for purchase price of 501000" in {
      val resSlices = july2020Slices(band1Tax = 0, band2Tax = 50, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(501000, july2020EffectiveDate))

      res shouldBe baseResult(50, baseCalculationDetails(50, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }

    "return 1 for purchase price of 500025" in {
      val resSlices = july2020Slices(band1Tax = 0, band2Tax = 1, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(500025, july2020EffectiveDate))

      res shouldBe baseResult(1, baseCalculationDetails(1, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }

    "return 21250 for purchase price of 925000" in {
      val resSlices = july2020Slices(band1Tax = 0, band2Tax = 21250, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(925000, july2020EffectiveDate))

      res shouldBe baseResult(21250, baseCalculationDetails(21250, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }

    "return 21252 for purchase price of 925025" in {
      val resSlices = july2020Slices(band1Tax = 0, band2Tax = 21250, band3Tax = 2, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(925025, july2020EffectiveDate))

      res shouldBe baseResult(21252, baseCalculationDetails(21252, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }

    "return 78750 for purchase price of 1500000" in {
      val resSlices = july2020Slices(band1Tax = 0, band2Tax = 21250, band3Tax = 57500, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(1500000, july2020EffectiveDate))

      res shouldBe baseResult(78750, baseCalculationDetails(78750, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }

    "return 138750 for purchase price of 2000000" in {
      val resSlices = july2020Slices(band1Tax = 0, band2Tax = 21250, band3Tax = 57500, band4Tax = 60000)
      val res = testFreeholdCalcService.freeholdResidentialJuly20Onwards(baseRequestIndividual(2000000, july2020EffectiveDate))

      res shouldBe baseResult(138750, baseCalculationDetails(138750, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }
  }

  "calculating freeholdResidentialJuly20Onwards as a Company" must {

    val resultHeading: Option[String] = Some("Results of calculation based on SDLT rules for the effective date entered")
    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")

    "return 3750 for purchase price of 125000" in {
      val resSlices = july2020AddPropSlices(band1Tax = 3750, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(125000, july2020EffectiveDate))

      res shouldBe Seq(baseResult(3750,
        baseCalculationDetails(3750, resSlices, detailHeading, bandHeading, detailFooter),
        resultHeading = resultHeading))
    }

    "return 14970 for purchase price of 499000" in {
      val resSlices = july2020AddPropSlices(band1Tax = 14970, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(499000, july2020EffectiveDate))

      res shouldBe Seq(baseResult(14970,
        baseCalculationDetails(14970, resSlices, detailHeading, bandHeading, detailFooter),
        resultHeading = resultHeading))
    }

    "return 15000 for purchase price of 500000" in {
      val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(500000, july2020EffectiveDate))

      res shouldBe Seq(baseResult(15000,
        baseCalculationDetails(15000, resSlices, detailHeading, bandHeading, detailFooter),
        resultHeading = resultHeading))
    }

    "return 15000 for purchase price of 500000 with final effective date" in {
      val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 0, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(500000, march2021EffectiveDate))

      res shouldBe Seq(baseResult(15000,
        baseCalculationDetails(15000, resSlices, detailHeading, bandHeading, detailFooter),
        resultHeading = resultHeading))
    }

    "return 15002 for purchase price of 500025" in {
      val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 2, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(500025, july2020EffectiveDate))

      res shouldBe Seq(baseResult(15002,
        baseCalculationDetails(15002, resSlices, detailHeading, bandHeading, detailFooter),
        resultHeading = resultHeading))
    }

    "return 15080 for purchase price of 501000" in {
      val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 80, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(501000, july2020EffectiveDate))

      res shouldBe Seq(baseResult(15080,
        baseCalculationDetails(15080, resSlices, detailHeading, bandHeading, detailFooter),
        resultHeading = resultHeading))
    }

    "return 49000 for purchase price of 925000" in {
      val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 0, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(925000, july2020EffectiveDate))

      res shouldBe Seq(baseResult(49000,
        baseCalculationDetails(49000, resSlices, detailHeading, bandHeading, detailFooter),
        resultHeading = resultHeading))
    }

    "return 49003 for purchase price of 925025" in {
      val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 3, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(925025, july2020EffectiveDate))

      res shouldBe Seq(baseResult(49003,
        baseCalculationDetails(49003, resSlices, detailHeading, bandHeading, detailFooter),
        resultHeading = resultHeading))
    }

    "return 123750 for purchase price of 1500000" in {
      val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 74750, band4Tax = 0)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(1500000, july2020EffectiveDate))

      res shouldBe Seq(baseResult(123750,
        baseCalculationDetails(123750, resSlices, detailHeading, bandHeading, detailFooter),
        resultHeading = resultHeading))
    }

    "return 198750 for purchase price of 2000000" in {
      val resSlices = july2020AddPropSlices(band1Tax = 15000, band2Tax = 34000, band3Tax = 74750, band4Tax = 75000)
      val res = testFreeholdCalcService.freeholdResidentialAddPropJuly20Onwards(baseRequestCompany(2000000, july2020EffectiveDate))

      res shouldBe Seq(baseResult(198750,
        baseCalculationDetails(198750, resSlices, detailHeading, bandHeading, detailFooter),
        resultHeading = resultHeading))
    }
  }

  "calculating freeholdResidentialNov17OnwardsFTB" must {

    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")

    val MAX_PREMIUM_FTB = 500000

    "return 0 for purchase price of 299999" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )
      val res = testFreeholdCalcService.freeholdResidentialNov17OnwardsFTB(baseRequestFTB(299999, jan2018EffectiveDate))

      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter))
    }


    "return 0 for purchase price of 300000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )
      val res = testFreeholdCalcService.freeholdResidentialNov17OnwardsFTB(baseRequestFTB(300000, jan2018EffectiveDate))

      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 0 for purchase price of 300000 with effective date after 31 July 2021" in {
      val resultHeading: Option[String] = Some("Results of calculation based on SDLT rules for the effective date entered")
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )
      val res = testFreeholdCalcService.freeholdResidentialNov17OnwardsFTB(baseRequestFTB(300000, october2021EffectiveDate))

      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading)
    }

    "return 1 for purchase price of 300025" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 1)
      )
      val res = testFreeholdCalcService.freeholdResidentialNov17OnwardsFTB(baseRequestFTB(300025, jan2018EffectiveDate))

      res shouldBe baseResult(1, baseCalculationDetails(1, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 9999 for purchase price of 499999" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 9999)
      )
      val res = testFreeholdCalcService.freeholdResidentialNov17OnwardsFTB(baseRequestFTB(499999, jan2018EffectiveDate))

      res shouldBe baseResult(9999, baseCalculationDetails(9999, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 10000 for purchase price of 500000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 10000)
      )
      val res = testFreeholdCalcService.freeholdResidentialNov17OnwardsFTB(baseRequestFTB(500000, jan2018EffectiveDate))

      res shouldBe baseResult(10000, baseCalculationDetails(10000, resSlices, detailHeading, bandHeading, detailFooter))
    }
  }

  "calculating freeholdResidentialSept22OnwardsFTB" must {

    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")

    val MAX_PREMIUM_FTB = 625000

    "return 0 for purchase price of 424000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 0, taxDue = 0),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )
      val res = testFreeholdCalcService.freeholdResidentialSept22OnwardsFTB(baseRequestFTB(424000, sep2022EffectiveDate))

      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter))
    }


    "return 0 for purchase price of 425000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 0, taxDue = 0),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )
      val res = testFreeholdCalcService.freeholdResidentialSept22OnwardsFTB(baseRequestFTB(425000, sep2022EffectiveDate))

      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 1 for purchase price of 425025" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 0, taxDue = 0),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 1)
      )
      val res = testFreeholdCalcService.freeholdResidentialSept22OnwardsFTB(baseRequestFTB(425025, sep2022EffectiveDate))

      res shouldBe baseResult(1, baseCalculationDetails(1, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 9950 for purchase price of 624000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 0, taxDue = 0),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 9950)
      )
      val res = testFreeholdCalcService.freeholdResidentialSept22OnwardsFTB(baseRequestFTB(624000, sep2022EffectiveDate))

      res shouldBe baseResult(9950, baseCalculationDetails(9950, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 10000 for purchase price of 625000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 0, taxDue = 0),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 10000)
      )
      val res = testFreeholdCalcService.freeholdResidentialSept22OnwardsFTB(baseRequestFTB(625000, sep2022EffectiveDate))

      res shouldBe baseResult(10000, baseCalculationDetails(10000, resSlices, detailHeading, bandHeading, detailFooter))
    }
  }

  "calculating freeholdResidentialSept22OnwardsFTBNonUKRes" must {

    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")
    val nonUkHint: Option[String] = Some("The results are based on the answers you have provided and show that the non-resident rate of SDLT applies.")
    val repaymentResult: Option[String] = Some("Result if you become eligible for a repayment of the non-resident rate of SDLT")
    val eligibility: Option[String] = Some("If you become resident in the UK for SDLT purposes within 12 months of your purchase, you may be eligible for a refund. You must apply for any repayment within 2 years of the purchase date.")

    val MAX_PREMIUM_FTB = 625000

    "return 8480 or 0 if eligible for a refund, for purchase price of 424000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 2, taxDue = 8480),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 0)
      )
      val resSliceRefunded = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 0, taxDue = 0),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )
      val res = testFreeholdCalcService.freeholdResidentialSept22OnwardsFTBNonUKRes(baseRequestFTB(424000, sep2022EffectiveDate))

      res shouldBe Seq(
        baseResult(8480, baseCalculationDetails(8480, resSlices, detailHeading, bandHeading, detailFooter), resultHint = nonUkHint),
        baseResult(0, baseCalculationDetails(0, resSliceRefunded, detailHeading, bandHeading, detailFooter), repaymentResult, eligibility)
      )
    }


    "return 8500 or 0 if eligible for a refund, for purchase price of 425000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 2, taxDue = 8500),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 0)
      )
      val resSliceRefunded = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 0, taxDue = 0),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )
      val res = testFreeholdCalcService.freeholdResidentialSept22OnwardsFTBNonUKRes(baseRequestFTB(425000, sep2022EffectiveDate))

      res shouldBe Seq(
        baseResult(8500, baseCalculationDetails(8500, resSlices, detailHeading, bandHeading, detailFooter), resultHint = nonUkHint),
        baseResult(0, baseCalculationDetails(0, resSliceRefunded, detailHeading, bandHeading, detailFooter), repaymentResult, eligibility)
      )
    }

    "return 8501 or 1 if eligible for a refund, for purchase price of 425025" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 2, taxDue = 8500),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 1)
      )
      val resSliceRefunded = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 0, taxDue = 0),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 1)
      )
      val res = testFreeholdCalcService.freeholdResidentialSept22OnwardsFTBNonUKRes(baseRequestFTB(425025, sep2022EffectiveDate))

      res shouldBe Seq(
        baseResult(8501, baseCalculationDetails(8501, resSlices, detailHeading, bandHeading, detailFooter), resultHint = nonUkHint),
        baseResult(1, baseCalculationDetails(1, resSliceRefunded, detailHeading, bandHeading, detailFooter), repaymentResult, eligibility)
      )
    }

    "return 22430 or 9950 if eligible for a refund, for purchase price of 624000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 2, taxDue = 8500),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 13930)
      )
      val resSliceRefunded = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 0, taxDue = 0),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 9950)
      )
      val res = testFreeholdCalcService.freeholdResidentialSept22OnwardsFTBNonUKRes(baseRequestFTB(624000, sep2022EffectiveDate))

      res shouldBe Seq(
        baseResult(22430, baseCalculationDetails(22430, resSlices, detailHeading, bandHeading, detailFooter), resultHint = nonUkHint),
        baseResult(9950, baseCalculationDetails(9950, resSliceRefunded, detailHeading, bandHeading, detailFooter), repaymentResult, eligibility)
      )
    }

    "return 22500 or 10000 if eligible for a refund, for purchase price of 625000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 2, taxDue = 8500),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 14000)
      )
      val resSliceRefunded = Seq(
        SliceDetails(from = 0, to = Some(425000), rate = 0, taxDue = 0),
        SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 10000)
      )
      val res = testFreeholdCalcService.freeholdResidentialSept22OnwardsFTBNonUKRes(baseRequestFTB(625000, sep2022EffectiveDate))

      res shouldBe Seq(
        baseResult(22500, baseCalculationDetails(22500, resSlices, detailHeading, bandHeading, detailFooter), resultHint = nonUkHint),
        baseResult(10000, baseCalculationDetails(10000, resSliceRefunded, detailHeading, bandHeading, detailFooter), repaymentResult, eligibility)
      )
    }
  }

  "calculating freeholdResidentialDec14Onwards" must {

    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")

    "return 0 for purchase price of 125000" in {

      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 0),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialDec14Onwards(baseRequestIndividual(125000, dec2014EffectiveDate))
      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 1 for purchase price of 125050" in {

      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 1),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialDec14Onwards(baseRequestIndividual(125050, dec2014EffectiveDate))
      res shouldBe baseResult(1, baseCalculationDetails(1, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 2500 for purchase price of 250000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialDec14Onwards(baseRequestIndividual(250000, dec2014EffectiveDate))
      res shouldBe baseResult(2500, baseCalculationDetails(2500, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 2501 for purchase price of 250020" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 1),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialDec14Onwards(baseRequestIndividual(250020, dec2014EffectiveDate))
      res shouldBe baseResult(2501, baseCalculationDetails(2501, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 36250 for purchase price of 925000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialDec14Onwards(baseRequestIndividual(925000, dec2014EffectiveDate))
      res shouldBe baseResult(36250, baseCalculationDetails(36250, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 36251 for purchase price of 925010" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 1),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialDec14Onwards(baseRequestIndividual(925010, dec2014EffectiveDate))
      res shouldBe baseResult(36251, baseCalculationDetails(36251, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 93750 for purchase price of 1500000" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialDec14Onwards(baseRequestIndividual(1500000, dec2014EffectiveDate))
      res shouldBe baseResult(93750, baseCalculationDetails(93750, resSlices, detailHeading, bandHeading, detailFooter))
    }

    "return 93751 for purchase price of 1500009" in {
      val resSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 1)
      )

      val res = testFreeholdCalcService.freeholdResidentialDec14Onwards(baseRequestIndividual(1500009, dec2014EffectiveDate))
      res shouldBe baseResult(93751, baseCalculationDetails(93751, resSlices, detailHeading, bandHeading, detailFooter))

    }
  }

  "calculating freeholdResidentialMar12toDec14" must {

    "return 0 for purchase price of 125000" in {
      val res = testFreeholdCalcService.freeholdResidentialMar12toDec14(baseRequestIndividual(125000, march2012EffectiveDate))
      res shouldBe baseResult(0, baseSlabCalculationDetails(0, 0))
    }

    "return 1250 for purchase price of 125001" in {
      val res = testFreeholdCalcService.freeholdResidentialMar12toDec14(baseRequestIndividual(125001, march2012EffectiveDate))
      res shouldBe baseResult(1250, baseSlabCalculationDetails(1250, 1))
    }

    "return 2500 for purchase price of 250000" in {
      val res = testFreeholdCalcService.freeholdResidentialMar12toDec14(baseRequestIndividual(250000, march2012EffectiveDate))
      res shouldBe baseResult(2500, baseSlabCalculationDetails(2500, 1))
    }

    "return 7500 for purchase price of 250001" in {
      val res = testFreeholdCalcService.freeholdResidentialMar12toDec14(baseRequestIndividual(250001, march2012EffectiveDate))
      res shouldBe baseResult(7500, baseSlabCalculationDetails(7500, 3))
    }

    "return 15000 for purchase price of 500000" in {
      val res = testFreeholdCalcService.freeholdResidentialMar12toDec14(baseRequestIndividual(500000, march2012EffectiveDate))
      res shouldBe baseResult(15000, baseSlabCalculationDetails(15000, 3))

    }

    "return 20000 for purchase price of 500001" in {
      val res = testFreeholdCalcService.freeholdResidentialMar12toDec14(baseRequestIndividual(500001, march2012EffectiveDate))
      res shouldBe baseResult(20000, baseSlabCalculationDetails(20000, 4))
    }

    "return 40000 for purchase price of 1000000" in {
      val res = testFreeholdCalcService.freeholdResidentialMar12toDec14(baseRequestIndividual(1000000, march2012EffectiveDate))
      res shouldBe baseResult(40000, baseSlabCalculationDetails(40000, 4))
    }

    "return 50000 for purchase price of 1000001" in {
      val res = testFreeholdCalcService.freeholdResidentialMar12toDec14(baseRequestIndividual(1000001, march2012EffectiveDate))
      res shouldBe baseResult(50000, baseSlabCalculationDetails(50000, 5))
    }

    "return 100000 for purchase price of 2000000" in {
      val res = testFreeholdCalcService.freeholdResidentialMar12toDec14(baseRequestIndividual(2000000, march2012EffectiveDate))
      res shouldBe baseResult(100000, baseSlabCalculationDetails(100000, 5))
    }

    "return 140000 for purchase price of 2000001" in {
      val res = testFreeholdCalcService.freeholdResidentialMar12toDec14(baseRequestIndividual(2000001, march2012EffectiveDate))
      res shouldBe baseResult(140000, baseSlabCalculationDetails(140000, 7))
    }
  }

  "calculating freeholdNonResidentialMar16Onwards" must {
    val resultHeading: Option[String] = Some("Results based on SDLT rules from 17 March 2016")
    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated " +
      "based on the rules from 17 March 2016")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")
    val prevResultHeading: Option[String] = Some("Results based on SDLT rules before 17 March 2016")
    val prevResultHint: Option[String] = Some("You may be entitled to pay SDLT using the old rules if you exchanged contracts before " +
      "17 March 2016.")

    "return current: 0, prev: 0 for purchase price of 150000" in {
      val slices = Seq(
        SliceDetails(from = 0, to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 0),
        SliceDetails(from = 250000, to = None, rate = 5, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdNonResidentialMar16Onwards(baseRequestIndividual(150000, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(0, baseCalculationDetails(0, slices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading),
        basePrevResult(0, baseSlabCalculationDetails(0, 0), resultHeading = prevResultHeading, resultHint = prevResultHint)
      )
    }

    "return current: 2, prev: 1501 for purchase price of 150100" in {
      val slices = Seq(
        SliceDetails(from = 0, to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 2),
        SliceDetails(from = 250000, to = None, rate = 5, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdNonResidentialMar16Onwards(baseRequestIndividual(150100, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(2, baseCalculationDetails(2, slices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading),
        basePrevResult(1501, baseSlabCalculationDetails(1501, 1), resultHeading = prevResultHeading, resultHint = prevResultHint)
      )
    }

    "return current: 2000, prev: 2500 for purchase price of 250000" in {
      val slices = Seq(
        SliceDetails(from = 0, to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 2000),
        SliceDetails(from = 250000, to = None, rate = 5, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdNonResidentialMar16Onwards(baseRequestIndividual(250000, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(2000, baseCalculationDetails(2000, slices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading),
        basePrevResult(2500, baseSlabCalculationDetails(2500, 1), resultHeading = prevResultHeading, resultHint = prevResultHint)
      )
    }

    "return current: 2005, prev: 7503 for purchase price of 250100" in {
      val slices = Seq(
        SliceDetails(from = 0, to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 2000),
        SliceDetails(from = 250000, to = None, rate = 5, taxDue = 5)
      )

      val res = testFreeholdCalcService.freeholdNonResidentialMar16Onwards(baseRequestIndividual(250100, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(2005, baseCalculationDetails(2005, slices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading),
        basePrevResult(7503, baseSlabCalculationDetails(7503, 3), resultHeading = prevResultHeading, resultHint = prevResultHint)
      )
    }

    "return current: 14500, prev: 15000 for purchase price of 500000" in {
      val slices = Seq(
        SliceDetails(from = 0, to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 2000),
        SliceDetails(from = 250000, to = None, rate = 5, taxDue = 12500)
      )

      val res = testFreeholdCalcService.freeholdNonResidentialMar16Onwards(baseRequestIndividual(500000, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(14500, baseCalculationDetails(14500, slices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading),
        basePrevResult(15000, baseSlabCalculationDetails(15000, 3), resultHeading = prevResultHeading, resultHint = prevResultHint)
      )
    }

    "return current: 14505, prev: 20004 for purchase price of 500100" in {
      val slices = Seq(
        SliceDetails(from = 0, to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 2000),
        SliceDetails(from = 250000, to = None, rate = 5, taxDue = 12505)
      )

      val res = testFreeholdCalcService.freeholdNonResidentialMar16Onwards(baseRequestIndividual(500100, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(14505, baseCalculationDetails(14505, slices, detailHeading, bandHeading, detailFooter), resultHeading = resultHeading),
        basePrevResult(20004, baseSlabCalculationDetails(20004, 4), resultHeading = prevResultHeading, resultHint = prevResultHint)
      )
    }
  }

  "calculating freeholdNonResidentialMar12toMar16" must {

    "return 0 for purchase price of 150000" in {
      val res = testFreeholdCalcService.freeholdNonResidentialMar12toMar16(baseRequestIndividual(150000, march2012EffectiveDate))
      res shouldBe baseResult(0, baseSlabCalculationDetails(0, 0))
    }

    "return 1500 for purchase price of 150001" in {
      val res = testFreeholdCalcService.freeholdNonResidentialMar12toMar16(baseRequestIndividual(150001, march2012EffectiveDate))
      res shouldBe baseResult(1500, baseSlabCalculationDetails(1500, 1))
    }

    "return 2500 for purchase price of 250000" in {
      val res = testFreeholdCalcService.freeholdNonResidentialMar12toMar16(baseRequestIndividual(250000, march2012EffectiveDate))
      res shouldBe baseResult(2500, baseSlabCalculationDetails(2500, 1))
    }

    "return 7500 for purchase price of 250001" in {
      val res = testFreeholdCalcService.freeholdNonResidentialMar12toMar16(baseRequestIndividual(250001, march2012EffectiveDate))
      res shouldBe baseResult(7500, baseSlabCalculationDetails(7500, 3))
    }

    "return 15000 for purchase price of 500000" in {
      val res = testFreeholdCalcService.freeholdNonResidentialMar12toMar16(baseRequestIndividual(500000, march2012EffectiveDate))
      res shouldBe baseResult(15000, baseSlabCalculationDetails(15000, 3))
    }

    "return 20000 for purchase price of 500001" in {
      val res = testFreeholdCalcService.freeholdNonResidentialMar12toMar16(baseRequestIndividual(500001, march2012EffectiveDate))
      res shouldBe baseResult(20000, baseSlabCalculationDetails(20000, 4))
    }

    "return 20000 for purchase price of 75000000" in {
      val res = testFreeholdCalcService.freeholdNonResidentialMar12toMar16(baseRequestIndividual(75000000, march2012EffectiveDate))
      res shouldBe baseResult(3000000, baseSlabCalculationDetails(3000000, 4))
    }
  }

  "calculating freeholdResidentialAddPropApr16Onwards" must {

    val resultHeading: Option[String] = Some("Results based on SDLT rules from 1 April 2016")
    val detailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated " +
      "based on the rules from 1 April 2016")
    val bandHeading: Option[String] = Some("Purchase price bands (£)")
    val detailFooter: Option[String] = Some("Total SDLT due")
    val resultHint: String = "If you dispose of your previous main residence within 3 years " +
      s"you may be eligible for a refund of"
    val prevDetailHeading: Option[String] = Some("This is a breakdown of how the total amount of SDLT was calculated " +
      "based on the rules before 1 April 2016")
    val prevResultHeading: Option[String] = Some("Results based on SDLT rules before 1 April 2016")
    val prevResultHint: Option[String] = Some("You may be entitled to pay SDLT using the old rules if you exchanged contracts before " +
      "26 November 2015.")

    "return current: 1200, prev: 0 for purchase price of 40000" in {

      val currentSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 1200),
        SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 0),
        SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )
      val prevSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 0),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialAddPropApr16Onwards(baseRequestAddProps(40000, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(1200,
          baseCalculationDetails(1200, currentSlices, detailHeading, bandHeading, detailFooter),
          resultHeading,
          Some(hint(resultHint, "£1,200"))),

        basePrevResult(0,
          prevSliceCalculationDetails(0, prevSlices, prevDetailHeading, bandHeading, detailFooter),
          prevResultHeading,
          prevResultHint)
      )
    }

    "return current: 3750, prev: 0 for purchase price of 125000" in {

      val currentSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
        SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 0),
        SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )
      val prevSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 0),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialAddPropApr16Onwards(baseRequestAddProps(125000, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(3750,
          baseCalculationDetails(3750, currentSlices, detailHeading, bandHeading, detailFooter),
          resultHeading,
          Some(hint(resultHint, "£3,750"))),

        basePrevResult(0,
          prevSliceCalculationDetails(0, prevSlices, prevDetailHeading, bandHeading, detailFooter),
          prevResultHeading,
          prevResultHint)
      )
    }

    "return current: 3755, prev: 2 for purchase price of 125100" in {

      val currentSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
        SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 5),
        SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )
      val prevSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialAddPropApr16Onwards(baseRequestAddProps(125100, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(3755,
          baseCalculationDetails(3755, currentSlices, detailHeading, bandHeading, detailFooter),
          resultHeading,
          Some(hint(resultHint, "£3,753"))),

        basePrevResult(2,
          prevSliceCalculationDetails(2, prevSlices, prevDetailHeading, bandHeading, detailFooter),
          prevResultHeading,
          prevResultHint)
      )
    }

    "return current: 10000, prev: 2500 for purchase price of 250000" in {

      val currentSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
        SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 6250),
        SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )
      val prevSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialAddPropApr16Onwards(baseRequestAddProps(250000, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(10000,
          baseCalculationDetails(10000, currentSlices, detailHeading, bandHeading, detailFooter),
          resultHeading,
          Some(hint(resultHint, "£7,500"))),

        basePrevResult(2500,
          prevSliceCalculationDetails(2500, prevSlices, prevDetailHeading, bandHeading, detailFooter),
          prevResultHeading,
          prevResultHint)
      )
    }

    "return current: 10008, prev: 2505 for purchase price of 250100" in {

      val currentSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
        SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 6250),
        SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 8),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )
      val prevSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 5),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialAddPropApr16Onwards(baseRequestAddProps(250100, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(10008,
          baseCalculationDetails(10008, currentSlices, detailHeading, bandHeading, detailFooter),
          resultHeading,
          Some(hint(resultHint, "£7,503"))),

        basePrevResult(2505,
          prevSliceCalculationDetails(2505, prevSlices, prevDetailHeading, bandHeading, detailFooter),
          prevResultHeading,
          prevResultHint)
      )
    }

    "return current: 64000, prev: 36250 for purchase price of 925000" in {

      val currentSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
        SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 6250),
        SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 54000),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )
      val prevSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialAddPropApr16Onwards(baseRequestAddProps(925000, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(64000,
          baseCalculationDetails(64000, currentSlices, detailHeading, bandHeading, detailFooter),
          resultHeading,
          Some(hint(resultHint, "£27,750"))),

        basePrevResult(36250,
          prevSliceCalculationDetails(36250, prevSlices, prevDetailHeading, bandHeading, detailFooter),
          prevResultHeading,
          prevResultHint)
      )
    }

    "return current: 64013, prev: 36260 for purchase price of 925100" in {

      val currentSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
        SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 6250),
        SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 54000),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 13),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )
      val prevSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 10),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialAddPropApr16Onwards(baseRequestAddProps(925100, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(64013,
          baseCalculationDetails(64013, currentSlices, detailHeading, bandHeading, detailFooter),
          resultHeading,
          Some(hint(resultHint, "£27,753"))),

        basePrevResult(36260,
          prevSliceCalculationDetails(36260, prevSlices, prevDetailHeading, bandHeading, detailFooter),
          prevResultHeading,
          prevResultHint)
      )
    }

    "return current: 138750, prev: 93750 for purchase price of 1500000" in {

      val currentSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
        SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 6250),
        SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 54000),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 74750),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )
      val prevSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      val res = testFreeholdCalcService.freeholdResidentialAddPropApr16Onwards(baseRequestAddProps(1500000, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(138750,
          baseCalculationDetails(138750, currentSlices, detailHeading, bandHeading, detailFooter),
          resultHeading,
          Some(hint(resultHint, "£45,000"))),

        basePrevResult(93750,
          prevSliceCalculationDetails(93750, prevSlices, prevDetailHeading, bandHeading, detailFooter),
          prevResultHeading,
          prevResultHint)
      )
    }

    "return current: 138765, prev: 93762 for purchase price of 1500100" in {

      val currentSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
        SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 6250),
        SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 54000),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 74750),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 15)
      )
      val prevSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 12)
      )

      val res = testFreeholdCalcService.freeholdResidentialAddPropApr16Onwards(baseRequestAddProps(1500100, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(138765,
          baseCalculationDetails(138765, currentSlices, detailHeading, bandHeading, detailFooter),
          resultHeading,
          Some(hint(resultHint, "£45,003"))),

        basePrevResult(93762,
          prevSliceCalculationDetails(93762, prevSlices, prevDetailHeading, bandHeading, detailFooter),
          prevResultHeading,
          prevResultHint)
      )
    }

    "return current: 11163750, prev: 8913750 for purchase price of 75000000" in {

      val currentSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
        SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 6250),
        SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 54000),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 74750),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 11025000)
      )
      val prevSlices = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 8820000)
      )

      val res = testFreeholdCalcService.freeholdResidentialAddPropApr16Onwards(baseRequestAddProps(75000000, april2016EffectiveDate))
      res shouldBe Seq(
        baseResult(11163750,
          baseCalculationDetails(11163750, currentSlices, detailHeading, bandHeading, detailFooter),
          resultHeading,
          Some(hint(resultHint, "£2,250,000"))),

        basePrevResult(8913750,
          prevSliceCalculationDetails(8913750, prevSlices, prevDetailHeading, bandHeading, detailFooter),
          prevResultHeading,
          prevResultHint)
      )
    }

    "throw the correct exception" when {
      "there is no calculation details in the previous calculation" in {
        val testPrevErrorService = new FreeholdCalculationService(new BaseCalculationService, new RefundEntitlementService) {
          override def freeholdResidentialDec14Onwards(request: Request, asPrevResult: Boolean, nonUKResident: Boolean = false): Result = {
            Result(
              totalTax = 50000,
              npv = None,
              taxCalcs = Seq.empty
            )
          }
        }

        the[RequiredValueNotDefinedException] thrownBy
          testPrevErrorService.freeholdResidentialAddPropApr16Onwards(baseRequestAddProps(75000000, april2016EffectiveDate)) should
          have message "[FreeholdCalculationService] [freeholdResidentialAddPropApr16Onwards] - " +
          "Premium result not defined in previous calculation"
      }
    }

    "return no hints about refunds" when {
      "The purchaser is not an individual" in {

        val req = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2016, 4, 1),
          nonUKResident = None,
          premium = 125100,
          highestRent = 0,
          leaseDetails = None,
          propertyDetails = Some(
            PropertyDetails(
              individual = false,
              twoOrMoreProperties = None,
              replaceMainResidence = None,
              sharedOwnership = None,
              currentValue = None
            )
          ),
          relevantRentDetails = None,
          taxReliefDetails = None,
          firstTimeBuyer = None
        )

        val currentSlices = Seq(
          SliceDetails(from = 0, to = Some(125000), rate = 3, taxDue = 3750),
          SliceDetails(from = 125000, to = Some(250000), rate = 5, taxDue = 5),
          SliceDetails(from = 250000, to = Some(925000), rate = 8, taxDue = 0),
          SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
          SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
        )
        val prevSlices = Seq(
          SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
          SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2),
          SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 0),
          SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
          SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
        )

        val res = testFreeholdCalcService.freeholdResidentialAddPropApr16Onwards(req)
        res shouldBe Seq(
          baseResult(3755,
            baseCalculationDetails(3755, currentSlices, detailHeading, bandHeading, detailFooter),
            resultHeading = resultHeading),
          basePrevResult(2,
            prevSliceCalculationDetails(2, prevSlices, prevDetailHeading, bandHeading, detailFooter),
            prevResultHeading,
            prevResultHint)
        )
      }
    }
  }

  "calculating freehold~ Acquisition Relief code 14" must {
    "return tax response for AcquisitionRelief: residential and not linked" in {

      val AcquisitionReliefTestRequest = Request(
        HoldingTypes.freehold,
        PropertyTypes.residential,
        LocalDate.of(2000, 11, 22),
        nonUKResident = None,
        premium = 1000000,
        highestRent = BigDecimal(0),
        propertyDetails = None,
        leaseDetails = None,
        relevantRentDetails = None,
        firstTimeBuyer = None,
        taxReliefDetails = Some(TaxReliefDetails(taxReliefCode = AcquisitionRelief, isPartialRelief = None))
      )

      val calcDetails = CalculationDetails(
        taxType = TaxTypes.premium,
        calcType = CalcTypes.slab,
        taxDue = 5000,
        detailHeading = None,
        bandHeading = None,
        detailFooter = None,
        rate = Some(0),
        rateFraction = Some(5),
        slices = None
      )
      val expectedRes = Result(
        totalTax = 5000,
        resultHeading = Some("Results of calculation based on SDLT rules for the effective date entered"),
        resultHint = None,
        npv = None,
        taxCalcs = Seq(calcDetails)
      )

        val res = testFreeholdCalcService.freeholdAcquisitionTaxRelief(AcquisitionReliefTestRequest)
        res shouldBe expectedRes

    }
  }

  "calculation for a zero percent tax relief" must {

    "return the zero percent tax relief result" in {
      val calcDetails =
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slab,
          taxDue = 0,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          rate = Some(0),
          slices = None
        )
      val expectedRes = Result(
        totalTax = 0,
        resultHeading = Some(RESULT_HEADING_TAX_RELIEF),
        resultHint = None,
        npv = None,
        taxCalcs = Seq(calcDetails)
      )

      val actual =
        testFreeholdCalcService
          .freeholdZeroRateTaxReliefRes

      actual shouldBe expectedRes
    }
  }

  "calculation for freehold self assessed" must {

    "return the self assessed result" in {
      val expectedRes = Result(
        totalTax = 0,
        resultHeading = Some(RESULT_HEADING_TAX_RELIEF_SELF_ASSESSMENT),
        resultHint = None,
        npv = None,
        taxCalcs = Seq.empty
      )

      val actual =
        testFreeholdCalcService
          .freeholdSelfAssessedRes

      actual shouldBe expectedRes
    }
  }
}