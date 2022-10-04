/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package services

import java.time.LocalDate
import enums.{CalcTypes, HoldingTypes, PropertyTypes, TaxTypes}
import exceptions.InvalidDateException
import models.{CalculationDetails, CalculationResponse, Result, _}
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec

class CalculationServiceSpec extends PlaySpec with MockFactory {

  val april2021EffectiveDate: LocalDate = LocalDate.of(2021, 4, 1)
  val july2020EffectiveDate: LocalDate = LocalDate.of(2020, 7, 8)
  val july2021EffectiveDate: LocalDate = LocalDate.of(2021, 7, 1)
  val sept2022EffectiveDate: LocalDate = LocalDate.of(2022, 9, 22)
  val mockLeaseholdCalculationService: LeaseholdCalculationService = mock[LeaseholdCalculationService]
  val mockFreeholdCalculationService: FreeholdCalculationService = mock[FreeholdCalculationService]
  val mockAdditionalPropertyService: AdditionalPropertyService = mock[AdditionalPropertyService]
  val testCalculationService = new CalculationService(
    mockLeaseholdCalculationService,
    mockFreeholdCalculationService,
    mockAdditionalPropertyService
  )

  "CalculateTax" must {
    val calcDetails = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slab,
      taxDue = 0,
      detailHeading = None,
      bandHeading = None,
      detailFooter = None,
      rate = None,
      slices = None
    )

    def createResult(msg: String) = Result(
      totalTax = 0,
      resultHeading = Some(msg),
      resultHint = None,
      npv = None,
      taxCalcs = Seq(calcDetails)
    )

    def createResultInSeq(msg: String) = Seq(
      createResult(msg)
    )

    def createRequest(hType: HoldingTypes.Value, pType: PropertyTypes.Value, eDate: LocalDate) =  Request(
      holdingType = hType,
      propertyType = pType,
      effectiveDate = eDate,
      nonUKResident = None,
      premium = BigDecimal(0),
      highestRent = BigDecimal(0),
      propertyDetails = None,
      leaseDetails = None,
      relevantRentDetails = None,
      firstTimeBuyer = None
    )

    def createRequestWithPropDetails(hType: HoldingTypes.Value, pType: PropertyTypes.Value, eDate: LocalDate, premiumAmount: BigDecimal= 0, nonUKResident: Option[Boolean] = None) =  Request(
      holdingType = hType,
      propertyType = pType,
      effectiveDate = eDate,
      nonUKResident = nonUKResident,
      premium = premiumAmount,
      highestRent = BigDecimal(0),
      propertyDetails =  Some(
        PropertyDetails(
        individual = true,
        twoOrMoreProperties = Some(false),
        replaceMainResidence = Some(true),
        sharedOwnership = None,
        currentValue = None
        )
      ),
      leaseDetails = None,
      relevantRentDetails = None,
      firstTimeBuyer = Some(true)
    )

    "select the freeholdResidential function for March2012 to December2014" when {
      "given a request with an effective date of 3/12/2014" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2014, 12, 3))
        val result = createResult("freeholdResidential, March2012 to December2014")

        (mockFreeholdCalculationService.freeholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 22/3/2012" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2012, 3, 22))
        val result = createResult("freeholdResidential, March2012 to December2014")

        (mockFreeholdCalculationService.freeholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the freeholdNonResidential function for March2016 onwards" when {
      "given a request with an effective date of 1/1/2017" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.nonResidential, LocalDate.of(2017, 1, 1))
        val result = createResultInSeq("freeholdNonResidential, March2016 onwards")

        (mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 17/3/2016" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.nonResidential, LocalDate.of(2016, 3, 17))
        val result = createResultInSeq("freeholdNonResidential, March2016 onwards")

        (mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }
    }

    "select the freeholdNonResidential function for March2012 to March2016" when {
      "given a request with an effective date of 16/3/2016" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.nonResidential, LocalDate.of(2016, 3, 16))
        val result = createResult("freeholdNonResidential, March2012 to March2016")

        (mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/3/2011" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.nonResidential, LocalDate.of(2011, 3, 4))
        val result = createResult("freeholdNonResidential, March2012 to March2016")

        (mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the freeholdResidential November2017 first time buyer function" when {
      "given a request with an effective date of 22/11/2017 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2017, 11, 22))
        val result = createResult("freeholdResidential, November2017 onwards")

        (mockFreeholdCalculationService.freeholdResidentialNov17OnwardsFTB _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 23/11/2017 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2017, 11, 23))
        val result = createResult("freeholdResidential, November2017 onwards")

        (mockFreeholdCalculationService.freeholdResidentialNov17OnwardsFTB _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 30/11/2019 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2019, 11, 30))
        val result = createResult("freeholdResidential, November2017 onwards")

        (mockFreeholdCalculationService.freeholdResidentialNov17OnwardsFTB _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the freeholdResidential additional property function" when {
      "given a request with an effective date of 08/07/2020,the user is an individual without twoOrMoreProperties but the premium is >500000" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, july2020EffectiveDate, BigDecimal(500001))
        val result = createResultInSeq("freeholdResidential, July2020 onwards")

        (mockFreeholdCalculationService.freeholdResidentialAddPropJuly20Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 08/7/2020 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, july2020EffectiveDate)
        val result = createResultInSeq("freeholdResidential, July2020 onwards")

        (mockFreeholdCalculationService.freeholdResidentialAddPropJuly20Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 11/7/2020 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2020, 7, 11))
        val result = createResultInSeq("freeholdResidential, July2020 onwards")

        (mockFreeholdCalculationService.freeholdResidentialAddPropJuly20Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 31/3/2021 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2021, 3, 31))
        val result = createResultInSeq("freeholdResidential, July2020 onwards")

        (mockFreeholdCalculationService.freeholdResidentialAddPropJuly20Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 1/10/2021 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2021, 10, 1))
        val result = createResultInSeq("freeholdResidential, April2016 onwards")

        (mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 30/11/2019,the user is an individual without twoOrMoreProperties but the premium is >500000" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2019, 11, 30), BigDecimal(500001))
        val result = createResultInSeq("freeholdResidential, April2016 onwards")

        (mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 2/4/2016 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2016, 4, 2))
        val result = createResultInSeq("freeholdResidential, April2016 onwards")

        (mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

         testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 1/4/2016 and additional property eligibility check returns true" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2016, 4, 1))
        val result = createResultInSeq("freeholdResidential, April2016 onwards")

        (mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

         testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }
    }

    "select the freeholdResidential function for July 2021" when {
      "given a request with an effective date of 1/7/2021" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2021, 7, 1))
        val result = createResult("freeholdResidential, July 2021")

        (mockFreeholdCalculationService.freeholdResidentialJuly21Onwards _)
          .expects(testRequest, false)
          .returns(result)

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(false)
          .noMoreThanTwice()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the freeholdResidential function for July 2020" when {
      "given a request with an effective date of 09/07/2020" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2020, 7, 30))
        val result = createResult("freeholdResidential, July2020 onwards")

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, None, None)
          .noMoreThanTwice()

        (mockFreeholdCalculationService.freeholdResidentialJuly20Onwards _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 08/07/2020" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, july2020EffectiveDate)
        val result = createResult("freeholdResidential, July2020 onwards")

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, None, *)
          .noMoreThanTwice()

        (mockFreeholdCalculationService.freeholdResidentialJuly20Onwards _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
      "given a request with an effective date of 08/7/2020 but with additional property eligibility check is false" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, july2020EffectiveDate)
        val result = createResult("freeholdResidential, July2020 onwards")


        (mockFreeholdCalculationService.freeholdResidentialJuly20Onwards _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(false)
          .noMoreThanTwice()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 31/3/2021 but with additional property eligibility check is false" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2021, 3, 31))
        val result = createResult("freeholdResidential, July2020 onwards")


        (mockFreeholdCalculationService.freeholdResidentialJuly20Onwards _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(false)
          .noMoreThanTwice()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the freeholdResidential function for December2014 to April2016" when {

      "given a request with an effective date of 31/3/2016" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2016, 3, 31))
        val result = createResult("freeholdResidential, December2014 to April2016")

        (mockFreeholdCalculationService.freeholdResidentialDec14Onwards _)
          .expects(testRequest, false, false)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/12/2014" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2014, 12, 4))
        val result = createResult("freeholdResidential, December2014 to April2016")

        (mockFreeholdCalculationService.freeholdResidentialDec14Onwards _)
          .expects(testRequest, false, false)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 1/4/2016 but with additional property eligibility check is false" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2016, 4, 1))
        val result = createResult("freeholdResidential, December2014 to April2016")

        (mockFreeholdCalculationService.freeholdResidentialDec14Onwards _)
          .expects(testRequest, false, false)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(false)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "throw an exception for freehold residential" when {
      "given a request with an effective date of 21/3/2012" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2012, 3, 21))

        the [InvalidDateException] thrownBy testCalculationService.calculateTax(testRequest) must have message s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"
      }
    }

                                           ///////////LEASEHOLD TESTS ///////////////

    "select the leaseholdNonResidential function for March2016 onwards" when {
      "given a request with an effective date of 1/1/2017" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.nonResidential,LocalDate.of(2017, 1, 1))
        val result = createResultInSeq("leaseholdNonResidential, March2016 onwards")

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 17/3/2016" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.nonResidential,LocalDate.of(2016, 3, 17))
        val result = createResultInSeq("leaseholdNonResidential, March2016 onwards")

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }
    }

    "select the leaseholdNonResidential function for March2012 to March2016" when {
      "given a request with an effective date of 16/3/2016" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.nonResidential,LocalDate.of(2016, 3, 16))
        val result = createResult("leaseholdNonResidential, March2012 to March2016")

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false, None)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/3/2011" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.nonResidential,LocalDate.of(2011, 3, 4))
        val result = createResult("leaseholdNonResidential, March2012 to March2016")

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false, None)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the leaseholdResidential November2017 first time buyer function" when {
      "given a request with an effective date of 22/11/2017 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2017, 11, 22))
        val result = createResult("leaseholdResidential, November2017 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialNov17OnwardsFTB _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 23/11/2017 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2017, 11, 23))
        val result = createResult("leaseholdResidential, November2017 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialNov17OnwardsFTB _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 30/11/2019 and the user is an individual without twoOrMoreProperties" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2019, 11, 30))
        val result = createResult("leaseholdResidential, November2017 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialNov17OnwardsFTB _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the leaseholdResidential additional property function for July2020 onwards" when {
      "given a request with an effective date of 30/11/2020,the user is an individual without twoOrMoreProperties but the premium is >500000" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2020, 11, 30), BigDecimal(500001))
        val result = createResultInSeq("leaseholdResidential, July2020 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialAddPropJuly20Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 11/7/2020 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2020, 7, 11))
        val result = createResultInSeq("leaseholdResidential, July2020 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialAddPropJuly20Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 08/7/2020 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, july2020EffectiveDate)
        val result = createResultInSeq("leaseholdResidential, July2020 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialAddPropJuly20Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }
    }

    "select the leaseholdResidential additional property function for April2016 onwards" when {
      "given a request with an effective date of 30/11/2019,the user is an individual without twoOrMoreProperties but the premium is >500000" in {
        val testRequest = createRequestWithPropDetails(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2019, 11, 30), BigDecimal(500001))
        val result = createResultInSeq("leaseholdResidential, April2016 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 2/4/2016 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2016, 4, 2))
        val result = createResultInSeq("leaseholdResidential, April2016 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 1/4/2016 and additional property rates check true" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2016, 4, 1))
        val result = createResultInSeq("leaseholdResidential, April2016 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(true)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(result)
      }
    }

    "select the leaseholdResidential function for July 2020 onwards" when {
      "given a request with an effective date of 31/7/2020" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2020, 7, 31))
        val result = createResult("leaseholdResidential, July2020 onwards")

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, None, *)
          .noMoreThanTwice()

        (mockLeaseholdCalculationService.leaseholdResidentialJuly20Onwards _)
          .expects(testRequest, false, None)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 11/7/2020" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, LocalDate.of(2020, 7, 11))
        val result = createResult("leaseholdResidential, July2020 onwards")


        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, None, *)
          .noMoreThanTwice()

        (mockLeaseholdCalculationService.leaseholdResidentialJuly20Onwards _)
          .expects(testRequest, false, None)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 08/7/2020 and additional property rates check false" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential, july2020EffectiveDate)
        val result = createResult("leaseholdResidential, July2020 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialJuly20Onwards _)
          .expects(testRequest, false, None)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(false)
          .noMoreThanTwice()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the leaseholdResidential function for December2014 to April2016" when {
      "given a request with an effective date of 31/3/2016" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2016, 3, 31))
        val result = createResult("leaseholdResidential, December2014 to April2016")

        (mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards _)
          .expects(testRequest, false, None, false)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/12/2014" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2014, 12, 4))
        val result = createResult("leaseholdResidential, December2014 to April2016")

        (mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards _)
          .expects(testRequest, false, None, false)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 1/4/2016 and additional property rates check false" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2016, 4, 1))
        val result = createResult("leaseholdResidential, December2014 to April2016")

        (mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards _)
          .expects(testRequest, false, None, false)
          .returns(result)
          .noMoreThanOnce()

        (mockAdditionalPropertyService.additionalPropertyRatesApply _)
          .expects(*, *, *)
          .returns(false)

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the leaseholdResidential function for March2012 to December2014" when {
      "given a request with an effective date of 3/12/2014" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2014, 12, 3))
        val result = createResult("leaseholdResidential, March2012 to December2014")

        (mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 22/3/2012" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2012, 3, 22))
        val result = createResult("leaseholdResidential, March2012 to December2014")

        (mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.calculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "throw an exception for leasehold residential" when {
      "given a request with an effective date of 21/3/2012" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2012, 3, 21))

        the [InvalidDateException] thrownBy testCalculationService.calculateTax(testRequest) must have message s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"
      }
    }
  }

  "checkFTB" must {
    "return true" when {
      "the user is an individual who does not own twoOrMoreProperties and the premium < 500000"in{
         val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
          )
         )
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(true), 499999) shouldBe true
      }
    }

    "return false" when{
      "the user is not an individual regardless of premium" in{
         val propertyDetails = Some(PropertyDetails(
          individual = false,
          twoOrMoreProperties = None,
          replaceMainResidence = None,
          sharedOwnership = None,
          currentValue = None
          )
         )
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(false), 499999) shouldBe false
      }

      "the user is an individual who does not own twoOrMoreProperties but FTB is false regardless of premium"in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(false), 500000) shouldBe false
      }

      "the user is an individual who owns twoOrMoreProperties regardless of premium" in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = None
         )
        )
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(false), 499999) shouldBe false
      }

      "the property details are not defined regardless of premium" in{
        val propertyDetails = None
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(false), 500000) shouldBe false
      }

      "the user is an individual who does not own twoOrMoreProperties but the premium > 500000"in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTB(propertyDetails, firstTimeBuyer = Some(true), 500001) shouldBe false
      }

    }
  }

  "checkFTBHigherThreshold" must {
    "return true" when {
      "the user is an individual who does not own twoOrMoreProperties and the premium < 625000"in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(true), 624000) shouldBe true
      }
    }

    "return false" when{
      "the user is not an individual regardless of premium" in{
        val propertyDetails = Some(PropertyDetails(
          individual = false,
          twoOrMoreProperties = None,
          replaceMainResidence = None,
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(false), 624000) shouldBe false
      }

      "the user is an individual who does not own twoOrMoreProperties but FTB is false regardless of premium"in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(false), 625000) shouldBe false
      }

      "the user is an individual who owns twoOrMoreProperties regardless of premium" in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(true),
          replaceMainResidence = Some(false),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(false), 624000) shouldBe false
      }

      "the property details are not defined regardless of premium" in{
        val propertyDetails = None
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(false), 625000) shouldBe false
      }

      "the user is an individual who does not own twoOrMoreProperties but the premium > 625000"in{
        val propertyDetails = Some(PropertyDetails(
          individual = true,
          twoOrMoreProperties = Some(false),
          replaceMainResidence = Some(true),
          sharedOwnership = None,
          currentValue = None
        )
        )
        testCalculationService.checkFTBHigherThreshold(propertyDetails, firstTimeBuyer = Some(true), 626000) shouldBe false
      }

    }
  }
  
}
