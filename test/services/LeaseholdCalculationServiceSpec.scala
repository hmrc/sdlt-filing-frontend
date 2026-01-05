/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import data.ResultText.RESULT_HEADING_TAX_RELEIF

import java.time.LocalDate
import enums.{CalcTypes, HoldingTypes, PropertyTypes, TaxTypes}
import exceptions.RequiredValueNotDefinedException
import fixtures.{LeaseholdRequestFeature, LeaseholdResultFixture}
import models.{CalculationDetails, Result, _}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec

class LeaseholdCalculationServiceSpec extends PlaySpec with LeaseholdRequestFeature with LeaseholdResultFixture {

  val april2021EffectiveEndDate: LocalDate = LocalDate.of(2021, 4, 1)
  val march2021EffectiveDate: LocalDate = LocalDate.of(2021, 3, 31)
  val july2020EffectiveDate: LocalDate = LocalDate.of(2020, 7, 8)
  val nov2018EffectiveDate: LocalDate = LocalDate.of(2018, 1, 1)
  val dec2017EffectiveDate: LocalDate = LocalDate.of(2017, 12, 30)
  val feb2017EffectiveDate: LocalDate = LocalDate.of(2017, 2, 14)
  val sept2022EffectiveDate: LocalDate = LocalDate.of(2022, 9, 23)
  val oct2024EffectiveDate: LocalDate = LocalDate.of(2024, 10, 31)
  val april2025EffectiveDate: LocalDate = LocalDate.of(2025, 4, 1)

  class PredefinedNPVSetup(predefinedNPV: BigDecimal, refundEntitlement: Option[Int] = None){
    protected val npv: Int = predefinedNPV.toInt
    val service = new LeaseholdCalculationService(
      new BaseCalculationService{
        override def calculateNPV(leaseDetails: LeaseDetails): BigDecimal = predefinedNPV
      },
      new RefundEntitlementService {
        override def calculateRefundEntitlement(premiumResultTaxDue: BigDecimal, prevResultTax: Int, reqPropertyDetails: Option[PropertyDetails]): Option[Int] = refundEntitlement
      }
    )
  }

  class Setup {
    val service = new LeaseholdCalculationService(new BaseCalculationService, new RefundEntitlementService)
  }

  "getNPV" must {
    "provide the NPV when the lease details are defined" in new PredefinedNPVSetup(1000) {
      service.getNPV("getNPVTestFunction", Some(testLeaseDetails)) shouldBe 1000
    }
    "throw the correct exception when the lease details are not defined" in new PredefinedNPVSetup(1000) {
      the[RequiredValueNotDefinedException] thrownBy
        service.getNPV("getNPVTestFunction", None) must have message
        "[LeaseholdCalculationService] [getNPVTestFunction] Lease details not defined when required"
    }
  }

  "checkIfShared" must {
    "return true" when {
      "sharedOwnerships is Some(true)" in new Setup{
        val propertyDetails: Option[PropertyDetails] = Some(
          PropertyDetails(
            individual = true,
            twoOrMoreProperties = Some(false),
            replaceMainResidence = None,
            sharedOwnership = Some(true),
            currentValue = None
          )
        )
        service.checkIfShared(propertyDetails) shouldBe true
      }
    }

    "return false" when {
      "sharedOwnership is Some(false)" in new Setup {
        val propertyDetails: Option[PropertyDetails] = Some(
          PropertyDetails(
            individual = true,
            twoOrMoreProperties = Some(false),
            replaceMainResidence = None,
            sharedOwnership = Some(false),
            currentValue = None
          )
        )
        service.checkIfShared(propertyDetails) shouldBe false
      }

      "sharedOwnership is None" in new Setup{
        val propertyDetails: Option[PropertyDetails] = Some(
          PropertyDetails(
            individual = true,
            twoOrMoreProperties = Some(false),
            replaceMainResidence = None,
            sharedOwnership = None,
            currentValue = None
          )
        )
        service.checkIfShared(propertyDetails) shouldBe false
      }
    }
  }

  "leaseholdResidentialNov17OnwardsFTBShared" must {
    val MAX_PREMIUM_FTB = 500000
    "return 0, 0 for purchase price of 195000, npv of 71428" in new PredefinedNPVSetup(71428) {
      val leaseTaxDue = 0
      val premTaxDue = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 0, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )

      val testLeaseDetails: LeaseDetails = LeaseDetails(
        startDate = LocalDate.of(2018, 11, 1),
        endDate = LocalDate.of(3007, 11, 1),
        leaseTerm = LeaseTerm(
          years = 5,
          days = 0,
          daysInPartialYear = 365
        ),
        year1Rent = 2500,
        year2Rent = Some(2500),
        year3Rent = Some(2500),
        year4Rent = Some(2500),
        year5Rent = Some(2500)
      )

      private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialNov17OnwardsFTB(leaseholdResidentialNov17OnwardsFTBRequestShared(195000, testLeaseDetails)) shouldBe res
    }

    "return 0, 0 for purchase price of 299999, npv of 71428" in new PredefinedNPVSetup(714285) {
      val leaseTaxDue = 0
      val premTaxDue = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 0, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )

      val testLeaseDetails: LeaseDetails = LeaseDetails(
        startDate = LocalDate.of(2018, 11, 1),
        endDate = LocalDate.of(3007, 10, 30),
        leaseTerm = LeaseTerm(
          years = 5,
          days = 0,
          daysInPartialYear = 365
        ),
        year1Rent = 25000,
        year2Rent = Some(25000),
        year3Rent = Some(25000),
        year4Rent = Some(25000),
        year5Rent = Some(25000)
      )

      private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialNov17OnwardsFTB(leaseholdResidentialNov17OnwardsFTBRequestShared(299999, testLeaseDetails)) shouldBe res
    }

    "return 0, 0 for purchase price of 255000, npv of 428751" in new PredefinedNPVSetup(428571) {
      val leaseTaxDue = 0
      val premTaxDue = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 0, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )

      val testLeaseDetails: LeaseDetails = LeaseDetails(
        startDate = LocalDate.of(2018, 11, 1),
        endDate = LocalDate.of(3007, 11, 1),
        leaseTerm = LeaseTerm(
          years = 5,
          days = 0,
          daysInPartialYear = 365
        ),
        year1Rent = 15000,
        year2Rent = Some(15000),
        year3Rent = Some(15000),
        year4Rent = Some(15000),
        year5Rent = Some(15000)
      )

      private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialNov17OnwardsFTB(leaseholdResidentialNov17OnwardsFTBRequestShared(255000, testLeaseDetails)) shouldBe res
    }

    "return 0, 750 for purchase price of 315000, npv of 428751" in new PredefinedNPVSetup(428571) {
      val leaseTaxDue = 0
      val premTaxDue = 750
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 0, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 750)
      )

      val testLeaseDetails: LeaseDetails = LeaseDetails(
        startDate = LocalDate.of(2018, 11, 1),
        endDate = LocalDate.of(3007, 11, 1),
        leaseTerm = LeaseTerm(
          years = 5,
          days = 0,
          daysInPartialYear = 365
        ),
        year1Rent = 15000,
        year2Rent = Some(15000),
        year3Rent = Some(15000),
        year4Rent = Some(15000),
        year5Rent = Some(15000)
      )

      private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialNov17OnwardsFTB(leaseholdResidentialNov17OnwardsFTBRequestShared(315000, testLeaseDetails)) shouldBe res
    }

    "return 0, 4750 for purchase price of 395000, npv of 124088" in new PredefinedNPVSetup(124088) {
      val leaseTaxDue = 0
      val premTaxDue = 4750
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 0, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 4750)
      )

      val testLeaseDetails: LeaseDetails = LeaseDetails(
        startDate = LocalDate.of(2018, 11, 1),
        endDate = LocalDate.of(2077, 10, 31),
        leaseTerm = LeaseTerm(
          years = 5,
          days = 0,
          daysInPartialYear = 365
        ),
        year1Rent = 5000,
        year2Rent = Some(5000),
        year3Rent = Some(5000),
        year4Rent = Some(5000),
        year5Rent = Some(5000)
      )

      private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialNov17OnwardsFTB(leaseholdResidentialNov17OnwardsFTBRequestShared(395000, testLeaseDetails)) shouldBe res
    }
  }

  "leaseholdResidentialAddPropJuly20Onwards" must {

    "return 15019, 19 for purchase price of 500000, npv of 501945" in new PredefinedNPVSetup(501945, Some(15000)) {
      val leaseTaxDue = 19
      val premTaxDue = 15000
      val prevTax = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 3, taxDue = 15000),
        SliceDetails(from = 500000, to = Some(925000), rate = 8, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )

      val prevSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      private val leaseRequest = leaseholdResidentialAddPropJuly20OnwardsRequestIsIndividual(500000, july2020EffectiveDate)

      private val result = leaseholdResidentialAddPropJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = Some("15,000"))
      private val prevResult = leaseholdResidentialJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, prevTax, prevSliceDetails, npv, asPreviousResult = true)

      service.leaseholdResidentialAddPropJuly20Onwards(leaseRequest) shouldBe Seq(result, prevResult)
    }

    "return 15099, 69 for purchase price of 501000, npv of 501945" in new PredefinedNPVSetup(501945, Some(15000)) {
      val leaseTaxDue = 19
      val premTaxDue = 15080
      val prevTax = 50
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )

      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 3, taxDue = 15000),
        SliceDetails(from = 500000, to = Some(925000), rate = 8, taxDue = 80),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )

      val prevSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 50),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      private val leaseRequest = leaseholdResidentialAddPropJuly20OnwardsRequestIsIndividual(501000, july2020EffectiveDate)

      private val result = leaseholdResidentialAddPropJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = Some("15,000"))
      private val prevResult = leaseholdResidentialJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, prevTax, prevSliceDetails, npv, asPreviousResult = true)

      service.leaseholdResidentialAddPropJuly20Onwards(leaseRequest) shouldBe Seq(result, prevResult)
    }

    "return 14989, 0 for purchase price of 499000, npv of 501945" in new PredefinedNPVSetup(501945, Some(15000)) {
      val leaseTaxDue = 19
      val premTaxDue = 14970
      val prevTax = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 3, taxDue = 14970),
        SliceDetails(from = 500000, to = Some(925000), rate = 8, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )

      val prevSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      private val leaseRequest = leaseholdResidentialAddPropJuly20OnwardsRequestIsIndividual(499000, july2020EffectiveDate)

      private val result = leaseholdResidentialAddPropJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = Some("15,000"))
      private val prevResult = leaseholdResidentialJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, prevTax, prevSliceDetails, npv, asPreviousResult = true)

      service.leaseholdResidentialAddPropJuly20Onwards(leaseRequest) shouldBe Seq(result, prevResult)
    }

    "return 15021, 1 for purchase price of 500025, npv of 501945" in new PredefinedNPVSetup(501945, Some(15000)) {
      val leaseTaxDue = 19
      val premTaxDue = 15002
      val prevTax = 1
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 3, taxDue = 15000),
        SliceDetails(from = 500000, to = Some(925000), rate = 8, taxDue = 2),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )

      val prevSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 1),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      private val leaseRequest = leaseholdResidentialAddPropJuly20OnwardsRequestIsIndividual(500025, july2020EffectiveDate)

      private val result = leaseholdResidentialAddPropJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = Some("15,000"))
      private val prevResult = leaseholdResidentialJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, prevTax, prevSliceDetails, npv, asPreviousResult = true)

      service.leaseholdResidentialAddPropJuly20Onwards(leaseRequest) shouldBe Seq(result, prevResult)
    }

    "return 15021, 1 for purchase price of 500025, npv of 501945 last effective date" in new PredefinedNPVSetup(501945, Some(15000)) {
      val leaseTaxDue = 19
      val premTaxDue = 15002
      val prevTax = 1
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 3, taxDue = 15000),
        SliceDetails(from = 500000, to = Some(925000), rate = 8, taxDue = 2),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )

      val prevSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 1),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      private val leaseRequest = leaseholdResidentialAddPropJuly20OnwardsRequestIsIndividual(500025, march2021EffectiveDate)

      private val result = leaseholdResidentialAddPropJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = Some("15,000"))
      private val prevResult = leaseholdResidentialJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, prevTax, prevSliceDetails, npv, asPreviousResult = true)

      service.leaseholdResidentialAddPropJuly20Onwards(leaseRequest) shouldBe Seq(result, prevResult)
    }
  }

  //Additional property from Oct 24 Before April 25 UK resident
  "leaseholdResidentialAddPropOct24BeforeApril25" must {

    "return 15019, 103250 for purchase price of 1600000, npv of 3452920" in new PredefinedNPVSetup(3452920, Some(80000)) {
      val leaseTaxDue = 32029
      val premTaxDue = 183250
      val prevTax = 103250
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(250000), rate = 0, taxDue = 0),
        SliceDetails(from = 250000, to = None, rate = 1, taxDue = 32029)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(250000), rate = 5, taxDue = 12500),
        SliceDetails(from = 250000, to = Some(925000), rate = 10, taxDue = 67500),
        SliceDetails(from = 925000, to = Some(1500000), rate = 15, taxDue = 86250),
        SliceDetails(from = 1500000, to = None, rate = 17, taxDue = 17000)
      )

      val prevSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(250000), rate = 0, taxDue = 0),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 12000)
      )

      private val leaseRequest = leaseholdResidentialAddPropOct24BeforeApril25RequestIsIndividual(1600000, oct2024EffectiveDate)

      private val result = leaseholdResidentialAddPropJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = Some("80,000"))
      private val prevResult = leaseholdResidentialJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, prevTax, prevSliceDetails, npv, asPreviousResult = true)

      service.leaseholdResidentialAddPropOct24BeforeApril25(leaseRequest) shouldBe Seq(result, prevResult)
    }
  }

  //Additional property from Oct 24 Before April 25 NonUK resident
  "leaseholdResidentialAddPropOct24BeforeApril25NonUKRes" must {

    "return 103136, 88136 for purchase price of 300000, npv of 2737887" in new PredefinedNPVSetup(2737887, Some(15000)) {
      val leaseTaxDue = 79636
      val premTaxDue = 23500
      val prevTax = 8500
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(250000), rate = 2, taxDue = 5000),
        SliceDetails(from = 250000, to = None, rate = 3, taxDue = 74636)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(250000), rate = 7, taxDue = 17500),
        SliceDetails(from = 250000, to = Some(925000), rate = 12, taxDue = 6000),
        SliceDetails(from = 925000, to = Some(1500000), rate = 17, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 19, taxDue = 0)
      )

      val prevSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(250000), rate = 2, taxDue = 5000),
        SliceDetails(from = 250000, to = Some(925000), rate = 7, taxDue = 3500),
        SliceDetails(from = 925000, to = Some(1500000), rate = 12, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 14, taxDue = 0)
      )

      private val leaseRequest = leaseholdResidentialAddPropOct24BeforeApril25NonUKResRequestIsIndividual(300000, oct2024EffectiveDate)

      private val result = leaseholdResidentialAddPropOct24BeforeApril25NonUKResResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = Some("15,000"))
      private val prevResult = leaseholdResidentialAddPropOct24BeforeApril25NonUKResResultPrev(leaseTaxDue, leaseSliceDetails, prevTax, prevSliceDetails, npv, asPreviousResult = true)

      service.leaseholdResidentialOct24BeforeApr25NonUKResAddProp(leaseRequest) shouldBe Seq(result, prevResult)
    }
  }

  //Additional property April 25 onwards UK resident
  "leaseholdResidentialAddPropApril25Onwards" must {

    "return 219029, 139029 for purchase price of 1600000, npv of 3452920" in new PredefinedNPVSetup(3452920, Some(80000)) {
      val leaseTaxDue = 33279
      val premTaxDue = 185750
      val prevTax = 105750
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None, rate = 1, taxDue = 33279)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 5, taxDue = 6250),
        SliceDetails(from = 125000, to = Some(250000), rate = 7, taxDue = 8750),
        SliceDetails(from = 250000, to = Some(925000), rate = 10, taxDue = 67500),
        SliceDetails(from = 925000, to = Some(1500000), rate = 15, taxDue = 86250),
        SliceDetails(from = 1500000, to = None, rate = 17, taxDue = 17000)
      )

      val prevSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = Some(250000), rate = 2, taxDue = 2500),
        SliceDetails(from = 250000, to = Some(925000), rate = 5, taxDue = 33750),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 12000)
      )

      private val leaseRequest = leaseholdResidentialAddPropApril25OnwardsRequestIsIndividual(1600000, april2025EffectiveDate)

      private val result = leaseholdResidentialAddPropJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = Some("80,000"))
      private val prevResult = leaseholdResidentialJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, prevTax, prevSliceDetails, npv, asPreviousResult = true)

      service.leaseholdResidentialAddPropApril25Onwards(leaseRequest) shouldBe Seq(result, prevResult)
    }
  }

  //Additional property April 25 onwards NonUK resident
  "leaseholdResidentialAddPropApril25OnwardsNonUKRes" must {

    "return 106886, 91886 for purchase price of 300000, npv of 2737887" in new PredefinedNPVSetup(2737887, Some(15000)) {
      val leaseTaxDue = 80886
      val premTaxDue = 26000
      val prevTax = 11000
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 2, taxDue = 2500),
        SliceDetails(from = 125000, to = None, rate = 3, taxDue = 78386)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 7, taxDue = 8750),
        SliceDetails(from = 125000, to = Some(250000), rate = 9, taxDue = 11250),
        SliceDetails(from = 250000, to = Some(925000), rate = 12, taxDue = 6000),
        SliceDetails(from = 925000, to = Some(1500000), rate = 17, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 19, taxDue = 0)
      )

      val prevSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(125000), rate = 2, taxDue = 2500),
        SliceDetails(from = 125000, to = Some(250000), rate = 4, taxDue = 5000),
        SliceDetails(from = 250000, to = Some(925000), rate = 7, taxDue = 3500),
        SliceDetails(from = 925000, to = Some(1500000), rate = 12, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 14, taxDue = 0)
      )

      private val leaseRequest = leaseholdResidentialAddPropApril25OnwardsNonUKResRequestIsIndividual(300000, april2025EffectiveDate)

      private val result = leaseholdResidentialAddPropOct24BeforeApril25NonUKResResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = Some("15,000"))
      private val prevResult = leaseholdResidentialAddPropOct24BeforeApril25NonUKResResultPrev(leaseTaxDue, leaseSliceDetails, prevTax, prevSliceDetails, npv, asPreviousResult = true)

      service.leaseholdResidentialApr25OnwardsNonUKResAddProp(leaseRequest) shouldBe Seq(result, prevResult)
    }
  }

  "leaseholdResidentialJuly20Onwards" must {

    "return 19, 0 for purchase price of 499000, npv of 501945 as individual" in new PredefinedNPVSetup(501945) {
      val leaseTaxDue = 19
      val premTaxDue = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      private val res = leaseholdResidentialJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialJuly20Onwards(leaseholdResidentialJuly20OnwardsIndividualRequest(499000, july2020EffectiveDate)) shouldBe res
    }

    "return 19, 0 for purchase price of 500000, npv of 501945 as individual" in new PredefinedNPVSetup(501945) {
      val leaseTaxDue = 19
      val premTaxDue = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      private val res = leaseholdResidentialJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialJuly20Onwards(leaseholdResidentialJuly20OnwardsIndividualRequest(500000, july2020EffectiveDate)) shouldBe res
    }

    "return 19, 50 for purchase price of 501000, npv of 501945 as individual" in new PredefinedNPVSetup(501945) {
      val leaseTaxDue = 19
      val premTaxDue = 50
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 50),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      private val res = leaseholdResidentialJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialJuly20Onwards(leaseholdResidentialJuly20OnwardsIndividualRequest(501000, july2020EffectiveDate)) shouldBe res
    }

    "return 19, 50 for purchase price of 501000, npv of 501945 as individual with last effective date" in new PredefinedNPVSetup(501945) {
      val leaseTaxDue = 19
      val premTaxDue = 50
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = Some(925000), rate = 5, taxDue = 50),
        SliceDetails(from = 925000, to = Some(1500000), rate = 10, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 12, taxDue = 0)
      )

      private val res = leaseholdResidentialJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialJuly20Onwards(leaseholdResidentialJuly20OnwardsIndividualRequest(501000, march2021EffectiveDate)) shouldBe res
    }

    "return 19, 14970 for purchase price of 499000, npv of 501945 as company" in new PredefinedNPVSetup(501945) {
      val leaseTaxDue = 19
      val premTaxDue = 14970
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )

      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 3, taxDue = 14970),
        SliceDetails(from = 500000, to = Some(925000), rate = 8, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )

      private val res = leaseholdResidentialAddPropJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialAddPropJuly20Onwards(leaseholdResidentialJuly20OnwardsCompanyRequest(499000, july2020EffectiveDate)) shouldBe Seq(res)
    }

    "return 19, 15000 for purchase price of 500000, npv of 501945 as company" in new PredefinedNPVSetup(501945) {
      val leaseTaxDue = 19
      val premTaxDue = 15000
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )

      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 3, taxDue = 15000),
        SliceDetails(from = 500000, to = Some(925000), rate = 8, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )

      private val res = leaseholdResidentialAddPropJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialAddPropJuly20Onwards(leaseholdResidentialJuly20OnwardsCompanyRequest(500000, july2020EffectiveDate)) shouldBe Seq(res)
    }

    "return 19, 15000 for purchase price of 500000, npv of 501945 as company with last effective date" in new PredefinedNPVSetup(501945) {
      val leaseTaxDue = 19
      val premTaxDue = 15000
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )

      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 3, taxDue = 15000),
        SliceDetails(from = 500000, to = Some(925000), rate = 8, taxDue = 0),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )

      private val res = leaseholdResidentialAddPropJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialAddPropJuly20Onwards(leaseholdResidentialJuly20OnwardsCompanyRequest(500000, march2021EffectiveDate)) shouldBe Seq(res)
    }

    "return 19, 15080 for purchase price of 501000, npv of 501945 as company" in new PredefinedNPVSetup(501945) {
      val leaseTaxDue = 19
      val premTaxDue = 15080
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 0, taxDue = 0),
        SliceDetails(from = 500000, to = None, rate = 1, taxDue = 19)
      )

      val premSliceDetails = Seq(
        SliceDetails(from = 0, to = Some(500000), rate = 3, taxDue = 15000),
        SliceDetails(from = 500000, to = Some(925000), rate = 8, taxDue = 80),
        SliceDetails(from = 925000, to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None, rate = 15, taxDue = 0)
      )

      private val res = leaseholdResidentialAddPropJuly20OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialAddPropJuly20Onwards(leaseholdResidentialJuly20OnwardsCompanyRequest(501000, july2020EffectiveDate)) shouldBe Seq(res)
    }
  }

  "leaseholdResidentialNov17OnwardsFTB" must {
    val MAX_PREMIUM_FTB = 500000
    "return 0, 0 for purchase price of 299999, npv of 125000" in new PredefinedNPVSetup(125000) {
      val leaseTaxDue = 0
      val premTaxDue = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )

      private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialNov17OnwardsFTB(leaseholdResidentialNov17OnwardsFTBRequest(299999, nov2018EffectiveDate)) shouldBe res
    }

    "return 0, 0 for purchase price of 300000, npv of 125000" in new PredefinedNPVSetup(125000) {
      val leaseTaxDue = 0
      val premTaxDue = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )

      private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialNov17OnwardsFTB(leaseholdResidentialNov17OnwardsFTBRequest(300000, nov2018EffectiveDate)) shouldBe res
    }

    "return 0, 0 for purchase price of 300000, npv of 125000 with a date after 31 March 2021" in new PredefinedNPVSetup(125000) {
      val leaseTaxDue = 0
      val premTaxDue = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
      )

      private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialNov17OnwardsFTB(leaseholdResidentialNov17OnwardsFTBRequest(300000, april2021EffectiveEndDate)) shouldBe res
    }

    "return 0, 1 for purchase price of 300025, npv of 125000" in new PredefinedNPVSetup(125000) {
      val leaseTaxDue = 0
      val premTaxDue = 1
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 1)
      )

      private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialNov17OnwardsFTB(leaseholdResidentialNov17OnwardsFTBRequest(300025, nov2018EffectiveDate)) shouldBe res
    }

    "return 0, 9999 for purchase price of 499999, npv of 125000" in new PredefinedNPVSetup(125000) {
      val leaseTaxDue = 0
      val premTaxDue = 9999
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 9999)
      )

      private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialNov17OnwardsFTB(leaseholdResidentialNov17OnwardsFTBRequest(499999, nov2018EffectiveDate)) shouldBe res
    }

    "return 0, 10000 for purchase price of 500000, npv of 125000" in new PredefinedNPVSetup(125000) {
      val leaseTaxDue = 0
      val premTaxDue = 10000
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(300000), rate = 0, taxDue = 0),
        SliceDetails(from = 300000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 10000)
      )

      private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialNov17OnwardsFTB(leaseholdResidentialNov17OnwardsFTBRequest(500000, nov2018EffectiveDate)) shouldBe res
    }
  }

  "leaseholdResidentialSept22OnwardsFTB" when {
    val MAX_PREMIUM_FTB = 625000
    "a shared ownership scheme is not being used" must {
      "return 4395, 0 for purchase price of 424999, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 4395
        val premTaxDue = 0
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 4395)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
        )

        private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTB(leaseholdResidentialSept2022OnwardsFTBRequest(424999, sept2022EffectiveDate)) shouldBe res
      }

      "return 4395, 0 for purchase price of 425000, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 4395
        val premTaxDue = 0
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 4395)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
        )

        private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTB(leaseholdResidentialSept2022OnwardsFTBRequest(425000, sept2022EffectiveDate)) shouldBe res
      }

      "return 4395, 1 for purchase price of 425025, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 4395
        val premTaxDue = 1
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 4395)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 1)
        )

        private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTB(leaseholdResidentialSept2022OnwardsFTBRequest(425025, sept2022EffectiveDate)) shouldBe res
      }

      "return 4395, 9999 for purchase price of 624999, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 4395
        val premTaxDue = 9999
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 4395)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 9999)
        )

        private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTB(leaseholdResidentialSept2022OnwardsFTBRequest(624999, sept2022EffectiveDate)) shouldBe res
      }

      "return 4395, 10000 for purchase price of 625000, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 4395
        val premTaxDue = 10000
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 4395)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 10000)
        )

        private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTB(leaseholdResidentialSept2022OnwardsFTBRequest(625000, sept2022EffectiveDate)) shouldBe res
      }
    }

    "a shared ownership scheme is being used" must {
      "return 0, 0 for purchase price of 424999, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 0
        val premTaxDue = 0
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
        )

        private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTB(leaseholdResidentialSept2022OnwardsSharedOwnershipFTBRequest(424999, sept2022EffectiveDate)) shouldBe res
      }

      "return 0, 0 for purchase price of 425000, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 0
        val premTaxDue = 0
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
        )

        private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTB(leaseholdResidentialSept2022OnwardsSharedOwnershipFTBRequest(425000, sept2022EffectiveDate)) shouldBe res
      }

      "return 0, 1 for purchase price of 425025, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 0
        val premTaxDue = 1
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 1)
        )

        private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTB(leaseholdResidentialSept2022OnwardsSharedOwnershipFTBRequest(425025, sept2022EffectiveDate)) shouldBe res
      }

      "return 0, 9999 for purchase price of 624999, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 0
        val premTaxDue = 9999
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 9999)
        )

        private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTB(leaseholdResidentialSept2022OnwardsSharedOwnershipFTBRequest(624999, sept2022EffectiveDate)) shouldBe res
      }

      "return 0, 10000 for purchase price of 625000, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 0
        val premTaxDue = 10000
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 10000)
        )

        private val res = leaseholdResidentialNov17OnwardsFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTB(leaseholdResidentialSept2022OnwardsSharedOwnershipFTBRequest(625000, sept2022EffectiveDate)) shouldBe res
      }
    }
  }

  "leaseholdResidentialSept22OnwardsFTBNonUKRes" when {
    val MAX_PREMIUM_FTB = 625000

    "not using a shared ownership scheme" must {

      "return 18187, 8499 for purchase price of 424999, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 18187
        val premTaxDue = 8499
        val leaseTaxDueRefunded = 4395
        val premTaxDueRefunded = 0
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 2, taxDue = 5000),
          SliceDetails(from = 250000, to = None,         rate = 3, taxDue = 13187)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 2, taxDue = 8499),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 0)
        )
        val leaseSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 4395)
        )
        val premSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
        )

        private val res = leaseholdResidentialSept2022OnwardsNonUKFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, leaseTaxDueRefunded, leaseSliceRefundedDetails, premTaxDueRefunded, premSliceRefundedDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTBNonUKRes(leaseholdResidentialSept2022OnwardsFTBRequest(424999, sept2022EffectiveDate, Some(true))) shouldBe res
      }

      "return 18187, 8500 for purchase price of 425000, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 18187
        val premTaxDue = 8500
        val leaseTaxDueRefunded = 4395
        val premTaxDueRefunded = 0
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 2, taxDue = 5000),
          SliceDetails(from = 250000, to = None,         rate = 3, taxDue = 13187)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 2, taxDue = 8500),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 0)
        )
        val leaseSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 4395)
        )
        val premSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
        )

        private val res = leaseholdResidentialSept2022OnwardsNonUKFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, leaseTaxDueRefunded, leaseSliceRefundedDetails, premTaxDueRefunded, premSliceRefundedDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTBNonUKRes(leaseholdResidentialSept2022OnwardsFTBRequest(425000, sept2022EffectiveDate, Some(true))) shouldBe res
      }

      "return 18187, 8501 for purchase price of 425025, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 18187
        val premTaxDue = 8501
        val leaseTaxDueRefunded = 4395
        val premTaxDueRefunded = 1
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 2, taxDue = 5000),
          SliceDetails(from = 250000, to = None,         rate = 3, taxDue = 13187)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 2, taxDue = 8500),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 1)
        )
        val leaseSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 4395)
        )
        val premSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 1)
        )

        private val res = leaseholdResidentialSept2022OnwardsNonUKFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, leaseTaxDueRefunded, leaseSliceRefundedDetails, premTaxDueRefunded, premSliceRefundedDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTBNonUKRes(leaseholdResidentialSept2022OnwardsFTBRequest(425025, sept2022EffectiveDate, Some(true))) shouldBe res
      }

      "return 18187, 22499 for purchase price of 624999, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 18187
        val premTaxDue = 22499
        val leaseTaxDueRefunded = 4395
        val premTaxDueRefunded = 9999
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 2, taxDue = 5000),
          SliceDetails(from = 250000, to = None,         rate = 3, taxDue = 13187)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 2, taxDue = 8500),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 13999)
        )
        val leaseSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 4395)
        )
        val premSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 9999)
        )

        private val res = leaseholdResidentialSept2022OnwardsNonUKFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, leaseTaxDueRefunded, leaseSliceRefundedDetails, premTaxDueRefunded, premSliceRefundedDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTBNonUKRes(leaseholdResidentialSept2022OnwardsFTBRequest(624999, sept2022EffectiveDate, Some(true))) shouldBe res
      }

      "return 4395, 10000 for purchase price of 625000, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 18187
        val premTaxDue = 22500
        val leaseTaxDueRefunded = 4395
        val premTaxDueRefunded = 10000
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 2, taxDue = 5000),
          SliceDetails(from = 250000, to = None,         rate = 3, taxDue = 13187)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 2, taxDue = 8500),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 14000)
        )
        val leaseSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 1, taxDue = 4395)
        )
        val premSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 10000)
        )

        private val res = leaseholdResidentialSept2022OnwardsNonUKFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, leaseTaxDueRefunded, leaseSliceRefundedDetails, premTaxDueRefunded, premSliceRefundedDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTBNonUKRes(leaseholdResidentialSept2022OnwardsFTBRequest(625000, sept2022EffectiveDate, Some(true))) shouldBe res
      }
    }

    "using a shared ownership scheme" must {
      "return 0, 8499 for purchase price of 424999, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 0
        val premTaxDue = 8499
        val leaseTaxDueRefunded = 0
        val premTaxDueRefunded = 0
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 2, taxDue = 8499),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 0)
        )
        val leaseSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
        )

        private val res = leaseholdResidentialSept2022OnwardsNonUKFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, leaseTaxDueRefunded, leaseSliceRefundedDetails, premTaxDueRefunded, premSliceRefundedDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTBNonUKRes(leaseholdResidentialSept2022OnwardsSharedOwnershipFTBRequest(424999, sept2022EffectiveDate, Some(true))) shouldBe res
      }

      "return 0, 8500 for purchase price of 425000, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 0
        val premTaxDue = 8500
        val leaseTaxDueRefunded = 0
        val premTaxDueRefunded = 0
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 2, taxDue = 8500),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 0)
        )
        val leaseSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 0)
        )

        private val res = leaseholdResidentialSept2022OnwardsNonUKFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, leaseTaxDueRefunded, leaseSliceRefundedDetails, premTaxDueRefunded, premSliceRefundedDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTBNonUKRes(leaseholdResidentialSept2022OnwardsSharedOwnershipFTBRequest(425000, sept2022EffectiveDate, Some(true))) shouldBe res
      }

      "return 0, 8501 for purchase price of 425025, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 0
        val premTaxDue = 8501
        val leaseTaxDueRefunded = 0
        val premTaxDueRefunded = 1
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 2, taxDue = 8500),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 1)
        )
        val leaseSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 1)
        )

        private val res = leaseholdResidentialSept2022OnwardsNonUKFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, leaseTaxDueRefunded, leaseSliceRefundedDetails, premTaxDueRefunded, premSliceRefundedDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTBNonUKRes(leaseholdResidentialSept2022OnwardsSharedOwnershipFTBRequest(425025, sept2022EffectiveDate, Some(true))) shouldBe res
      }

      "return 0, 22499 for purchase price of 624999, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 0
        val premTaxDue = 22499
        val leaseTaxDueRefunded = 0
        val premTaxDueRefunded = 9999
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 2, taxDue = 8500),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 13999)
        )
        val leaseSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 9999)
        )

        private val res = leaseholdResidentialSept2022OnwardsNonUKFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, leaseTaxDueRefunded, leaseSliceRefundedDetails, premTaxDueRefunded, premSliceRefundedDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTBNonUKRes(leaseholdResidentialSept2022OnwardsSharedOwnershipFTBRequest(624999, sept2022EffectiveDate, Some(true))) shouldBe res
      }

      "return 0, 10000 for purchase price of 625000, npv of 689579" in new PredefinedNPVSetup(689579) {
        val leaseTaxDue = 0
        val premTaxDue = 22500
        val leaseTaxDueRefunded = 0
        val premTaxDueRefunded = 10000
        val leaseSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 2, taxDue = 8500),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 7, taxDue = 14000)
        )
        val leaseSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(250000), rate = 0, taxDue = 0),
          SliceDetails(from = 250000, to = None,         rate = 0, taxDue = 0)
        )
        val premSliceRefundedDetails = Seq(
          SliceDetails(from = 0,      to = Some(425000), rate = 0, taxDue = 0),
          SliceDetails(from = 425000, to = Some(MAX_PREMIUM_FTB), rate = 5, taxDue = 10000)
        )

        private val res = leaseholdResidentialSept2022OnwardsNonUKFTBResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, leaseTaxDueRefunded, leaseSliceRefundedDetails, premTaxDueRefunded, premSliceRefundedDetails, npv)
        service.leaseholdResidentialSept22OnwardsFTBNonUKRes(leaseholdResidentialSept2022OnwardsSharedOwnershipFTBRequest(625000, sept2022EffectiveDate, Some(true))) shouldBe res
      }
    }
  }

  "leaseholdResidentialAddPropApr16Onwards" must{

    "return 0, 1 for purchase price of 40000, npv of 125000" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 1200

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 1200),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 0),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )


      private val leaseRequestIndividual = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(40000, dec2017EffectiveDate)
      private val leaseRequestCompany = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(40000, dec2017EffectiveDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val outputIndividual: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestIndividual)
      private val outputCompany: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestCompany)

      outputIndividual.head shouldBe result
      outputCompany.head shouldBe result
      outputIndividual.tail.isEmpty shouldBe false
      outputCompany.tail.isEmpty shouldBe false
    }

    "return 1, 3750 for purchase price of 125000, npv of 125100" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 3750

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 0),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )

      private val leaseRequest = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(125000, dec2017EffectiveDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val output: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequest)

      output.head shouldBe result
      output.tail.isEmpty shouldBe false

    }

    "return 1, 3752 for purchase price of 125050, npv of 125100" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 3752

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 2),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )

      private val leaseRequest = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(125050, dec2017EffectiveDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val output: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequest)

      output.head shouldBe result
      output.tail.isEmpty shouldBe false
    }

    "return 1, 10000 for purchase price of 250000, npv of 125100" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 10000

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 6250),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )

      private val leaseRequestIndividual = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(250000,dec2017EffectiveDate)
      private val leaseRequestCompany = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(250000, dec2017EffectiveDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val outputIndividual: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestIndividual)
      private val outputCompany: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestCompany)

      outputIndividual.head shouldBe result
      outputCompany.head shouldBe result
      outputIndividual.tail.isEmpty shouldBe false
      outputCompany.tail.isEmpty shouldBe false
    }

    "return 1, 10000 for purchase price of 250000, npv of 125100 with a date after 31 March 2021" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 10000

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 6250),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 0),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )

      private val leaseRequestIndividual = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(250000, april2021EffectiveEndDate)
      private val leaseRequestCompany = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(250000, april2021EffectiveEndDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None, afterMarch2021 = true)
      private val outputIndividual: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestIndividual)
      private val outputCompany: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestCompany)

      outputIndividual.head shouldBe result
      outputCompany.head shouldBe result
      outputIndividual.tail.isEmpty shouldBe false
      outputCompany.tail.isEmpty shouldBe true
    }

    "return 1, 10001 for purchase price of 250020, npv of 125100" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 10001

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 6250),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 1),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )

      private val leaseRequestIndividual = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(250020, dec2017EffectiveDate)
      private val leaseRequestCompany = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(250020, dec2017EffectiveDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val outputIndividual: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestIndividual)
      private val outputCompany: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestCompany)

      outputIndividual.head shouldBe result
      outputCompany.head shouldBe result
      outputIndividual.tail.isEmpty shouldBe false
      outputCompany.tail.isEmpty shouldBe false
    }

    "return 1, 30000 for purchase price of 500000, npv of 125100" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 30000

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 6250),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 20000),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )

      private val leaseRequestIndividual = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(500000, dec2017EffectiveDate)
      private val leaseRequestCompany = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(500000, dec2017EffectiveDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val outputIndividual: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestIndividual)
      private val outputCompany: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestCompany)

      outputIndividual.head shouldBe result
      outputCompany.head shouldBe result
      outputIndividual.tail.isEmpty shouldBe false
      outputCompany.tail.isEmpty shouldBe false
    }

    "return 1, 30005 for purchase price of 500050, npv of 125100" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 30004

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 6250),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 20004),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )

      private val leaseRequestIndividual = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(500050, dec2017EffectiveDate)
      private val leaseRequestCompany = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(500050, dec2017EffectiveDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val outputIndividual: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestIndividual)
      private val outputCompany: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestCompany)

      outputIndividual.head shouldBe result
      outputCompany.head shouldBe result
      outputIndividual.tail.isEmpty shouldBe false
      outputCompany.tail.isEmpty shouldBe false
    }

    "return 1, 64000 for purchase price of 925000, npv of 125100" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 64000

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 6250),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 54000),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 0),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )

      private val leaseRequestIndividual = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(925000, dec2017EffectiveDate)
      private val leaseRequestCompany = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(925000, dec2017EffectiveDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val outputIndividual: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestIndividual)
      private val outputCompany: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestCompany)

      outputIndividual.head shouldBe result
      outputCompany.head shouldBe result
      outputIndividual.tail.isEmpty shouldBe false
      outputCompany.tail.isEmpty shouldBe false
    }

    "return 1, 64001 for purchase price of 925010, npv of 125100" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 64001

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 6250),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 54000),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 1),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )

      private val leaseRequestIndividual = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(925010, dec2017EffectiveDate)
      private val leaseRequestCompany = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(925010, dec2017EffectiveDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val outputIndividual: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestIndividual)
      private val outputCompany: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestCompany)

      outputIndividual.head shouldBe result
      outputCompany.head shouldBe result
      outputIndividual.tail.isEmpty shouldBe false
      outputCompany.tail.isEmpty shouldBe false
    }

    "return 1, 138750 for purchase price of 1500000, npv of 125100" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 138750

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 6250),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 54000),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 74750),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 0)
      )

      private val leaseRequestIndividual = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(1500000, dec2017EffectiveDate)
      private val leaseRequestCompany = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(1500000, dec2017EffectiveDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val outputIndividual: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestIndividual)
      private val outputCompany: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestCompany)

      outputIndividual.head shouldBe result
      outputCompany.head shouldBe result
      outputIndividual.tail.isEmpty shouldBe false
      outputCompany.tail.isEmpty shouldBe false
    }

    "return 1, 138750 for purchase price of 1500009, npv of 125100" in new PredefinedNPVSetup(125100, None){
      val leaseTaxDue = 1
      val premTaxDue = 138751

      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 6250),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 54000),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 74750),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 1)
      )

      private val leaseRequestIndividual = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(1500009, dec2017EffectiveDate)
      private val leaseRequestCompany = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(1500009, dec2017EffectiveDate)
      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val outputIndividual: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestIndividual)
      private val outputCompany: Seq[Result] = service.leaseholdResidentialAddPropApr16Onwards(leaseRequestCompany)

      outputIndividual.head shouldBe result
      outputCompany.head shouldBe result
      outputIndividual.tail.isEmpty shouldBe false
      outputCompany.tail.isEmpty shouldBe false
    }

    "return [1250, 138765, 1250, 93762] for purchase price of 1500100, npv of 250000 and is not an individual." in new PredefinedNPVSetup(250000, None){
      val leaseTaxDue = 1250
      val premTaxDue = 138765
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1250)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 6250),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 54000),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 74750),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 15)
      )

      val prevLeaseTaxDue = 1250
      val prevPremTaxDue = 93762
      val prevSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 33750),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 12)
      )

      private val leaseRequest = leaseholdResidentialAddPropApr16OnwardsRequestNotIndividual(1500100, dec2017EffectiveDate)

      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = None)
      private val prevResult = leaseholdResidentialDec14OnwardsResult(prevLeaseTaxDue, leaseSliceDetails, prevPremTaxDue, prevSliceDetails, npv, asPreviousResult = true)

      service.leaseholdResidentialAddPropApr16Onwards(leaseRequest) shouldBe Seq(result,prevResult)
    }

    "return [1250, 138765, 1250, 93762] for purchase price of 1500100, npv of 250000 and is an individual." in new PredefinedNPVSetup(250000, Some(4000)){
      val leaseTaxDue = 1250
      val premTaxDue = 138765
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 1250)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 3,  taxDue = 3750),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 5,  taxDue = 6250),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 8,  taxDue = 54000),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 13, taxDue = 74750),
        SliceDetails(from = 1500000, to = None,          rate = 15, taxDue = 15)
      )

      val prevLeaseTaxDue = 1250
      val prevPremTaxDue = 93762
      val prevSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(125000),  rate = 0,  taxDue = 0),
        SliceDetails(from = 125000,  to = Some(250000),  rate = 2,  taxDue = 2500),
        SliceDetails(from = 250000,  to = Some(925000),  rate = 5,  taxDue = 33750),
        SliceDetails(from = 925000,  to = Some(1500000), rate = 10, taxDue = 57500),
        SliceDetails(from = 1500000, to = None,          rate = 12, taxDue = 12)
      )

      private val leaseRequest = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(1500100, dec2017EffectiveDate)

      private val result = leaseholdResidentialAddPropApr16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, resHintAmount = Some("4,000"))
      private val prevResult = leaseholdResidentialDec14OnwardsResult(prevLeaseTaxDue, leaseSliceDetails, prevPremTaxDue, prevSliceDetails, npv, asPreviousResult = true)

      service.leaseholdResidentialAddPropApr16Onwards(leaseRequest) shouldBe Seq(result,prevResult)
    }

    "throw the correct exception" when {
      class testPrevErrorSetup(){
        protected val npv: Int = 1000
        val service: LeaseholdCalculationService = new LeaseholdCalculationService(
          new BaseCalculationService{
            override def calculateNPV(leaseDetails: LeaseDetails): BigDecimal = npv
          },
          new RefundEntitlementService {
            override def calculateRefundEntitlement(premiumResultTaxDue: BigDecimal, prevResultTax: Int, reqPropertyDetails: Option[PropertyDetails]): Option[Int] = Some(1000)
          }
        ) {
          override def leaseholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None, nonUKRes: Boolean = false): Result = {
            Result(
              totalTax = 1000,
              npv = Some(npv),
              taxCalcs = Seq(
                CalculationDetails(
                  taxType = TaxTypes.rent,
                  calcType = CalcTypes.slice,
                  taxDue = 1000
                )
              )
            )
          }
        }
      }
      "the previous calculation doesn't return any lease calculation details" in new testPrevErrorSetup {
        private val req = leaseholdResidentialAddPropApr16OnwardsRequestIsIndividual(1500100, dec2017EffectiveDate)
        the[RequiredValueNotDefinedException] thrownBy
          service.leaseholdResidentialAddPropApr16Onwards(req) should
          have message "[LeaseholdCalculationService] [leaseholdResidentialAddPropApr16Onwards] - " +
          "Premium result not defined in previous calculation"
      }
    }
  }

  "leaseholdResidentialDec14Onwards" must {

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

      private val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(125000, feb2017EffectiveDate)) shouldBe res
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

      private val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(125050, feb2017EffectiveDate)) shouldBe res
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

      private val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(250000, feb2017EffectiveDate)) shouldBe res
    }

    "return 2500, 0 for purchase price of 250000, npv of 125000 with a date after March 31 2021" in new PredefinedNPVSetup(125000) {
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

      private val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv, afterMarch2021 = true)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(250000, april2021EffectiveEndDate)) shouldBe res
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

      private val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(250020, feb2017EffectiveDate)) shouldBe res
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

      private val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(925000, feb2017EffectiveDate)) shouldBe res
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

      private val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(925010, feb2017EffectiveDate)) shouldBe res
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

      private val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(1500000, feb2017EffectiveDate)) shouldBe res
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

      private val res = leaseholdResidentialDec14OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdResidentialDec14Onwards(leaseholdResidentialDec14OnwardsRequest(1500100, feb2017EffectiveDate)) shouldBe res
    }
  }

  "leaseholdResidentialMar12toDec14" must {

    "return 0, 0 for purchase price of 125000, npv of 125000" in new PredefinedNPVSetup(125000) {
      val leaseTaxDue = 0
      val premTaxDue = 0
      val premRate = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(125000), rate = 0, taxDue = 0),
        SliceDetails(from = 125000, to = None,         rate = 1, taxDue = 0)
      )

      private val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
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

      private val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
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

      private val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
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

      private val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
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

      private val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
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

      private val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
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

      private val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
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

      private val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
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

      private val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
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

      private val res = leaseholdResidentialMar12toDec14Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdResidentialMar12toDec14(leaseholdResidentialMar12toDec14Request(2000001)) shouldBe res
    }
  }

  "leaseholdNonResidentialMar16Onwards" must {
    "return 0, 0 for premium of 149000, npv of 150000, prevCalc is FALSE (exchanged contracts post March 2016)" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue, premTaxDue = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(150000),  rate = 0, taxDue = 0),
        SliceDetails(from = 150000,  to = Some(5000000), rate = 1, taxDue = 0),
        SliceDetails(from = 5000000, to = None,          rate = 2, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000,  to = Some(250000), rate = 2, taxDue = 0),
        SliceDetails(from = 250000,  to = None,         rate = 5, taxDue = 0)
      )
      private val res = leaseholdNonResidentialMar16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      service.leaseholdNonResidentialMar16Onwards(leaseholdNonResidentialMar16OnwardsRequest(149000, exchangedPreMarch2016 = false)) shouldBe Seq(res)
    }

    "return (1, 1), (1500, 1) for premium of 150050, npv of 150100, prevCalc is TRUE" in new PredefinedNPVSetup(150100) {
      val leaseTaxDue = 1
      val premTaxDue = 1
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(150000),  rate = 0, taxDue = 0),
        SliceDetails(from = 150000,  to = Some(5000000), rate = 1, taxDue = 1),
        SliceDetails(from = 5000000, to = None,          rate = 2, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000,  to = Some(250000), rate = 2, taxDue = 1),
        SliceDetails(from = 250000,  to = None,         rate = 5, taxDue = 0)
      )

      val prevLeaseTaxDue = 1
      val prevPremTaxDue = 1500
      val prevPremRate = 1
      val prevLeaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = None,         rate = 1, taxDue = 1)
      )

      private val res = leaseholdNonResidentialMar16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      private val prevRes = leaseholdNonResidentialMar12toMar16PrevResult(prevLeaseTaxDue, prevLeaseSliceDetails, prevPremTaxDue, prevPremRate, npv)
      service.leaseholdNonResidentialMar16Onwards(leaseholdNonResidentialMar16OnwardsRequest(150050)) shouldBe Seq(res, prevRes)
    }
    "return (2000, 48500), (2500, 48500) for premium of 250000, npv of 5000000, prevCalc is TRUE" in new PredefinedNPVSetup(5000000) {
      val leaseTaxDue = 48500
      val premTaxDue = 2000
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(150000),  rate = 0, taxDue = 0),
        SliceDetails(from = 150000,  to = Some(5000000), rate = 1, taxDue = 48500),
        SliceDetails(from = 5000000, to = None,          rate = 2, taxDue = 0)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000,  to = Some(250000), rate = 2, taxDue = 2000),
        SliceDetails(from = 250000,  to = None,         rate = 5, taxDue = 0)
      )

      val prevLeaseTaxDue = 48500
      val prevPremTaxDue = 2500
      val prevPremRate = 1
      val prevLeaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = None,         rate = 1, taxDue = 48500)
      )

      private val res = leaseholdNonResidentialMar16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      private val prevRes = leaseholdNonResidentialMar12toMar16PrevResult(prevLeaseTaxDue, prevLeaseSliceDetails, prevPremTaxDue, prevPremRate, npv)
      service.leaseholdNonResidentialMar16Onwards(leaseholdNonResidentialMar16OnwardsRequest(250000)) shouldBe Seq(res, prevRes)
    }
    "return (2001, 48501), (7500, 48500) for premium of 250020, npv of 5000050, prevCalc is TRUE" in new PredefinedNPVSetup(5000050) {
      val leaseTaxDue = 48501
      val premTaxDue = 2001
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(150000),  rate = 0, taxDue = 0),
        SliceDetails(from = 150000,  to = Some(5000000), rate = 1, taxDue = 48500),
        SliceDetails(from = 5000000, to = None,          rate = 2, taxDue = 1)
      )
      val premSliceDetails = Seq(
        SliceDetails(from = 0,       to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000,  to = Some(250000), rate = 2, taxDue = 2000),
        SliceDetails(from = 250000,  to = None,         rate = 5, taxDue = 1)
      )

      val prevLeaseTaxDue = 48500
      val prevPremTaxDue = 7500
      val prevPremRate = 3
      val prevLeaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = None,         rate = 1, taxDue = 48500)
      )

      private val res = leaseholdNonResidentialMar16OnwardsResult(leaseTaxDue, leaseSliceDetails, premTaxDue, premSliceDetails, npv)
      private val prevRes = leaseholdNonResidentialMar12toMar16PrevResult(prevLeaseTaxDue, prevLeaseSliceDetails, prevPremTaxDue, prevPremRate, npv)
      service.leaseholdNonResidentialMar16Onwards(leaseholdNonResidentialMar16OnwardsRequest(250020)) shouldBe Seq(res, prevRes)
    }
  }

  "leaseholdNonResidentialMar12toMar16" must {
    "return 0, 0 for purchase price of 149000, npv of 150000 and zeroRate is TRUE" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue, premTaxDue = 0
      val premRate = 0
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = None,         rate = 1, taxDue = 0)
      )

      private val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(149000)) shouldBe res
    }
    "return 1490, 0 for purchase price of 149000, npv of 150000 and zeroRate is FALSE (because year 2 rent > £2000)" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue = 0
      val premTaxDue = 1490
      val premRate = 1
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = None,         rate = 1, taxDue = 0)
      )

      private val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(149000, year2Rent = 2001)) shouldBe res
    }
    "return 2500, 0 for purchase price of 250000, npv of 150000 and zeroRate is FALSE (because premium > £150000)" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue = 0
      val premTaxDue = 2500
      val premRate = 1
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = None,         rate = 1, taxDue = 0)
      )

      private val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(250000)) shouldBe res
    }
    "return 7500, 0 for purchase price of 250001, npv of 150000 and zeroRate is FALSE (because premium > £150000)" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue = 0
      val premTaxDue = 7500
      val premRate = 3
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = None,         rate = 1, taxDue = 0)
      )

      private val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(250001)) shouldBe res
    }
    "return 15000, 0 for purchase price of 500000, npv of 150000 and zeroRate is FALSE (because premium > £150000)" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue = 0
      val premTaxDue = 15000
      val premRate = 3
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = None,         rate = 1, taxDue = 0)
      )

      private val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(500000)) shouldBe res
    }
    "return 20000, 0 for purchase price of 500001, npv of 150000 and zeroRate is FALSE (because premium > £150000)" in new PredefinedNPVSetup(150000) {
      val leaseTaxDue = 0
      val premTaxDue = 20000
      val premRate = 4
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = None,         rate = 1, taxDue = 0)
      )

      private val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(500001)) shouldBe res
    }

    "return 20000, 1 for purchase price of 500001, npv of 150100 and zeroRate is FALSE (because premium > £150000)" in new PredefinedNPVSetup(150100) {
      val leaseTaxDue = 1
      val premTaxDue = 20000
      val premRate = 4
      val leaseSliceDetails = Seq(
        SliceDetails(from = 0,      to = Some(150000), rate = 0, taxDue = 0),
        SliceDetails(from = 150000, to = None,         rate = 1, taxDue = 1)
      )

      private val res = leaseholdNonResidentialMar12toMar16Result(leaseTaxDue, leaseSliceDetails, premTaxDue, premRate, npv)
      service.leaseholdNonResidentialMar12toMar16(leaseholdNonResidentialMar12toMar16Request(500001)) shouldBe res
    }
  }

  "eligibleForZeroRate" must {

    def testRequest(
                     premium: BigDecimal = 150000,
                     year2Rent: BigDecimal = 200,
                     relevantRent: BigDecimal = 200
                   ) = Request(
      holdingType = HoldingTypes.leasehold,
      propertyType = PropertyTypes.nonResidential,
      effectiveDate = LocalDate.of(2017, 1, 13),
      nonUKResident = None,
      premium = premium,
      highestRent = 1000,
      propertyDetails = None,
      leaseDetails = Some(leaseDetailsWithYear2Rent(year2Rent)),
      relevantRentDetails = Some(RelevantRentDetails(
        exchangedContractsBeforeMar16 = Some(true),
        contractChangedSinceMar16 = Some(false),
        relevantRent = Some(relevantRent)
      )),
      taxReliefDetails = None,
      firstTimeBuyer = None
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
          have message "[LeaseholdCalculationService] [eligibleForZeroRate] - relevant rent details not defined"
      }
      "premium is under £150,000, all rents are below £2,000 but there is no relevant rent in the relevant rent details" in new Setup {
        private val request = testRequest(premium = 149999).copy(relevantRentDetails = Some(RelevantRentDetails(
          exchangedContractsBeforeMar16 = Some(true),
          contractChangedSinceMar16 = Some(false),
          relevantRent = None
        )))
        the[RequiredValueNotDefinedException] thrownBy
          service.eligibleForZeroRate(request) should
          have message "[LeaseholdCalculationService] [eligibleForZeroRate] - relevant rent amount not defined"
      }
    }
  }

  "nonResPrevCalcRequired" must {

    def testRequest(
                     premium: BigDecimal = 150000,
                     year2Rent: BigDecimal = 200,
                     relevantRent: BigDecimal = 200
                   ) = Request(
      holdingType = HoldingTypes.leasehold,
      propertyType = PropertyTypes.nonResidential,
      effectiveDate = LocalDate.of(2017, 1, 13),
      nonUKResident = None,
      premium = premium,
      highestRent = 1000,
      propertyDetails = None,
      leaseDetails = Some(leaseDetailsWithYear2Rent(year2Rent)),
      relevantRentDetails = Some(RelevantRentDetails(
        exchangedContractsBeforeMar16 = Some(true),
        contractChangedSinceMar16 = Some(false),
        relevantRent = Some(relevantRent)
      )),
      taxReliefDetails = None,
      firstTimeBuyer = None
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
          have message "[LeaseholdCalculationService] [nonResPrevCalcRequired] - relevant rent details not defined"
      }
    }
  }

  "calculating leasehold FreePortRelief  for FreeportsTaxSiteRelief or InvestmentZonesTaxSiteRelief" must {
    "return zero tax response for all residential or non-residential and not linked" in  new PredefinedNPVSetup(50126) {
      private val calcDetails: Seq[CalculationDetails] = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slab,
          taxDue = 0,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          rate = Some(0),
          slices = None
        ),
        CalculationDetails(
          taxType = TaxTypes.rent,
          calcType = CalcTypes.slab,
          taxDue = 0,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          rate = Some(0),
          slices = None
        )
      )

      val expectedRes: Result = Result(
        totalTax = 0,
        resultHeading = Some(RESULT_HEADING_TAX_RELEIF),
        resultHint = None,
        npv = Some(npv),
        taxCalcs = calcDetails
      )
      val res: CalculationResponse = service.zeroRateLeaseHoldFreePortRelief(Some(testLeaseDetails))
      res shouldBe CalculationResponse(Seq(expectedRes))
    }
  }

}
