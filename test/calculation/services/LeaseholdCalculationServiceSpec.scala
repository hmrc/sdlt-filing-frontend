package calculation.services

import calculation.exceptions.RequiredValueNotDefinedException
import calculation.fixtures.{LeaseholdRequestFeature, LeaseholdResultFixture}
import calculation.models._
import uk.gov.hmrc.play.test.UnitSpec

class LeaseholdCalculationServiceSpec extends UnitSpec with LeaseholdRequestFeature with LeaseholdResultFixture {

  class PredefinedNPVSetup(predefinedNPV: BigDecimal){
    protected val npv: Int = predefinedNPV.toInt
    val service = new LeaseholdCalculationService(
      new BaseCalculationService{
        override def calculateNPV(leaseDetails: LeaseDetails): BigDecimal = predefinedNPV
      },
     new RefundEntitlementService
    )
  }

  "getNPV" should {
    "provide the NPV when the lease details are defined" in new PredefinedNPVSetup(1000) {
      service.getNPV("getNPVTestFunction", Some(testLeaseDetails)) shouldBe 1000
    }
    "throw the correct exception when the lease details are not defined" in new PredefinedNPVSetup(1000) {
      the[RequiredValueNotDefinedException] thrownBy
        service.getNPV("getNPVTestFunction", None) should have message
        "[LeaseholdCalculationService] [getNPVTestFunction] Lease details not defined when required"
    }
  }

  "leaseholdResidentialAddPropApr16Onwards" should{
    "return 0, 0 for purchase price of 39,999.99, npv of 125000" in new PredefinedNPVSetup(125000){
      val leaseTaxDue, premTaxDue = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 0),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )

      val prevSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 0),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 0)
      )

      val leaseRequest = leaseholdResidentialAddPropApr16OnwardsRequest(39999.99)
      val prevResult = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, prevSliceDetails, npv, true)
      val refundEntitlementValue = service.refundEntitlementService.calculateRefundEntitlement(premTaxDue,prevResult.totalTax, leaseRequest.propertyDetails)

      val refundEntitlementMessage = if(refundEntitlementValue.isDefined) Some(s"If you dispose of your previous main residence within 3 years you may be eligible for a refund of £$refundEntitlementValue.")
      else None

      val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = refundEntitlementMessage)

      service.leaseholdResidentialAddPropApr16Onwards(leaseRequest) shouldBe Seq(result,prevResult)
    }
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

  "leaseholdResidentialMar12toDec14" should {

    "return 0, 0 for purchase price of 125000, npv of 125000" in new PredefinedNPVSetup(125000) {
      val leaseTaxDue = 0
      val premTaxDue = 0
      val premRate = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )

      val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdResidentialMar12toDec14(leaseholdResidentialMar12toDec14Request(125000)) shouldBe res
    }

    "return 1250, 0 for purchase price of 125001, npv of 125000" in new PredefinedNPVSetup(125000) {
      val leaseTaxDue = 0
      val premTaxDue = 1250
      val premRate = 1
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )

      val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdResidentialMar12toDec14(leaseholdResidentialMar12toDec14Request(125001)) shouldBe res
    }

    "return 2500, 0 for purchase price of 250000, npv of 125000" in new PredefinedNPVSetup(125000) {
      val leaseTaxDue = 0
      val premTaxDue = 2500
      val premRate = 1
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )

      val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdResidentialMar12toDec14(leaseholdResidentialMar12toDec14Request(250000)) shouldBe res
    }

    "return 7500, 1 for purchase price of 250001, npv of 125100" in new PredefinedNPVSetup(125100) {
      val leaseTaxDue = 1
      val premTaxDue = 7500
      val premRate = 3
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )

      val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdResidentialMar12toDec14(leaseholdResidentialMar12toDec14Request(250001)) shouldBe res
    }

    "return 15000, 1 for purchase price of 500000, npv of 125100" in new PredefinedNPVSetup(125100) {
      val leaseTaxDue = 1
      val premTaxDue = 15000
      val premRate = 3
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )

      val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdResidentialMar12toDec14(leaseholdResidentialMar12toDec14Request(500000)) shouldBe res
    }

    "return 20000, 1 for purchase price of 500001, npv of 125100" in new PredefinedNPVSetup(125100) {
      val leaseTaxDue = 1
      val premTaxDue = 20000
      val premRate = 4
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )

      val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdResidentialMar12toDec14(leaseholdResidentialMar12toDec14Request(500001)) shouldBe res
    }

    "return 40000, 1 for purchase price of 1000000, npv of 125100" in new PredefinedNPVSetup(125100) {
      val leaseTaxDue = 1
      val premTaxDue = 40000
      val premRate = 4
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )

      val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdResidentialMar12toDec14(leaseholdResidentialMar12toDec14Request(1000000)) shouldBe res
    }

    "return 50000, 1 for purchase price of 1000001, npv of 125100" in new PredefinedNPVSetup(125100) {
      val leaseTaxDue = 1
      val premTaxDue = 50000
      val premRate = 5
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )

      val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdResidentialMar12toDec14(leaseholdResidentialMar12toDec14Request(1000001)) shouldBe res
    }

    "return 100000, 1 for purchase price of 2000000, npv of 125100" in new PredefinedNPVSetup(125100) {
      val leaseTaxDue = 1
      val premTaxDue = 100000
      val premRate = 5
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )

      val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdResidentialMar12toDec14(leaseholdResidentialMar12toDec14Request(2000000)) shouldBe res
    }

    "return 140000, 1250 for purchase price of 2000001, npv of 250000" in new PredefinedNPVSetup(250000) {
      val leaseTaxDue = 1250
      val premTaxDue = 140000
      val premRate = 7
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1250)
      )

      val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdResidentialMar12toDec14(leaseholdResidentialMar12toDec14Request(2000001)) shouldBe res
    }
  }

}
