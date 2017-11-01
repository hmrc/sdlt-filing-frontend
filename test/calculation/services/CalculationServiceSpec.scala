package calculation.services

import java.time.LocalDate

import calculation.enums.{CalcTypes, HoldingTypes, PropertyTypes, TaxTypes}
import calculation.exceptions.InvalidDateException
import calculation.models.{CalculationDetails, CalculationResponse, Request, Result}
import uk.gov.hmrc.play.test.UnitSpec
import org.scalamock.scalatest.MockFactory

class CalculationServiceSpec extends UnitSpec with MockFactory {

  val mockLeaseholdCalculationService = mock[LeaseholdCalculationService]
  val mockFreeholdCalculationService = mock[FreeholdCalculationService]
  val testCalculationService = new CalculationService(
    mockLeaseholdCalculationService,
    mockFreeholdCalculationService
  )

  "CalculateTax" should {
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

    def createRequest (hType: HoldingTypes.Value, pType: PropertyTypes.Value, eDate: LocalDate) =  Request(
      holdingType = hType,
      propertyType = pType,
      effectiveDate = eDate,
      premium = BigDecimal(0),
      highestRent = BigDecimal(0),
      propertyDetails = None,
      leaseDetails = None,
      relevantRentDetails = None
    )

    "select the freeholdNonResidential function for March2016 onwards" when {
      "given a request with an effective date of 1/1/2017" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.nonResidential, LocalDate.of(2017, 1, 1))
        val result = createResultInSeq("freeholdNonResidential, March2016 onwards")

        (mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 17/3/2016" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.nonResidential, LocalDate.of(2016, 3, 17))
        val result = createResultInSeq("freeholdNonResidential, March2016 onwards")

        (mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(result)
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

         testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/3/2011" in {
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.nonResidential, LocalDate.of(2011, 3, 4))
        val result = createResult("freeholdNonResidential, March2012 to March2016")

        (mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the freeholdResidential function for April2016 onwards" when {
      "given a request with an effective date of 2/4/2016" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2017, 4, 2))
        val result = createResultInSeq("freeholdResidential, April2016 onwards")

        (mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 1/4/2016" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2017, 4, 1))
        val result = createResultInSeq("freeholdResidential, April2016 onwards")

        (mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(result)
      }
    }

    "select the freeholdResidential function for December2014 to April2016" when {
      "given a request with an effective date of 31/3/2016" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential, LocalDate.of(2016, 3, 31))
        val result = createResult("freeholdResidential, December2014 to April2016")

        (mockFreeholdCalculationService.freeholdResidentialDec14Onwards _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/12/2014" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2014, 12, 4))
        val result = createResult("freeholdResidential, December2014 to April2016")

        (mockFreeholdCalculationService.freeholdResidentialDec14Onwards _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the freeholdResidential function for March2012 to December2014" when {
      "given a request with an effective date of 3/12/2014" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2014, 12, 3))
        val result = createResult("freeholdResidential, March2012 to December2014")

        (mockFreeholdCalculationService.freeholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 22/3/2012" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2012, 3, 22))
        val result = createResult("freeholdResidential, March2012 to December2014")

        (mockFreeholdCalculationService.freeholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

         testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "throw an exception for freehold residential" when {
      "given a request with an effective date of 21/3/2012" in{
        val testRequest = createRequest(HoldingTypes.freehold, PropertyTypes.residential,LocalDate.of(2012, 3, 21))

        the [InvalidDateException] thrownBy testCalculationService.CalculateTax(testRequest) should have message s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"
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

        testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 17/3/2016" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.nonResidential,LocalDate.of(2016, 3, 17))
        val result = createResultInSeq("leaseholdNonResidential, March2016 onwards")

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(result)
      }
    }


    "select the leaseholdNonResidential function for March2012 to March2016" when {
      "given a request with an effective date of 16/3/2016" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.nonResidential,LocalDate.of(2016, 3, 16))
        val result = createResult("leaseholdNonResidential, March2012 to March2016")

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/3/2011" in {
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.nonResidential,LocalDate.of(2011, 3, 4))
        val result = createResult("leaseholdNonResidential, March2012 to March2016")

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the leaseholdResidential function for April2016 onwards" when {
      "given a request with an effective date of 2/4/2016" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2016, 4, 2))
        val result = createResultInSeq("leaseholdResidential, April2016 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(result)
      }

      "given a request with an effective date of 1/4/2016" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2016, 4, 1))
        val result = createResultInSeq("leaseholdResidential, April2016 onwards")

        (mockLeaseholdCalculationService.leaseholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(result)
      }
    }

    "select the leaseholdResidential function for December2014 to April2016" when {
      "given a request with an effective date of 31/3/2016" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2016, 3, 31))
        val result = createResult("leaseholdResidential, December2014 to April2016")

        (mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/12/2014" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2014, 12, 4))
        val result = createResult("leaseholdResidential, December2014 to April2016")

        (mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards _)
          .expects(testRequest, false)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
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

        testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 22/3/2012" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2012, 3, 22))
        val result = createResult("leaseholdResidential, March2012 to December2014")

        (mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)
          .noMoreThanOnce()

        testCalculationService.CalculateTax(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "throw an exception for leasehold residential" when {
      "given a request with an effective date of 21/3/2012" in{
        val testRequest = createRequest(HoldingTypes.leasehold, PropertyTypes.residential,LocalDate.of(2012, 3, 21))

        the [InvalidDateException] thrownBy testCalculationService.CalculateTax(testRequest) should have message s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"
      }
    }
  }
}
