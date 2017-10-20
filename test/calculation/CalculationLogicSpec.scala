package calculation

import calculation.models.SliceDetails
import calculation.models.calculationtables.{Slab, SlabResult, Slice, SliceResult}
import uk.gov.hmrc.play.test.UnitSpec

class CalculationLogicSpec extends UnitSpec {
  "calculateTaxDueSlab" should {
    val slabsArray = Seq(
      Slab(threshold = 2000000, rate = 7),
      Slab(threshold = 1000000, rate = 5),
      Slab(threshold = 500000, rate = 4),
      Slab(threshold = 250000, rate = 3),
      Slab(threshold = 125000, rate = 1))

    "return a slab result with a taxDue & rate of 0" when {
      "amount is less than the minimum[125000] threshold" in {
        val calculate = CalculationLogic.calculateTaxDueSlab(amount = 1000, slabs = slabsArray)
        val slabResult = SlabResult(rate = 0, taxDue = 0)

        calculate.taxDue shouldBe slabResult.taxDue
        calculate.rate shouldBe slabResult.rate
      }

      "amount is equal to the minimum[125000] threshold" in {
        val calculate = CalculationLogic.calculateTaxDueSlab(amount = 125000, slabs = slabsArray)
        val slabResult = SlabResult(rate = 0, taxDue = 0)

        calculate.taxDue shouldBe slabResult.taxDue
        calculate.rate shouldBe slabResult.rate
      }
    }

    "return a slab result with a taxDue of 1250 & rate of 1" when {
      "amount is greater than the minimum[125000] threshold and less than 250000" in {
        val calculate = CalculationLogic.calculateTaxDueSlab(amount = 125001, slabs = slabsArray)
        val slabResult = SlabResult(rate = 1, taxDue = 1250)

        calculate.taxDue shouldBe slabResult.taxDue
        calculate.rate shouldBe slabResult.rate
      }
    }

    "return a slab result with a taxDue of 100000 & rate of 5" when {
      "amount is equal to the highest[2000000] threshold" in {
        val calculate = CalculationLogic.calculateTaxDueSlab(amount = 2000000, slabs = slabsArray)
        val slabResult = SlabResult(rate = 5, taxDue = 100000)

        calculate.taxDue shouldBe slabResult.taxDue
        calculate.rate shouldBe slabResult.rate
      }
    }

    "return a slab result with a taxDue of 140000 & rate of 7" when {
      "amount is greater than the highest[2000000] threshold" in {
        val calculate = CalculationLogic.calculateTaxDueSlab(amount = 2000001, slabs = slabsArray)
        val slabResult = SlabResult(rate = 7, taxDue = 140000)

        calculate.taxDue shouldBe slabResult.taxDue
        calculate.rate shouldBe slabResult.rate
      }
    }

    "calculateTaxDueSlice" should {
      val sliceArray = Seq(
        Slice( from= 0,       to = Some(125000),  rate = 0),
        Slice( from= 125000,  to = Some(250000),  rate = 2),
        Slice( from= 250000,  to = Some(925000),  rate = 5),
        Slice( from= 925000,  to = Some(1500000), rate = 10),
        Slice( from= 1500000, to = None,   rate = 12))

      "return a totalTaxDue of 0" when {
        "the purchase amount is -1" in {
          val calculate = CalculationLogic.calculateTaxDueSlice(amount = -1, slices = sliceArray)

          val result = Seq(
            SliceDetails( from= 0,       to = Some(125000),  rate = 0, taxDue = 0),
            SliceDetails( from= 125000,  to = Some(250000),  rate = 2, taxDue = 0),
            SliceDetails( from= 250000,  to = Some(925000),  rate = 5, taxDue = 0),
            SliceDetails( from= 925000,  to = Some(1500000), rate = 10, taxDue = 0),
            SliceDetails( from= 1500000, to = None, rate = 12, taxDue = 0))

          val sliceResult = SliceResult(taxDue = 0, slices = result)

          calculate.taxDue shouldBe sliceResult.taxDue
          calculate.slices shouldBe sliceResult.slices
        }

        "the purchase amount is 0" in {
          val calculate = CalculationLogic.calculateTaxDueSlice(amount = 0, slices = sliceArray)

          val result = Seq(
            SliceDetails( from= 0,       to = Some(125000),  rate = 0, taxDue = 0),
            SliceDetails( from= 125000,  to = Some(250000),  rate = 2, taxDue = 0),
            SliceDetails( from= 250000,  to = Some(925000),  rate = 5, taxDue = 0),
            SliceDetails( from= 925000,  to = Some(1500000), rate = 10, taxDue = 0),
            SliceDetails( from= 1500000, to = None, rate = 12, taxDue = 0))

          val sliceResult = SliceResult(taxDue = 0, slices = result)

          calculate.taxDue shouldBe sliceResult.taxDue
          calculate.slices shouldBe sliceResult.slices
        }
        
        "the purchase amount is 125000" in {
          val calculate = CalculationLogic.calculateTaxDueSlice(amount = 125000, slices = sliceArray)

          val result = Seq(
            SliceDetails( from= 0,       to = Some(125000),  rate = 0, taxDue = 0),
            SliceDetails( from= 125000,  to = Some(250000),  rate = 2, taxDue = 0),
            SliceDetails( from= 250000,  to = Some(925000),  rate = 5, taxDue = 0),
            SliceDetails( from= 925000,  to = Some(1500000), rate = 10, taxDue = 0),
            SliceDetails( from= 1500000, to = None, rate = 12, taxDue = 0))

          val sliceResult = SliceResult(taxDue = 0, slices = result)

          calculate.taxDue shouldBe sliceResult.taxDue
          calculate.slices shouldBe sliceResult.slices
        }
      }

      "return a totalTaxDue of 1" when {
        "the purchase amount is 125050" in {
          val calculate = CalculationLogic.calculateTaxDueSlice(amount = 125050, slices = sliceArray)

          val result = Seq(
            SliceDetails( from= 0,       to = Some(125000),  rate = 0, taxDue = 0),
            SliceDetails( from= 125000,  to = Some(250000),  rate = 2, taxDue = 1),
            SliceDetails( from= 250000,  to = Some(925000),  rate = 5, taxDue = 0),
            SliceDetails( from= 925000,  to = Some(1500000), rate = 10, taxDue = 0),
            SliceDetails( from= 1500000, to = None, rate = 12, taxDue = 0))

          val sliceResult = SliceResult(taxDue = 1, slices = result)

          calculate.taxDue shouldBe sliceResult.taxDue
          calculate.slices shouldBe sliceResult.slices
        }
      }

      "return a totalTaxDue of 2500" when {
        "the purchase amount is 250000" in {
          val calculate = CalculationLogic.calculateTaxDueSlice(amount = 250000, slices = sliceArray)

          val result = Seq(
            SliceDetails( from= 0,       to = Some(125000),  rate = 0, taxDue = 0),
            SliceDetails( from= 125000,  to = Some(250000),  rate = 2, taxDue = 2500),
            SliceDetails( from= 250000,  to = Some(925000),  rate = 5, taxDue = 0),
            SliceDetails( from= 925000,  to = Some(1500000), rate = 10, taxDue = 0),
            SliceDetails( from= 1500000, to = None, rate = 12, taxDue = 0))

          val sliceResult = SliceResult(taxDue = 2500, slices = result)

          calculate.taxDue shouldBe sliceResult.taxDue
          calculate.slices shouldBe sliceResult.slices
        }
      }


      "return a totalTaxDue of 2501" when {
        "the purchase amount is 250020" in {
          val calculate = CalculationLogic.calculateTaxDueSlice(amount = 250020, slices = sliceArray)

          val result = Seq(
            SliceDetails( from= 0,       to = Some(125000),  rate = 0, taxDue = 0),
            SliceDetails( from= 125000,  to = Some(250000),  rate = 2, taxDue = 2500),
            SliceDetails( from= 250000,  to = Some(925000),  rate = 5, taxDue = 1),
            SliceDetails( from= 925000,  to = Some(1500000), rate = 10, taxDue = 0),
            SliceDetails( from= 1500000, to = None, rate = 12, taxDue = 0))

          val sliceResult = SliceResult(taxDue = 2501, slices = result)

          calculate.taxDue shouldBe sliceResult.taxDue
          calculate.slices shouldBe sliceResult.slices
        }
      }

      "return a totalTaxDue of 93750" when {
        "the purchase amount is 1500000" in {
          val calculate = CalculationLogic.calculateTaxDueSlice(amount = 1500000, slices = sliceArray)

          val result = Seq(
            SliceDetails( from= 0,       to = Some(125000),  rate = 0, taxDue = 0),
            SliceDetails( from= 125000,  to = Some(250000),  rate = 2, taxDue = 2500),
            SliceDetails( from= 250000,  to = Some(925000),  rate = 5, taxDue = 33750),
            SliceDetails( from= 925000,  to = Some(1500000), rate = 10, taxDue = 57500),
            SliceDetails( from= 1500000, to = None, rate = 12, taxDue = 0))

          val sliceResult = SliceResult(taxDue = 93750, slices = result)

          calculate.taxDue shouldBe sliceResult.taxDue
          calculate.slices shouldBe sliceResult.slices
        }
      }

      "return a totalTaxDue of 93751" when {
        "the purchase amount is 1500009" in {
          val calculate = CalculationLogic.calculateTaxDueSlice(amount = 1500009, slices = sliceArray)

          val result = Seq(
            SliceDetails( from= 0,       to = Some(125000),  rate = 0, taxDue = 0),
            SliceDetails( from= 125000,  to = Some(250000),  rate = 2, taxDue = 2500),
            SliceDetails( from= 250000,  to = Some(925000),  rate = 5, taxDue = 33750),
            SliceDetails( from= 925000,  to = Some(1500000), rate = 10, taxDue = 57500),
            SliceDetails( from= 1500000, to = None, rate = 12, taxDue = 1))

          val sliceResult = SliceResult(taxDue = 93751, slices = result)

          calculate.taxDue shouldBe sliceResult.taxDue
          calculate.slices shouldBe sliceResult.slices
        }
      }
    }
  }
}

