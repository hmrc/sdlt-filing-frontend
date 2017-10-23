package calculation.services

import java.time.LocalDate

import calculation.enums.{CalcTypes, HoldingTypes, PropertyTypes, TaxTypes}
import calculation.models._
import uk.gov.hmrc.play.test.UnitSpec

class FreeholdCalculationServiceSpec extends UnitSpec {

  "calculating freeholdResidentialAddPropApr16Onwards" should {

    "return no hints about refunds" when {
      "The purchaser is not an individual" in {

      }
      "The purchaser is an individual but tax is less than would be due from old rates" in {

      }
    }
  }

  "calculating freeholdResidentialDec14Onwards" should {

    def baseCalculationDetails(taxDue: Int, slices: Seq[SliceDetails]) = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slice,
      detailHeading = Some("This is a breakdown of how the total amount of SDLT was calculated"),
      bandHeading = Some("Purchase price bands (£)"),
      detailFooter = Some("Total SDLT due"),
      taxDue = taxDue,
      slices = Some(slices)
    )

    def baseResult(taxDue: Int, calcDeets: CalculationDetails) = Result(
      totalTax = taxDue,
      resultHeading = None,
      resultHint = None,
      npv = None,
      taxCalcs = Seq(calcDeets)
    )

    def baseRequest(premium: BigDecimal) = Request(
      holdingType = HoldingTypes.freehold,
      propertyType = PropertyTypes.residential,
      effectiveDate = LocalDate.of(2014, 12, 30),
      premium = premium,
      highestRent = 0,
      leaseDetails = None,
      propertyDetails = None
    )

    "return 0 for purchase price of 125000" in {

      val resSlices = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 0),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = FreeholdCalculationService.freeholdResidentialDec14Onwards(baseRequest(125000))
      res shouldBe baseResult(0, baseCalculationDetails(0, resSlices))
    }

    "return 1 for purchase price of 125050" in {

      val resSlices = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 1),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = FreeholdCalculationService.freeholdResidentialDec14Onwards(baseRequest(125050))
      res shouldBe baseResult(1, baseCalculationDetails(1, resSlices))
    }

    "return 2500 for purchase price of 250000" in {
      val resSlices = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = FreeholdCalculationService.freeholdResidentialDec14Onwards(baseRequest(250000))
      res shouldBe baseResult(2500, baseCalculationDetails(2500, resSlices))
    }

    "return 2501 for purchase price of 250020" in {
      val resSlices = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 1),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = FreeholdCalculationService.freeholdResidentialDec14Onwards(baseRequest(250020))
      res shouldBe baseResult(2501, baseCalculationDetails(2501, resSlices))
    }

    "return 36250 for purchase price of 925000" in {
      val resSlices = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 33750),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = FreeholdCalculationService.freeholdResidentialDec14Onwards(baseRequest(925000))
      res shouldBe baseResult(36250, baseCalculationDetails(36250, resSlices))
    }

    "return 36251 for purchase price of 925010" in {
      val resSlices = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 33750),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 1),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = FreeholdCalculationService.freeholdResidentialDec14Onwards(baseRequest(925010))
      res shouldBe baseResult(36251, baseCalculationDetails(36251, resSlices))
    }

    "return 93750 for purchase price of 1500000" in {
      val resSlices = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 33750),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = FreeholdCalculationService.freeholdResidentialDec14Onwards(baseRequest(1500000))
      res shouldBe baseResult(93750, baseCalculationDetails(93750, resSlices))
    }

    "return 93751 for purchase price of 1500009" in {
      val resSlices = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 33750),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 1)
      )

      val res = FreeholdCalculationService.freeholdResidentialDec14Onwards(baseRequest(1500009))
      res shouldBe baseResult(93751, baseCalculationDetails(93751, resSlices))

    }
  }

  "calculating freeholdResidentialMar12toDec14" should {

    def baseCalculationDetails(taxDue: Int, rate: Int) = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slab,
      detailHeading = None,
      bandHeading = None,
      detailFooter = None,
      taxDue = taxDue,
      rate = Some(rate),
      slices = None
    )

    def baseResult(taxDue: Int, calcDeets: CalculationDetails) = Result(
      totalTax = taxDue,
      resultHeading = None,
      resultHint = None,
      npv = None,
      taxCalcs = Seq(calcDeets)
    )

    def baseRequest(premium: BigDecimal) = Request(
      holdingType = HoldingTypes.freehold,
      propertyType = PropertyTypes.residential,
      effectiveDate = LocalDate.of(2012, 4, 1),
      premium = premium,
      highestRent = 0,
      leaseDetails = None,
      propertyDetails = None
    )

    "return 0 for purchase price of 125000" in {
      val res = FreeholdCalculationService.freeholdResidentialMar12toDec14(baseRequest(125000))
      res shouldBe baseResult(0, baseCalculationDetails(0, 0))
    }

    "return 1250 for purchase price of 125001" in {
      val res = FreeholdCalculationService.freeholdResidentialMar12toDec14(baseRequest(125001))
      res shouldBe baseResult(1250, baseCalculationDetails(1250, 1))
    }

    "return 2500 for purchase price of 250000" in {
      val res = FreeholdCalculationService.freeholdResidentialMar12toDec14(baseRequest(250000))
      res shouldBe baseResult(2500, baseCalculationDetails(2500, 1))
    }

    "return 7500 for purchase price of 250001" in {
      val res = FreeholdCalculationService.freeholdResidentialMar12toDec14(baseRequest(250001))
      res shouldBe baseResult(7500, baseCalculationDetails(7500, 3))
    }

    "return 15000 for purchase price of 500000" in {
      val res = FreeholdCalculationService.freeholdResidentialMar12toDec14(baseRequest(500000))
      res shouldBe baseResult(15000, baseCalculationDetails(15000, 3))

    }

    "return 20000 for purchase price of 500001" in {
      val res = FreeholdCalculationService.freeholdResidentialMar12toDec14(baseRequest(500001))
      res shouldBe baseResult(20000, baseCalculationDetails(20000, 4))
    }

    "return 40000 for purchase price of 1000000" in {
      val res = FreeholdCalculationService.freeholdResidentialMar12toDec14(baseRequest(1000000))
      res shouldBe baseResult(40000, baseCalculationDetails(40000, 4))
    }

    "return 50000 for purchase price of 1000001" in {
      val res = FreeholdCalculationService.freeholdResidentialMar12toDec14(baseRequest(1000001))
      res shouldBe baseResult(50000, baseCalculationDetails(50000, 5))
    }

    "return 100000 for purchase price of 2000000" in {
      val res = FreeholdCalculationService.freeholdResidentialMar12toDec14(baseRequest(2000000))
      res shouldBe baseResult(100000, baseCalculationDetails(100000, 5))
    }

    "return 140000 for purchase price of 2000001" in {
      val res = FreeholdCalculationService.freeholdResidentialMar12toDec14(baseRequest(2000001))
      res shouldBe baseResult(140000, baseCalculationDetails(140000, 7))
    }
  }

  "calculating freeholdNonResidentialMar16Onwards" should {


    def baseSliceCalculationDetails(taxDue: Int, slices: Seq[SliceDetails]) = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slice,
      detailHeading = Some("This is a breakdown of how the total amount of SDLT was calculated " +
                           "based on the rules from 17 March 2016"),
      bandHeading = Some("Purchase price bands (£)"),
      detailFooter = Some("Total SDLT due"),
      taxDue = taxDue,
      rate = None,
      slices = Some(slices)
    )

    def baseSlabCalculationDetails(taxDue: Int, rate: Int) = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slab,
      detailHeading = None,
      bandHeading = None,
      detailFooter = None,
      taxDue = taxDue,
      rate = Some(rate),
      slices = None
    )

    def baseResult(taxDue: Int, calcDeets: CalculationDetails) = Result(
      totalTax = taxDue,
      resultHeading = Some("Results based on SDLT rules from 17 March 2016"),
      resultHint = None,
      npv = None,
      taxCalcs = Seq(calcDeets)
    )

    def basePrevResult(taxDue: Int, calcDeets: CalculationDetails) = Result(
      totalTax = taxDue,
      resultHeading = Some("Results based on SDLT rules before 17 March 2016"),
      resultHint = Some("You may be entitled to pay SDLT using the old rules if you exchanged contracts before " +
                        "17 March 2016."),
      npv = None,
      taxCalcs = Seq(calcDeets)
    )

    def baseRequest(premium: BigDecimal) = Request(
      holdingType = HoldingTypes.freehold,
      propertyType = PropertyTypes.nonResidential,
      effectiveDate = LocalDate.of(2016, 4, 1),
      premium = premium,
      highestRent = 0,
      leaseDetails = None,
      propertyDetails = None
    )

    "return current: 0, prev: 0 for purchase price of 150000" in {
      val slices = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 0),
        SliceDetails(from = 250000, to = None,         rate = 5, taxDue = 0)
      )

      val res = FreeholdCalculationService.freeholdNonResidentialMar16Onwards(baseRequest(150000))
      res shouldBe Seq(
        baseResult(0, baseSliceCalculationDetails(0, slices)),
        basePrevResult(0, baseSlabCalculationDetails(0, 0))
      )
    }

    "return current: 2, prev: 1501 for purchase price of 150100" in {
      val slices = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 2),
        SliceDetails(from = 250000, to = None,         rate = 5, taxDue = 0)
      )

      val res = FreeholdCalculationService.freeholdNonResidentialMar16Onwards(baseRequest(150100))
      res shouldBe Seq(
        baseResult(2, baseSliceCalculationDetails(2, slices)),
        basePrevResult(1501, baseSlabCalculationDetails(1501, 1))
      )
    }

    "return current: 2000, prev: 2500 for purchase price of 250000" in {
      val slices = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 2000),
        SliceDetails(from = 250000, to = None,         rate = 5, taxDue = 0)
      )

      val res = FreeholdCalculationService.freeholdNonResidentialMar16Onwards(baseRequest(250000))
      res shouldBe Seq(
        baseResult(2000, baseSliceCalculationDetails(2000, slices)),
        basePrevResult(2500, baseSlabCalculationDetails(2500, 1))
      )
    }

    "return current: 2005, prev: 7503 for purchase price of 250100" in {
      val slices = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 2000),
        SliceDetails(from = 250000, to = None,         rate = 5, taxDue = 5)
      )

      val res = FreeholdCalculationService.freeholdNonResidentialMar16Onwards(baseRequest(250100))
      res shouldBe Seq(
        baseResult(2005, baseSliceCalculationDetails(2005, slices)),
        basePrevResult(7503, baseSlabCalculationDetails(7503, 3))
      )
    }

    "return current: 14500, prev: 15000 for purchase price of 500000" in {
      val slices = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 2000),
        SliceDetails(from = 250000, to = None,         rate = 5, taxDue = 12500)
      )

      val res = FreeholdCalculationService.freeholdNonResidentialMar16Onwards(baseRequest(500000))
      res shouldBe Seq(
        baseResult(14500, baseSliceCalculationDetails(14500, slices)),
        basePrevResult(15000, baseSlabCalculationDetails(15000, 3))
      )
    }

    "return current: 14505, prev: 20004 for purchase price of 500100" in {
      val slices = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = Some(250000), rate = 2, taxDue = 2000),
        SliceDetails(from = 250000, to = None,         rate = 5, taxDue = 12505)
      )

      val res = FreeholdCalculationService.freeholdNonResidentialMar16Onwards(baseRequest(500100))
      res shouldBe Seq(
        baseResult(14505, baseSliceCalculationDetails(14505, slices)),
        basePrevResult(20004, baseSlabCalculationDetails(20004, 4))
      )
    }
  }

  "calculating freeholdNonResidentialMar12toMar16" should {

    def baseCalculationDetails(taxDue: Int, rate: Int) = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slab,
      detailHeading = None,
      bandHeading = None,
      detailFooter = None,
      taxDue = taxDue,
      rate = Some(rate),
      slices = None
    )

    def baseResult(taxDue: Int, calcDeets: CalculationDetails) = Result(
      totalTax = taxDue,
      resultHeading = None,
      resultHint = None,
      npv = None,
      taxCalcs = Seq(calcDeets)
    )

    def baseRequest(premium: BigDecimal) = Request(
      holdingType = HoldingTypes.freehold,
      propertyType = PropertyTypes.nonResidential,
      effectiveDate = LocalDate.of(2012, 4, 1),
      premium = premium,
      highestRent = 0,
      leaseDetails = None,
      propertyDetails = None
    )

    "return 0 for purchase price of 150000" in {
      val res = FreeholdCalculationService.freeholdNonResidentialMar12toMar16(baseRequest(150000))
      res shouldBe baseResult(0, baseCalculationDetails(0, 0))
    }

    "return 1500 for purchase price of 150001" in {
      val res = FreeholdCalculationService.freeholdNonResidentialMar12toMar16(baseRequest(150001))
      res shouldBe baseResult(1500, baseCalculationDetails(1500, 1))
    }

    "return 2500 for purchase price of 250000" in {
      val res = FreeholdCalculationService.freeholdNonResidentialMar12toMar16(baseRequest(250000))
      res shouldBe baseResult(2500, baseCalculationDetails(2500, 1))
    }

    "return 7500 for purchase price of 250001" in {
      val res = FreeholdCalculationService.freeholdNonResidentialMar12toMar16(baseRequest(250001))
      res shouldBe baseResult(7500, baseCalculationDetails(7500, 3))
    }

    "return 15000 for purchase price of 500000" in {
      val res = FreeholdCalculationService.freeholdNonResidentialMar12toMar16(baseRequest(500000))
      res shouldBe baseResult(15000, baseCalculationDetails(15000, 3))
    }

    "return 20000 for purchase price of 500001" in {
      val res = FreeholdCalculationService.freeholdNonResidentialMar12toMar16(baseRequest(500001))
      res shouldBe baseResult(20000, baseCalculationDetails(20000, 4))
    }
  }
}
