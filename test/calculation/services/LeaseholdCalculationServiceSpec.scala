package calculation.services

import java.time.LocalDate

import calculation.enums.{HoldingTypes, PropertyTypes}
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
      }
    )
  }

  class Setup {
    val service = new LeaseholdCalculationService(new BaseCalculationService)
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

  "leaseholdNonResidentialMar12toMar16" should {
    "return 0, 0 for purchase price of 149000, npv of 150000 and zeroRate is TRUE" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue, premTaxDue = 0
      val premRate = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
        SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 0)
      )

      val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(149000)) shouldBe res
    }
    "return 1490, 0 for purchase price of 149000, npv of 150000 and zeroRate is FALSE (because year 2 rent > £2000)" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue = 0
      val premTaxDue = 1490
      val premRate = 1
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
        SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 0)
      )

      val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(149000, year2Rent = 2001)) shouldBe res
    }
    "return 2500, 0 for purchase price of 250000, npv of 150000 and zeroRate is FALSE (because premium > £150000)" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue = 0
      val premTaxDue = 2500
      val premRate = 1
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
        SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 0)
      )

      val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(250000)) shouldBe res
    }
    "return 7500, 0 for purchase price of 250001, npv of 150000 and zeroRate is FALSE (because premium > £150000)" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue = 0
      val premTaxDue = 7500
      val premRate = 3
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
        SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 0)
      )

      val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(250001)) shouldBe res
    }
    "return 15000, 0 for purchase price of 500000, npv of 150000 and zeroRate is FALSE (because premium > £150000)" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue = 0
      val premTaxDue = 15000
      val premRate = 3
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
        SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 0)
      )

      val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(500000)) shouldBe res
    }
    "return 20000, 0 for purchase price of 500001, npv of 150000 and zeroRate is FALSE (because premium > £150000)" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue = 0
      val premTaxDue = 20000
      val premRate = 4
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
        SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 0)
      )

      val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(500001)) shouldBe res
    }
    "return 20000, 1 for purchase price of 500001, npv of 250100 and zeroRate is FALSE (because premium > £150000)" in new PredefinedNPVSetup(250100) {
      val leaseTaxDue = 1
      val premTaxDue = 20000
      val premRate = 4
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
        SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 1)
      )

      val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(500001)) shouldBe res
    }
  }

  "eligibleForZeroRate" should {

    def testRequest(
                   premium: BigDecimal = 150000,
                   year2Rent: BigDecimal = 200,
                   relevantRent: BigDecimal = 200
                   ) = Request(
      holdingType = HoldingTypes.leasehold,
      propertyType = PropertyTypes.nonResidential,
      effectiveDate = LocalDate.of(2017, 1, 13),
      premium = premium,
      highestRent = 1000,
      propertyDetails = None,
      leaseDetails = Some(leaseDetailsWithYear2Rent(year2Rent)),
      relevantRentDetails = Some(RelevantRentDetails(
        exchangedContractsBeforeMar16 = Some(true),
        contractChangedSinceMar16 = Some(false),
        relevantRent = Some(relevantRent)
      ))
    )

    def leaseDetailsWithYear2Rent(rent: BigDecimal) = testLeaseDetailsAllRentsUnder2000.copy(year2Rent = Some(rent))

    "return false" when {
      "premium is £150,000" in new Setup {
        service.eligibleForZeroRate(testRequest()) shouldBe false
      }
      "premium is over £150,000" in new Setup {
        private val request = testRequest(premium = 150001)
        service.eligibleForZeroRate(request) shouldBe false
      }
      "premium is under £150,000 but there is an annual rent of £2,000" in new Setup {
        private val request = testRequest(premium = 149999, year2Rent = 2000)
        service.eligibleForZeroRate(request) shouldBe false
      }
      "premium is under £150,000 but there is an annual rent of over £2,000" in new Setup {
        private val request = testRequest(premium = 149999, year2Rent = 2001)
        service.eligibleForZeroRate(request) shouldBe false
      }
      "premium is under £150,000, all rents are below £2,000 but relevant rent is £1,000" in new Setup {
        private val request = testRequest(premium = 149999, year2Rent = 1999, relevantRent = 1000)
        service.eligibleForZeroRate(request) shouldBe false
      }
      "premium is under £150,000, all rents are below £2,000 but relevant rent is over £1,000" in new Setup {
        private val request = testRequest(premium = 149999, year2Rent = 1999, relevantRent = 1001)
        service.eligibleForZeroRate(request) shouldBe false
      }
    }
    "return true" when {
      "premium is under £150,000, all rents are below £2,000 and relevant rent is below £1,000" in new Setup {
        private val request = testRequest(premium = 149999, year2Rent = 1999, relevantRent = 999)
        service.eligibleForZeroRate(request) shouldBe true
      }
    }
    "throw the correct exception" when {
      "premium is under £150,000 but there are no lease details" in new Setup {
        private val request = testRequest(premium = 149999).copy(leaseDetails = None)
        the[RequiredValueNotDefinedException] thrownBy
          service.eligibleForZeroRate(request) should
          have message "[LeaseholdCalculationService] [eligibleForZeroRate] - " +
          "lease details not defined when premium less than £150,000"
      }
      "premium is under £150,000, all rents are below £2,000 but there are no relevant rent details" in new Setup {
        private val request = testRequest(premium = 149999).copy(relevantRentDetails = None)
        the[RequiredValueNotDefinedException] thrownBy
          service.eligibleForZeroRate(request) should
          have message "[LeaseholdCalculationService] [extractRelevantRent] - relevant rent not defined"
      }
    }
  }

  "nonResPrevCalcRequired" should {

    def testRequest(
                     premium: BigDecimal = 150000,
                     year2Rent: BigDecimal = 200,
                     relevantRent: BigDecimal = 200
                   ) = Request(
      holdingType = HoldingTypes.leasehold,
      propertyType = PropertyTypes.nonResidential,
      effectiveDate = LocalDate.of(2017, 1, 13),
      premium = premium,
      highestRent = 1000,
      propertyDetails = None,
      leaseDetails = Some(leaseDetailsWithYear2Rent(year2Rent)),
      relevantRentDetails = Some(RelevantRentDetails(
        exchangedContractsBeforeMar16 = Some(true),
        contractChangedSinceMar16 = Some(false),
        relevantRent = Some(relevantRent)
      ))
    )

    def leaseDetailsWithYear2Rent(rent: BigDecimal) = testLeaseDetailsAllRentsUnder2000.copy(year2Rent = Some(rent))

    "return true" when {
      "premium is £150,000" in new Setup {
        service.nonResPrevCalcRequired(testRequest()) shouldBe true
      }
      "premium is over £150,000" in new Setup {
        private val request = testRequest(premium = 150001)
        service.nonResPrevCalcRequired(request) shouldBe true
      }
      "premium is under £150,000 and there is an annual rent of £2,000" in new Setup {
        private val request = testRequest(premium = 149999, year2Rent = 2000)
        service.nonResPrevCalcRequired(request) shouldBe true
      }
      "premium is under £150,000 and there is an annual rent of over £2,000" in new Setup {
        private val request = testRequest(premium = 149999, year2Rent = 2001)
        service.nonResPrevCalcRequired(request) shouldBe true
      }
      "premium is under £150,000, all rents are below £2,000 and contract was exchanged < March 2016 and not altered since" in new Setup {
        private val request = testRequest(premium = 149999, year2Rent = 1999).copy(
          relevantRentDetails = Some(RelevantRentDetails(
            exchangedContractsBeforeMar16 = Some(true),
            contractChangedSinceMar16 = Some(false),
            relevantRent = Some(100)
          ))
        )
        service.nonResPrevCalcRequired(request) shouldBe true
      }
    }
    "return false" when {
      "premium is under £150,000, all rents are below £2,000 but contract was exchanged > March 2016" in new Setup {
        private val request = testRequest(premium = 149999, year2Rent = 1999).copy(
          relevantRentDetails = Some(RelevantRentDetails(
            exchangedContractsBeforeMar16 = Some(false),
            contractChangedSinceMar16 = Some(false),
            relevantRent = Some(100)
          ))
        )
        service.nonResPrevCalcRequired(request) shouldBe false
      }
    }
    "throw the correct exception" when {
      "premium is under £150,000 but there are no lease details" in new Setup {
        private val request = testRequest(premium = 149999).copy(leaseDetails = None)
        the[RequiredValueNotDefinedException] thrownBy
          service.nonResPrevCalcRequired(request) should
          have message "[LeaseholdCalculationService] [nonResPrevCalcRequired] - " +
          "lease details not defined when premium less than £150,000"
      }
      "premium is under £150,000, all rents are below £2,000 but there are no relevant rent details" in new Setup {
        private val request = testRequest(premium = 149999).copy(relevantRentDetails = None)
        the[RequiredValueNotDefinedException] thrownBy
          service.nonResPrevCalcRequired(request) should
          have message "[LeaseholdCalculationService] [nonResPrevCalcRequired] - relevant rent not defined"
      }
    }
  }

}
