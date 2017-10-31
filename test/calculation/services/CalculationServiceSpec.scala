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

    val freeholdNonResAfter2016Result = Seq(
      Result(
        totalTax = 0,
        resultHeading = Some("Freehold Non-Resident after March 17 2016"),
        resultHint = None,
        npv = None,
        taxCalcs = Seq(calcDetails)
      )
    )

    val freeholdNonResBefore2016Result = Result(
        totalTax = 0,
        resultHeading = Some("Freehold Non-Resident before March 17 2016"),
        resultHint = None,
        npv = None,
        taxCalcs = Seq(calcDetails)
      )

    val freeholdResAfter2016Result = Seq(
      Result(
        totalTax = 0,
        resultHeading = Some("Freehold Resident after April 17 2016"),
        resultHint = None,
        npv = None,
        taxCalcs = Seq(calcDetails)
      )
    )

    val freeholdResAfter2014Result = Result(
        totalTax = 0,
        resultHeading = Some("Freehold Resident after December 4 2014"),
        resultHint = None,
        npv = None,
        taxCalcs = Seq(calcDetails)
      )

    val freeholdResAfter2012Result = Result(
      totalTax = 0,
      resultHeading = Some("Freehold Resident after March 22 2012"),
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
          .returns(freeholdNonResAfter2016Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(freeholdNonResAfter2016Result)
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
          .returns(freeholdNonResAfter2016Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(freeholdNonResAfter2016Result)
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
          .returns(freeholdNonResBefore2016Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(Seq(freeholdNonResBefore2016Result))
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
          .returns(freeholdNonResBefore2016Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(Seq(freeholdNonResBefore2016Result))
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
          .returns(freeholdNonResBefore2016Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(Seq(freeholdNonResBefore2016Result))
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
          .returns(freeholdResAfter2016Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(freeholdResAfter2016Result)
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
          .returns(freeholdResAfter2016Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(freeholdResAfter2016Result)
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
          .returns(freeholdResAfter2016Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(freeholdResAfter2016Result)
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
          .returns(freeholdResAfter2014Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(Seq(freeholdResAfter2014Result))
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
          .returns(freeholdResAfter2014Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(Seq(freeholdResAfter2014Result))
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
          .returns(freeholdResAfter2014Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(Seq(freeholdResAfter2014Result))
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
          .returns(freeholdResAfter2012Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(Seq(freeholdResAfter2012Result))
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
          .returns(freeholdResAfter2012Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(Seq(freeholdResAfter2012Result))
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
          .returns(freeholdResAfter2012Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(Seq(freeholdResAfter2012Result))
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
          .returns(freeholdResAfter2012Result)

        val selectRequest = testCalculationService.selectCalculationFunction(testRequest)

        selectRequest shouldBe CalculationResponse(Seq(freeholdResAfter2012Result))
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
  }
}
