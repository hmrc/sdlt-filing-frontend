package calculation.services

import java.time.LocalDate

import calculation.enums.{CalcTypes, HoldingTypes, PropertyTypes, TaxTypes}
import calculation.exceptions.RequiredValueNotDefinedException
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

  def baseCalculationDetails(taxDue: Int, rate: Int) = CalculationDetails(
    taxType = TaxTypes.premium,
    calcType = CalcTypes.slab,
    detailHeading = None,
    bandHeading = None,
    detailFooter = None,
    taxDue = taxDue,
    rate = Some(rate),
    slices = None
  )

  "selectCalculationFunction" should {
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

    val resultInSeq = Seq(
      Result(
        totalTax = 0,
        resultHeading = Some("Some text"),
        resultHint = None,
        npv = None,
        taxCalcs = Seq(calcDetails)
      )
    )

    val result = Result(
        totalTax = 0,
        resultHeading = Some("Some text"),
        resultHint = None,
        npv = None,
        taxCalcs = Seq(calcDetails)
    )

    "select the freeholdNonResidential function for March2016 onwards" when {
      "given a request with an effective date of 1/1/2017" in {
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2017, 1, 1),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(resultInSeq)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(resultInSeq)
      }

      "given a request with an effective date of 17/3/2016" in {
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2016, 3, 17),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(resultInSeq)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(resultInSeq)
      }
    }


    "select the freeholdNonResidential function for March2012 to March2016" when {
      "given a request with an effective date of 16/3/2016" in {
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2016, 3, 16),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false)
          .returns(result)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 2/1/2015" in {
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2015, 1, 2),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false)
          .returns(result)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/3/2011" in {
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2011, 3, 4),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false)
          .returns(result)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the freeholdResidential function for April2016 onwards" when {
      "given a request with an effective date of 6/5/2017" in{
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2017, 5, 6),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(resultInSeq)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(resultInSeq)
      }

      "given a request with an effective date of 2/4/2016" in{
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2016, 4, 2),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(resultInSeq)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(resultInSeq)
      }

      "given a request with an effective date of 1/4/2016" in{
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2016, 4, 1),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(resultInSeq)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(resultInSeq)
      }
    }

    "select the freeholdResidential function for December2014 to April2016" when {
      "given a request with an effective date of 31/3/2016" in{
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2016, 3, 31),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdResidentialDec14Onwards _)
          .expects(testRequest, false)
          .returns(result)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 5/12/2014" in{
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 12, 5),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdResidentialDec14Onwards _)
          .expects(testRequest, false)
          .returns(result)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/12/2014" in{
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 12, 4),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdResidentialDec14Onwards _)
          .expects(testRequest, false)
          .returns(result)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the freeholdResidential function for March2012 to December2014" when {
      "given a request with an effective date of 3/12/2014" in{
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 12, 3),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 16/8/2013" in{
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2013, 8, 16),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 23/3/2012" in{
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2012, 3, 23),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 22/3/2012" in{
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2012, 3, 22),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockFreeholdCalculationService.freeholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)

         testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "throw an exception for freehold residential" when {
      "given a request with an effective date of 21/3/2012" in{
        val testRequest = Request(
          holdingType = HoldingTypes.freehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2012, 3, 21),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        the [RequiredValueNotDefinedException] thrownBy testCalculationService.selectCalculationFunction(testRequest) should have message s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"
      }
    }

///////////////////////////////////////////////////////////////////////

    "select the leaseholdNonResidential function for March2016 onwards" when {
      "given a request with an effective date of 1/1/2017" in {
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2017, 1, 1),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(resultInSeq)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(resultInSeq)
      }

      "given a request with an effective date of 17/3/2016" in {
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2016, 3, 17),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar16Onwards _)
          .expects(testRequest)
          .returns(resultInSeq)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(resultInSeq)
      }
    }


    "select the leaseholdNonResidential function for March2012 to March2016" when {
      "given a request with an effective date of 16/3/2016" in {
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2016, 3, 16),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false)
          .returns(result)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 2/1/2015" in {
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2015, 1, 2),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false)
          .returns(result)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/3/2011" in {
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.nonResidential,
          effectiveDate = LocalDate.of(2011, 3, 4),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdNonResidentialMar12toMar16 _)
          .expects(testRequest, false)
          .returns(result)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the leaseholdResidential function for April2016 onwards" when {
      "given a request with an effective date of 6/5/2017" in{
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2017, 5, 6),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(resultInSeq)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(resultInSeq)
      }

      "given a request with an effective date of 2/4/2016" in{
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2016, 4, 2),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(resultInSeq)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(resultInSeq)
      }

      "given a request with an effective date of 1/4/2016" in{
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2016, 4, 1),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdResidentialAddPropApr16Onwards _)
          .expects(testRequest)
          .returns(resultInSeq)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(resultInSeq)
      }
    }

    "select the leaseholdResidential function for December2014 to April2016" when {
      "given a request with an effective date of 31/3/2016" in{
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2016, 3, 31),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards _)
          .expects(testRequest, false)
          .returns(result)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 5/12/2014" in{
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 12, 5),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards _)
          .expects(testRequest, false)
          .returns(result)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 4/12/2014" in{
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 12, 4),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdResidentialDec14Onwards _)
          .expects(testRequest, false)
          .returns(result)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "select the leaseholdResidential function for March2012 to December2014" when {
      "given a request with an effective date of 3/12/2014" in{
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2014, 12, 3),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 16/8/2013" in{
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2013, 8, 16),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 23/3/2012" in{
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2012, 3, 23),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }

      "given a request with an effective date of 22/3/2012" in{
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2012, 3, 22),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        (mockLeaseholdCalculationService.leaseholdResidentialMar12toDec14 _)
          .expects(testRequest)
          .returns(result)

        testCalculationService.selectCalculationFunction(testRequest) shouldBe CalculationResponse(Seq(result))
      }
    }

    "throw an exception for leasehold residential" when {
      "given a request with an effective date of 21/3/2012" in{
        val testRequest = Request(
          holdingType = HoldingTypes.leasehold,
          propertyType = PropertyTypes.residential,
          effectiveDate = LocalDate.of(2012, 3, 21),
          premium = BigDecimal(0),
          highestRent = BigDecimal(0),
          propertyDetails = None,
          leaseDetails = None
        )

        the [RequiredValueNotDefinedException] thrownBy testCalculationService.selectCalculationFunction(testRequest) should have message s"Date of ${testRequest.effectiveDate} is invalid or before 22/3/2012"
      }
    }

  }
}
