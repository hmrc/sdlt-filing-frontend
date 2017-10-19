package calculation

import calculation.models.calculationtables.{Slab, SlabResult}
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
  }
}

