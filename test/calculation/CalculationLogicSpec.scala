package calculation

/**
  * Created by udaniel on 17/10/17.
  */

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
      "amount is less than the minimum[125000]  threshold" in {
        val calculate = CalculationLogic.calculateTaxDueSlab(amount = 1000, slabs = slabsArray)
        val slabResult = SlabResult(rate = 0, taxDue = 0)

        calculate.taxDue shouldBe slabResult.taxDue
        calculate.rate shouldBe slabResult.rate
      }
    }

    "return a slab result with a taxDue & rate of 0" when {
      "amount is equal to the minimum[125000]  threshold" in {
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


    "calculateTaxDueSLice" should {
      val sliceArray = Seq(
        Slice( from= 0,       to = Some(125000),  rate = 0),
        Slice( from= 125000,  to = Some(250000),  rate = 2),
        Slice( from= 250000,  to = Some(925000),  rate = 5),
        Slice( from= 925000,  to = Some(1500000), rate = 10),
        Slice( from= 1500000, to = None,   rate = 12))

      "return a totalTaxDue of 0" when {
        "there is no Slice.to and the given Amount is greater than Slice.from// PURCHASE OF 125000" in {
          val calculate = CalculationLogic.calculateTaxDueSlice(amount = 250020, slices = sliceArray)

          val placeholder = Seq(SliceDetails(0, Some(10), 2, 55))

          val sliceResult = SliceResult(taxDue = 0, slices = placeholder)

          calculate.taxDue shouldBe sliceResult.taxDue
          calculate.slices shouldBe sliceResult.slices
        }
      }

      "return a valid totalTaxDue" when {
        "there is no slice.to and the given Amount is less than Slice.from" in {

        }
      }

      "return a valid totalTaxDue" when {
        "there is a Slice.to and it's less than the given Amount" in {

        }
      }

      "return a totalTaxDue of 0" when {
        "there is a Slice.to that's less than the Amount AND the Amount is less than or equal to Slice.from" in {

        }
      }

      "return a valid totalTaxDue" when {
        "there is a Slice.to that's less than the Amount AND the Amount is greater than or equal to Slice.from" in {

        }
      }
    }
  }
}

