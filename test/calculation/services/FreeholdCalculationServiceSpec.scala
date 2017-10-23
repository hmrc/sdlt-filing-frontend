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

    "93751 for purchase price of 1500009" in {
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
}
