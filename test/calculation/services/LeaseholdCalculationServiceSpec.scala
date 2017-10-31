package calculation.services

import calculation.fixtures.{LeaseholdRequestFeature, LeaseholdResultFixture}
import calculation.models._
import uk.gov.hmrc.play.test.UnitSpec

class LeaseholdCalculationServiceSpec extends UnitSpec with LeaseholdRequestFeature with LeaseholdResultFixture {

  class PredefinedNPVSetup(predefinedNPV: BigDecimal){
    protected val npv: Int = predefinedNPV.toInt
    val service = new LeaseholdCalculationService(
      new BaseCalculationService{
        override def calculateNPV(leaseDetails: LeaseDetails): BigDecimal = predefinedNPV
      }
    )
  }

  "leaseholdResidentialDec14Onwards" should {

    "return 0, 0 for purchase price of 125000, npv of 125000" in new PredefinedNPVSetup(125000) {

      val leaseTaxDue, premTaxDue = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 0),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(125000)) shouldBe res
    }

    "return 1, 0 for purchase price of 125050, npv of 125000" in new PredefinedNPVSetup(125000) {

      val leaseTaxDue = 0
      val premTaxDue = 1
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 1),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(125050)) shouldBe res
    }

    "return 2500, 0 for purchase price of 250000, npv of 125000" in new PredefinedNPVSetup(125000) {

      val leaseTaxDue = 0
      val premTaxDue = 2500
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(250000)) shouldBe res
    }

    "return 2501, 0 for purchase price of 250020, npv of 125000" in new PredefinedNPVSetup(125000) {

      val leaseTaxDue = 0
      val premTaxDue = 2501
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 1),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(250020)) shouldBe res
    }

    "return 36250, 1 for purchase price of 925000, npv of 125100" in new PredefinedNPVSetup(125100) {

      val leaseTaxDue = 1
      val premTaxDue = 36250
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 33750),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(925000)) shouldBe res
    }

    "return 36251, 1 for purchase price of 925010, npv of 125100" in new PredefinedNPVSetup(125100) {

      val leaseTaxDue = 1
      val premTaxDue = 36251
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 33750),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 1),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(925010)) shouldBe res
    }

    "return 93750, 1 for purchase price of 1500000, npv of 125100" in new PredefinedNPVSetup(125100) {

      val leaseTaxDue = 1
      val premTaxDue = 93750
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 33750),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(1500000)) shouldBe res
    }

    "return 93762, 1 for purchase price of 1500100, npv of 250000" in new PredefinedNPVSetup(250000) {

      val leaseTaxDue = 1250
      val premTaxDue = 93762
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1250)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 33750),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 12)
      )

      val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(1500100)) shouldBe res
    }

  }

}
