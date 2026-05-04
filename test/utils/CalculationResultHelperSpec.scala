/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import base.SpecBase
import models.taxCalculation.{CalcTypes, CalculationDetails, SliceDetails, TaxCalculationResult, TaxTypes}
import models.{FullReturn, Land, Transaction, UserAnswers}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, Text}
import utils.CalculationResultHelper.*

class CalculationResultHelperSpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()

  private def slice(from: Int, to: Option[Int], rate: Int, taxDue: Int): SliceDetails =
    SliceDetails(from, to, rate, taxDue)

  private def sliceCalc(taxType: TaxTypes.Value, taxDue: Int, slices: SliceDetails*): CalculationDetails =
    CalculationDetails(taxType, CalcTypes.slice, taxDue, None, None, None, None, None, Some(slices))

  private def slabCalc(taxType: TaxTypes.Value, taxDue: Int, rate: Int, fraction: Option[Int] = None): CalculationDetails =
    CalculationDetails(taxType, CalcTypes.slab, taxDue, None, None, None, Some(rate), fraction, None)

  private def answersWith(transactionDescription: String): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(FullReturn(
      stornId           = "TESTSTORN",
      returnResourceRef = "REF001",
      transaction       = Some(Transaction(
        effectiveDate          = Some("2024-04-01"),
        totalConsideration     = Some(BigDecimal(300000)),
        claimingRelief         = Some("no"),
        transactionDescription = Some(transactionDescription),
        isLinked               = Some("no")
      )),
      land              = Some(Seq(Land(propertyType = Some("R"))))
    )))

  private val freeholdAnswers  = answersWith("F")
  private val leaseholdAnswers = answersWith("L")

  private val freeholdResult = TaxCalculationResult(9000, None, None, None, Seq(
    sliceCalc(TaxTypes.premium, 9000,
      slice(0,      Some(250000), 0, 0),
      slice(250000, Some(925000), 5, 9000)
    )
  ))

  private val leaseResult = TaxCalculationResult(11000, None, None, None, Seq(
    sliceCalc(TaxTypes.premium, 8000,
      slice(0,      Some(250000), 0, 0),
      slice(250000, Some(925000), 5, 8000)
    ),
    sliceCalc(TaxTypes.rent, 3000,
      slice(0,      Some(150000), 0, 0),
      slice(150000, None,         1, 3000)
    )
  ))

  ".getTaxCalculationSummary" - {

    "produces effective date, consideration, relief and total rows for a freehold result" in {
      val rows = getTaxCalculationSummary(freeholdResult, freeholdAnswers).rows
      rows.map(_.key.content) mustEqual Seq(
        Text("taxCalculation.calculation.taxCalculation.effectiveDate"),
        Text("taxCalculation.calculation.taxCalculation.totalConsideration"),
        Text("taxCalculation.calculation.taxCalculation.reliefClaimed"),
        Text("taxCalculation.calculation.taxCalculation.sdltDue")
      )
      rows.map(_.value.content) mustEqual Seq(
        Text("1 April 2024"), Text("£300,000"), Text("site.no"), Text("£9,000")
      )
    }

    "swaps total consideration for premium + NPV tax rows when a rent calc is present" in {
      val rows = getTaxCalculationSummary(leaseResult, leaseholdAnswers).rows
      rows.map(_.key.content) mustEqual Seq(
        Text("taxCalculation.calculation.taxCalculation.effectiveDate"),
        Text("taxCalculation.calculation.taxCalculation.taxDuePremium"),
        Text("taxCalculation.calculation.taxCalculation.taxDueNpv"),
        Text("taxCalculation.calculation.taxCalculation.reliefClaimed"),
        Text("taxCalculation.calculation.taxCalculation.sdltDue")
      )
      rows.map(_.value.content) must contain allOf(Text("£8,000"), Text("£3,000"), Text("£11,000"))
    }

    "returns an empty list when fullReturn is missing" in {
      getTaxCalculationSummary(freeholdResult, emptyUserAnswers).rows mustBe empty
    }
  }

  ".getRateCardSummary" - {

    "produces the four rate card rows" in {
      val rows = getRateCardSummary(freeholdAnswers).rows
      rows.map(_.key.content) mustEqual Seq(
        Text("taxCalculation.calculation.rateCard.transactionType"),
        Text("taxCalculation.calculation.rateCard.claimingRelief"),
        Text("taxCalculation.calculation.rateCard.propertyType"),
        Text("taxCalculation.calculation.rateCard.linked")
      )
    }

    "returns an empty list when there is no land entry" in {
      val noLand = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
        stornId = "TESTSTORN", returnResourceRef = "REF001",
        transaction = Some(Transaction(transactionDescription = Some("F")))
      )))
      getRateCardSummary(noLand).rows mustBe empty
    }
  }

  ".getPremiumRateTable" - {

    "renders one row per non-zero slice with the freehold caption" in {
      val table = getPremiumRateTable(freeholdResult)
      table.caption mustBe Some("taxCalculation.calculation.rates.caption")
      table.rows must have size 1
      table.rows.head.map(_.content).last mustBe Text("£9,000")
    }

    "drops slices with zero tax due" in {
      // freeholdResult has two slices, only the second has tax due
      getPremiumRateTable(freeholdResult).rows must have size 1
    }

    "renders a single slab row using formatRate when calc is slab-based" in {
      val slabResult = TaxCalculationResult(7500, None, None, None, Seq(slabCalc(TaxTypes.premium, 7500, rate = 3)))
      getPremiumRateTable(slabResult).rows.head.map(_.content) mustEqual Seq(
        Text("taxCalculation.calculation.rates.premium"), Text("3%"), Text("£7,500")
      )
    }

    "renders the rate with one fractional digit when rateFraction is set" in {
      val slabResult = TaxCalculationResult(5000, None, None, None, Seq(slabCalc(TaxTypes.premium, 5000, rate = 0, fraction = Some(5))))
      getPremiumRateTable(slabResult).rows.head.map(_.content)(1) mustBe Text("0.5%")
    }

    "skips the slab row entirely when its tax due is zero" in {
      val zeroSlab = TaxCalculationResult(0, None, None, None, Seq(slabCalc(TaxTypes.premium, 0, rate = 0)))
      getPremiumRateTable(zeroSlab).rows mustBe empty
    }

    "uses the leasehold caption and appends a totalOnPremium footer when rent calc is present" in {
      val table = getPremiumRateTable(leaseResult)
      table.caption mustBe Some("taxCalculation.calculation.rates.captionPremium")
      table.rows.last.map(_.content) mustEqual Seq(
        Text("taxCalculation.calculation.rates.totalOnPremium"), Empty, Text("£8,000")
      )
    }

    "appends the leasehold footer to a slab premium too" in {
      val slabPremiumLease = TaxCalculationResult(7500, None, None, None, Seq(
        slabCalc(TaxTypes.premium, 7500, rate = 3),
        sliceCalc(TaxTypes.rent, 0, slice(0, None, 0, 0))
      ))
      val table = getPremiumRateTable(slabPremiumLease)
      table.caption mustBe Some("taxCalculation.calculation.rates.captionPremium")
      table.rows.head.map(_.content) mustEqual Seq(
        Text("taxCalculation.calculation.rates.premium"), Text("3%"), Text("£7,500")
      )
      table.rows.last.map(_.content) mustEqual Seq(
        Text("taxCalculation.calculation.rates.totalOnPremium"), Empty, Text("£7,500")
      )
    }

    "throws an IllegalStateException when there is no premium calc" in {
      val rentOnly = TaxCalculationResult(3000, None, None, None, Seq(sliceCalc(TaxTypes.rent, 3000, slice(0, None, 1, 3000))))
      a[IllegalStateException] must be thrownBy getPremiumRateTable(rentOnly)
    }
  }

  ".getNpvRateTable" - {

    "is None when no rent calc is present" in {
      getNpvRateTable(freeholdResult) mustBe None
    }

    "renders slice rows plus a totalOnNpv footer when rent calc is slice-based" in {
      val table = getNpvRateTable(leaseResult).value
      table.caption mustBe Some("taxCalculation.calculation.rates.captionNpv")
      table.rows.last.map(_.content) mustEqual Seq(
        Text("taxCalculation.calculation.rates.totalOnNpv"), Empty, Text("£3,000")
      )
    }

    "renders a single slab row + footer when rent calc is slab-based with tax due" in {
      val slabRent = TaxCalculationResult(2500, None, None, None, Seq(
        slabCalc(TaxTypes.premium, 0,    rate = 0),
        slabCalc(TaxTypes.rent,    2500, rate = 1)
      ))
      val table = getNpvRateTable(slabRent).value
      table.rows.head.map(_.content) mustEqual Seq(
        Text("taxCalculation.calculation.rates.npv"), Text("1%"), Text("£2,500")
      )
      table.rows.last.map(_.content) mustEqual Seq(
        Text("taxCalculation.calculation.rates.totalOnNpv"), Empty, Text("£2,500")
      )
    }

    "skips the slab row when rent slab tax due is zero" in {
      val zeroSlabRent = TaxCalculationResult(0, None, None, None, Seq(
        slabCalc(TaxTypes.premium, 0, rate = 0),
        slabCalc(TaxTypes.rent,    0, rate = 0)
      ))
      val table = getNpvRateTable(zeroSlabRent).value
      table.rows must have size 1
      table.rows.head.map(_.content) mustEqual Seq(
        Text("taxCalculation.calculation.rates.totalOnNpv"), Empty, Text("£0")
      )
    }
  }

  ".getTotalTaxTable" - {

    "renders the total with a bold label" in {
      getTotalTaxTable(15000).rows.head.map(_.content) mustEqual Seq(
        Text("taxCalculation.calculation.totalSdltDue"), Empty, Text("£15,000")
      )
    }
  }

  ".toViewModel" - {

    "wires up all five components for a freehold slice result" in {
      val vm = toViewModel(freeholdResult, freeholdAnswers)
      vm.taxCalculationSummary.rows must not be empty
      vm.rateCardSummary.rows       must not be empty
      vm.premiumRateTable.caption   mustBe Some("taxCalculation.calculation.rates.caption")
      vm.npvRateTable               mustBe None
      vm.totalTax.rows.head.map(_.content).last mustBe Text("£9,000")
    }

    "wires up the freehold caption for a slab premium" in {
      val slabFreehold = TaxCalculationResult(7500, None, None, None, Seq(slabCalc(TaxTypes.premium, 7500, rate = 3)))
      val vm = toViewModel(slabFreehold, freeholdAnswers)
      vm.premiumRateTable.caption mustBe Some("taxCalculation.calculation.rates.caption")
      vm.premiumRateTable.rows.head.map(_.content) mustEqual Seq(
        Text("taxCalculation.calculation.rates.premium"), Text("3%"), Text("£7,500")
      )
      vm.npvRateTable mustBe None
      vm.totalTax.rows.head.map(_.content).last mustBe Text("£7,500")
    }

    "produces both tables for a leasehold slice/slice result" in {
      val vm = toViewModel(leaseResult, leaseholdAnswers)
      vm.premiumRateTable.caption mustBe Some("taxCalculation.calculation.rates.captionPremium")
      vm.npvRateTable.value.caption mustBe Some("taxCalculation.calculation.rates.captionNpv")
      vm.totalTax.rows.head.map(_.content).last mustBe Text("£11,000")
    }

    "renders a slab premium with a slice NPV for a leasehold result" in {
      val mixed = TaxCalculationResult(10500, None, None, None, Seq(
        slabCalc(TaxTypes.premium, 7500, rate = 3),
        sliceCalc(TaxTypes.rent, 3000, slice(0, Some(150000), 0, 0), slice(150000, None, 1, 3000))
      ))
      val vm = toViewModel(mixed, leaseholdAnswers)
      vm.premiumRateTable.caption mustBe Some("taxCalculation.calculation.rates.captionPremium")
      vm.premiumRateTable.rows.head.map(_.content)(1) mustBe Text("3%")
      vm.npvRateTable.value.rows.last.map(_.content).last mustBe Text("£3,000")
      vm.totalTax.rows.head.map(_.content).last mustBe Text("£10,500")
    }

    "renders a slice premium with a slab NPV for a leasehold result" in {
      val mixed = TaxCalculationResult(9000, None, None, None, Seq(
        sliceCalc(TaxTypes.premium, 9000, slice(0, Some(250000), 0, 0), slice(250000, Some(925000), 5, 9000)),
        slabCalc(TaxTypes.rent, 0, rate = 0)
      ))
      val vm = toViewModel(mixed, leaseholdAnswers)
      vm.premiumRateTable.caption    mustBe Some("taxCalculation.calculation.rates.captionPremium")
      vm.npvRateTable.value.caption  mustBe Some("taxCalculation.calculation.rates.captionNpv")
      vm.totalTax.rows.head.map(_.content).last mustBe Text("£9,000")
    }

    "renders both as slab for a leasehold slab/slab result" in {
      val slabBoth = TaxCalculationResult(7500, None, None, None, Seq(
        slabCalc(TaxTypes.premium, 7500, rate = 3),
        slabCalc(TaxTypes.rent,    0,    rate = 0)
      ))
      val vm = toViewModel(slabBoth, leaseholdAnswers)
      vm.premiumRateTable.caption mustBe Some("taxCalculation.calculation.rates.captionPremium")
      vm.premiumRateTable.rows.head.map(_.content)(1) mustBe Text("3%")
      vm.npvRateTable.value.caption mustBe Some("taxCalculation.calculation.rates.captionNpv")
      vm.totalTax.rows.head.map(_.content).last mustBe Text("£7,500")
    }
  }
}
